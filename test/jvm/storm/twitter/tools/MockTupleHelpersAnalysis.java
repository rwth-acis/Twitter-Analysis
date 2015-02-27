package storm.twitter.tools;

import backtype.storm.tuple.Tuple;
import org.json.simple.JSONObject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by achueva on 11/20/14.
 */
public class MockTupleHelpersAnalysis {
    private MockTupleHelpersAnalysis() {   }

    public static Tuple mockTuple(String componentId, String streamId) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.getSourceComponent()).thenReturn(componentId);
        when(tuple.getSourceStreamId()).thenReturn(streamId);
        return tuple;
    }
    public static Tuple mockTuple(JSONObject componentId, String streamId) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.getSourceStreamId()).thenReturn(streamId);
        return tuple;
    }
}
