/**
 * Runner.java
 * 
 * Copyright 2022 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.filteredpush.qc.bdqtestrunner;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runner, run tests on TDWG BDQ TG2 Test implementations.
 * 
 * @author mole
 *
 */
public class Runner {

	private static final Log logger = LogFactory.getLog(Runner.class);
	
	public static void main( String[] args ) {
		
		logger.debug("Starting");
        
		try {
			TestRunner testRunner = new TestRunner();
			testRunner.runTests();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
    	
    	System.out.println("Done");
    }
}
