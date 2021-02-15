package com.experian.tablevalidation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DBValidation {

	/* Set Up PCO Database Connection */
	public Connection SetUpDatabaseConnection(String connectionString) {
		
		String[] dbusercredential = connectionString.split(";");
		
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(dbusercredential[0],dbusercredential[1].split("=")[1],dbusercredential[2].split("=")[1]);
			if (connection != null) {
				System.out.println("RTP Database Connection Established");
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return connection;
	}

	public List<HashMap<String, String>> FetchRecordsBasedOnPowerCurveIdAndConstraints(String dbConnectionString,
			String tableName, HashMap<String, String> constraintMap, List<String> targetTableList,
			String defaultTableMapping) {
		List<HashMap<String, String>> listMap = new ArrayList<HashMap<String, String>>();

		String sqlQuery = ConstructSqlQuery(targetTableList, tableName, constraintMap, defaultTableMapping);

		Connection con = SetUpDatabaseConnection(dbConnectionString);
		if (con != null) {
			Statement stmt;
			try {
				stmt = con.createStatement();
				ResultSet resultSet = stmt.executeQuery(sqlQuery);

				int rowCount = 0;
				while (resultSet.next()) {
					rowCount = rowCount + 1;
					HashMap<String, String> resultMap = new HashMap<String, String>();
					ResultSetMetaData meta = resultSet.getMetaData();
					for (int i = 1; i <= meta.getColumnCount(); i++) {
						String key = meta.getColumnName(i);
						String value = resultSet.getString(key);
						resultMap.put(key, value);

					}
					listMap.add(resultMap);

				}

				System.out.println("Row Count is " + rowCount);
				/*
				 * if (rowCount > 1) { return FetchRecordsFromAReprocessApplication(sqlQuery,
				 * tableName, con); }
				 */

			} catch (SQLException e) {

				e.printStackTrace();
			} finally {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println(listMap);
		return listMap;

	}

	private HashMap<String, String> FetchRecordsFromAReprocessApplication(String sqlQuery, String tableName,
			Connection con) {

		String getLastGeneratedRecordQuery = "select TOP 1 B.COLUMN_NAME as PCN from INFORMATION_SCHEMA.TABLE_CONSTRAINTS A inner join INFORMATION_SCHEMA.KEY_COLUMN_USAGE B on A.CONSTRAINT_TYPE='PRIMARY KEY' and A.TABLE_SCHEMA="
				+ quote(tableName.split("\\.")[0]) + " AND  A.TABLE_NAME=" + quote(tableName.split("\\.")[1])
				+ " AND A.CONSTRAINT_NAME = B.CONSTRAINT_NAME ORDER BY A.TABLE_NAME,B.ORDINAL_POSITION";

		sqlQuery = sqlQuery + " ORDER BY (" + getLastGeneratedRecordQuery + ") DESC";
		System.out.println(sqlQuery);
		Statement stmt;
		HashMap<String, String> resultMap = new HashMap<String, String>();
		try {
			stmt = con.createStatement();
			ResultSet resultSet = stmt.executeQuery(sqlQuery);

			ResultSetMetaData meta = resultSet.getMetaData();
			int rowCount = 0;
			while (resultSet.next()) {
				rowCount = rowCount + 1;
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					String key = meta.getColumnName(i);
					String value = resultSet.getString(key);
					resultMap.put(key, value);
				}

			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return resultMap;
	}

	public static String quote(String s) {
		return new StringBuilder().append('\'').append(s).append('\'').toString();
	}

	private String ConstructSqlQuery(List<String> targetTableList, String tableName,
			HashMap<String, String> constraintMap, String defaultTableMapping) {
		boolean check_keyperson_table = false;
		boolean check_applicant_table = false;
		String defaultTableName = "";
		String powerCurveIdColumnName = "";

		if (defaultTableMapping != "") {
			String[] splitArr = defaultTableMapping.split("\\.");
			powerCurveIdColumnName = splitArr[splitArr.length - 1];
			for (int i = 0; i < splitArr.length - 1; i++) {
				defaultTableName = defaultTableName + splitArr[i] + ".";
			}

			defaultTableName = defaultTableName.substring(0, defaultTableName.length() - 1);

		} else {
//			defaultTableName = "EDA_TENANT1.APPLICATION";
//			powerCurveIdColumnName = "APPLCTN_ID";

			defaultTableName = tableName;
		}
		if (targetTableList.size() > 0) {
			tableName = "";
			for (int i = 0; i < targetTableList.size(); i++) {
				System.out.println(tableName);
				if (targetTableList.get(i).split(",")[0].equalsIgnoreCase("EDA_TENANT1.KEY_PERSON")) {
					check_keyperson_table = true;
				}
				if (targetTableList.get(i).split(",")[0].equalsIgnoreCase("EDA_TENANT1.APPLICANT")) {
					check_applicant_table = true;
				}
				tableName = tableName + targetTableList.get(i).split(",")[0] + ".*,";

			}
			tableName = tableName.substring(0, tableName.length() - 1);
		} else {
			tableName = tableName + ".*";
		}
		String sqlQuery = "";
		String sqlQuery_join = "";

		for (int i = 0; i < targetTableList.size(); i++) {
			if (i == 0) {

				if (!targetTableList.get(i).split(",")[1].contains("-")) {
					sqlQuery_join = sqlQuery_join + " INNER JOIN " + targetTableList.get(i).split(",")[0] + " ON "
							+ defaultTableName + "." + targetTableList.get(i).split(",")[1] + "="
							+ targetTableList.get(i).split(",")[0] + "." + targetTableList.get(i).split(",")[1];
				} else {
					sqlQuery_join = sqlQuery_join + " INNER JOIN " + targetTableList.get(i).split(",")[0] + " ON "
							+ defaultTableName + "." + targetTableList.get(i).split(",")[1].split("-")[0] + "="
							+ targetTableList.get(i).split(",")[0] + "."
							+ targetTableList.get(i).split(",")[1].split("-")[1];
				}

			} else if (targetTableList.get(i).split(",")[0].equalsIgnoreCase("EDA_TENANT1.KEY_PERSON")) {
				sqlQuery_join = sqlQuery_join + " " + StaticSqlQueryClass.GetKeyPersonApplicationMappingQuery;
			} else if (targetTableList.get(i).split(",")[0].equalsIgnoreCase("EDA_TENANT1.Address")) {

				if (check_keyperson_table)
					sqlQuery_join = sqlQuery_join + " " + StaticSqlQueryClass.GetKeyPersonAddressMappingQuery;
				else if (check_applicant_table)
					sqlQuery_join = sqlQuery_join + " " + StaticSqlQueryClass.GetApplicantAddressMappingQuery;
			} else {
				if (!targetTableList.get(i).split(",")[1].contains("-")) {
					sqlQuery_join = sqlQuery_join + " INNER JOIN " + targetTableList.get(i).split(",")[0] + " ON "
							+ targetTableList.get(i - 1).split(",")[0] + "." + targetTableList.get(i).split(",")[1]
							+ "=" + targetTableList.get(i).split(",")[0] + "." + targetTableList.get(i).split(",")[1];
				} else {
					sqlQuery_join = sqlQuery_join + " INNER JOIN " + targetTableList.get(i).split(",")[0] + " ON "
							+ targetTableList.get(i - 1).split(",")[0] + "."
							+ targetTableList.get(i).split(",")[1].split("-")[0] + "="
							+ targetTableList.get(i).split(",")[0] + "."
							+ targetTableList.get(i).split(",")[1].split("-")[1];
				}
			}

		}
		String sqlQuery_constraint = "";
		String constraint_pcid_value = "";
		for (Entry constraint : constraintMap.entrySet()) {

			sqlQuery_constraint = sqlQuery_constraint  + constraint.getKey().toString() + " = "
					+ quote(constraint.getValue().toString()) + " AND ";

		}

		sqlQuery = "SELECT " + tableName + " FROM " + defaultTableName + " " + sqlQuery_join + " WHERE " +
		// powerCurveIdColumnName + " = " + quote(constraint_pcid_value) +
				sqlQuery_constraint.substring(0, sqlQuery_constraint.length()-5);

		System.out.println(sqlQuery);
		return sqlQuery;
	}
}
