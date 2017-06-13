package org.apache.flink.quickstart;


import org.apache.flink.api.common.io.FilePathFilter;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;

import java.util.*;

/**
 * Created by Varsha Kirani on 6/8/2017.
 * Example for Basic Data Sorces: Collections and Files
 *
 */
public class Demo2 {
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> names = env.fromElements("Varsha","Kirani");

        List<String> namesList = new ArrayList<String>();
        namesList.add("Varsha");
        namesList.add("Kirani");
        namesList.add("Gopinath");

        DataStream<String> names2 = env.fromCollection(namesList);

    DataStream<String> lines = env.readTextFile("C:\\Users\\Varsha Kirani\\Documents\\Work\\DFKI\\LearningFlink\\flink-java-project\\input.txt");
//Not sure how to give FileInputFormat
    //DataStream<String> lines2 = env.readFile(,"C:\\Users\\Varsha Kirani\\Documents\\Work\\DFKI\\LearningFlink\\flink-java-project\\input.txt");
        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path("C:\\Users\\Varsha Kirani\\Documents\\Work\\DFKI\\LearningFlink\\flink-java-project\\input.txt"));
        DataStream<String> inputStream = env.readFile(format,"C:\\Users\\Varsha Kirani\\Documents\\Work\\DFKI\\LearningFlink\\flink-java-project\\input.txt",
                                                        FileProcessingMode.PROCESS_CONTINUOUSLY,100, FilePathFilter.createDefaultFilter());

        //lines.print();
        inputStream.print();
        names.print();
        names2.print();

        env.execute();
    }
}
