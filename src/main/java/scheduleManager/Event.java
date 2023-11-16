package scheduleManager;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter

public class Event {
    private Date date;
    private Room room;
    private Date dateTo;
    private Time startTime;
    private Time endTime;
    private DayOfWeek dayOfWeek;
    private Map<String, String > additionalData;


    public Event(Date date, Room room, Time startTime, Time endTime, DayOfWeek dayOfWeek, Map<String, String > additionalData) {
        this.additionalData = new HashMap<>();
        this.date = date;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.additionalData = additionalData;
    }

    public Event(Date date, Room room, Time startTime, Time endTime, DayOfWeek dayOfWeek) {
        this.additionalData = new HashMap<>();
        this.date = date;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
    }

    public Event(Date date, Date dateTo, Room room, Time startTime, Time endTime, DayOfWeek dayOfWeek, Map<String, String > additionalData) {
        this(date, room, startTime, endTime, dayOfWeek, additionalData);
        this.dateTo = dateTo;
    }

    public Event(Date date, Date dateTo, Room room, Time startTime, Time endTime, DayOfWeek dayOfWeek) {
        this(date, room, startTime, endTime, dayOfWeek);
        this.dateTo = dateTo;
    }

    @Override
    public String toString() {
        if(dateTo == null)
            return "Event{" +
                "datum=" + date +
                ", ucionica=" + room.getName() +
                ", vreme pocetka=" + startTime +
                ", vreme kraja=" + endTime +
                ", dan u nedelji=" + dayOfWeek +
                 additionalData +
                '}';
        else
            return "Event{" +
                "datum od=" + date +
                ", datum do=" + dateTo +
                ", ucionica=" + room.getName() +
                ", vreme pocetka=" + startTime +
                ", vreme kraja=" + endTime +
                ", dan u nedelji=" + dayOfWeek +
                 additionalData +
                '}';
    }
}
