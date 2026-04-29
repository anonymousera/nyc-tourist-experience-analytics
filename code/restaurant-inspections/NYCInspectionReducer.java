import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class NYCInspectionReducer extends Reducer<Text, Text, NullWritable, Text> {

    private static final String[] OutputCSVCol = {
        "INSPECTION DATE", "CAMIS", "DBA", 
        "BORO", "BUILDING", "STREET",
        "ZIPCODE", "PHONE", "CUISINE DESCRIPTION",
        "ACTION", "VIOLATION CODE", "VIOLATION DESCRIPTION",
        "CRITICAL FLAG", "SCORE", "GRADE",
        "INSPECTION TYPE"
    };

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        String firstColEntry = String.join(",", OutputCSVCol);
        context.write(NullWritable.get(), new Text(firstColEntry));
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String eachKy = key.toString();

        for(Text vl: values){
            String outVl = vl.toString();
            context.write(NullWritable.get(), new Text(outVl));
        }
    }
}