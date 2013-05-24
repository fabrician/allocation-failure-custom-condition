/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 * 
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code. 
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.tibco.fabric.server;

import java.io.Serializable;
import java.util.logging.Logger;

import com.datasynapse.fabric.admin.AdminManager;
import com.datasynapse.fabric.admin.ComponentAdmin;
import com.datasynapse.fabric.admin.StackAdmin;
import com.datasynapse.fabric.admin.info.ComponentAllocationEntryInfo;
import com.datasynapse.fabric.broker.userartifact.condition.AbstractCustomRuleCondition;

/**
 * The AllocationFailureCustomCondition will monitor for a named Component being unable to activate.
 * If that occurs the condition will become true, which will enable a second Component with the intention
 * that it would be able to resolve whatever problem is stopping the first Component from starting.  For example
 * a Tomcat Component may require a specific operating system configuration to run.  If there are no Hosts suitably
 * configured a Puppet Component (the remediatingComponent) could be started which can apply an appropriate configuration to the OS.
 * After that, the Puppet Component could be immediately deactivated (after waiting for the waitTime) since its job is done 
 * and this is the typical expected usage.  
 * 
 * However there are circumstances where it could be desirable to wait until the first Component has started up
 * before deactivating the remediating component, or to never deactivate it and these are catered for with the
 * "wait" or "never" settings for autoDisable.
 */
public class AllocationFailureCustomCondition extends AbstractCustomRuleCondition implements Serializable {

    private static final long serialVersionUID = 2443519394918129351L;
    
    private String componentName = null;
    private String remediatingComponentName = null;
    private String autoDisable = "immediate";
    private String waitFor = "2";
    private int intWaitCount = 0;
    private int desiredRemEngineCount = -1;
    private final StackAdmin sa = AdminManager.getStackAdmin();
    private final ComponentAdmin ca = AdminManager.getComponentAdmin();
    boolean satisfied = false;

    @Override
    public String getDescription() {
        return "Component " + componentName + " is unable to activate";
    }

    @Override
    public boolean isSatisfied() {
        try {
            ComponentAllocationEntryInfo caei = sa.getComponentAllocationMap().getAllocationEntry(componentName);

            if (caei != null) {
                // Allocated Engine Count in ComponentAdmin only returns successfully started Engines
                int remEngineCount = ca.getAllocatedEngineCount(remediatingComponentName);
                // we want to have one more remediatingComponent than we had before we started i.e. we will have successfully started a new instance of the Component
                if (desiredRemEngineCount == -1){
                	desiredRemEngineCount = remEngineCount + 1;
                	Logger.getLogger(getClass().getSimpleName()).fine("Engine Count for " + remediatingComponentName + " " +remEngineCount + " DesiredEngineCount " + desiredRemEngineCount);
               }
                // Engine Count in ComponentAllocationEntryInfo includes Allocating but not started Engines
                int engineCount = caei.getEngineCount();
                int expectedEngineCount = caei.getExpectedEngineCount();
                Logger.getLogger(getClass().getSimpleName()).fine("Engine Count for " + componentName + " " + engineCount + " expectedEngineCount " + expectedEngineCount);
                if (engineCount < expectedEngineCount && !satisfied){
                    satisfied = true;
                    // when I first developed this the condition would be tested once per minute
                    // there could be issues with timing if the polling period changes
                    intWaitCount = Integer.parseInt(waitFor);
                }
                
                if (autoDisable.equalsIgnoreCase("immediate") && remEngineCount == desiredRemEngineCount){
                    // remediating component is active, so we could disable it immediately
                	// we are not going to wait for the other component to complete starting up
                    // however, disabling too soon can cause the component activated event to not be sent.
                	// if you have a hook waiting for this event, that can cause problems.
                    // so, advise waiting a small amount of time (waitCount at least 2)
                    satisfied = false;
                    Logger.getLogger(getClass().getSimpleName()).fine("Counting down to disablement of " + remediatingComponentName + " " + intWaitCount);
                    intWaitCount--;
                    if (intWaitCount > 0) {
                        satisfied = true;
                    }
                }
                else if (autoDisable.equalsIgnoreCase("wait") && engineCount == expectedEngineCount){
                    // all instances of the component we are monitoring are active
                	// so we can now disable the remediating component
                    // however, disabling too soon can cause the component activated event to not be sent.
                	// if you have a hook waiting for this event, that can cause problems.
                    // so, advise waiting a small amount (waitcount at least 2)
                    satisfied = false;
                    Logger.getLogger(getClass().getSimpleName()).fine("Counting down to disablement of " + remediatingComponentName + " " + intWaitCount);
                    intWaitCount--;
                    if (intWaitCount > 0) {
                        satisfied = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return satisfied;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getRemediatingComponentName() {
        return remediatingComponentName;
    }

    public void setRemediatingComponentName(String remediatingComponentName) {
        this.remediatingComponentName = remediatingComponentName;
    }

    public String getAutoDisable() {
        return autoDisable;
    }

    public void setAutoDisable(String autoDisable) {
    	// would like to validate here since this is a free text field
    	// but this would break the presentation of the "help" string that
    	// prepopulates the txt box in the UI when the Condition is set up 
        this.autoDisable = autoDisable;
    }
    
    public String getWaitFor() {
        return waitFor;
    }

    public void setWaitFor(String waitFor) {
        this.waitFor = waitFor;
    }

}
