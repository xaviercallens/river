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
package org.apache.river.test.spec.security.proxytrust.util;

import java.util.logging.Level;

// java
import java.rmi.RemoteException;


/**
 * Class implementing TrustVerifier.Context interface having constructor with 1
 * parameter: Collection. This value will be returned by getCallerContext
 * method. 'isTrustedObject' method of this class always throws
 * RemoteException.
 */
public class TrustVerifierContextThrowingRE extends BaseTrustVerifierContext
        implements ThrowingRE {

    /** RemoteException which will be thrown during 'isTrustedObject' call */
    protected RemoteException re;

    /**
     * Constructor saving array of context objects.
     * Prepares RemoteException.
     */
    public TrustVerifierContextThrowingRE(Object[] objs) {
        super(objs);
        re = new RemoteException();
    }

    /**
     * Always throws RemoteException.
     */
    public boolean isTrustedObject(Object obj) throws RemoteException {
        objList.add(obj);
        srcArray.add(this);
        throw re;
    }

    /**
     * Returns exception which was thrown during 'isTrustedObject' call.
     *
     * @return exception which was thrown during 'isTrustedObject' call
     */
    public RemoteException getException() {
        return re;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return "TrustVerifierContextThrowingRE[" + super.toString() + "]";
    }
}
