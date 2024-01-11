/**
 * Report.java
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mole
 *
 * Structure for reporting test results by test, summarizing over rows of
 * validation data.
 *
 */
public class Report {

	private static final Log logger = LogFactory.getLog(Report.class);
	
	private Integer encountered;
	
	private Integer pass;
	
	private Integer fail;
	
	private String testLabel;
	
	private String testNumber;
	
	/**
	 * Construct a report instance. 
	 * 
	 * @param testLabel the label for the test to report on
	 * @param testNumber the number for the test to report on
	 */
	public Report(String testLabel, String testNumber) { 
		encountered = 0;
		pass = 0;
		fail = 0;
		this.testLabel = testLabel;
		this.testNumber = testNumber;
	}

	/**
	 * @return the encountered
	 */
	public Integer getEncountered() {
		return encountered;
	}

	/**
	 * @param encountered the encountered to set
	 */
	public void setEncountered(Integer encountered) {
		this.encountered = encountered;
	}

	/** 
	 * increment the value of encountered by 1
	 */
	public void incrementEncountered() {
		if (encountered==null) { 
			encountered = 0;
		}
		encountered = encountered + 1;
	}
	
	/**
	 * @return the pass
	 */
	public Integer getPass() {
		return pass;
	}

	/** 
	 * increment the value of pass by 1
	 * also increments encountered by 1
	 */
	public void incrementPass() {
		if (pass==null) { 
			pass = 0;
		}
		pass = pass + 1;
		incrementEncountered();
	}
	
	/**
	 * @param pass the pass to set
	 */
	public void setPass(Integer pass) {
		this.pass = pass;
	}

	/**
	 * @return the fail
	 */
	public Integer getFail() {
		return fail;
	}
	
	/** 
	 * increment the value of fail by 1
	 * also increments the value of encountered by 1
	 */
	public void incrementFail() {
		if (fail==null) { 
			fail = 0;
		}
		fail = fail + 1;
		incrementEncountered();
	}

	/**
	 * @param fail the fail to set
	 */
	public void setFail(Integer fail) {
		this.fail = fail;
	}

	/**
	 * @return the testLabel
	 */
	public String getTestLabel() {
		return testLabel;
	}

	/**
	 * @param testLabel the testLabel to set
	 */
	public void setTestLabel(String testLabel) {
		this.testLabel = testLabel;
	}

	/**
	 * @return the testNumber
	 */
	public String getTestNumber() {
		return testNumber;
	}

	/**
	 * @param testNumber the testNumber to set
	 */
	public void setTestNumber(String testNumber) {
		this.testNumber = testNumber;
	}
	
	/**
	 * @return the total number of tests reported on, pass plus fail.
	 */
	public Integer totalTests() { 
		return pass + fail;
	}
	
	public String toString() { 
		StringBuilder result = new StringBuilder();
		result.append(String.format("%2d",encountered)).append(" ");
		if (pass+fail != encountered) { 
			result.append("*");
		}
		result.append("P:").append(String.format("%2d", pass)).append(" ");
		result.append("F:").append(String.format("%2d", fail)).append(" ");
		result.append(testLabel).append(" #").append(testNumber);
		
		return result.toString();
	}

}
