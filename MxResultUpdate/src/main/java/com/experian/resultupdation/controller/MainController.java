package com.experian.resultupdation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.experian.resultupdation.PCOValidations;
import com.experian.resultupdation.OutputClass.ResultMap;

@Controller
public class MainController {

	@RequestMapping(value = "resultupdation", method = RequestMethod.POST)
	@ResponseBody
	public ResultMap trigger(@RequestBody MainRequest mainRequest) {

		String runID = (String) mainRequest.input.get("RunId");
		String user = (String) mainRequest.input.get("User");
		String databaseConnectionString = (String) mainRequest.input.get("DatabaseConnectionString");

		List<String> tableNames = new ArrayList<String>();

		List<Map<String, Object>> hashMapList = new ArrayList<Map<String, Object>>();
		hashMapList = (List<Map<String, Object>>) mainRequest.input.get("ResultUpdations");

		JSONObject inputJson = ConstructInputJsonObject(hashMapList, runID,user,
				tableNames);
		PCOValidations objPcoValidation = new PCOValidations();

		ResultMap objResultMap = null;
		try {
			objResultMap = objPcoValidation.ReturnOutputBasedOnResultUpdation(inputJson);
		} catch (IOException e) {
			System.out.println(e.toString());
		}

		return objResultMap;

	}

	private JSONObject ConstructInputJsonObject(List<Map<String, Object>> hashMapList,
			String runID,String user, List<String> tableNames) {

		System.out.println(hashMapList);
		JSONObject rootJson = new JSONObject();
		JSONObject inputJson = new JSONObject();
		try {
			inputJson.put("RunId", runID);	
			inputJson.put("User", user);	
			
			JSONArray tableJsonArray = new JSONArray();
			JSONObject tableJsonObject = null;
			for (Map<String, Object> tableInputMap : hashMapList) {
				tableJsonObject = new JSONObject(tableInputMap);
				
				tableJsonArray.put(tableJsonObject);
			}
			inputJson.accumulate("ResultUpdations", tableJsonArray);

			rootJson.accumulate("input", inputJson);
	//		System.out.println(rootJson.toString());
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return inputJson;
	}

	@RequestMapping(value = "test", method = RequestMethod.POST)
	@ResponseBody
	public String test(@RequestBody MainRequest mainRequest) {
//		System.out.println(mainRequest.toString());

		// JsonResponseTableMapping
		return mainRequest.toString();
	}
}
