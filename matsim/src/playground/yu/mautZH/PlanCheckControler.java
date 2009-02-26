/* *********************************************************************** *
 * project: org.matsim.*
 * PlanCheckControler.java
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

/**
 *
 */
package playground.yu.mautZH;

import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationReader;

/**
 * @author ychen
 *
 */
public class PlanCheckControler {
	public static void main(final String[] args) {
		final String netFilename = "./test/yu/schweiz/input/ch.xml";
		final String plansFilename = "./test/yu/schweiz/input/100ITERs_pt-6t-6output_plans.xml";
		final String planCheckFilename = "./test/yu/schweiz/output/planCheck.txt";
		Gbl
				.createConfig(new String[] { "./test/yu/schweiz/multipleIterations_.xml" });

		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(netFilename);

		Population population = new PopulationImpl();
		PlanChecker pc = new PlanChecker(planCheckFilename);
		population.addAlgorithm(pc);
		PopulationReader plansReader = new MatsimPopulationReader(population,
				network);
		plansReader.readFile(plansFilename);
		population.runAlgorithms();
		pc.writeResult();
	}
}
