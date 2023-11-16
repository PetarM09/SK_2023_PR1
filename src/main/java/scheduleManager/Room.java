package scheduleManager;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter

public class Room {
    private String name;
    private int capacity;
    private Map<String, String> additionalData;

    public Room(String name, int capacity, Map<String, String> additionalData) {
        this.name = name;
        this.capacity = capacity;
        this.additionalData = additionalData;
    }

    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public Room() {
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", capacity=" + capacity +
                ", additionalData=" + additionalData +
                '}';
    }
}
