/**
 * TestRunner.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.datakurator.ffdq.annotations.ActedUpon;
import org.datakurator.ffdq.annotations.Consulted;
import org.datakurator.ffdq.annotations.Provides;
import org.datakurator.ffdq.api.DQResponse;
import org.datakurator.ffdq.api.ResultValue;
import org.datakurator.ffdq.api.result.AmendmentValue;
import org.datakurator.ffdq.api.result.ComplianceValue;
import org.datakurator.ffdq.api.result.NumericalValue;
import org.filteredpush.qc.date.DwCEventDQ;
import org.filteredpush.qc.date.DwCOtherDateDQ;
import org.filteredpush.qc.georeference.DwCGeoRefDQ;
import org.filteredpush.qc.sciname.DwCSciNameDQ;
import org.filteredpush.qc.sciname.DwCSciNameDQDefaults;

/**
 * @author mole
 *
 */
public class TestRunner {

	private static final Log logger = LogFactory.getLog(TestRunner.class);
	
	private Reader in;
	
	private FileWriter outFileWriter;
	
	private String source;
	
	private List<String> targetClasses;
	
	private List<String> targetIssueNumbers;
	
	private Map<String,Integer> encounteredTests;
	
	/**
	 * @throws IOException 
	 * 
	 */
	public TestRunner() throws IOException {
		source = "https://raw.githubusercontent.com/tdwg/bdq/master/tg2/core/TG2_test_validation_data.csv";
		URL sourceUrl = new URL(source);
		InputStreamReader inputStream = new InputStreamReader(sourceUrl.openStream());
		in = new BufferedReader(inputStream);
		init();
		
	}
	
	public TestRunner(File inputFile) throws IOException { 
		in = new FileReader(inputFile);
		source = inputFile.getName();
		init();
	}

	/**
	 * setup actions common to all constructors;
	 */
	private void init() throws IOException { 
	    outFileWriter = new FileWriter("test_run_output.txt");
	    targetClasses = new ArrayList<String>();
	    targetClasses.add("DwCGeoRefDQ");
	    targetClasses.add("DwCEventDQ");
	    targetClasses.add("DwCOtherDateDQ");
	    //  targetClasses.add("DwCSciNameDQ");  // @Parameter sourceAuthority default gbif not implemented here. 
	    targetClasses.add("DwCSciNameDQDefaults");
	    targetIssueNumbers = new ArrayList<String>();  // empty=run all tests.
	    encounteredTests = new HashMap<String,Integer>();
	}
	
	public void setOutputFile(String filename) throws IOException {
		File testOutput = new File(filename);
		if (testOutput.exists()) { 
			throw new IOException("Specified output file already exists, cannot overwrite");
		}
	    outFileWriter = new FileWriter(filename);
	}
	
	public void setListToRun(List<String> namesOfClassesToRun) { 
		targetClasses.clear();
		targetClasses.addAll(namesOfClassesToRun);
	}
	
	public void setIssuesToRun(List<String> namesOfIssueNumbersToRun) { 
		targetIssueNumbers.clear();
		targetIssueNumbers.addAll(namesOfIssueNumbersToRun);
	}
	
