package com.koch.classbrowser.compare;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import com.koch.classbrowser.java.ExitCode;
import com.koch.classbrowser.java.OutputUtil;
import com.koch.classbrowser.java.TypeDescription;

/**
 * @author juergen.panser
 *
 */
public class CompareApplication {

	// ---- CONSTANTS ----------------------------------------------------------

	public static final String DECO = "\n========================================\n";

	public static final String ARG_KEY_HELP = "help";

	public static final String ARG_KEY_JAVAFILE = "javaFile";

	public static final String ARG_KEY_CSHARPFILE = "csFile";

	public static final String ARG_KEY_TARGETPATH = "targetPath";

	public static final String ARG_KEY_MISMATCHESONLY = "mismatchesOnly";

	public static final String ARG_KEY_RESULTTYPE = "resultType";

	public static final String EXPORT_TYPE_COMPARE = "c";

	public static final String EXPORT_TYPE_TEST = "t";

	public static final String EXPORT_TYPE_CHECKSTYLE = "s";

	private static final String COMPARE_RESULTS_FILE_NAME = "compare_results";

	private static final String TEST_RESULTS_FILE_NAME = "test_results";

	private static final String CHECKSTYLE_RESULTS_FILE_NAME = "checkstyle_results";

	private static final Pattern AMBETH_VERSION = Pattern.compile("-\\d\\.\\d\\.\\d+");

	private static final List<CompareStatus> INFO_STATES = Arrays.asList(CompareStatus.NOT_COMPARED,
			CompareStatus.NO_MATCHING_CSHARP_CLASS_FOUND, CompareStatus.NO_MATCHING_JAVA_CLASS_FOUND,
			CompareStatus.MODULENAME_CASE, CompareStatus.INTERNAL_METHOD_NOT_FOUND);

	private static final List<CompareStatus> WARNING_STATES =
			Arrays.asList(CompareStatus.NO_MODULENAME_FOUND, CompareStatus.MODULENAME_DIFFERS,
					CompareStatus.PATTERN_VIOLATION, CompareStatus.METHOD_NAME_CASE,
					CompareStatus.PUBLIC_METHOD_NOT_FOUND);

	// ---- VARIABLES ----------------------------------------------------------

	private static boolean doLog = true;

	// ---- METHODS ------------------------------------------------------------

