/* *********************************************************************** *
 * project: org.matsim.*
 * PopulationCreation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.balmermi.census2000v2;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.config.ConfigWriter;
import org.matsim.facilities.Facilities;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.network.NetworkWriter;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationReader;
import org.matsim.population.PopulationWriter;
import org.matsim.world.algorithms.WorldCheck;
import org.matsim.world.algorithms.WorldConnectLocations;
import org.matsim.world.algorithms.WorldMappingInfo;

import playground.balmermi.census2000v2.modules.PersonAssignToNetwork;

public class IIDMAssign2Network {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final static Logger log = Logger.getLogger(IIDMAssign2Network.class);

	//////////////////////////////////////////////////////////////////////
	// createPopulation()
	//////////////////////////////////////////////////////////////////////

	public static void assignNetwork() {

		log.info("MATSim-DB: assignNetwork...");

		//////////////////////////////////////////////////////////////////////

		log.info("  extracting input directory... ");
		String indir = Gbl.getConfig().facilities().getInputFile();
		indir = indir.substring(0,indir.lastIndexOf("/"));
		log.info("    "+indir);
		log.info("  done.");

		log.info("  extracting output directory... ");
		String outdir = Gbl.getConfig().facilities().getOutputFile();
		outdir = outdir.substring(0,outdir.lastIndexOf("/"));
		log.info("    "+outdir);
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  reading facilities xml file...");
		Facilities facilities = (Facilities)Gbl.getWorld().createLayer(Facilities.LAYER_TYPE, null);
		new MatsimFacilitiesReader(facilities).readFile(Gbl.getConfig().facilities().getInputFile());
		Gbl.getWorld().complete();
		log.info("  done.");

		System.out.println("  reading the network xml file...");
		NetworkLayer network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
		Gbl.getWorld().complete();
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  running world modules... ");
		// the link types where no facility can be placed on
		// Here: for the ivtch network (nationales Netzmodell)
		Set<String> excludingLinkTypes = new HashSet<String>();
		excludingLinkTypes.add("0"); excludingLinkTypes.add("1"); excludingLinkTypes.add("2"); excludingLinkTypes.add("3");
		excludingLinkTypes.add("4"); excludingLinkTypes.add("5"); excludingLinkTypes.add("6"); excludingLinkTypes.add("7");
		excludingLinkTypes.add("8"); excludingLinkTypes.add("9");
		excludingLinkTypes.add("10"); excludingLinkTypes.add("11"); excludingLinkTypes.add("12"); excludingLinkTypes.add("13");
		excludingLinkTypes.add("14"); excludingLinkTypes.add("15"); excludingLinkTypes.add("16"); excludingLinkTypes.add("17");
		excludingLinkTypes.add("18"); excludingLinkTypes.add("19");
		excludingLinkTypes.add("20"); excludingLinkTypes.add("21"); excludingLinkTypes.add("22"); excludingLinkTypes.add("23");
		excludingLinkTypes.add("24"); excludingLinkTypes.add("25"); excludingLinkTypes.add("26"); excludingLinkTypes.add("27");
		excludingLinkTypes.add("28"); excludingLinkTypes.add("29");
		excludingLinkTypes.add("90"); excludingLinkTypes.add("91"); excludingLinkTypes.add("92"); excludingLinkTypes.add("93");
		excludingLinkTypes.add("94"); excludingLinkTypes.add("95"); excludingLinkTypes.add("96"); excludingLinkTypes.add("97");
		excludingLinkTypes.add("98"); excludingLinkTypes.add("99");
		new WorldCheck().run(Gbl.getWorld());
		new WorldConnectLocations(excludingLinkTypes).run(Gbl.getWorld());
		new WorldMappingInfo().run(Gbl.getWorld());
		new WorldCheck().run(Gbl.getWorld());
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  setting up population objects...");
		Population pop = new PopulationImpl(PopulationImpl.USE_STREAMING);
		PopulationWriter pop_writer = new PopulationWriter(pop);
		PopulationReader pop_reader = new MatsimPopulationReader(pop);
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  adding person modules... ");
		pop.addAlgorithm(new PersonAssignToNetwork(network));
		log.info("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  reading, processing, writing plans...");
		pop.addAlgorithm(pop_writer);
		pop_reader.readFile(Gbl.getConfig().plans().getInputFile());
		pop.printPlansCount();
		pop_writer.write();
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		log.info("  writing network xml file... ");
		new NetworkWriter(network).write();
		log.info("  done.");

		log.info("  writing facilities xml file... ");
		new FacilitiesWriter(facilities).write();
		log.info("  done.");

		log.info("  writing config xml file... ");
		new ConfigWriter(Gbl.getConfig()).write();
		log.info("  done.");

		log.info("done.");
	}

	//////////////////////////////////////////////////////////////////////
	// main
	//////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) {

		Gbl.startMeasurement();

		Gbl.createConfig(args);
		Gbl.createWorld();

		assignNetwork();

		Gbl.printElapsedTime();
	}
}
