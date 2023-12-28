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
}
//Test change by arjun
// Tes2 