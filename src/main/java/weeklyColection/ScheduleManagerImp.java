package weeklyColection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import scheduleManager.Event;
import scheduleManager.Room;
import scheduleManager.Schedule;
import scheduleManager.ScheduleManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleManagerImp extends ScheduleManager {
    @Override
    protected void loadScheduleFromJSONFile() {
        schedule = initializeSchedule();
        Scanner scanner = new Scanner(System.in);
        //getDatesAndExceptedDays();
        Event event = null;
        String jsonFile = scanner.nextLine();
        Map<String, String> additionalData = new HashMap<>();

        try {
            //Učionica;Datum od;Datum do;Dan;Termin;Predmet;Tip;Nastavnik;Grupe
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
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date dateOd;
                Date dateTo;
                try {
                    dateOd = format.parse(eventNode.get(fieldsNames.get(1)).asText());
                    dateTo = format.parse(eventNode.get(fieldsNames.get(2)).asText());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                String dan = eventNode.get(fieldsNames.get(3)).asText();
                String termin = eventNode.get(fieldsNames.get(4)).asText();

                for (int i = 4; i < fieldsNames.size(); i++) {
                    additionalData.put(fieldsNames.get(i), eventNode.get(fieldsNames.get(i)).asText());
                }
                event = createEventFromFile(dateOd, dateTo, ucionica, dan, termin, additionalData);
                schedule.getSchedule().add(event);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void loadScheduleFromCSVFile(){
        schedule = initializeSchedule();
        Scanner scanner = new Scanner(System.in);
        //getDatesAndExceptedDays();
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
                Date dateOd;
                Date dateTo;
                String ucionica = parts[0];
                try {
                    dateOd = format.parse(parts[1]);
                    dateTo = format.parse(parts[2]);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                String dan = parts[3];

                StringBuilder result = new StringBuilder();

                for (char c : dan.toCharArray()) {
                    if (c < 128) {
                        result.append(c);
                    }
                }
                dan = result.toString();
                dan = dan.trim();

                String termin = parts[4];
                for (int i = 4; i < parts.length; i++) {

                    additionalData.put(head[i], parts[i]);
                }

                event = createEventFromFile(dateOd, dateTo, ucionica, dan, termin, additionalData);
                schedule.getSchedule().add(event);

            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }
}
