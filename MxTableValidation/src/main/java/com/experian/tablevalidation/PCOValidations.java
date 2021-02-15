package com.experian.tablevalidation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.experian.tablevalidation.OutputClass.ResultMap;
import com.experian.tablevalidation.OutputClass.TableResultMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PCOValidations {

	public ResultMap ReturnOutputBasedOnTableValidation(JSONObject inputJson) {
		ResultMap objResultMap = new ResultMap();
		objResultMap.setTestStatus("PASSED");
		try {
//			String powerCurveID = inputJson.getString("PowerCurveID");
			String defaultTableMapping = inputJson.getString("DefaultTableMapping");
			String dbConnectionString = inputJson.getString("DatabaseConnectionString");

//			System.out.println(powerCurveID);
			System.out.println(dbConnectionString);

			JSONArray array_tables = inputJson.getJSONArray("TableValidations");
			String arry = array_tables.toString();
			// System.out.println(arry);
			arry = arry.substring(1, arry.length() - 1);
			array_tables = new JSONArray(arry);
			HashMap<String, String> constraintMap = null;
			List<TableResultMap> tableResultMapList = new ArrayList<TableResultMap>();
			for (int i = 0; i < array_tables.length(); i++) {

				JSONObject obj_table = array_tables.getJSONObject(i);

				String tableName = GetTableName(obj_table);

//				objResultMap.setPowercurveID(powerCurveID);

				TableResultMap objTableResultMap = new TableResultMap();
				objTableResultMap.setTableName(tableName);
				objTableResultMap.setValidationStatus("PASSED");
				System.out.println("************************************************" + tableName + " Validation "
						+ "************************************************");
				JSONObject targetTableObject = null;
				List<String> differenceList = new ArrayList<String>();
				List<String> targetTableList = new ArrayList<String>();
				constraintMap = new HashMap<String, String>();
				try {
					if (obj_table.getJSONObject("Constraints") != null) {
						GetConstraints(obj_table, constraintMap);
					}

					if (obj_table.getJSONObject("TargetTableMapping") != null) {
						targetTableObject = obj_table.getJSONObject("TargetTableMapping");
						String targetTableJsonData = SortTargetTableObjectsBasedonKeys(targetTableObject);
						FetchTargetTableDetails(targetTableList, targetTableJsonData, obj_table);
					}
				} catch (Exception ex) {

				}

//				if (constraintMap.containsKey("PowerCurveId")) {
//					String reProcess_pcid = constraintMap.get("PowerCurveId");
//					if (!reProcess_pcid.equals("")) {
//
//						objResultMap.setPowercurveID(reProcess_pcid);
//					}
//				}

				HashMap<String, String> inputtableColumnValuemap = new HashMap<String, String>();
				JSONObject inputtableColumnValueobj = obj_table.getJSONObject("TableColumnValueMapping");
				for (int j = 0; j < inputtableColumnValueobj.names().length(); j++) {
					inputtableColumnValuemap.put(inputtableColumnValueobj.names().getString(j),
							(String) inputtableColumnValueobj.get(inputtableColumnValueobj.names().getString(j)));
				}

				DBValidation objDBValidation = new DBValidation();
				List<HashMap<String, String>> dbResultMapList = objDBValidation
						.FetchRecordsBasedOnPowerCurveIdAndConstraints(dbConnectionString, tableName,
								constraintMap, targetTableList, defaultTableMapping);
				if (dbResultMapList.size() > 0) {
					if (dbResultMapList.size() == 1) {
						for (HashMap<String, String> dbResultMap : dbResultMapList) {

							ValidateDbResultObjectsForSingleRecord(objTableResultMap, differenceList,
									inputtableColumnValuemap, dbResultMap);

							objTableResultMap.setDifferenceList(differenceList);
							tableResultMapList.add(objTableResultMap);
						}
					} else {
						boolean check_all_valid = false;

						for (int k = 0; k < dbResultMapList.size(); k++) {
							String difference = "";
							HashMap<String, String> dbResultMap = dbResultMapList.get(k);
							// System.out.println(dbResultMap);

							check_all_valid = ValidateDbResultObjectsForMultipleRecords(objTableResultMap,
									differenceList, inputtableColumnValuemap, dbResultMap);
							if (check_all_valid) {
								tableResultMapList.add(objTableResultMap);
								objTableResultMap.setValidationStatus("PASSED");
								break;
							}

						}
						if (!check_all_valid) {

							// String diff = differenceList.get(differenceList.size() - 1);
							String diff = "No Database Records based on constraints"
									+ " that matches all the expected values";
							differenceList.clear();
							differenceList.add(diff);

							objTableResultMap.setValidationStatus("FAILED");
							objTableResultMap.setFailedMessage("");
							objTableResultMap.setDifferenceList(differenceList);
							tableResultMapList.add(objTableResultMap);
						}
					}
				} else {
					objTableResultMap.setValidationStatus("FAILED");
					objTableResultMap.setFailedMessage(
							"No Database Records based on " + " or DataBase Connection Error");
					objTableResultMap.setDifferenceList(null);
					tableResultMapList.add(objTableResultMap);
				}

			}
			objResultMap.setTableResultList(tableResultMapList);
			for (TableResultMap tableResultMap : objResultMap.getTableResultList()) {

				if (tableResultMap.getValidationStatus().equalsIgnoreCase("FAILED")) {
					objResultMap.setTestStatus("FAILED");
					break;
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			objResultMap.setTestStatus("FAILED");
		}

		// TODO Auto-generated method stub
		return objResultMap;
	}

	private void ValidateDbResultObjectsForSingleRecord(TableResultMap objTableResultMap, List<String> differenceList,
			HashMap<String, String> inputtableColumnValuemap, HashMap<String, String> dbResultMap) {
		for (Entry iterateeachinputData : inputtableColumnValuemap.entrySet()) {
			boolean isDate = false;

			String inputJsonValue = "";
			DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
			inputJsonValue = iterateeachinputData.getValue().toString();
			isDate = isValidDate(inputJsonValue);
//			if (iterateeachinputData.getValue().toString().equals("CurrentDate")) {
//				isDate = true;
//
//				inputJsonValue = formatter.format(new Date());
//
//			}
			if (dbResultMap.containsKey(iterateeachinputData.getKey().toString())) {
				if (isDate) {
					String dbDate = dbResultMap.get(iterateeachinputData.getKey().toString());
					dbDate = dbDate.substring(0, 10);
					if (!dbDate.equals(inputJsonValue)) {

						String difference = "Column To Validate : " + iterateeachinputData.getKey().toString() + "  ; "
								+

								"Expected Value : " + inputJsonValue + "  ; " + "Database Value : " + dbDate;

						differenceList.add(difference);
						objTableResultMap.setValidationStatus("FAILED");
						objTableResultMap.setFailedMessage("");
					}
				} else {

					if (dbResultMap.get(iterateeachinputData.getKey().toString()) != null) {

						// inputJsonValue = inputJsonValue.trim();
						String dbVal = dbResultMap.get(iterateeachinputData.getKey().toString());

						if (isNumeric(dbVal)) {
							Double nodeValue_double = Double.parseDouble((String) inputJsonValue);
							Double dbValue_double = Double.parseDouble((String) inputJsonValue);
							if (!dbValue_double.equals(nodeValue_double)) {

								String difference = "Column To Validate : " + iterateeachinputData.getKey().toString()
										+ "  ; " +

										"Expected Value : " + inputJsonValue + "  ; " + "Database Value : "
										+ dbVal.toString();

								differenceList.add(difference);
								objTableResultMap.setValidationStatus("FAILED");
								objTableResultMap.setFailedMessage("");
							}

						} else {

							dbVal = RemoveTimeandGetString(dbResultMap.get(iterateeachinputData.getKey().toString()));
							if (!dbVal.equals(inputJsonValue)) {

								String difference = "Column To Validate : " + iterateeachinputData.getKey().toString()
										+ "  ; " +

										"Expected Value : " + inputJsonValue + "  ; " + "Database Value : " + dbVal;

								differenceList.add(difference);
								objTableResultMap.setValidationStatus("FAILED");
								objTableResultMap.setFailedMessage("");
							}
						}
					} else {

						if (inputJsonValue.equalsIgnoreCase("NULL")) {

						} else {
							String difference = "Column To Validate : " + iterateeachinputData.getKey().toString()
									+ "  ; " +

									"Expected Value : " + inputJsonValue + "  ; " + "Database Value : " + "NULL ";

							differenceList.add(difference);
							objTableResultMap.setValidationStatus("FAILED");
							objTableResultMap.setFailedMessage("");
						}
					}
				}
			} else {
				String difference = "There is no Column named " + iterateeachinputData.getKey().toString();

				differenceList.add(difference);
				objTableResultMap.setValidationStatus("FAILED");
				objTableResultMap.setFailedMessage("");
			}
		}
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean ValidateDbResultObjectsForMultipleRecords(TableResultMap objTableResultMap,
			List<String> differenceList, HashMap<String, String> inputtableColumnValuemap,
			HashMap<String, String> dbResultMap) {
		// differenceList.clear();
		boolean check_all_valid = true;
		for (Entry iterateeachinputData : inputtableColumnValuemap.entrySet()) {
			boolean isDate = false;

			String inputJsonValue = "";
			DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
			inputJsonValue = iterateeachinputData.getValue().toString();
			isDate = isValidDate(inputJsonValue);
			if (iterateeachinputData.getValue().toString().equals("CurrentDate")) {
				isDate = true;

				inputJsonValue = formatter.format(new Date());

			}
			if (dbResultMap.containsKey(iterateeachinputData.getKey().toString())) {
				String dbVal = dbResultMap.get(iterateeachinputData.getKey().toString());
				System.out.println(dbVal);

				if (isDate) {
					if (dbVal != null)
						dbVal = dbVal.substring(0, 10);
				}
				if (dbVal != null) {
					if (!isDate) {
						dbVal = RemoveTimeandGetString(dbResultMap.get(iterateeachinputData.getKey().toString()));
					}
					// inputJsonValue = inputJsonValue.trim();
					if (isNumeric(dbVal)) {
						Double nodeValue_double = Double.parseDouble((String) inputJsonValue);
						Double dbValue_double = Double.parseDouble((String) inputJsonValue);
						if (!dbValue_double.equals(nodeValue_double)) {

							String difference = "Column To Validate : " + iterateeachinputData.getKey().toString()
									+ "  ; " +

									"Expected Value : " + inputJsonValue + "  ; " + "Database Value : " + dbVal;

							differenceList.add(difference);
							objTableResultMap.setValidationStatus("FAILED");
							objTableResultMap.setFailedMessage("");
						}

					} else {
						if (!dbVal.equalsIgnoreCase(inputJsonValue)) {

							String difference = "Column To Validate : " + iterateeachinputData.getKey().toString()
									+ "  ; " +

									"Expected Value : " + inputJsonValue + "  ; " + "Database Value : " + dbVal;

							differenceList.add(difference);
							objTableResultMap.setValidationStatus("FAILED");
							objTableResultMap.setFailedMessage("");
							check_all_valid = false;
							break;
						}
					}

				} else {

					if (inputJsonValue.equalsIgnoreCase("NULL")) {

					} else {

						String difference = "Column To Validate : " + iterateeachinputData.getKey().toString() + "  ; "
								+

								"Expected Value : " + inputJsonValue + "  ; " + "Database Value : " + "NULL ";

						differenceList.add(difference);
						objTableResultMap.setValidationStatus("FAILED");
						objTableResultMap.setFailedMessage("");
						check_all_valid = false;
						break;
					}
				}

			} else {
				System.out.println("Column Name not found : " + iterateeachinputData.getKey().toString());
				String difference = "Column Name not found : " + iterateeachinputData.getKey().toString();
				differenceList.add(difference);
				check_all_valid = false;
				break;
			}
		}
		return check_all_valid;
	}

	public static boolean isValidDate(String inDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM");

		try {
			Date date = dateFormat.parse(inDate.trim());
		} catch (Exception pe) {
			return false;
		}
		return true;
	}

	private String SortTargetTableObjectsBasedonKeys(JSONObject targetTableObject) {
		String json = targetTableObject.toString();
		String jsonData = "";
		JSONObject targetTableObj = null;
		try {
			ObjectMapper om = new ObjectMapper();
			om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
			Map<String, Object> map = om.readValue(json, HashMap.class);

			jsonData = om.writeValueAsString(map);
			// System.out.println(jsonData);
			jsonData = jsonData.substring(1, jsonData.length() - 1);
		} catch (IOException e) {

			e.printStackTrace();
		}
		return jsonData;

	}

	private String RemoveTimeandGetString(String dbVal) {

		boolean isCheck = false;
		String returnVal = "";
		String[] words = dbVal.split(" ");
		for (String word : words) {
			if (word.contains("Duration:")) {
				isCheck = true;
				returnVal = returnVal + word + " ";
				break;
			} else {
				returnVal = returnVal + word + " ";
			}
			/*
			 * if (word.contains("ms")) { if (word.matches(".*\\d.*")) { isCheck = true;
			 * System.out.println("hit"); } } else { returnVal = returnVal + word + " "; }
			 */

		}
		returnVal = returnVal.trim();
		if (!isCheck) {
			returnVal = dbVal;
		}

		return returnVal;
	}

	public static boolean isAlphaNumeric(String dbVal) {
		return dbVal != null && dbVal.matches("^[a-zA-Z0-9]*$");
	}

	private void FetchTargetTableDetails(List<String> targetTableList, String targetTableJsonData,
			JSONObject obj_table) {

		String[] targetTableSplit = targetTableJsonData.split(",");
		for (String targetTableobjects : targetTableSplit) {

			String mapValue = targetTableobjects.split(":")[1];

			String tableName = "";
			String columnName = "";

			String[] splitMapValue = mapValue.split("\\.");
			for (int j = 0; j < splitMapValue.length - 1; j++) {
				tableName = tableName + splitMapValue[j] + ".";
			}
			tableName = tableName.substring(0, tableName.length() - 1);
			columnName = splitMapValue[splitMapValue.length - 1];
			String tableNameColumnName = tableName + "," + columnName;
			tableNameColumnName = tableNameColumnName.substring(1, tableNameColumnName.length() - 1);
			targetTableList.add(tableNameColumnName);
		}

	}

	private void GetConstraints(JSONObject obj_table, HashMap<String, String> constraintMap) {
		Iterator iterator = obj_table.keys();
		try {
			while (iterator.hasNext()) {
				String key = (String) iterator.next();

				if (key.contains("Constraints")) {
					JSONObject obj_constraints = obj_table.getJSONObject("Constraints");
					for (int i = 0; i < obj_constraints.names().length(); i++) {
						constraintMap.put(obj_constraints.names().getString(i),
								(String) obj_constraints.get(obj_constraints.names().getString(i)));
					}
				}
			}
		}

		catch (

		JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String GetTableName(JSONObject obj_table) {
		String tableName = null;
		try {
			tableName = obj_table.getString("TableName");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tableName;
	}
}

class ValueComparator implements Comparator<String> {

	Map<String, String> map = new HashMap<String, String>();

	public ValueComparator(Map<String, String> map) {
		this.map = map;
	}

	@Override
	public int compare(String s1, String s2) {
		return map.get(s1).compareTo(map.get(s2));
	}
}