package com.manoj.bigdata.samples;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import scala.Tuple2;



public class SparkCassandraWrite {
	Logger LOG = LoggerFactory.getLogger(SparkCassandraWrite.class);
	
	public static class WordCount {
		private String word;
		private String count;
		public String getWord() {
			return word;
		}
		public void setWord(String word) {
			this.word = word;
		}
		public String getCount() {
			return count;
		}
		public void setCount(String count) {
			this.count = count;
		}
		
	}
		
	
	
	public static void main(String[] args) {
		
		long startTime = Calendar.getInstance().getTimeInMillis();
	    SparkConf conf = new SparkConf().setAppName("Spark Cassandra Write");
	    conf.setMaster("local[4]");//run on 4 cores locally
        conf.set("spark.cassandra.connection.host", ""); //set your cassandra IP address here
	    
	    JavaSparkContext sc = new JavaSparkContext(conf);
	    
	    JavaPairRDD<String,Integer> words = sc.textFile(args[0]).flatMap(new FlatMapFunction<String,String>(){
	    	
	    	public List<String> call(String line) {
	    		return Arrays.asList(line.split(" "));//split each line into words
	    	}
	    	
	    }).mapToPair(new PairFunction<String,String,Integer>() {
	    	public Tuple2 call(String word) {
	    		return new Tuple2<String,Integer>(word,1); //count each word
	    	}
	    });
	    
	    JavaPairRDD<String,Integer> wordcount = words.reduceByKey(new Function2<Integer,Integer,Integer>() {
	    	public Integer call(Integer val1 , Integer val2) {
	    		return val1+ val2;//for every word(i.e key) all the values are sent to this function. So just add them together to get word count
	    	}
	    });
	    
	    JavaRDD<WordCount> wc = wordcount.map(new Function<Tuple2<String,Integer>,WordCount>() {
	    	public WordCount call(Tuple2<String,Integer> row) {
	    		WordCount wc = new WordCount();
	    		if(StringUtils.isEmpty(row._1())) {
	    			wc.setWord("empty");
	    		} else {
	    			wc.setWord(row._1());
	    		}
	    		wc.setCount(row._2().toString());
	    		
	    		return wc;
	    	}
	    });
	    
	    javaFunctions(wc).writerBuilder("bigdata", "wordcountshakespeare",mapToRow(WordCount.class)).saveToCassandra();
	    
	    long endTime = Calendar.getInstance().getTimeInMillis();
	    System.out.println("Time taken for Spark MR Write to Cassandra:"+(endTime-startTime)/(1000)+" sec");

}
}
