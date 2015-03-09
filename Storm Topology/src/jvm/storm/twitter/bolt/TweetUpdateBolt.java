package storm.twitter.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import storm.twitter.tools.Tool;

import java.util.Map;

/**
 * {@inheritDoc}
 *
 * The TweetUpdateBolt gets data from the TwitterSearchSpout. It checks whether the input tweet is new or it is
 * already exists in the database.
 *
 * @see backtype.storm.topology.base.BaseRichBolt
 * @author achueva
 * @since 8/14/14
 */
public class TweetUpdateBolt extends BaseRichBolt {

    private OutputCollector collector;
    private final String mongoHost;
    private final int mongoPort;
    private final String mongoDbName;
    private DBCollection mongoCollTweets;
    private DBCollection mongoCollUsers;
    private DB mongoDB;
    private MongoClient mongoClient;

    /**
     * The method defines private objects.
     *
     * @param mongoDbName The name of the database in MongoDB
     */
    public TweetUpdateBolt(String mongoDbName) {
        this.mongoHost = Tool.MONGO_IP;
        this.mongoPort = Tool.MONGO_PORT;
        this.mongoDbName = mongoDbName;
    }

    /**
     * {@inheritDoc}
     *
     * The method establishes database connection.
     *
     * @param map The Storm configuration for the bolt.
     * @param topologyContext The context contains information about the place of a task within the topology,
     *            including the task id, the component id of a task, input and output information, etc.
     * @param collector The collector is used to emit tuples from the bolt to send them to next bolt.
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
        try {
            this.mongoClient = new MongoClient(mongoHost, mongoPort);
            this.mongoDB = mongoClient.getDB(mongoDbName);
            this.mongoCollTweets = mongoDB.getCollection("tweets");
            this.mongoCollUsers = mongoDB.getCollection("users");
        } catch (Exception e) {
            System.out.print("Mongo ERROR: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The method processes the incoming tuple. It extracts a tweet from the tuple and checks it in the database.
     * If the tweet already exists then its information is updated and nothing is emitted to an output stream.
     * If the tweet is new then it is emitted to the output stream.
     *
     * @param input
     */
    @Override
    public void execute(Tuple input) {
        // Get tweet from Tuple
        String JSONString = input.getStringByField("tweet");
        JSONObject tweet = null;
        // Convert string to jsonObject
        try {
            tweet = (JSONObject)new JSONParser().parse(JSONString);
        } catch (ParseException e) {
            System.out.print("Error: " + e.toString());
        }
        boolean exists = false;
        // Check the tweet in DB
        try {
            DBCursor cursor = mongoCollTweets.find(new BasicDBObject("id", tweet.get("id")));
            try {
                if(cursor.hasNext()) {
                    exists = true;
                }
                else {
                    exists = false;
                }
            } finally {
                cursor.close();
            }
        } catch (MongoException me) {
            me.printStackTrace();
        }

        if (exists){
            // Extract user info from the tweet
            JSONObject user = (JSONObject) tweet.get("user");
            user.remove("screen_name");
            JSONObject tweetUpdate = new JSONObject();
            tweetUpdate.put("retweet_count", tweet.get("retweet_count"));
            tweetUpdate.put("favorite_count", tweet.get("favorite_count"));
            tweetUpdate.put("id", tweet.get("id"));
            DBObject dbObjectUser = (DBObject) JSON.parse(user.toString());
            DBObject dbObjectUpdateTweet = (DBObject) JSON.parse(tweetUpdate.toString());
            // Update tweet info in DB (retweet_count, favorite_count, etc.)
            if (dbObjectUpdateTweet != null) {
                try {
                    BasicDBObject obj = new BasicDBObject("$set", dbObjectUpdateTweet);
                    WriteResult res = mongoCollTweets.update(new BasicDBObject("id", tweet.get("id")),
                            obj, true, false, new WriteConcern(1));
                    if (!res.isUpdateOfExisting()) {
                        System.out.print("ERROR!");
                    }
                    collector.ack(input);
                } catch (MongoException me) {
                    collector.fail(input);
                }
            }
            // Update user info in DB (listed_count, statuses_count, etc.)
            if (dbObjectUser != null) {
                try {
                    mongoCollUsers.update(new BasicDBObject("id", user.get("id")),
                                new BasicDBObject("$set", dbObjectUser), true, false, new WriteConcern(1));
                    collector.ack(input);
                } catch (MongoException me) {
                    collector.fail(input);
                }
            }
        }
        else {
            // If tweet doetn't exist in DB then emit tweet info to collector
            collector.emit(new Values(input.getStringByField("tweet")));
        }
    }

    /**
     * {@inheritDoc}
     *
     * The method closes the MongoDB database connection.
     */
    @Override
    public void cleanup() {
        this.mongoClient.close();
    }

    /**
     * The method declares the output schema for the output stream of a bolt. The output stream consists of JSON objects.
     * Each of them contains a tweet.
     *
     * @param declarer The declarer is used to declare details of an output stream
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweet"));
    }
}