
# A Bridge between Spring and the Equinox Extension Registry
      
  Have you ever asked yourself who to inject spring beans via Spring Dynamic Modules
  into views, editors or other things you have registered as extensions with your Eclipse-based
  app? If so, this SpringExtensionFactory is for you.

  If allows you to use Spring Dynamic Modules together with the Extension Registry
  of Eclipse in an easy way.

## Installation

  Download the pre-build plugin and add it to your target platform.

## Defining the Extension

  Instead of telling the extension the real class of your view or editor, use
  
    org.eclipse.springframework.util.SpringExtensionFactory

  Defining a view extension for a typical RCP app then looks like:

  <extension point="org.eclipse.ui.views">
    <view name="Message"
      allowmultiple="true"
      icon="icons/sample2.gif"
      class="org.eclipse.springframework.
             util.SpringExtensionFactory:myview"
      id="org.eclipse.example.mailview">
    </view>
  </extension>

  With the ":myview" at the end of the class attributes value you
  can define which Spring bean to use for this extension.

## Defining the Spring Bean

  The "real" view implementation can then be defined within the Spring context
  of this bundle:

  <bean id="myview"
      class="org.eclipse.example.springdm.rcpview.View"
      scope="prototype">
    <property name="myService" ref="serviceBean"/>
  </bean>

  Its important to use the scope="prototype" to let Spring create a new object
  every time a new extension is created. Otherwise Spring would always return
  a singleton bean, which can cause a lot of trouble within your RCP app.

  Now you can do with this Spring bean whatever Spring allows you to do with
  beans. You can inject dependencies via setter methods or constructors, etc.

## Different ways to identify your bean from within the extension definition

  The notation ":myview" at the end of the extensions class attribute is the
  preferred way of identifying the bean. But there might be situations where
  this seems to be duplicated information inside the extension definition.
  For example: views in RCP apps typically already have an id attribute as
  part of their extension ("org.eclipse.example.mailview" in the example above)
  Every extension itself can have an id attribute as well.

  Therefore the Spring Extension Registry uses a three-step model to find
  out which bean it should use:

  * First the ":beanid" that is written at the end of the class attribute.
  * The id of the containing extension definition (the view element in the example)
  * At last the id of the extension definition itself.
  
This allows you to avoid duplication of ids. Nevertheless I would always
vote for the first point, since this is the most explicit one.

## Spring context creation

  Sometimes the bundle that contains is not yet active when the extension
  should be created. Therefore the spring context for this bundle hasn't
  been created at that point. The good thing is that you don't have to take
  care of this yourself. The spring extension factory will automatically
  start the bundle (which triggers the spring dm osgi extender to create
  the context). But this happens asynchronously and the extension creation
  has to wait for this to finish. To avoid system hangs if something goes
  wrong, the spring extension factory uses a timeout for this. The default
  value for this is 5 seconds. In any case your spring context needs more
  time to come up (for whatever reason) you can define the timeout value
  with this property:

    -Dorg.eclipse.springextensionfactory.timeout=20000

  The value is in milliseconds, so this would set the timeout up to 20sec.

## License 

This work is made available under EPL (Eclipse Public License)

Copyright (c) 2008-2010 Martin Lippert and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
