package scheduleManager;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
    public static Schedule schedule = null;
    protected abstract void loadScheduleFromJSONFile();
    protected abstract void loadScheduleFromCSVFile();

    protected static Schedule initializeSchedule(){
        if (schedule == null) {
            synchronized (Schedule.class) {
                if (schedule == null)
                    schedule = new Schedule();
            }
        }
        return schedule;
    }
    public void ispis(){
        for (Event event : schedule.getSchedule()) {
            System.out.println(event);
        }
    }
    private Event parser(String input){
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
            room = new Room();
            room.setName(roomName);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        Date dateTo = null;
        try {
            date = format.parse(parts[1]);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        int i = 0;

        if(parts[2].matches("\\d{1,4}-\\d{1,2}-\\d{1,2}")) {
            i = 1;
            try {
                dateTo = format.parse(parts[2]);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        LocalDate localDate = LocalDate.parse(parts[1]);

        Time startTime = Time.valueOf(parts[2+i].concat(":00"));
        Time endTime;
        if(parts[3+i].contains(":")){
            endTime = Time.valueOf(parts[3+i].concat(":00"));
        }else{
            double duration = Double.parseDouble(parts[3+i]);
            endTime = new Time((long) (startTime.getTime() + duration * 60 * 60 * 1000));
        }

        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        if(parts.length == 4+i){
            return new Event(date, room, startTime, endTime, dayOfWeek);
        }
        Map<String, String> additionalData = new HashMap<>();
        for(int j = 4+i; j < parts.length; j++){
            String [] token = parts[j].split(":");
            additionalData.put(token[0], token[1]);
        }
        if(i == 0)
            return new Event(date, room, startTime, endTime, dayOfWeek, additionalData);
        else
            return new Event(date, dateTo, room, startTime, endTime, dayOfWeek, additionalData);
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
            List<Event> lista = schedule.getSchedule();
            for (Event event : lista) {
                if (event.getRoom().getName().equals(room)) {
                    if (event.getStartTime().equals(time)) {
                        if (event.getDayOfWeek().equals(day)) {
                            if (event.getDate() != null && event.getDate().equals(date))
                                return event;
                        }
                    }
                }
            }

        return null;
    }

    public boolean addEvent(String input) {
        Event event = parser(input);
        schedule.getSchedule().add(event);
        return true;
    }

    public void removeEvent(String input) {
        //Ime eventa:Vremepocetka:Datum
        Event toRemove = findEvent(input);
        schedule.getSchedule().remove(toRemove);
    }

    public void updateEvent(String input) {
        //Room:Vremepocetka:Datum
        //Soba123,2023-10-15,08:00,10:00,nedelja,Matematika,Profesor X,Predavanje,GrupaA,Dodatne informacije1:2, Dodatna informacija 2:5

        Event toUpdate = findEvent(input);

        System.out.println("Unesite izmenjen event: \n");
        Scanner scanner = new Scanner(System.in);
        String izmenjenEvent = scanner.nextLine();

        Event novi = parser(izmenjenEvent);

        schedule.getSchedule().remove(toUpdate);
        schedule.getSchedule().add(novi);
    }
    
    public void loadRoomsFromFile(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Unesite putanju do fajla sa ucionicama: ");
        String csvFile = scanner.nextLine();

        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            Map<String, String> additionalData = new HashMap<>();
            List<String[]> records = csvReader.readAll();
            String [] head = new String[0];
            boolean csv = false;
            Room room = null;

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
                String roomName = parts[0];
                int capacity = Integer.parseInt(parts[1]);

                for (int i = 2; i < parts.length; i++) {

                    additionalData.put(head[i], parts[i]);
                }
                room = new Room(roomName, capacity, additionalData);
                System.out.println(room);
                this.rooms.add(new Room(roomName, capacity, additionalData));
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
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

    protected void getDatesAndExceptedDays(){
        Scanner scanner = new Scanner(System.in);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            schedule.setStartDate(format.parse(scanner.nextLine()));
            schedule.setEndDate(format.parse(scanner.nextLine()));
            System.out.println("Unesite datume koje zelite da izuzmete (END za kraj):");
            String izuzati_dani = scanner.nextLine();
            while(!izuzati_dani.equals("END")){
                schedule.getExceptions().add(format.parse(izuzati_dani));
                izuzati_dani = scanner.nextLine();
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Event createEventFromFile(Date date, Date dateTo, String ucionica, String dan, String termin, Map<String, String> additionalData){
        Event event = null;
        Room room = null;
        for(Room r : rooms){
            if(r.getName().equals(ucionica)){
                room = r;
                break;
            }
        }
        if(room == null){
            room = new Room();
            room.setName(ucionica);
        }
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
            if(dateTo == null)
                event = new Event(date, room, startTime, endTime, DayOfWeek.valueOf(dan));
            else
                event = new Event(date, dateTo, room, startTime, endTime, DayOfWeek.valueOf(dan));
            HashMap<String, String> map = (HashMap<String, String>) event.getAdditionalData();
            map.putAll(additionalData);
        }
        return event;
    }

    private List<Event> printCriteria() throws ParseException {
        System.out.println("Unesite kriterijum sortiranja: ");
        System.out.println("1. po datumu");
        System.out.println("2. grupisano po danima");
        System.out.println("3. za odredjeni period");
        System.out.println("4. ostali kriterijumi");
        Scanner scanner = new Scanner(System.in);
        List<Event> events = null;
        String kriterijum = scanner.nextLine();

        if(kriterijum.equals("1"))
            return new ArrayList<>(schedule.sortByDate());
        else if (kriterijum.equals("2")) {
            return new ArrayList<>(schedule.sortByDayOfWeekDay());
        } else if (kriterijum.equals("3")) {
            System.out.println("Unesite pocetni datum: ");
            String pocetniDatum = scanner.nextLine();
            System.out.println("Unesite krajnji datum: ");
            String krajnjiDatum = scanner.nextLine();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date pocetak = format.parse(pocetniDatum);
            Date kraj = format.parse(krajnjiDatum);
            return new ArrayList<>(schedule.scheduleFromDateToDate(pocetak, kraj));
        } else if (kriterijum.equals("4")) {
            System.out.println("Unesite kriterijum: ");
            String kriterijum1 = scanner.nextLine();
            return new ArrayList<>(schedule.sortByAdditionalData(kriterijum1));
        }
        return events;
    }

    public void loadScheduleFromFile(){
        System.out.println("Kog formata je raspored koji zelite da ucitate?");
        System.out.println("1. JSON");
        System.out.println("2. CSV");
        Scanner scanner = new Scanner(System.in);
        String format = scanner.nextLine();
        System.out.println("Unesite putanju do fajla: ");
        if(format.equals("1"))
            loadScheduleFromJSONFile();
        else if(format.equals("2"))
            loadScheduleFromCSVFile();
    }

    public void saveSchedule(){
        System.out.println("U koji format zelite da sacuvate raspored?");
        System.out.println("1. JSON");
        System.out.println("2. CSV");
        System.out.println("3. PDF");
        Scanner scanner = new Scanner(System.in);
        String format = scanner.nextLine();
        System.out.println("Unesite putanju do fajla: ");
        if(format.equals("1"))
            saveToJson(scanner.nextLine());
        else if(format.equals("2"))
            saveToCsv(scanner.nextLine());
        else if(format.equals("3"))
            saveToPDF(scanner.nextLine());
    }
    private void saveToJson(String filePath){
        try {
            List<Event> events = printCriteria();

            if(events == null)
                events = schedule.getSchedule();

            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            for (Event event : events) {
                ObjectNode formattedJson = objectMapper.createObjectNode();

                if(event.getDateTo() != null) {
                    formattedJson.put("Datum od", dateFormat.format(event.getDate()));
                    formattedJson.put("Datum do", dateFormat.format(event.getDateTo()));
                }else {
                    formattedJson.put("Datum", dateFormat.format(event.getDate()));
                }
                formattedJson.put("Učionica", event.getRoom().getName());
                formattedJson.put("Dan u nedelji", event.getDayOfWeek().toString());
                formattedJson.put("Termi", event.getStartTime() + "-" + event.getEndTime());
                for(int i = 0; i < event.getAdditionalData().keySet().size(); i++){
                    formattedJson.put(event.getAdditionalData().keySet().toArray()[i].toString(), event.getAdditionalData().values().toArray()[i].toString());
                }
                arrayNode.add(formattedJson);
            }
            ObjectWriter objectWriter = objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT);
            objectWriter.writeValue(new File(filePath), arrayNode);
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToCsv(String filePath) {
        try{
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            CSVWriter writer = new CSVWriter(new FileWriter(filePath));
            List<String> head = new ArrayList<>();

            if(schedule.getSchedule().get(0).getDateTo() != null) {
                head.add("Datum od");
                head.add("Datum do");
            }else{
                head.add("Datum");
            }

            head.add("Ucionica");
            head.add("Dan u nedelji");
            head.add("Vreme");

            head.addAll(schedule.getSchedule().get(0).getAdditionalData().keySet());

            List<Event> events = printCriteria();

            if(events == null)
                events = schedule.getSchedule();

            writer.writeNext(head.toArray(new String[0]));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            for (Event event : events) {
                int i=0;
                String[] rowData = new String[head.size()];
                rowData[i++] = dateFormat.format(event.getDate());
                if(event.getDateTo() != null) {
                    rowData[i++] = dateFormat.format(event.getDateTo());
                }
                rowData[i++] = event.getRoom().getName();
                rowData[i++] = event.getDayOfWeek().toString();
                rowData[i++] = event.getStartTime().toString() + "-" + event.getEndTime().toString();
                for (String value : event.getAdditionalData().values()) {
                    rowData[i] = value;
                    i++;
                }
                writer.writeNext(rowData);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToPDF(String filePath) {
        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Raspored"));
            List<String> head = new ArrayList<>();

            if(schedule.getSchedule().get(0).getDateTo() != null) {
                head.add("Datum od");
                head.add("Datum do");
            }else{
                head.add("Datum");
            }
            head.add("Ucionica");
            head.add("Dan u nedelji");
            head.add("Vreme");


            PdfPTable table = new PdfPTable(head.size() + schedule.getSchedule().get(0).getAdditionalData().keySet().size());
            head.addAll(schedule.getSchedule().get(0).getAdditionalData().keySet());


            for (String cell : head) {
                PdfPCell headerCell = new PdfPCell(new Paragraph(cell));
                table.addCell(headerCell);
            }
            List<Event> events = printCriteria();

            if(events == null)
                events = schedule.getSchedule();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            for (Event event : events) {
                PdfPCell cell;
                table.addCell(new Paragraph(dateFormat.format(event.getDate())));
                if (event.getDateTo() != null) {
                    table.addCell(new Paragraph(dateFormat.format(event.getDateTo())));
                }
                cell = new PdfPCell(new Paragraph(event.getRoom().getName()));
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(event.getDayOfWeek().toString()));
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(event.getStartTime().toString() + "-" + event.getEndTime().toString()));
                table.addCell(cell);
                for (String value : event.getAdditionalData().values()) {
                    cell = new PdfPCell(new Paragraph(value));
                    table.addCell(cell);
                }
            }


            document.add(table);
            document.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean doesEventExist(String input){

        if (findEvent(input) != null){
            System.out.println("Termin koji ste uneli je zauzet.\n");
            return true;
        }

        return false;
    }

   // Pretrazivanje termina po additionalData podacima
    public List<Event> searchAdditionalData(String input) {
        List<Event> found = new ArrayList<>();
        List<Event> events = schedule.getSchedule();

        String[] filterPairs = input.split(",");
        Map<String, String> filters = new HashMap<>();

        // Razdvajanje i dodavanje filtera u mapu
        for (String pair : filterPairs) {
            String[] parts = pair.trim().split(":");
            if (parts.length == 2) {
                filters.put(parts[0], parts[1]);
            }
        }

        for (Event event : events) {
            Map<String, String> data = event.getAdditionalData();
            boolean matchesAllFilters = true;

            for (Map.Entry<String, String> filter : filters.entrySet()) {
                String key = filter.getKey();
                String value = filter.getValue();

                if (data.containsKey(key) && !data.get(key).equals(value)) {
                    matchesAllFilters = false;
                    break;
                }
            }

            if (matchesAllFilters) {
                found.add(event);
                System.out.println(event);
            }
        }

        return found;
    }

    private Map<String, List<Event>> sortEventsByRoom(List<Event> events){
        Map<String, List<Event>> eventsPerRoom = new HashMap<>();
        for (Event event : events) {

            if (!eventsPerRoom.containsKey(event.getRoom().getName())) {
                eventsPerRoom.put(event.getRoom().getName(), new ArrayList<>());
            }

            eventsPerRoom.get(event.getRoom().getName()).add(event);
        }
        
        return eventsPerRoom;
    }

    //Pretrazivanje slobodnih termina
    public Map<Pair<String, String>, List<String>> findAvailableTime() {
        Map<Pair<String, String>, List<String>> availableTimes = new HashMap<>();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("E MMM dd yyyy");

        for (DayOfWeek day : DayOfWeek.values()) {
            List<Event> eventsForDay = schedule.getEventsByDay(day);
            Map<String, List<Event>> eventsPerRoom = sortEventsByRoom(eventsForDay);

            for (String room : eventsPerRoom.keySet()) {
                StringBuilder timeSlots = new StringBuilder();
                List<Event> sortedList = new ArrayList<>(eventsPerRoom.get(room));
                sortedList.sort(Comparator.comparing(Event::getStartTime));

                Time lastEndTime = Time.valueOf("00:00:00");

                for (Event event : sortedList) {
                    if (!event.getStartTime().equals(lastEndTime)) {
                        if (timeSlots.length() > 0) {
                            timeSlots.append(", ");
                        }
                        timeSlots.append(lastEndTime).append("-").append(event.getStartTime());
                    }
                    lastEndTime = event.getEndTime();
                }

                if (!lastEndTime.equals(Time.valueOf("23:59:59"))) {
                    if (timeSlots.length() > 0) {
                        timeSlots.append(", ");
                    }
                    timeSlots.append(lastEndTime).append("-").append("23:59:59");
                }

                String formattedDate = dateFormatter.format(sortedList.get(0).getDate());
                Pair<String, String> roomDatePair = Pair.of(room, formattedDate);

                availableTimes.computeIfAbsent(roomDatePair, k -> new ArrayList<>());
                availableTimes.get(roomDatePair).add(timeSlots.toString());

                // Ispis termina za svaku sobu
                System.out.println("Soba: " + room + ", Datum: " + formattedDate + " - Termini: " + timeSlots);
            }
        }
        return availableTimes;
    }
}
