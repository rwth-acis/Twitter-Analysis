/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package storm.twitter.spout;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import backtype.storm.utils.Utils;
import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

/**
 * {@inheritDoc}
 *
 * The class gets data from Twitter Streaming API and emits it to an output stream.
 *
 * @see backtype.storm.topology.base.BaseRichSpout
 */
@SuppressWarnings("serial")
public class TwitterSampleSpout extends BaseRichSpout {

	SpoutOutputCollector _collector;
	LinkedBlockingQueue<String> queue = null;
	TwitterStream _twitterStream;
	String consumerKey;
	String consumerSecret;
	String accessToken;
	String accessTokenSecret;
	String[] keyWords;

    /**
     * The method defines private objects.
     *
     * @param consumerKey The consumer key for Twitter API
     * @param consumerSecret The consumer secret for Twitter API
     * @param accessToken The access token for Twitter API
     * @param accessTokenSecret The access token secret for Twitter API
     * @param keyWords The set of words that are used as a filter for Twitter Stream API
     */
	public TwitterSampleSpout(String consumerKey, String consumerSecret,
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
     * The method receives tweets from Twitter Streaming API and puts them into queue variable.
     *
     * <p>The method is called when a task for this component is initialized within a worker on the cluster.
     * It provides the spout with the environment in which the spout executes.
     *
     * @param conf The configuration of Apache Storm for the spout.
     * @param context The context contains information about the place of a task in the topology. It contains
     *                information about the task id, component id, I/O information, etc.
     * @param collector The collector is used to emit tuples to the output stream of the spout.
     */
	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
        // Create queue of Strings for tweets
		queue = new LinkedBlockingQueue<String>(1000);
		_collector = collector;

		StatusListener listener = new StatusListener() {

            // Get json from Twitter API and put it to queue
			@Override
			public void onStatus(Status status) {
                String json = TwitterObjectFactory.getRawJSON(status);
                queue.offer(json);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
			}

			@Override
			public void onTrackLimitationNotice(int i) {
			}

			@Override
			public void onScrubGeo(long l, long l1) {
			}

			@Override
			public void onException(Exception ex) {
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
			}

		};

        // Create twitterStream
		TwitterStream twitterStream = new TwitterStreamFactory(
				new ConfigurationBuilder().setJSONStoreEnabled(true).build())
				.getInstance();

        // Twitter API configuration
		twitterStream.addListener(listener);
		twitterStream.setOAuthConsumer(consumerKey, consumerSecret);
		AccessToken token = new AccessToken(accessToken, accessTokenSecret);
		twitterStream.setOAuthAccessToken(token);

        // if there is no keywork filter - get all 1% of tweets from API
		if (keyWords.length == 0) {
			twitterStream.sample();
		}
        // filter tweets from the stream
		else {
			FilterQuery query = new FilterQuery().track(keyWords);
			twitterStream.filter(query);
		}
	}

    /**
     * {@inheritDoc}
     *
     * The method requests the spout to emit tuples to the output collector. When the queue is empty the method
     * sleeps for a small amount of time to avoid wasting too much CPU time .
     */
	@Override
	public void nextTuple() {
        // get tweets from queue and emit it (create output stream from a spout)
		String ret = queue.poll();
		if (ret == null) {
			Utils.sleep(50);
		} else {
			_collector.emit(new Values(ret));

		}
	}

    /**
     * {@inheritDoc}
     *
     * The method is called when the spout is going to be shutdown. Before the shutdown the method closes the
     * Twitter Stream connection.
     */
	@Override
	public void close() {
		_twitterStream.shutdown();
	}

    /**
     * {@inheritDoc}
     *
     * The method declares a configuration that is specific to this component.
     *
     * @return The configuration of the component.
     */
	@Override
	public Map<String, Object> getComponentConfiguration() {
		Config ret = new Config();
		ret.setMaxTaskParallelism(1);
		return ret;
	}

    /**
     * {@inheritDoc}
     *
     * The method notifies Apache Storm that the tuple emitted by this spout with the msgId identifier
     * has been fully processed.
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
     * @param msgId The id of a tuple
     */
	@Override
	public void fail(Object msgId) {
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
