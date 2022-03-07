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
import org.datakurator.ffdq.api.result.ComplianceValue;
import org.filteredpush.qc.date.DwCOtherDateDQ;

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
				
				Class cls = DwCOtherDateDQ.class;
				Object instance = cls.getDeclaredConstructor().newInstance();
				for (Method javaMethod : cls.getMethods()) {
					for (Annotation annotation : javaMethod.getAnnotations()) {
		                if (annotation instanceof Provides) {
		                	String foundGuid = ((Provides) annotation).value();
		                	if (foundGuid.equals(GUID) || "urn:uuid:".concat(foundGuid).equals(GUID)) {
		                		logger.debug("Found implementation for: " + GUID);
		                		List<String> paramValues = new ArrayList<String>();
		                        for (Parameter parameter : javaMethod.getParameters()) {
		                        	
		                            for (Annotation parAnnotation : parameter.getAnnotations()) {
		                            	String parValue = null;
		                                if (parAnnotation instanceof ActedUpon) {
		                                	logger.debug(parAnnotation.toString());
		                                	parValue = record.get( ((ActedUpon)parAnnotation).value() );
		                                	logger.debug(parValue);
		                                	paramValues.add(parValue);
		                                } else if (parAnnotation instanceof Consulted) {
		                                	logger.debug(parAnnotation.toString());
		                                	parValue = record.get( ((Consulted)parAnnotation).value() );
		                                	logger.debug(parValue);
		                                	paramValues.add(parValue);
		                                }
		                            }
		                        }
		                        try {
		                        	DQResponse<ComplianceValue> retval = 
		                        			(DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
		                        	logger.debug(retval.getResultState().getLabel());
		                        	logger.debug(retval.getValue());
		                        	logger.debug(retval.getComment());
		                        	if (expectedStatus.equals(retval.getResultState().getLabel()) && (
		                        			expectedStatus.equals("INTERNAL_PREREQUISITES_NOT_MET") || 
		                        			expectedStatus.equals("EXTERNAL_PREREQUISITES_NOT_MET") || 
		                        			expectedResult.equals(retval.getValue().getLabel())) 
		                        	) {
		                        		StringBuilder message = new StringBuilder().append(lineNumber).append(" Pass");
		                        		logger.debug(message);
		                        		outFileWriter.write(message.toString());
		                        		outFileWriter.write("\n");
		                        	} else { 
		                        		StringBuilder message = new StringBuilder().append(lineNumber).append(" Fail got ").append(retval.getResultState().getLabel()).append(" expected ").append(expectedStatus );
		                        		logger.debug(retval.getResultState().getLabel());
		                        		logger.debug(retval.getValue());
		                        		logger.debug(retval.getComment());
		                        		logger.debug(message);
		                        		outFileWriter.write(message.toString());
		                        		outFileWriter.write("\n");
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
			outFileWriter.close();
			
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		return result;
	}
	
}
