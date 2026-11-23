/**
 * ResponseComparisonTest.java
 *
 * Copyright 2026 President and Fellows of Harvard College
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests of how the runner compares a Test's Response with the expected Response.
 *
 * @author mole
 */
public class ResponseComparisonTest {

	@Test
	public void testSameStatusAndResult() {
		assertTrue(TestRunner.responseMatches("RUN_HAS_RESULT", "COMPLIANT", "RUN_HAS_RESULT", "COMPLIANT"));
		assertTrue(TestRunner.responseMatches("RUN_HAS_RESULT", "259200", "RUN_HAS_RESULT", "259200"));
	}

	@Test
	public void testDifferentResult() {
		assertFalse(TestRunner.responseMatches("RUN_HAS_RESULT", "COMPLIANT", "RUN_HAS_RESULT", "NOT_COMPLIANT"));
	}

	@Test
	public void testDifferentStatus() {
		assertFalse(TestRunner.responseMatches("RUN_HAS_RESULT", "", "INTERNAL_PREREQUISITES_NOT_MET", ""));
	}

	@Test
	public void testPrerequisitesNotMetWithNoResult() {
		assertTrue(TestRunner.responseMatches("INTERNAL_PREREQUISITES_NOT_MET", "", "INTERNAL_PREREQUISITES_NOT_MET", ""));
		assertTrue(TestRunner.responseMatches("EXTERNAL_PREREQUISITES_NOT_MET", "", "EXTERNAL_PREREQUISITES_NOT_MET", ""));
	}

	@Test
	public void testPrerequisitesNotMetWithAResult() {
		// A Test that returns a value where its prerequisites are not met fails, e.g. a Measure returning NOT_REPORTED.
		assertFalse(TestRunner.responseMatches("INTERNAL_PREREQUISITES_NOT_MET", "", "INTERNAL_PREREQUISITES_NOT_MET", "NOT_REPORTED"));
		assertFalse(TestRunner.responseMatches("EXTERNAL_PREREQUISITES_NOT_MET", "", "EXTERNAL_PREREQUISITES_NOT_MET", "0"));
	}
}
