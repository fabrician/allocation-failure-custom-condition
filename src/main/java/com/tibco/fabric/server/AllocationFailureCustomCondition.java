package com.tibco.fabric.server;

import java.io.Serializable;
import java.util.logging.Logger;

import com.datasynapse.fabric.admin.AdminManager;
import com.datasynapse.fabric.admin.ComponentAdmin;
import com.datasynapse.fabric.admin.StackAdmin;
import com.datasynapse.fabric.admin.info.ComponentAllocationEntryInfo;
import com.datasynapse.fabric.broker.userartifact.condition.AbstractCustomRuleCondition;

public class AllocationFailureCustomCondition extends AbstractCustomRuleCondition implements Serializable {

	private static final long serialVersionUID = 2443519394918129351L;
	
	private String componentName = null;
	private String remediatingComponentName = null;
	private String autoDisable = "false";
	private String waitFor = "2";
	private int intWaitCount = 0;
	private final StackAdmin sa = AdminManager.getStackAdmin();
	private final ComponentAdmin ca = AdminManager.getComponentAdmin();
	private String description = null;
	private String lastModifiedBy = null;

	@Override
	public String getDescription() {
		return "Component " + componentName + " is unable to activate";
	}

	@Override
	public boolean isSatisfied() {
		boolean satisfied = false;
		try {
			ComponentAllocationEntryInfo caei = sa.getComponentAllocationMap().getAllocationEntry(componentName);

			if (caei != null) {
				// Allocated Engine Count in ComponentAdmin only returns successfully started Engines
				int remEngineCountCA = ca.getAllocatedEngineCount(remediatingComponentName);
				// Engine Count in ComponentAllocationEntryInfo includes Allocating but not started Engines
				int engineCount = caei.getEngineCount();
				int expectedEngineCount = caei.getExpectedEngineCount();
                Logger.getLogger(getClass().getSimpleName()).fine("Engine Count " + engineCount + " expectedEngineCount " + expectedEngineCount);
				if (engineCount < expectedEngineCount){
					satisfied = true;
					intWaitCount = Integer.parseInt(waitFor);
				}
				if (autoDisable.equalsIgnoreCase("true") && remEngineCountCA > 0){
					//component is active, so we could disable it immediately
					// however, disabling too soon can cause the component activated event to not be sent.
					// so, advise waiting a small amount (waitcount at least 2)
					satisfied = false;
					Logger.getLogger(getClass().getSimpleName()).fine("Counting down to disablement of " + componentName + " " + intWaitCount);
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
		this.autoDisable = autoDisable;
	}
	
	public String getWaitFor() {
		return waitFor;
	}

	public void setWaitFor(String waitFor) {
		this.waitFor = waitFor;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

}
