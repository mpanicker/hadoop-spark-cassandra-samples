package com.manoj.bigdata.samples;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.hadoop.cql3.CqlConfigHelper;
import org.apache.cassandra.hadoop.cql3.CqlInputFormat;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.datastax.driver.core.Row;

public class HadoopCassandraRead {
	
	public static class Mapper1 extends Mapper<Long,Row, Text, IntWritable> {
		public void map(Long keys,Row rows, Context context) throws IOException, InterruptedException{
			context.write(new Text(rows.getString(0)), new IntWritable(new Integer(rows.getString(1))));
		}
	}
	
	public static class Reducer1 extends Reducer<Text, IntWritable, Text, IntWritable> {
		Text maxWord = new Text();
		int max = 0;
		public void reduce(Text key, Iterable<IntWritable> values, Context context)throws IOException, InterruptedException{
			int sum = 0;
			for(IntWritable count: values) {
				sum += count.get();
			}
			
			if(sum > max) {
				max = sum;
				maxWord.set(key);
			}
			
		}
		
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException{
		    context.write(new Text(maxWord), new IntWritable(max));
		}
	}
	
	public static void main(String[] args) throws Exception {
		long start_time = Calendar.getInstance().getTime().getTime();
		Configuration conf = new Configuration();
		
		// configuration should contain reference to your namenode
		FileSystem fs = FileSystem.get(conf);
		// true stands for recursively deleting the folder you gave
		fs.delete(new Path(args[0]), true);
		
		Job job = Job.getInstance(conf,"aggregation");
		job.setJarByClass(HadoopCassandraRead.class);
		job.setMapperClass(Mapper1.class);
		job.setReducerClass(Reducer1.class);
		job.setInputFormatClass(CqlInputFormat.class);
		
		ConfigHelper.setInputInitialAddress(job.getConfiguration(), "192.168.222.137");
		ConfigHelper.setInputColumnFamily(job.getConfiguration(), "bigdata", "wordcountshakespeare");
		ConfigHelper.setInputPartitioner(job.getConfiguration(), "Murmur3Partitioner");
		CqlConfigHelper.setInputCQLPageRowSize(job.getConfiguration(), "3");
		
		job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        //job.setMapOutputKeyClass(Text.class);
        //job.setMapOutputValueClass(IntWritable.class);
        
        FileOutputFormat.setOutputPath(job, new Path(args[0]));
        
        boolean status = job.waitForCompletion(true) ;
        
        if(status) {
        	long end_time = Calendar.getInstance().getTime().getTime();
        	System.out.println("Time taken for Hadoop MR:"+(end_time - start_time)/1000+" seconds");
        } 
		
		
	}

}
