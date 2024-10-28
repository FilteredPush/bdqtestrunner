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
import java.io.SequenceInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.datakurator.ffdq.api.result.IssueValue;
import org.datakurator.ffdq.api.result.NumericalValue;
import org.filteredpush.qc.date.DwCEventDQ;
import org.filteredpush.qc.date.DwCEventDQDefaults;
import org.filteredpush.qc.date.DwCOtherDateDQ;
import org.filteredpush.qc.date.DwCOtherDateDQDefaults;
import org.filteredpush.qc.date.util.DateUtils;
import org.filteredpush.qc.georeference.DwCGeoRefDQ;
import org.filteredpush.qc.georeference.DwCGeoRefDQDefaults;
import org.filteredpush.qc.metadata.DwCMetadataDQ;
import org.filteredpush.qc.metadata.DwCMetadataDQDefaults;
import org.filteredpush.qc.sciname.DwCSciNameDQ;
import org.filteredpush.qc.sciname.DwCSciNameDQDefaults;
import org.filteredpush.qc.sciname.SciNameSourceAuthority;
import org.filteredpush.qc.sciname.SciNameUtils;
import org.filteredpush.qc.sciname.SourceAuthorityException;

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
	
	private Map<String,Report> encounteredTests;
	
	/**
	 * Default constructor, references test validation data file at expected location on tdwg github.
	 * 
	 * @throws IOException 
	 * 
	 */
	public TestRunner() throws IOException {
		source = "https://raw.githubusercontent.com/tdwg/bdq/master/tg2/core/TG2_test_validation_data.csv";
		URL sourceUrl = new URL(source);
		String sourceNp = "https://raw.githubusercontent.com/tdwg/bdq/master/tg2/core/TG2_test_validation_data_nonprintingchars.csv";
		URL sourceNpUrl = new URL(sourceNp);
		SequenceInputStream streams = new SequenceInputStream(sourceUrl.openStream(), sourceNpUrl.openStream());
		InputStreamReader inputStream = new InputStreamReader(streams);
		//InputStreamReader inputStream = new InputStreamReader(sour`ceUrl.openStream());
		in = new BufferedReader(inputStream);
		init();
		
	}
	
	/**
	 * Constructor specifying an alternative file of validation data to run tests against.
	 * 
	 * @param inputFile containing test validation data 
	 * @throws IOException if unable to read inputFile
	 */
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
	    //targetClasses.add("DwCMetadataDQ");
	    targetClasses.add("DwCMetadataDQDefaults");
	    targetClasses.add("DwCGeoRefDQDefaults");
	    //targetClasses.add("DwCEventDQ");
	    targetClasses.add("DwCEventDQDefaults");
	    //targetClasses.add("DwCOtherDateDQ");
	    targetClasses.add("DwCOtherDateDQDefaults");
	    //  targetClasses.add("DwCSciNameDQ");  // @Parameter sourceAuthority default gbif not implemented here. 
	    targetClasses.add("DwCSciNameDQDefaults");
	    targetIssueNumbers = new ArrayList<String>();  // empty=run all tests.
	    encounteredTests = new HashMap<String,Report>();
	}
	
	/**
	 * Set the file into which output is to be written.
	 * 
	 * @param filename into which to write output, must not exist.
	 * @throws IOException if filename exists.
	 */
	public void setOutputFile(String filename) throws IOException {
		File testOutput = new File(filename);
		if (testOutput.exists()) { 
			throw new IOException("Specified output file already exists, cannot overwrite");
		}
	    outFileWriter = new FileWriter(filename);
	}
	
	/**
	 * Set the list of classes containing test immpelmettations to be run.
	 * 
	 * @param namesOfClassesToRun list of string class names, without paths.
	 * @throws Exception if a class is unsupported.
	 * @see getSupportedClasses()
	 */
	public void setListToRun(List<String> namesOfClassesToRun) throws Exception { 
		Iterator<String> i = namesOfClassesToRun.iterator();
		while (i.hasNext()) { 
			String putativeClass = i.next();
			if (!getSupportedClasses().contains(putativeClass)) { 
				throw new Exception("Unsupported class: " + putativeClass);
			}
		}
		targetClasses.clear();
		targetClasses.addAll(namesOfClassesToRun);
	}
	
	/**
	 * Set the list of issues to be run 
	 * 
	 * @param namesOfIssueNumbersToRun list of issue numbers to evaluate.
	 */
	public void setIssuesToRun(List<String> namesOfIssueNumbersToRun) { 
		targetIssueNumbers.clear();
		targetIssueNumbers.addAll(namesOfIssueNumbersToRun);
	}
	
	/**
	 * Obtain the set of classes that are supported by this implementation.
	 * 
	 * @return set of strings of supported class names, without paths.
	 */
	public Set<String> getSupportedClasses() { 
		Set<String> supportedClasses = new HashSet<String>();
		supportedClasses.add("DwCMetadataDQ");
		supportedClasses.add("DwCMetadataDQDefaults");
		supportedClasses.add("DwCGeoRefDQ");
		supportedClasses.add("DwCGeoRefDQDefaults");
		supportedClasses.add("DwCEventDQ");
		supportedClasses.add("DwCEventDQDefaults");
		supportedClasses.add("DwCOtherDateDQ");
		supportedClasses.add("DwCOtherDateDQDefaults");
		supportedClasses.add("DwCSciNameDQ");
		supportedClasses.add("DwCSciNameDQDefaults");
		return supportedClasses;
	}
	
	/**
	 * Run the specified tests against the validation data.
	 * 
	 * @return false
	 */
	public boolean runTests() {
		boolean result = false;

		@SuppressWarnings("rawtypes")
		List<Class> listToRun = new ArrayList<Class>(); 
		if (targetClasses.contains("DwCMetadataDQ")) {
			listToRun.add(DwCMetadataDQ.class);
		}
		if (targetClasses.contains("DwCMetadataDQDefaults")) {
			listToRun.add(DwCMetadataDQDefaults.class);
		}
		if (targetClasses.contains("DwCGeoRefDQ")) {
			listToRun.add(DwCGeoRefDQ.class);
		} else if (targetClasses.contains("DwCGeoRefDQDefaults")) {
			listToRun.add(DwCGeoRefDQDefaults.class);
		}
		if (targetClasses.contains("DwCEventDQDefaults")) {
			listToRun.add(DwCEventDQDefaults.class);
		} else if (targetClasses.contains("DwCEventDQ")) { 
			listToRun.add(DwCEventDQ.class);
		}
		if (targetClasses.contains("DwCOtherDateDQ")) {
			listToRun.add(DwCOtherDateDQ.class);
		}
		if (targetClasses.contains("DwCOtherDateDQDefaults")) {
			listToRun.add(DwCOtherDateDQDefaults.class);
		}		
		if (targetClasses.contains("DwCSciNameDQDefaults")) {
			listToRun.add(DwCSciNameDQDefaults.class);
		} else if (targetClasses.contains("DwCSciNameDQ")) {
			listToRun.add(DwCSciNameDQ.class);
		} 

		Set<String> dataIDsRun = new HashSet<String>();
		Map<String,String> dataIDsNotRun = new HashMap<String,String>();
		int dataIDCounter = 0;

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
				// get a line from the validation spreadsheet
				dataIDCounter ++;
				String GUID = record.get("GUID");
				if (GUID!=null) { 
					GUID = GUID.trim();
				}
				String lineNumber = record.get("LineNumber");
				String dataID = record.get("dataID");
				String lineForTest = record.get("LineForTest");
				String gitHubIssueNo = record.get("GitHubIssueNo");
				String label = record.get("Label");
				String expectedStatus = record.get("Response.status");
				String expectedResult = record.get("Response.result");
				StringBuilder logMessage = new StringBuilder();
				logMessage.append("Record, dataID=").append(dataID).append(" Issue: ").append(gitHubIssueNo).append(" ").append(label);
				logger.debug(logMessage);

				boolean runMe = true;
				if (targetIssueNumbers.size()>0) {
					if (!targetIssueNumbers.contains(gitHubIssueNo)) { 
						runMe = false;
						logger.debug("Skipping #" + gitHubIssueNo +" not in list of target issue numbers");
					}
				}
				if (dataIDsRun.contains(dataID)) {
					// if duplicate dataID values exist in the spreadsheet.
					runMe=false;
					logger.debug("Test already run (? duplicate dataID in input spreadsheet ?) for "  + dataID + " #" + gitHubIssueNo +  " on line number " + lineNumber );
					outFileWriter.write("Test already run (? duplicate dataID in input spreadsheet ?) for "  + dataID + " #" + gitHubIssueNo +  " on line number " + lineNumber  + "\n");
				}
				if (runMe) { 
					// find if a method exists to run the specified test
					// find the method with the largest number of matched parameters to the validation data
					Method javaMethod = findBestMethod(listToRun, GUID, record);
					logger.debug(javaMethod);
					// run the selected method
					if (javaMethod!=null) { 
						dataIDsRun = runMethod(javaMethod, GUID, label, gitHubIssueNo, dataID, record, expectedStatus, expectedResult, dataIDsRun, outFileWriter);
					}
				}
				if (runMe==true) { 
					if (! dataIDsRun.contains(dataID)) { 
						dataIDsNotRun.put(dataID, gitHubIssueNo + " " + label);
					}
				}
			}
			Set<String> encKeys = encounteredTests.keySet();
			outFileWriter.write("Ran " + Integer.toString(encounteredTests.size()) + " tests against the validation data.\n" );
			Iterator<String> ik = encKeys.iterator();
			Integer totalCount = 0;
			while (ik.hasNext()) { 
				String key = ik.next();
				//outFileWriter.write(key + " " + Integer.toString(encounteredTests.get(key).getEncountered()) + "\n");
				outFileWriter.write(key + " " + encounteredTests.get(key).toString() + "\n");
				totalCount = totalCount + encounteredTests.get(key).getEncountered();
			}
			outFileWriter.write("Test cases: " + Integer.toString(totalCount) + "\n");
			Iterator<String> inr = dataIDsNotRun.keySet().iterator();
			int headersEncountered = 0;
			while (inr.hasNext()) { 
				String notRun = inr.next();
				if (notRun.equals("dataID")) {
					headersEncountered++;
					logger.debug("No test found, probably header line: " + notRun + " " + dataIDsNotRun.get(notRun).toString() + "\n");
				} else { 
					outFileWriter.write("No test found: " + notRun + " " + dataIDsNotRun.get(notRun).toString() + "\n");
				}
			}
			outFileWriter.write("Total cases with no implementation: " + Integer.toString(dataIDsNotRun.size() - headersEncountered) + "\n");
			outFileWriter.write("Total dataID validation rows: " + Integer.toString(dataIDCounter) + "\n");
			outFileWriter.write("Header Lines Skipped: " + Integer.toString(headersEncountered) + "\n");

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
	
	/**
	 * Given a GUID for a test a list of classes that may contain an implementation, and 
	 * a data record, return the most appropriate implementation (parameterized, or using 
	 * parameter defaults) of the test to run. 
	 * If the record contains a bdq parameter (e.g. bdq:sourceAuthority) with a value, return
	 * a method with method parameter with a Provides annotation for that bdq parameter.  
	 * 
	 * @param listToRun classes that may contain one or more implementations of the test to 
	 *   be run.
	 * @param GUID of the test to be run
	 * @param record containing data to be run against the test.
	 * @return the choice of the best implementation of the test to run given the data record.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private Method findBestMethod(List<Class> listToRun, String GUID, CSVRecord record) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Method match = null;
		logger.debug("Looking For: [" + GUID + "]");
		Map<Method,Boolean> potentialMethods = new HashMap<Method,Boolean>();  // method, hasBDQParameterWithData
		for (Class cls : listToRun) { 
			logger.debug(cls.getSimpleName());
			Object instance = cls.getDeclaredConstructor().newInstance();
			for (Method javaMethod : cls.getMethods()) {
				int parametersInMethod = 0;
				int parametersInData = 0;
				boolean hasBdqParameterWithValue = false;
				for (Annotation annotation : javaMethod.getAnnotations()) {
					if (annotation instanceof Provides) {
						String foundGuid = ((Provides) annotation).value();
						logger.debug(foundGuid);
						if (foundGuid.equals(GUID) || "urn:uuid:".concat(GUID).equals(foundGuid)) {
							logger.debug("Found implementation for: " + GUID);
							logger.debug(javaMethod.getDeclaringClass());
							logger.debug(javaMethod.toGenericString());
							for (Parameter parameter : javaMethod.getParameters()) {
								for (Annotation parAnnotation : parameter.getAnnotations()) {
									if (parAnnotation instanceof ActedUpon ||
										parAnnotation instanceof Consulted ||
										parAnnotation instanceof org.datakurator.ffdq.annotations.ActedUpon ||
										parAnnotation instanceof org.datakurator.ffdq.annotations.Parameter
									) {
										parametersInMethod ++;
									} else { 
										logger.debug("Unrecognized type: " + parAnnotation.annotationType());
									}
									try { 
										String parValue;
										if (parAnnotation instanceof ActedUpon) {
											logger.debug(parAnnotation.toString());
											parValue = record.get( ((ActedUpon)parAnnotation).value() );
											parametersInData++;
											logger.debug(parValue);
										} else if (parAnnotation instanceof Consulted) {
											logger.debug(parAnnotation.toString());
											parValue = record.get( ((Consulted)parAnnotation).value() );
											parametersInData++;
										} else if (parAnnotation instanceof org.datakurator.ffdq.annotations.Parameter) { 
											logger.debug(parAnnotation.toString());
											try { 
												parValue = record.get( ((org.datakurator.ffdq.annotations.Parameter)parAnnotation).name() );
											} catch (IllegalArgumentException exe) {
												// not all parameters may be in input.  log and assume empty.
												logger.debug(exe);
												parValue = "";
											}
											parametersInData++;
											if (!SciNameUtils.isEmpty(parValue)) { 
												hasBdqParameterWithValue = true;
											}
										}
									} catch (IllegalArgumentException ex) { 
										logger.error(ex.getMessage(),ex);
									}
								}
							}
							if (parametersInMethod>0 && parametersInData==parametersInMethod) { 
								potentialMethods.put(javaMethod, hasBdqParameterWithValue);
							} else { 
								logger.debug("Parameter count missmatch");
							}
						}
					}
				}
			}
		}
		if (potentialMethods.size() > 0) { 
			// limit to method that takes a bdq: parameter, if any has a value
			Iterator<Method> im = potentialMethods.keySet().iterator();
			Method method = im.next();
			match = method;
			while (im.hasNext()) { 
				method = im.next();
				if (potentialMethods.get(method)==true) { 
					match = method;
				}
			}
		}
		logger.debug(match);
		return match;
	}
	
	/**
	 * Run a java method that implements a specified test against the validation data.
	 * 
	 * @param javaMethod
	 * @param GUID
	 * @param label
	 * @param gitHubIssueNo
	 * @param dataID
	 * @param record
	 * @param expectedStatus
	 * @param expectedResult
	 * @param dataIDsRun
	 * @param outFileWriter
	 * @return results as a set of string values
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IOException
	 */
	private Set<String> runMethod (Method javaMethod, String GUID, String label, String gitHubIssueNo, String dataID, CSVRecord record, String expectedStatus, String expectedResult, Set<String> dataIDsRun, FileWriter outFileWriter) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException { 
		Class cls = javaMethod.getDeclaringClass();
		logger.debug("Running method from: " + cls.getSimpleName());
		Object instance = cls.getDeclaredConstructor().newInstance();
		for (Annotation annotation : javaMethod.getAnnotations()) {
			if (annotation instanceof Provides) {
				String foundGuid = ((Provides) annotation).value();
				if (foundGuid.equals(GUID) || "urn:uuid:".concat(GUID).equals(foundGuid)) {
					logger.debug("Running implementation for: " + GUID);
					logger.debug(javaMethod.getDeclaringClass());
					logger.debug(javaMethod.toGenericString());
					// count how many times this test has been run
					if (!encounteredTests.containsKey(GUID)) { 
						Report testReport = new Report(label,gitHubIssueNo);
						encounteredTests.put(GUID, testReport);
					}
					// count how many parameters this method takes that also have matches in the input data
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
										parValue=new String(Character.toChars('\1'));
									}
									if (parValue.equals("[null]")) { 
										parValue=new String(Character.toChars('\0'));
									}														
									paramValues.add(parValue);
								} else if (parAnnotation instanceof Consulted) {
									logger.debug(parAnnotation.toString());
									parValue = record.get( ((Consulted)parAnnotation).value() );
									logger.debug(parValue);
									logger.debug(parValue==null);
									if (parValue.equals("[non-printing characters]")) { 
										parValue=new String(Character.toChars('\1'));
									}
									if (parValue.equals("[null]")) { 
										parValue=new String(Character.toChars('\0'));
									}														
									paramValues.add(parValue);
								} else if (parAnnotation instanceof org.datakurator.ffdq.annotations.Parameter) { 
									logger.debug(parAnnotation.toString());
									try {
										parValue = record.get( ((org.datakurator.ffdq.annotations.Parameter)parAnnotation).name() );
									} catch (IllegalArgumentException exe) {
										// not all parameters may be in input.  log and assume empty.
										parValue = "";
										logger.debug("Set " + ((org.datakurator.ffdq.annotations.Parameter)parAnnotation).name()  + " to Empty.");
									}
									logger.debug(parValue);
									if (SciNameUtils.isEmpty(parValue)) { 
										parValue=null;
									}
									paramValues.add(parValue);
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
							logger.debug(instance.getClass().getSimpleName());
							logger.debug(javaMethod.toGenericString());
							logger.debug(javaMethod.getParameterCount());
							if (paramValues.size()==1 && javaMethod.getParameterCount()==1) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0));
							} else if (paramValues.size()==2 && javaMethod.getParameterCount()==2) { 
								if (javaMethod.getParameters()[1].getType().equals(SciNameSourceAuthority.class)) {
									SciNameSourceAuthority replacementParam = new SciNameSourceAuthority();
									if (DateUtils.isEmpty(paramValues.get(1))) { 
										// pass null as parameter to select default source authority.
										replacementParam = null;
									} else {
										replacementParam = new SciNameSourceAuthority(paramValues.get(1));
									}
									retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), replacementParam);
								} else { 
									retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
								}
							} else if (paramValues.size()==3 && javaMethod.getParameterCount()==3) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2));
							} else if (paramValues.size()==4 && javaMethod.getParameterCount()==4) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2), paramValues.get(3));
							} else if (paramValues.size()==5 && javaMethod.getParameterCount()==5) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
										paramValues.get(0), 
										paramValues.get(1), 
										paramValues.get(2), 
										paramValues.get(3), 
										paramValues.get(4));
							} else if (paramValues.size()==6 && javaMethod.getParameterCount()==6) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
										paramValues.get(0), 
										paramValues.get(1), 
										paramValues.get(2), 
										paramValues.get(3), 
										paramValues.get(4), 
										paramValues.get(5));
							} else if (paramValues.size()==7 && javaMethod.getParameterCount()==7) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
										paramValues.get(0), 
										paramValues.get(1), 
										paramValues.get(2), 
										paramValues.get(3), 
										paramValues.get(4), 
										paramValues.get(5), 
										paramValues.get(6));
							} else if (paramValues.size()==8 && javaMethod.getParameterCount()==8) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
										paramValues.get(0), 
										paramValues.get(1), 
										paramValues.get(2), 
										paramValues.get(3), 
										paramValues.get(4), 
										paramValues.get(5), 
										paramValues.get(6), 
										paramValues.get(7));		
							} else if (paramValues.size()==9 && javaMethod.getParameterCount()==9) { 
								retval = (DQResponse<ComplianceValue>)javaMethod.invoke(instance, 
										paramValues.get(0), 
										paramValues.get(1), 
										paramValues.get(2), 
										paramValues.get(3), 
										paramValues.get(4), 
										paramValues.get(5), 
										paramValues.get(6), 
										paramValues.get(7), 
										paramValues.get(8));				
							} else if (paramValues.size()==10 && javaMethod.getParameterCount()==10) { 
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
										paramValues.get(9));									
							} else if (paramValues.size()==11 && javaMethod.getParameterCount()==11) { 
								if (javaMethod.getParameters()[10].getType().equals(SciNameSourceAuthority.class)) {
									SciNameSourceAuthority replacementParam = new SciNameSourceAuthority();
									if (DateUtils.isEmpty(paramValues.get(10))) { 
										// pass null as parameter to select default source authority.
										replacementParam = null;
									} else {
										replacementParam = new SciNameSourceAuthority(paramValues.get(1));
									}
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
											replacementParam);	
								} else { 
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
											paramValues.get(10));			
								}
							} else if (paramValues.size()==19 && javaMethod.getParameterCount()==19) { 
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
							} else if (paramValues.size()==20 && javaMethod.getParameterCount()==20) { 
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
							} else if (paramValues.size()==22 && javaMethod.getParameterCount()==22) { 
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
										paramValues.get(21));													
							} else if (paramValues.size()==23 && javaMethod.getParameterCount()==23) { 
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
							} else if (paramValues.size()==24 && javaMethod.getParameterCount()==24) { 
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
							} else if (paramValues.size()==25 && javaMethod.getParameterCount()==25) { 
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
										paramValues.get(23), 
										paramValues.get(24));								
							} else if (paramValues.size()==27 && javaMethod.getParameterCount()==27) { 
								logger.debug(paramValues.get(20));
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
										paramValues.get(23), 
										paramValues.get(24), 
										paramValues.get(25), 
										paramValues.get(26));								
							} else { 
								logger.error("No implementation of invocation with needed number of parameters " + Integer.toString(paramValues.size()) + " for " + GUID );
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
						} else if (label.startsWith("ISSUE_")) { 
							DQResponse<IssueValue> retval = null;
							logger.debug(paramValues.size());
							logger.debug(instance.getClass().getSimpleName());
							logger.debug(javaMethod.toGenericString());
							logger.debug(javaMethod.getParameterCount());
							if (paramValues.size()==1 && javaMethod.getParameterCount()==1) { 
								retval = (DQResponse<IssueValue>)javaMethod.invoke(instance, paramValues.get(0));
							} else if (paramValues.size()==2 && javaMethod.getParameterCount()==2) { 
								retval = (DQResponse<IssueValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
							} else if (paramValues.size()==3 && javaMethod.getParameterCount()==3) { 
								retval = (DQResponse<IssueValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2));
							} else if (paramValues.size()==4 && javaMethod.getParameterCount()==4) { 
								retval = (DQResponse<IssueValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2), paramValues.get(3));
							} else if (paramValues.size()==5 && javaMethod.getParameterCount()==5) { 
								retval = (DQResponse<IssueValue>)javaMethod.invoke(instance, 
										paramValues.get(0), 
										paramValues.get(1), 
										paramValues.get(2), 
										paramValues.get(3), 
										paramValues.get(4));
							} else if (paramValues.size()==6 && javaMethod.getParameterCount()==6) { 
								retval = (DQResponse<IssueValue>)javaMethod.invoke(instance, 
										paramValues.get(0), 
										paramValues.get(1), 
										paramValues.get(2), 
										paramValues.get(3), 
										paramValues.get(4), 
										paramValues.get(5));								
							} else { 
								logger.error("No implementation of invocation with needed number of parameters " + Integer.toString(paramValues.size()) + " for " + GUID );
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
								logger.debug(paramValues.size());
								logger.debug(javaMethod.getDeclaringClass().getSimpleName());
								logger.debug(javaMethod.toGenericString());
								logger.debug(javaMethod.getParameterCount());
								if (paramValues.size()==1 && javaMethod.getParameterCount()==1) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0));
								} else if (paramValues.size()==2 && javaMethod.getParameterCount()==2) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
								} else if (paramValues.size()==3 && javaMethod.getParameterCount()==3) { 
									if (javaMethod.getParameters()[2].getType().equals(SciNameSourceAuthority.class)) {
										SciNameSourceAuthority replacementParam = new SciNameSourceAuthority();
										if (DateUtils.isEmpty(paramValues.get(2))) { 
											// pass null as parameter to select default source authority.
											replacementParam = null;
										} else {
											replacementParam = new SciNameSourceAuthority(paramValues.get(2));
										}
										retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), replacementParam);
									} else { 
										retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2));
									}
								} else if (paramValues.size()==4 && javaMethod.getParameterCount()==4) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1), paramValues.get(2), paramValues.get(3));
								} else if (paramValues.size()==5 && javaMethod.getParameterCount()==5) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
											paramValues.get(0), 
											paramValues.get(1), 
											paramValues.get(2), 
											paramValues.get(3), 
											paramValues.get(4));
								} else if (paramValues.size()==6 && javaMethod.getParameterCount()==6) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
											paramValues.get(0), 
											paramValues.get(1), 
											paramValues.get(2), 
											paramValues.get(3), 
											paramValues.get(4), 
											paramValues.get(5));			
								} else if (paramValues.size()==7 && javaMethod.getParameterCount()==7) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
											paramValues.get(0), 
											paramValues.get(1), 
											paramValues.get(2), 
											paramValues.get(3), 
											paramValues.get(4), 
											paramValues.get(5), 
											paramValues.get(6));	
								} else if (paramValues.size()==8 && javaMethod.getParameterCount()==8) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
											paramValues.get(0), 
											paramValues.get(1), 
											paramValues.get(2), 
											paramValues.get(3), 
											paramValues.get(4), 
											paramValues.get(5), 
											paramValues.get(6), 
											paramValues.get(7));
								} else if (paramValues.size()==22 && javaMethod.getParameterCount()==22) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
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
											paramValues.get(21));
								} else if (paramValues.size()==23 && javaMethod.getParameterCount()==23) { 
									retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
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
								} else if (paramValues.size()==25 && javaMethod.getParameterCount()==25) { 
									if (javaMethod.getParameters()[24].getType().equals(SciNameSourceAuthority.class)) {
										SciNameSourceAuthority replacementParam = new SciNameSourceAuthority();
										if (DateUtils.isEmpty(paramValues.get(24))) { 
											// pass null as parameter to select default source authority.
											replacementParam = null;
										} else {
											replacementParam = new SciNameSourceAuthority(paramValues.get(24));
										}
										retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
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
												paramValues.get(23), 
												replacementParam);	
									} else { 
										retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
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
											paramValues.get(23), 
											paramValues.get(24));									
									}
								} else if (paramValues.size()==26 && javaMethod.getParameterCount()==26) { 
									if (javaMethod.getParameters()[25].getType().equals(SciNameSourceAuthority.class)) {
										SciNameSourceAuthority replacementParam = new SciNameSourceAuthority();
										if (DateUtils.isEmpty(paramValues.get(25))) { 
											// pass null as parameter to select default source authority.
											replacementParam = null;
										} else {
											replacementParam = new SciNameSourceAuthority(paramValues.get(25));
										}
										retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
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
												paramValues.get(23), 
												paramValues.get(24), 
												replacementParam);	
									} else { 
										retval = (DQResponse<AmendmentValue>)javaMethod.invoke(instance, 
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
											paramValues.get(23), 
											paramValues.get(24), 
											paramValues.get(25));									
									}
								} else { 
									logger.error("No implementation of invocation with needed number of parameters " + Integer.toString(paramValues.size()));
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
												strretval.append(separator).append("\"").append(key).append("\":\"").append(value).append("\"");
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
							logger.debug(paramValues.size());
							logger.debug(javaMethod.getDeclaringClass().getSimpleName());
							logger.debug(javaMethod.toGenericString());
							logger.debug(javaMethod.getParameterCount());
							DQResponse<ResultValue> retval = null;
							if (paramValues.size()==1 && javaMethod.getParameterCount()==1) { 
								retval = (DQResponse<ResultValue>)javaMethod.invoke(instance, paramValues.get(0));
							} else if (paramValues.size()==2 && javaMethod.getParameterCount()==2) { 
								retval = (DQResponse<ResultValue>)javaMethod.invoke(instance, paramValues.get(0), paramValues.get(1));
							} else { 
								logger.error("No implementation of invocation with needed number of parameters " + Integer.toString(paramValues.size()));
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
						if (dataIDsRun.contains(dataID)) { 
							doComparison=false;
						}
						if (doComparison) { 
							boolean sameResultValue = false;
							if (!expectedResult.equals(resultValue))  {
								logger.debug(expectedResult);
								logger.debug(resultValue);
								// TODO: Handle different order of terms in amendment results.
								// split and compare list
							} else { 
								sameResultValue=true;
							}
							if (expectedStatus.equals(resultStatus) && (
									expectedStatus.equals("INTERNAL_PREREQUISITES_NOT_MET") || 
									expectedStatus.equals("EXTERNAL_PREREQUISITES_NOT_MET") || 
									sameResultValue) 
									) {
								StringBuilder message = new StringBuilder()
										.append(dataID)
										.append(" #").append(gitHubIssueNo)
										.append(" Pass");
								logger.debug(message);
								outFileWriter.write(message.toString());
								outFileWriter.write("\n");
								dataIDsRun.add(dataID);
								encounteredTests.get(GUID).incrementPass();
							} else { 
								StringBuilder message = new StringBuilder()
										.append(dataID)
										.append(" #").append(gitHubIssueNo)
										.append(" Fail got ");
								if (!resultStatus.equals(expectedStatus)) { 
									message.append(resultStatus).append(" expected ").append(expectedStatus);
									message.append(" ").append(resultComment);
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
								dataIDsRun.add(dataID);
								encounteredTests.get(GUID).incrementFail();
							}
						} else { 
							StringBuilder message = new StringBuilder()
									.append(dataID)
									.append(" #").append(gitHubIssueNo)
									.append(" Skipped ").append("Type not found.").append("[").append(label).append("]:");
							logger.debug(message);
							if (dataIDsRun.contains(dataID)) {  
								logger.debug("An implementation already run.");
							} else {
								outFileWriter.write(message.toString());
								outFileWriter.write("\n");
								encounteredTests.get(GUID).incrementEncountered();;
							}
						}
					} catch ( InvocationTargetException ex) { 
						// such as same number of parameters, but different data types.
						logger.debug(ex);
						logger.debug(ex.getMessage());
						String errorMessage = ex.getMessage();
						if (ex.getCause()!=null) { 
						 	logger.debug(ex.getCause().getMessage());
							errorMessage = ex.getCause().getMessage();
						}
						StringBuilder message = new StringBuilder()
								.append(dataID)
								.append(" #").append(gitHubIssueNo)
								.append("Exception: ").append(errorMessage);
						outFileWriter.write(message.toString());
						outFileWriter.write("\n");
						encounteredTests.get(GUID).incrementFail();
					} catch ( IllegalAccessException | IllegalArgumentException e) { 
						logger.error(e.getMessage(), e);
					} catch (SourceAuthorityException e) {
						logger.error(e.getMessage(),e);
					}
				}
			}
		}
		return dataIDsRun;
	}
	
}
