import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestCases {
    @Test
    public void testFindNearestStation() {
        // Create charging stations
        ChargingStation station1 = new ChargingStation(1, 5, 5, null, null, null);
        ChargingStation station2 = new ChargingStation(2, 10, 5, null, null, null);
        ChargingStation station3 = new ChargingStation(3, 15, 5, null, null, null);

        ChargingStation[] stations = { station1, station2, station3 };

        // Create cars at different locations
        Car car1 = new Car("car1", 7, 2, stations);
        Car car2 = new Car("car2", 12, 3, stations);
        Car car3 = new Car("car3", 18, 1, stations);

        // Check if cars find the nearest charging station correctly
        assertEquals(station1, car1.findNearestStation());
        assertEquals(station2, car2.findNearestStation());
        assertEquals(station3, car3.findNearestStation());
    }

    @Test
    public void testAssignEnergySourceInValidRange() {
        ChargingStation chargingStation = new ChargingStation(1, 1, 2, null, null, null);
        boolean solarFound = false;
        boolean windFound = false;
        boolean gridFound = false;

        // Check if assignEnrgySourse returns all three types of sources
        for (int i = 0; i < 50; i++) {
            String energySource = chargingStation.assignEnrgySourse();
            if (energySource.equals("Solar")) {
                solarFound = true;
            }
            if (energySource.equals("Wind")) {
                windFound = true;
            }
            if (energySource.equals("Power Grid")) {
                gridFound = true;
            }
        }

        assertTrue("Solar energy source not found", solarFound);
        assertTrue("Wind energy source not found", windFound);
        assertTrue("Power Grid energy source not found", gridFound);
    }

    @Test
    public void testAvailableSlotsisEqualtoTotalSlot() throws IOException, InterruptedException {

        ChargingStation chargingStation = new ChargingStation(1, 1, 2, null, null, null);

        assertEquals(2, chargingStation.availableSlots); // Initial available slots
    }

    @Test
    public void testChargingStationCreation() {
        ChargingStation chargingStation = new ChargingStation(1, 1, 2, null, null, null);
        assertNotNull(chargingStation);
    }
    @Test
    public void testEnergySourceGeneration() {
        String solar = ChargingStation.eSourseGen(0);
        assertEquals("Solar", solar);

        String wind = ChargingStation.eSourseGen(1);
        assertEquals("Wind", wind);

        String grid = ChargingStation.eSourseGen(2);
        assertEquals("Power Grid", grid);
    }
    @Test
    public void testFileCreation() {
        String currentDirectory = System.getProperty("user.dir");
        String newDirectoryName = "TestDirectory2"; // Directory in which the file will be created
        String newFileName = "testFile.txt"; // Name of the file to be created

        File newDirectory = new File(currentDirectory, newDirectoryName);
        File newFile = new File(newDirectory, newFileName);

        boolean isDirectoryCreated = newDirectory.mkdir(); // Create the directory
        assertTrue("Directory was not created", isDirectoryCreated);

        try {
            boolean isFileCreated = newFile.createNewFile(); // Attempt to create the file
            assertTrue("File was not created", isFileCreated);
            assertTrue("File does not exist", newFile.exists());
            assertTrue("File is not a file", newFile.isFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        newFile.delete();
        newDirectory.delete();
    }
    @Test
    public void testCarCreation() {
        ChargingStation[] stations = new ChargingStation[2];
        stations[0] = new ChargingStation(1, 1, 2, null, null, null);
        stations[1] = new ChargingStation(2, 7, 2, null, null, null);

        Car car = new Car("car1", 1, 2, stations);
        assertNotNull(car);
    }

    @Test
    public void testAdminAuthorization() {
        ChargingStation station1 = new ChargingStation(1, 1, 2, null, null, null);
        ChargingStation station2 = new ChargingStation(2, 7, 2, null, null, null);

        Admin admin1 = new Admin(001, "admin1", station1);
        Admin admin2 = new Admin(002, "admin2", station2);

        // Verify correct admin access
        assertTrue(admin1.ID == 001 && admin1.pass.equals("admin1"));
        assertTrue(admin2.ID == 002 && admin2.pass.equals("admin2"));

        // Invalid credentials should fail
        assertFalse(admin1.ID == 002 && admin1.pass.equals("admin2"));
        assertFalse(admin2.ID == 001 && admin2.pass.equals("admin1"));
    }
        @Test
    public void testThreadSynchronization() throws InterruptedException, IOException {
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

        File s1file = new File(newDirectory, "TestStation1LogFiles");
        FileWriter f1 = new FileWriter(s1file.getAbsolutePath());

        ChargingStation chargingStation = new ChargingStation(1, 1, 2, f1, null, null);
        Car car1 = new Car("car1", 1, 2, new ChargingStation[] { chargingStation });
        Car car2 = new Car("car2", 2, 2, new ChargingStation[] { chargingStation });

        car1.start();
        car2.start();

        car1.join();
        car2.join();

        // Additional assertions or validations if needed
        assertEquals(2, chargingStation.availableSlots); // Both slots occupied

        f1.close();
        if (s1file.exists()) {
            s1file.delete();
        }
    }

    @Test
    public void testDirectoryCreation() {
        String currentDirectory = System.getProperty("user.dir");
        String newDirectoryName = "TestDirectory1"; // Directory name to be created

        File newDirectory = new File(currentDirectory, newDirectoryName);

        boolean isCreated = newDirectory.mkdir(); // Attempt to create the directory

        assertTrue("Directory was not created", isCreated);
        assertTrue("Directory does not exist", newDirectory.exists());
        assertTrue("Directory is not a directory", newDirectory.isDirectory());
        newDirectory.delete();
    }
}
//Test change by arjun
// Tes2 