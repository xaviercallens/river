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

// java.rmi
import java.rmi.RemoteException;

// net.jini
import net.jini.security.TrustVerifier;


/**
 * Class implementing ProxyTrust and RemoteMethodControl interfaces whose
 * 'getProxyVerifier' method always throws SecurityException.
 */
public class ProxyTrustThrowingSE1 extends BaseProxyTrust {

    /**
     * Always throws SecurityException.
     */
    public TrustVerifier getProxyVerifier() throws RemoteException {
        throw new SecurityException();
    }
}
