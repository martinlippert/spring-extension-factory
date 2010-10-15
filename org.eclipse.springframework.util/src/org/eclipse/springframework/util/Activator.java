/*******************************************************************************
 * Copyright (c) 2008-2010 Martin Lippert and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Lippert            initial implementation     
 *******************************************************************************/
package org.eclipse.springframework.util;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	private static Activator instance;
	private BundleContext context;

	public Activator() {
		instance = this;
	}

	public void start(BundleContext context) throws Exception {
		this.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		this.context = null;
	}
	
	public BundleContext getBundleContext() {
		return context;
	}
	
	public static Activator getDefault() {
		return instance;
	}

}
