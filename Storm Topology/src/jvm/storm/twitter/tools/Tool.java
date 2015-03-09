package storm.twitter.tools;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import com.mongodb.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The class contains information about the database connection, data for UnitTests, and methods for spouts.
 *
 * @author achueva
 * @since 8/1/14
 */
public abstract class Tool {

    public static final String MONGO_IP = "10.211.55.7";
    public static final int MONGO_PORT = 27017;
    public static final String MONGO_TEST_DB = "db4test";

    public static final String[] slangWords = new String[]{"rt", "w/", "gtgt", "amp", "lol"};
    ;
    public static final String[] stopWords = new String[]{"a", "a's", "as", "able", "about", "above", "according", "accordingly", "across", "actually",
            "after", "afterwards", "again", "against", "ain't", "aint", "all", "allow", "allows", "almost", "alone", "along",
            "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody",
            "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate",
            "appropriate", "are", "aren't", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available",
            "away", "awfully", "b", "be", "became", "because", "become", "becomes", "becoming", "been", "before",
            "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between",
            "beyond", "both", "brief", "but", "by", "c", "c'mon", "cmon", "c's", "cs", "came", "can", "can't", "cannot", "cant",
            "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning",
            "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could",
            "couldn't", "couldnt", "course", "currently", "d", "definitely", "described", "despite", "did", "didn't", "didnt", "different",
            "do", "does", "doesn't", "doesnt", "doing", "don't", "dont", "done", "down", "downwards", "during", "e", "each", "edu", "eg",
            "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever",
            "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "f",
            "far", "few", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly",
            "forth", "four", "from", "further", "furthermore", "g", "get", "gets", "getting", "given", "gives", "go",
            "goes", "going", "gone", "got", "gotten", "greetings", "h", "had", "hadn't", "hadnt", "happens", "hardly", "has",
            "hasn't", "hasnt", "have", "haven't", "havent", "having", "he", "he's", "hes", "hello", "help", "hence", "her", "here", "here's", "heres",
            "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither",
            "hopefully", "how", "howbeit", "however", "i", "i'd", "i'll", "i'm", "im", "i've", "ie", "if", "ignored",
            "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar",
            "instead", "into", "inward", "is", "isn't", "isnt", "it", "it'd", "itd", "it'll", "itll", "it's", "its", "itself", "j", "just", "k",
            "keep", "keeps", "kept", "know", "knows", "known", "l", "last", "lately", "later", "latter", "latterly",
            "least", "less", "lest", "let", "let's", "lets", "like", "liked", "likely", "little", "look", "looking", "looks",
            "ltd", "m", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more",
            "moreover", "most", "mostly", "much", "must", "my", "myself", "n", "name", "namely", "nd", "near", "nearly",
            "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody",
            "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "o", "obviously",
            "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other",
            "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "p",
            "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably",
            "probably", "provides", "q", "que", "quite", "qv", "r", "rather", "rd", "re", "really", "reasonably",
            "regarding", "regardless", "regards", "relatively", "respectively", "right", "s", "said", "same", "saw",
            "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen",
            "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should",
            "shouldn't", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime",
            "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub",
            "such", "sup", "sure", "t", "t's", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks",
            "thanx", "that", "that's", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence",
            "there", "there's", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they",
            "they'd", "theyd", "they'll", "theyll", "they're", "theyre", "they've", "theyve", "think", "third", "this", "thorough", "thoroughly", "those",
            "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward",
            "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "u", "un", "under", "unfortunately",
            "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using",
            "usually", "uucp", "v", "value", "various", "very", "via", "viz", "vs", "w", "want", "wants", "was",
            "wasn't", "wasnt", "way", "we", "we'd", "wed", "we'll", "we're", "were", "we've", "weve", "welcome", "well", "went", "were", "weren't", "werent",
            "what", "what's", "whats", "whatever", "when", "whence", "whenever", "where", "where's", "wheres", "whereafter", "whereas",
            "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "who's", "whos",
            "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "won't", "wont",
            "wonder", "would", "would", "wouldn't", "wouldnt", "x", "y", "yes", "yet", "you", "you'd", "youd", "you'll", "youll", "you're", "youre", "you've", "youve",
            "your", "yours", "yourself", "yourselves", "z", "zero", ""};

