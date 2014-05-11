/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.jini.test.spec.lookupservice.test_set02;
import com.sun.jini.qa.harness.QAConfig;
import com.sun.jini.qa.harness.Test;

import java.util.logging.Level;
import com.sun.jini.qa.harness.TestException;

import com.sun.jini.test.spec.lookupservice.QATestRegistrar;
import com.sun.jini.test.spec.lookupservice.QATestUtils;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceEvent;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.entry.Entry;
import net.jini.core.lease.UnknownLeaseException;
import java.rmi.MarshalledObject;
import java.rmi.StubNotFoundException;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** This class is used to verify that after using templates containing a 
 *  combination of service types and attributes with Non-Null fields to
 *  request event notification, and upon replacement (setting) of the 
 *  attributes corresponding to the services registered with lookup, the 
 *  expected set of events will be generated by the lookup service.
 *
 *  @see com.sun.jini.qa.harness.TestEnvironment
 *  @see com.sun.jini.test.spec.lookupservice.QATestRegistrar
 *  @see com.sun.jini.test.spec.lookupservice.QATestUtils
 */
public class NotifyOnComboAttrSet extends QATestRegistrar {

    private static boolean SHOW_TIMINGS = false;

    /** Class which handles all events sent by the lookup service */
    private class Listener extends BasicListener 
                          implements RemoteEventListener
    {
        public Listener() throws RemoteException {
            super();
        }
        /** Method called remotely by lookup to handle the generated event. */
        public void notify(RemoteEvent ev) {
            ServiceEvent srvcEvnt = (ServiceEvent)ev;
            synchronized (NotifyOnComboAttrSet.this){
                evntVec.add(srvcEvnt);
                try {
                    QATestUtils.SrvcAttrTuple tuple = (QATestUtils.SrvcAttrTuple)
                                          (srvcEvnt.getRegistrationObject().get());

                    receivedTuples.add(new QATestUtils.SrvcAttrTuple
                                                       (srvcItems,tmplAttrs,
                                                        tuple.getSrvcObj(),
                                                        tuple.getAttrObj(),
                                                        srvcEvnt.getTransition()));
                } catch (Exception e) {
                    logger.log(Level.INFO, "Unexpected exception in listener", e);
                }
            }
        }
    }

    protected QATestUtils.SrvcAttrTuple[][][] oldState;
    protected QATestUtils.SrvcAttrTuple[][][] newState;
    protected List expectedTuples = new ArrayList(2850);
    protected List receivedTuples = new ArrayList(2850);
    protected List<ServiceEvent> evntVec = new ArrayList<ServiceEvent>(2850);

    private ServiceItem[] srvcItems;
    private ServiceItem[] srvcsForEquals;
    private ServiceRegistration[] srvcRegs ;
    private ServiceTemplate[][] tmpl;
    private ServiceTemplate[][] sTmpl;
    private ServiceRegistrar proxy;

    private int nClasses = getNTestClasses();
    private int nAttrClasses = getNAttrClasses();
    private int nSrvcInstances;
    private int nSrvcsPerClass;

    private int nSrvcs;
    private int srvcChainLen = MAX_SRVC_CHAIN_LEN;

    private Entry[] attrEntries;
    private Entry[][] addAttrs;
    private Entry[][] tmplAttrs;

    private long maxNMsToWaitForEvents;

    /** The event handler for the services registered by this class */
    private static RemoteEventListener listener;

