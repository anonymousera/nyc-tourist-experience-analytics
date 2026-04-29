import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

public class HotelReviewsReducer extends Reducer<Text, Text, Text, Text> {

    private MultipleOutputs<Text, Text> mos;

    @Override
    protected void setup(Context context) {
        mos = new MultipleOutputs<>(context);
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

        String keyStr = key.toString();

        if ("FILTERED".equals(keyStr)) {
            // Write all the full review data to filtered output
            for (Text val : values) {
                mos.write("filtered", null, val);
            }
        } else if ("STATS".equals(keyStr)) {
            // Calculate statistics for all 6 ratings
            int count = 0;
            double sumService = 0;
            double sumCleanliness = 0;
            double sumOverall = 0;
            double sumValue = 0;
            double sumLocation = 0;
            double sumSleepQuality = 0;

            for (Text val : values) {
                String line = val.toString();
                String[] parts = line.split("\t");

                if(parts.length < 7) continue;

                String service = parts[0];
                String cleanliness = parts[1];
                String overall = parts[2];
                String value = parts[3];
                String location = parts[4];
                String sleepQuality = parts[5];
                String rooms = parts[6];

                try {
                    sumService += service.isEmpty() ? 0 : Double.parseDouble(service);
                    sumCleanliness += cleanliness.isEmpty() ? 0 : Double.parseDouble(cleanliness);
                    sumOverall += overall.isEmpty() ? 0 : Double.parseDouble(overall);
                    sumValue += value.isEmpty() ? 0 : Double.parseDouble(value);
                    sumLocation += location.isEmpty() ? 0 : Double.parseDouble(location);
                    sumSleepQuality += sleepQuality.isEmpty() ? 0 : Double.parseDouble(sleepQuality);
                    sumRooms += rooms.isEmpty() ? 0 : Double.parseDouble(rooms);
                    count++;
                } catch (NumberFormatException e) {
                    // Skip invalid numbers
                }
            }

            if (count > 0) {
                mos.write("stats", new Text("Number of NYC Records"), new Text(String.valueOf(count)));
                mos.write("stats", new Text("Average Service Rating"), new Text(String.valueOf(sumService / count)));
                mos.write("stats", new Text("Average Cleanliness Rating"), new Text(String.valueOf(sumCleanliness / count)));
                mos.write("stats", new Text("Average Overall Rating"), new Text(String.valueOf(sumOverall / count)));
                mos.write("stats", new Text("Average Value Rating"), new Text(String.valueOf(sumValue / count)));
                mos.write("stats", new Text("Average Location Rating"), new Text(String.valueOf(sumLocation / count)));
                mos.write("stats", new Text("Average Sleep Quality Rating"), new Text(String.valueOf(sumSleepQuality / count)));
                mos.write("stats", new Text("Average Rooms Rating"), new Text(String.valueOf(sumRooms / count)));
            }
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        mos.close();
    }
}