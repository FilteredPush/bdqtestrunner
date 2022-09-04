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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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
        
		Options options = new Options();
		options.addOption("i","input",true,"File (filename of a local file) containing test data against which to validate tests, if not specified https://raw.githubusercontent.com/tdwg/bdq/master/tg2/core/TG2_test_validation_data.csv will be used.");
		options.addOption("o","output",true,"File to which to write output, if specified must not exist.  Default if not specified is test_run_output.txt which will be overwritten if it exists.");
		options.addOption("c","classes",true,"Comma separated list of classes containing test implementations to validate against the test data (default DwCMetadataDQ,DwCGeoRefDQDefaults,DwCEventDQDefaults,DwCOtherDateDQ,DwCSciNameDQDefaults)");
		options.addOption("g","gitHubIssues", true, "Comma separated list of github issue numbers for tests to run, if not specified all tests will run, if specified only the listed tests will be run.");
		options.addOption("h","help",false,"Show help.");

		try { 
			// Get option values
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			
			if (cmd.hasOption("h")) { 
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "java -jar bdqtestrunner-{version}-{gitcommit}-executable.jar", options);
				System.exit(0);
			} else {
				String outfile = "test_run_output.txt";
				String infile = null;
				TestRunner testRunner;
				// setup test runner against target validation data file
				if (cmd.hasOption("i")) { 
					infile = cmd.getOptionValue("i");
					File inputFile = new File(infile);
					if (!inputFile.exists()) {
						throw new Exception("Specified input file [" + infile + "] not found.");
					}
					if (!inputFile.canRead()) {
						throw new Exception("Unable to read specified input file [" + infile + "].");
					}
					testRunner = new TestRunner(inputFile);
				} else { 
					testRunner = new TestRunner();
				}
				// set optional conditions
				if (cmd.hasOption("o")) {
					outfile = cmd.getOptionValue("o");
					testRunner.setOutputFile(outfile);
				}
				if (cmd.hasOption("c")) {
					String[] classes = cmd.getOptionValues("c");
					if (classes !=null && classes.length==1 && classes[0].contains(",")) { 
						// handle a comma separated list as the argument
						classes = classes[0].split(",");
					}
					List<String> classList = Arrays.asList(classes);
					classList.replaceAll(String::trim);
					testRunner.setListToRun(classList);
				}
				if (cmd.hasOption("g")) {
					String[] issues = cmd.getOptionValues("g");
					if (issues !=null && issues.length==1 && issues[0].contains(",")) {
						// handle a comma separated list as the argument
						issues = issues[0].split(",");
					}
					List<String> issueList = Arrays.asList(issues);
					issueList.replaceAll(String::trim);
					testRunner.setIssuesToRun(issueList);
				}
				// run the tests
				testRunner.runTests();
			}
		
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
			System.out.println(e.getMessage());
		}
    	
    	System.out.println("Done");
    }
}
