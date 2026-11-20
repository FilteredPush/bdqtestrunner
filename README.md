# bdqtestrunner
Tool for validating TDWG Biodiversity Data Quality TG2 test implementations in FilteredPush libraries against the BDQ TG2 test data set.

## Build

mvn package

## Run

    $ java -jar bdqtestrunner-{version}-{commit}-executable.jar -h

	usage: java -jar bdqtestrunner-{version}-{gitcommit}-executable.jar
	 -c,--classes <arg>        Comma separated list of classes containing test
	                           implementations to validate against the test
	                           data (default
	                           DwCMetadataDQDefaults,DwCGeoRefDQDefaults,DwCEv
	                           entDQDefaults,DwCOtherDateDQDefaults,DwCSciName
	                           DQDefaults)
	 -g,--gitHubIssues <arg>   Comma separated list of github issue numbers
	                           for tests to run, if not specified all tests
	                           will run, if specified only the listed tests
	                           will be run.
	 -h,--help                 Show help.
	 -i,--input <arg>          File (filename of a local file) containing test
	                           data against which to validate tests, if not
	                           specified
	                           https://raw.githubusercontent.com/tdwg/bdq/mast
	                           er/tg2/_review/docs/guide/implementers/TG2_test
	                           _validation_data.csv will be used.
	 -o,--output <arg>         File to which to write output, if specified
	                           must not exist.  Default if not specified is
	                           test_run_output.txt which will be overwritten
	                           if it exists.

Tests will be run from the specified classes and their superclasses, e.g. DwCSciNameDQDefaults extends DwCSciNameDQ, so
specifying -c DwCScinNameDQDefaults will attempt to run all the tests in both classes, with only those where the number
of parameters in the test method matches the number of parameters presented in a row of test validation data.  Log
messages are written to the console by default, but can be redirected to a file for examination.  

	$ java -jar bdqtestrunner-0.0.1-SNAPSHOT-7bf484e-executable.jar -c DwCSciNameDQDefaults > output.log

Skipped tests may result from missing blocks invoking a method with a specified number of parameters (where the TestRunner
class needs to add a block to invoke the method found by reflection with the correct number of parameters), or from a multiple
methods for invoking the same test with different numbers of parameters (e.g. where one method takes a bdq:sourceAuthority parameter
and the other method doesn't include this parameter but uses the default value).  Examining the log output can help diagnose 
the causes of skipped tests, for example with: 

	$ grep -A2 "No implementation of invocation" output.log  

## TG2 task group maintinance:

### Reformatting test validation data into expected input form from the (current as of June 2023 v46) working source .xlsx spreadsheet 

Export Data sheet as csv to src/main/resources/ e.g. as src/main/resources/Test_data_23_2022-08-22.csv

Fix the column headers to match those of other Test_data_ csv files (remove columns from end of list, correct several names that have
diverged in the .xlsx file, remove three extraneous rows from end of the sheet).

Edit src/main/java/org/filteredpush/qc/bdqtestrunner/TestOfTestSpreadsheetUtility.java to add a this file as the value of
filename in the last set of the value of the filename variable after line 159  e.g. add the line below as line 175:

   filename = "/Test_data_23_2022-08-22.csv";

Execute the main method of TestOfTestSpreadsheetUtility.java.

   mvn clean package
   sh extract_validation.sh

Examine the console output for error messages concerning commas 
or equals signs that need special case handling (the parser for Input.data and Output.data is very crude), e.g. 

    Error in 217 Line:218 dwc:scientificName="Hakea decurrens ssp. Physocarpa (Barker
    Error in 217 Line:218  1996)"

If found, add appropriate special case handling in the vicinity of line 238, e.g. 

    inputfields = inputfields.replace("Barker, 1996", "Barker| 1996");

Rerun TestOfTestSpreadsheetUtility.java, other errors may need correction by @tasilee in the source .xlxs spreadsheet.

Output will be found as TG2_test_validation_data.csv, 

Copy all lines not containing "non-printing characters" or "[null]" from this file to tdwg:bdq/tg2/core/TG2_test_validation_data.csv  

	grep -v "non-printing characters" TG2_test_validation_data.csv | grep -v "\[null\]" > ../bdq/tg2/core/TG2_test_validation_data.csv

Split out with grep the header and lines containing "non-printing characters" or "[null]" into a separate file, then edit this file to replace those placeholder strings with non-printing low ASCII characters.

	head -n 1 TG2_test_validation_data.csv > ../bdq/tg2/core/TG2_test_validation_data_nonprintingchars.csv
	grep "\(non-printing characters\|\[null\]\)" TG2_test_validation_data.csv >> ../bdq/tg2/core/TG2_test_validation_data_nonprintingchars.csv

