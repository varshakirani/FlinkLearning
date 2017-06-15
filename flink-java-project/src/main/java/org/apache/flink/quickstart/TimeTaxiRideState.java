package org.apache.flink.quickstart;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.executiongraph.restart.RestartStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.concurrent.TimeUnit;

/**
 * Created by varsha on 15.06.17.
 */
public class TimeTaxiRideState {

    public static void main(String[] args)throws Exception{

        //set up Stream environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //enabling checkpointing for every 1s
        env.enableCheckpointing(1000);

        //restart strategy
        env.setRestartStrategy(
                RestartStrategies.fixedDelayRestart(6, Time.of(10, TimeUnit.SECONDS))
        );

        

    }
}
