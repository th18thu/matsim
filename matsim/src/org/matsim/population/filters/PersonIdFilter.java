/* *********************************************************************** *
 * project: org.matsim.*
 * PersonIdFilter.java
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

package org.matsim.population.filters;

import java.util.regex.Pattern;

import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.PersonAlgorithm;

/**
 * Filters persons whose id matches a certain pattern (regular expression).
 *
 * @author kmeister
 */
public class PersonIdFilter extends AbstractPersonFilter {

	private String personIdPattern = null;
	
	public PersonIdFilter(String personIdPattern, PersonAlgorithm nextAlgorithm) {
		super();
		this.personIdPattern = personIdPattern;
		this.nextAlgorithm = nextAlgorithm;
	}

	@Override
	public boolean judge(Person person) {

		String personId = person.getId().toString();
		
		if (Pattern.matches(this.personIdPattern, personId)) {
			return true;
		}
		
		return false;
	}

}
