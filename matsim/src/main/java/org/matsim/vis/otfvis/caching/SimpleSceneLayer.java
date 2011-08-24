/* *********************************************************************** *
 * project: org.matsim.*
 * SimpleSceneLayer.java
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

package org.matsim.vis.otfvis.caching;

import java.util.ArrayList;
import java.util.List;

import org.matsim.vis.otfvis.data.OTFDrawable;


/**
 *  * The SimpleSceneLayer is the one SceneLayer that is guaranteed to be present in the OTFVis.
 * Every element that is not mapped to a specific layer is added to this layer.

 * @author dstrippgen
 *
 */
public class SimpleSceneLayer implements SceneLayer {
	
	private final List<OTFDrawable> items = new ArrayList<OTFDrawable>();

	@Override
	public void addItem(OTFDrawable item) {
		items.add(item);
	}
	
	@Override
	public void glInit() {
		// Nothing to do
	}

	@Override
	public void draw() {
		for(OTFDrawable item : items) {
			item.draw();
		}
	}
	
	@Override
	public void finish() {
	}

	@Override
	public void init(boolean initConstData) {
	}

	@Override
	public int getDrawOrder() {
		return 100;
	}

	@Override
	public OTFDrawable newInstanceOf(Class clazz) {
		try {
			return (OTFDrawable) clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	
	
}