    /** Performs actions necessary to prepare for execution of the 
     *  current QA test.
     *
     *  Creates the lookup service. Creates the event handler. Loads each of
     *  the attribute classes and creates two sets of initialized instances of
     *  the loaded attributes; where each instance from both sets is 
     *  initialized (non-null fields) and corresponding instances are not 
     *  equal. Retrieves the proxies to the lookup service. Computes the 
     *  maximimum time to wait for all events to arrive from lookup; based 
     *  on that event wait time, computes and sets values for the maximum 
     *  duration of all service and event leases. The maximum duration 
     *  should be large enough to allow all events to arrive and be 
     *  processed before any of the service or event leases expire. Loads 
     *  and instantiates each of the service classes; then registers each 
     *  service instance with the lookup service. Creates another set of 
     *  instances of the service classes that will be used to determine 
     *  equality in the method objEquals() from the class 
     *  QATestUtils.SrvcAttrTuple. Adds each attribute instance from 
     *  the first set to each registered service. For each combination 
     *  of service and attribute, creates a template containing a combination
     *  of both the service's class type and an attribute instance from the
     *  second set of attributes. Registers an event notification request
     *  based on the contents of that template and the appropriate transition 
     *  mask; requests a handback which contains the corresponding
     *  service/attribute "tuple" to be associated with the event. 
     *  @exception QATestException will usually indicate an "unresolved"
     *  condition because at this point the test has not yet begun.
     */
    public synchronized Test construct(QAConfig sysConfig) throws Exception {
        int i,j,k;
        int transitionMask =   ServiceRegistrar.TRANSITION_MATCH_NOMATCH
                             | ServiceRegistrar.TRANSITION_NOMATCH_MATCH
	                     | ServiceRegistrar.TRANSITION_MATCH_MATCH;

        /* compute the maximum the number of milliseconds to wait for all of
         * the events to arrive from lookup
         */
        nSrvcsPerClass = sysConfig.getIntConfigVal
            ("com.sun.jini.test.spec.lookupservice.nInstancesPerClass",10);
        nSrvcInstances = TEST_SRVC_CLASSES.length * nSrvcsPerClass;
        /* based on the number of service class instances and the number of
         * attributes per service participating in this test run, get
         * the number of milliseconds to wait for all events to arrive
         * from the lookup service before attempting to verify the events.
         */
        maxNMsToWaitForEvents = QATestUtils.getMaxNMsToWaitForEvents
                                    (nSrvcInstances,nAttrClasses,SHOW_TIMINGS);
        /* based on the event wait time computed above, compute and set 
         * values for the maximum duration of all service and event leases
         */
        QATestUtils.setLeaseDuration(sysConfig,maxNMsToWaitForEvents);

        /* create the lookup service */
	super.construct(sysConfig);
        /* create the event handler */
	listener = new Listener();
        ((BasicListener) listener).export();
        /* load and create an initialized instance (non-null fields) of each
         * of the attribute classes; which will be added to each registered
         * service
         */
        attrEntries = super.createNonNullFieldAttributes();
        addAttrs = new Entry[attrEntries.length][1];
        for (i=0;i<attrEntries.length;i++) {
	    addAttrs[i][0] = attrEntries[i];
	}
        /* create another instance of each of the attribute classes, different
         * from the corresponding instances created above; which will be
         * used in the creation of the template for event notification
         */
        attrEntries = super.createModifiedAttributes(ATTR_CLASSES);
        tmplAttrs = new Entry[attrEntries.length][1];
        for (i=0;i<attrEntries.length;i++) {
	    tmplAttrs[i][0] = attrEntries[i];
	}
        /* retrieve the proxies to the lookup service */
	proxy = super.getProxy();
        /* load and instantiate each of the service classes; then register
         * each service instance with the lookup service
         */
	srvcItems = super.createServiceItems(TEST_SRVC_CLASSES);
	srvcRegs = super.registerAll();
        nSrvcs = srvcRegs.length;
        /* create another set of instances of the service classes that will
         * be used to determine equality in the method objEquals() in the
         * class QATestUtils.SrvcAttrTuple
         */
        srvcsForEquals = createSrvcsForEquals(nClasses,nSrvcs);
        /* add each of the instances from the first set of attributes to
         * each of the registered services
         */
	oldState
           = new QATestUtils.SrvcAttrTuple[nSrvcs][nAttrClasses][srvcChainLen];
        QATestUtils.initStateTupleArray(srvcItems,srvcsForEquals,
                                        addAttrs,oldState);
	newState
           = new QATestUtils.SrvcAttrTuple[nSrvcs][nAttrClasses][srvcChainLen];
        QATestUtils.initStateTupleArray(srvcItems,srvcsForEquals,
                                        tmplAttrs,newState);
        for (i=0;i<nSrvcs;i++) {
            for (j=0;j<nAttrClasses;j++) {
		srvcRegs[i].addAttributes(addAttrs[j]);
		QATestUtils.initStateTupleArray(i,j,srvcChainLen,
						srvcItems,srvcsForEquals,
						addAttrs,oldState);
	    }
	}
        /* for each combination of service and attribute, create a template
         * containing both the service's class type and the attribute instance
         * from the second set of attributes created above, then register
         * an event notification request based on the contents of that
         * template and the appropriate transition mask; request a handback
         * which contains the associated service/attribute "tuple"
         */
	tmpl = new ServiceTemplate[nClasses][nAttrClasses];
	sTmpl = new ServiceTemplate[nClasses][nAttrClasses];
        for (i=0;i<nClasses;i++) {
	    Class c = Class.forName(TEST_SRVC_CLASSES[i]);
	    Class[] classType = {c};

	    /* XXXX need to make this code more general; loop thru
	     *      the super classes populating the sClassType[] array
	     */
	    Class sc = c.getSuperclass();
	    Class[] sClassType;
	    if ( sc.equals(Object.class) ){
		sClassType = new Class[1];
		sClassType[0] = c;
	    } else {
		sClassType = new Class[srvcChainLen];
		sClassType[0] = c;
		sClassType[1] = sc;
	    }
	    /* XXXX end */

	    for (j=0;j<nAttrClasses;j++) {
		tmpl[i][j] = new ServiceTemplate(null,classType,
						 tmplAttrs[j]);
		sTmpl[i][j] = new ServiceTemplate(null,sClassType,
						  tmplAttrs[j]);
		proxy.notify(tmpl[i][j],
			     transitionMask,
			     listener,
			     new MarshalledObject
				 (new QATestUtils.SrvcAttrTuple
				     (srvcItems[nSrvcsPerClass*i].service,
				      tmplAttrs[j][0])),
			     Long.MAX_VALUE);
	    }
	}
        return this;
    }

