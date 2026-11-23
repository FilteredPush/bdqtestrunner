/**
 * ResponseListParser.java
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.datakurator.ffdq.api.DQResponse;
import org.datakurator.ffdq.api.ResultValue;
import org.datakurator.ffdq.api.result.AmendmentValue;
import org.datakurator.ffdq.api.result.ComplianceValue;
import org.datakurator.ffdq.api.result.IssueValue;
import org.datakurator.ffdq.model.ResultState;

/**
 * Parses the simulated Responses given in the validation data for the single-record Measures
 * that count the outcomes of other Tests run on the same record (bdqval:AllValidationTestsRunOnSingleRecord,
 * bdqval:AllAmendmentTestsRunOnSingleRecord), into the collection of responses those Measures act upon.
 *
 * Form of the value, one entry per Test, entries separated by " | ":
 * <pre>
 * {Test GUID}: Response.status={status}, Response.result="{result}"
 * </pre>
 * The result is everything between the quotes after Response.result= up to the end of the entry, so it
 * may itself contain quotes (an Amendment's JSON). An empty value is an empty collection (no Tests run).
 *
 * @author mole
 */
public class ResponseListParser {

	private static final Pattern ENTRY = Pattern.compile(
			"^\\s*([0-9a-fA-F-]{36})\\s*:\\s*Response\\.status=([A-Z_]+)\\s*(?:,\\s*Response\\.result=\"(.*)\")?\\s*$");
	private static final Pattern JSON_PAIR = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");

	private ResponseListParser() { }

	/**
	 * A simulated response, with the GUID of the Test that produced it.
	 */
	public static class SimulatedResponse<T extends ResultValue> extends DQResponse<T> {
		private final String testGuid;

		public SimulatedResponse(String testGuid) {
			super();
			this.testGuid = testGuid;
		}

		public String getTestGuid() {
			return testGuid;
		}
	}

	/**
	 * Parse a list of simulated responses.
	 *
	 * @param value the value of the bdqval:All…TestsRunOnSingleRecord column
	 * @return the responses, in the order given, empty for a null or blank value
	 * @throws IllegalArgumentException if an entry is not in the expected form, or a Test GUID is repeated
	 */
	public static List<DQResponse<?>> parse(String value) {
		List<DQResponse<?>> responses = new ArrayList<DQResponse<?>>();
		if (value == null || value.trim().isEmpty()) {
			return responses;
		}
		List<String> seen = new ArrayList<String>();
		for (String entry : value.split("\\s\\|\\s")) {
			Matcher m = ENTRY.matcher(entry);
			if (!m.matches()) {
				throw new IllegalArgumentException("Unrecognized simulated response: [" + entry + "]");
			}
			String guid = m.group(1).toLowerCase();
			if (seen.contains(guid)) {
				throw new IllegalArgumentException("Test " + guid + " given more than once; one response per distinct Test.");
			}
			seen.add(guid);
			String status = m.group(2);
			String result = m.group(3) == null ? "" : m.group(3);
			responses.add(toResponse(guid, status, result));
		}
		return responses;
	}

	private static DQResponse<?> toResponse(String guid, String status, String result) {
		SimulatedResponse<ResultValue> response = new SimulatedResponse<ResultValue>(guid);
		response.setResultState(new ResultState(status));
		if (!result.isEmpty()) {
			if ("COMPLIANT".equals(result)) {
				response.setValue(ComplianceValue.COMPLIANT);
			} else if ("NOT_COMPLIANT".equals(result)) {
				response.setValue(ComplianceValue.NOT_COMPLIANT);
			} else if ("POTENTIAL_ISSUE".equals(result)) {
				response.setValue(IssueValue.POTENTIAL_ISSUE);
			} else if ("NOT_ISSUE".equals(result)) {
				response.setValue(IssueValue.NOT_ISSUE);
			} else if (result.trim().startsWith("{")) {
				Map<String, String> changes = new LinkedHashMap<String, String>();
				Matcher pair = JSON_PAIR.matcher(result);
				while (pair.find()) {
					changes.put(pair.group(1), pair.group(2));
				}
				response.setValue(new AmendmentValue(changes));
			} else {
				throw new IllegalArgumentException("Unrecognized Response.result for " + guid + ": [" + result + "]");
			}
		}
		return response;
	}
}
