/**
 * TestOfTestSpreasheetUtility.java
 */
package org.filteredpush.qc.bdqtestrunner;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mole
 *
 */
public class TestOfTestSpreasheetUtility {

	private static final Log logger = LogFactory.getLog(TestOfTestSpreasheetUtility.class);
	
	/**
	 * Get a map of the supported information elements with each entry 
	 * in the form "dwc:county","dwc:county".
	 * 
	 * @return a map of information elements
	 */
	public static Map<String,String> getInformationElements() { 
		Map<String,String> result = new LinkedHashMap<String,String>();
		result.put("bdq:annotation","bdq:annotation");
		result.put("bdq:sourceAuthority","bdq:sourceAuthority");
		result.put("dc:type","dc:type");
		result.put("dcterms:license","dcterms:license");
		result.put("dwc:acceptedNameUsageID","dwc:acceptedNameUsageID");
		result.put("dwc:basisOfRecord","dwc:basisOfRecord");
		result.put("dwc:class","dwc:class");
		result.put("dwc:continent","dwc:continent");
		result.put("dwc:coordinateUncertaintyInMeters","dwc:coordinateUncertaintyInMeters");
		result.put("dwc:country","dwc:country");
		result.put("dwc:countryCode","dwc:countryCode");
		result.put("dwc:county","dwc:county");
		result.put("dwc:dataGeneralizations","dwc:dataGeneralizations");
		result.put("dwc:dateIdentified","dwc:dateIdentified");
		result.put("dwc:day","dwc:day");
		result.put("dwc:decimalLatitude","dwc:decimalLatitude");
		result.put("dwc:decimalLongitude","dwc:decimalLongitude");
		result.put("dwc:endDayOfYear","dwc:endDayOfYear");
		result.put("dwc:establishmentMeans","dwc:establishmentMeans");
		result.put("dwc:eventDate","dwc:eventDate");
		result.put("dwc:family","dwc:family");
		result.put("dwc:genus","dwc:genus");
		result.put("dwc:geodeticDatum","dwc:geodeticDatum");
		result.put("dwc:higherClassification","dwc:higherClassification");
		result.put("dwc:higherGeography","dwc:higherGeography");
		result.put("dwc:higherGeographyID","dwc:higherGeographyID");
		result.put("dwc:infraspecificEpithet","dwc:infraspecificEpithet");
		result.put("dwc:island","dwc:island");
		result.put("dwc:islandGroup","dwc:islandGroup");
		result.put("dwc:kingdom","dwc:kingdom");
		result.put("dwc:locality","dwc:locality");
		result.put("dwc:locationID","dwc:locationID");
		result.put("dwc:maximumDepthInMeters","dwc:maximumDepthInMeters");
		result.put("dwc:maximumElevationInMeters","dwc:maximumElevationInMeters");
		result.put("dwc:minimumDepthInMeters","dwc:minimumDepthInMeters");
		result.put("dwc:minimumElevationInMeters","dwc:minimumElevationInMeters");
		result.put("dwc:month","dwc:month");
		result.put("dwc:municipality","dwc:municipality");
		result.put("dwc:occurrenceID","dwc:occurrenceID");
		result.put("dwc:occurrenceStatus","dwc:occurrenceStatus");
		result.put("dwc:order","dwc:order");
		result.put("dwc:originalNameUsageID","dwc:originalNameUsageID");
		result.put("dwc:parentNameUsageID","dwc:parentNameUsageID");
		result.put("dwc:phylum","dwc:phylum");
		result.put("dwc:scientificName","dwc:scientificName");
		result.put("dwc:scientificNameAuthorship","dwc:scientificNameAuthorship");
		result.put("dwc:scientificNameID","dwc:scientificNameID");
		result.put("dwc:specificEpithet","dwc:specificEpithet");
		result.put("dwc:startDayOfYear","dwc:startDayOfYear");
		result.put("dwc:stateProvince","dwc:stateProvince");
		result.put("dwc:subgenus","dwc:subgenus");
		result.put("dwc:taxon","dwc:taxon");
		result.put("dwc:taxonConceptID","dwc:taxonConceptID");
		result.put("dwc:taxonID","dwc:taxonID");
		result.put("dwc:taxonRank","dwc:taxonRank");
		result.put("dwc:verbatimCoordinateSystem","dwc:verbatimCoordinateSystem");
		result.put("dwc:verbatimCoordinates","dwc:verbatimCoordinates");
		result.put("dwc:verbatimDepth","dwc:verbatimDepth");
		result.put("dwc:verbatimElevation","dwc:verbatimElevation");
		result.put("dwc:verbatimEventDate","dwc:verbatimEventDate");
		result.put("dwc:verbatimLatitude","dwc:verbatimLatitude");
		result.put("dwc:verbatimLocality","dwc:verbatimLocality");
		result.put("dwc:verbatimLongitude","dwc:verbatimLongitude");
		result.put("dwc:verbatimSRS","dwc:verbatimSRS");
		result.put("dwc:vernacularName","dwc:vernacularName");
		result.put("dwc:waterBody","dwc:waterBody");
		result.put("dwc:year","dwc:year");
		result.put("dwc:subfamily","dwc:subfamily");
		result.put("dwc:superfamily","dwc:superfamily");
		result.put("dwc:tribe","dwc:tribe");
		result.put("dwc:subtribe","dwc:subtribe");
		result.put("dwc:genericName","dwc:genericName");
		result.put("dwc:infragenericEpithet","dwc:infragenericEpithet");
		result.put("dwc:cultivarEpithet","dwc:cultivarEpithet");
		result.put("dwc:individualCount","dwc:individualCount");
		result.put("dwc:organismQuantity","dwc:organismQuantity");
		result.put("dwc:footprintWKT","dwc:footprintWKT");
		result.put("dwc:coordinatePrecision","dwc:coordinatePrecision");
		result.put("dwc:namePublishedInYear","dwc:namePublishedInYear");
		result.put("dwc:sex","dwc:sex");
		result.put("dwc:typeStatus","dwc:typeStatus");
		result.put("dwc:pathway","dwc:pathway");
		result.put("dwc:degreeOfEstablishment","dwc:degreeOfEstablishment");
		
		return result;
	}
	
