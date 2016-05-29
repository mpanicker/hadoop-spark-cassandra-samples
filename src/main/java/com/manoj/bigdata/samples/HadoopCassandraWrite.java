package com.manoj.bigdata.samples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.hadoop.cql3.CqlConfigHelper;
import org.apache.cassandra.hadoop.cql3.CqlOutputFormat;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.datastax.driver.core.Row;



public class HadoopCassandraWrite {
	
	public static class Mapper1 extends Mapper<Object, Text, Text, IntWritable> {
		
		private final static IntWritable one = new IntWritable(1);
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			String[] words = value.toString().split(" ");
            for(String word:words) {
            	context.write(new Text(word), one);
            }	
			
		}
	}
	
	public static class Reducer1 extends Reducer<Text, IntWritable, Map<String, ByteBuffer>, List<ByteBuffer>> {
		private Map<String, ByteBuffer> keys; 
		
		protected void setup(org.apache.hadoop.mapreduce.Reducer.Context context) 
		        throws IOException, InterruptedException 
		        { 
		            keys = new HashMap<String, ByteBuffer>(); 
		        } 
		
		public void reduce(Text key, Iterable<IntWritable> values, Context context)throws IOException, InterruptedException{
			
			//Map<String,String> keys = new HashMap<String,String>();
			
			if(StringUtils.isEmpty(key.toString()) ) {
				keys.put("word", ByteBufferUtil.bytes("empty"));
			} else {
				keys.put("word", ByteBufferUtil.bytes(key.toString()));
			}	
			
			List<ByteBuffer> variables = new ArrayList<ByteBuffer>();
			
			int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            variables.add(ByteBufferUtil.bytes(String.valueOf(sum)));
            
            context.write(keys, variables);
            //context.write(key, new IntWritable(sum));
			
		}
		
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException{
		    
		}
	}
	
	public static void main(String[] args) throws Exception {
		long start_time = Calendar.getInstance().getTime().getTime();
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Hadoop Cassandra Write");
        job.setJarByClass(HadoopCassandraWrite.class);
        job.setMapperClass(Mapper1.class);
        //job.setCombinerClass(Reducer1.class);
        job.setReducerClass(Reducer1.class);
        
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Map.class);
        job.setOutputValueClass(List.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        
        
        //Cassandra configurations
        job.setOutputFormatClass(CqlOutputFormat.class);
        
        ConfigHelper.setOutputInitialAddress(job.getConfiguration(), "");//set your cassandra IP address here
        ConfigHelper.setOutputColumnFamily(job.getConfiguration(), "bigdata", "wordcountshakespeare");
        
        ConfigHelper.setOutputPartitioner(job.getConfiguration(), "Murmur3Partitioner");
        String query = "update bigdata.wordcountshakespeare set count=?";
        CqlConfigHelper.setOutputCql(job.getConfiguration(), query);
        
        boolean status = job.waitForCompletion(true) ;
        
        if(status) {
        	long end_time = Calendar.getInstance().getTime().getTime();
        	System.out.println("Time taken for Hadoop MR:"+(end_time - start_time)/1000+" seconds");
        } 
    }
}
