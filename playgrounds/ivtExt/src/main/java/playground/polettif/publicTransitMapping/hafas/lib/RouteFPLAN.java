/*
 * *********************************************************************** *
 * project: org.matsim.*                                                   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package playground.polettif.publicTransitMapping.hafas.lib;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import playground.polettif.publicTransitMapping.hafas.HafasDefinitions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A public transport route as read out from HAFAS FPLAN.
 *
 * @author boescpa
 */
public class RouteFPLAN {
	private static Logger log = Logger.getLogger(RouteFPLAN.class);

	private final int initialDelay = 60; // [s] In MATSim a pt route starts with the arrival at the first station. In HAFAS with the departure at the first station. Ergo we have to set a delay which gives some waiting time at the first station while still keeping the schedule.

	private static TransitScheduleFactory scheduleBuilder = new TransitScheduleFactoryImpl();
	private static Set<Id<TransitStopFacility>> facilitiesNotFound = new HashSet<>();

	public final static String PT = "pt";

	private final String operator;
	private final String routeName;
	private final int numberOfDepartures;
	private final int cycleTime; // [sec]
	private final List<TransitRouteStop> stops = new ArrayList<>();
	private String lineDescription;
	private Id<TransitRoute> routeId;

	public RouteFPLAN(String operator, String routeName, int numberOfDepartures, int cycleTime) {
		this.operator = operator;
		this.routeName = routeName;
		this.numberOfDepartures = numberOfDepartures + 1; // Number gives all occurrences of route additionally to first... => +1
		this.cycleTime = cycleTime * 60; // Cycle time is given in minutes in HAFAS -> Have to change it here...
		this.lineDescription = null;
	}

	public void setLineDescription(String lineDescription) {
		if (this.lineDescription == null) {
			this.lineDescription = "line" + lineDescription + "_" + routeName;
		}
	}
	// First departure time:
	private int firstDepartureTime = -1; //[sec]

	public void setFirstDepartureTime(int hour, int minute) {
		if (firstDepartureTime < 0) {
			this.firstDepartureTime = (hour * 3600) + (minute * 60);
		}
	}
	// Used vehicle type, Id and mode:
	private String usedVehicleID = null;
	private VehicleType usedVehicleType = null;
	private String usedMode = PT;

	public void setUsedVehicle(Id<VehicleType> typeId, VehicleType type) {
		usedVehicleType = type;
		usedVehicleID = typeId.toString();

		if (usedVehicleType != null) {
			if(HafasDefinitions.Vehicles.valueOf(usedVehicleType.getId().toString()).addToSchedule) {
				usedMode = HafasDefinitions.Vehicles.valueOf(usedVehicleType.getId().toString()).transportMode.modeName;
			} else {
				usedMode = "REMOVE";
			}
		}
	}

	public String getUsedVehicleId() {
		return usedVehicleID + "_" + operator + "_" + routeId;
	}

	/**
	 * Creates a schedule-route with the set characteristics.
	 * @return TransitRoute or NULL if no departures can be created for the route.
	 */
	/*
	public TransitRoute createTransitRoute() {
		List<Departure> departures = this.getDepartures();
		if (departures != null) {
			TransitRoute transitRoute = scheduleBuilder.createTransitRoute(routeName(), null, stops, usedMode);
			for (Departure departure : departures) {
				transitRoute.addDeparture(departure);
			}
			return transitRoute;
		} else {
			return null;
		}
	}
*/
	/**
	 * @param stopId Id of the stop to add...
	 * @param arrivalTime   Expected as seconds from midnight or zero if not available.
	 * @param departureTime Expected as seconds from midnight or zero if not available.
	 */
	public void addStop(Id<TransitStopFacility> stopId, TransitStopFacility stopFacility, double arrivalTime, double departureTime) {
		if (stopFacility == null) {
			if (!facilitiesNotFound.contains(stopId)) {
				facilitiesNotFound.add(stopId);
				log.error(operator + "-" + routeName + ": " + "Stop facility " + stopId.toString() + " not found in facilities. Stops connected to this facility will not be added to routes. Please check.");
			}
			return;
		}
		double arrivalDelay = 0.0;
		if (arrivalTime > 0 && firstDepartureTime > 0) {
			arrivalDelay = arrivalTime + initialDelay - firstDepartureTime;
		}
		double departureDelay = 0.0;
		if (departureTime > 0 && firstDepartureTime > 0) {
			departureDelay = departureTime + initialDelay - firstDepartureTime;
		} else if (arrivalDelay > 0) {
			departureDelay = arrivalDelay + initialDelay;
		}
		stops.add(createRouteStop(stopFacility, arrivalDelay, departureDelay));
	}

	/**
	 * @return A list of all departures of this route.
	 * If firstDepartureTime or usedVehicle are not set before this is called, null is returned.
	 * If vehicleType is not set, the vehicle is not in the list and entry will not be created.
	 */
	public List<Departure> getDepartures() {
		if (firstDepartureTime < 0 || getUsedVehicleId() == null) {
			log.error("getDepartures before first departureTime and usedVehicleId set.");
			return null;
		}
		if (usedVehicleType == null) {
			//log.warn("VehicleType not defined in vehicles list.");
			return null;
		}

		List<Departure> departures = new ArrayList<>();
		for (int i = 0; i < numberOfDepartures; i++) {
			// Departure ID
			Id<Departure> departureId = Id.create(routeId + "_" + String.format("%04d", i + 1), Departure.class);
			// Departure time
			double departureTime = firstDepartureTime + (i * cycleTime) - initialDelay;
			// Departure vehicle
			Id<Vehicle> vehicleId = Id.create(getUsedVehicleId() + "_" + String.format("%04d", i + 1), Vehicle.class);
			// Departure
			departures.add(createDeparture(departureId, departureTime, vehicleId));
		}
		return departures;
	}

	public List<TransitRouteStop> getStops() {
		return stops;
	}

	private TransitRouteStop createRouteStop(TransitStopFacility stopFacility, double arrivalDelay, double departureDelay) {
		TransitRouteStop routeStop = scheduleBuilder.createTransitRouteStop(stopFacility, arrivalDelay, departureDelay);
		routeStop.setAwaitDepartureTime(true); // Only *T-Lines (currently not implemented) would have this as false...
		return routeStop;
	}

	private Departure createDeparture(Id<Departure> departureId, double departureTime, Id<Vehicle> vehicleId) {
		Departure departure = scheduleBuilder.createDeparture(departureId, departureTime);
		departure.setVehicleId(vehicleId);
		return departure;
	}

	private static boolean isTypeOf(Class<? extends Enum> vehicleGroup, String vehicle) {
		for (Object val : vehicleGroup.getEnumConstants()) {
			if (((Enum) val).name().equals(vehicle)) {
				return true;
			}
		}
		return false;
	}

	public Id<TransitLine> getLineId() {
		return Id.create(operator+"_"+ routeName, TransitLine.class);
	}

	public Id<TransitRoute> createRouteId(int routeNr) {
		routeId = Id.create(routeName + "_" + String.format("%03d", routeNr), TransitRoute.class);
		return Id.create(routeName + "_" + String.format("%03d", routeNr), TransitRoute.class);
	}

	public String getMode() {
		return usedMode;
	}

}
