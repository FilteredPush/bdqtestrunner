# bdqtestrunner
Tool for validating TDWG Biodiversity Data Quality TG2 test implementations in FilteredPush libraries against the BDQ TG2 test data set.

## Build

mvn package

## Run

    $ java -jar bdqtestrunner-{version}-{commit}-executable.jar -h

    usage: java -jar bdqtestrunner-{version}-{gitcommit}-executable.jar
     -c,--classes <arg>        List of classes containing test implementations
                               to validate against the test data (dafault
                               DwCGeoRefDQ DwCEventDQ DwCOtherDateDQ
                               DwCSciNameDQ)
     -g,--gitHubIssues <arg>   List of github issue numbers for tests to run,
                               if not specified all tests will run, if
                               specified only the listed tests will be run.
     -h,--help                 Show help.
     -i,--input <arg>          File (filename of a local file) containing test
                               data against which to validate tests, if not
                               specified
                               https://raw.githubusercontent.com/tdwg/bdq/mast
                               er/tg2/core/TG2_test_validation_data.csv will
                               be used.
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

### Reformatting test data into expected input form from the (current as of August 2022) working source .xlsx spreadsheet 

Export Data sheet as csv to src/main/resources/ e.g. as src/main/resources/Test_data_23_2022-08-22.csv

Edit src/main/java/org/filteredpush/qc/bdqtestrunner/TestOfTestSpreadsheetUtility.java to add a this file as the value of
filename in the last set of the value of the filename variable after line 159  e.g. add the line below as line 175:

   filename = "/Test_data_23_2022-08-22.csv";

Execute the main method of TestIfTestSpreadsheetUtility.java, examine the console output for error messages concerning commas 
or equals signs that need special case handling (the parser for Input.data and Output.data is very crude), e.g. 

    Error in 217 Line:218 dwc:scientificName="Hakea decurrens ssp. Physocarpa (Barker
    Error in 217 Line:218  1996)"

If found, add appropriate special case handling in the vicinity of line 238, e.g. 

    inputfields = inputfields.replace("Barker, 1996", "Barker| 1996");

Rerun TestOfTestSpreadsheetUtility.java, other errors may need correction by @tasilee in the source .xlxs spreadsheet.

Output will be found as TG2_test_validation_data.csv, this file is tdwg:bdq/tg2/core/TG2_test_validation_data.csv
