package edu.eci.dosw.tdd.skyrescue.center;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.eci.dosw.tdd.skyrescue.drone.Drone;


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

}
