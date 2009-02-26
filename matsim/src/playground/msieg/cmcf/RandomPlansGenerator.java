/* *********************************************************************** *
 * project: org.matsim.*
 * RandomPlanGenerator.java
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

package playground.msieg.cmcf;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.matsim.basic.v01.IdImpl;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.Coord;
import org.matsim.interfaces.basic.v01.BasicLeg.Mode;
import org.matsim.interfaces.core.v01.Act;
import org.matsim.interfaces.core.v01.CarRoute;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.PersonImpl;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationWriter;
import org.matsim.population.PopulationWriterHandlerImplV4;
import org.matsim.population.routes.NodeCarRoute;
import org.matsim.router.Dijkstra;
import org.matsim.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.router.util.LeastCostPathCalculator;
import org.matsim.router.util.PreProcessDijkstra;
import org.xml.sax.SAXException;

/**
 * This class is designed to create random plan files for
 * a given network.
 *
 * @author msieg
 *
 */
public class RandomPlansGenerator {

	protected NetworkLayer network;

	private Random rand;

	private String networkFile;

	private LeastCostPathCalculator routingAlgo;

	private final int workDuration = 4 * 3600; //4 hours of work

	public RandomPlansGenerator(){
		this.setSeed(this.hashCode());
	}


	public RandomPlansGenerator(final String s){
		this();
		this.setNetworkFile(s);
		try {
			this.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setSeed(final long seed){
		if(this.rand == null)
			this.rand = new Random(seed);
		else
			this.rand.setSeed(seed);
	}

	public String getNetworkFile() {
		return this.networkFile;
	}

	public void setNetworkFile(final String networkFile) {
		this.networkFile = networkFile;
	}

	public void read() throws IOException {
		this.network = new NetworkLayer();
		MatsimNetworkReader netReader = new MatsimNetworkReader( this.network );
		try {
			netReader.parse(this.networkFile);
			this.network.connect();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw e;
		}
	}

	private void initDijkstra(){
		PreProcessDijkstra preProcessData = new PreProcessDijkstra();
		preProcessData.run(this.network);
		if(Gbl.getConfig() == null){
			Gbl.createWorld();
			Gbl.createConfig(new String[] {});
		}
		final FreespeedTravelTimeCost costFunction = new FreespeedTravelTimeCost();
		this.routingAlgo = new Dijkstra(this.network, costFunction, costFunction, preProcessData);
	}

	public Population createPlans(final int numberOfAgents){
		return this.createPlans(numberOfAgents, 6*3600, 7*3600);
	}

	public Population createPlans(final int numberOfAgents, final int startTime, final int endTime){
		Population pop = new PopulationImpl(PopulationImpl.NO_STREAMING);
		this.initDijkstra();

		for(int i=1; i <= numberOfAgents; i++){
			Person person = (new PersonImpl(new IdImpl(i)));
			Plan plan = new org.matsim.population.PlanImpl(person);
			person.addPlan(plan);

			//create random home and work link
			Link lHome, lWork;
			LeastCostPathCalculator.Path lPath = null;
			double randTime;
			do{
				lHome = this.getRandomLink();
				lWork = lHome;
				while(lHome == lWork)
					lWork = this.getRandomLink();
				randTime = startTime+this.rand.nextInt(endTime-startTime);
				lPath = this.routingAlgo.calcLeastCostPath(lHome.getToNode(), lWork.getFromNode(), randTime);
			}while(lPath == null);
			Coord homeCoord = lHome.getCenter(), workCoord = lWork.getCenter();

			//create home act
			Act a = plan.createAct("h", homeCoord);
			a.setLink(lHome);
			a.setStartTime(startTime);
			a.setEndTime(endTime);

			//create leg to work
			Leg leg = plan.createLeg(Mode.car);
			leg.setDepartureTime(randTime);
			CarRoute route = new NodeCarRoute(lHome, lWork);
			route.setNodes(lHome, lPath.nodes, lWork);
			leg.setRoute(route);

			//create work act
			a = plan.createAct("h", workCoord);
			a.setLink(lWork);
			a.setDuration(this.workDuration);

			//finally add person to population instance
			pop.addPerson(person);
		}

		return pop;
	}

	private Link getRandomLink(){
		int i = this.rand.nextInt(this.network.getLinks().size());
		Link randomLink = null;
		for (Iterator<Link> iterator = this.network.getLinks().values().iterator(); i >= 0; i--) {
			randomLink = iterator.next();
		}
		return randomLink;
	}

	public void writePlans(final Population plans){
		PopulationWriter pwriter = new PopulationWriter(plans);
		pwriter.setWriterHandler(new PopulationWriterHandlerImplV4());
		pwriter.write();
	}

	public void writePlans(final Population plans, final String file){
		PopulationWriter pwriter = new PopulationWriter(plans, file, "v4", 1.0);
		pwriter.setWriterHandler(new PopulationWriterHandlerImplV4());
		pwriter.write();
	}

	public static void main(final String[] args) {
		String inFile = "examples/siouxfalls/network.xml";
		if(args.length == 0){
			System.out.println("usage: java "+RandomPlansGenerator.class.getSimpleName() +
					"networkFile [numberOfAgents] [startTime] [endTime]");
			//System.exit(10);
		}
		else
			inFile = args[0];
		int number=3, start=6*3600, end=7*3600;
		if(args.length > 1)
			number = Integer.parseInt(args[1]);
		if(args.length > 2)
			start = Integer.parseInt(args[2]);
		if(args.length > 3)
			end = Integer.parseInt(args[3]);

		RandomPlansGenerator rpg = new RandomPlansGenerator(inFile);

		Population pop = rpg.createPlans(number, start, end);

		String outFile = "";
		if(inFile.lastIndexOf('/') != -1)
			outFile = inFile.substring(0, inFile.lastIndexOf('/')+1);
		outFile += "randPlans"+number+"_"+System.currentTimeMillis()+".xml";

		System.out.println(pop);
		rpg.writePlans(pop, outFile);
		System.out.println("\tNew Plans-File written to: "+outFile);
	}
}

