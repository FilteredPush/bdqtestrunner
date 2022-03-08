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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.filteredpush.qc.date.DwCEventDQ;
import org.filteredpush.qc.date.DwCOtherDateDQ;
import org.filteredpush.qc.georeference.DwCGeoRefDQ;
import org.filteredpush.qc.sciname.DwCSciNameDQ;

/**
 * @author mole
 *
 */
public class TestRunner {

	private static final Log logger = LogFactory.getLog(TestRunner.class);
	
	private Reader in;
	
	private FileWriter outFileWriter;
	
	
	
	/**
	 * @throws IOException 
	 * 
	 */
	public TestRunner() throws IOException {
		URL sourceUrl = new URL("https://raw.githubusercontent.com/tdwg/bdq/master/tg2/core/TG2_test_validation_data.csv");
		InputStreamReader inputStream = new InputStreamReader(sourceUrl.openStream());
		in = new BufferedReader(inputStream);
		
	    outFileWriter = new FileWriter("test_run_output.txt");
		
	}
	
	public TestRunner(File inputFile) throws IOException { 
		in = new FileReader(inputFile);
	    outFileWriter = new FileWriter("test_run_output.txt");
	}

	public boolean runTests() {
		boolean result = false;

		@SuppressWarnings("rawtypes")
		List<Class> listToRun = new ArrayList<Class>(); 
		listToRun.add(DwCGeoRefDQ.class);
		listToRun.add(DwCEventDQ.class);
		listToRun.add(DwCOtherDateDQ.class);
		listToRun.add(DwCSciNameDQ.class);
		
		try {

			CSVParser records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
			Map<String,Integer> header = records.getHeaderMap();
			int line = 2; // first line in spreadsheet, header is 1.
			int errors = 0;
			for (CSVRecord record : records) {
				String GUID = record.get("GUID");
				String lineNumber = record.get("LineNumber");
				String lineForTest = record.get("LineForTest");
				String label = record.get("Label");
				String expectedStatus = record.get("Response.Status");
				String expectedResult = record.get("Response.Result");
				
				for (Class cls : listToRun) { 
				//Class cls = DwCOtherDateDQ.class;
				Object instance = cls.getDeclaredConstructor().newInstance();
				for (Method javaMethod : cls.getMethods()) {
					for (Annotation annotation : javaMethod.getAnnotations()) {
						if (annotation instanceof Provides) {
							String foundGuid = ((Provides) annotation).value();
							if (foundGuid.equals(GUID) || "urn:uuid:".concat(GUID).equals(foundGuid)) {
								logger.debug("Found implementation for: " + GUID);
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
										if (paramValues.size()==1) { 
											retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0));
										} else if (paramValues.size()==2) { 
											retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
										} else if (paramValues.size()==3) { 
											retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2));
										} else if (paramValues.size()==4) { 
											retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2), paramValues.get(2));
										}
										if (retval!=null) { 
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
												retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2), paramValues.get(2));
											}
											if (retval!=null) { 
												resultStatus = retval.getResultState().getLabel();
												if (retval.getValue()!=null) { 
													resultValue = retval.getValue().toString();
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
												resultValue = retval.getValue().toString();  // different between completenessvalue and numericalvalue
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
											StringBuilder message = new StringBuilder().append(lineNumber).append(" Pass");
											logger.debug(message);
											outFileWriter.write(message.toString());
											outFileWriter.write("\n");
										} else { 
											StringBuilder message = new StringBuilder().append(lineNumber).append(" Fail got ");
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
										StringBuilder message = new StringBuilder().append(lineNumber).append(" Skipped ");
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
