package com.manoj.bigdata.samples;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowTo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

public class SparkCassandraRead {
	Logger LOG = LoggerFactory.getLogger(SparkCassandraRead.class);
	
	public static class WordCount implements Serializable{
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
	    SparkConf conf = new SparkConf().setAppName("Spark Cassandra Read");
	    conf.setMaster("local[4]");//run on 4 cores locally
        conf.set("spark.cassandra.connection.host", ""); //set your cassandra IP address here
	    
	    JavaSparkContext sc = new JavaSparkContext(conf);
	    
	    JavaRDD<WordCount> wcRdd = javaFunctions(sc).cassandraTable( "bigdata", "wordcountshakespeare",mapRowTo(WordCount.class));
	    
	    JavaPairRDD<Integer,String> wordCountMax = wcRdd.mapToPair(new PairFunction<WordCount,Integer,String>() { //first create a Tuple of word_count and word from WordCount class
	    	public Tuple2<Integer,String> call(WordCount wc) {
	    		return new Tuple2<Integer,String>(new Integer(wc.getCount()),wc.getWord());
	    	}
	    }).sortByKey(false); //then sort by count desc(false means desc) 
	    
	    System.out.println("Word that occurs the most is:"+wordCountMax.first()._2+ " number of occurences:"+wordCountMax.first()._1); 
	    
	    long endTime = Calendar.getInstance().getTimeInMillis();
	    System.out.println("Time taken for Spark MR Read from Cassandra:"+(endTime-startTime)/(1000)+" sec");

}
}
