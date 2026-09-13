package edu.eci.dosw.tdd.skyrescue.center;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.eci.dosw.tdd.skyrescue.drone.Drone;
import edu.eci.dosw.tdd.skyrescue.mission.Mission;
import edu.eci.dosw.tdd.skyrescue.mission.MissionStatus;
import edu.eci.dosw.tdd.skyrescue.operator.RescueOperator;


class RescueCenterTest {
    private RescueCenter rescueCenter;

    @BeforeEach
    void setUp() {
        rescueCenter = new RescueCenter();
    }

    @Test
    void shouldRegisterDroneWhenDataIsValid() {
        Drone drone = new Drone("D1", "DJI Mini 4K", 15);
        boolean registered = rescueCenter.addDrone(drone);
        assertTrue(registered);
    }

    @Test
    void shouldAssignMissionWhenOperatorAndDroneAreValidAndDistanceIsAllowed() {

        RescueOperator operator = new RescueOperator("O1", "Ana Torres");
        Drone drone = new Drone("D1", "DJI Mini 4K", 15);
        rescueCenter.addOperator(operator);
        rescueCenter.addDrone(drone);

        Mission mission = rescueCenter.assignMission("O1", "D1", "Calle 10 con Carrera 5", 10);

        assertNotNull(mission);
        assertEquals(MissionStatus.ACTIVE, mission.getStatus());
        assertFalse(drone.isAvailable());
    }

    @Test
    void shouldCompleteAnActiveMission() {
        RescueOperator operator = new RescueOperator("O1", "Ana Torres");
        Drone drone = new Drone("D1", "DJI Mini 4K", 15);
        rescueCenter.addOperator(operator);
        rescueCenter.addDrone(drone);
        Mission activeMission = rescueCenter.assignMission("O1", "D1", "Calle 10 con Carrera 5", 10);

        Mission completed = rescueCenter.completeMission(activeMission.getId());

        assertEquals(MissionStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getEndDate());
        assertTrue(drone.isAvailable());
    }

    @Test
    void shouldReturnFalseWhenAddingNullDrone() {
        boolean result = rescueCenter.addDrone(null);
        assertFalse(result);
    }

    @Test
    void shouldThrowExceptionWhenDroneDoesNotExist() {
        RescueOperator operator = new RescueOperator("O1", "Ana Torres");
        rescueCenter.addOperator(operator);

        assertThrows(
                IllegalArgumentException.class,
                () -> rescueCenter.assignMission(
                        "O1",
                        "D999",
                        "Calle 10 con Carrera 5",
                        10));
    }
    @Test
    void shouldThrowExceptionWhenMissionDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> rescueCenter.completeMission("M999"));
    }

    @Test
    void shouldNotRegisterDroneWithEmptyId() {
        // Arrange
        String id = "";
        String model = "DJI Mini 5";
        int maxRangeKm = 200;

        // Act
        Drone drone = new Drone(id, model, maxRangeKm);

        // Assert
        assertFalse(rescueCenter.addDrone(drone));
    }

    @Test
    void shouldNotAssignMissionToNotAvailableDrone(){
        // Arrange
        String droneId = "D01";
        String droneModel = "DJI Mini 5";
        int droneMaxRangeKm = 200;

        String operatorId = "O01";
        String operatorName = "Pedro Perez";

        String missionLocation = "Bogotá";
        int missionDistanceKm = 100;

        // Act
        Drone drone = new Drone(droneId, droneModel, droneMaxRangeKm);
        RescueOperator rOperator = new RescueOperator(operatorId, operatorName);

        rescueCenter.addOperator(rOperator);
        rescueCenter.addDrone(drone);
        drone.setAvailable(false);

        //Assert
        assertThrows(IllegalStateException.class, () -> rescueCenter.assignMission(operatorId, 
            droneId, missionLocation, missionDistanceKm));
        
    }

    @Test 
    void shouldNotCompleteMissionTwice(){
        //Arrange
        String droneId = "D01";
        String droneModel = "DJI Mini 5";
        int droneMaxRangeKm = 200;

        String operatorId = "O01";
        String operatorName = "Pedro Perez";

        String missionId = "M1";
        String missionLocation = "Bogotá";
        int missionDistanceKm = 100;
        LocalDateTime missionStartDate = LocalDateTime.now();
        MissionStatus missionStatus = MissionStatus.ACTIVE;

        //Act
        Drone drone = new Drone(droneId, droneModel, droneMaxRangeKm);
        RescueOperator rOperator = new RescueOperator(operatorId, operatorName);
        Mission mission = new Mission(missionId, missionLocation, missionDistanceKm, 
            drone, rOperator, missionStartDate, missionStatus);

        rescueCenter.addDrone(drone);
        rescueCenter.addOperator(rOperator);
        rescueCenter.assignMission(operatorId, droneId, missionLocation, missionDistanceKm);
        rescueCenter.completeMission(missionId);

        //Assert
        assertThrows(IllegalStateException.class, () -> rescueCenter.completeMission(missionId));
    }

    @Test
    void shouldNotRegisterTwoDronesWithSameId(){
        Drone drone1 = new Drone("D5", "Pro-Max", 25);
        Drone drone2 = new Drone("D5", "Mini", 5);
        boolean register1 = rescueCenter.addDrone(drone1);
        boolean register2 = rescueCenter.addDrone(drone2);
        assertTrue(register1);
        assertFalse(register2);
    }
    
    @Test 
    void shouldNotAssignMissionWithDistanceExceedingRange(){
        Drone drone = new Drone("D6", "Small", 4);
        RescueOperator rOperator = new RescueOperator("RO", "Daniel");
        assertThrows(IllegalArgumentException.class,() -> {
            rescueCenter.addDrone(drone);
            rescueCenter.addOperator(rOperator);
            rescueCenter.assignMission("RO", "D6", "Medellin", 10);
        });
    }

    @Test
    void shouldCloseMissionWithoutModifyingActiveOne(){
        Drone drone = new Drone("D6", "Small", 4);
        RescueOperator rOperator = new RescueOperator("RO", "Daniel");
        rescueCenter.addDrone(drone);
        rescueCenter.addOperator(rOperator);
        Mission mission1 = rescueCenter.assignMission("RO", "D6", "Medellin", 2);

        Drone drone2 = new Drone("D7", "Medium", 10);
        RescueOperator rOperator2 = new RescueOperator("RO2", "Paco");
        rescueCenter.addDrone(drone2);
        rescueCenter.addOperator(rOperator2);
        Mission mission2 = rescueCenter.assignMission("RO2", "D7", "Cali", 5);

        assertEquals(MissionStatus.ACTIVE, mission1.getStatus());
        assertEquals(MissionStatus.ACTIVE, mission2.getStatus());

        rescueCenter.completeMission(mission2.getId());

        assertEquals(MissionStatus.COMPLETED, mission2.getStatus());
        assertEquals(MissionStatus.ACTIVE, mission1.getStatus());
        assertTrue(drone.isAvailable());
    }

}
