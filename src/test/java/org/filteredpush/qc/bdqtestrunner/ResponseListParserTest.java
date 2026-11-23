/**
 * ResponseListParserTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.datakurator.ffdq.api.DQResponse;
import org.datakurator.ffdq.api.result.AmendmentValue;
import org.datakurator.ffdq.api.result.ComplianceValue;
import org.datakurator.ffdq.model.ResultState;
import org.junit.Test;

public class ResponseListParserTest {

	@Test
	public void testEmpty() {
		assertTrue(ResponseListParser.parse("").isEmpty());
		assertTrue(ResponseListParser.parse(null).isEmpty());
		assertTrue(ResponseListParser.parse("  ").isEmpty());
	}

	@Test
	public void testValidations() {
		List<DQResponse<?>> r = ResponseListParser.parse(
				"0493bcfb-652e-4d17-815b-b0cce0742fbe: Response.status=RUN_HAS_RESULT, Response.result=\"COMPLIANT\""
				+ " | 4f2bf8fd-fc5c-493f-a44c-e7b16153c803: Response.status=RUN_HAS_RESULT, Response.result=\"NOT_COMPLIANT\""
				+ " | ad0c8855-de69-4843-a80c-a5387d20fbc8: Response.status=INTERNAL_PREREQUISITES_NOT_MET, Response.result=\"\"");
		assertEquals(3, r.size());
		assertEquals(ResultState.RUN_HAS_RESULT, r.get(0).getResultState());
		assertEquals(ComplianceValue.COMPLIANT.getLabel(), ((ComplianceValue) r.get(0).getValue()).getLabel());
		assertEquals(ComplianceValue.NOT_COMPLIANT.getLabel(), ((ComplianceValue) r.get(1).getValue()).getLabel());
		assertEquals(ResultState.INTERNAL_PREREQUISITES_NOT_MET, r.get(2).getResultState());
		assertNull(r.get(2).getValue());
		assertEquals("0493bcfb-652e-4d17-815b-b0cce0742fbe", ((ResponseListParser.SimulatedResponse<?>) r.get(0)).getTestGuid());
	}

	@Test
	public void testAmendment() {
		List<DQResponse<?>> r = ResponseListParser.parse(
				"6d0a0c10-5e4a-4759-b448-88932f399812: Response.status=FILLED_IN, Response.result=\"{\"dwc:eventDate\":\"2025-03-12\"}\""
				+ " | 07c28ace-561a-476e-a9b9-3d5ad6e35933: Response.status=NOT_AMENDED, Response.result=\"\"");
		assertEquals(2, r.size());
		assertEquals(ResultState.FILLED_IN, r.get(0).getResultState());
		Map<String, String> changes = ((AmendmentValue) r.get(0).getValue()).getObject();
		assertEquals("2025-03-12", changes.get("dwc:eventDate"));
		assertEquals(ResultState.NOT_AMENDED, r.get(1).getResultState());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMalformed() {
		ResponseListParser.parse("not a response");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRepeatedTest() {
		ResponseListParser.parse(
				"0493bcfb-652e-4d17-815b-b0cce0742fbe: Response.status=RUN_HAS_RESULT, Response.result=\"COMPLIANT\""
				+ " | 0493bcfb-652e-4d17-815b-b0cce0742fbe: Response.status=RUN_HAS_RESULT, Response.result=\"NOT_COMPLIANT\"");
	}
}
