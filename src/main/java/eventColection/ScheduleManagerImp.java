package eventColection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.Getter;
import scheduleManager.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public class ScheduleManagerImp extends ScheduleManager {

    public Schedule getSchedule() {
        return schedule;
    }
    @Override
    protected void loadScheduleFromJSONFile() {
        schedule = new Schedule();
        Scanner scanner = new Scanner(System.in);
        //getDatesAndExceptedDays();
        Event event = null;

        String jsonFile = scanner.nextLine();
        Map<String, String> additionalData = new HashMap<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFile));
            ArrayNode eventsArray = (ArrayNode) rootNode;
            List<String> fieldsNames = new ArrayList<>();

            for (Iterator<String> it = eventsArray.get(0).fieldNames(); it.hasNext(); ) {
                String x = it.next();
                fieldsNames.add(x);
            }

            for (JsonNode eventNode : eventsArray) {
                additionalData.clear();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String ucionica = eventNode.get(fieldsNames.get(0)).asText();
                Date date = format.parse(eventNode.get(fieldsNames.get(1)).asText());

                String dan = eventNode.get(fieldsNames.get(2)).asText();
                String termin = eventNode.get(fieldsNames.get(3)).asText();

                for (int i = 4; i < fieldsNames.size(); i++) {
                    additionalData.put(fieldsNames.get(i), eventNode.get(fieldsNames.get(i)).asText());
                }
                event = createEventFromFile(date, null, ucionica, dan, termin, additionalData);
                schedule.getSchedule().add(event);

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void loadScheduleFromCSVFile(){
        schedule = new Schedule();
        Scanner scanner = new Scanner(System.in);
        //getDatesAndExceptedDays();
        String csvFile = scanner.nextLine();

        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            Map<String, String> additionalData = new HashMap<>();
            List<String[]> records = csvReader.readAll();
            String [] head = new String[0];
            boolean csv = false;
            Event event = null;

            for (String[] record : records) {
                additionalData.clear();
                String line = Arrays.toString(record);
                line = line.replace("[", "");
                line = line.replace("]", "");
                String[] parts = line.split(";");
                if (!csv) {
                    head = parts;
                    csv = true;
                    continue;
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date;
                String ucionica = parts[0];
                try {
                    date = format.parse(parts[1]);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                String dan = parts[2];

                StringBuilder result = new StringBuilder();

                for (char c : dan.toCharArray()) {
                    if (c < 128) {
                        result.append(c);
                    }
                }
                dan = result.toString();
                dan = dan.trim();

                String termin = parts[3];
                for (int i = 4; i < parts.length; i++) {

                    additionalData.put(head[i], parts[i]);
                }

                event = createEventFromFile(date, null, ucionica, dan, termin, additionalData);
                schedule.getSchedule().add(event);



            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }
}
