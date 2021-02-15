package com.runid.java;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
			//dbusercredential = parseDBconfigxml();
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rtp", "root","u23219");
			//System.out.println("Connection String is "+dbusercredential.get(0));
			//System.out.println("UserName is "+dbusercredential.get(1));
			//System.out.println("Password String is "+dbusercredential.get(2));
          //connection = DriverManager.getConnection(dbusercredential.get(0), dbusercredential.get(1), dbusercredential.get(2));
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
		List<String> dbusercredential = null;
		try {

			String filePath = "";//System.getProperty("user.dir");
			//filePath = filePath + "\\DBConfig\\dbconfig.xml";
			filePath = "\\DBConfig\\dbconfig.xml";
          	System.out.println(filePath);
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

			System.out.println(ex.toString());
		}

		return dbusercredential;
	}

	public long AddEntriesToRunTable(long projectId) {
		long runId = 0;
		Connection connection = null;
		try {
			connection = SetUpDatabaseConnection();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String currentTime = sdf.format(new Date());
			CallableStatement stmt = null;
			String query = null;
			query = "{CALL insert_run(?,?,?,?)}";
			stmt = connection.prepareCall(query);
			stmt.setLong("projectId", projectId);
			stmt.setString("runStatus", "NoRun");
			stmt.setString("rundate", currentTime);
			stmt.setString("testApproach", "BDD");
			System.out.println(stmt);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				runId = rs.getLong("run_id");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			System.out.println(e.toString());

		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println(runId);
		return runId;

	}

}
