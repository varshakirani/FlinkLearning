package org.apache.flink.quickstart;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment;
import org.apache.flink.quickstart.TaxiRideFiltering;
import org.apache.flink.quickstart.TaxiStream;
/**
 * Created by varsha on 14.06.17.
 */
public class TaxiCleansingKafka {
    public static void main(String[] args) throws Exception{

        //prepare Stream Execution Environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);


    }
}
