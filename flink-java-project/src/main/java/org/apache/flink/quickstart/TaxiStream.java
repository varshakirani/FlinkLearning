package org.apache.flink.quickstart;

import com.dataartisans.flinktraining.exercises.datastream_java.datatypes.TaxiRide;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;

import java.io.IOException;

/**
 * Created by varsha on 14.06.17.
 */

public  class TaxiStream implements DeserializationSchema<TaxiRide> {

    @Override
    public TaxiRide deserialize(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public boolean isEndOfStream(TaxiRide taxiRide) {
        return false;
    }



    @Override
    public TypeInformation getProducedType() {
        return null;
    }
}
