package scheduleManager;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter

public class Schedule {
    private Map<DayOfWeek, List<Event>> schedule;


    public Schedule() {
        this.schedule = new HashMap<>();
        this.schedule.put(DayOfWeek.MONDAY, new ArrayList<>());
        this.schedule.put(DayOfWeek.TUESDAY, new ArrayList<>());
        this.schedule.put(DayOfWeek.WEDNESDAY, new ArrayList<>());
        this.schedule.put(DayOfWeek.THURSDAY, new ArrayList<>());
        this.schedule.put(DayOfWeek.FRIDAY, new ArrayList<>());
        this.schedule.put(DayOfWeek.SATURDAY, new ArrayList<>());
        this.schedule.put(DayOfWeek.SUNDAY, new ArrayList<>());
    }

    public Map<DayOfWeek, List<Event>> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<DayOfWeek, List<Event>> schedule) {
        this.schedule = schedule;
    }
}
