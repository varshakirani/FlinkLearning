package org.apache.flink.quickstart;

import com.dataartisans.flinktraining.exercises.datastream_java.connectors.PopularPlacesFromKafka;
import com.dataartisans.flinktraining.exercises.datastream_java.datatypes.TaxiRide;
import com.dataartisans.flinktraining.exercises.datastream_java.utils.TaxiRideSchema;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.streaming.api.scala.KeyedStream;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.elasticsearch2.ElasticsearchSink;
import org.apache.flink.streaming.connectors.elasticsearch2.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch2.RequestIndexer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by varsha on 15.06.17.
 *
 * Class to read cleansed rides from cleansedRides topic in Kafka and execute popular place logic
 */
public class TaxiPopularPlacesKafka {

    public static void main(String[] args)throws Exception{

         final int popularityThreshold = 20;
        //setup stream environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //Checkpointing
        //start a checkpoint every 1000 ms
      /*  env.enableCheckpointing(1000);
        //set mode to exactly once
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //make sure 500ms of progress happen between checkpoints
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
        //checkpoints have to complete within one minute or are discarded
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        //allow only one checkpoint to be in progress at the same time-
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);*/
        //add consumer to read data from kafka
        Properties props = new Properties();
        props.setProperty("zookeeper.connect","localhost:2181");
        props.setProperty("bootstrap.servers","localhost:9092");
        props.setProperty("group.id","cRides");
        props.setProperty("auto.offset.reset", "earliest");

        // create a Kafka consumer
        FlinkKafkaConsumer010<TaxiRide> consumer = new FlinkKafkaConsumer010<TaxiRide>(
                "cleansedRides",
                new TaxiRideSchema(),
                props
        );
        // assign a timestamp extractor to the consumer
        consumer.assignTimestampsAndWatermarks(new PopularPlacesFromKafka.TaxiRideTSExtractor());

        DataStream<TaxiRide>  cleanRides = env.addSource(consumer);
        //cleanRides.writeAsText("output.txt");
        DataStream<Tuple5<Float,Float,Long,Boolean,Integer>> gridPlaces = cleanRides
                 .map(new TaxiPopularPlaces.GridCell())
                .<KeyedStream< Tuple2<Integer, Boolean>, Tuple2 <Integer,Boolean> >>keyBy(0,1)
                .timeWindow(Time.minutes(15),Time.minutes(5))
                .apply(new TaxiPopularPlaces.placeCounter())
                .filter(new FilterFunction<Tuple4<Integer, Long, Boolean, Integer>>() {
                    @Override
                    public boolean filter(Tuple4<Integer, Long, Boolean, Integer> place) throws Exception {
                        return place.f3 > popularityThreshold;
                    }
                })
                .map(new TaxiPopularPlaces.fetchCoordinates());

      /*  Map<String, String> config = new HashMap<>();
        config.put("bulk.flush.max.actions", "1");   // flush inserts after every event
        config.put("cluster.name", "elasticsearch"); // default cluster name

        List<InetSocketAddress> transports = new ArrayList<>();
// set default connection details
        transports.add(new InetSocketAddress(InetAddress.getByName("localhost"), 9300));

        gridPlaces.addSink(
            new ElasticsearchSink<>(config, transports, new PopularPlaceInserter()));*/

            gridPlaces.print();
        env.execute();

    }
    public static class PopularPlaceInserter
            implements ElasticsearchSinkFunction<Tuple5<Float, Float, Long, Boolean, Integer>> {

        // construct index request
        @Override
        public void process(
                Tuple5<Float, Float, Long, Boolean, Integer> record,
                RuntimeContext ctx,
                RequestIndexer indexer) {

            // construct JSON document to index
            Map<String, String> json = new HashMap<>();
            json.put("time", record.f2.toString());         // timestamp
            json.put("location", record.f1+","+record.f0);  // lat,lon pair
            json.put("isStart", record.f3.toString());      // isStart
            json.put("cnt", record.f4.toString());          // count

            IndexRequest rqst = Requests.indexRequest()
                    .index("nyc-places")           // index name
                    .type("popular-locations")     // mapping name
                    .source(json);

            indexer.add(rqst);
        }
    }


}
