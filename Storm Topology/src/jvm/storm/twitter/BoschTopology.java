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
 *  The class specifies a topology for the Bosch event monitoring.
 *
 * @author achueva
 * @since 11/25/14
 */
public class BoschTopology {
    public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException {
        String consumerKey = "<Your Twitter Consumer Key>";
        String consumerSecret = "<Your Twitter Consumer Secret>";
        String accessToken = "Your Twitter Access Token";
        String accessTokenSecret = "Your Twitter Access Token Secret";
        String[] keyWords = new String[1];
        keyWords[0] = "#bosch";

        TopologyBuilder builder = new TopologyBuilder();

        String dbName = "bosch_evaluation";
        String eventName = "Bosch";
        String description = "Bosch monitoring";

        builder.setSpout("tweetSearch", new TwitterSearchSpout(consumerKey, consumerSecret, accessToken,
                accessTokenSecret, keyWords, dbName));
        builder.setBolt("tweetsUpdate", new TweetUpdateBolt(dbName), 2).shuffleGrouping("tweetSearch");
        builder.setSpout("batchHour", new BatchSpout(dbName, eventName, "bosch"));

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
            cluster.submitTopology("Bosch", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        }
    }
}
