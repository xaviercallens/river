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
// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

public final class RegistryRetainCodebase_Stub
    extends java.rmi.server.RemoteStub
    implements BasicRemote, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_simpleMethod_0;
    
    static {
	try {
	    $method_simpleMethod_0 = BasicRemote.class.getMethod("simpleMethod", new java.lang.Class[] {});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public RegistryRetainCodebase_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of simpleMethod()
    public void simpleMethod()
	throws java.rmi.RemoteException
    {
	try {
	    java.security.AccessController.doPrivileged(
		new java.security.PrivilegedExceptionAction() {
		public Object run()
		    throws Exception
		{
		    ref.invoke(RegistryRetainCodebase_Stub.this, $method_simpleMethod_0, null, 4871065749645736786L);
		    return null;
		}
	    });
	    
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}