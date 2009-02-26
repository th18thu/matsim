/* *********************************************************************** *
 * project: org.matsim.*
 * TravelTimeModalSplitTest.java
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
package playground.yu.analysis;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.utils.io.IOUtils;

/**
 * @author ychen
 *
 */
public class LegCountTest2 {
	public static class LegCount extends AbstractPersonAlgorithm {
		private BufferedWriter writer;
		// private BasicLeg tmpLeg;
		// private boolean actsAtSameLink;
		private int carUserCount = 0, carLegCount = 0, ptUserCount = 0,
				ptLegCount = 0, licensedCarUserCount = 0,
				licensedPtUserCount = 0, licensedCarLegCount = 0,
				licensedPtLegCount = 0;

		public LegCount(final String filename) {
			try {
				this.writer = IOUtils.getBufferedWriter(filename);
				this.writer.write("personId\tLegNumber\n");
				this.writer.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void end() {
			try {
				this.writer.write("------------------------------------\ncarUser:\t"
						+ this.carUserCount
						+ ";\tcarLegs:\t"
						+ this.carLegCount
						+ ";\t"
						+ (double) this.carLegCount / (double) this.carUserCount
						+ "\tLegs pro carUser;\tlicensedCarUser:\t"
						+ this.licensedCarUserCount
						+ ";\tlicensedCarLegs:\t"
						+ this.licensedCarLegCount
						+ ";\t"
						+ (double) this.licensedCarLegCount
								/ (double) this.licensedCarUserCount
						+ "\tLegs pro licensedCarUser;\n" + "\nptUser:\t"
						+ this.ptUserCount + ";\tptLegs:\t" + this.ptLegCount + ";\t"
						+ (double) this.ptLegCount / (double) this.ptUserCount
						+ "\tLegs pro ptUser;\tlicensedPtUser:\t"
						+ this.licensedPtUserCount + ";\tlicensedPtLegs:\t"
						+ this.licensedPtLegCount + ";\t"
						+ (double) this.licensedPtLegCount
						/ (double) this.licensedPtUserCount
						+ "\tLegs pro licensedPtUser.");
				this.writer.flush();
				this.writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void carAppend(final int legsNumber) {
			this.carUserCount++;
			this.carLegCount += legsNumber;
		}

		private void carLicensedAppend(final int legsNumber) {
			this.licensedCarUserCount++;
			this.licensedCarLegCount += legsNumber;
		}

		@Override
		public void run(final Person person) {
			Plan p = person.getSelectedPlan();
			if (p != null) {
				int nLegs = (p.getActsLegs().size() + 1) / 2;
				try {
					this.writer.write(person.getId() + "\t" + nLegs + "\n");
					this.writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Plan.Type planType = p.getType();
				if (// planType != null && Plan.Type.UNDEFINED != planType
				!PlanModeJudger.useUndefined(p))
					if (// planType.equals(Plan.Type.CAR)
					PlanModeJudger.useCar(p)) {
						carAppend(nLegs);
						if (person.getLicense().equals("yes"))
							carLicensedAppend(nLegs);
					} else if (
					// planType.equals(Plan.Type.PT)
					PlanModeJudger.usePt(p)) {
						this.ptUserCount++;
						this.ptLegCount += nLegs;
						if (person.getLicense().equals("yes")) {
							this.licensedPtUserCount++;
							this.licensedPtLegCount += nLegs;
						}
					}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		// final String netFilename = "./test/yu/ivtch/input/network.xml";
		final String netFilename = "../data/ivtch/input/network.xml";
		// final String netFilename = "./test/yu/equil_test/equil_net.xml";
		// final String plansFilename = "../runs/run264/100.plans.xml.gz";
		final String plansFilename = "../data/ivtch/run271/ITERS/it.100/100.plans.xml.gz";
		// final String plansFilename =
		// "./test/yu/equil_test/output/100.plans.xml.gz";
		// final String outFilename = "./output/legsCount.txt.gz";
		final String outFilename = "../data/ivtch/run271/legsCount.txt";

		Gbl.startMeasurement();
		Gbl.createConfig(null);

		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(netFilename);

		Population population = new PopulationImpl();

		LegCount lc = new LegCount(outFilename);
		population.addAlgorithm(lc);

		System.out.println("-->reading plansfile: " + plansFilename);
		new MatsimPopulationReader(population, network).readFile(plansFilename);

		population.runAlgorithms();

		lc.end();

		System.out.println("--> Done!");
		Gbl.printElapsedTime();
		System.exit(0);
	}
}
