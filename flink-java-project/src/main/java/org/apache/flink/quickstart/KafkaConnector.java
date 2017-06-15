package org.apache.flink.quickstart;

import org.apache.flink.streaming.api.CheckpointingMode;
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

        //Checkpointing
        //start a checkpoint every 1000 ms
        env.enableCheckpointing(1000);
        //set mode to exactly once
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //make sure 500ms of progress happen between checkpoints
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
        //checkpoints have to complete within one minute or are discarded
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        //allow only one checkpoint to be in progress at the same time-
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

        //define properties to connect to Kafka
        Properties props = new Properties();
        props.setProperty("zookeeper.connect","localhost:2181");
        props.setProperty("bootstrap.servers","localhost:9092");
        props.setProperty("group.id","myGroup");

        //create a data source
        DataStream<String> data = env.addSource(new FlinkKafkaConsumer010<String>(
                "myReplicatedTopic",                  //Kafka topic
                new SimpleStringSchema(),         //deserialization schema
                props )                           //consumer configuration
        );

        //Initialize DataStream
        DataStream<String> aStream = env.fromElements("This is trial to put data to Kafka topic 1111","I hope this works 2222");

        //write data to Kafka
        aStream.addSink(new FlinkKafkaProducer010<String>(
                "localhost:9092",       //default broker
                "myReplicatedTopic",                 //Kafka topic
                new SimpleStringSchema()           //serialization scheme
        ));
            //data.writeAsText("output.txt");
                data.print();
        env.execute();
    }
}
