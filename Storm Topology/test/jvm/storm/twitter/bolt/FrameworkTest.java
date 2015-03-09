package storm.twitter.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import storm.twitter.tools.MockTupleHelpersAnalysis;
import storm.twitter.tools.Tool;
import java.util.List;
import java.util.Map;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import org.junit.runners.MethodSorters;

import org.junit.FixMethodOrder;

/**
 * Created by achueva on 11/11/14.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FrameworkTest {

    private static final String EVENT_NAME = "unit tests";
    private static final String DESCRIPTION = "Test event for unit tests";

    MongoClient mongoClient;
    DB mongoDB;
    DBCollection mongoCollUsers;
    DBCollection mongoCollTweets;
    DBCollection mongoCollTerms;
    DBCollection mongoCollUserTypes;
    DBCollection mongoCollEvent;
    DBCollection mongoCollHourlyStats;
    DBCollection mongoCollDailyStats;
    DBCollection mongoCollTweetSources;
    DBCollection mongoCollTweetLanguages;
    DBCollection mongoCollPopularVideo;
    DBCollection mongoCollPopularLinks;
    DBCollection mongoCollPopularPics;
    DBCollection mongoCollTweetsContent;
    DBCollection mongoCollTermNetwork;
    DBCollection mongoCollTopics;
    DBCollection mongoCollMostMentionedUsers;
    DBCollection mongoCollMostFollowedUsers;
    DBCollection mongoCollMostAuthUsers;
    DBCollection mongoCollLanguages;
    DBCollection mongoCollUserNetwork;

    private static final String STREAM = "tweet";

    private Tuple mockTweetTuple(String TupleOutput) {
        Tuple tuple = MockTupleHelpersAnalysis.mockTuple(TupleOutput, STREAM);
        when(tuple.getStringByField("tweet")).thenReturn(TupleOutput);
        when(tuple.getString(0)).thenReturn(TupleOutput);
        return tuple;
    }

    private Tuple mockTweetTuple(JSONObject TupleOutput) {
        Tuple tuple = MockTupleHelpersAnalysis.mockTuple(TupleOutput, STREAM);
        when(tuple.getValueByField("tweet")).thenReturn(TupleOutput);
        return tuple;
    }

    //***** Test DB Connection on test database *****//

    @Test
    public void TestA_DBConnectionOK() {
        System.out.print("\n***************************1***************************\n");
        int tweetsDBCount = 0;
        int usersDBCount = 0;
        try {
            this.mongoClient = new MongoClient(Tool.MONGO_IP, Tool.MONGO_PORT);
            this.mongoDB = mongoClient.getDB(Tool.MONGO_TEST_DB);
            this.mongoDB.dropDatabase();
            this.mongoCollUsers = mongoDB.getCollection("users");
            this.mongoCollTweets = mongoDB.getCollection("tweets");
            this.mongoCollTerms = mongoDB.getCollection("terms");
            this.mongoCollUserTypes = mongoDB.getCollection("user_types");
            this.mongoCollEvent = mongoDB.getCollection("event");
            this.mongoCollHourlyStats = mongoDB.getCollection("hourly_stats");
            this.mongoCollDailyStats = mongoDB.getCollection("daily_stats");
            this.mongoCollTweetSources = mongoDB.getCollection("tweet_sources");
            this.mongoCollTweetLanguages = mongoDB.getCollection("tweet_languages");
            this.mongoCollPopularVideo = mongoDB.getCollection("popular_video");
            this.mongoCollPopularLinks = mongoDB.getCollection("popular_links");
            this.mongoCollPopularPics = mongoDB.getCollection("popular_pics");
            this.mongoCollTweetsContent = mongoDB.getCollection("tweet_content");
            this.mongoCollTermNetwork = mongoDB.getCollection("term_network");
            this.mongoCollTopics = mongoDB.getCollection("topics");
            this.mongoCollMostMentionedUsers = mongoDB.getCollection("most_mentioned_users");
            this.mongoCollMostFollowedUsers = mongoDB.getCollection("most_followed_users");
            this.mongoCollMostAuthUsers = mongoDB.getCollection("most_auth_users");
            this.mongoCollLanguages = mongoDB.getCollection("user_languages");
            this.mongoCollUserNetwork = mongoDB.getCollection("user_network");;

            this.mongoCollTweets.save((DBObject) JSON.parse(Tool.TWEET_DB));
            this.mongoCollUsers.save((DBObject) JSON.parse(Tool.USER_DB));
            tweetsDBCount = mongoCollTweets.find().count();
            usersDBCount = mongoCollUsers.find().count();
        } catch (Exception e) {
            System.out.print("Mongo ERROR: " + e.toString());
            throw new RuntimeException(e);
        }
        assertThat(tweetsDBCount).isEqualTo(1);
        assertThat(usersDBCount).isEqualTo(1);
    }


    //***** Test TweetUpdateBolt *****//

    @DataProvider
    public Object[][] illegalTupleInput() {
        return new Object[][]{ { "" }, { "illegal input" } };
    }
    // Test invalit Tuple input
    @Test(expectedExceptions = NullPointerException.class, dataProvider = "illegalTupleInput")
    public void TestB_illegalTupleInput(String input) {
        System.out.print("\n***************************2***************************\n");
        // given
        TweetUpdateBolt bolt = new TweetUpdateBolt(Tool.MONGO_TEST_DB);
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tickTuple = mockTweetTuple(input);
        // when
        bolt.prepare(map, context, collector);
        bolt.execute(tickTuple);
    }

    // Tweet is new => it should be emitted to collector
    @Test
    public void TestC_EmitNewTweet() {
        System.out.print("\n***************************3***************************\n");
        // given
        TweetUpdateBolt bolt = new TweetUpdateBolt(Tool.MONGO_TEST_DB);
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tickTuple = mockTweetTuple(Tool.TWEET_NEW);
        // when
        bolt.prepare(map, context, collector);
        bolt.execute(tickTuple);
        // then - Check if is emitted
        verify(collector).emit(any(Values.class));
    }

    // If tweet already exists in DB - update tweet's information
    @Test
    public void TestD_UpdateOldTweet() {
        System.out.print("\n***************************4***************************\n");
        // given
        TweetUpdateBolt bolt = new TweetUpdateBolt(Tool.MONGO_TEST_DB);
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tickTuple = mockTweetTuple(Tool.TWEET_DB_UPDATE);
        // when
        bolt.prepare(map, context, collector);
        bolt.execute(tickTuple);
        // then - check 2 ack and no fail
        //verifyZeroInteractions(collector);
        verify(collector, times(2)).ack(any(Tuple.class));
        verify(collector, times(0)).fail(any(Tuple.class));
        verifyNoMoreInteractions(collector);
        // then - check that tweet info is updated
        int tweetsDBUpdate = mongoCollTweets.find(new BasicDBObject("retweet_count", 5)).count();
        assertThat(tweetsDBUpdate).isEqualTo(1);
        // then - check that user info is updated
        int usersDBUpdate = mongoCollUsers.find(new BasicDBObject("listed_count", 23)).count();
        assertThat(usersDBUpdate).isEqualTo(1);
    }

    // DeclareOutputFields test
    @Test
    public void TestDeclareOutputFields_TweetUpdateBolt() {
        System.out.print("\n***************************7***************************\n");
        // given
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        TweetUpdateBolt bolt = new TweetUpdateBolt(Tool.MONGO_TEST_DB);
        // when
        bolt.declareOutputFields(declarer);
        // then
        verify(declarer, times(1)).declare(any(Fields.class));
    }


    //***** Test AnalysisBolt *****//

    // Test prepare function
    @Test
    public void TestE_prepareAnalysisBolt() {
        System.out.print("\n***************************8***************************\n");
        // given
        AnalysisBolt bolt = new AnalysisBolt();
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        // when => no exceptions & errors
        bolt.prepare(map, context, collector);
    }

    @DataProvider
    public Object[][] wordsInput() {
        return new Object[][]{ { "" }, { "good" }, { "amazing" }, { "door" }, { "strike" }, { "nervous" } };
    }
    // Test sentiment extract function
    @Test(dataProvider = "wordsInput")
    public void TestF_extractSentiment(String word) {
        System.out.print("\n***************************9***************************\n");
        // given
        AnalysisBolt bolt = new AnalysisBolt();
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        // when => no exceptions & errors
        bolt.prepare(map, context, collector);
        // then next values according to words
        if (word.equals("") || word.equals("door"))
            assertThat(bolt.extract(word)).isEqualTo(0);
        else
            if (word.equals("good"))
                assertThat(bolt.extract(word)).isEqualTo(0.47792107327461797);
            else
                if (word.equals("amazing"))
                    assertThat(bolt.extract(word)).isEqualTo(0.4166666666666667);
                else
                    if (word.equals("strike"))
                        assertThat(bolt.extract(word)).isEqualTo(-0.03197647877793211);
                    else
                        if (word.equals("nervous"))
                            assertThat(bolt.extract(word)).isEqualTo(-0.11496350364963505);
    }

    // Test invalit Tuple input
    @Test(expectedExceptions = NullPointerException.class, dataProvider = "illegalTupleInput")
    public void TestG_illegalTupleInput(String input) {
        System.out.print("\n***************************11**************************\n");
        // given
        AnalysisBolt bolt = new AnalysisBolt();
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tickTuple = mockTweetTuple(input);

        // when
        bolt.prepare(map, context, collector);
        bolt.execute(tickTuple);
    }

    // When sentiment is extracted => emit to collector
    @Test
    public void TestG_EmitNewTweet() {
        System.out.print("\n***************************10**************************\n");
        // given
        AnalysisBolt bolt = new AnalysisBolt();
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tickTuple = mockTweetTuple(Tool.TWEET_NEW);
        // when
        bolt.prepare(map, context, collector);
        bolt.execute(tickTuple);
        // then - Check if is emitted
        verify(collector).emit(any(Values.class));
    }

    // DeclareOutputFields test
    @Test
    public void TestDeclareOutputFields_AnalysisBolt() {
        System.out.print("\n***************************5***************************\n");
        // given
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        AnalysisBolt bolt = new AnalysisBolt();
        // when
        bolt.declareOutputFields(declarer);
        // then
        verify(declarer, times(1)).declare(any(Fields.class));
    }


    //***** Test MongoSaveBolt *****//

    // Test invalit Tuple input
    @Test(expectedExceptions = NullPointerException.class, dataProvider = "illegalTupleInput")
    public void TestH_illegalTupleInput(String input) {
        System.out.print("\n***************************12**************************\n");
        // given
        MongoSaveBolt bolt = new MongoSaveBolt(Tool.MONGO_TEST_DB, EVENT_NAME, DESCRIPTION);
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tickTuple = mockTweetTuple(input);
        // when
        bolt.prepare(map, context, collector);
        bolt.execute(tickTuple);
    }


    @Test
    public void TestI_AnalyseAndSave() {
        System.out.print("\n***************************13**************************\n");
        // given
        MongoSaveBolt bolt = new MongoSaveBolt(Tool.MONGO_TEST_DB, EVENT_NAME, DESCRIPTION);
        Map map = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        JSONObject json = new JSONObject();
        try {
            json = (JSONObject)new JSONParser().parse(Tool.TWEET_NEW_MONGO_SAVE);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        Tuple tickTuple = mockTweetTuple(json);
        // when
        bolt.prepare(map, context, collector);
        bolt.execute(tickTuple);

        // then - collect mention stats to DB
        BasicDBObject find = new BasicDBObject("screen_name", "frienduser");
        find.put("in_links.hotdamn20", 1);
        int mentionDBUpdate = mongoCollUsers.find(find).count();
        assertThat(mentionDBUpdate).isEqualTo(1);

        // then - collect terms information to DB + no slang words
        DBCursor cursor = mongoCollTerms.find();
        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
        }
        assertThat(cursor.count()).isEqualTo(6);

        // then - check nutrition & sentiment and cleanText field in tweet
        cursor = mongoCollTweets.find(new BasicDBObject("id", 533270156244303872L));
        if (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            double nutrition = obj.getDouble("nutrition");
            assertThat(nutrition).isEqualTo(6D);
            // check sentiment score
            double sentiment = obj.getDouble("sentiment_score");
            assertThat(sentiment).isEqualTo(0.36722368321637033D);
            // check clean_text
            String cleanText = obj.getString("clean_text");
            assertThat(cleanText).isEqualTo("good today morning great friends friday.");
        }

        // then - check nutrition & sentiment in user
        cursor = mongoCollUsers.find(new BasicDBObject("screen_name", "hotdamn20"));
        if (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();

            // check nutrition
            double nutrition = obj.getDouble("nutrition");
            assertThat(nutrition).isEqualTo(6D);

            // check sentiment score
            double sentiment = obj.getDouble("sentiment_score");
            assertThat(sentiment).isEqualTo(0.36722368321637033D);
        }

        // then - check event object parameters in DB
        cursor = mongoCollEvent.find(new BasicDBObject("_id", EVENT_NAME));
        if (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();

            // check nutrition values
            double nutrition = obj.getDouble("nutrition");
            assertThat(nutrition).isEqualTo(6D);
            nutrition = obj.getDouble("nutrition_day");
            assertThat(nutrition).isEqualTo(6D);
            nutrition = obj.getDouble("nutrition_sum");
            assertThat(nutrition).isEqualTo(6D);

            // check sentiment values
            double sentiment = obj.getDouble("sentiment_score");
            assertThat(sentiment).isEqualTo(0.36722368321637033D);
            sentiment = obj.getDouble("sentiment_day");
            assertThat(sentiment).isEqualTo(0.36722368321637033D);
            sentiment = obj.getDouble("sentiment_sum");
            assertThat(sentiment).isEqualTo(0.36722368321637033D);

            sentiment = obj.getDouble("sentiment_positive_sum");
            assertThat(sentiment).isEqualTo(0.36722368321637033D);
            int sentiment_amount = obj.getInt("sentiment_positive_amount");
            assertThat(sentiment_amount).isEqualTo(1);

            // check tweets count values
            int tweets = obj.getInt("tweets_day");
            assertThat(tweets).isEqualTo(1);
            tweets = obj.getInt("tweets_sum");
            assertThat(tweets).isEqualTo(1);
            tweets = obj.getInt("tweets_count");
            assertThat(tweets).isEqualTo(1);
        }

        // then - tweet and user info are saved
        int tweetsDBCount = mongoCollTweets.find().count();
        int usersDBCount = mongoCollUsers.find().count();
        assertThat(tweetsDBCount).isEqualTo(2);
        assertThat(usersDBCount).isEqualTo(3);

        // then - 2 ack, 0 fail
        verify(collector, times(2)).ack(any(Tuple.class));
        verify(collector, times(0)).fail(any(Tuple.class));
        verifyNoMoreInteractions(collector);
    }

    // DeclareOutputFields test
    @Test
    public void TestDeclareOutputFields_MongoSaveBolt() {
        System.out.print("\n***************************6***************************\n");
        // given
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        MongoSaveBolt bolt = new MongoSaveBolt(Tool.MONGO_TEST_DB, EVENT_NAME, DESCRIPTION);
        // when
        bolt.declareOutputFields(declarer);
        // then
        verify(declarer, times(0)).declare(any(Fields.class));
    }


    //***** Test Batch Analysis *****//

    // Test BatchAnalysis function
    @Test
    public void TestK() {
        System.out.print("\n***************************14**************************\n");
        // when
        Tool.BatchAnalysis(Tool.MONGO_TEST_DB, "good", EVENT_NAME);
        // then - check index
        Utils.sleep(2000);
        List<DBObject> indexes = mongoCollTweets.getIndexInfo();
        assertThat(indexes.size()).isEqualTo(2);
        // then - user types are calculated
        DBCursor cursor = mongoCollUserTypes.find();
        if (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            if (obj.get("_id").equals("Broadcasters"))
                assertThat(obj.get("count")).isEqualTo(1);
            else
                if (obj.get("_id").equals("Selebrities"))
                    assertThat(obj.get("count")).isEqualTo(0);
                else
                    if (obj.get("_id").equals("Spreaders"))
                        assertThat(obj.get("count")).isEqualTo(1);
                    else
                        if (obj.get("_id").equals("Active Users"))
                            assertThat(obj.get("count")).isEqualTo(0);
                        else
                            if (obj.get("_id").equals("Chatters"))
                                assertThat(obj.get("count")).isEqualTo(0);
                            else
                                if (obj.get("_id").equals("All Users"))
                                    assertThat(obj.get("count")).isEqualTo(2);
        }
        // then - most_mentioned_users
        assertThat(mongoCollMostMentionedUsers.find().count()).isEqualTo(3);
        // then - most_followed_users
        assertThat(mongoCollMostFollowedUsers.find().count()).isEqualTo(3);
        // then - most_auth_users
        assertThat(mongoCollMostAuthUsers.find().count()).isEqualTo(3);
        // then - user_languages
        assertThat(mongoCollLanguages.find().count()).isEqualTo(1);
        // then - popular_pics
        assertThat(mongoCollPopularPics.find().count()).isEqualTo(1);
        // then - popular_video
        assertThat(mongoCollPopularVideo.find().count()).isEqualTo(0);
        // then - popular_links
        assertThat(mongoCollPopularLinks.find().count()).isEqualTo(0);
        // then - event object
        cursor = mongoCollEvent.find();
        if (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            assertThat(obj.getDouble("nutrition")).isEqualTo(0D);
            assertThat(obj.getDouble("nutrition_day")).isEqualTo(6D);
            assertThat(obj.getDouble("nutrition_sum")).isEqualTo(6D);
            assertThat(obj.getDouble("sentiment_score")).isEqualTo(0D);
            assertThat(obj.getDouble("sentiment_day")).isEqualTo(0.36722368321637033D);
            assertThat(obj.getDouble("sentiment_sum")).isEqualTo(0.36722368321637033D);
            assertThat(obj.getDouble("tweets_day")).isEqualTo(1D);
            assertThat(obj.getDouble("tweets_sum")).isEqualTo(1D);
            assertThat(obj.getDouble("tweets_count")).isEqualTo(0D);
            assertThat(obj.getDouble("sentiment_positive_sum")).isEqualTo(0.36722368321637033D);
            assertThat(obj.getDouble("sentiment_positive_amount")).isEqualTo(1D);
            assertThat(obj.getString("description")).isEqualTo("Test event for unit tests");
            assertThat(obj.getDouble("retweets_count")).isEqualTo(5D);
            assertThat(obj.getDouble("favorites_count")).isEqualTo(0D);
            assertThat(obj.getDouble("retweets_day")).isEqualTo(5D);
            assertThat(obj.getDouble("favorites_day")).isEqualTo(0D);
        }
        // then - mongoCollHourlyStats
        cursor = mongoCollHourlyStats.find();
        if (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            assertThat(obj.getDouble("nutrition")).isEqualTo(6D);
            assertThat(obj.getDouble("sentiment")).isEqualTo(0.36722368321637033D);
            assertThat(obj.getDouble("tweets_count")).isEqualTo(1D);
            assertThat(obj.getDouble("retweets_count")).isEqualTo(5D);
            assertThat(obj.getDouble("favorites_count")).isEqualTo(0D);
        }
        // then - mongoCollTweetSources
        assertThat(mongoCollTweetSources.find().count()).isEqualTo(3);
        // then - mongoCollTweetLangueages
        assertThat(mongoCollTweetLanguages.find().count()).isEqualTo(2);
        // then - tweet_content
        assertThat(mongoCollTweetsContent.find().count()).isEqualTo(4);
        // then - mongoCollTermNetwork
        assertThat(mongoCollTermNetwork.find().count()).isEqualTo(7);
        // then - user_network
        assertThat(mongoCollUserNetwork.find().count()).isEqualTo(3);
        // then - no emit
    }

    // Drop test collection
    @Test
    public void TestZ_DropDB() {
        System.out.print("\n***************************15**************************\n");
        boolean dropped = false;
        try {
            mongoDB.dropDatabase();
            dropped = true;
            mongoClient.close();
        } catch (Exception e) {
            System.out.print("Mongo ERROR: " + e.toString());
            throw new RuntimeException(e);
        }
        assertThat(dropped).isEqualTo(true);
    }
}
