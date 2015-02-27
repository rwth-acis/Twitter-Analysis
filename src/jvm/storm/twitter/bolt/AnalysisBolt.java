package storm.twitter.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import backtype.storm.tuple.Values;

import java.io.*;
import java.util.*;

/**
 * {@inheritDoc}
 *
 * The class calculates a sentiment score for tweets. It receives tweets in tuples that come in the bolt.
 * Each incoming tuple contains exactly one tweet.
 *
 * @see backtype.storm.topology.base.BaseRichBolt
 * @author achueva
 * @since 7/28/14
 */

public class AnalysisBolt extends BaseRichBolt {

    private Map<String, Double> dictionary;
    private OutputCollector collector;

    /**
     * {@inheritDoc}
     *
     * The method prepares the bolt for Sentiment Analysis of incoming tuples: it reads
     * a SentiWordNet_3.0.0_20130122.txt file into a private dictionary map object that is used later in
     * {@link storm.twitter.bolt.AnalysisBolt#extract} method.
     *
     * @see storm.twitter.bolt.AnalysisBolt#extract(String)
     *
     * @param map The Storm configuration for the bolt.
     * @param topologyContext The context contains information about the place of a task within the topology,
     *            including the task id, the component id of a task, input and output information, etc.
     * @param collector The collector is used to emit tuples from the bolt to send them to next bolt.
     */

    public final void prepare(final Map map, final TopologyContext topologyContext,
                              final OutputCollector collector) {

        this.collector = collector;
        dictionary = new HashMap<String, Double>();
        HashMap<String, HashMap<Integer, Double>> tempDictionary = new HashMap<String, HashMap<Integer, Double>>();
        BufferedReader csv = null;
        // Read the sentiment txt file into memory
        try {
            InputStream in = this.getClass().getResourceAsStream("/resources/SentiWordNet_3.0.0_20130122.txt");
            csv = new BufferedReader(new InputStreamReader(in));
            int lineNumber = 0;
            String line;
            while ((line = csv.readLine()) != null) {
                lineNumber++;
                if (!line.trim().startsWith("#")) {
                    String[] data = line.split("\t");
                    String wordTypeMarker = data[0];
                    if (data.length != 6) {
                        throw new IllegalArgumentException(
                                "Incorrect tabulation format in file, line: "
                                        + lineNumber);
                    }
                    Double synsetScore = Double.parseDouble(data[2])
                            - Double.parseDouble(data[3]);
                    String[] synTermsSplit = data[4].split(" ");
                    for (String synTermSplit : synTermsSplit) {
                        String[] synTermAndRank = synTermSplit.split("#");
                        String synTerm = synTermAndRank[0] + "#"
                                + wordTypeMarker;
                        int synTermRank = Integer.parseInt(synTermAndRank[1]);
                        if (!tempDictionary.containsKey(synTerm)) {
                            tempDictionary.put(synTerm,
                                    new HashMap<Integer, Double>());
                        }
                        tempDictionary.get(synTerm).put(synTermRank,
                                synsetScore);
                    }
                }
            }
            for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary
                    .entrySet()) {
                String word = entry.getKey();
                Map<Integer, Double> synSetScoreMap = entry.getValue();
                double score = 0.0;
                double sum = 0.0;
                for (Map.Entry<Integer, Double> setScore : synSetScoreMap
                        .entrySet()) {
                    score += setScore.getValue() / (double) setScore.getKey();
                    sum += 1.0 / (double) setScore.getKey();
                }
                score /= sum;
                dictionary.put(word, score);
            }
        } catch (Exception e) {
            System.out.print("Error: " + e.toString());
        } finally {
            if (csv != null) {
                try {
                    csv.close();
                } catch (IOException e) {
                    System.out.print("Error: " + e.toString());
                }
            }
        }
    }

    /**
     * The method returns a sentiment score of a word.
     *
     * @param word One of the words of a tweet
     * @return The sentiment score of a word
     */

    // Return a sentiment score of a word
    public Double extract(String word)
    {
        Double total = new Double(0);
        int divider = 0;
        if(dictionary.get(word+"#n") != null) {
            total = dictionary.get(word + "#n") + total;
            divider++;
        }
        if(dictionary.get(word+"#a") != null) {
            total = dictionary.get(word+"#a") + total;
            divider++;
        }
        if(dictionary.get(word+"#r") != null) {
            total = dictionary.get(word+"#r") + total;
            divider++;
        }
        if(dictionary.get(word+"#v") != null) {
            total = dictionary.get(word+"#v") + total;
            divider++;
        }
        if (total != 0)
            return total/divider;
        else
            return 0.0;
    }

    /**
     * {@inheritDoc}
     * The method processes the incoming tuple. It extracts a tweet from a tuple and calculates a sentiment score of it.
     *
     * @see backtype.storm.topology.base.BaseRichBolt#execute(backtype.storm.tuple.Tuple)
     * @param tuple A tweet in JSON format
     */

    @Override
    public void execute(Tuple tuple) {
        // Get tweet data from tuple
        String JSONString = tuple.getString(0);
        JSONObject json = null;
        try {
            json = (JSONObject)new JSONParser().parse(JSONString);
        } catch (ParseException e) {
            System.out.print("Error: " + e.toString());
        }
        String lang = (String) json.get("lang");
        // Calculate the sentiment value only for english tweets
        if (lang.equals("en")) {
            String text = (String) json.get("text");
            String[] words = text.split("\\s+");
            double totalScore = 0, averageScore;
            for (String word : words) {
                word = word.replaceAll("\\p{Punct}|\\n", " ").toLowerCase();
                Double score = extract(word);
                if (score == null)
                    continue;
                totalScore += score;
            }
            averageScore = totalScore;
            json.put("sentiment_score", averageScore);
        }
        else {
            json.put("sentiment_score", 0D);
        }
        // Emit tweet to next bolt
        this.collector.emit(new Values(json));
    }


    /**
     * {@inheritDoc}
     *
     * The method declares the output schema for output stream of a bolt. The output stream consists of JSON objects.
     * Each of them contains a tweet with a calculated sentiment score.
     *
     * @param declarer The declarer is used to declare details of an output stream
     */
    // Declare output fields
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweet"));
    }

}
