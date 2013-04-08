package com.tibco.fabric.server;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.datasynapse.commons.util.LogUtils;
import com.datasynapse.fabric.admin.AdminManager;
import com.datasynapse.fabric.admin.ComponentAdmin;
import com.datasynapse.fabric.admin.EngineDaemonAdmin;
import com.datasynapse.fabric.admin.StackAdmin;
import com.datasynapse.fabric.admin.info.AllocationInfo;
import com.datasynapse.fabric.admin.info.ComponentAllocationEntryInfo;
import com.datasynapse.fabric.admin.info.ComponentInfo;
import com.datasynapse.fabric.admin.info.FabricEngineDaemonInfo;
import com.datasynapse.fabric.admin.info.RuntimeContextVariableInfo;
import com.datasynapse.fabric.admin.info.StackInfo;
import com.datasynapse.fabric.broker.FabricServerEvent;
import com.datasynapse.fabric.broker.userartifact.condition.AbstractCustomRuleCondition;
import com.datasynapse.gridserver.admin.Property;
import com.datasynapse.gridserver.server.ServerEvent;
import com.datasynapse.gridserver.server.ServerHook;

public class AllocationFailureCustomCondition extends AbstractCustomRuleCondition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2443519394918129351L;
	private String ComponentName = null;
	private String RemediatingComponentName = null;
	private String AutoDisable = "false";
	private String WaitFor = "2";
	private int intWaitCount = 0;
	private final StackAdmin sa = AdminManager.getStackAdmin();
	private final ComponentAdmin ca = AdminManager.getComponentAdmin();
	private String description = null;
	private String lastModifiedBy = null;

	@Override
	public String getDescription() {
		return "Component " + ComponentName + " is unable to activate";
	}

	@Override
	public boolean isSatisfied() {
		boolean satisfied = false;
		try {
			ComponentAllocationEntryInfo caei = sa.getComponentAllocationMap().getAllocationEntry(ComponentName);

			if (caei != null) {
				// Allocated Engine Count in ComponentAdmin only returns successfully started Engines
				int remEngineCountCA = ca.getAllocatedEngineCount(RemediatingComponentName);
				// Engine Count in ComponentAllocationEntryInfo includes Allocating but not started Engines
				int engineCount = caei.getEngineCount();
				int expectedEngineCount = caei.getExpectedEngineCount();
				LogUtils.forObject(this).fine("Engine Count " + engineCount + " expectedEngineCount " + expectedEngineCount);
				
				if (engineCount < expectedEngineCount){
					satisfied = true;
					intWaitCount = Integer.parseInt(WaitFor);
				}
				if (AutoDisable.equalsIgnoreCase("true") && remEngineCountCA > 0){
					//component is active, so we could disable it immediately
					// however, disabling too soon can cause the component activated event to not be sent.
					// so, advise waiting a small amount (waitcount at least 2)
					satisfied = false;
					LogUtils.forObject(this).fine("Counting down to disablement of " + ComponentName + " " + intWaitCount);
					intWaitCount--;
					if (intWaitCount > 0) {
						satisfied = true;
					}
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return satisfied;
	}

	public String getComponentName() {
		return ComponentName;
	}

	public void setComponentName(String ComponentName) {
		this.ComponentName = ComponentName;
	}

	public String getRemediatingComponentName() {
		return RemediatingComponentName;
	}

	public void setRemediatingComponentName(String remediatingComponentName) {
		RemediatingComponentName = remediatingComponentName;
	}

	public String getAutoDisable() {
		return AutoDisable;
	}

	public void setAutoDisable(String autoDisable) {
		AutoDisable = autoDisable;
	}
	
	public String getWaitFor() {
		return WaitFor;
	}

	public void setWaitFor(String waitFor) {
		this.WaitFor = waitFor;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

}
