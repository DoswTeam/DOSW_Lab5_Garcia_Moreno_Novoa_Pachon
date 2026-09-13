package edu.eci.dosw.tdd.skyrescue.center;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

}
