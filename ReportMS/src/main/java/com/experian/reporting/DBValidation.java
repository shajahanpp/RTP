package com.experian.reporting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.experian.reporting.model.OutputClass.ResultMap;

public class DBValidation {
	/* Set Up PCO Database Connection */
	public Connection SetUpDatabaseConnection() {
		List<String> dbusercredential = null;
		Connection connection = null;
		try {
			dbusercredential = parseDBconfigxml();
			//Class.forName("com.mysql.jdbc.Driver");
          	//connection = DriverManager.getConnection("jdbc:mysql://10.188.31.79:3306/experian_webautomationui", "root", "root");
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rtp", "root","u23219");
			//System.out.println("Connection String is "+dbusercredential.get(0));
			//System.out.println("UserName is "+dbusercredential.get(1));
			//System.out.println("Password String is "+dbusercredential.get(2));
			if (connection != null) {
				System.out.println("Database Connection Established");
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return connection;
	}

	/* Parse DB Connection Details from DBConfig xml */
	private List<String> parseDBconfigxml() {
		Document doc;
		List<String> dbusercredential = new ArrayList<String>();;
		try {
			String filePath ="";
			//String filePath = System.getProperty("user.dir");
			filePath = "\\DBConfig\\dbconfig.xml";
			//System.out.println("DB File Path : "+filePath);
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

	public String ReadProjectNameBasedOnRunId(long runid) {
		String project_name = null;
		Connection connection = null;
		try {
			CallableStatement stmt = null;
			String query = null;
			connection = SetUpDatabaseConnection();
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
			System.out.println(e.toString());
		} finally {
			try {
				System.out.println("Connection Status is " + connection);
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return project_name;
	}

	public ResultMap ReadRunDetails(long runid) {
		ResultMap objResultMap = null;
		Connection connection = null;
		try {
			CallableStatement stmt = null;
			String query = null;
			connection = SetUpDatabaseConnection();
			query = "{CALL get_run_details(?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("runid", runid);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				objResultMap = new ResultMap();
				objResultMap.setReportLocation(rs.getString("report_location"));
				objResultMap.setRunStatus(rs.getString("status"));
				objResultMap.setStatusMessage(rs.getString("status_message"));
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			System.out.println(e.toString());

		} finally {
			try {

				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return objResultMap;
	}
}
