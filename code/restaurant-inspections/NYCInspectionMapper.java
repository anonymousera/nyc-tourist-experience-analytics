import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
    References:

    1.  SimpleDateFormat, OFFICIAL DOC - https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
        Used for easy date parsiing with month, day and year format.

    2.  Apache Commons CSV, OFFICIAL DOC - https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html#DEFAULT
        Used for CSV parsing with ',' as delimiter and ignoring empty lines.
        The official page - https://commons.apache.org/proper/commons-csv/

    3. Date comparison logic - https://stackoverflow.com/questions/494180/how-do-i-check-if-a-date-is-within-a-certain-range

    4. Escape sequence handling for CSV - https://ssojet.com/escaping/csv-escaping-in-java/
*/


public class NYCInspectionMapper extends Mapper<LongWritable, Text, Text, Text> {

    /*
        [1]
    */
    private static final SimpleDateFormat InspectionDataFomatterClass = new SimpleDateFormat("MM/dd/yyyy");

    private static final String[] InputCSVCol = {
        "CAMIS", "DBA", "BORO", 
        "BUILDING", "STREET", "ZIPCODE", 
        "PHONE", "CUISINE DESCRIPTION", "INSPECTION DATE", 
        "ACTION", "VIOLATION CODE", "VIOLATION DESCRIPTION", 
        "CRITICAL FLAG", "SCORE", "GRADE", "GRADE DATE", "RECORD DATE", 
        "INSPECTION TYPE", "Latitude", "Longitude", 
        "Community Board", "Council District", "Census Tract",
        "BIN", "BBL", "NTA", 
        "Location"
    };

    // Output CSV columns.
    private static final String[] OutputCSVCol = {
        "INSPECTION DATE", "CAMIS", "DBA", 
        "BORO", "BUILDING", "STREET",
        "ZIPCODE", "PHONE", "CUISINE DESCRIPTION",
        "ACTION", "VIOLATION CODE", "VIOLATION DESCRIPTION",
        "CRITICAL FLAG", "SCORE", "GRADE",
        "INSPECTION TYPE"
    };
    
    private int getColToIndexMapping(String colNm){
        int idx = -1;

        for (int i = 0; i < InputCSVCol.length; i++) {
            if (InputCSVCol[i].equals(colNm)) {
                idx = i;
                return idx;
            }
        }

        return -1;
    }

    private String valLookup(CSVRecord rcd, String colNm){
        int idx = getColToIndexMapping(colNm);

        if(idx == -1){
            return null;
        }
        return rcd.get(idx).trim();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        String ip = value.toString();

        if(ip == null || ip.isEmpty()){
            return;
        }

        context.getCounter("Total Records", "Total Input Records").increment(1);

        try {
                /*
                    [2]
                */
                CSVParser parser = CSVParser.parse(new StringReader(ip), CSVFormat.DEFAULT);

                List<CSVRecord> tempOp = parser.getRecords();

                if(tempOp.get(0).get(0).startsWith("CAMIS")){
                    return;
                }

            CSVRecord op = tempOp.get(0);

            // SCORE validation.
            String scr = valLookup(op, "SCORE");

            if(scr == null || scr.isEmpty()){
                return;
            }
            try{
                double tmp = Double.parseDouble(scr);
            }catch(Exception e){
                return;
            }

            // Record selection before 1st January 2015
            String inscpDate = valLookup(op, "INSPECTION DATE");
            if(inscpDate == null || inscpDate.isEmpty()){
                return;
            }
            try{
                Date dt = InspectionDataFomatterClass.parse(inscpDate);
                Date DateLimit = InspectionDataFomatterClass.parse("01/01/2015");

                /*
                    [3]
                */
                if(dt.before(DateLimit)){
                    context.getCounter("Date Time Counter", "Dropped rows for before my analysis limit").increment(1);
                    return;
                }
            }catch(Exception e){
                return;
            }

            // ZIPCODE validation
            String zpCode = valLookup(op, "ZIPCODE");
            if(zpCode == null || zpCode.trim().isEmpty()){
                context.getCounter("ZIPCODE Related Counter", "Dropped rows for invalid ZIPCODE").increment(1);
                return;
            }

            // VIOLATION CODE validation
            String violCode = valLookup(op, "VIOLATION CODE");
            if(violCode == null || violCode.trim().isEmpty()){
                context.getCounter("VIOLATION Related Counter", "Dropped rows for invalid VIOLATION CODE").increment(1);
                return;
            }

            Map<String, String> outputRcd = new HashMap<>();

            for(String colEntry : OutputCSVCol){
                String csvField = valLookup(op, colEntry);

                if(csvField == null){
                    csvField = "";
                }

                if(colEntry.equals("VIOLATION DESCRIPTION")){
                    if((csvField == null || csvField.trim().isEmpty())){
                        csvField = "";
                    }
                    csvField = csvField.trim();
                }

                if(colEntry.equals("BUILDING")){
                    if(csvField == null || csvField.trim().isEmpty()){
                        csvField = "";
                    }
                    csvField = csvField.trim();
                }

                // Handle invalid Phone Number
                if(colEntry.equals("PHONE")){
                    if(csvField == null || csvField.trim().isEmpty()){
                        csvField = "0";
                    }
                    csvField = csvField.trim();
                }

                if(colEntry.equals("GRADE")){
                    if(csvField == null || csvField.trim().isEmpty()){
                        // Handle invalid GRADE
                        double scoreVal = Double.parseDouble(scr);
                        if(scoreVal >= 0 && scoreVal <= 13){
                            csvField = "A";
                        }else if(scoreVal > 13 && scoreVal <= 27){
                            csvField = "B";
                        }else if(scoreVal > 27){
                            csvField = "C";
                        }
                    }
                    else{
                        csvField = csvField.trim();
                    }
                }

                if(colEntry.equals("SCORE")){
                    if(csvField == null || csvField.trim().isEmpty()){
                        csvField = "0.0";
                    }
                    else{
                        double tmp = Double.parseDouble(csvField);
                        int intScr = (int) tmp;
                        csvField = Integer.toString(intScr);
                    }
                }

                outputRcd.put(colEntry, csvField);
            }

            List<String> outputValues = new ArrayList<>();
            for (String colName : OutputCSVCol) {
                String tmp = outputRcd.get(colName);

                /*
                    [4]
                */
                if (tmp.matches(".*[\",].*")) {
                    String reformat = tmp.replace("\"", "\"\"");
                    tmp = "\"" + reformat + "\"";
                }
                outputValues.add(tmp);
            }
            
            String finalOpRow = String.join(",", outputValues);

            if(finalOpRow.isEmpty()){
                return;
            }
            else{
                context.write(new Text(""), new Text(finalOpRow));

                context.getCounter("Total Records", "Valid record").increment(1);

                String eachBorough = valLookup(op, "BORO");

                String gradeVal = outputRcd.get("GRADE");

                context.getCounter("Total Valid Records Per Borough", eachBorough).increment(1);

                if(gradeVal.equalsIgnoreCase("A")){
                    context.getCounter("Total Valid Records Per Borough - Grade A", eachBorough).increment(1);
                }

                if(outputRcd.get("CUISINE DESCRIPTION").equalsIgnoreCase("American")){
                    context.getCounter("Total Valid Records Per Borough - Cuisine American", eachBorough).increment(1);
                }
            }
        } catch (Exception e) {
            return;
        }
    }
}