	/**
	 * Entry point of the application.
	 *
	 * @param args Program arguments
	 */
	public static void main(String[] args) {
		try {
			run();
			System.exit(ExitCode.SUCCESS.getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			// Program.showMessage(e.getMessage() + "\n\n");
		}
		System.exit(ExitCode.ERROR.getCode());
	}

	/**
	 * The program logic.
	 */
	private static void run() {

		if (wantsHelp()) {
			displayHelpAndWait();
			return;
		}

		// Read the files
		log("Reading files...");
		SortedMap<String, TypeDescription> analyzedJavaClasses = extractFromFile(ARG_KEY_JAVAFILE);
		SortedMap<String, TypeDescription> analyzedCsharpClasses = extractFromFile(ARG_KEY_CSHARPFILE);

		// Compare the files
		log("Comparing...");
		List<CompareResult> compareResults =
				CompareUtil.compare(analyzedJavaClasses, analyzedCsharpClasses);

		// Export the results
		if (doExportResults(EXPORT_TYPE_COMPARE)) {
			boolean mismatchOnly = Boolean.getBoolean(ARG_KEY_MISMATCHESONLY);
			ExportResults(compareResults, System.getProperty(ARG_KEY_TARGETPATH), true, mismatchOnly);
		}
		if (doExportResults(EXPORT_TYPE_TEST)) {
			ExportAsJunitTestResult(compareResults, System.getProperty(ARG_KEY_TARGETPATH));
		}
		if (doExportResults(EXPORT_TYPE_CHECKSTYLE)) {
			ExportAsCheckstyleTestResult(compareResults, System.getProperty(ARG_KEY_TARGETPATH));
		}
		log("FINISHED!");
	}

	/**
	 * Check if help should be displayed.
	 *
	 * @return True if the help should be displayed
	 */
	private static boolean wantsHelp() {
		return System.getProperties().containsKey(ARG_KEY_HELP);
	}

	/**
	 * Display the help text.
	 */
	private static void displayHelpAndWait() {
		List<String> messages = Arrays.asList( //
				DECO, //
				"    Welcome to the class comparer.", //
				DECO, //
				"The following arguments are supported:\n", //
				ARG_KEY_HELP + " displays this help screen\n", //
				ARG_KEY_JAVAFILE + "={filename} sets the name of the export file for the Java types\n", //
				ARG_KEY_CSHARPFILE + "={filename} sets the name of the export file for the C# types\n", //
				ARG_KEY_TARGETPATH
						+ "={path} sets the path where the compare result file should be written to\n", //
				ARG_KEY_MISMATCHESONLY + "=true if only mismatches should be reported\n", //
				ARG_KEY_RESULTTYPE + "=" + EXPORT_TYPE_TEST + " (export as JUnit test results), ="
						+ EXPORT_TYPE_CHECKSTYLE + " (export as Checkstyle test results), ="
						+ EXPORT_TYPE_COMPARE
						+ " (export as compare results) or any combination; export all is default if omitted\n", //
				"Example:\n",
				"java -D" + ARG_KEY_JAVAFILE + "=c:\\temp\\export_java.xml -D" + ARG_KEY_CSHARPFILE
						+ "=c:\\temp\\export_csharp.xml -D" + ARG_KEY_TARGETPATH
						+ "=c:\\temp\\results -jar Classcomparer.jar", //
				DECO);
		Console console = System.console();
		if (console == null) {
			for (String message : messages) {
				log(message);
			}
			return;
		}
		for (String message : messages) {
			console.format(message);
		}
	}

	/**
	 * @param resultType Export result type: c or t
	 * @return True if the export should be done for the given type
	 */
	private static boolean doExportResults(String resultType) {
		String property = System.getProperty(ARG_KEY_RESULTTYPE);
		if (property != null) {
			return property.contains(resultType);
		}
		return true;
	}

	/**
	 * Log out the given message. Convenience method to have a single point where to change the
	 * behavior.
	 *
	 * @param message Message to log
	 */
	public static void log(String message) {
		if (doLog && !StringUtils.isBlank(message)) {
			System.out.println(message);
		}
	}

	/**
	 * @param fileNameKey Key of the system property to get the file name from
	 * @return TypeDescription entities from the given file
	 */
	private static SortedMap<String, TypeDescription> extractFromFile(String fileNameKey) {
		// Read the needed file name and check it
		String fileName = System.getProperty(fileNameKey);
		if (StringUtils.isBlank(fileName)) {
			// Use the default name and assume that the file is in the same folder
			fileName =
					ARG_KEY_JAVAFILE.equals(fileNameKey) ? OutputUtil.EXPORT_FILE_NAME : "export_csharp.xml";
		}
		File file = new File(fileName);
		if (!file.exists()) {
			throw new IllegalArgumentException("File '" + fileName + "' does not exist!");
		}
		// Import file content
		return OutputUtil.importFromFile(fileName);
	}

	/**
	 * Export the compare results. If a target path is given a file is written, otherwise only the
	 * console is used.
	 *
	 * @param results List with CompareResult entities
	 * @param targetPath Target path or null
	 * @param asXML Flag if the output should be XML (only if target path is set and a file is
	 *        written)
	 * @param mismatchesOnly Flag if only the compare results should be exported which aren't equal
	 */
	private static void ExportResults(List<CompareResult> results, String targetPath, boolean asXML,
			boolean mismatchesOnly) {
		if (results == null) {
			throw new IllegalArgumentException(
					"Mandatory values for the compare result export are missing!");
		}

		// Filter
		final List<CompareResult> resultsToExport;
		if (mismatchesOnly) {
			resultsToExport = getNonEqualOnly(results);
		}
		else {
			resultsToExport = results;
		}
		// Sort can't be done any more because a result now can have more than one error (which contains
		// the status to
		// be sorted by)

		// Write
		if (StringUtils.isBlank(targetPath)) {
			// Write to console
			log(DECO + "Compare results are:" + DECO + StringUtils.join(resultsToExport, "\n\n"));
		}
		else {
			// Write to file
			if (asXML) {
				String fileName = FilenameUtils.concat(targetPath, COMPARE_RESULTS_FILE_NAME + ".xml");
				Export(resultsToExport, fileName);
			}
			else {
				String fileName = FilenameUtils.concat(targetPath, COMPARE_RESULTS_FILE_NAME + ".txt");
				File file = new File(fileName);
				try {
					FileUtils.writeLines(file, resultsToExport);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * @param sourceResults Source list
	 * @return List with CompareResult entities which have compare errors
	 */
	private static List<CompareResult> getNonEqualOnly(List<CompareResult> sourceResults) {
		List<CompareResult> filteredResults = new ArrayList<>();
		for (CompareResult compareResult : sourceResults) {
			if (compareResult != null && !compareResult.getErrors().isEmpty()) {
				filteredResults.add(compareResult);
			}
		}
		return filteredResults;
	}

	/**
	 * Export the given compare results to an XML file in the given directory.
	 *
	 * @param results Compare results; mandatory (but may be empty)
	 * @param fileName File name of the result file; mandatory
	 */
	private static void Export(List<CompareResult> results, String fileName) {
		if (results == null || StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException("Mandatory values for the export are missing!");
		}

		Element rootNode = new Element("CompareResults");
		Document doc = new Document(rootNode);

		Map<CompareStatus, Element> groupMap = new HashMap<>();
		for (CompareResult result : results) {
			if (result.getErrors().isEmpty()) {
				// No errors -> types are equal -> one single node
				CompareStatus status = CompareStatus.EQUAL;
				Element statusNode = groupMap.get(status);
				if (statusNode == null) {
					statusNode = new Element("GroupedByStatus");
					statusNode.setAttribute(new Attribute("Status", status.getLabel()));
					rootNode.addContent(statusNode);
					groupMap.put(status, statusNode);
				}
				Element resultNode = CreateCompareResultNode(result, null, false);
				statusNode.addContent(resultNode);
			}
			else {
				// There are errors -> one node for each error
				for (CompareError compareError : result.getErrors()) {
					CompareStatus status = compareError.getStatus();
					Element statusNode = groupMap.get(status);
					if (statusNode == null) {
						statusNode = new Element("GroupedByStatus");
						statusNode.setAttribute(new Attribute("Status", status.getLabel()));
						rootNode.addContent(statusNode);
						groupMap.put(status, statusNode);
					}
					Element resultNode = CreateCompareResultNode(result, compareError, false);
					statusNode.addContent(resultNode);
				}
			}
		}

		log("Exporting results to '" + fileName + "'...");
		OutputUtil.writeExportToFile(fileName, doc);
	}

	/**
	 * Create a XML node which represents the given compare result and/or error.
	 *
	 * @param result CompareResult used to fill the node
	 * @param compareError Error or null (if types are equal)
	 * @param showStatus Flag if the status should be used as attribute
	 * @return XML node; never null
	 */
	private static Element CreateCompareResultNode(CompareResult result, CompareError compareError,
			boolean showStatus) {
		if (result == null || result.getFullTypeName() == null) {
			throw new IllegalArgumentException(
					"Mandatory values for creating the result node are missing!");
		}

		Element resultNode = new Element("CompareResult");

		if (showStatus) {
			if (compareError == null) {
				resultNode.setAttribute(new Attribute("Status", CompareStatus.EQUAL.getLabel()));
			}
			else {
				resultNode.setAttribute(new Attribute("Status", compareError.getStatus().getLabel()));
			}
		}
		resultNode.setAttribute(new Attribute("NetTypeName", getName(result.getCsharpType())));
		resultNode.setAttribute(new Attribute("JavaTypeName", getName(result.getJavaType())));
		resultNode.setAttribute(new Attribute("NetSource", getSource(result.getCsharpType())));
		resultNode.setAttribute(new Attribute("JavaSource", getSource(result.getJavaType())));
		if (compareError != null && !StringUtils.isBlank(compareError.getAdditionalInformation())) {
			Element infoNode = new Element("AdditionalInformation");
			infoNode.setText(compareError.getAdditionalInformation());
			resultNode.addContent(infoNode);
		}

		return resultNode;
	}

	/**
	 * Export the given compare results to an Junit test result XML file in the given directory.
	 *
	 * @param results Compare results; mandatory (but may be empty)
	 * @param targetPath Target path of the result file; mandatory
	 */
	private static void ExportAsJunitTestResult(List<CompareResult> results, String targetPath) {
		if (results == null || StringUtils.isBlank(targetPath)) {
			throw new IllegalArgumentException("Mandatory values for the export are missing!");
		}
		String fileName = FilenameUtils.concat(targetPath, TEST_RESULTS_FILE_NAME + ".xml");

		Element rootNode = new Element("testsuites");
		Document doc = new Document(rootNode);

		Map<CompareStatus, Element> groupMap = new HashMap<>();
		for (CompareResult result : results) {
			if (result.getErrors().isEmpty()) {
				// No errors -> types are equal -> one single node
				CompareStatus status = CompareStatus.EQUAL;
				Element statusNode = groupMap.get(status);
				if (statusNode == null) {
					statusNode = new Element("testsuite");
					statusNode.setAttribute(new Attribute("name", status.getLabel()));
					rootNode.addContent(statusNode);
					groupMap.put(status, statusNode);
				}
				Element resultNode = createJunitResultNode(result, null);
				statusNode.addContent(resultNode);
			}
			else {
				// There are errors -> one node for each error
				for (CompareError compareError : result.getErrors()) {
					CompareStatus status = compareError.getStatus();
					Element statusNode = groupMap.get(status);
					if (statusNode == null) {
						statusNode = new Element("testsuite");
						statusNode.setAttribute(new Attribute("name", status.getLabel()));
						rootNode.addContent(statusNode);
						groupMap.put(status, statusNode);
					}
					Element resultNode = createJunitResultNode(result, compareError);
					statusNode.addContent(resultNode);
				}
			}
		}

		log("Exporting results (as JUnit test results) to '" + fileName + "'...");
		OutputUtil.writeExportToFile(fileName, doc);
	}

	/**
	 * @param result CompareResult
	 * @param compareError CompareError (null if equal)
	 * @return XML element
	 */
	private static Element createJunitResultNode(CompareResult result, CompareError compareError) {
		CompareStatus status = compareError == null ? CompareStatus.EQUAL : compareError.getStatus();
		Element resultNode = new Element("testcase");
		resultNode.setAttribute(new Attribute("name", result.getFullTypeName()));
		if (!CompareStatus.EQUAL.equals(status)) {
			if (INFO_STATES.contains(status)) {
				resultNode.addContent(new Element("skipped"));
			}
			String errorElementName = "error";
			if (WARNING_STATES.contains(status)) {
				errorElementName = "failure";
			}
			Element errorNode = new Element(errorElementName);
			if (compareError != null && !StringUtils.isBlank(compareError.getAdditionalInformation())) {
				resultNode.setAttribute(new Attribute("message", compareError.getAdditionalInformation()));
			}
			resultNode.addContent(errorNode);
		}
		return resultNode;
	}

	/**
	 * Export the given compare results to an Checkstyle test result XML file in the given directory.
	 *
	 * @param results Compare results; mandatory (but may be empty)
	 * @param targetPath Target path of the result file; mandatory
	 */
	private static void ExportAsCheckstyleTestResult(List<CompareResult> results, String targetPath) {
		if (results == null || StringUtils.isBlank(targetPath)) {
			throw new IllegalArgumentException("Mandatory values for the export are missing!");
		}
		String fileName = FilenameUtils.concat(targetPath, CHECKSTYLE_RESULTS_FILE_NAME + ".xml");

		Element rootNode = new Element("checkstyle");
		rootNode.setAttribute(new Attribute("version", "5.6"));
		Document doc = new Document(rootNode);

		for (CompareResult result : results) {
			Element fileNode = new Element("file");

			String source = getCheckstyleTypeSource(result);
			fileNode.setAttribute(new Attribute("name", source));

			// If there are no errors nothing has to be added
			for (CompareError compareError : result.getErrors()) {
				CompareStatus status = compareError.getStatus();
				if (!CompareStatus.EQUAL.equals(status)) {
					Element errorNode = new Element("error");
					String severity = "error";
					if (INFO_STATES.contains(status)) {
						severity = "info";
					}
					if (WARNING_STATES.contains(status)) {
						severity = "warning";
					}
					errorNode.setAttribute(new Attribute("severity", severity));
					if (!StringUtils.isBlank(compareError.getAdditionalInformation())) {
						errorNode
								.setAttribute(new Attribute("message", compareError.getAdditionalInformation()));
					}
					else {
						errorNode.setAttribute(new Attribute("message", status.getLabel()));
					}
					errorNode.setAttribute(new Attribute("source", getCheckstyleStatusSource(status)));

					fileNode.addContent(errorNode);
				}
			}
			rootNode.addContent(fileNode);
		}

		log("Exporting results (as Checkstyle test results) to '" + fileName + "'...");
		OutputUtil.writeExportToFile(fileName, doc);
	}

	/**
	 * @param result CompareResult; mandatory
	 * @return Source
	 */
	private static String getCheckstyleStatusSource(CompareStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("Mandatory status is missing!");
		}
		return "com.koch.compare." + status.name();
	}

	/**
	 * @param result CompareResult; mandatory
	 * @return Source
	 */
	private static String getCheckstyleTypeSource(CompareResult result) {
		if (result == null) {
			throw new IllegalArgumentException(
					"Mandatory value to get the Checkstyle source is missing!");
		}
		String netTypeName = getName(result.getCsharpType());
		String javaTypeName = getName(result.getJavaType());
		String netSource = getSource(result.getCsharpType());
		String javaSource = getSource(result.getJavaType());
		javaSource = AMBETH_VERSION.matcher(javaSource).replaceAll("");
		String source = StringUtils.EMPTY;
		if (!StringUtils.isBlank(netTypeName)) {
			source = "net:" + netTypeName;
			if (!StringUtils.isBlank(netSource)) {
				source += " from " + netSource;
			}
		}
		else {
			if (!StringUtils.isBlank(netSource)) {
				source = "net:" + netSource;
			}
		}
		if (!StringUtils.isBlank(javaTypeName)) {
			if (!StringUtils.isBlank(source)) {
				source += ";";
			}
			source += "java:" + javaTypeName;
			if (!StringUtils.isBlank(javaSource)) {
				source += " from " + javaSource;
			}
		}
		else {
			if (!StringUtils.isBlank(javaSource)) {
				if (!StringUtils.isBlank(source)) {
					source += ";";
				}
				source = "java:" + javaSource;
			}
		}
		return source;
	}

	/**
	 * Get the type name from the given TypeDescription.
	 *
	 * @param typeDescription TypeDescription to get the information from
	 * @return Type name
	 */
	private static String getName(TypeDescription typeDescription) {
		if (typeDescription == null) {
			return StringUtils.EMPTY;
		}
		return typeDescription.getFullTypeName();
	}

	/**
	 * Get the source (short form) from the given TypeDescription.
	 *
	 * @param typeDescription TypeDescription to get the information from
	 * @return Source
	 */
	private static String getSource(TypeDescription typeDescription) {
		if (typeDescription == null) {
			return StringUtils.EMPTY;
		}
		String[] sourceParts = StringUtils.split(typeDescription.getSource(), "/\\");
		return sourceParts[sourceParts.length - 1];
	}

}
