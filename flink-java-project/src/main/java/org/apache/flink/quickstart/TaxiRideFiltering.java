package org.apache.flink.quickstart;

import com.dataartisans.flinktraining.exercises.datastream_java.datatypes.TaxiRide;
import com.dataartisans.flinktraining.exercises.datastream_java.sources.TaxiRideSource;
import com.dataartisans.flinktraining.exercises.datastream_java.utils.GeoUtils;
import com.dataartisans.flinktraining.exercises.datastream_java.utils.TaxiRideSchema;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.io.IOException;

/**
 * Created by Varsha Kirani on 6/6/2017.
 */
public class TaxiRideFiltering {
    public static void main(String[] args) throws Exception{
        //get an ExecutionEnvironment
        StreamExecutionEnvironment env =  StreamExecutionEnvironment.getExecutionEnvironment();
        //configure event-time processing
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //get the taxi ride data stream from nycTaxiRides.gz
        //GZIPInputStream is a flink function that reads gz file. It is been called in TaxiRideSource class
        final int maxDelay = 60; //events are out of order by max 60seconds
        final int servingSpeed = 6000; //events of 100 minutes are served in 1 second
        DataStream<TaxiRide> rides = env.addSource(new TaxiRideSource("E:\\Work\\DFKI\\FlinkLearning\\nycTaxiRides.gz",maxDelay,servingSpeed));

        DataStream<TaxiRide> filteredRides = rides.filter(new checkInNYC());

        //write data Kafka
        //filteredRides.addSink(new FlinkKafkaProducer010<TaxiRide>(
//                "localhost:9092", //default broker
  //              "taxiRide",               //topic Id
    //            new TaxiRideSchema1()
      //          ));

        //write to file
        //filteredRides.writeAsText("e:\\work\\dfki\\flinklearning\\flink-java-project\\src\\main\\resources\\output.txt");
        env.execute();

    }

    public static class TaxiRideSchema1 implements DeserializationSchema <TaxiRide> {

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

    public static class checkInNYC implements FilterFunction<TaxiRide> {

        @Override
        public boolean filter(TaxiRide rides) throws Exception {
            return GeoUtils.isInNYC(rides.startLon,rides.startLat) && GeoUtils.isInNYC(rides.endLon,rides.endLat);
        }
    }
}

