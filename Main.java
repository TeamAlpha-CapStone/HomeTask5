import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

class Admin {
    int ID;
    String pass;
    ChargingStation cs;

    public Admin(int ID, String pass, ChargingStation cs) {
        this.ID = ID;
        this.pass = pass;
        this.cs = cs;
    }

}

class TC extends Thread {
    ChargingStation chargingstation;
    Car c;
    FileWriter f;

    public TC(Car c, ChargingStation chargingstation, FileWriter f) {
        this.chargingstation = chargingstation;
        this.c = c;
        this.f = f;
    }

    public synchronized void run() {
        for (int i = 1; i <= 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // System.out.println(i+" Second over for "+c.ID());
        }
        if (chargingstation.waitingList.contains(c)) {
            // System.out.println(c.ID()+" removing inside time checker....");
            System.out.println(c.ID() + " waited for so long in Waiting List, hence it is leaving without charging.");
            try {
                f.write(c.ID() + " waited for so long in Waiting List, hence it is leaving without charging.");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            chargingstation.waitingList.remove(c);
            System.out.println(c.ID() + "Left without charging");
            try {
                f.write(c.ID() + "Left without charging");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // System.out.println(c.ID()+" removed inside time checker....");
            // System.out.println("gonna stop "+c.ID()+" thread.");
            Thread.interrupted();
        } else {
            Thread.interrupted();
            // System.out.println("stopped "+c.ID()+"TC thread.");
        }
    }
}

class ChargingStation {
    int id;
    int location;
    int totalSlots;
    int availableSlots;
    public List<Car> waitingList;
    public FileWriter f;
    FileWriter[] esFW;
    String date_name;
    Car c;

    public ChargingStation(int id, int location, int totalSlots, FileWriter f, FileWriter[] esFW, String date_name) {
        this.id = id;
        this.location = location;
        this.totalSlots = totalSlots;
        this.availableSlots = totalSlots;
        this.waitingList = new ArrayList<>();
        this.f = f;
        this.esFW = esFW;
        this.date_name = date_name;
    }

    public static String assignEnrgySourse() {
        Random rand = new Random();
        int esourse = rand.nextInt(3);
        String assignedString = eSourseGen(esourse);
        return assignedString;
    }

    public static String eSourseGen(int value) {
        String result = "";
        switch (value) {
            case 0:
                result = "Solar";
                break;
            case 1:
                result = "Wind";
                break;
            case 2:
                result = "Power Grid";
                break;
            default:
                result = "Invalid value";
                break;
        }
        return result;
    }

    void timechecker(Car c, FileWriter f) throws InterruptedException {
        TC tc = new TC(c, this, f);
        tc.start();
        // tc.join();
    }

    public synchronized boolean bookslot(Car car, int duration) throws InterruptedException {
        c = car;
        if (availableSlots > 0) {
            String assigned_e_sourse = assignEnrgySourse();
            System.out.println(car.ID() + " will be charging for " + duration + " Minutes in Station" + id
                    + " ,with Energy Sourse as: " + assigned_e_sourse);
            try {
                f.write(car.ID() + " Charged for " + duration + " Minutes in Station" + id + " ,with Energy Sourse as: "
                        + assigned_e_sourse + "\n");
                FileWriter dwfw = esFW[3];
                dwfw.write(car.ID() + " Charged for " + duration + " Minutes in Station" + id
                        + " ,with Energy Sourse as: " + assigned_e_sourse + "on date: " + date_name + "\n");
                if (assigned_e_sourse.equals("Solar")) {
                    FileWriter fwsolar = esFW[0];
                    fwsolar.write(car.ID() + " Charged for " + duration + " Minutes in Station" + id
                            + " ,with Energy Sourse as: " + assigned_e_sourse + "\n");
                }
                if (assigned_e_sourse.equals("Wind")) {
                    FileWriter fwwind = esFW[1];
                    fwwind.write(car.ID() + " Charged for " + duration + " Minutes in Station" + id
                            + " ,with Energy Sourse as: " + assigned_e_sourse + "\n");
                }
                if (assigned_e_sourse.equals("Power Grid")) {
                    FileWriter fwgrid = esFW[2];
                    fwgrid.write(car.ID() + " Charged for " + duration + " Minutes in Station" + id
                            + " ,with Energy Sourse as: " + assigned_e_sourse + "\n");
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            availableSlots = availableSlots - 1;
            return true;
        } else {
            System.out.println(car.ID() + " is added to the waiting list.");
            try {
                f.write(car.ID() + " is added to the waiting list.\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!waitingList.contains(car)) {
                waitingList.add(car);
                timechecker(car, f);
            }
            try {
                wait(); // Car is added to the waiting list
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false; // Room booking failed
        }
    }

    public synchronized void releaseSlot(Car car) throws InterruptedException {
        availableSlots++;
        System.out.println(car.ID() + " Charging slot released. Available slots: " + availableSlots);
        try {
            f.write(car.ID() + " Charging slot released. Available slots: " + availableSlots + "\n");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        checkWaitingList();
        notify(); // Notify waiting cars about the availability of a slot
    }

    private void checkWaitingList() throws InterruptedException {
        while (!waitingList.isEmpty() && availableSlots > 0) {
            Car car = waitingList.remove(0);
            if (bookslot(car, car.chargingDuration())) {
                System.out.println(car.ID() + " moved from the waiting list and got a slot.");
            }
        }
    }
}

class Car extends Thread {
    private String ID;
    private int location;
    private int chargingDuration;
    private ChargingStation[] stations;
    private ChargingStation nearestStation;

    public Car(String ID, int location, int chargingDuration, ChargingStation[] stations) {
        this.ID = ID;
        this.location = location;
        this.chargingDuration = chargingDuration;
        this.stations = stations;
        this.nearestStation = findNearestStation();
    }

    public ChargingStation findNearestStation() {
        int minDistance = Integer.MAX_VALUE;
        ChargingStation nearest = null;

        for (ChargingStation station : stations) {
            int distance = Math.abs(station.location - this.location);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = station;
            }
        }
        return nearest;
    }

    public String ID() {
        return ID;
    }

    public int chargingDuration() {
        return chargingDuration;
    }

    public void run() {
        try {
            if (nearestStation.bookslot(this, chargingDuration)) {
                try {
                    Thread.sleep(chargingDuration * 1000); // Simulating charging duration in seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nearestStation.releaseSlot(this);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class DirCreator {
    public File CreateDir(File currentdir, String newdirname) {

        File newDirectory = new File(currentdir, newdirname);

        if (!newDirectory.exists()) {
            if (newDirectory.mkdir()) {
                System.out.println(newdirname + " --> directory Created");
            } else {
                System.out.println("Failed to create: " + newdirname + " directory.");
            }
        }

        return newDirectory;

    }
}

class FileCreator {
    public File CreateFile(File DirName, String FileName) {
        File f = new File(DirName, FileName);
        try {
            if (f.createNewFile()) {
                System.out.println(FileName + " file created");
            }

        } catch (IOException e) {
            System.out.println("Error Occured in creating " + FileName + " Log files");
            e.printStackTrace();
        }
        return f;
    }
}

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        String currentDirectory = System.getProperty("user.dir");
        String newDirectoryName = "StationLogFiles";

        File newDirectory = new File(currentDirectory, newDirectoryName);

        if (!newDirectory.exists()) {
            if (newDirectory.mkdir()) {
                System.out.println("Directory created: " + newDirectory.getAbsolutePath());
            } else {
                System.out.println("Failed to create directory.");
                return;
            }
        }
        // ***************************************************
        DirCreator DC_Date_Wise = new DirCreator();
        File date_dir = DC_Date_Wise.CreateDir(newDirectory, "Date_Wise");
        // ***************************************************
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String today_date = dateFormat.format(currentDate); // File name based on date
        String today_date_name = today_date + ".txt";

        FileCreator FC_date_file = new FileCreator();
        File date_file = FC_date_file.CreateFile(date_dir, today_date_name);

        // --------------------------------------
        DirCreator DC_solarDirectory = new DirCreator();
        File solarDirectory = DC_Date_Wise.CreateDir(newDirectory, "Solar");

        DirCreator DC_windDirectory = new DirCreator();
        File windDirectory = DC_Date_Wise.CreateDir(newDirectory, "Wind");

        DirCreator DC_gridDirectory = new DirCreator();
        File gridDirectory = DC_Date_Wise.CreateDir(newDirectory, "Grid");
        // ===============================================================
        FileCreator FC_s1file = new FileCreator();
        File s1file = FC_s1file.CreateFile(newDirectory, "Station1LogFiles");
        // ===============================================================
        FileCreator FC_s2file = new FileCreator();
        File s2file = FC_s2file.CreateFile(newDirectory, "Station2LogFiles");
        // ===============================================================
        FileCreator FC_solarfile = new FileCreator();
        File solarfile = FC_solarfile.CreateFile(solarDirectory, "Solar_E_Sourse_Logfile");
        // =================================================================
        FileCreator FC_windfile = new FileCreator();
        File windfile = FC_windfile.CreateFile(windDirectory, "Wind_E_Sourse_Logfile");
        // ================================================================
        FileCreator FC_gridfile = new FileCreator();
        File gridfile = FC_gridfile.CreateFile(gridDirectory, "Grid_E_Sourse_Logfile");
        // ===============================================================
        FileWriter f1 = new FileWriter(s1file.getAbsolutePath());
        FileWriter f2 = new FileWriter(s2file.getAbsolutePath());
        FileWriter solarFW = new FileWriter(solarfile.getAbsolutePath());
        FileWriter windFW = new FileWriter(windfile.getAbsolutePath());
        FileWriter gridFW = new FileWriter(gridfile.getAbsolutePath());
        FileWriter dateFW = new FileWriter(date_file.getAbsolutePath());

        System.out.println(s1file.getAbsolutePath());

        FileWriter[] esFW = { solarFW, windFW, gridFW, dateFW };

        ChargingStation station1 = new ChargingStation(1, 1, 2, f1, esFW, today_date); // Create charging stations
        ChargingStation station2 = new ChargingStation(2, 7, 2, f2, esFW, today_date);

        Admin as1 = new Admin(001, "admin1", station1);
        Admin as2 = new Admin(002, "admin2", station2);

        ChargingStation[] stations = { station1, station2 }; // Create an array of charging stations

        // Create cars with different charging durations and locations
        Car car1 = new Car("car1", 1, 2, stations);
        Car car2 = new Car("car2", 2, 3, stations);
        Car car3 = new Car("car3", 3, 1, stations);
        Car car4 = new Car("car4", 4, 3, stations);
        Car car5 = new Car("car5", 5, 3, stations);
        Car car6 = new Car("car6", 6, 2, stations);
        Car car7 = new Car("car7", 7, 4, stations);
        Car car8 = new Car("car8", 8, 1, stations);

        // Start threads for each car
        car1.start();
        car2.start();
        car3.start();
        car4.start();
        car5.start();
        car6.start();
        car7.start();
        car8.start();

        car1.join();
        car2.join();
        car3.join();
        car4.join();
        car5.join();
        car6.join();
        car7.join();
        car8.join();

        f1.close();
        f2.close();
        solarFW.close();
        windFW.close();
        gridFW.close();
        dateFW.close();
        Thread.sleep(10000);
        System.out.println("All cars have finished their charging. Program terminated.");
        System.out.println(
                "=========================================================================================================");

        Scanner sc = new Scanner(System.in);
        System.out.print("Would You like to View log files(yes/no)");
        if ("yes".equals(sc.next())) {
            System.out.print("Would you like to view Station log files(yes/no)");
            if ("yes".equals(sc.next())) {
                System.out.print("Station1 or Station2 or ESourse or Date_Wise:");
                String sname = sc.next();
                if ("Station1".equals(sname)) {
                    System.out.print("Enter your ID:");
                    int id = sc.nextInt();
                    System.out.print("Enter your password:");
                    String pass = sc.next();
                    if (id == as1.ID && pass.equals(as1.pass)) {
                        System.out.println("*******************************************************************");
                        System.out.println("File Name: " + s1file.getName() + "\n" + "File Length: " + s1file.length()
                                + "\n" + "File Last Modified: " + s1file.lastModified() + "\n");
                        Scanner myreader = new Scanner(s1file);
                        while (myreader.hasNext()) {
                            System.out.println(myreader.nextLine());
                        }
                        myreader.close();
                        System.out.println("*******************************************************************");
                        System.out.println("Would u like to delete the file(yes/no):");
                        if ("yes".equals(sc.next())) {
                            s1file.delete();
                        } else {
                            System.out.println("Thank u then.----End of Program----");
                        }

                    } else {
                        System.out.println("Wrong credentials, Get Lost.----End of Program----");
                    }
                } else if ("Station2".equals(sname)) {
                    System.out.print("Enter your ID:");
                    int id = sc.nextInt();
                    System.out.print("Enter your password:");
                    String pass = sc.next();
                    if (id == as2.ID && pass.equals(as2.pass)) {
                        System.out.println("*******************************************************************");
                        System.out.println("File Name: " + s2file.getName() + "\n" + "File Length: " + s2file.length()
                                + "\n" + "File Last Modified: " + s2file.lastModified() + "\n");
                        Scanner myreader = new Scanner(s2file);
                        while (myreader.hasNext()) {
                            System.out.println(myreader.nextLine());
                        }
                        System.out.println("*******************************************************************");
                        myreader.close();
                        System.out.println("Would u like to delete the file(yes/no):");
                        if ("yes".equals(sc.next())) {
                            s2file.delete();
                        } else {
                            System.out.println("Thank u then.----End of Program----");
                        }

                    }
                } else if ("ESourse".equals(sname)) {
                    System.out.print("Enter any of the Sourse: \"solarfile or windfile or gridfile\":");
                    String es = sc.next();

                    switch (es) {
                        case "solarfile":
                            System.out.println("*******************************************************************");
                            Scanner myreader1 = new Scanner(solarfile);
                            System.out.println(
                                    "File Name: " + solarfile.getName() + "\n" + "File Length: " + solarfile.length()
                                            + "\n" + "File Last Modified: " + solarfile.lastModified() + "\n");
                            while (myreader1.hasNext()) {
                                System.out.println(myreader1.nextLine());
                            }
                            System.out.println("*******************************************************************");
                            myreader1.close();
                            break;
                        case "windfile":
                            System.out.println("*******************************************************************");
                            System.out.println(
                                    "File Name: " + windfile.getName() + "\n" + "File Length: " + windfile.length()
                                            + "\n" + "File Last Modified: " + windfile.lastModified() + "\n");
                            Scanner myreader2 = new Scanner(windfile);
                            while (myreader2.hasNext()) {
                                System.out.println(myreader2.nextLine());
                            }
                            System.out.println("*******************************************************************");
                            myreader2.close();
                            break;
                        case "gridfile":
                            System.out.println("*******************************************************************");
                            System.out.println(
                                    "File Name: " + gridfile.getName() + "\n" + "File Length: " + gridfile.length()
                                            + "\n" + "File Last Modified: " + gridfile.lastModified() + "\n");
                            Scanner myreader3 = new Scanner(gridfile);
                            while (myreader3.hasNext()) {
                                System.out.println(myreader3.nextLine());
                            }
                            System.out.println("*******************************************************************");
                            myreader3.close();
                            break;
                        default:
                            System.out.println("Invalid Input ----End of Program----");
                            break;

                    }

                } //
                else {
                    System.out.println("Invalid Input ----End of Program----");
                }
            } else {
                System.out.println("Thank you. ----End of Program----");
            }
        } else {
            System.out.println("Then thank you. ----End of Program----");
        }

    }
}