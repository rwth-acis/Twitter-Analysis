package storm.twitter.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.mongodb.*;
import storm.twitter.tools.Tool;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@inheritDoc}
 *
 *  The class gets data from Twitter Search API every 15 minutes and emits it to an output stream.
 *
 * @see backtype.storm.topology.base.BaseRichSpout
 * @author achueva
 * @since 7/2/14
 */
public class TwitterSearchSpout extends BaseRichSpout {

    private final String mongoHost;
    private final int mongoPort;
    private final String mongoDbName;
    private SpoutOutputCollector collector;
    private ConfigurationBuilder cb;
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String[] keyWords;
    private String keyWord;
    private Long maxID = 0L;
    private Long lastTweetId = 0L;
    private int requestDepth = 10;
    private Twitter twitter;
    private DBCollection mongoCollTweets;
    private MongoClient mongoClient;
    private DB mongoDB;

    /**
     * The method defines private objects.
     *
     * @param consumerKey The consumer key for Twitter API
     * @param consumerSecret The consumer secret for Twitter API
     * @param accessToken The access token for Twitter API
     * @param accessTokenSecret The access token secret for Twitter API
     * @param keyWords The set of words that are used as a filter for Twitter Stream API
     * @param mongoDbName The name of the database in MongoDB
     */
    public TwitterSearchSpout(String consumerKey, String consumerSecret,
                              String accessToken, String accessTokenSecret, String[] keyWords,String mongoDbName) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.keyWords = keyWords;
        this.mongoHost = Tool.MONGO_IP;
        this.mongoPort = Tool.MONGO_PORT;
        this.mongoDbName = mongoDbName;

    }

    /**
     * {@inheritDoc}
     *
     * The method receives tweets from Twitter Search API and emits them to the output stream. It requests tweets
     * that are older than the first tweet about an event collected to the database.
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
        // configurations for Twitter API
        cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        StringBuilder builder = new StringBuilder();
        // build filter string
        for (String s : keyWords) {
            builder.append(s);
        }
        keyWord = builder.toString();
        TwitterFactory tf = new TwitterFactory(cb.setJSONStoreEnabled(true).build());
        twitter = tf.getInstance();

        // Check tweet's updates every 15 minutes
        Timer timer = new Timer();
        TimerTask QuaterHourTask = new TimerTask() {
            @Override
            public void run() {
                if (lastTweetId == 0) {
                    // Open MongoDB
                    try {
                        mongoClient = new MongoClient(mongoHost, mongoPort);
                        mongoDB = mongoClient.getDB(mongoDbName);
                        mongoCollTweets = mongoDB.getCollection("tweets");
                        // if tweets exist in database -> get mit tweet id
                        //db.tweets.find({},{_id:0, id:1}).sort({id:1}).limit(1)
                        DBCursor cursor = mongoCollTweets.find(new BasicDBObject())
                                .sort(new BasicDBObject("id", 1)).limit(1);
                        if (cursor.hasNext()) {
                            BasicDBObject obj = (BasicDBObject) cursor.next();
                            lastTweetId = obj.getLong("id");
                            System.out.print("LAST ID: " + lastTweetId);
                        }
                        mongoClient.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                int j = 0;
                maxID = 0L;
                boolean searchDeeper;
                if (lastTweetId != 0)
                    searchDeeper = true;
                else
                    searchDeeper = false;
                try {
                    for (int i = 0; (i < requestDepth) && (searchDeeper); i++) {
                        System.out.print("ROUND: " + i);
                        Query query;
                        if (maxID == 0) {
                            System.out.print("MAXID 0");
                            query = new Query(keyWord).count(100);
                        } else {
                            query = new Query(keyWord).count(100).maxId(maxID);
                        }
                        QueryResult result;
                        // Get tweets
                        result = twitter.search(query);
                        List<Status> tweets = result.getTweets();
                        // Go through the tweets received by query
                        for (Status tweet : tweets) {
                            if (tweet.getId() > lastTweetId) {
                                // emit tweet to output stream
                                if (maxID > tweet.getId() || maxID == 0) {
                                    maxID = tweet.getId();
                                }
                                String json = TwitterObjectFactory.getRawJSON(tweet);
                                collector.emit(new Values(json));
                                j++;
                            } else {
                                searchDeeper = false;
                            }
                        }
                    }
                } catch (TwitterException te) {
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
