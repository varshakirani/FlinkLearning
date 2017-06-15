package org.apache.flink.quickstart;

import com.dataartisans.flinktraining.exercises.datastream_java.datatypes.TaxiRide;
import com.dataartisans.flinktraining.exercises.datastream_java.sources.TaxiRideSource;
import com.dataartisans.flinktraining.exercises.datastream_java.utils.TaxiRideSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.scala.DataStream;
//import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment;
import org.apache.flink.quickstart.TaxiRideFiltering.checkInNYC;
import org.apache.flink.quickstart.TaxiStream;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;

/**
 * Created by varsha on 14.06.17.
 */
public class TaxiCleansingKafka {
    public static void main(String[] args) throws Exception{

        //prepare Stream Execution Environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //unzip the data and read it as Datastreams
        final int maxDelay = 60; //events are out of order by max 60seconds
        final int servingSpeed = 6000; //events of 100 minutes are served in 1 second

        DataStream<TaxiRide> taxiridesBefore = env.addSource(new TaxiRideSource("/home/varsha/Documents/Work/DFKI/FlinkLearning/nycTaxiRides.gz",maxDelay,servingSpeed));

        //apply filter written in TaxiRideFiltering checkInNYC()

        DataStream<TaxiRide> filteredRides = taxiridesBefore.filter(new checkInNYC());
        //filteredRides.writeAsText("/home/varsha/Documents/Work/DFKI/FlinkLearning/flink-java-project/src/main/resources/output.txt");
        filteredRides.addSink(new FlinkKafkaProducer010<TaxiRide>(
                "localhost:9092",
                "cleansedRides",
                new TaxiRideSchema()));
        //filteredRides.print();
        env.execute();
    }
}
