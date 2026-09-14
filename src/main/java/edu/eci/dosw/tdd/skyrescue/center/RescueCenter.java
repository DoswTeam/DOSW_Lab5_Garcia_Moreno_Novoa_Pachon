package edu.eci.dosw.tdd.skyrescue.center;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.eci.dosw.tdd.skyrescue.drone.Drone;
import edu.eci.dosw.tdd.skyrescue.mission.Mission;
import edu.eci.dosw.tdd.skyrescue.mission.MissionStatus;
import edu.eci.dosw.tdd.skyrescue.operator.RescueOperator;

/**
 * Coordinates drones, operators and emergency missions.
 */
public class RescueCenter {

    private final List<RescueOperator> operators;
    private final Map<String, Drone> drones;
    private final List<Mission> missions;

    public RescueCenter() {
        this.operators = new ArrayList<>();
        this.drones = new HashMap<>();
        this.missions = new ArrayList<>();
    }

    /**
     * Registers a drone in the rescue center.
     *
     * Rules:
     * - The drone cannot be null.
     * - The drone id cannot be null or blank.
     * - Two drones cannot have the same id.
     * - A valid drone is stored as available.
     *
     * @param drone drone to register.
     * @return true if it was registered; false otherwise.
     */
    public boolean addDrone(Drone drone) {
        if (drone == null || drone.getId() == null || drone.getId().isBlank()) {
            return false;
        }
        if (drones.containsKey(drone.getId())) {
            return false;
        }
        drones.put(drone.getId(), drone);
        return true;
    }

    /**
     * Assigns an emergency mission to an operator and an available drone.
     *
     * Rules:
     * - operatorId, droneId and location must be valid.
     * - The operator must exist.
     * - The drone must exist and be available.
     * - distanceKm must be greater than zero.
     * - distanceKm cannot exceed the drone maxRangeKm.
     * - The same operator cannot have two ACTIVE missions.
     * - On success, create an ACTIVE mission with the current date.
     * - On success, the selected drone becomes unavailable.
     * - The created mission must be stored in the center.
     *
     * Suggested error policy:
     * - Invalid/nonexistent data -> IllegalArgumentException.
     * - Valid resource but invalid state -> IllegalStateException.
     *
     * @param operatorId operator identifier.
     * @param droneId drone identifier.
     * @param location emergency location description.
     * @param distanceKm mission distance in kilometers.
     * @return created mission.
     */
    public Mission assignMission(
            String operatorId,
            String droneId,
            String location,
            int distanceKm) {
        if (operatorId == null || operatorId.isBlank()
                || droneId == null || droneId.isBlank()
                || location == null || location.isBlank()) {
            throw new IllegalArgumentException("operatorId, droneId and location are required.");
        }
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("distanceKm must be greater than zero.");
        }

        RescueOperator operator = findOperatorById(operatorId);
        if (operator == null) {
            throw new IllegalArgumentException("Operator does not exist: " + operatorId);
        }

        Drone drone = drones.get(droneId);
        if (drone == null) {
            throw new IllegalArgumentException("Drone does not exist: " + droneId);
        }
        if (!drone.isAvailable()) {
            throw new IllegalStateException("Drone is not available: " + droneId);
        }
        if (distanceKm > drone.getMaxRangeKm()) {
            throw new IllegalArgumentException("distanceKm exceeds the drone maxRangeKm.");
        }
        if (hasActiveMission(operatorId)) {
            throw new IllegalStateException("Operator already has an active mission: " + operatorId);
        }

        Mission mission = new Mission(
                UUID.randomUUID().toString(),
                location,
                distanceKm,
                drone,
                operator,
                LocalDateTime.now(),
                MissionStatus.ACTIVE);

        drone.setAvailable(false);
        missions.add(mission);
        return mission;
    }

    /**
     * Completes an active mission.
     *
     * Rules:
     * - missionId must be valid.
     * - The mission must exist.
     * - An already COMPLETED mission cannot be completed again.
     * - The mission status changes to COMPLETED.
     * - The end date is the current date/time.
     * - The drone assigned to the mission becomes available again.
     *
     * Suggested error policy:
     * - Invalid/nonexistent mission -> IllegalArgumentException.
     * - Mission already completed -> IllegalStateException.
     *
     * @param missionId mission identifier.
     * @return completed mission.
     */
    public Mission completeMission(String missionId) {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException("missionId is required.");
        }

        Mission mission = findMissionById(missionId);
        if (mission == null) {
            throw new IllegalArgumentException("Mission does not exist: " + missionId);
        }
        if (mission.getStatus() == MissionStatus.COMPLETED) {
            throw new IllegalStateException("Mission is already completed: " + missionId);
        }

        mission.setStatus(MissionStatus.COMPLETED);
        mission.setEndDate(LocalDateTime.now());
        mission.getDrone().setAvailable(true);
        return mission;
    }

    public boolean addOperator(RescueOperator operator) {
        return operators.add(operator);
    }

    private RescueOperator findOperatorById(String operatorId) {
        return operators.stream()
                .filter(o -> o.getId().equals(operatorId))
                .findFirst()
                .orElse(null);
    }

    private Mission findMissionById(String missionId) {
        return missions.stream()
                .filter(m -> m.getId().equals(missionId))
                .findFirst()
                .orElse(null);
    }

    private boolean hasActiveMission(String operatorId) {
        return missions.stream()
                .anyMatch(m -> m.getOperator().getId().equals(operatorId)
                        && m.getStatus() == MissionStatus.ACTIVE);
    }
}