package storm.twitter.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.simple.JSONObject;
import storm.twitter.tools.Tool;
import java.util.*;

/**
 * {@inheritDoc}
 *
 * The class stores all collected and analyzed information in the MongoDB database.
 *
 * The MongoSaveBolt does not output any tuples.
 *
 * @see backtype.storm.topology.base.BaseRichBolt
 * @author achueva
 * @since 7/30/14
 *
 */
public class MongoSaveBolt extends BaseRichBolt {

    private OutputCollector collector;
    //private Tool tool;

    private final String mongoHost;
    private final int mongoPort;
    private final String mongoDbName;
    private DBCollection mongoCollTerms;
    private DBCollection mongoCollUsers;
    private DBCollection mongoCollTweets;
    private DBCollection mongoCollEvent;
    private String description;
    private DB mongoDB;
    private MongoClient mongoClient;
    private String eventName;

    /**
     * The method defines private objects.
     *
     * @param mongoDbName The name of a database
     * @param eventName The name of an event
     * @param description The description of an event
     */
    public MongoSaveBolt(String mongoDbName, String eventName, String description) {
        this.mongoHost = Tool.MONGO_IP;
        this.mongoPort = Tool.MONGO_PORT;
        this.mongoDbName = mongoDbName;
        this.eventName = eventName;
        this.description = description;
    }

