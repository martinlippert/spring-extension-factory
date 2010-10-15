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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.springframework.context.ApplicationContext;

/**
 * The Spring Extension Factory builds a bridge between the Eclipse Extension
 * Registry and the Spring Framework (especially Spring Dynamic Modules).
 * 
 * It allows you to define your extension as a spring bean within the spring
 * application context of your bundle. If you would like to use this bean as an
 * instance of an extension (an Eclipse RCP view, for example) you define the
 * extension with this spring extension factory as the class to be created.
 * 
 * To let the spring extension factory pick the right bean from your application
 * context you need to set the bean id to the same value as the id of the view
 * within the view definition, for example. This is important if your extension
 * definition contains more than one element, where each element has its own id.
 * 
 * If the extension definition elements themselves have no id attribute the
 * spring extension factory uses the id of the extension itself to identify the
 * bean.
 * 
 * The Spring extension factory waits for the application context to be created
 * using a timeout. Its default setting is five seconds. If you have a situation
 * where you need a longer timeout because the spring context creation takes
 * more time you can set the property
 * 
 *   org.eclipse.springextensionfactory.timeout
 * 
 * to the milliseconds you would like to use as timeout.
 * 
 * @author Martin Lippert
 */
public class SpringExtensionFactory implements IExecutableExtensionFactory,
		IExecutableExtension {

	private static final String TIMEOUT_PROPERTY_NAME = "org.eclipse.springextensionfactory.timeout";
	private Object bean;

	public Object create() throws CoreException {
		return bean;
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		String beanName = getBeanName(data, config);
		ApplicationContext appContext = getApplicationContext(config);

		if (appContext == null) {
			throw new RuntimeException("application context for bundle " + config.getContributor().getName() + " not found");
		}
		
		if (beanName == null) {
			throw new RuntimeException("bean id cound not be identified. Please check your extension declaration");
		}
		
		this.bean = appContext.getBean(beanName);
		if (this.bean instanceof IExecutableExtension) {
			((IExecutableExtension) this.bean).setInitializationData(
					config, propertyName, data);
		}
	}

	private String getBeanName(Object data, IConfigurationElement config) {
		
		// try the specific bean id the extension defines
		if (data != null && data.toString().length() > 0) {
			return data.toString();
		}

		// try the id of the config element
		if (config.getAttribute("id") != null) {
			return config.getAttribute("id");
		}

		// try the id of the extension element itself
		if (config.getParent() != null
				&& config.getParent() instanceof IExtension) {
			IExtension extensionDefinition = (IExtension) config.getParent();
			return extensionDefinition.getSimpleIdentifier();
		}

		return null;
	}

	private ApplicationContext getApplicationContext(
			IConfigurationElement config) {
		
		int timeout = 5000;
		try {
			String timeoutSetting = System.getProperty(TIMEOUT_PROPERTY_NAME);
			timeout = Integer.parseInt(timeoutSetting);
		}
		catch (Exception e) {
		}
		
		String contributorName = config.getContributor().getName();
		Bundle contributorBundle = Platform.getBundle(contributorName);

		if (contributorBundle.getState() != Bundle.ACTIVE) {
			try {
				contributorBundle.start();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}

		final ApplicationContextTracker applicationContextTracker = new ApplicationContextTracker(
				contributorBundle, Activator.getDefault().getBundleContext());
		ApplicationContext applicationContext = null;
		try {
			applicationContext = applicationContextTracker
					.getApplicationContext(timeout);
		} finally {
			applicationContextTracker.close();
		}
		return applicationContext;
	}

}
