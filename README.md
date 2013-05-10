[fabrician.org](http://fabrician.org/)
==========================================================================
Custom Condition - Activation Failure
==========================================================================

Introduction
--------------------------------------
A Custom Condition is a Silver Fabric add on module which can be used to determine
if a specfic condition is satisfied.  Custom Conditions may be referenced in Threshold
Activation or Enablement Condition Rules to define allocation behavior.  See the Silver
Fabric Developers Guide and the API for more details.

The Allocation Failure Custom Condition defines the condition for when an Allocation has failed.

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