    /** Executes the current QA test.
     *
     *  For each service instance registered in lookup
     *    1. Retrieves the expected event information corresponding to
     *       the current service and attribute instance; populates
     *       the "accumulation" vector with that information.
     *    2. Replaces (sets) all of the attributes belonging to the
     *       current service with the second set of attributes created 
     *       during construct.
     *    3. Verifies that the events expected are actually sent by the
     *       lookup service.
     *  @exception QATestException usually indicates test failure
     */
    public synchronized void run() throws Exception {
	int i=0;
	for (i=0;i<nSrvcs;i++) {
	    setStateAttrInfo(i,tmplAttrs,newState);
	    QATestUtils.setExpectedEvents(srvcItems[i].service,
					  srvcItems,srvcsForEquals,
					  tmplAttrs,nSrvcsPerClass,
					  sTmpl,oldState,newState,
					  expectedTuples);
	    srvcRegs[i].setAttributes(attrEntries);
	}
	QATestUtils.verifyEventTuples
	    (receivedTuples,expectedTuples,maxNMsToWaitForEvents,SHOW_TIMINGS, this);
	QATestUtils.verifyEventItems(evntVec);
    }

    /** Performs cleanup actions necessary to achieve a graceful exit of 
     *  the current QA test.
     *
     *  Unexports the listener and then performs any remaining standard
     *  cleanup duties.
     */
    public synchronized void tearDown() {
	unexportListener(listener, true);
	super.tearDown();
    }

    /* Convenience method so it doesn't have to be done inline */
    private void setStateAttrInfo(int srvcIndx,
                                  Entry[][] attrs,
                                  QATestUtils.SrvcAttrTuple[][][] state)
                                                        throws Exception
    {
        for(int i=0;i<state[srvcIndx].length;i++) {
            for(int j=0;j<state[srvcIndx][i].length;j++) {
                (state[srvcIndx][i][j]).setAttrs(attrs);
                (state[srvcIndx][i][j]).setAttrObj(attrs[i][0]);
	    }
	}
    }
}
