import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class HotelReviewsDriver {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: HotelReviewsDriver <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "NYC Hotel Reviews Filter & Stats");
        job.setJarByClass(HotelReviewsDriver.class);

        job.setMapperClass(HotelReviewsMapper.class);
        job.setReducerClass(HotelReviewsReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Setup MultipleOutputs
        MultipleOutputs.addNamedOutput(job, "filtered", TextOutputFormat.class, Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job, "stats", TextOutputFormat.class, Text.class, Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}