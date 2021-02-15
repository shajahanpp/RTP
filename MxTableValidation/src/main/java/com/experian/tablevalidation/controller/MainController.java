package com.experian.tablevalidation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.experian.tablevalidation.PCOValidations;
import com.experian.tablevalidation.OutputClass.ResultMap;

@Controller
public class MainController {

	@RequestMapping(value = "tablevalidation", method = RequestMethod.POST)
	@ResponseBody
	public ResultMap trigger(@RequestBody MainRequest mainRequest) {

//		String powercurverID = (String) mainRequest.input.get("PowerCurveID");
		String defaultTableMapping = (String) mainRequest.input.get("DefaultTableMapping");
		if(defaultTableMapping==null)
		{
			defaultTableMapping="";
		}
		String databaseConnectionString = (String) mainRequest.input.get("DatabaseConnectionString");

		List<String> tableNames = new ArrayList<String>();

		List<Map<String, Object>> hashMapList = new ArrayList<Map<String, Object>>();
		hashMapList = (List<Map<String, Object>>) mainRequest.input.get("TableValidations");

		JSONObject inputJson = ConstructInputJsonObject(hashMapList, databaseConnectionString,
				tableNames,defaultTableMapping);
		PCOValidations objPcoValidation = new PCOValidations();

		ResultMap objResultMap = objPcoValidation.ReturnOutputBasedOnTableValidation(inputJson);

		return objResultMap;

	}

	private JSONObject ConstructInputJsonObject(List<Map<String, Object>> hashMapList, String databaseConnectionString,
			 List<String> tableNames, String defaultTableMapping) {

		System.out.println(hashMapList);
		JSONObject rootJson = new JSONObject();
		JSONObject inputJson = new JSONObject();
		try {
			
		
				inputJson.put("DefaultTableMapping", defaultTableMapping);
			
			inputJson.put("DatabaseConnectionString", databaseConnectionString);
			int i = 0;
			JSONArray tableJsonArray = new JSONArray();
			JSONObject tableJsonObject = null;
			for (Map<String, Object> tableInputMap : hashMapList) {
				tableJsonObject = new JSONObject(tableInputMap);
				
				tableJsonArray.put(tableJsonObject);
			}
			inputJson.accumulate("TableValidations", tableJsonArray);

			rootJson.accumulate("input", inputJson);
			System.out.println(rootJson.toString());
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return inputJson;
	}

	@RequestMapping(value = "test", method = RequestMethod.POST)
	@ResponseBody
	public String test(@RequestBody MainRequest mainRequest) {
		System.out.println(mainRequest.toString());

		// JsonResponseTableMapping
		return mainRequest.toString();
	}
}