    /**
     * {@inheritDoc}
     * The method establishes connection with the MongoDB database.
     *
     * @param map The Storm configuration for the bolt.
     * @param topologyContext The context contains information about the place of a task within the topology,
     *            including the task id, the component id of a task, input and output information, etc.
     * @param cltr The collector is used to emit tuples from the bolt to send them to next bolt.
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector cltr) {
        this.collector = cltr;
        try {
            this.mongoClient = new MongoClient(mongoHost, mongoPort);
            this.mongoDB = mongoClient.getDB(mongoDbName);
            this.mongoCollTerms = mongoDB.getCollection("terms");
            this.mongoCollUsers = mongoDB.getCollection("users");
            this.mongoCollTweets = mongoDB.getCollection("tweets");
            this.mongoCollEvent = mongoDB.getCollection("event");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * The method receives tweets from incoming tuples. Each of incoming tuple contains exactly one tweet.
     * <p>The method restructures a tweet into tweet and user objects and analyses the text of a tweet:
     * <ul>
     *              <li>Stores information about mentions to the "users" collection</li>
     *              <li>Saves all words that are not slang and stop words to the "terms" collection with their relationships.</li>
     *              <li>Cleans the text of the tweet from slang and stop words and adds it to a tweet JSON object in the field "clean_text"</li>
     * </ul>
     * <p>The method calculates nutrition values of terms, tweet, user, and an event and stores them to the database.
     * <p>It also updates event information in the database.
     *
     *
     * @param input The incoming tuple that contains a tweet
     */
    @Override
    public void execute(Tuple input) {

        // Get tweet from input
        JSONObject tweet = (JSONObject) input.getValueByField("tweet");

        // Restructure tweet for tweets and users collections
        boolean engLang = false;
        if (tweet.get("lang").equals("en")) {
            engLang = true;
        }
        JSONObject user = (JSONObject) tweet.get("user");
        String username = user.get("screen_name").toString();
        username = username.toLowerCase();
        tweet.remove("user");
        tweet.put("user", username);
        user.remove("screen_name");
        user.put("screen_name", username);

        // Structures for data collection
        double nutrition = 0;
        Map<String, Integer> termsOccur = new HashMap<String, Integer>();
        String text = tweet.get("text").toString();
        List<String> outLinks = new ArrayList<String>();
        List<String> words = Arrays.asList(text.split("\\s+"));
        List<String> uniqueWords = new ArrayList<String>(new HashSet<String>(words));
        List<String> terms = new ArrayList<String>();

        // Go through unique words of the tweet
        for (int i = 0; i < uniqueWords.size(); i++) {
            String cleanWord = uniqueWords.get(i);
            // Check if the word is no a stop word or slang
            if (!cleanWord.startsWith("http")) {
                cleanWord = cleanWord.toLowerCase();
                cleanWord = cleanWord.replaceAll("[,.;:]", "");
                // the word is a username
                if (cleanWord.startsWith("@")) {
                    // if it is not a self-mention
                    if (!cleanWord.substring(1, cleanWord.length()).equals(username)) {
                        // delete @ symbol
                        outLinks.add(cleanWord.substring(1, cleanWord.length()));
                        // add mention statistics to DB
                        BasicDBObject inc = new BasicDBObject("in_links.".concat(user.get("screen_name").toString()), 1);
                        inc.put("mentioned", 1);
                        BasicDBObject mentionedUserUpdate = new BasicDBObject("$inc", inc);
                        try {
                            mongoCollUsers.update(new BasicDBObject("screen_name", cleanWord.substring(1, cleanWord.length())),
                                    mentionedUserUpdate, true, false, new WriteConcern(1));
                        } catch (MongoException me) {
                            System.out.print("MONGO ERROR: " + me.toString());
                        }
                    }
                } else {
                    // if the language of a tweet is english => collect information about terms relationships
                    if (engLang) {
                        cleanWord = cleanWord.replaceAll("[^a-zA-Z0-9]", "");
                        if (!Arrays.asList(Tool.slangWords).contains(cleanWord)
                                && !Arrays.asList(Tool.stopWords).contains(cleanWord)) {
                            if (cleanWord.length() > 0) {
                                try {
                                    //db.terms.update({term:"test"},{$inc:{occurrence:1}},{upsert:1})
                                    BasicDBObject inc = new BasicDBObject("occurrence", 1);
                                    mongoCollTerms.update(new BasicDBObject("term", cleanWord), new BasicDBObject("$inc", inc), true, false);
                                    // Get occurrence for nutrition estimation
                                    DBCursor cursor = mongoCollTerms.find(new BasicDBObject("term", cleanWord));
                                    int occur = 0;
                                    try {
                                        if (cursor.hasNext()) {
                                            occur = (Integer) cursor.next().get("occurrence");
                                        }
                                    } finally {
                                        cursor.close();
                                    }
                                    termsOccur.put(cleanWord, occur);
                                } catch (MongoException me) {
                                    System.out.print("MONGO ERROR: " + me.toString());
                                }
                                terms.add(cleanWord);
                            }
                        }
                    }
                }
            }
        }

        // Save terms' links

        // Get only unique terms
        List<String> uniqueTerms = new ArrayList<String>(new HashSet<String>(terms));
        // create string from terms array
        StringBuilder sb = new StringBuilder("");
        for (String term : terms) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(term);
        }
        if (sb.length() > 0) {
            sb.append(".");
            String cleanText = sb.toString();
            tweet.put("clean_text", cleanText);
        }

        for (int i = 0; i < uniqueTerms.size(); i++) {
            BasicDBList linksList = new BasicDBList();
            for (int j = 0; j < uniqueTerms.size(); j++) {
                if (i != j) {
                    // Try to increment cooccurrence
                    //db.terms.update({"term":"t1", "links.term":"t2"}, {$inc:{"links.$.co_occur":1}})
                    BasicDBObject find = new BasicDBObject("term", uniqueTerms.get(i));
                    find.put("links.term", uniqueTerms.get(j));
                    BasicDBObject update = new BasicDBObject("links.$.co_occur", 1);
                    BasicDBObject inc = new BasicDBObject("$inc", update);
                    try {
                        WriteResult result = mongoCollTerms.update(find, inc);
                        // Check if updated
                        if (result.getN() == 0) {
                            BasicDBObject link = new BasicDBObject("term", uniqueTerms.get(j));
                            link.put("co_occur", 1);
                            linksList.add(link);
                        }
                    } catch (MongoException me) {
                        collector.fail(input);
                    }
                }
            }
            // if there are new links between therms, then $addToSet construction
            if (linksList.size() != 0) {
                //db.terms.update({"term":"t1"},
                //                {$addToSet:{links:{$each:[{term:"t5", occurrence:1},
                //                                          {term:"t6", occurrence:1}]}}})
                BasicDBObject find = new BasicDBObject("term", uniqueTerms.get(i));
                BasicDBObject each = new BasicDBObject("$each", linksList);
                BasicDBObject links = new BasicDBObject("links", each);
                BasicDBObject addToSet = new BasicDBObject("$addToSet", links);
                try {
                    mongoCollTerms.update(find, addToSet);
                } catch (MongoException me) {
                    collector.fail(input);
                }
            }
        }

