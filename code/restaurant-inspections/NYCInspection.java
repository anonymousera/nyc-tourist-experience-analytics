import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class NYCInspection {

    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.err.println("Program: NYCInspection path_of_input_csv path_of_output");
            System.exit(-1);
        }

        Job job = Job.getInstance();

        job.setJarByClass(NYCInspection.class);
        job.setJobName("NYC Restaurant MapReduce job");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(NYCInspectionMapper.class);
        job.setReducerClass(NYCInspectionReducer.class);

        job.setNumReduceTasks(1);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // Different output format for mapper and reducer
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}