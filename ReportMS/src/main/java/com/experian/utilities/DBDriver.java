package com.experian.utilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.experian.utilities.HashMapUtility.DataInputMap;
import com.experian.utilities.HashMapUtility.KeywordMap;
import com.experian.utilities.HashMapUtility.RunMap;
import com.experian.utilities.HashMapUtility.TestCaseMap;
import com.experian.utilities.HashMapUtility.TestRun;
import com.experian.utilities.HashMapUtility.TestSuiteMap;

/*Class for Database Operations*/
public class DBDriver {

	/* Connection Declaration */
	Connection connection;
	/* Classes and HashMap Declarations */
	TestSuiteMap testsuiteMap;
	TestCaseMap testcaseMap;
	List<TestSuiteMap> testSuiteMapList;
	List<TestCaseMap> testcaseMapList;

	private HashMap<Long, List<TestSuiteMap>> testSuiteHashMap;
	private HashMap<Long, List<TestCaseMap>> testcaseHashMap;
	KeywordMap keywordmap;
	List<KeywordMap> keywordMapList;
	private HashMap<Long, List<KeywordMap>> keywordHashMap;
	DataInputMap dataInputMap;
	List<DataInputMap> dataInputMapList;
	private HashMap<Long, List<DataInputMap>> dataInputHashMap;
	int runID = 1;
	Boolean isSuite_Executed = false;
	List<String> headerNamesList;
	List<String> dbusercredential;
	CallableStatement stmt = null;
	String query = null;

	/* Constructor */
	public DBDriver() throws CustomException {
		// setupJDBCConnection();
	}

	/* Set MySqlDatabase Connection */
	private void setupJDBCConnection() throws CustomException {
		try {
//			parseDBconfigxml();
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rtp", "root", "u23219");
          //	Class.forName("com.mysql.jdbc.Driver");
       //	connection = DriverManager.getConnection(dbusercredential.get(0), dbusercredential.get(1), dbusercredential.get(2));
//			if (connection != null) {
//				 System.out.println(" Connection established");
//			}
		} catch (Exception e) {
//			 System.out.println("\n Lithin Connection established Error");
			throw new CustomException("SqlException or IOException has thrown", e);
		}
	}

