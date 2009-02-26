/* *********************************************************************** *
 * project: org.matsim.*
 * PersonTest.java
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

package org.matsim.population;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.IdImpl;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.utils.CRCChecksum;

public class DesiresTest extends MatsimTestCase {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final static Logger log = Logger.getLogger(DesiresTest.class);

	//////////////////////////////////////////////////////////////////////
	// setUp / tearDown
	//////////////////////////////////////////////////////////////////////

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		super.loadConfig(null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//////////////////////////////////////////////////////////////////////
	// tests
	//////////////////////////////////////////////////////////////////////

	public void testReadWriteDesires() {
		log.info("running testReadWriteDesires()... ");

		log.info("  creating single person with desires... ");
		PopulationImpl pop = new PopulationImpl(false);
		Person p = new PersonImpl(new IdImpl(0));
		pop.addPerson(p);
		Desires d = p.createDesires("created by 'DesiresTest.testReadWriteDesires'");
		if (!d.putActivityDuration("home","16:00:00")) throw new RuntimeException("'home' actDur not added to the desires.");
		if (!d.putActivityDuration("work",8*3600)) throw new RuntimeException("'work' actDur not added to the desires.");
		if (!d.removeActivityDuration("home")) throw new RuntimeException("'home' actDur not removed from the desires.");
		if (d.removeActivityDuration("home")) throw new RuntimeException("non extisting 'home' actDur removed from the desires.");
		if (!d.putActivityDuration("home",16*3600)) throw new RuntimeException("'home' actDur not added to the desires.");
		log.info("  done.");

		log.info("  writing population file...");
		new PopulationWriter(pop,super.getOutputDirectory()+"plans.xml","v4").write();
		log.info("  done.");

		log.info("  clean up population...");
		pop.clearPersons();
		log.info("  done.");

		log.info("  reading in created population file...");
		new PopulationReaderMatsimV4(pop, null).readFile(super.getOutputDirectory()+"plans.xml");
		log.info("  done.");

		log.info("  writing population file again...");
		new PopulationWriter(pop,super.getOutputDirectory()+"plans.equal.xml","v4").write();
		log.info("  done.");

		log.info("  check for identity ofthe two population...");
		long checksum_ref = CRCChecksum.getCRCFromFile(super.getOutputDirectory()+"plans.xml");
		long checksum_run = CRCChecksum.getCRCFromFile(super.getOutputDirectory()+"plans.equal.xml");
		assertEquals("different population files",checksum_ref,checksum_run);
		log.info("  done.");

		log.info("done.");
	}
}
