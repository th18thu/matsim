/* *********************************************************************** *
 * project: org.matsim.*
 * SimVehicle.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package org.matsim.ptproject.qsim.qnetsimengine;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.DriverAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.ptproject.qsim.interfaces.MobsimVehicle;
import org.matsim.vehicles.Vehicle;

/**
 * interface to the ``Q'' implementation of the MobsimVehicle.  
 * <p/>
 * Design thoughts:<ul>
 * <li> This needs to be public since the ``Q'' version of the 
 * vehicle is used by more than one package.  This interfaces should, however, not be used outside the relevant 
 * netsimengines.  In particular, the information should not be used for visualization.  kai, nov'11
 * <li> Might be possible to make all methods in this hierarchy protected and just inherit to a QueueVehicle.  kai, nov'11
 * </ul>
 * 
 * @author nagel
 */

public class QVehicle extends QItem implements MobsimVehicle {

	private double linkEnterTime = Double.NaN;
	private double earliestLinkExitTime = 0;
	private DriverAgent driver = null;
	private Id id;
	private Link currentLink = null;
	private double sizeInEquivalents;
	private Vehicle basicVehicle;
	
	protected QVehicle(final Vehicle basicVehicle) {
		this(basicVehicle, 1.0);
	}

	protected QVehicle(final Vehicle basicVehicle, final double sizeInEquivalents) {
		this.id = basicVehicle.getId();
		this.sizeInEquivalents = sizeInEquivalents;
		this.basicVehicle = basicVehicle;
	}

	public void setCurrentLink(final Link link) {
		this.currentLink = link;
	}
	// yy not sure if this needs to be publicly exposed

	/**Design thoughts:<ul>
	 * <li> I am fairly sure that this should not be publicly exposed.  As far as I can tell, it is used in order to 
	 * figure out of a visualizer should make a vehicle "green" or "red".  But green or red should be related to 
	 * vehicle speed, and the mobsim should figure that out, not the visualizer.  So something like "getCurrentSpeed" 
	 * seems to be a more useful option. kai, nov'11
	 * <li> The problem is not only the speed, but also the positioning of the vehicle in the "asQueue" plotting method.
	 * (Although, when thinking about it: Should be possible to obtain same result by using "getEarliestLinkExitTime()".
	 * But I am not sure if that would really be a conceptual improvement ... linkEnterTime is, after all, a much more
	 * "physical" quantity.)  kai, nov'11
	 * <li> But maybe it should then go into MobsimVehicle?  kai, nov'11
	 * <li> Also see comment under setLinkEnterTime().  kai, nov'11 
	 * </ul>
	 */
	public double getLinkEnterTime() {
		return this.linkEnterTime;
	}

	/**Design thoughts:<ul>
	 * <li> This has to remain public as long as QVehicle/QVehicleImpl is both used by QueueSimulation and QSim.  At best,
	 * we could say that there should also be a MobsimVehicle interface that does not expose this.  kai, nov'11.
	 * (This is there now.  kai, nov'11)
	 * </ul>
	 */
	public void setLinkEnterTime(final double time) {
		this.linkEnterTime = time;
	}

	@Override
	public double getEarliestLinkExitTime() {
		return this.earliestLinkExitTime;
	}

	@Override
	public void setEarliestLinkExitTime(final double time) {
		this.earliestLinkExitTime = time;
	}

	@Override
	public Link getCurrentLink() {
		return this.currentLink;
	}

	@Override
	public MobsimDriverAgent getDriver() {
		if ( this.driver instanceof MobsimDriverAgent ) {
			return (MobsimDriverAgent) this.driver;
		} else if ( this.driver==null ) {
			return null ;
		} else {
			throw new RuntimeException( "error (downstream methods need to be made to accept DriverAgent)") ;
		}
	}

	public void setDriver(final DriverAgent driver) {
		this.driver = driver;
	}

	@Override
	public Id getId() {
		return this.id;
	}

	@Override
	public double getSizeInEquivalents() {
		return this.sizeInEquivalents;
	}

	@Override
	public Vehicle getVehicle() {
		return this.basicVehicle;
	}

	@Override
	public String toString() {
		return "Vehicle Id " + getId() + ", driven by (personId) " + this.driver.getId()
				+ ", on link " + this.currentLink.getId();
	}

}
