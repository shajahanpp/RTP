package com.wrapper.clientutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseValidations {

	public HashMap<String, String> ReadExcelData(String excelinputPath, String referenceID) {
		HashMap<String, String> excelDataMap = new HashMap<String, String>();
		ResponseExcelUtility excelUtil = new ResponseExcelUtility();
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

	public JSONObject ConstructInputJson(HashMap<String, String> excelDataMap, String responseContent,
			String responseType) {
		JSONObject rootjsonObj = new JSONObject();
		JSONObject inputjsonObj = new JSONObject();
		JSONObject validationCriteriaObj = ConstructValidationJsonBlock(excelDataMap);
		try {
			inputjsonObj.put("ResponseContent", responseContent);
			inputjsonObj.put("ValidationCriteria", validationCriteriaObj);

			if (responseType != "")
				inputjsonObj.put("ResponseType", responseType);
			rootjsonObj.put("input", inputjsonObj);
		} catch (JSONException e) {

			e.printStackTrace();
		}
		System.out.println(rootjsonObj);
		return rootjsonObj;
	}

	private JSONObject ConstructValidationJsonBlock(HashMap<String, String> excelDataMap) {
//		System.out.println("INSPECT1" + excelDataMap);
		JSONObject validationObj = new JSONObject();
		for (Entry excelmap : excelDataMap.entrySet()) {

			if (excelmap.getKey().toString().contains("Validation")) {
				try {
					String[] valueList = excelmap.getValue().toString().split("=");

					String key = valueList[0];
					String value = "";
					for (int i = 1; i < valueList.length; i++) {
						value += valueList[i];
					}

					validationObj.put(key, value);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		}
//		System.out.println("INSPECT" + validationObj);
		return validationObj;
	}

	public String GetConnectionString(String excelPath, String sheetName) {
		String connectionString = "";

		ResponseExcelUtility excelUtility = new ResponseExcelUtility();
		excelUtility.setExcel(excelPath, sheetName);
		connectionString = excelUtility.getdata(1, 0);

		return connectionString;
	}

	public JSONArray ConstructTableJsonBlocks(HashMap<String, String> excelDataMap) {
		int flag = 1;

		JSONArray tablejsonArray = new JSONArray();
		do {
			HashMap<String, String> tableMap = new HashMap<String, String>();
			for (Entry excelmap : excelDataMap.entrySet()) {

				if (excelmap.getKey().toString().contains("Table" + flag)) {
					tableMap.put(excelmap.getValue().toString().split("=")[0],
							excelmap.getValue().toString().split("=")[1]);
				}

			}
			JSONObject tableJsonObj = ConstructTableJsonBlock(tableMap);

			tablejsonArray.put(tableJsonObj);
			flag++;

		} while (excelDataMap.containsKey("Table" + flag + ".Name"));

		return tablejsonArray;

	}

	private JSONObject ConstructTableJsonBlock(HashMap<String, String> tableMap) {

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
						constraintObj.put(map.getValue().toString().split("=")[0],
								map.getValue().toString().split("=")[1]);
					} else {
						constraintObj.put(map.getValue().toString().split("=")[0], "");
					}
				}

				if (map.getKey().toString().contains("Validation")) {

					validationObj.put(map.getValue().toString().split("=")[0], map.getValue().toString().split("=")[1]);

				}

				if (map.getKey().toString().contains("TableMapping")) {
					mappingObj.put("Mapping" + mappingFlag, map.getValue().toString());
					mappingFlag++;
				}

			}

			if (!constraintObj.has("PowerCurveId")) {
				constraintObj.put("PowerCurveId", "");
			}
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

			System.out.println("\n Validation Service Payload :\n" + outputJson);
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
