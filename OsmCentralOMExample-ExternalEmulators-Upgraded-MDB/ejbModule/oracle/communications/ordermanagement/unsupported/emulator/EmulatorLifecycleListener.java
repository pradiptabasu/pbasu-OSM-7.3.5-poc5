/*
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
 */
package oracle.communications.ordermanagement.unsupported.emulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weblogic.application.ApplicationLifecycleEvent;
import weblogic.application.ApplicationLifecycleListener;

/**
 * EmulatorLifecycleListener handles emulator application shutdown and startup.
 * 
 */
public class EmulatorLifecycleListener extends ApplicationLifecycleListener {

    private static final Log LOG = LogFactory.getLog(EmulatorLifecycleListener.class);

    /**
     * preStart is used to set the variables before start of an application.
     * 
     * @param evt
     *            ApplicationLifecycleEvent help to get the Application Context
     */

    @Override
    public void preStart(final ApplicationLifecycleEvent evt) {
        Environment.setBuildVersion("1.0.0");
        Environment.setPluginName(evt.getApplicationContext().getApplicationId());
        Environment.logPluginPreStartMessage();
    }

    /**
     * postStart is used to set the variables after start of an application.
     * 
     * @param evt
     *            ApplicationLifecycleEvent help to get the Application Context
     */
    @Override
    public void postStart(final ApplicationLifecycleEvent evt) {

        Environment.logPluginPostStartMessage();

    }

    /**
     * preStop is used to set the variables before stop of an application.
     * 
     * @param evt
     *            ApplicationLifecycleEvent help to get the Application Context
     */
    @Override
    public void preStop(final ApplicationLifecycleEvent evt) {
        Environment.logPluginPreStopMessage();

    }

    /**
     * postStop is used to set the variables after stop of an application.
     * 
     * @param evt
     *            ApplicationLifecycleEvent help to get the Application Context
     */
    @Override
    public void postStop(final ApplicationLifecycleEvent evt) {

        Environment.logPluginPostStopMessage();
    }

}
