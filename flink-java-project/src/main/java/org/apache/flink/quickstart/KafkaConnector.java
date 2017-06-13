package org.apache.flink.quickstart;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;

/**
 * Created by Varsha Kirani on 6/13/2017.
 */
public class KafkaConnector {
    public static void main(String[] args) throws Exception{
        //set up stream execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //define properties to connect to Kafka
        Properties props = new Properties();
        props.setProperty("zookeeper.connect","localhost:2181");
        props.setProperty("bootstrap.servers","localhost:9092");
        props.setProperty("group.id","myGroup");

        //create a data source
        DataStream<String> data = env.addSource(new FlinkKafkaConsumer010<String>(
                "my-replicated-topic",                  //Kafka topic
                new SimpleStringSchema(),         //deserialization schema
                props )                           //consumer configuration
        );

        //Initialize DataStream
        DataStream<String> aStream = env.fromElements("This is trial to put data to Kafka topic 1111","I hope this works 2222");

        //write data to Kafka
        aStream.addSink(new FlinkKafkaProducer010<String>(
                "localhost:9092",       //default broker
                "my-replicated-topic",                 //Kafka topic
                new SimpleStringSchema()           //serialization scheme
        ));
            data.writeAsText("e:\\work\\dfki\\flinklearning\\flink-java-project\\src\\main\\resources\\output.txt");
        //        data.print();
        env.execute();
    }
}
