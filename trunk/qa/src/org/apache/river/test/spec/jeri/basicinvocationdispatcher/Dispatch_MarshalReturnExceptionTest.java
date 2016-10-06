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
package org.apache.river.test.spec.jeri.basicinvocationdispatcher;

import java.util.logging.Level;


import net.jini.io.MarshalInputStream;

import org.apache.river.test.spec.jeri.util.FakeInboundRequest;
import org.apache.river.test.spec.jeri.util.FakeArgument;

import java.util.logging.Level;
import java.util.ArrayList;
import java.lang.reflect.UndeclaredThrowableException;
import java.io.IOException;
import java.rmi.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.MarshalException;
import java.rmi.UnmarshalException;
import java.rmi.ServerException;
import java.rmi.ConnectIOException;

/**
 * <pre>
 * Purpose
 *   This test verifies the behavior of the BasicInvocationDispatcher.dispatch
 *   method when marshalReturn throws an exception.
 *
 * Test Cases
 *   This test iterates over a set of exceptions.  Each exception
 *   denotes one test case and is defined by the variable:
 *      Throwable marshalReturnException
 *
 * Infrastructure
 *   This test requires the following infrastructure:
 *     1) FakeRemoteImpl
 *          -implements Remote
 *          -a class which declares one method that takes one argument,
 *           throws RemoteException, and returns an Object
 *     2) FakeInboundRequest
 *          -constructor takes four parameters (first 2 bytes, method hash,
 *           and arguments) and writes them to request stream
 *          -abort method does nothing
 *          -checkConstraints does nothing
 *          -checkPermissions does nothing
 *          -populateContext does nothing
 *          -getClientHost returns local host name
 *          -getRequestInputStream and getResponseOutputStream methods
 *           return streams created in constructor
 *     3) FakeServerCapabilities
 *          -checkConstraints method returns InvocationConstraints.EMPTY
 *     4) FakeBasicInvocationDispatcher
 *          -subclasses BasicInvocationDispatcher
 *          -overloaded marshalReturn method can throw a configurable exception
 *     5) FakeArgument
 *          -extends Exception
 *          -if constructed with a write object exception (IOException,
 *           RuntimeException, or Error) then throw
 *           that exception in the custom writeObject method
 *
 * Actions
 *   For each test case the test performs the following steps:
 *     1) construct a FakeRemoteImpl and FakeServerCapabilities
 *     2) construct a FakeBasicInvocationDispatcher, passing in
 *        marshalReturnException
 *     3) construct a FakeInboundRequest, passing in method hash of
 *        FakeRemoteImpl's method
 *     4) construct a FakeArgument, passing in a readObject IOException
 *        (will be thrown if marshalReturnException is null)
 *     5) call BasicInvocationDispatcher.dispatch with FakeRemoteImpl,
 *        FakeInboundRequest, and an empty context
 *     6) assert FakeInboundRequest response stream returns the byte 0x01
 *        and closes the stream
 * </pre>
 */
public class Dispatch_MarshalReturnExceptionTest 
     extends AbstractDispatcherTest 
{

    // test cases
    Throwable[] cases = {
        new IOException(),
        new java.net.UnknownHostException(),    //IOException subclass
        new java.net.ConnectException(),        //IOException subclass
        new RemoteException(),                  //IOException subclass
        new java.rmi.UnknownHostException(""),  //RemoteException subclass
        new java.rmi.ConnectException(""),      //RemoteException subclass
        new MarshalException(""),               //RemoteException subclass
        new UnmarshalException(""),             //RemoteException subclass
        new ConnectIOException(""),             //RemoteException subclass
        new SecurityException(),                //RuntimeException subclass
        new ArrayIndexOutOfBoundsException(),   //RuntimeException subclass
        new UndeclaredThrowableException(null), //RuntimeException subclass
        new NullPointerException(),             //RuntimeException subclass
        new LinkageError(),                     //Error subclass
        new AssertionError(),                   //Error subclass
    };

    // inherit javadoc
    public void run() throws Exception {
        logger.log(Level.FINE,"=================================");
        logger.log(Level.FINE,"test case " + (counter++)
            + ": IOException thrown from FakeArgument.writeObject()");
        logger.log(Level.FINE,"");

        // initialize FakeRemoteImpl
        impl.setFakeMethodReturn(
            new FakeArgument(new IOException(),null));

        // initialize FakeInboundRequest
        request = new FakeInboundRequest(methodHash,nullArgs,0x00,0x00);

        // call dispatch and verify the proper result
        dispatcher.dispatch(impl,request,context);
        response = request.getResponseStream();
        assertion(response.read() == 0x01);
        MarshalInputStream mis = new MarshalInputStream(
            response,null,false,null,new ArrayList());
        assertion(mis.read() == -1);

        // iterate over test cases
        for (int i = 0; i < cases.length; i++) {
            logger.log(Level.FINE,"=================================");
            Throwable marshalReturnException = cases[i];
            logger.log(Level.FINE,"test case " + (counter++)
                + ": marshalReturn exception: " + marshalReturnException);
            logger.log(Level.FINE,"");

            // initialize FakeBasicInvocationDispatcher
            dispatcher.setMarshalReturnException(
                marshalReturnException);

            // initialize FakeInboundRequest
            request = new FakeInboundRequest(methodHash,nullArgs,0x00,0x00);

            // call dispatch and verify the proper result
            dispatcher.dispatch(impl,request,context);
            response = request.getResponseStream();
            assertion(response.read() == 0x01);
            mis = new MarshalInputStream(
                response,null,false,null,new ArrayList());
            assertion(mis.read() == -1);
        }
    }

}

