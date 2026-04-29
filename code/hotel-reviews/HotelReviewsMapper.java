import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HotelReviewsMapper extends Mapper<LongWritable, Text, Text, Text> {

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();
        Map<String, Object> data;
        
        try {
            data = jsonMapper.readValue(line, Map.class);
        } catch (Exception e) {
            return;
        }

        // Extract all the nested maps
        Map<String, Object> titleMap = (Map<String, Object>) data.get("title");
        Map<String, Object> textMap = (Map<String, Object>) data.get("text");
        Map<String, Object> offeringIdMap = (Map<String, Object>) data.get("offering_id");
        Map<String, Object> numHelpfulVotesMap = (Map<String, Object>) data.get("num_helpful_votes");
        Map<String, Object> reviewIdMap = (Map<String, Object>) data.get("review_id");
        Map<String, Object> hotelClassMap = (Map<String, Object>) data.get("hotel_class");
        Map<String, Object> regionIdMap = (Map<String, Object>) data.get("region_id");
        Map<String, Object> urlMap = (Map<String, Object>) data.get("url");
        // Map<String, Object> phoneMap = (Map<String, Object>) data.get("phone");
        // Map<String, Object> detailsMap = (Map<String, Object>) data.get("details");
        Map<String, Object> typeMap = (Map<String, Object>) data.get("type");
        Map<String, Object> nameMap = (Map<String, Object>) data.get("name");
        Map<String, Object> regionMap = (Map<String, Object>) data.get("region");
        Map<String, Object> localityMap = (Map<String, Object>) data.get("locality");
        Map<String, Object> streetAddressMap = (Map<String, Object>) data.get("street-address");
        Map<String, Object> zipcodeMap = (Map<String, Object>) data.get("zipcode");
        Map<String, Object> dateReviewedMap = (Map<String, Object>) data.get("date_reviewed");
        Map<String, Object> dateStayedMap = (Map<String, Object>) data.get("date_stayed");
        Map<String, Object> serviceMap = (Map<String, Object>) data.get("service");
        Map<String, Object> cleanlinessMap = (Map<String, Object>) data.get("cleanliness");
        Map<String, Object> overallMap = (Map<String, Object>) data.get("overall");
        Map<String, Object> valueMap = (Map<String, Object>) data.get("value");
        Map<String, Object> locationMap = (Map<String, Object>) data.get("location");
        Map<String, Object> sleepQualityMap = (Map<String, Object>) data.get("sleep_quality");
        Map<String, Object> roomsMap = (Map<String, Object>) data.get("rooms");
        Map<String, Object> fullAddressMap = (Map<String, Object>) data.get("full_address");

        if (textMap == null) return;

        // Iterate through each review index
        for (String reviewKey : textMap.keySet()) {
            // Extract text
            String text = textMap.get(reviewKey) != null ? textMap.get(reviewKey).toString() : "";
            
            // Extract ratings for null check
            String service = serviceMap != null && serviceMap.get(reviewKey) != null 
                ? serviceMap.get(reviewKey).toString() : "";
            String cleanliness = cleanlinessMap != null && cleanlinessMap.get(reviewKey) != null 
                ? cleanlinessMap.get(reviewKey).toString() : "";
            String overall = overallMap != null && overallMap.get(reviewKey) != null 
                ? overallMap.get(reviewKey).toString() : "";
            String valueRating = valueMap != null && valueMap.get(reviewKey) != null 
                ? valueMap.get(reviewKey).toString() : "";
            String locationRating = locationMap != null && locationMap.get(reviewKey) != null 
                ? locationMap.get(reviewKey).toString() : "";
            String sleepQualityRating = sleepQualityMap != null && sleepQualityMap.get(reviewKey) != null 
                ? sleepQualityMap.get(reviewKey).toString() : "";
            String roomsRating = roomsMap != null && roomsMap.get(reviewKey) != null 
                ? roomsMap.get(reviewKey).toString() : "";
            
            // Extract locality and region for null check
            String locality = localityMap != null && localityMap.get(reviewKey) != null 
                ? localityMap.get(reviewKey).toString() : "";
            String region = regionMap != null && regionMap.get(reviewKey) != null 
                ? regionMap.get(reviewKey).toString() : "";

            // NULL CHECK 1: Skip if all required ratings are missing
            if (text.isEmpty() && service.isEmpty() && cleanliness.isEmpty() && 
                overall.isEmpty() && valueRating.isEmpty() && locationRating.isEmpty() && 
                sleepQualityRating.isEmpty() && roomsRating.isEmpty()) {
                continue;
            }

            // NULL CHECK 2: Skip if both locality and region are missing
            if (locality.isEmpty() && region.isEmpty()) {
                continue;
            }

            // Filter by locality for NYC
            if (!"New York City".equals(locality)) continue;

            // Extract all other fields
            String title = titleMap != null && titleMap.get(reviewKey) != null 
                ? titleMap.get(reviewKey).toString() : "";
            String offeringId = offeringIdMap != null && offeringIdMap.get(reviewKey) != null 
                ? offeringIdMap.get(reviewKey).toString() : "";
            String numHelpfulVotes = numHelpfulVotesMap != null && numHelpfulVotesMap.get(reviewKey) != null 
                ? numHelpfulVotesMap.get(reviewKey).toString() : "";
            String reviewId = reviewIdMap != null && reviewIdMap.get(reviewKey) != null 
                ? reviewIdMap.get(reviewKey).toString() : "";
            String hotelClass = hotelClassMap != null && hotelClassMap.get(reviewKey) != null 
                ? hotelClassMap.get(reviewKey).toString() : "";
            String regionId = regionIdMap != null && regionIdMap.get(reviewKey) != null 
                ? regionIdMap.get(reviewKey).toString() : "";
            String url = urlMap != null && urlMap.get(reviewKey) != null 
                ? urlMap.get(reviewKey).toString() : "";
            // String phone = phoneMap != null && phoneMap.get(reviewKey) != null 
                // ? phoneMap.get(reviewKey).toString() : "";
            // String details = detailsMap != null && detailsMap.get(reviewKey) != null 
                // ? detailsMap.get(reviewKey).toString() : "";
            String type = typeMap != null && typeMap.get(reviewKey) != null 
                ? typeMap.get(reviewKey).toString() : "";
            String name = nameMap != null && nameMap.get(reviewKey) != null 
                ? nameMap.get(reviewKey).toString() : "";
            String streetAddress = streetAddressMap != null && streetAddressMap.get(reviewKey) != null 
                ? streetAddressMap.get(reviewKey).toString() : "";
            String zipcode = zipcodeMap != null && zipcodeMap.get(reviewKey) != null 
                ? zipcodeMap.get(reviewKey).toString() : "";
            String dateReviewed = dateReviewedMap != null && dateReviewedMap.get(reviewKey) != null 
                ? dateReviewedMap.get(reviewKey).toString() : "";
            String dateStayed = dateStayedMap != null && dateStayedMap.get(reviewKey) != null 
                ? dateStayedMap.get(reviewKey).toString() : "";
            String sleepQuality = sleepQualityMap != null && sleepQualityMap.get(reviewKey) != null 
                ? sleepQualityMap.get(reviewKey).toString() : "";
            String rooms = roomsMap != null && roomsMap.get(reviewKey) != null 
                ? roomsMap.get(reviewKey).toString() : "";
            String fullAddress = fullAddressMap != null && fullAddressMap.get(reviewKey) != null 
                ? fullAddressMap.get(reviewKey).toString() : "";

            // Create filtered line with all columns
            String filteredLine = title + "\t" + text + "\t" + offeringId + "\t" + 
                                 numHelpfulVotes + "\t" + reviewId + "\t" + hotelClass + "\t" + 
                                 regionId + "\t" + url + "\t" + type + "\t" + name + "\t" + region + "\t" + locality + "\t" +    //removed phone and details column 12/5
                                 streetAddress + "\t" + zipcode + "\t" + dateReviewed + "\t" + 
                                 dateStayed + "\t" + service + "\t" + cleanliness + "\t" + 
                                 overall + "\t" + valueRating + "\t" + locationRating + "\t" + 
                                 sleepQuality + "\t" + rooms + "\t" + fullAddress;
            
            // Create stats line with ratings only
            String statsLine = service + "\t" + cleanliness + "\t" + overall + "\t" + 
                              valueRating + "\t" + locationRating + "\t" + sleepQuality + "\t" + rooms;
            
            context.write(new Text("FILTERED"), new Text(filteredLine));
            context.write(new Text("STATS"), new Text(statsLine));
        }
    }
}