    public static final String TWEET_NEW_MONGO_SAVE = "{\"filter_level\" : \"medium\", " +
            "\"retweeted\" : false, " +
            "\"in_reply_to_screen_name\" : null, " +
            "\"sentiment_score\" : 0.36722368321637033, " +
            "\"possibly_sensitive\" : false, " +
            "\"truncated\" : false, " +
            "\"lang\" : \"en\", " +
            "\"in_reply_to_status_id_str\" : null, " +
            "\"id\" : 533270156244303872, " +
            "\"extended_entities\" : { \"media\" : [ { \"id\" : 533270136824672256, " +
            "\"sizes\" : { \"thumb\" : { \"w\" : 150, \"h\" : 150, \"resize\" : \"crop\" }, " +
            "\"small\" : { \"w\" : 340, \"h\" : 421, \"resize\" : \"fit\" }, " +
            "\"medium\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" }, " +
            "\"large\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" } }, " +
            "\"media_url_https\" : \"https://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"media_url\" : \"http://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"expanded_url\" : \"http://twitter.com/hotdamn20/status/533270156244303872/photo/1\", " +
            "\"indices\" : [ 63, 85 ], \"id_str\" : \"533270136824672256\", " +
            "\"display_url\" : \"pic.twitter.com/GsBZL4xt9d\", " +
            "\"type\" : \"photo\", \"url\" : \"http://t.co/GsBZL4xt9d\" } ] }, " +
            "\"in_reply_to_user_id_str\" : null, " +
            "\"timestamp_ms\" : \"1415976487591\", " +
            "\"in_reply_to_status_id\" : null, " +
            "\"created_at\" : \"Fri Nov 14 14:48:07 +0000 2014\", " +
            "\"favorite_count\" : 0, " +
            "\"place\" : null, " +
            "\"coordinates\" : null, " +
            "\"text\" : \"RT @FriendUser Good morning friends! I'm off today :)) have a great Friday .. http://t.co/GsBZL4xt9d\", " +
            "\"contributors\" : null, " +
            "\"geo\" : null, " +
            "\"entities\" : { \"trends\" : [ ], \"symbols\" : [ ], \"urls\" : [ ], \"hashtags\" : [ ], " +
            "\"media\" : [ { \"id\" : 533270136824672256, " +
            "\"sizes\" : { \"thumb\" : { \"w\" : 150, \"h\" : 150, \"resize\" : \"crop\" }, " +
            "\"small\" : { \"w\" : 340, \"h\" : 421, \"resize\" : \"fit\" }, " +
            "\"medium\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" }, " +
            "\"large\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" } }, " +
            "\"media_url_https\" : \"https://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"media_url\" : \"http://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"expanded_url\" : \"http://twitter.com/hotdamn20/status/533270156244303872/photo/1\", " +
            "\"indices\" : [ 63, 85 ], \"id_str\" : \"533270136824672256\", " +
            "\"display_url\" : \"pic.twitter.com/GsBZL4xt9d\", \"type\" : \"photo\", " +
            "\"url\" : \"http://t.co/GsBZL4xt9d\" } ], \"user_mentions\" : [ ] }, " +
            "\"source\" : \"<a href=\\\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone</a>\", " +
            "\"favorited\" : false, " +
            "\"in_reply_to_user_id\" : null, " +
            "\"retweet_count\" : 0, " +
            "\"id_str\" : \"533270156244303872\", " +
            "\"sentiment_score\" : 0.36722368321637033" +
            "\"user\" : {\"screen_name\" : \"hotdamn20\", " +
            "\"location\" : \"\", " +
            "\"default_profile\" : true, " +
            "\"statuses_count\" : 4142, " +
            "\"profile_background_tile\" : false, " +
            "\"lang\" : \"en\", " +
            "\"profile_link_color\" : \"0084B4\", " +
            "\"profile_banner_url\" : \"https://pbs.twimg.com/profile_banners/2391964836/1399605793\", " +
            "\"id\" : 2391964836, " +
            "\"following\" : null, " +
            "\"favourites_count\" : 4138, " +
            "\"protected\" : false, " +
            "\"profile_text_color\" : \"333333\", " +
            "\"verified\" : false, " +
            "\"description\" : \"Camping-nature-ocean-travel-football and motorsports-Bruins-Patriots are what makes life interesting:)\", " +
            "\"contributors_enabled\" : false, " +
            "\"profile_sidebar_border_color\" : \"C0DEED\", " +
            "\"name\" : \"JEDI#NFB\", " +
            "\"profile_background_color\" : \"C0DEED\", " +
            "\"created_at\" : \"Sun Mar 16 02:52:45 +0000 2014\", " +
            "\"default_profile_image\" : false, " +
            "\"followers_count\" : 453, " +
            "\"profile_image_url_https\" : \"https://pbs.twimg.com/profile_images/515711111878828032/FrD6fKYC_normal.jpeg\", " +
            "\"geo_enabled\" : true, " +
            "\"profile_background_image_url\" : \"http://abs.twimg.com/images/themes/theme1/bg.png\", " +
            "\"profile_background_image_url_https\" : \"https://abs.twimg.com/images/themes/theme1/bg.png\", " +
            "\"follow_request_sent\" : null, " +
            "\"url\" : null, " +
            "\"utc_offset\" : null, " +
            "\"time_zone\" : null, " +
            "\"notifications\" : null, " +
            "\"friends_count\" : 653, " +
            "\"profile_use_background_image\" : true, " +
            "\"profile_sidebar_fill_color\" : \"DDEEF6\", " +
            "\"id_str\" : \"2391964836\", " +
            "\"profile_image_url\" : \"http://pbs.twimg.com/profile_images/515711111878828032/FrD6fKYC_normal.jpeg\", " +
            "\"is_translator\" : false, " +
            "\"listed_count\" : 3 } }";

    public static final String TWEET_NEW = "{\"filter_level\" : \"medium\", " +
            "\"retweeted\" : false, " +
            "\"in_reply_to_screen_name\" : null, " +
            "\"sentiment_score\" : 0.36722368321637033, " +
            "\"possibly_sensitive\" : false, " +
            "\"truncated\" : false, " +
            "\"lang\" : \"en\", " +
            "\"in_reply_to_status_id_str\" : null, " +
            "\"id\" : 533270156244303872, " +
            "\"extended_entities\" : { \"media\" : [ { \"id\" : 533270136824672256, " +
            "\"sizes\" : { \"thumb\" : { \"w\" : 150, \"h\" : 150, \"resize\" : \"crop\" }, " +
            "\"small\" : { \"w\" : 340, \"h\" : 421, \"resize\" : \"fit\" }, " +
            "\"medium\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" }, " +
            "\"large\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" } }, " +
            "\"media_url_https\" : \"https://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"media_url\" : \"http://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"expanded_url\" : \"http://twitter.com/hotdamn20/status/533270156244303872/photo/1\", " +
            "\"indices\" : [ 63, 85 ], \"id_str\" : \"533270136824672256\", " +
            "\"display_url\" : \"pic.twitter.com/GsBZL4xt9d\", " +
            "\"type\" : \"photo\", \"url\" : \"http://t.co/GsBZL4xt9d\" } ] }, " +
            "\"in_reply_to_user_id_str\" : null, " +
            "\"timestamp_ms\" : \"1415976487591\", " +
            "\"in_reply_to_status_id\" : null, " +
            "\"created_at\" : \"Fri Nov 14 14:48:07 +0000 2014\", " +
            "\"favorite_count\" : 0, " +
            "\"place\" : null, " +
            "\"coordinates\" : null, " +
            "\"text\" : \"Good morning friends! I'm off today :)) have a great Friday .. http://t.co/GsBZL4xt9d\", " +
            "\"contributors\" : null, " +
            "\"geo\" : null, " +
            "\"entities\" : { \"trends\" : [ ], \"symbols\" : [ ], \"urls\" : [ ], \"hashtags\" : [ ], " +
            "\"media\" : [ { \"id\" : 533270136824672256, " +
            "\"sizes\" : { \"thumb\" : { \"w\" : 150, \"h\" : 150, \"resize\" : \"crop\" }, " +
            "\"small\" : { \"w\" : 340, \"h\" : 421, \"resize\" : \"fit\" }, " +
            "\"medium\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" }, " +
            "\"large\" : { \"w\" : 387, \"h\" : 480, \"resize\" : \"fit\" } }, " +
            "\"media_url_https\" : \"https://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"media_url\" : \"http://pbs.twimg.com/media/B2aOXTLCMAA3XR5.jpg\", " +
            "\"expanded_url\" : \"http://twitter.com/hotdamn20/status/533270156244303872/photo/1\", " +
            "\"indices\" : [ 63, 85 ], \"id_str\" : \"533270136824672256\", " +
            "\"display_url\" : \"pic.twitter.com/GsBZL4xt9d\", \"type\" : \"photo\", " +
            "\"url\" : \"http://t.co/GsBZL4xt9d\" } ], \"user_mentions\" : [ ] }, " +
            "\"source\" : \"<a href=\\\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone</a>\", " +
            "\"favorited\" : false, " +
            "\"in_reply_to_user_id\" : null, " +
            "\"retweet_count\" : 0, " +
            "\"id_str\" : \"533270156244303872\", " +
            "\"user\" : {\"screen_name\" : \"hotdamn20\", " +
            "\"location\" : \"\", " +
            "\"default_profile\" : true, " +
            "\"statuses_count\" : 4142, " +
            "\"profile_background_tile\" : false, " +
            "\"lang\" : \"en\", " +
            "\"profile_link_color\" : \"0084B4\", " +
            "\"profile_banner_url\" : \"https://pbs.twimg.com/profile_banners/2391964836/1399605793\", " +
            "\"id\" : 2391964836, " +
            "\"following\" : null, " +
            "\"favourites_count\" : 4138, " +
            "\"protected\" : false, " +
            "\"profile_text_color\" : \"333333\", " +
            "\"verified\" : false, " +
            "\"description\" : \"Camping-nature-ocean-travel-football and motorsports-Bruins-Patriots are what makes life interesting:)\", " +
            "\"contributors_enabled\" : false, " +
            "\"profile_sidebar_border_color\" : \"C0DEED\", " +
            "\"name\" : \"JEDI#NFB\", " +
            "\"profile_background_color\" : \"C0DEED\", " +
            "\"created_at\" : \"Sun Mar 16 02:52:45 +0000 2014\", " +
            "\"default_profile_image\" : false, " +
            "\"followers_count\" : 453, " +
            "\"profile_image_url_https\" : \"https://pbs.twimg.com/profile_images/515711111878828032/FrD6fKYC_normal.jpeg\", " +
            "\"geo_enabled\" : true, " +
            "\"profile_background_image_url\" : \"http://abs.twimg.com/images/themes/theme1/bg.png\", " +
            "\"profile_background_image_url_https\" : \"https://abs.twimg.com/images/themes/theme1/bg.png\", " +
            "\"follow_request_sent\" : null, " +
            "\"url\" : null, " +
            "\"utc_offset\" : null, " +
            "\"time_zone\" : null, " +
            "\"notifications\" : null, " +
            "\"friends_count\" : 653, " +
            "\"profile_use_background_image\" : true, " +
            "\"profile_sidebar_fill_color\" : \"DDEEF6\", " +
            "\"id_str\" : \"2391964836\", " +
            "\"profile_image_url\" : \"http://pbs.twimg.com/profile_images/515711111878828032/FrD6fKYC_normal.jpeg\", " +
            "\"is_translator\" : false, " +
            "\"listed_count\" : 3 } }";

    public static final String TWEET_DB_UPDATE = "{ \"filter_level\" : \"medium\"," +
            " \"retweeted\" : false, " +
            "\"in_reply_to_screen_name\" : null, " +
            "\"sentiment_score\" : -0.3390265782294899, " +
            "\"possibly_sensitive\" : false, " +
            "\"truncated\" : false, " +
            "\"lang\" : \"en\", " +
            "\"in_reply_to_status_id_str\" : null, " +
            "\"id\" : 533270156966129664, " +
            "\"clean_text\" : \"player chess good worried carlsenanand.\"," +
            " \"in_reply_to_user_id_str\" : null, " +
            "\"timestamp_ms\" : \"1415976487763\", " +
            "\"in_reply_to_status_id\" : null, " +
            "\"created_at\" : \"Fri Nov 14 14:48:07 +0000 2014\", " +
            "\"favorite_count\" : 0, " +
            "\"place\" : null, " +
            "\"coordinates\" : null, " +
            "\"text\" : \"If you're always worried about what others think, you could be a good chess player. #CarlsenAnand\", " +
            "\"contributors\" : null, " +
            "\"geo\" : null, " +
            "\"entities\" : { \"trends\" : [ ], " +
            "\"symbols\" : [ ], " +
            "\"urls\" : [ ], " +
            "\"hashtags\" : [ { \"text\" : \"CarlsenAnand\", " +
            "\"indices\" : [ 84, 97 ] } ], " +
            "\"user_mentions\" : [ ] }, " +
            "\"nutrition\" : 3, " +
            "\"source\" : \"<a href=\\\"http://twitter.com\\\" rel=\\\"nofollow\\\">Twitter Web Client</a>\", " +
            "\"favorited\" : false, " +
            "\"in_reply_to_user_id\" : null, " +
            "\"retweet_count\" : 5, " +
            "\"id_str\" : \"533270156966129664\", " +
            "\"user\" : { \"screen_name\" : \"itxsmg\", " +
            "\"location\" : \"Sargodha, Karachi, Pakistan ❤\", " +
            "\"default_profile\" : false, " +
            "\"statuses_count\" : 112557, " +
            "\"profile_background_tile\" : true, " +
            "\"lang\" : \"en\", " +
            "\"profile_link_color\" : \"009999\", " +
            "\"profile_banner_url\" : \"https://pbs.twimg.com/profile_banners/377458709/1385002518\", " +
            "\"id\" : 377458709, " +
            "\"following\" : null, " +
            "\"favourites_count\" : 4, " +
            "\"protected\" : false, " +
            "\"profile_text_color\" : \"3D1957\", " +
            "\"verified\" : false, " +
            "\"description\" : \"A 5 feet 9 inches tall, Grey aged. Extremely Temperamental, Lethargic Wanderer.GK Analyst.Reader.Chess.Social Media Addict.The Guy Behind http://t.co/z0P3kGIAL8\", " +
            "\"contributors_enabled\" : false, " +
            "\"profile_sidebar_border_color\" : \"000000\", " +
            "\"name\" : \"Sheraz Masoud Gondal\", " +
            "\"profile_background_color\" : \"131516\", " +
            "\"created_at\" : \"Wed Sep 21 16:20:05 +0000 2011\", " +
            "\"default_profile_image\" : false, " +
            "\"followers_count\" : 100010, " +
            "\"profile_image_url_https\" : \"https://pbs.twimg.com/profile_images/497396761070030849/KzOdGrTe_normal.jpeg\", " +
            "\"geo_enabled\" : true, " +
            "\"profile_background_image_url\" : \"http://pbs.twimg.com/profile_background_images/452602570045784064/9vOi7ooT.jpeg\", " +
            "\"profile_background_image_url_https\" : \"https://pbs.twimg.com/profile_background_images/452602570045784064/9vOi7ooT.jpeg\", " +
            "\"follow_request_sent\" : null, " +
            "\"url\" : \"http://about.me/itxSMG\", " +
            "\"utc_offset\" : 18000, " +
            "\"time_zone\" : \"Islamabad\", " +
            "\"notifications\" : null, " +
            "\"friends_count\" : 1610, " +
            "\"profile_use_background_image\" : true, " +
            "\"profile_sidebar_fill_color\" : \"7AC3EE\", " +
            "\"id_str\" : \"377458709\", " +
            "\"profile_image_url\" : \"http://pbs.twimg.com/profile_images/497396761070030849/KzOdGrTe_normal.jpeg\", " +
            "\"is_translator\" : false, " +
            "\"listed_count\" : 23, " +
            "\"tweets_count\" : 1, " +
            "\"sentiment_score\" : -0.3390265782294899, " +
            "\"nutrition\" : 3 } }";

    public static final String TWEET_DB = "{ \"filter_level\" : \"medium\"," +
            " \"retweeted\" : false, " +
            "\"in_reply_to_screen_name\" : null, " +
            "\"sentiment_score\" : -0.3390265782294899, " +
            "\"possibly_sensitive\" : false, " +
            "\"truncated\" : false, " +
            "\"lang\" : \"en\", " +
            "\"in_reply_to_status_id_str\" : null, " +
            "\"id\" : 533270156966129664, " +
            "\"clean_text\" : \"player chess good worried carlsenanand.\"," +
            " \"in_reply_to_user_id_str\" : null, " +
            "\"timestamp_ms\" : \"1415976487763\", " +
            "\"in_reply_to_status_id\" : null, " +
            "\"created_at\" : \"Fri Nov 14 14:48:07 +0000 2014\", " +
            "\"favorite_count\" : 0, " +
            "\"place\" : null, " +
            "\"coordinates\" : null, " +
            "\"text\" : \"If you're always worried about what others think, you could be a good chess player. #CarlsenAnand\", " +
            "\"contributors\" : null, " +
            "\"geo\" : null, " +
            "\"entities\" : { \"trends\" : [ ], " +
            "\"symbols\" : [ ], " +
            "\"urls\" : [ ], " +
            "\"hashtags\" : [ { \"text\" : \"CarlsenAnand\", " +
            "\"indices\" : [ 84, 97 ] } ], " +
            "\"user_mentions\" : [ ] }, " +
            "\"nutrition\" : 3, " +
            "\"source\" : \"<a href=\\\"http://twitter.com\\\" rel=\\\"nofollow\\\">Twitter Web Client</a>\", " +
            "\"favorited\" : false, " +
            "\"in_reply_to_user_id\" : null, " +
            "\"retweet_count\" : 0, " +
            "\"id_str\" : \"533270156966129664\", " +
            "\"user\" : \"itxsmg\" }";

    public static final String USER_DB = "{ \"screen_name\" : \"itxsmg\", " +
            "\"location\" : \"Sargodha, Karachi, Pakistan ❤\", " +
            "\"default_profile\" : false, " +
            "\"statuses_count\" : 112557, " +
            "\"profile_background_tile\" : true, " +
            "\"lang\" : \"en\", " +
            "\"profile_link_color\" : \"009999\", " +
            "\"profile_banner_url\" : \"https://pbs.twimg.com/profile_banners/377458709/1385002518\", " +
            "\"id\" : 377458709, " +
            "\"following\" : null, " +
            "\"favourites_count\" : 4, " +
            "\"protected\" : false, " +
            "\"profile_text_color\" : \"3D1957\", " +
            "\"verified\" : false, " +
            "\"description\" : \"A 5 feet 9 inches tall, Grey aged. Extremely Temperamental, Lethargic Wanderer.GK Analyst.Reader.Chess.Social Media Addict.The Guy Behind http://t.co/z0P3kGIAL8\", " +
            "\"contributors_enabled\" : false, " +
            "\"profile_sidebar_border_color\" : \"000000\", " +
            "\"name\" : \"Sheraz Masoud Gondal\", " +
            "\"profile_background_color\" : \"131516\", " +
            "\"created_at\" : \"Wed Sep 21 16:20:05 +0000 2011\", " +
            "\"default_profile_image\" : false, " +
            "\"followers_count\" : 915, " +
            "\"profile_image_url_https\" : \"https://pbs.twimg.com/profile_images/497396761070030849/KzOdGrTe_normal.jpeg\", " +
            "\"geo_enabled\" : true, " +
            "\"profile_background_image_url\" : \"http://pbs.twimg.com/profile_background_images/452602570045784064/9vOi7ooT.jpeg\", " +
            "\"profile_background_image_url_https\" : \"https://pbs.twimg.com/profile_background_images/452602570045784064/9vOi7ooT.jpeg\", " +
            "\"follow_request_sent\" : null, " +
            "\"url\" : \"http://about.me/itxSMG\", " +
            "\"utc_offset\" : 18000, " +
            "\"time_zone\" : \"Islamabad\", " +
            "\"notifications\" : null, " +
            "\"friends_count\" : 1610, " +
            "\"profile_use_background_image\" : true, " +
            "\"profile_sidebar_fill_color\" : \"7AC3EE\", " +
            "\"id_str\" : \"377458709\", " +
            "\"profile_image_url\" : \"http://pbs.twimg.com/profile_images/497396761070030849/KzOdGrTe_normal.jpeg\", " +
            "\"is_translator\" : false, " +
            "\"listed_count\" : 17, " +
            "\"tweets_count\" : 1, " +
            "\"sentiment_score\" : -0.3390265782294899, " +
            "\"nutrition\" : 3 }";

    /**
     * The method executes methods from the MALLET library. As the input data, a document that is made of strings from
     * the field "clean_text" of every document in "tweets" collection is used. The method builds Mallet Parallel Topic
     * Model that creates eight topics, uses two threads, makes 1000 iterations and provides eight terms for each topic.
     * <p>The output topics are stored in the MongoDB database collection named "topics".
     *
     * @param mongoDbName The name of the database in MongoDB
     */
    public static void TopicMining (String mongoDbName) {

        // Open MongoDB
        MongoClient mongoClient;
        DB mongoDB;
        DBCollection mongoCollTweets, mongoCollTopics;
        BasicDBObject obj;
        DBObject match, group;
        AggregationOutput output = null;

        try {
            mongoClient = new MongoClient(MONGO_IP, MONGO_PORT);
            mongoDB = mongoClient.getDB(mongoDbName);
            mongoCollTweets = mongoDB.getCollection("tweets");
            mongoCollTopics = mongoDB.getCollection("topics");

        //***// Topic Mining //***//
        StringBuilder sb = new StringBuilder(System.getProperty("user.dir").toString());
        sb.append("/in_tmp");
        File dir = new File(sb.toString());
        dir.mkdirs();
        sb.append("/data.txt");
        File f = new File(sb.toString());
        try {
            f.createNewFile();

            // mongo request: db.tweets.aggregate({$match:{lang:"en"}},
            //                                     $group:{_id:"$clean_text"})
            match = new BasicDBObject("$match", new BasicDBObject("lang", "en"));
            group = new BasicDBObject("$group", new BasicDBObject("_id", "$clean_text"));
            List<DBObject> pipeline = Arrays.asList(match, group);
            output = mongoCollTweets.aggregate(pipeline);
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
            for (DBObject objRes : output.results()) {
                obj = (BasicDBObject) objRes;
                String text = (String) obj.get("_id");
                bw.write(text);

            }
            bw.close();

            // Begin by importing documents from text to feature sequences
            ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
            // Pipes: lowercase, tokenize, remove stopwords, map to features
            pipeList.add(new CharSequenceLowercase());
            pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
            //pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
            pipeList.add(new TokenSequence2FeatureSequence());
            InstanceList instances = new InstanceList(new SerialPipes(pipeList));
            FileInputStream fInput = null;
            fInput = new FileInputStream(f);


            Reader fileReader = null;
            fileReader = new InputStreamReader(fInput, "UTF-8");

            instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                    3, 2, 1)); // data, label, name fields
            fInput.close();

            // Create a model with 10 topics, alpha_t = 0.01, beta_w = 0.01
            //  Note that the first parameter is passed as the sum over topics, while
            //  the second is the parameter for a single dimension of the Dirichlet prior.
            int numTopics = 8;
            ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.12, 0.2);
            model.addInstances(instances);
            // Use two parallel samplers, which each look at one half the corpus and combine
            //  statistics after every iteration.
            model.setNumThreads(2);
            // Run the model for 50 iterations and stop (this is for testing only,
            //  for real applications, use 1000 to 2000 iterations)
            model.setNumIterations(1000);
            model.estimate();
            // Save to topics collections
            mongoCollTopics.drop();

            // Get an array of sorted sets of word ID/count pairs
            ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
            // Get the topic distribution given the current Gibbs state.
            double[] topicDistribution = model.getTopicProbabilities(0);
            // Get the data alphabet maps word IDs to strings
            Alphabet dataAlphabet = instances.getDataAlphabet();
            // create json topic object
            for (int topic = 0; topic < numTopics; topic++) {
                Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
                BasicDBObject topicObj = new BasicDBObject("distribution", topicDistribution[topic]);
                BasicDBList words = new BasicDBList();
                int rank = 0;
                while (iterator.hasNext() && rank < 8) {
                    IDSorter idCountPair = iterator.next();
                    if (rank == 0) {
                        topicObj.put("label", dataAlphabet.lookupObject(idCountPair.getID()));
                        topicObj.put("label_weight", idCountPair.getWeight());
                    } else {
                        BasicDBObject word = new BasicDBObject("word", dataAlphabet.lookupObject(idCountPair.getID()));
                        word.put("weight", idCountPair.getWeight());
                        words.add(word);
                    }
                    rank++;
                }
                topicObj.put("words", words);
                // if topic is not empty
                if (rank != 0) {
                    mongoCollTopics.save(topicObj);
                }
            }
            f.delete();
        } catch (Exception e) {
            System.out.print("ERROR: " + e.toString());
        }

        mongoClient.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The method provides audience analysis, content analysis, time trends calculation, and stores results into
     * the MongoDB database.
     *
     * <p>For the audience analysis, the method assigns roles to users, calculates who are the most
     * followed, mentioned user, which users have the greatest authority value, finds the distribution of user
     * languages and builds a user network for 200 most active users (users who wrote the greater number of tweets
     * about the event). For the content analysis, it aggregates information about the most popular tweets with
     * pictures, videos and links, it builds a keyword correlation tree and finds the distribution of tweets sources,
     * distribution of the content and languages. For time trends, the method builds a time line of different values
     * (provide historical data). It collects historical data in collections named "daily_stats", "hourly_stats",
     * "daily_stats_avg", and "hourly_stats_avg".
     *
     * @param mongoDbName The name of the database in MongoDB
     * @param keyword The word that is used as filter for Twitter Streaming API
     * @param eventName The name of an event
     */
    public static void BatchAnalysis(String mongoDbName, String keyword, String eventName) {

        // Open MongoDB
        MongoClient mongoClient;
        DB mongoDB;
        DBCollection mongoCollUsers, mongoCollTweets, mongoCollTerms, mongoCollUserTypes, mongoCollEvent;
        DBCollection mongoCollHourlyStats, mongoCollDailyStats, mongoCollTweetSources, mongoCollTweetLanguages;
        DBCollection mongoCollPopularVideo, mongoCollPopularLinks, mongoCollPopularPics, mongoCollTweetsContent;
        DBCollection mongoCollTermNetwork;
        DBCursor cursor;
        BasicDBObject obj;
        DBObject set, match, group, sort, unwind, limit, out, groupFields, project, update, group1;
        BasicDBList condList;
        AggregationOutput output = null;

        try {
            mongoClient = new MongoClient(MONGO_IP, MONGO_PORT);
            mongoDB = mongoClient.getDB(mongoDbName);
            mongoCollUsers = mongoDB.getCollection("users");
            mongoCollTweets = mongoDB.getCollection("tweets");
            mongoCollTerms = mongoDB.getCollection("terms");
            mongoCollUserTypes = mongoDB.getCollection("user_types");
            mongoCollEvent = mongoDB.getCollection("event");
            mongoCollHourlyStats = mongoDB.getCollection("hourly_stats");
            mongoCollDailyStats = mongoDB.getCollection("daily_stats");
            mongoCollTweetSources = mongoDB.getCollection("tweet_sources");
            mongoCollTweetLanguages = mongoDB.getCollection("tweet_languages");
            mongoCollPopularVideo = mongoDB.getCollection("popular_video");
            mongoCollPopularLinks = mongoDB.getCollection("popular_links");
            mongoCollPopularPics = mongoDB.getCollection("popular_pics");
            mongoCollTweetsContent = mongoDB.getCollection("tweet_content");
            mongoCollTermNetwork = mongoDB.getCollection("term_network");
            // check text index existence
            List<DBObject> indexes = mongoCollTweets.getIndexInfo();
            // create index if doesn't exist
            if (indexes.size() == 1) {
                mongoCollTweets.createIndex(new BasicDBObject("entities.urls.expanded_url", "text"));
            }

            // Get current hour of day and day of week
            Date date = new Date();   // given date
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date
            int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
            int day = calendar.get(Calendar.DAY_OF_WEEK); // gets number of the week's day: 0 - Saturday 1 - Sunday ... 6 - Friday
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int year = calendar.get(Calendar.YEAR);

            // Check if already updated at this hour
            int lastHour = 0;
            int lastDay = 0;
            int lastDayOfMonth = 0;
            int lastYear = 0;

            cursor = mongoCollHourlyStats.find(new BasicDBObject()).sort(new BasicDBObject("date", -1)).limit(1);
            if (cursor.hasNext()) {
                obj = (BasicDBObject) cursor.next();
                Date lastUpdate = (Date) obj.get("date");
                Calendar tmpCalendar = GregorianCalendar.getInstance();
                tmpCalendar.setTime(lastUpdate);
                lastHour = tmpCalendar.get(Calendar.HOUR_OF_DAY);
                lastDay = tmpCalendar.get(Calendar.DAY_OF_WEEK);
                lastDayOfMonth = tmpCalendar.get(Calendar.DAY_OF_MONTH);
                lastYear = tmpCalendar.get(Calendar.YEAR);
            }


            if (hour != lastHour || day != lastDay || dayOfMonth != lastDayOfMonth || year != lastYear) {

                //***// User types: broadcaster //***//
                //db.users.aggregate({$match:{"followers_count":{$gt:100000}}})
                groupFields = new BasicDBObject("$gt", 100000);
                match = new BasicDBObject("$match", new BasicDBObject("followers_count", groupFields));
                List<DBObject> pipelineBroadcaster = Arrays.asList(match);
                output = mongoCollUsers.aggregate(pipelineBroadcaster);
                set = new BasicDBObject("broadcaster", false);
                mongoCollUsers.update(new BasicDBObject(), new BasicDBObject("$set", set), false, true);
                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    String user = obj.getString("screen_name");
                    set = new BasicDBObject("broadcaster", true);
                    mongoCollUsers.update(new BasicDBObject("screen_name", user), new BasicDBObject("$set", set));
                }

                //***// User types: celebrity //***//
                //db.users.find({mentioned:{$gt:100}})
                cursor = mongoCollUsers.find(new BasicDBObject("mentioned", new BasicDBObject("$gt", 100)));
                set = new BasicDBObject("celebrity", false);
                mongoCollUsers.update(new BasicDBObject(), new BasicDBObject("$set", set), false, true);
                while (cursor.hasNext()) {
                    obj = (BasicDBObject) cursor.next();
                    String user = obj.getString("screen_name");
                    set = new BasicDBObject("celebrity", true);
                    mongoCollUsers.update(new BasicDBObject("screen_name", user), new BasicDBObject("$set", set));
                }

                //***// User types: spreader //***//
                //db.tweets.aggregate({$group:{_id:"$user", "retweet_count":{$sum:"$retweet_count"}, "tweet_count":{$sum:1}}},
                //                    {$project:{_id:1, spreader:{$subtract: ["$retweet_count", "$tweet_count"]}}},
                //                    {$match:{spreader:{$gt:0}}} )
                groupFields = new BasicDBObject("_id", "$user");
                groupFields.put("retweet_count", new BasicDBObject("$sum", "$retweet_count"));
                groupFields.put("tweet_count", new BasicDBObject("$sum", 1));
                group = new BasicDBObject("$group", groupFields);
                condList = new BasicDBList();
                condList.add("$retweet_count");
                condList.add("$tweet_count");
                groupFields = new BasicDBObject("_id", 1);
                groupFields.put("spreader", new BasicDBObject("$subtract", condList));
                project = new BasicDBObject("$project", groupFields);
                groupFields = new BasicDBObject("$gt", 0);
                match = new BasicDBObject("$match", new BasicDBObject("spreader", groupFields));
                List<DBObject> pipelineSpreader = Arrays.asList(group, project, match);
                output = mongoCollTweets.aggregate(pipelineSpreader);
                set = new BasicDBObject("spreader", false);
                mongoCollUsers.update(new BasicDBObject(), new BasicDBObject("$set", set), false, true);
                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    String user = obj.getString("_id");
                    set = new BasicDBObject("spreader", true);
                    mongoCollUsers.update(new BasicDBObject("screen_name", user), new BasicDBObject("$set", set));
                }

                //***// User types: active_users //***//
                //db.tweets.aggregate({$group:{_id:"$user", tweet_count:{$sum:1}}},
                //                    {$match: {"tweet_count":{$gt:5}}})
                groupFields = new BasicDBObject("_id", "$user");
                groupFields.put("tweet_count", new BasicDBObject("$sum", 1));
                group = new BasicDBObject("$group", groupFields);
                groupFields = new BasicDBObject("$gt", 5);
                match = new BasicDBObject("$match", new BasicDBObject("tweet_count", groupFields));
                List<DBObject> pipelineActive_users = Arrays.asList(group, match);
                output = mongoCollTweets.aggregate(pipelineActive_users);

                set = new BasicDBObject("active_user", false);
                mongoCollUsers.update(new BasicDBObject(), new BasicDBObject("$set", set), false, true);

                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    String user = obj.getString("_id");
                    set = new BasicDBObject("active_user", true);
                    mongoCollUsers.update(new BasicDBObject("screen_name", user), new BasicDBObject("$set", set));
                }

                //***// User types: chatter
                //db.tweets.aggregate({$match:{"in_reply_to_status_id":{$ne: null}}},
                //                    {$group:{_id:"$user", conversation:{$sum:1}}})
                groupFields = new BasicDBObject("$ne", null);
                match = new BasicDBObject("$match", new BasicDBObject("in_reply_to_status_id", groupFields));
                groupFields = new BasicDBObject("_id", "$user");
                groupFields.put("conversation", new BasicDBObject("$sum", 1));
                group = new BasicDBObject("$group", groupFields);
                List<DBObject> pipelineChatter = Arrays.asList(match, group);
                output = mongoCollTweets.aggregate(pipelineChatter);
                set = new BasicDBObject("chatter", false);
                mongoCollUsers.update(new BasicDBObject(), new BasicDBObject("$set", set), false, true);
                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    String user = obj.getString("_id");
                    set = new BasicDBObject("chatter", true);
                    mongoCollUsers.update(new BasicDBObject("screen_name", user), new BasicDBObject("$set", set));
                }

                //***// User types: all users //***//
                //db.tweets.aggregate({$group:{_id:"$user"}},
                //                    {$group:{_id:null, count:{$sum:1}}})
                groupFields = new BasicDBObject("_id", null);
                groupFields.put("count", new BasicDBObject("$sum", 1));
                group1 = new BasicDBObject("$group", groupFields);
                group = new BasicDBObject("$group", new BasicDBObject("_id", "$user"));
                List<DBObject> pipelineAllUsers = Arrays.asList(group, group1);
                int all_users = 0;
                output = mongoCollTweets.aggregate(pipelineAllUsers);
                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    all_users = obj.getInt("count");
                }

                //***// User types aggregation //***//
                int broadcasters = mongoCollUsers.find(new BasicDBObject("broadcaster", true)).count();
                int celebrities = mongoCollUsers.find(new BasicDBObject("celebrity", true)).count();
                int spreaders = mongoCollUsers.find(new BasicDBObject("spreader", true)).count();
                int active_users = mongoCollUsers.find(new BasicDBObject("active_user", true)).count();
                int chatters = mongoCollUsers.find(new BasicDBObject("chatter", true)).count();
                if (all_users == 0) {
                    all_users = mongoCollUsers.find().count();
                }
                mongoCollUserTypes.drop();
                groupFields = new BasicDBObject("_id", "Broadcasters");
                groupFields.put("count", broadcasters);
                mongoCollUserTypes.save(groupFields);
                groupFields = new BasicDBObject("_id", "Celebrities");
                groupFields.put("count", celebrities);
                mongoCollUserTypes.save(groupFields);
                groupFields = new BasicDBObject("_id", "Spreaders");
                groupFields.put("count", spreaders);
                mongoCollUserTypes.save(groupFields);
                groupFields = new BasicDBObject("_id", "Active Users");
                groupFields.put("count", active_users);
                mongoCollUserTypes.save(groupFields);
                groupFields = new BasicDBObject("_id", "Chatters");
                groupFields.put("count", chatters);
                mongoCollUserTypes.save(groupFields);
                groupFields = new BasicDBObject("_id", "All Users");
                groupFields.put("count", all_users);
                mongoCollUserTypes.save(groupFields);

                //***// Most mentioned users (max tweets_count) //***//
                //db.users.aggregate({$project:{_id:0, mentioned:1, screen_name:1}},
                //                   {$sort:{mentioned:-1}},
                //                   {$limit:10},
                //                   {$out:"most_mentioned_users"})
                groupFields = new BasicDBObject("_id", 0);
                groupFields.put("mentioned", 1);
                groupFields.put("screen_name", 1);
                project = new BasicDBObject("$project", groupFields);
                sort = new BasicDBObject("$sort", new BasicDBObject("mentioned", -1));
                limit = new BasicDBObject("$limit", 10);
                out = new BasicDBObject("$out", "most_mentioned_users");
                List<DBObject> pipeline = Arrays.asList(project, sort, limit, out);
                mongoCollUsers.aggregate(pipeline);

                //***// Most followed users (max followers_count) //***//
                //db.users.aggregate({$project:{_id:0, followers_count:1, screen_name:1}},
                //                   {$sort:{followers_count:-1}},
                //                   {$limit:10},
                //                   {$out:"most_followed_users"})
                groupFields = new BasicDBObject("_id", 0);
                groupFields.put("followers_count", 1);
                groupFields.put("screen_name", 1);
                project = new BasicDBObject("$project", groupFields);
                sort = new BasicDBObject("$sort", new BasicDBObject("followers_count", -1));
                limit = new BasicDBObject("$limit", 10);
                out = new BasicDBObject("$out", "most_followed_users");
                pipeline = Arrays.asList(project, sort, limit, out);
                mongoCollUsers.aggregate(pipeline);

                //***// Users with max nutrition //***//
                //db.users.aggregate({$project:{_id:0, nutrition:1, screen_name:1}},
                //                   {$sort:{nutrition:-1}},
                //                   {$limit:10},
                //                   {$out:"most_auth_users"})
                groupFields = new BasicDBObject("_id", 0);
                groupFields.put("nutrition", 1);
                groupFields.put("screen_name", 1);
                project = new BasicDBObject("$project", groupFields);
                sort = new BasicDBObject("$sort", new BasicDBObject("nutrition", -1));
                limit = new BasicDBObject("$limit", 10);
                out = new BasicDBObject("$out", "most_auth_users");
                pipeline = Arrays.asList(project, sort, limit, out);
                mongoCollUsers.aggregate(pipeline);

                //***// Users' languages general aggregation //***//
                //db.users.aggregate({$group:{_id:"$lang", count:{$sum:1}}},
                //                   {$match: {_id:{$ne: null}}},
                //                   {$sort:{count:-1}},
                //                   {$limit: 10}
                //                   {$out: "user_languages"})
                groupFields = new BasicDBObject("_id", "$lang");
                groupFields.put("count", new BasicDBObject("$sum", 1));
                group = new BasicDBObject("$group", groupFields);
                match = new BasicDBObject("$match", new BasicDBObject("_id", new BasicDBObject("$ne", null)));
                sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
                limit = new BasicDBObject("$limit", 10);
                out = new BasicDBObject("$out", "user_languages");
                pipeline = Arrays.asList(group, match, sort, limit, out);
                mongoCollUsers.aggregate(pipeline);

                //***// Aggregate most popular pictures //***//
                //db.tweets.aggregate({$project:{user:1, "entities.media":1, created_at:1, retweet_count:1, favorite_count:1, nutrition:1}},
                //                    {$match:{"entities.media.type":"photo"}},
                //                    {$sort:{retweet_count:-1}},
                //                    {$limit:100},
                //                    {$out:"popular_pics"})
                unwind = new BasicDBObject("$unwind", "$entities.media");
                match = new BasicDBObject("$match", new BasicDBObject("entities.media.type", "photo"));
                groupFields = new BasicDBObject("retweet_count", -1);
                groupFields.put("favorite_count", -1);
                groupFields.put("nutrition", -1);
                sort = new BasicDBObject("$sort", groupFields);
                out = new BasicDBObject("$out", "popular_pics");
                groupFields = new BasicDBObject("_id", "$entities.media.url");
                groupFields.put("nutrition", new BasicDBObject("$max", "$nutrition"));
                groupFields.put("user", new BasicDBObject("$first", "$user"));
                groupFields.put("created_at", new BasicDBObject("$first", "$created_at"));
                groupFields.put("favorite_count", new BasicDBObject("$max", "$favorite_count"));
                groupFields.put("entities", new BasicDBObject("$first", "$entities.media"));
                groupFields.put("retweet_count", new BasicDBObject("$max", "$retweet_count"));
                groupFields.put("text", new BasicDBObject("$first", "$text"));
                group = new BasicDBObject("$group", groupFields);
                pipeline = Arrays.asList(unwind, match, group, sort, out);
                mongoCollTweets.aggregate(pipeline);

                //***// Most popular video //***//
                //db.tweets.aggregate({$match:{$text:{$search:"youtu"}}},
                //                    {$unwind:"$entities.urls"},
                //                    {$group:{_id:"$entities.urls.expanded_url", user:{$first:"$user"},
                //                              text:{$first:"$text"}, created_at:{$first:"$created_at"},
                //                              retweet_count:{$max:"$retweet_count"},
                //                              favorite_count:{$max:"$favorite_count"},
                //                              nutrition:{$max:"$nutrition"}}},
                //                    {$sort:{retweet_count:-1,  favorite_count:-1, nutrition:-1}},
                //                    {$limit:100},
                //                    {$out: "popular_video"})
                groupFields = new BasicDBObject("_id", "$entities.urls.expanded_url");
                groupFields.put("nutrition", new BasicDBObject("$max", "$nutrition"));
                groupFields.put("user", new BasicDBObject("$first", "$user"));
                groupFields.put("created_at", new BasicDBObject("$first", "$created_at"));
                groupFields.put("favorite_count", new BasicDBObject("$max", "$favorite_count"));
                groupFields.put("retweet_count", new BasicDBObject("$max", "$retweet_count"));
                groupFields.put("text", new BasicDBObject("$first", "$text"));
                group = new BasicDBObject("$group", groupFields);
                match = new BasicDBObject("$match", new BasicDBObject("$text", new BasicDBObject("$search", "youtu")));
                unwind = new BasicDBObject("$unwind", "$entities.urls");
                groupFields = new BasicDBObject("retweet_count", -1);
                groupFields.put("favorite_count", -1);
                groupFields.put("nutrition", -1);
                sort = new BasicDBObject("$sort", groupFields);
                out = new BasicDBObject("$out", "popular_video");
                pipeline = Arrays.asList(match, unwind, group, sort, out);
                mongoCollTweets.aggregate(pipeline);

                //***// Most popular links //***//
                //db.tweets.aggregate({$match:{"entities.urls.url":{$exists:1}}},
                //                    {$project:{user:1, text:1, created_at:1, retweet_count:1, favorite_count:1, nutrition:1, _id:0, "entities.urls.expanded_url":1}},
                //                    {$sort:{retweet_count:-1}},
                //                    {$limit:100},
                //                    {$out:"popular_links"})
                //db.tweets.aggregate({$unwind:"$entities.urls"}, {$group:{_id:"$entities.urls.expanded_url", media:{$first:"$entities.media.type"}}}, {$match:{media:null}})
                unwind = new BasicDBObject("$unwind", "$entities.urls");
                groupFields = new BasicDBObject("_id", "$entities.urls.expanded_url");
                groupFields.put("nutrition", new BasicDBObject("$max", "$nutrition"));
                groupFields.put("user", new BasicDBObject("$first", "$user"));
                groupFields.put("created_at", new BasicDBObject("$first", "$created_at"));
                groupFields.put("favorite_count", new BasicDBObject("$max", "$favorite_count"));
                groupFields.put("retweet_count", new BasicDBObject("$max", "$retweet_count"));
                groupFields.put("text", new BasicDBObject("$first", "$text"));
                groupFields.put("media", new BasicDBObject("$first", "$entities.media.type"));
                groupFields.put("url", new BasicDBObject("$first", "$entities.urls.display_url"));
                group = new BasicDBObject("$group", groupFields);
                match = new BasicDBObject("$match", new BasicDBObject("media", null));
                groupFields = new BasicDBObject("retweet_count", -1);
                groupFields.put("favorite_count", -1);
                groupFields.put("nutrition", -1);
                sort = new BasicDBObject("$sort", groupFields);
                out = new BasicDBObject("$out", "popular_links");
                pipeline = Arrays.asList(unwind, group, match, sort, out);
                mongoCollTweets.aggregate(pipeline);

                //***// Sentiment, nutrition, energy, retweets, favorites hourly update of event object
                //Retweets and Favorites for last hour (new_retweet_count, new_favorites_count)
                int new_retweets_count = 0;
                int new_favorites_count = 0;
                Double nutrition = 0D;
                Double sentiment = 0D;
                int tweetsCount = 0;
                int retweets_count = 0;
                int favorites_count = 0;
                //db.tweets.aggregate({$group:{_id:null, retweets_count:{$sum:"$retweet_count"},
                //                                       favorites_count:{$sum:"$favorite_count"}}})
                groupFields = new BasicDBObject("_id", null);
                groupFields.put("retweets_count", new BasicDBObject("$sum", "$retweet_count"));
                groupFields.put("favorites_count", new BasicDBObject("$sum", "$favorite_count"));
                group = new BasicDBObject("$group", groupFields);
                pipeline = Arrays.asList(group);
                output = mongoCollTweets.aggregate(pipeline);
                if (output.results().iterator().hasNext()) {
                    obj = (BasicDBObject) output.results().iterator().next();
                    new_retweets_count = obj.getInt("retweets_count");
                    new_favorites_count = obj.getInt("favorites_count");
                }

                // Sentiment, nutrition, energy, retweet, favorite count from event object
                cursor = mongoCollEvent.find(new BasicDBObject());
                if (cursor.hasNext()) {
                    obj = (BasicDBObject) cursor.next();
                    if (obj.keySet().contains("nutrition"))
                        nutrition = obj.getDouble("nutrition");
                    if (obj.keySet().contains("sentiment_score"))
                        sentiment = obj.getDouble("sentiment_score");
                    if (obj.keySet().contains("tweets_count"))
                        tweetsCount = obj.getInt("tweets_count");
                    if (obj.keySet().contains("retweets_count"))
                        retweets_count = obj.getInt("retweets_count");
                    if (obj.keySet().contains("favorites_count"))
                        favorites_count = obj.getInt("favorites_count");
                }

                // retweet and favorites count for last hour
                retweets_count = new_retweets_count - retweets_count;
                favorites_count = new_favorites_count - favorites_count;
                // update event object
                groupFields = new BasicDBObject("sentiment_score", 0);
                groupFields.put("nutrition", 0);
                groupFields.put("tweets_count", 0);
                groupFields.put("retweets_count", new_retweets_count);
                groupFields.put("favorites_count", new_favorites_count);
                update = new BasicDBObject("$set", groupFields);
                group = new BasicDBObject("retweets_day", retweets_count);
                group.put("favorites_day", favorites_count);
                update.put("$inc", group);
                mongoCollEvent.update(new BasicDBObject("_id", eventName), update);

                //***// Hourly statistics //***//
                obj = new BasicDBObject("date", new Date());
                obj.put("sentiment", sentiment);
                obj.put("nutrition", nutrition);
                obj.put("tweets_count", tweetsCount);
                obj.put("retweets_count", retweets_count);
                obj.put("favorites_count", favorites_count);
                obj.put("hour_number", hour);
                mongoCollHourlyStats.save(new BasicDBObject(obj));

                //***// Tweets' sources //***//
                groupFields = new BasicDBObject("_id", "$source");
                groupFields.put("count", new BasicDBObject("$sum", 1));
                group = new BasicDBObject("$group", groupFields);
                sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
                pipeline = Arrays.asList(group, sort);
                output = mongoCollTweets.aggregate(pipeline);
                int i = 0;
                mongoCollTweetSources.drop();
                DBObject newObj = new BasicDBObject("_id", "Other");
                int count = 0;
                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    if (i < 9) {
                        mongoCollTweetSources.save(obj);
                    } else {
                        count = count + obj.getInt("count");
                    }
                    i++;
                }
                newObj.put("count", count);
                mongoCollTweetSources.save(newObj);

                //***// Tweets' languages //***//
                groupFields = new BasicDBObject("_id", "$lang");
                groupFields.put("count", new BasicDBObject("$sum", 1));
                group = new BasicDBObject("$group", groupFields);
                sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
                pipeline = Arrays.asList(group, sort);
                output = mongoCollTweets.aggregate(pipeline);
                i = 0;
                mongoCollTweetLanguages.drop();
                newObj = new BasicDBObject("_id", "Other");
                count = 0;
                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    if (i < 9) {
                        mongoCollTweetLanguages.save(obj);
                    } else {
                        count = count + obj.getInt("count");
                    }
                    i++;
                }
                newObj.put("count", count);
                mongoCollTweetLanguages.save(newObj);

                //***// Tweets' content //***//
                // count of tweets with plain text
                //db.tweets.aggregate({$match:{$and: [{"entities.media.type":{$exists:false}},
                //                                    {"entities.urls.url":{$exists:false}}]}}
                //                    {$group:{_id:null, count:{$sum: 1}}},
                //                    {$out:"tweet_content"})
                condList = new BasicDBList();
                condList.add(new BasicDBObject("entities.media.type", new BasicDBObject("$exists", false)));
                condList.add(new BasicDBObject("entities.urls.url", new BasicDBObject("$exists", false)));
                group = new BasicDBObject("$and", condList);
                match = new BasicDBObject("$match", group);
                groupFields = new BasicDBObject("_id", "Plain Text");
                groupFields.put("count", new BasicDBObject("$sum", 1));
                group = new BasicDBObject("$group", groupFields);
                out = new BasicDBObject("$out", "tweet_content");
                pipeline = Arrays.asList(match, group, out);
                mongoCollTweets.aggregate(pipeline);
                DBObject content = new BasicDBObject("_id", "Pictures");
                count = (int) mongoCollPopularPics.count();
                content.put("count", count);
                mongoCollTweetsContent.save(content);
                content = new BasicDBObject("_id", "Links");
                count = (int) mongoCollPopularLinks.count();
                content.put("count", count);
                mongoCollTweetsContent.save(content);
                content = new BasicDBObject("_id", "Videos");
                count = (int) mongoCollPopularVideo.count();
                content.put("count", count);
                mongoCollTweetsContent.save(content);


                //***// Term Network collection construction //***//
                mongoCollTermNetwork.drop();
                // Get keyword term
                // db.terms.find({term: <keyword>},
                //               {_id:0, term:1, occurrence:1})
                BasicDBObject projection = new BasicDBObject("_id", 0);
                projection.put("term", 1);
                projection.put("occurrence", 1);
                cursor = mongoCollTerms.find(new BasicDBObject("term", keyword), projection);
                if (cursor.hasNext()) {
                    obj = (BasicDBObject) cursor.next();
                    obj.put("link_power", null);
                    obj.put("parent", null);
                    mongoCollTermNetwork.save(obj);
                } else {
                    System.out.print("ERROR: No keyword term! Something is wrong!");
                }

                // Get terms that are most related to the keyword
                // db.terms.aggregate({$unwind:"$links"},
                //                    {$match:{"links.term":<keyword>}},
                //                    {$sort:{"links.co_occur":-1}},
                //                    {$limit:10}),
                //                    {$project: {term:"$term", occurrence:"$occurrence",
                //                                link_power:"$links.co_occur", parent:"$links.term"}}
                unwind = new BasicDBObject("$unwind", "$links");
                match = new BasicDBObject("$match", new BasicDBObject("links.term", keyword));
                sort = new BasicDBObject("$sort", new BasicDBObject("links.co_occur", -1));
                limit = new BasicDBObject("$limit", 10);
                groupFields = new BasicDBObject("_id", 0);
                groupFields.put("term", "$term");
                groupFields.put("occurrence", "$occurrence");
                groupFields.put("link_power", "$links.co_occur");
                groupFields.put("parent", "$links.term");
                project = new BasicDBObject("$project", groupFields);
                pipeline = Arrays.asList(unwind, match, sort, limit, project);
                Map<String, Integer> termsStats = new HashMap<String, Integer>();
                BasicDBList termsOrList = new BasicDBList();
                output = mongoCollTerms.aggregate(pipeline);
                for (DBObject objRes : output.results()) {
                    obj = (BasicDBObject) objRes;
                    mongoCollTermNetwork.save(obj);
                    termsStats.put((String) objRes.get("term"), 5);
                    termsOrList.add(new BasicDBObject("links.term", objRes.get("term")));
                }

                // Get terms for 3d level of graph/star
                // db.terms.aggregate({$unwind:"$links"},
                //                    {$match:{$or:[{"links.term":<term from 2d level>}, ... , {"links.term":<term from 2d level>}]}},
                //                    {$sort:{"links.co_occur":-1}},
                //                    {$group:{_id:"$term", occurrence:{$max:"$occurrence"},
                //                             parent:{$first:"$links.term"}, link_power:{$first:"$links.co_occur"}}},
                //                    {$project:{term:"$_id", occurrence:"$occurrence", parent:"$parent",
                //                               link_power:"$link_power", _id:0}}).pretty()
                if (termsOrList.size() > 0) {
                    BasicDBObject or;
                    if (termsOrList.size() > 1) {
                        or = new BasicDBObject("$or", termsOrList);
                        match = new BasicDBObject("$match", or);
                    } else if (termsOrList.size() == 1)
                        match = new BasicDBObject("$match", termsOrList.get(0));
                    groupFields = new BasicDBObject("_id", "$term");
                    groupFields.put("occurrence", new BasicDBObject("$max", "$occurrence"));
                    groupFields.put("link_power", new BasicDBObject("$first", "$links.co_occur"));
                    groupFields.put("parent", new BasicDBObject("$first", "$links.term"));
                    group = new BasicDBObject("$group", groupFields);
                    groupFields = new BasicDBObject("_id", 0);
                    groupFields.put("term", "$_id");
                    groupFields.put("occurrence", "$occurrence");
                    groupFields.put("link_power", "$link_power");
                    groupFields.put("parent", "$parent");
                    project = new BasicDBObject("$project", groupFields);
                    pipeline = Arrays.asList(unwind, match, sort, group, project);
                    output = mongoCollTerms.aggregate(pipeline);
                    for (DBObject objRes : output.results()) {
                        String term = (String) objRes.get("parent");
                        if (termsStats.get(term) > 0 && !termsStats.keySet().contains(objRes.get("term"))) {
                            obj = (BasicDBObject) objRes;
                            mongoCollTermNetwork.save(obj);
                            termsStats.put(term, termsStats.get(term) - 1);
                        }
                    }
                }

                //***// User Network collection construction //***//
                //db.users.aggregate({$sort:{tweets_count: -1}},
                //                   {$limit:200})
                sort = new BasicDBObject("$sort", new BasicDBObject("tweets_count", -1));
                limit = new BasicDBObject("$limit", 200);
                out = new BasicDBObject("$out", "user_network");
                pipeline = Arrays.asList(sort, limit, out);
                mongoCollUsers.aggregate(pipeline);

                //***// Everyday's statistics //***//
                if (hour == 0) {
                    Double oldEnergy = 0D;
                    Double energy = 0D;
                    Double nutrition_day = 0D;
                    Double sentiment_day = 0D;
                    int tweetsCount_day = 0;
                    int retweets_day = 0;
                    int favorites_day = 0;
                    cursor = mongoCollEvent.find(new BasicDBObject());
                    if (cursor.hasNext()) {
                        obj = (BasicDBObject) cursor.next();
                        if (obj.keySet().contains("energy")) {
                            oldEnergy = obj.getDouble("energy");
                        }
                        if (obj.keySet().contains("nutrition_day")) {
                            nutrition_day = obj.getDouble("nutrition_day");
                        }
                        if (obj.keySet().contains("sentiment_day")) {
                            sentiment_day = obj.getDouble("sentiment_day");
                        }
                        if (obj.keySet().contains("tweets_day")) {
                            tweetsCount_day = obj.getInt("tweets_day");
                        }
                        if (obj.keySet().contains("retweets_day")) {
                            retweets_day = obj.getInt("retweets_day");
                        }
                        if (obj.keySet().contains("favorites_day")) {
                            favorites_day = obj.getInt("favorites_day");
                        }
                    }

                    energy = oldEnergy * 0.85 + nutrition_day;
                    boolean grow = true;
                    if (energy < oldEnergy)
                        grow = false;
                    groupFields = new BasicDBObject("nutrition_day", 0);
                    groupFields.put("sentiment_day", 0);
                    groupFields.put("tweets_day", 0);
                    groupFields.put("retweets_day", 0);
                    groupFields.put("favorites_day", 0);
                    groupFields.put("energy", energy);
                    groupFields.put("energy_grow", grow);
                    update = new BasicDBObject("$set", groupFields);
                    groupFields = new BasicDBObject("retweets_sum", retweets_day);
                    groupFields.put("favorites_sum", favorites_day);
                    update.put("$inc", groupFields);
                    mongoCollEvent.update(new BasicDBObject("_id", eventName), update);

                    //***// Daily statistics //***//
                    obj = new BasicDBObject("date", new Date());
                    obj.put("sentiment", sentiment_day);
                    obj.put("nutrition", nutrition_day);
                    obj.put("tweets_count", tweetsCount_day);
                    obj.put("energy", energy);
                    obj.put("retweets_count", retweets_day);
                    obj.put("favorites_count", favorites_day);
                    obj.put("day_number", day);
                    mongoCollDailyStats.save(new BasicDBObject(obj));

                    //***// Count averages for hours //***//
                    //db.hourly_stats.aggregate({$group:{_id:"$hour_number",
                    //                                   retweets_count_avg:{$avg:"$retweets_count"}}})
                    groupFields = new BasicDBObject("_id", "$hour_number");
                    groupFields.put("retweets_count_avg", new BasicDBObject("$avg", "$retweets_count"));
                    groupFields.put("favorites_count_avg", new BasicDBObject("$avg", "$favorites_count"));
                    groupFields.put("tweets_count_avg", new BasicDBObject("$avg", "$tweets_count"));
                    groupFields.put("nutrition_avg", new BasicDBObject("$avg", "$nutrition"));
                    groupFields.put("sentiment_avg", new BasicDBObject("$avg", "$sentiment"));
                    group = new BasicDBObject("$group", groupFields);
                    sort = new BasicDBObject("$sort", new BasicDBObject("_id", -1));
                    out = new BasicDBObject("$out", "hourly_stats_avg");
                    pipeline = Arrays.asList(group, sort, out);
                    mongoCollHourlyStats.aggregate(pipeline);

                    //***// Count averages for days //***//
                    groupFields = new BasicDBObject("_id", "$day_number");
                    groupFields.put("retweets_count_avg", new BasicDBObject("$avg", "$retweets_count"));
                    groupFields.put("favorites_count_avg", new BasicDBObject("$avg", "$favorites_count"));
                    groupFields.put("tweets_count_avg", new BasicDBObject("$avg", "$tweets_count"));
                    groupFields.put("nutrition_avg", new BasicDBObject("$avg", "$nutrition"));
                    groupFields.put("energy_avg", new BasicDBObject("$avg", "$energy"));
                    groupFields.put("sentiment_avg", new BasicDBObject("$avg", "$sentiment"));
                    group = new BasicDBObject("$group", groupFields);
                    sort = new BasicDBObject("$sort", new BasicDBObject("_id", -1));
                    out = new BasicDBObject("$out", "daily_stats_avg");
                    pipeline = Arrays.asList(group, sort, out);
                    mongoCollDailyStats.aggregate(pipeline);

                }
            }
            // Close MongoDB
            mongoClient.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
