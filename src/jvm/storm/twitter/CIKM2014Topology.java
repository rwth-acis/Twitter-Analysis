package storm.twitter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import storm.twitter.bolt.AnalysisBolt;
import storm.twitter.bolt.MongoSaveBolt;
import storm.twitter.bolt.TweetUpdateBolt;
import storm.twitter.spout.BatchSpout;
import storm.twitter.spout.TwitterSampleSpout;
import storm.twitter.spout.TwitterSearchSpout;

/**
 * {@inheritDoc}
 *
 *  The class specifies a topology for CIKM2014 conference monitoring.
 *
 * @author achueva
 * @since 11/3/14
 */
public class CIKM2014Topology {

    public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException {
        String consumerKey = "<Your Twitter Consumer Key>";
        String consumerSecret = "<Your Twitter Consumer Secret>";
        String accessToken = "Your Twitter Access Token";
        String accessTokenSecret = "Your Twitter Access Token Secret";
        String[] keyWords = new String[1];
        keyWords[0] = "cikm2014";

        TopologyBuilder builder = new TopologyBuilder();

        String dbName = "cikm2014";
        String eventName = "CIKM2014";
        String description = "Conference 2014";

        builder.setSpout("tweetSearch", new TwitterSearchSpout(consumerKey, consumerSecret, accessToken,
                accessTokenSecret, keyWords, dbName));
        builder.setBolt("tweetsUpdate", new TweetUpdateBolt(dbName), 2).shuffleGrouping("tweetSearch");
        builder.setSpout("batchHour", new BatchSpout(dbName, eventName, keyWords[0]));

        builder.setSpout("twitterStream", new TwitterSampleSpout(consumerKey, consumerSecret,
                accessToken, accessTokenSecret, keyWords));
        builder.setBolt("sentimentCount", new AnalysisBolt(), 2).shuffleGrouping("twitterStream").shuffleGrouping("tweetsUpdate");
        builder.setBolt("save", new MongoSaveBolt(dbName, eventName, description), 2).shuffleGrouping("sentimentCount");

        Config conf = new Config();

        conf.setDebug(true);

        if (args != null && args.length > 0) {
            conf.setNumWorkers(1);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        }
        else {
            conf.setMaxTaskParallelism(1);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("CIKM2014", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        }
    }
}
