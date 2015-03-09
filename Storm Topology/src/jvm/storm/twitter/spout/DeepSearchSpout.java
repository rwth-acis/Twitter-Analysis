package storm.twitter.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@inheritDoc}
 *
 *  The class gets all possible historical data (for 1 week) from Twitter Search API and emits it
 *  to an output stream.
 *
 * @see backtype.storm.topology.base.BaseRichSpout
 * @author achueva
 * @since 10/1/14
 */
public class DeepSearchSpout extends BaseRichSpout {

   private SpoutOutputCollector collector;
    private ConfigurationBuilder cb;
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String[] keyWords;
    private String keyWord;
    private Long maxID = 0L;
    private int requestDepth = 180;
    private Twitter twitter;

    /**
     * The method defines private objects.
     *
     * @param consumerKey The consumer key for Twitter API
     * @param consumerSecret The consumer secret for Twitter API
     * @param accessToken The access token for Twitter API
     * @param accessTokenSecret The access token secret for Twitter API
     * @param keyWords The set of words that are used as a filter for Twitter Stream API
     */
    public DeepSearchSpout(String consumerKey, String consumerSecret,
                              String accessToken, String accessTokenSecret, String[] keyWords) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.keyWords = keyWords;
    }

    /**
     * {@inheritDoc}
     *
     * The method receives tweets from Twitter Search API and emits them to the output stream.
     *
     * <p>The method is called when a task for this component is initialized within a worker on the cluster.
     * It provides the spout with the environment in which the spout executes.
     *
     * @param conf The configuration of Apache Storm for the spout.
     * @param context The context contains information about the place of a task in the topology. It contains
     *                information about the task id, component id, I/O information, etc.
     * @param coll The collector is used to emit tuples to the output stream of the spout.
     */
    @Override
    public void open(Map conf, TopologyContext context,
                     SpoutOutputCollector coll) {

        collector = coll;

        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        StringBuilder builder = new StringBuilder();
        for (String s : keyWords) {
            builder.append(s);
        }
        keyWord = builder.toString();
        TwitterFactory tf = new TwitterFactory(cb.setJSONStoreEnabled(true).build());
        twitter = tf.getInstance();

        Timer timer = new Timer();
        TimerTask QuaterHourTask = new TimerTask() {
            @Override
            public void run() {

                int j = 0;
                try {
                    for (int i = 0; i < requestDepth; i++) {
                        System.out.print("ROUND: " + i);
                        Query query;
                        if (maxID == 0) {
                            System.out.print("MAXID 0");
                            query = new Query(keyWord).count(100);
                        } else {
                            query = new Query(keyWord).count(100).maxId(maxID);
                            System.out.print("MAXID");
                            System.out.print(maxID);
                        }
                        QueryResult result;
                        result = twitter.search(query);
                        List<Status> tweets = result.getTweets();
                        for (Status tweet : tweets) {
                            maxID = tweet.getId();
                            System.out.print("MAXID: " + maxID);
                            System.out.print(tweet.getId());
                            String json = TwitterObjectFactory.getRawJSON(tweet);
                            collector.emit(new Values(json));
                            j++;
                        }
                    }
                    System.out.print("ITOGO: " + j);
                } catch (TwitterException te) {
                    System.out.println("Failed to search tweets: " + te.getMessage());
                    te.printStackTrace();
                    System.out.println("Failed to search tweets: " + te.getMessage());

                }
            }
        };
        timer.schedule(QuaterHourTask, 0, 1000 * 60 * 15);
    }

    /**
     * {@inheritDoc}
     *
     * The method requests the spout to emit tuples to the output collector. The method sleeps for a big amount of
     * time to avoid wasting too much CPU time.
     */
    @Override
    public void nextTuple() {
        Utils.sleep(500000000);
    }

    /**
     * {@inheritDoc}
     *
     * The method declares the output schema for all the streams of the topology: the output object contains one
     * field named "tweet".
     *
     * @param declarer The declarer contains information about output stream ids, output fields, etc.
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweet"));
    }

}
