package weeklyColection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import scheduleManager.Event;
import scheduleManager.Room;
import scheduleManager.ScheduleManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.util.*;

public class ScheduleManagerImp extends ScheduleManager {
    @Override
    public void loadScheduleFromJSONFile() {
        schedule = initializeSchedule();
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


                String ucionica = eventNode.get(fieldsNames.get(0)).asText();
                String dan = eventNode.get(fieldsNames.get(1)).asText();
                String termin = eventNode.get(fieldsNames.get(2)).asText();

                for (int i = 3; i < fieldsNames.size(); i++) {
                    additionalData.put(fieldsNames.get(i), eventNode.get(fieldsNames.get(i)).asText());
                }
                event = createEventFromFile(null, ucionica, dan, termin, additionalData);
                List<Event> lista = schedule.getSchedule();
                lista.add(event);
                schedule.getSchedule().add(event);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void loadScheduleFromCSVFile(){
        schedule = initializeSchedule();
        Scanner scanner = new Scanner(System.in);
        //getDatesAndExceptedDays();
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

                String ucionica = parts[0];
                String dan = parts[1];

                StringBuilder result = new StringBuilder();

                for (char c : dan.toCharArray()) {
                    if (c < 128) {
                        result.append(c);
                    }
                }
                dan = result.toString();
                dan = dan.trim();

                String termin = parts[2];
                for (int i = 3; i < parts.length; i++) {

                    additionalData.put(head[i], parts[i]);
                }

                event = createEventFromFile(null, ucionica, dan, termin, additionalData);
                schedule.getSchedule().add(event);

            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

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
}
