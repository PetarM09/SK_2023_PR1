import eventColection.ScheduleManagerImp;
import scheduleManager.ScheduleManager;

//import weeklyColection.ScheduleManagerImp;

public class Main {
    public static void main(String[] args) {
        ScheduleManagerImp scheduleManagerImp = new ScheduleManagerImp();
        scheduleManagerImp.loadScheduleFromFile();
        scheduleManagerImp.findAvailableTime();
        scheduleManagerImp.loadRoomsFromFile();
        //System.out.println(scheduleManagerImp.findAvailableTime());
        //scheduleManagerImp.findAvailableTime();

        //scheduleManagerImp.ispis();

        /*scheduleManagerImp.addEvent("Soba123,2023-10-10,08:00,10:00, Dodatne informacije1:2, Dodatna informacija 2:5");
        System.out.println("Nakon dodavanja\n\n\n\n\n\n\n");

        for(var x : ScheduleManager.schedule.getSchedule())
        {
            System.out.println(x);
        }

        scheduleManagerImp.addEvent(("Soba123,2023-10-10,2023-10-24,08:00,10:00, Dodatne informacije1:2, Dodatna informacija 2:5"));
        System.out.println("Nakon dodavanja sa dateTo\n\n\n\n\n\n\n");
        for(var x : ScheduleManager.schedule.getSchedule())
        {
            System.out.println(x);
        }*/
    }
}
