import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class PopularityLeague extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PopularityLeague(), args);
		System.exit(res);
	}
	public static class IntArrayWritable extends ArrayWritable {
		public IntArrayWritable() {
			super(IntWritable.class);
		}

		public IntArrayWritable(Integer[] numbers) {
			super(IntWritable.class);
			IntWritable[] ints = new IntWritable[numbers.length];
			for (int i = 0; i < numbers.length; i++) {
				ints[i] = new IntWritable(numbers[i]);
			}
			set(ints);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		// TODO
		Configuration conf = this.getConf();
		FileSystem fs = FileSystem.get(conf);
		Path tmpPath = new Path("/mp2/tmpF");
		fs.delete(tmpPath, true);

		Job jobA = Job.getInstance(conf, "Link Count");
		jobA.setOutputKeyClass(IntWritable.class);
		jobA.setOutputValueClass(IntWritable.class);

		jobA.setMapperClass(LinkCountMap.class);
		jobA.setReducerClass(LinkCountReduce.class);

		FileInputFormat.setInputPaths(jobA, new Path(args[0]));
		FileOutputFormat.setOutputPath(jobA, tmpPath);

		jobA.setJarByClass(PopularityLeague.class);
		jobA.waitForCompletion(true);

        Job jobB = Job.getInstance(conf, "League Rank");
        jobB.setOutputKeyClass(IntWritable.class);
        jobB.setOutputValueClass(IntWritable.class);

        jobB.setMapOutputKeyClass(NullWritable.class);
        jobB.setMapOutputValueClass(IntArrayWritable.class);

        jobB.setMapperClass(LeagueRankMap.class);
        jobB.setReducerClass(LeagueRankReduce.class);
        jobB.setNumReduceTasks(1);

        FileInputFormat.setInputPaths(jobB, tmpPath);
        FileOutputFormat.setOutputPath(jobB, new Path(args[1]));

        jobB.setInputFormatClass(KeyValueTextInputFormat.class);
        jobB.setOutputFormatClass(TextOutputFormat.class);

        jobB.setJarByClass(PopularityLeague.class);
        return jobB.waitForCompletion(true) ? 0 : 1;

	}
	public static String readHDFSFile(String path, Configuration conf) throws IOException{
		Path pt=new Path(path);
		FileSystem fs = FileSystem.get(pt.toUri(), conf);
		FSDataInputStream file = fs.open(pt);
		BufferedReader buffIn=new BufferedReader(new InputStreamReader(file));

		StringBuilder everything = new StringBuilder();
		String line;
		while( (line = buffIn.readLine()) != null) {
			everything.append(line);
			everything.append("\n");
		}
		return everything.toString();
	}

	// TODO
	public static class LinkCountMap extends Mapper<Object, Text, IntWritable, IntWritable> {
		// TODO
		List<Integer> leagueList=new ArrayList<Integer>();
		List<String> leagueListStr;

		@Override
		protected void setup(Context context) throws IOException,InterruptedException {

			Configuration conf = context.getConfiguration();

			String leagueListPath = conf.get("league");            
			System.out.println(leagueListPath);
			leagueListStr = Arrays.asList(readHDFSFile(leagueListPath, conf).split("\n"));
			System.out.println(leagueListStr);
			for(String s : leagueListStr) 
			{
				leagueList.add(Integer.valueOf(s.trim()));
				
			}
		}


		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String delimiters=":";
			StringTokenizer tokenizer = new StringTokenizer(line, delimiters);        
			Integer pageID = Integer.valueOf(tokenizer.nextToken());
			if (leagueList.contains(pageID)) {
				context.write(new IntWritable(pageID), new IntWritable(0));
			}
			//System.out.println(pageID);
			String linkedPages=tokenizer.nextToken();
			StringTokenizer tokenizer2 = new StringTokenizer(linkedPages, " ");
			while (tokenizer2.hasMoreTokens()){
				String token2=tokenizer2.nextToken();
				Integer pageIDLinked = Integer.valueOf(token2);
				//System.out.println(pageIDLinked); 
				if (leagueList.contains(pageIDLinked)) {
					context.write(new IntWritable(pageIDLinked), new IntWritable(1)); 
				}
			}
		}
	}

	public static class LinkCountReduce extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
		// TODO
		@Override
		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}   	
			context.write( key,new IntWritable(sum));
		}

	}
	public static class LeagueRankMap extends Mapper<Text, Text, NullWritable, IntArrayWritable> {
		Integer N;
		 private TreeSet<Pair<Integer, Integer>> countToLinksMap =
	        		new TreeSet<Pair<Integer, Integer>>();

		
		// TODO
		 @Override
	        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
	            // TODO
	        	Integer count = Integer.parseInt(value.toString());
	        	Integer pageId = Integer.parseInt(key.toString());
	        	countToLinksMap.add(new Pair<Integer, Integer>(count,
	        			pageId));	        	
	        	}
	        
	        

	        @Override
	        protected void cleanup(Context context) throws IOException, InterruptedException {
	            // TODO
	        	for (Pair<Integer, Integer> item : countToLinksMap) {
	        		Integer[] pageIdCounts = {item.second,
	        		item.first};
	        		IntArrayWritable val = new
	        				IntArrayWritable(pageIdCounts);
	        		context.write(NullWritable.get(), val);
	        		}
	        }
	}

	public static class LeagueRankReduce extends Reducer<NullWritable, IntArrayWritable, IntWritable, IntWritable> {		
		private TreeSet<Pair<IntWritable, IntWritable>> countToLinksMap =
        		new TreeSet<Pair<IntWritable, IntWritable>>();
		
		// TODO
	    @Override
        public void reduce(NullWritable key, Iterable<IntArrayWritable> values, Context context) throws IOException, InterruptedException {
            // TODO
        	for (IntArrayWritable val: values) {
        		IntWritable[] pair= (IntWritable[]) val.toArray();
        		IntWritable pageId = pair[0];
        		IntWritable count =pair[1];
        		countToLinksMap.add(new Pair<IntWritable,
        				IntWritable>(count, pageId));        		
        		}
        		Integer rank=0;
        		Integer oldValue=0;
        		Integer newValue=0;
        		Integer actualRank=0;
        		for (Pair<IntWritable, IntWritable> item: countToLinksMap) {
        		IntWritable pageId =item.second;
        		IntWritable value = item.first;
        		newValue=value.get();
        		
        		if (!newValue.equals(oldValue))
        		{        			
        			actualRank=rank;
        		}
        		IntWritable rankWrite=new IntWritable(actualRank);
        		context.write(pageId, rankWrite);
        		rank++;
        		oldValue=newValue;
        		}
        }
	}
}
//>>> Don't Change
class Pair<A extends Comparable<? super A>,
B extends Comparable<? super B>>
implements Comparable<Pair<A, B>> {

	public final A first;
	public final B second;

	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public static <A extends Comparable<? super A>,
	B extends Comparable<? super B>>
	Pair<A, B> of(A first, B second) {
		return new Pair<A, B>(first, second);
	}

	@Override
	public int compareTo(Pair<A, B> o) {
		int cmp = o == null ? 1 : (this.first).compareTo(o.first);
		return cmp == 0 ? (this.second).compareTo(o.second) : cmp;
	}

	@Override
	public int hashCode() {
		return 31 * hashcode(first) + hashcode(second);
	}

	private static int hashcode(Object o) {
		return o == null ? 0 : o.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair))
			return false;
		if (this == obj)
			return true;
		return equal(first, ((Pair<?, ?>) obj).first)
				&& equal(second, ((Pair<?, ?>) obj).second);
	}

	private boolean equal(Object o1, Object o2) {
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ')';
	}
}
//<<< Don't Change