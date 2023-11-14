//import eventColection.ScheduleManagerImp;

import weeklyColection.ScheduleManagerImp;

public class Main {
    public static void main(String[] args) {
        ScheduleManagerImp scheduleManagerImp = new ScheduleManagerImp();
        scheduleManagerImp.loadScheduleFromFile();
        scheduleManagerImp.saveSchedule();
        /*for(var x : scheduleManagerImp.getSchedule().getSchedule().entrySet()){
            System.out.println(x.getKey());
            for(var y : x.getValue()){
                System.out.println(y);
            }
        }*/

        /*for(var x : ScheduleManager.getSchedule().getSchedule())
        {
            System.out.println(x);
        }
        scheduleManagerImp.addEvent("Soba123,2023-10-10,08:00,10:00, Dodatne informacije1:2, Dodatna informacija 2:5");
        System.out.println("Nakon dodavanja\n\n\n\n\n\n\n");

        for(var x : ScheduleManager.getSchedule().getSchedule())
        {
            System.out.println(x);
        }

        scheduleManagerImp.removeEvent("Soba123,2023-10-10,08:00,10:00, Dodatne informacije1:2, Dodatna informacija 2:5");
        System.out.println("Nakon brisanja\n\n\n\n\n\n\n");
        for(var x : ScheduleManager.getSchedule().getSchedule())
        {
            System.out.println(x);
        }*/
    }
}