	/**
	 * Default constructor.
	 */
	public TestOfTestSpreasheetUtility() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Parse an (internally specified) spreadsheet of test specification data.
	 * 
	 * @throws IOException if unable to read file
	 * @throws URISyntaxException if filename is not correctly hardcoded.
	 */
	private static void parseSourceSpreadsheet() throws IOException, URISyntaxException { 
		
		List<String> classList = new ArrayList<String>();
		classList.add("MEASURE");
		classList.add("VALIDATION");
		classList.add("AMENDMENT");
		classList.add("ISSUE");
		List<String> validationResponseStatusTerms = new ArrayList<String>();
		validationResponseStatusTerms.add("RUN_HAS_RESULT");
		validationResponseStatusTerms.add("INTERNAL_PREREQUISITES_NOT_MET");
		validationResponseStatusTerms.add("EXTERNAL_PREREQUISITES_NOT_MET");
		List<String> validationResponseResultTerms = new ArrayList<String>();
		validationResponseResultTerms.add("COMPLIANT");
		validationResponseResultTerms.add("NOT_COMPLIANT");
		List<String> amendmentResponseStatusTerms = new ArrayList<String>();
		amendmentResponseStatusTerms.add("AMENDED");
		amendmentResponseStatusTerms.add("NOT_AMENDED");
		amendmentResponseStatusTerms.add("FILLED_IN");
		amendmentResponseStatusTerms.add("INTERNAL_PREREQUISITES_NOT_MET");
		amendmentResponseStatusTerms.add("EXTERNAL_PREREQUISITES_NOT_MET");
		
		Map<String,String> outputRow = new LinkedHashMap<String,String>();
		outputRow.put("LineNumber", "");
		outputRow.put("dataID", "");
		outputRow.put("LineForTest", "");
		outputRow.put("GitHubIssueNo", "");
		outputRow.put("GUID", "");
		outputRow.put("Label", "");
		outputRow.put("Response.status", "");
		outputRow.put("Response.result", "");
		outputRow.put("Response.comment", "");
		//outputRow.put("Explanation", "");
		outputRow.put("IssuesWithThisRow", "");
		
		Map<String,String> terms = new HashMap<String,String>();
		Map<String,String> outterms = new HashMap<String,String>();
	    String filename = "";
	    // previous header structure.
//	    filename = "/Test_data_7_2022-02-26.csv";
//	    filename = "/Test_data_9_2022-03-04.csv";
//	    filename = "/Test_data_10_2022-03-06.csv";
//	    filename = "/Test_data_11_2022-03-09.csv";
//	    filename = "/Test_data_12_2022-03-09.csv";
//	    filename = "/Test_data_13_2022-03-10.csv";
	    // new header structure with v14
	    filename = "/Test_data_14_2022-03-14.csv";
	    filename = "/Test_data_15_2022-03-14.csv";
	    filename = "/Test_data_16_2022-03-16.csv";
	    filename = "/Test_data_17_2022-03-17.csv";
	    filename = "/Test_data_18_2022-03-19.csv";
	    filename = "/Test_data_19_2022-03-21.csv";
	    filename = "/Test_data_22_2022-06-02.csv";
	    filename = "/Test_data_23_2022-08-22.csv";
	    filename = "/Test_data_24_2022-08-24.csv";
	    filename = "/Test_data_26_2022-08-30.csv";
	    filename = "/Test_data_28_2022-09-06.csv";
	    filename = "/Test_data_29_2022-09-07.csv";
	    filename = "/Test_data_32_2022-09-11.csv";
	    filename = "/Test_data_33_2022-09-12.csv";
	    filename = "/Test_data_35_2022-11-11.csv";
	    filename = "/Test_data_42_2023-03-30.csv";
	    filename = "/Test_data_45_2023-06-11.csv";
	    filename = "/Test_data_46_2023-06-12.csv";
	    filename = "/Test_data_50_2023-06-27.csv";
	    filename = "/Test_data_52_2023-07-02.csv";
	    filename = "/Test_data_53_2023-07-03.csv";
	    filename = "/Test_data_54_2023-07-10.csv";
	    filename = "/Test_data_57_2023-07-19.csv";
	    filename = "/Test_data_63_2023-12-15.csv";
	    filename = "/Test_data_67_2024_07_20.csv";
	    filename = "/Test_data_68_2024_07_22.csv";
	    filename = "/Test_data_69_2024_07_22.csv";
	    filename = "/Test_data_70_2024_07_22.csv";
	    //URL urlinfile = TestOfTestSpreasheetUtility.class.getResource(filename);
	    //File inputfile = new File(urlinfile.toURI());
	    //Reader in = new FileReader(inputfile);
	    InputStream is = TestOfTestSpreasheetUtility.class.getResourceAsStream(filename);
	    Reader in = new InputStreamReader(is);
	    String outputFileName = "TG2_test_validation_data.csv";
	    CSVPrinter printer = new CSVPrinter(new FileWriter(outputFileName), CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL));
	    CSVParser records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
	    Map<String,Integer> header = records.getHeaderMap();
	    int line = 2; // first line in spreadsheet, header is 1.
	    int errors = 0;
	    for (CSVRecord record : records) {
	    	outputRow.put("LineNumber",Integer.toString(line));
	    	//outputRow.put("LineForTest",record.get("InTestLine #"));
	    	outputRow.put("LineForTest",record.get("LineForTest"));
	    	String dataID = record.get("dataID");
	    	outputRow.put("dataID",dataID);
	    	outputRow.put("GitHubIssueNo",record.get("Number"));
	    	outputRow.put("GUID",record.get("GUID"));
	    	String testType = record.get("Output Type"); 
	    	if (!classList.contains(testType)) { 
	    		errors ++;
	    		System.out.println("Error in " + dataID + " Line:" + Integer.toString(line) + " unrecognized test class: " + testType);
	    	}
	    	outputRow.put("Label",testType + "_" + record.get("Label"));
	    	String responseStatus =  record.get("Response.status").trim();
	    	outputRow.put("Response.status", responseStatus);
	    	String responseResultValue = record.get("Response.result");
	    	String outputDataValue = record.get("Output.data");
	    	if (outputDataValue.length()>0 && !responseResultValue.equals(outputDataValue)) { 
	    		errors ++;
	    		System.out.println("Error in " + dataID + " Line:" + Integer.toString(line) + " missmatch between Response.result ["+responseResultValue+"] and Output.data ["+ outputDataValue +"] ");
	    	}
	    	if (responseResultValue.matches("^\"[0-9.]+\"$")) { 
	    		responseResultValue = responseResultValue.replace("\"", "");
	    	}
	    	outputRow.put("Response.result", responseResultValue);
	    	outputRow.put("Response.comment", record.get("Response.comment"));
	    	//outputRow.put("Explanation", record.get("Explanation"));
	    	outputRow.put("IssuesWithThisRow", record.get("ISSUE"));
	    	Iterator<String> iin = getInformationElements().keySet().iterator();
	    	while (iin.hasNext()) { 
	    		outputRow.put(iin.next(),"");
	    	}
	    	Map<String,String> resultTermValues = new HashMap<String,String>();
	    	
	    	if (testType.equals("VALIDATION") && !validationResponseStatusTerms.contains(responseStatus)) { 
	    		System.out.println("Error in " + Integer.toString(line) + " unrecognized response.status " +responseStatus + " for " + testType);
	    		errors++;
	    	}
	    	if (testType.equals("MEASURE") && !validationResponseStatusTerms.contains(responseStatus)) { 
	    		System.out.println("Error in " + Integer.toString(line) + " unrecognized response.status " +responseStatus + " for " + testType);
	    		errors++;
	    	}
	    	if (testType.equals("AMENDMENT") && !amendmentResponseStatusTerms.contains(responseStatus)) { 
	    		System.out.println("Error in " + Integer.toString(line) + " unrecognized response.status " +responseStatus + " for " + testType);
	    		errors++;
	    	}
	    	
	    	
	    	// {GitHub Issue=0, Number=1, GUID=2, Output Type=3, Label=4, Dimension=5, 
	    	// Line #=6, Input.data=7, Output.data=8, Response.status=9, Response.result=10, 
	    	// Response.comment=11, Explanation=12}
	    	//System.out.print(Integer.toString(line) + " " + record.get("GUID") + " ");
	    	String inputfields = record.get("Input.data");
	    	inputfields = inputfields.replace('”', '"');  // handle alternative quote characters
	    	// handle a couple of special cases for splitting on comma: 
	    	inputfields = inputfields.replace("Desmarest, 1804", "Desmarest| 1804");
	    	inputfields = inputfields.replace("Perry, 1811", "Perry| 1811");
	    	inputfields = inputfields.replace("Adanson, 1763", "Adanson| 1763");
	    	inputfields = inputfields.replace("Adans., 1763", "Adans.| 1763");
	    	inputfields = inputfields.replace("Jeffreys, 1867", "Jeffreys| 1867");
	    	inputfields = inputfields.replace("Barker, 1996", "Barker| 1996");
	    	inputfields = inputfields.replace("Györfi, 1952", "Györfi| 1952");
	    	inputfields = inputfields.replace(", 1822", "| 1822");
	    	inputfields = inputfields.replace(" 10m, ", " 10m| ");
	    	inputfields = inputfields.replace("maxElevation=100", "maxElevation@100");
	    	inputfields = inputfields.replace("maxdepth=100", "maxdepth@100");
	    	inputfields = inputfields.replace("?name=Puma", "?name@Puma");
	    	inputfields = inputfields.replace("check?dataset=APNI", "check?dataset@APNI");
	    	inputfields = inputfields.replace("taxon_profile.cfm?taxon_id=62947", "taxon_profile.cfm?taxon_id@62947");
	    	inputfields = inputfields.replace("POLYGON ((10 20, 11 20, 11 21, 10 21, 10 20))", "POLYGON ((10 20| 11 20| 11 21| 10 21| 10 20))");
	    	List<String> bits = Arrays.asList(inputfields.split(","));
	    	if (bits.isEmpty()) { 
	    		System.out.println("Error in " + Integer.toString(line) + " " +inputfields);
	    		errors++;
	    	}
	    	Iterator<String> i = bits.iterator();
	    	while (i.hasNext()) {
	    		String bit = i.next();
	    		List<String> subbits = Arrays.asList(bit.split("="));
	    		if (subbits.size()!=2) { 
	    			System.out.println("Error in " + dataID + " Line:" + Integer.toString(line) + " " + bit );
	    			errors++;
	    		} else { 
	    			// System.out.print(subbits.get(0) + " " + subbits.get(1));
	    			String term = subbits.get(0).trim();
	    			terms.put(term, term);
	    			String value = subbits.get(1).trim();
	    			if (!value.startsWith("\"") || !value.endsWith("\"")) { 
	    				System.out.println("Error in " + dataID + " Line:" + Integer.toString(line) + " " + bit);
	    				errors++;
	    			} else { 
	    				// output
	    				if (outputRow.containsKey(term)) { 
	    					String cleanedValue = value;
	    					// handle the special cases for splitting on comma: 
	    					cleanedValue = cleanedValue.replace("Desmarest| 1804", "Desmarest, 1804");
	    					cleanedValue = cleanedValue.replace("Perry| 1811", "Perry, 1811");
	    					cleanedValue = cleanedValue.replace("Adanson| 1763", "Adanson, 1763");
	    					cleanedValue = cleanedValue.replace("Adans.| 1763", "Adans., 1763");
	    					cleanedValue = cleanedValue.replace("Jeffreys| 1867", "Jeffreys, 1867");
	    					cleanedValue = cleanedValue.replace("Györfi| 1952", "Györfi, 1952");
	    					cleanedValue = cleanedValue.replace("| 1822", ", 1822");
	    					cleanedValue = cleanedValue.replace(" 10m| ", " 10m, ");
	    					cleanedValue = cleanedValue.replace("maxElevation@100", "maxElevation=100");
	    					cleanedValue = cleanedValue.replace("maxdepth@100", "maxdepth=100");
	    					cleanedValue = cleanedValue.replace("?name@Puma", "?name=Puma");
	    					cleanedValue = cleanedValue.replace("check?dataset@API", "check?dataset=API");
	    					cleanedValue = cleanedValue.replace("taxon_profile.cfm?taxon_id@62947", "taxon_profile.cfm?taxon_id=62947");
	    					cleanedValue = cleanedValue.replace("POLYGON ((10 20| 11 20| 11 21| 10 21| 10 20))", "POLYGON ((10 20, 11 20, 11 21, 10 21, 10 20))");
	    					// trim off leading/trailing quotes.
	    					if (cleanedValue.charAt(0)=='"') { 
	    						cleanedValue = cleanedValue.substring(1);
	    					}
	    					if (cleanedValue.charAt(cleanedValue.length()-1)=='"') { 
	    						cleanedValue = cleanedValue.substring(0,cleanedValue.length()-1);
	    					}
	    					outputRow.put(term, cleanedValue);
	    				} else {
	    					errors++;
	    					System.out.println("Error in " + dataID + " Line:" + Integer.toString(line) + " unrecognized input term " + term);
	    				}
	    			}
	    		}
	    	}
	    	String outfields = record.get("Output.data");
	    	List<String> outbits = Arrays.asList(outfields.split(","));
	    	i = outbits.iterator();
	    	while (i.hasNext()) {
	    		String bit = i.next();
	    		List<String> subbits = Arrays.asList(bit.split("="));
	    		if (bit.trim().length()>0  &&  subbits.size()!=2 && !testType.equals("MEASURE")) { 
	    			System.out.println("Error in Output " + dataID + " Line:" + Integer.toString(line) + " " + bit );
	    			errors++;
	    		} else if (subbits.size()==1 && testType.equals("MEASURE")) { 
	    			// skip, ok.
	    		} else if (bit.trim().length() > 0) { 
	    			// System.out.print(subbits.get(0) + " " + subbits.get(1));
	    			String term = subbits.get(0).trim();
	    			outterms.put(term, term);
	    			String value = subbits.get(1).trim();
	    			if (!value.startsWith("\"") || !value.endsWith("\"")) { 
	    				System.out.println("Error in Output " + dataID + " Line:" + Integer.toString(line) + " " + bit);
	    				errors++;
	    			} else { 
	    				// output
	    				if (outputRow.containsKey(term)) { 
	    					String cleanedValue = value;
	    					// handle a couple of special cases for splitting on comma: 
	    					cleanedValue = cleanedValue.replace("Desmarest| 1804", "Desmarest, 1804)");
	    					cleanedValue = cleanedValue.replace(" 10m| ", " 10m, ");
	    					// trim off leading/trailing quotes.
	    					if (cleanedValue.charAt(0)=='"') { 
	    						cleanedValue = cleanedValue.substring(1);
	    					}
	    					if (cleanedValue.charAt(cleanedValue.length()-1)=='"') { 
	    						cleanedValue = cleanedValue.substring(0,cleanedValue.length()-1);
	    					}
	    					resultTermValues.put(term, cleanedValue);
	    				} else { 
	    					errors++;
	    					System.out.println("Error in " + dataID + " Line:" + Integer.toString(line) + " unrecognized output term " + term);
	    				}
	    			}
	    		}
	    	}
	    	
	    	if (line==2) { 
	    		printer.printRecord(outputRow.keySet());
	    	}
	    	if (resultTermValues.size()>0) { 
	    		// convert 
	    		String resultVals = "{";
	    		Iterator<String> iout = resultTermValues.keySet().iterator();
	    		String separator = "";
	    		while (iout.hasNext()) { 
	    			String key = iout.next();
	    			resultVals = resultVals + separator + '"' + key + '"'+':'+'"'+ resultTermValues.get(key).replace("\"", "\\"+"\"") + '"';
	    			separator = ",";
	    		}
	    		resultVals = resultVals.concat("}");
	    		outputRow.put("Response.result", resultVals);
	    	}
	    	if (resultTermValues.size()>0 && !(outputRow.get("Response.status").equals("AMENDED") || outputRow.get("Response.status").equals("FILLED_IN")) ) {
	    		System.out.println("Error in Output " + dataID + " Line:" + Integer.toString(line) + " key:value pairs present when Response.status is not AMENDED or FILLED_IN");
	    		errors++;
	    	}
	    	printer.printRecord(outputRow.values());
	    	line++;
	    }
	    
	    printer.close();
	    
	    Set<String> keyset = terms.keySet();
	    SortedSet<String> keys = new TreeSet<String>();
	    keys.addAll(keyset);
	    Iterator<String> i = keys.iterator();
	    System.out.println("Terms matched in InputFields");
	    while (i.hasNext()) { 
	    	System.out.println(i.next());
	    }
	    Set<String> outkeyset = outterms.keySet();
	    SortedSet<String> outkeys = new TreeSet<String>();
	    outkeys.addAll(outkeyset);
	    i = keys.iterator();
	    System.out.println("Terms matched in OutputFields");
	    while (i.hasNext()) { 
	    	System.out.println(i.next());
	    }
	    System.out.println("Errors: " + Integer.toString(errors));
	    
	}

	/**
	 * Main method to launch parsing of a source spreadsheet from the command line.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			parseSourceSpreadsheet();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
