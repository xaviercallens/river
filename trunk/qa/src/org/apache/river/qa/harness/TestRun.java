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
package org.apache.river.qa.harness;

/**
 * A struct to hold a <TestDescription, configTag> pair.
 * This pair defines a single test run.
 */
public class TestRun {

    /** the test description */
    public TestDescription td;
    
    /** the test configuration */
    public String configTag;

    /** the test run represents a rerun */
    public boolean isRerun = false;

    public TestRun(TestDescription td, String configTag) {
        this.td = td;
        this.configTag = configTag;
    }
}

