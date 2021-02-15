package com.experian.resultupdation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class DBValidation {

	/* Set Up PCO Database Connection */
	public Connection SetUpDatabaseConnection() {
		List<String> dbusercredential = null;
		Connection connection = null;
		try {
			dbusercredential = parseDBconfigxml();
			Class.forName("com.mysql.cj.jdbc.Driver");
          	connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rtp", "root", "u23219");
			//connection = DriverManager.getConnection(dbusercredential.get(0), dbusercredential.get(1), dbusercredential.get(2));
			if (connection != null) {
				System.out.println("PCO BPS Database Connection Established");
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return connection;
	}

	/* Parse DB Connection Details from DBConfig xml */
	private List<String> parseDBconfigxml() {
		Document doc;
		List<String> dbusercredential = null;
		try {

			String filePath = System.getProperty("user.dir");
			filePath = filePath + "\\DBConfig\\dbconfig.xml";
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Path paths = Paths.get(filePath);
			// System.out.println(paths);
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

			System.out.println(ex.toString());
		}

		return dbusercredential;
	}

	/* Insert entries to Feature Master */
	public long AddEntriesToFeatureTable(String featureName, long runId) throws SQLException {
		long feature_Id = 0;
		Connection con = null;
		try {
			con = SetUpDatabaseConnection();
			System.out.println(con);
			String query = "{CALL insert_feature(?,?,?)}";
			CallableStatement stmt = con.prepareCall(query);
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

		} finally {
			con.close();
		}

		return feature_Id;

	}

	/* Insert entries to Feature Master */
	public long AddEntriesToScenarioTable(String scenarioName,String scenarioDesc, String browser, long featureId) throws SQLException {
		Connection connection = null;
		long scenario_Id = 0;
		try {
			connection = SetUpDatabaseConnection();
			String query = "{CALL insert_scenario(?,?,?,?)}";
			CallableStatement stmt = connection.prepareCall(query);
			stmt.setString("scenarioname", scenarioName);
			stmt.setString("scenariodesc", scenarioDesc);
			stmt.setString("scenariobrowser", browser);
			stmt.setLong("featureid", featureId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				scenario_Id = rs.getLong("scenarioId");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {

		} finally {
			connection.close();
		}

		return scenario_Id;

	}

	public long AddEntriesToScenarioExecutionTable(long runId, long featureId, long scenarioId,
			String testcaseExecutionTime) throws SQLException {
		Connection connection = null;
		long scenarioExec_Id = 0;
		try {
			connection = SetUpDatabaseConnection();
			String query = "{CALL insert_scenario_execution(?,?)}";
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			query = "{CALL insert_scenario_execution(?,?,?,?)}";
			CallableStatement stmt = connection.prepareCall(query);
			stmt.setString("executionTime", testcaseExecutionTime);
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

		} finally {
			connection.close();
		}

		return scenarioExec_Id;

	}

	public void UpdateTestExecutionStatus(long scenarioId, String resultStatus) throws SQLException {
		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();
			String query = "{CALL update_scenario_execution_status(?,?)}";

			CallableStatement stmt = connection.prepareCall(query);
			stmt.setLong("scenarioId", scenarioId);
			stmt.setString("resultStatus", resultStatus);
			ResultSet rs = stmt.executeQuery();

			rs.close();
			stmt.close();
		} catch (Exception e) {

		} finally {
			connection.close();
		}

	}
	
	/* Insert entries to Feature Master */
	public long AddEntriesToStepTable(String stepName, long scenarioId) throws SQLException {
		long step_Id = 0;
		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();
			String query = "{CALL insert_step(?,?)}";
			CallableStatement stmt = connection.prepareCall(query);
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
		
		} finally {
			connection.close();
		}

		return step_Id;

	}
	
	public long AddEntriesToStepExecutionTable(long scenarioExecId, long stepId, String status)
			throws SQLException {
		long stepExec_Id = 0;
		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();

			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			String query =   "{CALL insert_step_execution(?,?,?,?)}";
			CallableStatement stmt = connection.prepareCall(query);
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
			
		} finally {
			connection.close();
		}

		return stepExec_Id;

	}
	
	public String ReadProjectNameBasedOnRunId(long runid)  {
		String project_name = null;
		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();
			String query = "{CALL get_project_name(?)}";
			CallableStatement stmt = connection.prepareCall(query);
			stmt.setLong("runId", runid);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				project_name = rs.getString("project_name");
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			
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
	/* Update TestRun table to DB */
	public void updateRunTableStatus(String runStatus, long runId) throws SQLException {

		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();		
			String query = "{CALL update_run_status(?,?)}";
			CallableStatement stmt = connection.prepareCall(query);
			stmt.setString("runStatus", runStatus);
			stmt.setLong("runId", runId);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
			
		} finally {
			connection.close();
		}

	}
	
	public void UpdateTestLogsToStepExecution(long stepExec_Id, String logFilePath) {
		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();
			String query = "{CALL update_keyword_logs(?,?)}";
			CallableStatement stmt =  connection.prepareCall(query);
			stmt.setLong("Id", stepExec_Id);
			stmt.setString("keywordLogs", logFilePath);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
		} catch (Exception e) {
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public void updateUserinRunTable(String runID, String username) {
		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();
			String query = "{CALL update_run_username(?,?)}";
			CallableStatement stmt = connection.prepareCall(query);
			stmt.setLong("runId", Long.parseLong(runID));
			stmt.setString("username", username);
			ResultSet rs = stmt.executeQuery();
			rs.close();
			stmt.close();
          	System.out.println(username);
		} catch (Exception e) {

		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