In vim, replacing [null] with 0x00 (ascii null), and [non-printing characters] with 0x0E 0x0F (shift out shift in) uses these commands: 
(where ^N is ctrl-V-N, ^O is ctrl-V-O, and ^@ is ctrl-V-@).

	%s/\[non-printing characters\]/^N^O/g
	%s/\[null\]/^@/g

Commit and push these changes to github, as the test runner accesses the validation data from the github repository by default, unless -i is used to specify a local file.

### Validating test implementations ###

#### TIME tests, primarily in event_date_qc ####

From the bdqtestrunner directory using latest bdqtestrunner-{version}-{commit}-executable.jar:

	mvn clean package
	java -jar bdqtestrunner-1.0.1-SNAPSHOT-a445d7d-executable.jar -c DwCOtherDateDQDefaults,DwCEventDQDefaults,DwCMetadataDQDefaults -g 26,69,76,84,130,131,33,36,49,147,126,66,140,76,128,127,88,72,125,67,93,86,132,52,61 > output.log
	cat test_run_output.txt
	grep Fail test_run_output.txt

#### All tests, using default classes ####

From the bdqtestrunner directory using latest bdqtestrunner-{version}-{commit}-executable.jar:

	mvn clean package
	java -jar bdqtestrunner-1.0.1-SNAPSHOT-a445d7d-executable.jar > output.log
	cat test_run_output.txt
	grep Fail test_run_output.txt

## Related BDQ libraries and applications

This application is one of a set of libraries and applications for the draft TDWG [BDQ Standard](https://bdq.tdwg.org/) (Biodiversity Data Quality), about to go into public review ([review draft](https://bdq.tdwg.org/draft/)). None of them are part of the standard.

| Repository | Kind | Role | Available from |
|---|---|---|---|
| [ffdq-api](https://github.com/kurator-org/ffdq-api) | Library | Java annotations and result API for Test implementations | [Maven Central](https://central.sonatype.com/artifact/org.datakurator/ffdq-api), [latest release](https://github.com/kurator-org/ffdq-api/releases/latest) |
| [kurator-ffdq](https://github.com/kurator-org/kurator-ffdq) | Library | bdqffdq model, RDF reports, Test RDF generation and Test runner | [Maven Central](https://central.sonatype.com/artifact/org.datakurator/kurator-ffdq), [latest release](https://github.com/kurator-org/kurator-ffdq/releases/latest) |
| [sci_name_qc](https://github.com/FilteredPush/sci_name_qc) | Test implementations | NAME Tests | [Maven Central](https://central.sonatype.com/artifact/org.filteredpush/sci_name_qc), [latest release](https://github.com/FilteredPush/sci_name_qc/releases/latest) |
| [event_date_qc](https://github.com/FilteredPush/event_date_qc) | Test implementations | TIME Tests | [Maven Central](https://central.sonatype.com/artifact/org.filteredpush/event_date_qc), [latest release](https://github.com/FilteredPush/event_date_qc/releases/latest) |
| [rec_occur_qc](https://github.com/FilteredPush/rec_occur_qc) | Test implementations | OTHER (metadata) Tests and SingleRecord Measures | [Maven Central](https://central.sonatype.com/artifact/org.filteredpush/rec_occur_qc), [latest release](https://github.com/FilteredPush/rec_occur_qc/releases/latest) |
| [geo_ref_qc](https://github.com/FilteredPush/geo_ref_qc) | Test implementations | SPACE Tests | [Maven Central](https://central.sonatype.com/artifact/org.filteredpush/geo_ref_qc), [latest release](https://github.com/FilteredPush/geo_ref_qc/releases/latest) |
| [bdqtestrunner](https://github.com/FilteredPush/bdqtestrunner) (this repository) | Application | Checks Test implementations against the BDQ Test validation data | [latest release](https://github.com/FilteredPush/bdqtestrunner/releases/latest) |
| [bdq_workbench](https://github.com/FilteredPush/bdq_workbench) | Application | Runs BDQ Tests over Darwin Core data for a Use Case | [latest release](https://github.com/FilteredPush/bdq_workbench/releases/latest) |
| [bdq_usecase_builder](https://github.com/FilteredPush/bdq_usecase_builder) | Application | Authors Use Cases and Test definitions | [latest release](https://github.com/FilteredPush/bdq_usecase_builder/releases/latest) |
| [bdq_issue_to_csv](https://github.com/kurator-org/bdq_issue_to_csv) | Application | Converts tdwg/bdq Test issues to a term-version CSV | [latest release](https://github.com/kurator-org/bdq_issue_to_csv/releases/latest) |
