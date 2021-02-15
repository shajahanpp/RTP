package com.wrapper.clientutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.json.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class TableValidations {

	public HashMap<String, String> ReadExcelData(String excelinputPath, String referenceID) {
		HashMap<String, String> excelDataMap = new HashMap<String, String>();
		TableExcelUtility excelUtil = new TableExcelUtility();
		try {
			String[] arr = referenceID.split("_");
			arr = Arrays.copyOf(arr, arr.length - 1);
			String inputData = "";
			for (String input : arr) {
				inputData += input;
			}
			excelUtil.setExcel(excelinputPath, arr[0]);

			List<String> keyList = new ArrayList<String>();
			List<String> valueList = new ArrayList<String>();
			String columnHeader = null;
			String columnValue = null;
			int cell = 1;

			int getsheetRowCount = excelUtil.getRowCount();

			do {
				columnHeader = excelUtil.getdata(0, cell);
				cell++;
				if (columnHeader != "") {

					keyList.add(columnHeader);

				}
			} while (columnHeader != "");
			cell = 1;
			String findKeyword = null;
			for (int j = 1; j < getsheetRowCount; j++) {
				findKeyword = excelUtil.getdata(j, 0);
				if (referenceID.equals(findKeyword)) {
					for (int k = 0; k < keyList.size(); k++) {
						columnValue = excelUtil.getdata(j, cell);
						valueList.add(columnValue);
						cell++;
					}
				}
			}
//			System.out.println("Keylist"+keyList);
//			System.out.println("ValueList"+valueList);
			
			for (int i = 0; i < keyList.size(); i++) {
				if (valueList.get(i) != "") {
					excelDataMap.put(keyList.get(i), valueList.get(i));
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return excelDataMap;
	}

	public String GetInputReferenceID(String referenceID) {
		String[] arr = referenceID.split("_");
		arr = Arrays.copyOf(arr, arr.length - 1);
		String inputData = "";
		for (String input : arr) {
			inputData += input;
		}

		return arr[0];
	}

	public JSONObject ConstructInputJson(HashMap<String, String> excelDataMap, String connectionString,
			String masterTestDataPath, String masterTestDataId, String referenceid, String excelinputPath) {
		JSONObject rootjsonObj = new JSONObject();
		JSONObject inputjsonObj = new JSONObject();
		JSONArray tablejsonArray = ConstructTableJsonBlocks(excelDataMap, masterTestDataPath, masterTestDataId,
				referenceid, excelinputPath);
		try {
//			inputjsonObj.put("PowerCurveID", excelDataMap.get("PowerCurveID"));
			if (excelDataMap.get("DefaultTableMapping") != null) {
				inputjsonObj.put("DefaultTableMapping", excelDataMap.get("DefaultTableMapping"));
			}
			inputjsonObj.put("DatabaseConnectionString", connectionString);
			inputjsonObj.put("TableValidations", tablejsonArray);
			rootjsonObj.put("input", inputjsonObj);
			

		} catch (JSONException e) {

			e.printStackTrace();
		}
		return rootjsonObj;
	}

	public String GetConnectionString(String excelPath, String sheetName) {
		String connectionString = "";

		TableExcelUtility excelUtility = new TableExcelUtility();
		excelUtility.setExcel(excelPath, sheetName);
		connectionString = excelUtility.getdata(1, 0);

		return connectionString;
	}

	public JSONArray ConstructTableJsonBlocks(HashMap<String, String> excelDataMap, String masterTestDataPath,
			String masterTestDataId, String referenceid, String testdataPath) {
		int flag = 1;

		JSONArray tablejsonArray = new JSONArray();
		do {
			HashMap<String, String> tableMap = new HashMap<String, String>();
			for (Entry excelmap : excelDataMap.entrySet()) {

				if (excelmap.getKey().toString().contains("Table" + flag)) {

					tableMap.put(excelmap.getKey().toString(), excelmap.getValue().toString());

				}

			}
			JSONObject tableJsonObj = ConstructTableJsonBlock(tableMap, masterTestDataPath, masterTestDataId,
					referenceid, testdataPath);

			tablejsonArray.put(tableJsonObj);
			flag++;

		} while (excelDataMap.containsKey("Table" + flag + ".Name"));

		return tablejsonArray;

	}

	private JSONObject ConstructTableJsonBlock(HashMap<String, String> tableMap, String masterTestDataPath,
			String masterTestDataId, String referenceid, String testdatafilePath) {

		JSONObject tableObj = new JSONObject();
		JSONObject constraintObj = new JSONObject();
		JSONObject validationObj = new JSONObject();
		JSONObject mappingObj = new JSONObject();
		int mappingFlag = 1;
		try {
			for (Entry map : tableMap.entrySet()) {
				if (map.getKey().toString().contains("Name")) {
					tableObj.put("TableName", map.getValue().toString());
				}

				if (map.getKey().toString().contains("Constraint")) {
					if (map.getValue().toString().split("=")[1] != null) {

						if (map.getValue().toString().split("=")[1].contains("$")) {
							String masterSheetValue = GetTestDataValueFromMasterSheet(
									map.getValue().toString().split("=")[1], masterTestDataPath, masterTestDataId);
							if (!masterSheetValue.equals("")) {
								constraintObj.put(map.getValue().toString().split("=")[0], masterSheetValue);
							}
						} else {
							constraintObj.put(map.getValue().toString().split("=")[0],
									map.getValue().toString().split("=")[1]);
						}
					} else {
						constraintObj.put(map.getValue().toString().split("=")[0], "");
					}
				}

//				if (!referenceid.equalsIgnoreCase("BureauValidation")) {
					if (map.getKey().toString().contains("Validation")) {
						if (map.getValue().toString().contains("=")) {
							if (map.getValue().toString().split("=")[1].contains("$")) {

								String masterSheetValue = GetTestDataValueFromMasterSheet(
										map.getValue().toString().split("=")[1], masterTestDataPath, masterTestDataId);
								validationObj.put(map.getValue().toString().split("=")[0], masterSheetValue);
							} else {
								String[] splitEquals = map.getValue().toString().split("=");
								String mapSplitVal = "";
								int count = 0;
								for (String splitVal : splitEquals) {
									if (count > 0) {
										mapSplitVal += splitVal + "=";
									}
									count++;
								}
								mapSplitVal = mapSplitVal.substring(0, mapSplitVal.length() - 1);
								validationObj.put(map.getValue().toString().split("=")[0], mapSplitVal);
							}
						}
					}
//				} else {
//					if (map.getKey().toString().contains("ValidationMapping")) {
//						String validationmappingFilePath = map.getValue().toString();
//						testdatafilePath = testdatafilePath.substring(0, testdatafilePath.lastIndexOf(File.separator));
//						String testDataFilePath = testdatafilePath + "\\" + validationmappingFilePath;
//						HashMap<String, String> validationMap = ReadBulkValidationTestData(testDataFilePath);
//						for (Entry validationmap : validationMap.entrySet()) {
//							if (validationmap.getValue().toString().contains("$")) {
//
//								String masterSheetValue = GetTestDataValueFromMasterSheet(
//										validationmap.getValue().toString(), masterTestDataPath, masterTestDataId);
//								validationObj.put(validationmap.getKey().toString(), masterSheetValue);
//							}
//							else
//							{
//							validationObj.put(validationmap.getKey().toString(), validationmap.getValue().toString());
//							}
//
//						}
//					}
//				}

				if (map.getKey().toString().contains("TableMapping")) {
					String mapFlag = map.getKey().toString().substring(map.getKey().toString().length() - 1);
					mappingObj.put("Mapping" + mapFlag, map.getValue().toString());
					mappingFlag++;
				}

			}

//			if (!constraintObj.has("PowerCurveId")) {
//				constraintObj.put("PowerCurveId", "");
//			}
			if (constraintObj.length() != 0) {
				tableObj.put("Constraints", constraintObj);
			}
			if (validationObj.length() != 0) {
				tableObj.put("TableColumnValueMapping", validationObj);
			}
			if (mappingObj.length() != 0) {
				tableObj.put("TargetTableMapping", mappingObj);
			}
		} catch (JSONException ex) {

		}

		// todo-- construct json table block
		// msg ->$powercurveid --todo in server side

		return tableObj;
	}

	private HashMap<String, String> ReadBulkValidationTestData(String validationmappingFilePath) {
		HashMap<String, String> validationDataMap = new HashMap<String, String>();
		TableExcelUtility excelUtil = new TableExcelUtility();

		excelUtil.setFirstSheet(validationmappingFilePath);
		List<String> keyList = new ArrayList<String>();
		List<String> valueList = new ArrayList<String>();
		String columnValue = null;
		int cell = 1;

		int getsheetRowCount = excelUtil.getRowCount();

		for (int j = 1; j < getsheetRowCount; j++) {
			keyList.add(excelUtil.getdata(j, 0));
			columnValue = excelUtil.getdata(j, 1);
			valueList.add(columnValue);

		}

		for (int i = 0; i < keyList.size(); i++) {

			validationDataMap.put(keyList.get(i), valueList.get(i));

		}
		return validationDataMap;

	}

	private String GetTestDataValueFromMasterSheet(String sheetValue, String masterTestDataPath,
			String masterTestDataId) {
		String masterSheetValue = "";
		HashMap<String, String> testDataMap = GetTestDataDetails(masterTestDataPath, masterTestDataId);
		sheetValue = sheetValue.replaceAll("[\\[\\](){}#$]", "");
		for (Entry map : testDataMap.entrySet()) {
			if (sheetValue.equalsIgnoreCase(map.getKey().toString())) {
				masterSheetValue = map.getValue().toString();
				break;
			}
		}
		return masterSheetValue;
	}

	public HashMap<String, String> GetTestDataDetails(String testDataFilePath, String testDataId) {

		HashMap<String, String> testDataMap = new HashMap<String, String>();
		TableExcelUtility excelUtil = new TableExcelUtility();

		excelUtil.setFirstSheet(testDataFilePath);
		List<String> keyList = new ArrayList<String>();
		List<String> valueList = new ArrayList<String>();
		String columnHeader = null;
		String columnValue = null;
		int cell = 1;
		String keyword = testDataId;
		int getsheetRowCount = excelUtil.getRowCount();

		do {
			columnHeader = excelUtil.getdata(0, cell);
			cell++;
			if (columnHeader != "") {

				keyList.add(columnHeader);

			}
		} while (columnHeader != "");
		cell = 1;
		String findKeyword = null;
		for (int j = 1; j < getsheetRowCount; j++) {
			findKeyword = excelUtil.getdata(j, 0);
			if (keyword.equals(findKeyword)) {
				for (int k = 0; k < keyList.size(); k++) {
					columnValue = excelUtil.getdata(j, cell);
					valueList.add(columnValue);
					cell++;
				}
			}
		}

		for (int i = 0; i < keyList.size(); i++) {

			testDataMap.put(keyList.get(i), valueList.get(i));

		}
		return testDataMap;

	}

	public void DeleteFileIfExists(String requestFilepath) {
		File file = new File(requestFilepath);
		try {
			boolean result = Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void WriteToFile(JSONObject inputObj, String referenceID, String response_filepath) {

		String outputJson = inputObj.toString();

		ObjectMapper mapper = new ObjectMapper();

		Object json;
		try {
			json = mapper.readValue(outputJson, Object.class);
			outputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

			System.out.println("Table validation payload : \n"+ outputJson);
			
			File file = new File(response_filepath);
			file.getParentFile().mkdirs();
			file.createNewFile();

			FileOutputStream fooStream = new FileOutputStream(file, false);
			byte[] myBytes = outputJson.getBytes();
			fooStream.write(myBytes);
			fooStream.close();
		} catch (JsonParseException e) {

			e.printStackTrace();
		} catch (JsonMappingException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
