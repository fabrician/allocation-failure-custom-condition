[fabrician.org](http://fabrician.org/)
==========================================================================
Custom Condition - Allocation Failure
==========================================================================

Introduction
--------------------------------------
A Custom Condition is a Silver Fabric add on module which can be used to determine
if a specfic condition is satisfied.  Custom Conditions may be referenced in Threshold
Activation or Enablement Condition Rules to define allocation behavior.  See the Silver
Fabric Developers Guide and the API for more details.

Use Case: Monitor and remediate a failed component allocation.   A component fails to activate, 
triggering the enablement of a monitoring component, which starts, takes corrective action, and then stops.

For example, say a component has a resource preference for an engine with a description of “good” 
and another component, set up as a monitor, has the Allocation Failure Custom Condition, checking on 
whether the component starts.  However, the daemon description is set to “bad,” which precludes the 
component from starting; therefore, the monitor should start, remediate the condition, then exit.

The first scenario is where the condition on the monitor is set to autoDisable=”immediate” and wait=”1”.  
When the stack is started, the component will not be able to start, so you’ll see the “expects 1 engine has 0” 
on the dashboard, which will enable the monitoring component, which will then start to allocate.  
Upon allocating, the autodisable timer will kick in and for a fixed period of time then the monitor will shut itself down again.

In the second scenario we can change the properties of the enablement condition to set autoDisable=”wait”.  
This time monitor will start, but won’t shut itself down until the component has started, 
which requires two engines.  To make this happen, you need to edit the description property on the 
daemon to change it from “bad” to “good” and the component should then start up and after the waitFor 
period the monitor should turn itself off.

For the third scenario w reset the daemon property back to “bad” and update the condition in the stack 
to autoDisable=”never”.  The result should be the same as in scenario 2 but this time monitor should stay running.


Installation
--------------------------------------
The required classes for a Custom Condition are packaged in a jar file, which along with the
XML descriptor should be copied to the SF_HOME/webapps/livecluster/deploy/config/ruleConditions
directory of the Silver Fabric Broker.  The Custom Condition will automatically be detected
and loaded.  Once loaded, Custom Conditions will become available in the rule editors of 
the Component Wizard and the Stack Builder.


To build the Custom Condition run the maven project

```bash
mvn package
```

The jar file and the XML deployment descriptor are packaged in AllocationFailureCustomCondition-1.0-SNAPSHOT.tar.gz
which can simply be extracted in SF_HOME/webapps/livecluster/deploy/config/ruleConditions.