	/* Parse DB Username and Password from DBConfig xml */
	private void parseDBconfigxml() throws CustomException {
		Document doc;
		try {

			String filePath = System.getProperty("user.dir");
			System.out.println("current dir = " + filePath);

			
			filePath = filePath + "\\DBConfig\\dbconfig.xml";
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Path paths = Paths.get(filePath);
			System.out.println(paths);
			if (Files.exists(paths)) {
				doc = db.parse(filePath);

				doc.getDocumentElement().normalize();
				if (doc != null) {
					dbusercredential = new ArrayList<String>();
					Node node = doc.getFirstChild();

					NodeList nl = node.getChildNodes();
					for (int i = 0; i < nl.getLength(); i++) {
						node = nl.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							NodeList nl2 = node.getChildNodes();

							for (int i2 = 0; i2 < nl2.getLength(); i2++) {
								node = nl2.item(i2);
								dbusercredential.add(node.getNodeValue());
							}
						}
					}
				}
			}
		} catch (Exception ex) {

			throw new CustomException("XML Exception ", ex);
		}
	}

	public RunMap ReadRunMaster(long runid, String projectname) throws SQLException, CustomException {
		RunMap objRunmap = new RunMap();
		try {
			objRunmap.setId(runid);
			objRunmap.setProjectName(projectname);
			objRunmap = readTestSuiteMaster(objRunmap, runid);
		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
		return objRunmap;

	}

	public RunMap readTestSuiteMaster(RunMap objRunmap, long runid) throws CustomException, SQLException {
		try {
			testSuiteMapList = new ArrayList<TestSuiteMap>();
			testSuiteHashMap = new HashMap<Long, List<TestSuiteMap>>();
			setupJDBCConnection();
			query = "{CALL get_suite_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("run_Id", runid);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testsuiteMap = new TestSuiteMap();
				testsuiteMap.setId(rs.getLong("id"));
				testsuiteMap.setTestsuiteName(rs.getString("testsuite_name"));
				testsuiteMap.setTestSuiteDesc(rs.getString("testsuite_desc"));
				LoadTestCaseintoHashMapBasedonEachTestSuite(testsuiteMap, runid);

				testSuiteMapList.add(testsuiteMap);
				if (!testSuiteMapList.isEmpty()) {
					testSuiteHashMap.put(runid, testSuiteMapList);
				}
				objRunmap.setHashmapTestSuite(testSuiteHashMap);

			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
		return objRunmap;

	}

	public void LoadTestCaseintoHashMapBasedonEachTestSuite(TestSuiteMap testsuiteMap, long runId)
			throws CustomException {
		try {
			int tc_Count = 1;
			testcaseMapList = new ArrayList<TestCaseMap>();
			testcaseHashMap = new HashMap<Long, List<TestCaseMap>>();
			query = "{CALL get_testcase_master(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("suiteId", testsuiteMap.getId());
			stmt.setLong("runId", runId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testcaseMap = new TestCaseMap();
				testcaseMap.setTestCount(tc_Count);
				testcaseMap.setId(rs.getLong("id"));
				testcaseMap.setTestCaseDesc(rs.getString("testcase_desc"));
				testcaseMap.setTestCaseName(rs.getString("testcase_name"));
				System.out.println(rs.getString("testcase_name"));
				testcaseMap.setTestExecutionFlag(rs.getBoolean("test_exec_flag"));
				testcaseMap.setTestBrowser(rs.getString("browser"));
				testcaseMap.setTestcase_ExecutionId(rs.getLong("testcase_execution_id"));
				testcaseMap.setIsExecute(false);
				// if (testsuiteMap.getTestRun().getRunId() != 0) {
				LoadTestExecutionDataintoHashMapBasedOnEachRunId(runId, testcaseMap);
				List<String> scenarioLogs = LoadScenarioLogsintoHashmapBasedonEachTestCase(testcaseMap.getId());
				testcaseMap.setTestcaseLogs(scenarioLogs);
				// }
				LoadKeywordintoHashMapBasedonEachTestCase(testsuiteMap, testcaseMap);
				testcaseMapList.add(testcaseMap);
				if (!testcaseMapList.isEmpty()) {
					testcaseHashMap.put(testsuiteMap.getId(), testcaseMapList);
				}
				testsuiteMap.setHashmapTestCase(testcaseHashMap);
				tc_Count = tc_Count + 1;
			}

			tc_Count = tc_Count - 1;
			testsuiteMap.setTotalTestCount(tc_Count);

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}
	}

	/*
	 * Read TestSuiteMaster Table
	 */

	// --to do-------pass excel data sheet name corresponding to suite

	public TestSuiteMap readTestSuiteMaster(long testSuiteId, TestSuiteMap testsuiteMap, long runId)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL get_suite_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("suiteId", testSuiteId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testsuiteMap.setId(rs.getLong("id"));
				testsuiteMap.setTestsuiteName(rs.getString("testsuite_name"));
				testsuiteMap.setTestSuiteDesc(rs.getString("testsuite_desc"));
				// testsuiteMap.setTestBrowser(rs.getString("test_browser"));
				// testsuiteMap.setTestUrl(rs.getString("test_url"));
				LoadTestCaseintoHashMapBasedonEachTestSuite(testsuiteMap, runId);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return testsuiteMap;
	}

	public void CloseDBConnection() throws CustomException {
		try {
			connection.close();
		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}
	}

	/*
	 * Read KeywordMaster Table and Load Keyword into HashMap Based on Each TestCase
	 */
	public void LoadKeywordintoHashMapBasedonEachTestCase(TestSuiteMap testsuitemap, TestCaseMap testcaseMap)
			throws CustomException {
		try {
			keywordMapList = new ArrayList<KeywordMap>();
			keywordHashMap = new HashMap<Long, List<KeywordMap>>();
			query = "{CALL get_keyword_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("testcaseId", testcaseMap.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				keywordmap = new KeywordMap();
				keywordmap.setKeywordId(rs.getInt("id"));
				keywordmap.setKeyword(rs.getString("test_keyword_name"));
				keywordmap.setApplicationType(rs.getString("technology"));
				keywordmap.setFolderName(testsuitemap.getTestsuiteName() + "-" + testcaseMap.getTestCaseName() + "-"
						+ new Timestamp((new Date()).getTime()));
				if (testcaseMap.getTestcase_ExecutionId() != 0) {
					LoadKeywordExecutionDataintoHashMapBasedOnEachTestExecutionId(testcaseMap.getTestcase_ExecutionId(),
							keywordmap);
					LoadKeywordInputsBasedOnKeywordId(keywordmap);
				}
				LoadDataInputintoHashMapBasedonEachKeyword(keywordmap);
				keywordMapList.add(keywordmap);
				if (!keywordMapList.isEmpty()) {
					keywordHashMap.put(testcaseMap.getId(), keywordMapList);
				}
				testcaseMap.setKeywordHashMap(keywordHashMap);
			}

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}
	}

	private void LoadKeywordInputsBasedOnKeywordId(KeywordMap keywordmap) throws CustomException {
		HashMap<String, String> inputFiles = new HashMap<String, String>();
		try {
			query = "{CALL get_keyword_inputs(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("keywordIdtoTest", keywordmap.getKeywordId());
			ResultSet rs = stmt.executeQuery();
			String fileType, filename;
			while (rs.next()) {
				fileType = rs.getString("fileType");
				filename = rs.getString("filename");
				inputFiles.put(fileType, filename);
				keywordmap.setInputFiles(inputFiles);
			}

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}
	}

	/*
	 * Read DataInputMaster Table and Load DataInput into HashMap Based on Each
	 * Keyword
	 */
	private void LoadDataInputintoHashMapBasedonEachKeyword(KeywordMap keywordMap) throws CustomException {
		try {
			query = "{CALL get_testdata_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("keywordId", keywordMap.getKeywordId());
			ResultSet rs = stmt.executeQuery();
			dataInputMapList = new ArrayList<DataInputMap>();
			dataInputHashMap = new HashMap<Long, List<DataInputMap>>();
			headerNamesList = new ArrayList<String>();
			String headerName;
			while (rs.next()) {
				dataInputMap = new DataInputMap();
				dataInputMap.setDataInputValue(rs.getString("testdata_field_value"));
				headerName = rs.getString("testdata_field_name");
				headerNamesList.add(headerName);
				dataInputMapList.add(dataInputMap);
			}
			if (!dataInputMapList.isEmpty()) {
				dataInputHashMap.put(keywordMap.getKeywordId(), dataInputMapList);
			}
			keywordmap.setDataInputHashMap(dataInputHashMap);
			keywordmap.setDataInputHeaderNames(headerNamesList);

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}
	}

	/* Update TestRun table to DB */
	public void updateRunTableStatus(String runStatus, long runId) throws CustomException, SQLException {

		try {
			setupJDBCConnection();
			// runID = runId[0];
			query = "{CALL update_run_status(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("runStatus", runStatus);
			stmt.setLong("runId", runId);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	/* Update Test case Results to DB */
	public void updateTestResultToDB(TestCaseMap testCaseMap, int runID, long suiteId)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			int testcaseExecutnId = 1;

			query = "{CALL update_testcase_execution(?,?,?)}";
			stmt = connection.prepareCall(query);
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String currentTime = sdf.format(testCaseMap.getTestExecutionTime());

			// stmt.setString("executionTime", currentTime);
			stmt.setString("executionStatus", testCaseMap.getTestExecutionStatus());
			stmt.setLong("runId", runID);
			stmt.setLong("testexecutionId", testCaseMap.getTestcase_ExecutionId());
			ResultSet rs = stmt.executeQuery();

			query = "{CALL get_testcase_execution_id()}";
			stmt = connection.prepareCall(query);
			rs = stmt.executeQuery();
			while (rs.next()) {
				testcaseExecutnId = rs.getInt("testcase_execution_id");
			}

			for (List<KeywordMap> valueKeywordMap : testCaseMap.getKeywordHashMap().values()) {
				for (KeywordMap keyword : valueKeywordMap) {

					query = "{CALL insert_keyword_execution(?,?,?,?,?,?,?)}";
					stmt = connection.prepareCall(query);
					currentTime = sdf.format(keyword.getKeywordExecutionTime());
					stmt.setString("executionTime", currentTime);
					stmt.setString("failedDetails", ((keyword.getFailedScreenshotDet() == null) ? null
							: keyword.getFailedScreenshotDet().replaceAll("'", "''")));
					stmt.setString("screenshotName", keyword.getPassOrFailedScreenshotName());
					stmt.setString("keywordStatus", keyword.getKeywordExecutionStatus());
					stmt.setLong("testcaseExecutionId", testCaseMap.getTestcase_ExecutionId());
					stmt.setString("keywordlogs", keyword.getLogs());
					stmt.setLong("keywordId", keyword.getKeywordId());
					// stmt.setLong("keyword_execution_id", keyword.getKeywordId());
					rs = stmt.executeQuery();
				}
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	/* Read Test Execution Table Values Into TestCaseMap Class */
	private void LoadTestExecutionDataintoHashMapBasedOnEachRunId(long runId, TestCaseMap testcaseMap)
			throws CustomException {
		try {
			query = "{CALL get_testcase_execution(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runId", runId);
			stmt.setLong("testcaseId", testcaseMap.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testcaseMap.setTestcase_ExecutionId(rs.getInt("testcase_execution_id"));
				testcaseMap.setTestExecutionStatus(rs.getString("testcase_execution_status"));
				testcaseMap.setTestExecutionTime(rs.getDate("testcase_execution_time"));

				// System.out.println(testcaseMap.getTestExecutionTime());
			}

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}

	}

	/* Read Keyword Execution Table Values Into Keyword Class */
	private void LoadKeywordExecutionDataintoHashMapBasedOnEachTestExecutionId(long testExecutionId,
			KeywordMap keywordmap) throws CustomException {

		try {
			query = "{CALL get_keyword_execution(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("keywordId", keywordmap.getKeywordId());
			stmt.setLong("testcaseExecutionId", testExecutionId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				keywordmap.setKeywordExecutionStatus(rs.getString("keyword_status"));
				keywordmap.setKeywordExecutionTime(rs.getDate("keyword_executed_on"));
				keywordmap.setPassOrFailedScreenshotName(rs.getString("keyword_screenshot_name"));
				keywordmap.setFailedScreenshotDet(rs.getString("keyword_failed_details"));
				keywordmap.setLogs((rs.getString("keyword_logs")));
			}

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}

	}

	/* Add Input Files To Database */
	public void AddInputsToDatabase(long keywordId, String fileType, String filename)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL insert_keyword_inputs(?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("inputfiletype", fileType);
			stmt.setString("inputfilename", filename);
			stmt.setLong("keywordId", keywordId);
			ResultSet rs = stmt.executeQuery();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
	}

	/* Read TestSuite and run Ids from Run Table */
	public List<List<Long>> ReadTestSuitesFromRunTable(String runStatus) throws CustomException, SQLException {
		List<Long> testSuiteIds = new ArrayList<Long>(), runIds = new ArrayList<Long>();
		List<List<Long>> Ids = new ArrayList<List<Long>>();
		try {
			setupJDBCConnection();
			long testSuiteId, runId;
			query = "{CALL get_run(?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("runstatus", runStatus);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testSuiteId = rs.getLong("testsuite_master_id");
				runId = rs.getInt("run_id");
				testSuiteIds.add(testSuiteId);
				runIds.add(runId);
			}
			Ids.add(testSuiteIds);
			Ids.add(runIds);
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
		return Ids;
	}

	/* Read TestSuite and run Ids from Run Table */
	public List<Long> ReadRunIdsFromRunTable(String runStatus) throws CustomException, SQLException {
		List<Long> testSuiteIds = new ArrayList<Long>(), runIds = new ArrayList<Long>();
		List<Long> Ids = new ArrayList<Long>();
		try {
			setupJDBCConnection();
			long testSuiteId, runId;
			query = "{CALL get_run(?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("runstatus", runStatus);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				// testSuiteId = rs.getLong("testsuite_master_id");
				runId = rs.getInt("run_id");
				// testSuiteIds.add(testSuiteId);
				runIds.add(runId);
			}
			// Ids.add(testSuiteIds);
			// Ids.add(runIds);
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
		return runIds;
	}

	/* Method to Add Report File Path to Database */
	public void UpdateFileNameToDB(String reportFilePath, Long run_id) throws CustomException, SQLException {

		try {
			setupJDBCConnection();

			// System.out.println("Report File Path is " + reportFilePath);
			// System.out.println("Run Id is " + run_id);
			query = "{CALL update_run_reportlocation(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("reportlocation", reportFilePath);
			stmt.setLong("runId", run_id);
			ResultSet rs = stmt.executeQuery();

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
	}

	public long ReadSuiteIdBasedonRunId(long run_id) throws CustomException, SQLException {
		long testsuiteid = 0;
		try {
			setupJDBCConnection();
			query = "{CALL get_suite_id(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runId", run_id);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testsuiteid = rs.getLong("testsuite_master_id");
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
		return testsuiteid;
	}

	public String ReadProjectNameBasedOnRunId(long runid) throws CustomException {
		String project_name = null;
		try {
			setupJDBCConnection();
			query = "{CALL get_project_name(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runId", runid);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				project_name = rs.getString("project_name");
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return project_name;
	}

	public void updateRunTableStatus(String runstatus, String statusMessage, long runId)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL update_run_statusmessage(?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("statusMessage", statusMessage);
			stmt.setString("runStatus", runstatus);
			stmt.setLong("runId", runId);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	/* Read API Details based on Keyword */
	/* Project Specific Method */
	public void GetAPIRequestDetails(HashMap<String, String> inputList, String sheetName, int count, String projectName)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			String keywordName = sheetName + count;
			String keywordid = "";
			String key = "";
			String value = "";
			query = "{CALL get_keyword_id(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("keyword_name", keywordName);
			stmt.setString("projectname", projectName);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				keywordid = rs.getString("id");
			}
			query = "{CALL get_testdata_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("keywordId", Long.parseLong(keywordid));
			rs = stmt.executeQuery();

			while (rs.next()) {
				key = rs.getString("testdata_field_name");
				value = rs.getString("testdata_field_value");
				inputList.put(key, value);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	/* Delete Keyword Input Details based on keyword Id */
	public void DeleteKeywordInputs(Integer keywordId) throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL delete_keyword_inputs(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("keywordIdtoTest", keywordId);
			ResultSet rs = stmt.executeQuery();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	///// TAAF Enhancement /////

	public Long AddTestExecutionEntries(String projectId, List<String> suiteIds) throws CustomException, SQLException {
		return AddRunTableEntry(projectId, suiteIds);

	}

	private Long AddRunTableEntry(String projectId, List<String> suiteIds) throws CustomException, SQLException {
		Long runId = null;
		try {
			setupJDBCConnection();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (String suiteId : suiteIds) {

				query = "{CALL insert_run(?,?,?,?)}";
				stmt = connection.prepareCall(query);
				stmt.setLong("projectId", 3681);
				stmt.setString("runStatus", "NoRun");
				Date date = new Date();
				String currentTime = sdf.format(date);
				stmt.setString("rundate", currentTime);
				stmt.setLong("suiteId", Long.parseLong(suiteId));
				ResultSet rs = stmt.executeQuery();

				/*
				 * query = "{CALL get_run_id()}"; stmt = connection.prepareCall(query);
				 * 
				 * rs = stmt.executeQuery();
				 */
				while (rs.next()) {
					runId = rs.getLong("run_id");
				}
				rs.close();
				stmt.close();

				/*
				 * testcaseMapList = new ArrayList<TestCaseMap>(); query =
				 * "{CALL get_testcaseid(?,?)}"; stmt = connection.prepareCall(query);
				 * stmt.setLong("suiteId", Long.parseLong(suiteId)); stmt.setLong("runId",
				 * runId); rs = stmt.executeQuery(); while (rs.next()) { testcaseMap = new
				 * TestCaseMap(); testcaseMap.setId(rs.getLong("id"));
				 * testcaseMapList.add(testcaseMap); } for (TestCaseMap testcasemap :
				 * testcaseMapList) {
				 * 
				 * query = "{CALL insert_testcase_execution(?,?,?,?)}"; stmt =
				 * connection.prepareCall(query); stmt.setString("executionTime", currentTime);
				 * stmt.setLong("runId", runId); stmt.setLong("testcaseId",
				 * testcasemap.getId()); stmt.setLong("suiteId", Long.parseLong(suiteId));
				 * stmt.executeQuery(); }
				 */
			}
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
			stmt.close();
		}
		return runId;
	}

	/****************************************
	 * BDD Check
	 * 
	 * @throws CustomException
	 * @throws SQLException
	 ************************************************************/

	public long AddSuite(TestSuiteMap testsuitemap, long projectid) throws CustomException, SQLException {
		try {
			setupJDBCConnection();

			query = "{CALL insert_suite(?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("suitename", testsuitemap.getTestsuiteName());
			stmt.setString("suitedesc", testsuitemap.getTestSuiteDesc());
			stmt.setLong("projectId", projectid);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				testsuitemap.setId(rs.getLong("suiteId"));
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return testsuitemap.getId();
	}

	public void AddTestCase(long id, TestCaseMap testcaseMap) throws CustomException, SQLException {
		try {
			setupJDBCConnection();

			query = "{CALL insert_testcase(?,?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("testcasebrowser", testcaseMap.getTestBrowser());
			stmt.setString("testcasedesc", testcaseMap.getTestCaseDesc());
			stmt.setBoolean("testcaseflag", testcaseMap.getTestExecutionFlag());
			stmt.setString("testcasename", testcaseMap.getTestCaseName());
			stmt.setLong("suiteid", id);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				testcaseMap.setId(rs.getLong("testcaseId"));
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	public long AddKeyword(String keywordDet, long testcaseId) throws CustomException, SQLException {
		long keywordId = 0;
		try {
			setupJDBCConnection();

			query = "{CALL insert_keyword_master(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("keywordname", keywordDet);
			stmt.setLong("testcasemasterid", testcaseId);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				keywordId = rs.getLong("keywordId");
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
		return keywordId;
	}

	public void AddTestExecution(long runId, TestCaseMap testcaseMap, long suiteId)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL insert_testcase_execution(?,?,?,?,?)}";
			stmt = connection.prepareCall(query);
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			stmt.setString("executionTime", sdf.format(testcaseMap.getTestExecutionTime()));
			stmt.setString("executionStatus", testcaseMap.getTestExecutionStatus());
			stmt.setLong("runId", runId);
			stmt.setLong("testcaseId", testcaseMap.getId());
			stmt.setLong("suiteId", suiteId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testcaseMap.setTestcase_ExecutionId(rs.getLong("testcaseexecId"));
			}
			rs.close();
			stmt.close();

		} catch (

		Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
	}

	public long AddEntriesToRunTable(long projectId, String run_status) throws CustomException, SQLException {
		long runId = 0;
		try {
			setupJDBCConnection();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			query = "{CALL insert_run(?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("projectId", projectId);
			stmt.setString("runStatus", run_status);
			Date date = new Date();
			String currentTime = sdf.format(date);
			stmt.setString("rundate", currentTime);
			stmt.setLong("suiteId", (Long) null);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				runId = rs.getLong("run_id");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return runId;

	}

	/* Insert entries to Feature Master */
	public long AddEntriesToFeatureTable(String featureName, long runId) throws CustomException, SQLException {
		long feature_Id = 0;
		try {
			setupJDBCConnection();
			query = "{CALL insert_feature(?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("featurename", featureName);
			stmt.setString("featuredesc", featureName);
			stmt.setLong("runId", runId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				feature_Id = rs.getLong("featureId");
			}
			rs.close();
			stmt.close();

			System.out.println("Feature Master Entry added");
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return feature_Id;

	}

	/* Insert entries to Feature Master */
	public long AddEntriesToScenarioTable(String scenarioName, String browser, long featureId)
			throws CustomException, SQLException {
		long scenario_Id = 0;
		try {
			setupJDBCConnection();
			query = "{CALL insert_scenario(?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("scenarioname", scenarioName);
			stmt.setString("scenariodesc", scenarioName);
			stmt.setString("scenariobrowser", browser);
			stmt.setLong("featureid", featureId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				scenario_Id = rs.getLong("scenarioId");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return scenario_Id;

	}

	/* Insert entries to Feature Master */
	public long AddEntriesToStepTable(String stepName, long scenarioId) throws CustomException, SQLException {
		long step_Id = 0;
		try {
			setupJDBCConnection();
			query = "{CALL insert_step(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("stepname", stepName);
			stmt.setLong("scenarioId", scenarioId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				step_Id = rs.getLong("testcaseId");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			System.out.println(stepName.length());
			byte[] buffer = new byte[stepName.length()];
			buffer = stepName.getBytes();
			System.out.println(buffer.length);
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);

		} finally {
			connection.close();
		}

		return step_Id;

	}

	public long AddEntriesToScenarioExecutionTable(long runId, long featureId, long scenarioId)
			throws CustomException, SQLException {
		long scenarioExec_Id = 0;
		try {
			setupJDBCConnection();
			query = "{CALL insert_scenario_execution(?,?)}";
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			query = "{CALL insert_scenario_execution(?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("executionTime", timeStamp);
			stmt.setLong("runId", runId);
			stmt.setLong("featureId", featureId);
			stmt.setLong("scenarioId", scenarioId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				scenarioExec_Id = rs.getLong("scenarioexecId");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return scenarioExec_Id;

	}

	public long AddEntriesToStepExecutionTable(long scenarioExecId, long stepId, String status)
			throws CustomException, SQLException {
		long stepExec_Id = 0;
		try {

			setupJDBCConnection();

			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			query = "{CALL insert_step_execution(?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("executionTime", timeStamp);
			stmt.setString("keywordStatus", status);
			stmt.setLong("testcaseExecutionId", scenarioExecId);
			stmt.setLong("keywordId", stepId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				stepExec_Id = rs.getLong("stepexecId");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return stepExec_Id;

	}

	public void UpdateEntriesToStepExecutionTable(long stepId, String status, String errorMessagePath)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL update_step_execution_status(?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("stepexecutionStatus", status);
			stmt.setString("steperrormessage", errorMessagePath);
			stmt.setLong("stepId", stepId);

			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	public void UpdateTestExecutionStatus(long scenarioId, String resultStatus) throws CustomException, SQLException {

		try {
			setupJDBCConnection();
			query = "{CALL update_scenario_execution_status(?,?)}";

			stmt = connection.prepareCall(query);
			stmt.setLong("scenarioId", scenarioId);
			stmt.setString("resultStatus", resultStatus);
			ResultSet rs = stmt.executeQuery();

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	/* Read Feature Master */
	public List<TestSuiteMap> readFeatureMaster(long runId, String projectName) throws CustomException, SQLException {

		try {
			setupJDBCConnection();
			testSuiteMapList = new ArrayList<TestSuiteMap>();
			testSuiteHashMap = new HashMap<Long, List<TestSuiteMap>>();
			query = "{CALL get_feature_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runId", runId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testsuiteMap = new TestSuiteMap();
				testsuiteMap.setId(rs.getLong("id"));
				testsuiteMap.setTestsuiteName(rs.getString("feature_name"));
				testsuiteMap.setTestSuiteDesc(rs.getString("feature_desc"));
				TestRun objTestRun = new TestRun();
				objTestRun.setRunId(runId);
				testsuiteMap.setTestRun(objTestRun);
				LoadScenariointoHashMapBasedonEachFeature(testsuiteMap, runId);
				testSuiteMapList.add(testsuiteMap);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return testSuiteMapList;
	}

	/* Read Scenario Master */
	private void LoadScenariointoHashMapBasedonEachFeature(TestSuiteMap testsuiteMap, long runId)
			throws CustomException {
		try {
			int tc_Count = 1;
			testcaseMapList = new ArrayList<TestCaseMap>();
			testcaseHashMap = new HashMap<Long, List<TestCaseMap>>();
			query = "{CALL get_scenario_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("featureId", testsuiteMap.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testcaseMap = new TestCaseMap();
				testcaseMap.setTestCount(tc_Count);
				testcaseMap.setId(rs.getLong("id"));
				testcaseMap.setTestCaseDesc(rs.getString("scenario_desc"));
				testcaseMap.setTestCaseName(rs.getString("scenario_name"));
				testcaseMap.setTestBrowser(rs.getString("scenario_browser"));
				testcaseMap.setIsExecute(false);
				if (testsuiteMap.getTestRun().getRunId() != 0) {
					LoadTestExecutionDataintoHashMapBasedOnEachRunId(testsuiteMap.getTestRun().getRunId(), testcaseMap);
				}
				LoadStepsintoHashMapBasedonEachTestCase(testsuiteMap, testcaseMap);

				List<String> scenarioLogs = LoadScenarioLogsintoHashmapBasedonEachTestCase(testcaseMap.getId());
				testcaseMap.setTestcaseLogs(scenarioLogs);
				testcaseMapList.add(testcaseMap);
				if (!testcaseMapList.isEmpty()) {
					testcaseHashMap.put(testsuiteMap.getId(), testcaseMapList);
				}
				testsuiteMap.setHashmapTestCase(testcaseHashMap);
				tc_Count = tc_Count + 1;
			}

			tc_Count = tc_Count - 1;
			testsuiteMap.setTotalTestCount(tc_Count);

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}

	}

	private void LoadStepsintoHashMapBasedonEachTestCase(TestSuiteMap testsuiteMap, TestCaseMap testcaseMap)
			throws CustomException {
		try {
			keywordMapList = new ArrayList<KeywordMap>();
			keywordHashMap = new HashMap<Long, List<KeywordMap>>();
			query = "{CALL get_step_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("scenarioId", testcaseMap.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				keywordmap = new KeywordMap();
				keywordmap.setKeywordId(rs.getInt("id"));
				keywordmap.setKeyword(rs.getString("step_name"));
				if (testcaseMap.getTestcase_ExecutionId() != 0) {
					LoadKeywordExecutionDataintoHashMapBasedOnEachTestExecutionId(testcaseMap.getTestcase_ExecutionId(),
							keywordmap);

				}
				keywordMapList.add(keywordmap);
				if (!keywordMapList.isEmpty()) {
					keywordHashMap.put(testcaseMap.getId(), keywordMapList);
				}
				testcaseMap.setKeywordHashMap(keywordHashMap);
			}

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		}

	}

	/* Read TestApproach Based on Run Id from Run Table */

	public String ReadTestApproachfromRunTable(long runId) throws CustomException, SQLException {

		String testapproach = null;
		try {
			setupJDBCConnection();
			
			query = "{CALL get_run_testapproach(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runid", runId);
			System.out.println("RunId is " + runId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				testapproach = rs.getString("testapproach");
				System.out.println("TestApproach is " + testapproach);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
//			System.out.println("ReadTestApproachfromRunTable Exception caught "+e.getMessage());
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
//			System.out.println("SS1 "+connection);
			connection.close();
		}
		return testapproach;

	}

	public boolean CheckFeatureNameExistsInDatabase(String feature_name, long runId)
			throws CustomException, SQLException {
		boolean is_feature_exists = false;
		try {
			setupJDBCConnection();

			query = "{CALL check_feature_exists(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("featurename", feature_name);
			stmt.setLong("runId", runId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				is_feature_exists = true;
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return is_feature_exists;
	}

	public boolean CheckProjectExistsInDatabase(String project_name) throws CustomException, SQLException {
		boolean is_project_exists = false;
		try {
			setupJDBCConnection();

			query = "{CALL check_project_exists(?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("projectname", project_name);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				is_project_exists = true;
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return is_project_exists;
	}

	public long AddEntriesToRunTable(long projectId, String run_status, String testapproach)
			throws CustomException, SQLException {
		long runId = 0;
		try {
			setupJDBCConnection();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String currentTime = sdf.format(new Date());

			query = "{CALL insert_run(?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("projectId", projectId);
			stmt.setString("runStatus", run_status);
			stmt.setString("rundate", currentTime);
			stmt.setString("testApproach", testapproach);
			System.out.println(stmt);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				runId = rs.getLong("run_id");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return runId;

	}

	public void AddEntriesToMapRunIdSuiteIdtable(Long runid, Long testsuiteId) throws CustomException, SQLException {
		try {
			setupJDBCConnection();

			query = "{CALL insert_map_runid_suiteid(?,?)}";
			stmt = connection.prepareCall(query);
			System.out.println(stmt);
			stmt.setLong("runid", runid);
			stmt.setLong("testsuiteid", testsuiteId);
			System.out.println(stmt);
			ResultSet rs = stmt.executeQuery();

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	public List<Long> GetTestcaseIdfromTestcaseMaster(Long testsuiteId) throws CustomException, SQLException {
		List<Long> testcaseid = new ArrayList<Long>();

		// testcaseid = null;
		try {

			setupJDBCConnection();
			System.out.println(testsuiteId);
			// String testsuiteid="TS016LIM";
			query = "{CALL get_testcase_id(?)}";
			stmt = connection.prepareCall(query);
			System.out.println(stmt);
			stmt.setLong("suiteId", testsuiteId);
			// stmt.setLong("",);
			// stmt.setLong("testSuite_id", testsuiteId);

			System.out.println(stmt);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				Long id = rs.getLong("id");
				testcaseid.add(id);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			System.out.println(e);
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
		return testcaseid;

	}

	public void insertToTestExecution(Long test_case_id, Long runid, Long testsuiteId)
			throws CustomException, SQLException {
		try {

			setupJDBCConnection();

			query = "{CALL insert_test_case_execution(?,?,?,?,?,?)}";
			stmt = connection.prepareCall(query);
			System.out.println(stmt);
			Date date = new Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String currentTime = sdf.format(date);
			stmt.setString("executionTime", currentTime);
			stmt.setString("executionStatus", "PASSED");
			stmt.setLong("runId", runid);
			stmt.setLong("testcaseId", test_case_id);
			stmt.setLong("testsuiteId", testsuiteId);
			stmt.setBoolean("flag", true);
			// stmt.setLong("",);
			// stmt.setLong("testSuite_id", testsuiteId);

			System.out.println(stmt);

			ResultSet rs = stmt.executeQuery();

			rs.close();
			stmt.close();
		} catch (Exception e) {
			System.out.println(e);
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}
	}

	public List<String> ReadBddConfigTable(long projectId) throws CustomException {
		String projectname = null;
		String reponame = null;
		String featurefilespath = null;
		try {
			setupJDBCConnection();
			System.out.println("Connection established");
			query = "{CALL get_bdd_config(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("project_id", projectId);
			ResultSet rs = stmt.executeQuery();
			List<String> getbdd_configs = new ArrayList<String>();
			while (rs.next()) {
				projectname = rs.getString("NAME");
				featurefilespath = rs.getString("PATH");
				reponame = rs.getString("REPO");
				getbdd_configs.add(projectname + "#" + reponame + "#" + featurefilespath);
			}
			rs.close();
			stmt.close();
			return getbdd_configs;

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);

		}
	}

	public long AddProjectMasterEntry(String projectName) throws CustomException, SQLException {
		long projectId = 0;
		try {
			setupJDBCConnection();
			query = "{CALL insert_project_master(?)}";
			stmt = connection.prepareCall(query);
			stmt.setString("projectName", projectName);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				projectId = rs.getLong("project_id");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

		return projectId;

	}

	public void AddScenarioLogs(long scenarioId, String scenario_logs) throws CustomException, SQLException {
		// TODO Auto-generated method stub
		try {
			setupJDBCConnection();
			query = "{CALL insert_scenario_logs(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("scenarioid", scenarioId);
			stmt.setString("scenariologs", scenario_logs);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	public List<String> LoadScenarioLogsintoHashmapBasedonEachTestCase(long scenarioId)
			throws CustomException, SQLException {
		String scenarioLogs = null;
		List<String> logs = new ArrayList<String>();
		try {
			query = "{CALL get_scenario_logs(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("scenarioid", scenarioId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				scenarioLogs = rs.getString("scenario_logs");
				logs.add(scenarioLogs);
			}

		} catch (Exception e) {
			throw new CustomException("SqlException or IOException has thrown", e);

		}
		return logs;
	}

	/* Method to Update Keyword Logs */
	public void UpdateKeywordLogs(long stepExecutionId, String keywordLogs) throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL update_keyword_logs(?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("Id", stepExecutionId);
			stmt.setString("keywordLogs", keywordLogs);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	public void UpdateRunByStatus(long run_id) throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			// runID = runId[0];
			query = "{CALL update_run_username(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runId", run_id);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	public void AddJiraDefectToDB(String projectKey, long issueId, long runId, long testcaseId)
			throws CustomException, SQLException {
		try {
			setupJDBCConnection();
			query = "{CALL insert_jira_issue(?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runId", runId);
			stmt.setLong("issueId", issueId);
			stmt.setString("projectKey", projectKey);
			stmt.setLong("testcaseId", testcaseId);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new CustomException(
					"Error updating data values to the database - SqlException or IOException has thrown", e);
		} finally {
			connection.close();
		}

	}

	/*
	 * Excel and Suite table sync public void ExcelSuiteToDB(TestSuiteMap
	 * testsuitemap) throws CustomException { try { stmt =
	 * connection.createStatement(); testsuite_resultSet = testsuite_statement
	 * .executeQuery("SELECT * FROM experian_webautomationui.testsuite_master where testsuite_id='"
	 * + testsuitemap.getTestsuiteId() + "'"); if (testsuite_resultSet.next()) {
	 * testsuite_statement
	 * .executeUpdate("delete from experian_webautomationui.testsuite_master where testsuite_id='"
	 * + testsuitemap.getTestsuiteId() + "'");
	 * 
	 * } Timestamp timestamp = new Timestamp(System.currentTimeMillis()); int
	 * product_id = 1; testsuite_statement.
	 * executeUpdate("INSERT INTO experian_webautomationui.testsuite_master VALUES ('"
	 * + testsuitemap.getTestsuiteId() + "', '" + testsuitemap.getTestsuiteName() +
	 * "', '" + testsuitemap.getExecutionStatus() + "','" +
	 * testsuitemap.getTestUrl() + "'," + null + ", '" + timestamp + "'," + null +
	 * ", " + product_id + ")");
	 * 
	 * } catch (Exception e) { throw new CustomException(
	 * "Error updating test Suite to the database - SqlException or IOException has thrown"
	 * , e); } }
	 * 
	 * Excel and TestCase table sync public void ExcelTestCaseToDB(TestCaseMap
	 * testcasemap, String testSuiteId) throws CustomException { try {
	 * testcase_statement = connection.createStatement(); Timestamp timestamp = new
	 * Timestamp(System.currentTimeMillis()); testcase_statement.executeUpdate(
	 * "INSERT INTO experian_webautomationui.testcase_master VALUES ('" +
	 * testcasemap.getTestCaseId() + "', '" + testcasemap.getTestCaseName() + "', '"
	 * + testcasemap.getTestExecutionFlag() + "','" + timestamp + "', '" +
	 * testSuiteId + "'," + null + ")");
	 * 
	 * } catch (Exception e) { throw new CustomException(
	 * "Error updating test case to the database - SqlException or IOException has thrown"
	 * , e); } }
	 */

}
