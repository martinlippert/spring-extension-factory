/*******************************************************************************
 * Copyright (c) 2008-2010 metafinanz and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Heiko Seeberger - initial implementation
 *     Martin Lippert - slightly changed and using waitForService now
 *******************************************************************************/
package org.eclipse.springframework.util;

import static java.text.MessageFormat.format;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.context.ApplicationContext;

/**
 * @author Heiko Seeberger
 */
public class ApplicationContextTracker {

	private static final String FILTER = "(&(objectClass=org.springframework.context.ApplicationContext)" //$NON-NLS-1$
			+ "(org.springframework.context.service.name={0}))"; //$NON-NLS-1$

	private ServiceTracker applicationContextServiceTracker;

	/**
	 * @param contributorBundle
	 * 		OSGi bundle for which the Spring application context is to be
	 * 		tracked. Must not be null!
	 * @param factoryBundleContext
	 *      BundleContext object which can be used to track services
	 * @param timeout 
	 * @throws IllegalArgumentException
	 * 		if the given bundle is null.
	 */
	public ApplicationContextTracker(final Bundle contributorBundle, final BundleContext factoryBundleContext) {
		final String filter = format(FILTER, contributorBundle.getSymbolicName());
		try {
			applicationContextServiceTracker = new ServiceTracker(
					factoryBundleContext, FrameworkUtil.createFilter(filter), null);
			applicationContextServiceTracker.open();
		} catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (applicationContextServiceTracker != null) {
			applicationContextServiceTracker.close();
		}
	}

	public ApplicationContext getApplicationContext(int timeout) {
		ApplicationContext applicationContext = null;
		if (applicationContextServiceTracker != null) {
			try {
				applicationContext = (ApplicationContext) applicationContextServiceTracker
						.waitForService(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return applicationContext;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
