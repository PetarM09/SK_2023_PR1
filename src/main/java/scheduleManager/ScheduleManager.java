package scheduleManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import jdk.jfr.EventType;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
@Getter
@Setter

public abstract class ScheduleManager {
    protected List<Room>rooms = new ArrayList<>();
    protected String input;
    protected static Schedule schedule;

    public abstract void initializeSchedule();
    public abstract List<Event> sreachSchedule(String criteria);
    public abstract boolean isRoomOccupied(Room room, Date date, Time startTime, Time endTime);
    public abstract boolean isEventOccupied(Event event);
    public abstract boolean saveScheduleToFile(String filePath);
    public abstract boolean saveScheduleToCSV(String filePath);

    protected Event parser(String input){
        // Primer formata stringa: "Soba123,2023-10-15,08:00,10:00, Dodatne informacije1:2, Dodatna informacija 2:5"
        // Primer formata stringa: "Soba123,2023-10-15,08:00,2,Dodatne informacije1:2, Dodatna informacija 2:5"
        // Primer formata stringa: "Soba123,2023-10-15,08:00,10:00"
        // Primer formata stringa: "Soba123,2023-10-15,08:00,2"

        this.input = input;
        String[] parts = input.split(",");
        String roomName = parts[0];
        Room room = null;
        for(Room r : rooms){
            if(r.getName().equals(roomName)){
                 room = r;
                 break;
            }
        }
        if(room == null){
            room = new Room(); //TODO srediti praznu sobu???
            room.setName(roomName);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = format.parse(parts[1]);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        LocalDate localDate = LocalDate.parse(parts[1]);

        Time startTime = Time.valueOf(parts[2].concat(":00"));
        Time endTime;
        if(parts[3].contains(":")){
            endTime = Time.valueOf(parts[3].concat(":00"));
        }else{
            double duration = Double.parseDouble(parts[3]);
            endTime = new Time((long) (startTime.getTime() + duration * 60 * 60 * 1000));
        }

        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        if(parts.length == 4){
            return new Event(date, room, startTime, endTime, dayOfWeek);
        }
        Map<String, String> additionalData = new HashMap<>();
        for(int i = 4; i < parts.length; i++){
            String [] token = parts[i].split(":");
            additionalData.put(token[0], token[1]);
        }
        return new Event(date, room, startTime, endTime, dayOfWeek, additionalData);
    }

    protected Event findEvent(String input){


            //Room:Vremepocetka:Datum
            Event found;
            String[] parts = input.split(",");
            LocalDate localDate = LocalDate.parse(parts[1]);
            DayOfWeek day = localDate.getDayOfWeek();
            String room = parts[0];
            Time time = Time.valueOf(parts[2].concat(":00"));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            try {
                date = format.parse(parts[1]);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            //Lista eventova za taj dan
            List<Event> lista = schedule.getSchedule().get(day);
            for (Event event : lista) {
                if (event.getRoom().getName().equals(room)) {
                    if (event.getStartTime().equals(time)) {
                        return event;
                    }
                }
            }

        return null;
    }

    public boolean addEvent(String input) {
        Event event = parser(input);
        List<Event> lista = this.schedule.getSchedule().get(event.getDayOfWeek());
        lista.add(event);
        this.schedule.getSchedule().put(event.getDayOfWeek(), lista);
        return true;
    }

    public void removeEvent(String input) {
        //Ime eventa:Vremepocetka:Datum
        Event toRemove = findEvent(input);
        List<Event> lista = this.schedule.getSchedule().get(toRemove.getDayOfWeek());
        lista.remove(toRemove);
        this.schedule.getSchedule().put(toRemove.getDayOfWeek(), lista);
    }

    public void updateEvent(String input) {
        //Room:Vremepocetka:Datum
        //Soba123,2023-10-15,08:00,10:00,nedelja,Matematika,Profesor X,Predavanje,GrupaA,Dodatne informacije1:2, Dodatna informacija 2:5

        Event toUpdate = findEvent(input);

        System.out.println("Unesite izmenjen event: \n");
        Scanner scanner = new Scanner(System.in);
        String izmenjenEvent = scanner.nextLine();

        Event novi = parser(izmenjenEvent);

        List<Event> lista = this.schedule.getSchedule().get(toUpdate.getDayOfWeek());
        lista.remove(toUpdate);
        lista.add(novi);
        this.schedule.getSchedule().put(novi.getDayOfWeek(), lista);

    }
    public void addRoom(String input){
        ///Soba123,capacity
        ///Soba123,capacity, Dodatne informacije1:2, Dodatna informacija 2:5

        String[] parts = input.split(",");
        String roomName = parts[0];
        int capacity = Integer.parseInt(parts[1]);
        Map<String, String> additionalData = new HashMap<>();

        for(int i = 2; i < parts.length; i++){
            String [] token = parts[i].split(":");
            additionalData.put(token[0], token[1]);
        }
        Room room = new Room(roomName, capacity, additionalData);
        this.rooms.add(room);
    }

    public void removeRoom(String input){
        for(var x : this.rooms)
            if(x.getName().equals(input)) {
                this.rooms.remove(x);
                return;
            }
    }

    private List<Date> getDatesAndExceptedDays(){
        Scanner scanner = new Scanner(System.in);
        List<Date> listaDatuma = new ArrayList<>();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            listaDatuma.add(format.parse(scanner.nextLine()));
            listaDatuma.add(format.parse(scanner.nextLine()));
            System.out.println("Unesite datume koje zelite da izuzmete (END za kraj):");
            String izuzati_dani = scanner.nextLine();
            while(!izuzati_dani.equals("END")){
                listaDatuma.add(format.parse(izuzati_dani));
                izuzati_dani = scanner.nextLine();
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return listaDatuma;
    }

    private Event createEventFromFile(String ucionica, String dan, String termin, Map<String, String> additionalData){
        Event event = null;
        Room room = new Room();
        room.setName(ucionica);
        Date date = new Date();

        if(termin.contains("-")) {
            String[] token = termin.split("-");
            if (token[0].contains(":") && !token[0].matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {
                token[0] = token[0].concat(":00");
            } else if (!token[0].matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {
                token[0] = token[0].concat(":00:00");
            }
            if (token[1].contains(":") && !token[1].matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {
                token[1] = token[1].concat(":00");
            } else if (!token[1].matches("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")) {
                token[1] = token[1].concat(":00:00");
            }
            Time startTime = Time.valueOf(token[0]);
            Time endTime = Time.valueOf(token[1]);
            event = new Event(room, startTime, endTime, DayOfWeek.valueOf(dan));
            HashMap<String, String> map = (HashMap<String, String>) event.getAdditionalData();
            map.putAll(additionalData);
            System.out.println(event);


        }
        return event;
    }

    public void loadScheduleFromCSVFile(){
        schedule = new Schedule();
        Scanner scanner = new Scanner(System.in);
        List<Date> listaDatuma = new ArrayList<>();
        listaDatuma = getDatesAndExceptedDays();
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

                event = createEventFromFile(ucionica, dan, termin, additionalData);

                List<Event> lista = schedule.getSchedule().get(event.getDayOfWeek());
                lista.add(event);
                schedule.getSchedule().put(event.getDayOfWeek(), lista);

            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void loadScheduleFromJSONFile() {
        schedule = new Schedule();
        Scanner scanner = new Scanner(System.in);
        Event event = null;

        List<Date> listaDatuma = new ArrayList<>();
        listaDatuma = getDatesAndExceptedDays();

        System.out.println("Unesite putanju do JSON fajla:");
        String jsonFile = scanner.nextLine();
        Map<String, String> additionalData = new HashMap<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFile));

            // Prikupljanje datuma
            ArrayNode eventsArray = (ArrayNode) rootNode;
            List<String> fieldsNames = new ArrayList<>();
            for (Iterator<String> it = eventsArray.get(0).fieldNames(); it.hasNext(); ) {
                String x = it.next();
                fieldsNames.add(x);
            }

            // Prikupljanje događaja
            for (JsonNode eventNode : eventsArray) {
                additionalData.clear();


                String ucionica = eventNode.get(fieldsNames.get(0)).asText();
                String dan = eventNode.get(fieldsNames.get(1)).asText();
                String termin = eventNode.get(fieldsNames.get(2)).asText();

                for (int i = 3; i < fieldsNames.size(); i++) {
                    additionalData.put(fieldsNames.get(i), eventNode.get(fieldsNames.get(i)).asText());
                }
                event = createEventFromFile(ucionica, dan, termin, additionalData);
                List<Event> lista = schedule.getSchedule().get(event.getDayOfWeek());
                lista.add(event);
                schedule.getSchedule().put(event.getDayOfWeek(), lista);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToPDF(String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Dodajte naslov
            document.add(new Paragraph("Raspored"));

            // Dodajte podatke o rasporedu u PDF
            for (Map.Entry<DayOfWeek, List<Event>> entry : schedule.getSchedule().entrySet()) {
                document.add(new Paragraph("\n" + entry.getKey().toString()));
                for (Event event : entry.getValue()) {
                    document.add(new Paragraph(event.getRoom().getName() + " | " +
                            event.getStartTime() + "-" + event.getEndTime() + " | " +
                            "Predmet: " + event.getAdditionalData().get("Predmet") + " | " +
                            "Tip: " + event.getAdditionalData().get("Tip") + " | " +
                            "Nastavnik: " + event.getAdditionalData().get("Nastavnik") + " | " +
                            "Grupe: " + event.getAdditionalData().get("Grupe")));
                }
            }
            document.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    //Provera da li konkretan termin vec postoji
    public boolean doesEventExist(String input){

        if (findEvent(input) != null){
            System.out.println("Termin koji ste uneli je zauzet.\n");
            return true;
        }

        return false;
    }

    //Pretrazivanje termina po additionalData podacima
    public List searchAdditionalData(String input){
        //Ocekuje se format Key:Value

        String[] parts = input.split(":");
        List<Event> found = new ArrayList<>();

        //mapa svih eventova
        Map mapa = schedule.getSchedule();

        for (Map.Entry<DayOfWeek, List<Event>> entry: schedule.getSchedule().entrySet()){
            //Lista eventova za taj dan
            List<Event> listaZaDan = entry.getValue();

                for (Event event : listaZaDan){
                    //Additional data za event
                    Map<String, String > data = event.getAdditionalData();

                    //Sadrzi onaj additional data po kom se pretrazuje
                    if (data.containsKey(parts[0])){
                        found.add(event);
                    }
                }
        }
        return found;
    }

    public Schedule getSchedule(){
        return schedule;
    }




}
