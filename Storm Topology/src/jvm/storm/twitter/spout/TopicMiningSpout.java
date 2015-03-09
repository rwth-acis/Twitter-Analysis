package storm.twitter.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.utils.Utils;
import storm.twitter.tools.Tool;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@inheritDoc}
 *
 * The TopicMiningSpout class runs the TopicMining method from the Tool class every hour.
 *
 * @see backtype.storm.topology.base.BaseRichSpout
 * @see storm.twitter.tools.Tool
 * @author achueva
 * @since 12/5/14
 */
public class TopicMiningSpout extends BaseRichSpout {

    private final String mongoDbName;

    /**
     * The class defines private objects.
     *
     * @param mongoDbName The name of a database in MongoDB
     */
    public TopicMiningSpout(String mongoDbName) {
        this.mongoDbName = mongoDbName;
    }

    /**
     *  {@inheritDoc}
     *
     * The method starts a timer and executes Tool.TopicMining method according to the timer.
     *
     * <p>The method is called when a task for this component is initialized within a worker on the cluster.
     * It provides the spout with the environment in which the spout executes.
     *
     * @param conf The configuration of Apache Storm for the spout.
     * @param context The context contains information about the place of a task in the topology. It contains
     *                information about the task id, component id, I/O information, etc.
     * @param collect The collector is used to emit tuples to the output stream of the spout.
     *
     * @see storm.twitter.tools.Tool#TopicMining(String)
     */
    @Override
    public void open(Map conf, TopologyContext context, final SpoutOutputCollector collect) {
        // Start every hour statistic calculation
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                Tool.TopicMining(mongoDbName);
            }
        };
        // schedule the task to run starting now and then every hour
        timer.schedule(hourlyTask, 10000, 1000 * 60 * 60 * 12);
    }

    /**
     * {@inheritDoc}
     *
     *  Generally the method requests the spout to emit tuples to the output collector.
     *
     * <p>The aim of the spout is to start a batch analysis process on Storm, so no tuples are emitted. The method
     * sleeps for a long amount of time to avoid wasting CPU time.
     */
    @Override
    public void nextTuple() {
        Utils.sleep(500000000);
    }

    /**
     * {@inheritDoc}
     *
     * The method notifies Apache Storm that the tuple emitted by this spout with the msgId identifier
     * has been fully processed.
     *
     * <p>The spout emit nothing so it has nothing to ack.
     *
     * @param msgId The id of a tuple
     */
    @Override
    public void ack(Object msgId) {
    }

    /**
     * {@inheritDoc}
     *
     * The method notifies Apache Storm that the tuple emitted by this spout with the msgId identifier has failed
     * to be fully processed.
     *
     * <p>The spout emit nothing so it has nothing to fail.
     *
     * @param msgId The id of a tuple
     */
    @Override
    public void fail(Object msgId) {
    }

    /**
     * {@inheritDoc}
     *
     * The method declares the output schema for all the streams of the topology.
     *
     * <p> The spout does not have any output streams.
     *
     * @param declarer The declarer contains information about output stream ids, output fields, etc.
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //declarer.declare(new Fields("result", "collection"));
    }
}




