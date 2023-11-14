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


    @Override
    public boolean isRoomOccupied(Room room, Date date, Time startTime, Time endTime) {
        return false;
    }

    @Override
    public boolean isEventOccupied(Event event) {
        return false;
    }


    @Override
    public boolean saveScheduleToFile(String filePath) {
        return false;
    }

    @Override
    public boolean saveScheduleToCSV(String filePath) {
        return false;
    }

    @Override
    public void loadScheduleFromJSONFile() {
        schedule = new Schedule();
        Scanner scanner = new Scanner(System.in);
        getDatesAndExceptedDays();
        Event event = null;

        System.out.println("Unesite putanju do JSON fajla:");
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

                Date date = new Date(eventNode.get(fieldsNames.get(0)).asText());
                String ucionica = eventNode.get(fieldsNames.get(1)).asText();
                String dan = eventNode.get(fieldsNames.get(2)).asText();
                String termin = eventNode.get(fieldsNames.get(3)).asText();

                for (int i = 4; i < fieldsNames.size(); i++) {
                    additionalData.put(fieldsNames.get(i), eventNode.get(fieldsNames.get(i)).asText());
                }
                event = createEventFromFile(date, ucionica, dan, termin, additionalData);
                schedule.getSchedule().add(event);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //addDateToEvents();
    }
    @Override
    public void loadScheduleFromCSVFile(){
        schedule = new Schedule();
        Scanner scanner = new Scanner(System.in);
        getDatesAndExceptedDays();
        System.out.println("Unesite putanju do fajla:");
        String csvFile = scanner.nextLine();

        List<Event> events = new ArrayList<>();
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
                try {
                    date = format.parse(parts[0]);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                String ucionica = parts[1];
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

                event = createEventFromFile(date, ucionica, dan, termin, additionalData);

                schedule.getSchedule().add(event);

            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        //addDateToEvents();
    }
}
