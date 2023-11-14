package scheduleManager;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter

public class Schedule {
    private List<Event> schedule;
    private Date startDate;
    private Date endDate;
    private List<Date> exceptions;


    public Schedule() {
        schedule = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }
    

    public void sortByDate(){
        if(schedule.get(0).getDate() != null)
            schedule.sort(Comparator.comparing(Event::getDate));
    }

    public void sortByDayOfWeekDay(){
        ///Sortiranje prvo do dana u nedelji, pa po datumu ako datum nije null
        if(schedule.get(0).getDate() == null)
            schedule.sort(Comparator.comparing(Event::getDayOfWeek));
        else
            schedule.sort(Comparator.comparing(Event::getDayOfWeek).thenComparing(Event::getDate));
    }

    public List<Event> scheduleFromDateToDate(Date pocetak, Date kraj) {
        List<Event> events = new ArrayList<>();
        if(pocetak.after(kraj))
            throw new IllegalArgumentException("Pocetak ne moze biti posle kraja");
        if(schedule.get(0).getDate() != null)
            for (Event e : schedule) {
                if (e.getDate().after(pocetak) && e.getDate().before(kraj))
                    events.add(e);
            }
        return events;
    }

    public List<Event> sortByAdditionalData(String kriterijum1) {
        List<Event> events = new ArrayList<>();
        String []token = kriterijum1.split(":");
        if(token.length != 2)
            throw new IllegalArgumentException("Kriterijum mora biti u formatu kljuc:vrednost");
        if(schedule.get(0).getAdditionalData().containsKey(token[0]))
            events = schedule.stream().filter(e -> e.getAdditionalData().get(token[0]).equals(token[1])).collect(Collectors.toList());
        return events;
    }
}
