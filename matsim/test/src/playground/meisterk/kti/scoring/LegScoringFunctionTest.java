/* *********************************************************************** *
 * project: org.matsim.*
 * LegScoringFunctionTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package playground.meisterk.kti.scoring;

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.CharyparNagelScoringConfigGroup;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.GenericRoute;
import org.matsim.core.population.routes.RouteWRefs;
import org.matsim.core.scoring.CharyparNagelScoringParameters;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestCase;

import playground.meisterk.kti.config.KtiConfigGroup;
import playground.meisterk.kti.router.PlansCalcRouteKtiInfo;

public class LegScoringFunctionTest extends MatsimTestCase {

	private Config config = null;
	private KtiConfigGroup ktiConfigGroup = null;
	private NetworkLayer network = null;
	private PersonImpl testPerson = null;
	private PlanImpl testPlan = null;
	private PlansCalcRouteKtiInfo plansCalcRouteKtiInfo = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.config = super.loadConfig(null);
		
		ktiConfigGroup = new KtiConfigGroup();
		ktiConfigGroup.setUsePlansCalcRouteKti(false);
		ktiConfigGroup.setPtHaltestellenFilename(this.getClassInputDirectory() + "haltestellen.txt");
		ktiConfigGroup.setPtTraveltimeMatrixFilename(this.getClassInputDirectory() + "pt_Matrix.mtx");
		ktiConfigGroup.setWorldInputFilename(this.getClassInputDirectory() + "world.xml");
		ktiConfigGroup.setDistanceCostCar(1.6);
		ktiConfigGroup.setDistanceCostPtNoTravelCard(1.0);
		ktiConfigGroup.setDistanceCostPtUnknownTravelCard(0.5);
		ktiConfigGroup.setTravelingBike(-2.0);
		this.config.addModule(KtiConfigGroup.GROUP_NAME, ktiConfigGroup);
		
		CharyparNagelScoringConfigGroup charyparNagelConfigGroup = this.config.charyparNagelScoring();
		charyparNagelConfigGroup.setMarginalUtlOfDistancePt(-0.5);
		charyparNagelConfigGroup.setMarginalUtlOfDistanceCar(-0.1);
		charyparNagelConfigGroup.setTravelingPt(-10.0);
		charyparNagelConfigGroup.setTraveling(0.0);
		charyparNagelConfigGroup.setTravelingWalk(-100.0);

		network = new NetworkLayer();
		
		network.createNode(new IdImpl(1), new CoordImpl(1000.0, 1000.0));
		network.createNode(new IdImpl(2), new CoordImpl(1100.0, 1100.0));
		network.createNode(new IdImpl(3), new CoordImpl(1200.0, 1200.0));

		network.createLink(new IdImpl(1), network.getNode("1"), network.getNode("2"), 1.0, 1.0, 1.0, 1.0);
		network.createLink(new IdImpl(2), network.getNode("2"), network.getNode("3"), 1.0, 1.0, 1.0, 1.0);

		testPerson = new PersonImpl(new IdImpl("123"));
		testPlan = new PlanImpl();
		testPerson.addPlan(testPlan);
		
		ActivityImpl home = new ActivityImpl("home", network.getLink("1"));
		home.setCoord(new CoordImpl(1050.0, 1050.0));
		ActivityImpl work = new ActivityImpl("work", network.getLink("2"));
		work.setCoord(new CoordImpl(1150.0, 1150.0));

		LegImpl testLeg = new LegImpl(TransportMode.undefined);

		testPlan.addActivity(home);
		testPlan.addLeg(testLeg);
		testPlan.addActivity(work);
		
		this.plansCalcRouteKtiInfo = new PlansCalcRouteKtiInfo();
		this.plansCalcRouteKtiInfo.prepare(ktiConfigGroup, this.network);

	}

	@Override
	protected void tearDown() throws Exception {
		this.plansCalcRouteKtiInfo = null;
		this.testPlan = null;
		this.testPerson = null;
		this.network = null;
		this.ktiConfigGroup = null;
		this.config = null;
		super.tearDown();
	}

	public void testCalcLegScorePt() {
		this.runATest(TransportMode.pt, null, -10.0);
		this.ktiConfigGroup.setUsePlansCalcRouteKti(true);
		this.runATest(TransportMode.pt, null, -5.688799);
	}

	public void testCalcLegScorePtUnknown() {
		this.runATest(TransportMode.pt, "unknown", -7.5);
		this.ktiConfigGroup.setUsePlansCalcRouteKti(true);
		this.runATest(TransportMode.pt, "unknown", -5.618089);
	}	
	
	public void testCalcLegScoreCar() {
		this.runATest(TransportMode.car, null, -1.6);
	}
	
	public void testCalcLegScoreBike() {
		this.runATest(TransportMode.bike, null, -1.0);
	}
	
	public void testCalcLegScoreWalk() {
		this.runATest(TransportMode.walk, null, -50.0);
	}
	
	private void runATest(final TransportMode mode, final String travelCard, final double expectedScore) {
		
		if (travelCard != null) {
			this.testPerson.addTravelcard(travelCard);
		}
		
		LegImpl testLeg = (LegImpl) this.testPlan.getPlanElements().get(1);
		testLeg.setMode(mode);
		RouteWRefs route = network.getFactory().createRoute(
				mode, 
				testPlan.getPreviousActivity(testLeg).getLink(), 
				testPlan.getNextActivity(testLeg).getLink());
		testLeg.setRoute(route);
		if (TransportMode.pt.equals(mode)) {
			((GenericRoute) route).setRouteDescription(
					testPlan.getPreviousActivity(testLeg).getLink(), 
					"8503006 26101 26102 8503015", 
					testPlan.getNextActivity(testLeg).getLink());
		}
		route.setDistance(10000.0);

		CharyparNagelScoringParameters charyparNagelParams = new CharyparNagelScoringParameters(config.charyparNagelScoring());
		LegScoringFunction testee = new LegScoringFunction(
				testPlan, 
				charyparNagelParams,
				config,
				this.ktiConfigGroup, 
				this.plansCalcRouteKtiInfo);
		double actualLegScore = testee.calcLegScore(Time.parseTime("06:00:00"), Time.parseTime("06:30:00"), testLeg);

		assertEquals(expectedScore, actualLegScore, 1e-6);


	}

}