        // Tweet's nutrition calculation
        int maxOccur = 0;
        for (String key : termsOccur.keySet()) {
            if (termsOccur.get(key) > maxOccur) {
                maxOccur = termsOccur.get(key);
            }
        }
        for (String key : termsOccur.keySet()) {
            nutrition = nutrition + 0.5 + 0.5 * (termsOccur.get(key) / maxOccur);
        }
        tweet.put("nutrition", nutrition);
        DBObject dbObjectTweet = (DBObject) JSON.parse(tweet.toString());
        DBObject dbObjectUser = (DBObject) JSON.parse(user.toString());

        // Save tweet
        if (dbObjectTweet != null) {
            try {
                mongoCollTweets.save(dbObjectTweet, new WriteConcern(1));
                collector.ack(input);
            } catch (MongoException me) {
                collector.fail(input);
            }
        }

        // create object for event collection update
        try {
            Double sentiment = (Double) tweet.get("sentiment_score");
            BasicDBObject inc = new BasicDBObject("nutrition", nutrition);
            inc.put("nutrition_day", nutrition);
            inc.put("nutrition_sum", nutrition);
            inc.put("sentiment_score", sentiment);
            inc.put("sentiment_day", sentiment);
            inc.put("sentiment_sum", sentiment);
            inc.put("tweets_day", 1);
            inc.put("tweets_sum", 1);
            inc.put("tweets_count", 1);

            // sentiment positive/negative/neutral
            if (sentiment > 0.15) {
                inc.put("sentiment_positive_sum", sentiment);
                inc.put("sentiment_positive_amount", 1);
            } else {
                if (sentiment < -0.15) {
                    inc.put("sentiment_negative_sum", sentiment);
                    inc.put("sentiment_negative_amount", 1);
                } else {
                    inc.put("sentiment_neutral_sum", sentiment);
                    inc.put("sentiment_neutral_amount", 1);
                }
            }
            // Save event's sentiment
            mongoCollEvent.update(new BasicDBObject("_id", eventName), new BasicDBObject("$inc", inc).append("$set", new BasicDBObject("description", description)), true, false, new WriteConcern(1));
        } catch (MongoException me) {
            collector.fail(input);
        }

        // Save user
        if (dbObjectUser != null) {
            BasicDBObject upd = new BasicDBObject("$set", dbObjectUser);
            BasicDBObject inc = new BasicDBObject("tweets_count", 1);
            inc.put("sentiment_score", tweet.get("sentiment_score"));
            inc.put("nutrition", nutrition);
            for (int i = 0; i < outLinks.size(); i++) {
                inc.put("out_links.".concat(outLinks.get(i)), 1);
            }
            upd.append("$inc", inc);
            try {
                //mongoCollLog.save(new BasicDBObject("request", upd.toString()));
                mongoCollUsers.update(new BasicDBObject("screen_name", user.get("screen_name").toString()),
                        upd, true, false);
                collector.ack(input);
            } catch (MongoException me) {
                collector.fail(input);
            }
        }
    }

    // Declare output fields

    /**
     * {@inheritDoc}
     *
     * The method declares the output schema for the output stream of a bolt. The bolt MongoSaveBolt does not have an
     * output stream, therefore no schema is declared.
     *
     * @param declarer The declarer is used to declare details of an output stream
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