	public boolean runTests() {
		boolean result = false;

		@SuppressWarnings("rawtypes")
		List<Class> listToRun = new ArrayList<Class>(); 
		if (targetClasses.contains("DwCGeoRefDQ")) {
			listToRun.add(DwCGeoRefDQ.class);
		}
		if (targetClasses.contains("DwCEventDQ")) {
			listToRun.add(DwCEventDQ.class);
		}
		if (targetClasses.contains("DwCOtherDateDQ")) {
			listToRun.add(DwCOtherDateDQ.class);
		}
		if (targetClasses.contains("DwCSciNameDQDefaults")) {
			listToRun.add(DwCSciNameDQDefaults.class);
		} else if (targetClasses.contains("DwCSciNameDQ")) {
			listToRun.add(DwCSciNameDQ.class);
		} 
		
		try {

			outFileWriter.write("Validation Test Data From: " + source);
			outFileWriter.write("\n");
			outFileWriter.write(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			outFileWriter.write("\n");
			outFileWriter.write("Validating Test Implementations In:");
			outFileWriter.write("\n");
			Iterator<Class> i = listToRun.iterator();
			while (i.hasNext()) { 
				outFileWriter.write(i.next().getName());
				outFileWriter.write("\n");
			}
			CSVParser records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
			Map<String,Integer> header = records.getHeaderMap();
			for (CSVRecord record : records) {
				String GUID = record.get("GUID");
				String lineNumber = record.get("LineNumber");
				String dataID = record.get("dataID");
				String lineForTest = record.get("LineForTest");
				String gitHubIssueNo = record.get("GitHubIssueNo");
				String label = record.get("Label");
				String expectedStatus = record.get("Response.status");
				String expectedResult = record.get("Response.result");

				boolean runMe = true;
				if (targetIssueNumbers.size()>0) {
					if (!targetIssueNumbers.contains(gitHubIssueNo)) { 
						runMe = false;
						logger.debug("Skipping #" + gitHubIssueNo +" not in list of target issue numbers");
					}
				}
				if (runMe) { 
					for (Class cls : listToRun) { 
						Object instance = cls.getDeclaredConstructor().newInstance();
						for (Method javaMethod : cls.getMethods()) {
							for (Annotation annotation : javaMethod.getAnnotations()) {
								if (annotation instanceof Provides) {
									String foundGuid = ((Provides) annotation).value();
									if (foundGuid.equals(GUID) || "urn:uuid:".concat(GUID).equals(foundGuid)) {
										logger.debug("Found implementation for: " + GUID);
										// count how many times this test has been run
										if (!encounteredTests.containsKey(GUID)) { 
											encounteredTests.put(GUID, 0);
										}
										encounteredTests.put(GUID, encounteredTests.get(GUID)+1);
										
										List<String> paramValues = new ArrayList<String>();
										for (Parameter parameter : javaMethod.getParameters()) {

											for (Annotation parAnnotation : parameter.getAnnotations()) {
												String parValue = null;
												try { 
													if (parAnnotation instanceof ActedUpon) {
														logger.debug(parAnnotation.toString());
														parValue = record.get( ((ActedUpon)parAnnotation).value() );
														logger.debug(parValue);
														if (parValue.equals("[non-printing characters]")) { 
															parValue=new String(Character.toChars(Character.CONTROL));
														}
														paramValues.add(parValue);
													} else if (parAnnotation instanceof Consulted) {
														logger.debug(parAnnotation.toString());
														parValue = record.get( ((Consulted)parAnnotation).value() );
														logger.debug(parValue);
														if (parValue.equals("[non-printing characters]")) { 
															parValue=new String(Character.toChars(Character.CONTROL));
														}
														paramValues.add(parValue);
													} else if (parAnnotation instanceof org.datakurator.ffdq.annotations.Parameter) { 
														// TODO: Handle parameters
													}
												} catch (IllegalArgumentException ex) { 
													logger.error(ex.getMessage(),ex);
												}
											}
										}
										try {
											String resultStatus = "";
											String resultValue = "";
											String resultComment = "";
											boolean doComparison = false;
											if (label.startsWith("VALIDATION_")) { 
												DQResponse<ComplianceValue> retval = null;
												logger.debug(paramValues.size());
												if (paramValues.size()==1) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0));
												} else if (paramValues.size()==2) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
												} else if (paramValues.size()==3) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2));
												} else if (paramValues.size()==4) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2), paramValues.get(3));
												} else if (paramValues.size()==5) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
															paramValues.get(0), 
															paramValues.get(1), 
															paramValues.get(2), 
															paramValues.get(3), 
															paramValues.get(4));
												} else if (paramValues.size()==6) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
															paramValues.get(0), 
															paramValues.get(1), 
															paramValues.get(2), 
															paramValues.get(3), 
															paramValues.get(4), 
															paramValues.get(5));													
												} else if (paramValues.size()==19) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
															paramValues.get(0), 
															paramValues.get(1), 
															paramValues.get(2), 
															paramValues.get(3), 
															paramValues.get(4), 
															paramValues.get(5), 
															paramValues.get(6), 
															paramValues.get(7), 
															paramValues.get(8), 
															paramValues.get(9), 
															paramValues.get(10), 
															paramValues.get(11), 
															paramValues.get(12), 
															paramValues.get(13), 
															paramValues.get(14), 
															paramValues.get(15), 
															paramValues.get(16), 
															paramValues.get(17), 
															paramValues.get(18));
												} else if (paramValues.size()==20) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
															paramValues.get(0), 
															paramValues.get(1), 
															paramValues.get(2), 
															paramValues.get(3), 
															paramValues.get(4), 
															paramValues.get(5), 
															paramValues.get(6), 
															paramValues.get(7), 
															paramValues.get(8), 
															paramValues.get(9), 
															paramValues.get(10), 
															paramValues.get(11), 
															paramValues.get(12), 
															paramValues.get(13), 
															paramValues.get(14), 
															paramValues.get(15), 
															paramValues.get(16), 
															paramValues.get(17), 
															paramValues.get(18), 
															paramValues.get(19));
												} else if (paramValues.size()==23) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
															paramValues.get(0), 
															paramValues.get(1), 
															paramValues.get(2), 
															paramValues.get(3), 
															paramValues.get(4), 
															paramValues.get(5), 
															paramValues.get(6), 
															paramValues.get(7), 
															paramValues.get(8), 
															paramValues.get(9), 
															paramValues.get(10), 
															paramValues.get(11), 
															paramValues.get(12), 
															paramValues.get(13), 
															paramValues.get(14), 
															paramValues.get(15), 
															paramValues.get(16), 
															paramValues.get(17), 
															paramValues.get(18), 
															paramValues.get(19), 
															paramValues.get(20), 
															paramValues.get(21), 
															paramValues.get(22));
												} else if (paramValues.size()==24) { 
													retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
															paramValues.get(0), 
															paramValues.get(1), 
															paramValues.get(2), 
															paramValues.get(3), 
															paramValues.get(4), 
															paramValues.get(5), 
															paramValues.get(6), 
															paramValues.get(7), 
															paramValues.get(8), 
															paramValues.get(9), 
															paramValues.get(10), 
															paramValues.get(11), 
															paramValues.get(12), 
															paramValues.get(13), 
															paramValues.get(14), 
															paramValues.get(15), 
															paramValues.get(16), 
															paramValues.get(17), 
															paramValues.get(18), 
															paramValues.get(19), 
															paramValues.get(20), 
															paramValues.get(21), 
															paramValues.get(22), 
															paramValues.get(23));
												}
												if (retval!=null) { 
													logger.debug(retval.getResultState().getLabel());
													resultStatus = retval.getResultState().getLabel();
													if (retval.getValue()!=null) { 
														resultValue = retval.getValue().getLabel();
													} else { 
														resultValue = "";
													}
													resultComment = retval.getComment();
													doComparison = true;
												}
											} else if (label.startsWith("AMENDMENT_")) { 
												try { 
													DQResponse<AmendmentValue> retval = null;
													if (paramValues.size()==1) { 
														retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0));
													} else if (paramValues.size()==2) { 
														retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
													} else if (paramValues.size()==3) { 
														retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2));
													} else if (paramValues.size()==4) { 
														retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2), paramValues.get(3));
													}
													if (retval!=null) { 
														resultStatus = retval.getResultState().getLabel();
														if (retval.getValue()!=null) { 
															Map<String,String> obj = retval.getValue().getObject();
															StringBuilder strretval = new StringBuilder("");
															if (obj.size() > 0) { 
																strretval.append("{");
																String separator = "";
																for (Map.Entry<String, String> entry : obj.entrySet()) {
																	String key = entry.getKey();
																	String value = entry.getValue();
																	strretval.append("\"").append(key).append("\":\"").append(value).append("\"").append(separator);
																	separator=",";
																}
																strretval.append("}");
															}
															resultValue = strretval.toString();
															logger.debug(resultValue);
														} else {
															resultValue = "";
														}
														resultComment = retval.getComment();
														doComparison = true;
													}
												} catch (IndexOutOfBoundsException e) { 
													logger.debug(e.getMessage());
													// TODO: Error in parameter binding? 
												}
											} else if (label.startsWith("MEASURE_")) { 
												// TODO: Handle CompletenessValue and NumericalValue
												DQResponse<ResultValue> retval = null;
												if (paramValues.size()==1) { 
													retval = (DQResponse<ResultValue>)javaMethod.invoke(instance, paramValues.get(0));
												} else if (paramValues.size()==2) { 
													retval = (DQResponse<ResultValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
												}
												if (retval!=null) { 	
													resultStatus = retval.getResultState().getLabel();
													if (retval.getValue()==null) { 
														resultValue = "";
													} else { 
														if (retval.getValue().getClass().equals(NumericalValue.class)) {
															resultValue = retval.getValue().getObject().toString();
														} else {  
															resultValue = retval.getValue().toString();  // different between completenessvalue and numericalvalue
														}
													}
													resultComment = retval.getComment();
													doComparison = true;
												} 
											}
											if (doComparison) { 
												// TODO: Handle different order of terms in amendment results.
												if (expectedStatus.equals(resultStatus) && (
														expectedStatus.equals("INTERNAL_PREREQUISITES_NOT_MET") || 
														expectedStatus.equals("EXTERNAL_PREREQUISITES_NOT_MET") || 
														expectedResult.equals(resultValue)) 
														) {
													StringBuilder message = new StringBuilder()
															.append(dataID)
															.append(" #").append(gitHubIssueNo)
															.append(" Pass");
													logger.debug(message);
													outFileWriter.write(message.toString());
													outFileWriter.write("\n");
												} else { 
													StringBuilder message = new StringBuilder()
															.append(dataID)
															.append(" #").append(gitHubIssueNo)
															.append(" Fail got ");
													if (!resultStatus.equals(expectedStatus)) { 
														message.append(resultStatus).append(" expected ").append(expectedStatus);
													} else { 
														message.append(resultValue).append(" expected ").append(expectedResult);
														message.append(" ").append(resultComment);
													}
													logger.debug(resultStatus);
													logger.debug(resultValue);
													logger.debug(resultComment);
													logger.debug(message);
													outFileWriter.write(message.toString());
													outFileWriter.write("\n");
												}
											} else { 
												StringBuilder message = new StringBuilder()
														.append(dataID)
														.append(" #").append(gitHubIssueNo)
														.append(" Skipped ");
												logger.debug(message);
											}
										} catch (IllegalAccessException | IllegalArgumentException
												| InvocationTargetException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
			Set<String> encKeys = encounteredTests.keySet();
			outFileWriter.write("Ran " + Integer.toString(encounteredTests.size()) + " tests against the validation data.\n" );
			Iterator<String> ik = encKeys.iterator();
			while (ik.hasNext()) { 
				String key = ik.next();
				outFileWriter.write(key + " " + Integer.toString(encounteredTests.get(key)) + "\n");
			}

		} catch (FileNotFoundException e) {
			logger.debug(e.getMessage(), e);
		} catch (IOException e) {
			logger.debug(e.getMessage(), e);
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			logger.error(e1.getMessage(),e1);
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		try {
			outFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
}
