package com.experian.responsevalidation.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.RestController;

import com.experian.responsevalidation.PCOValidations;
import com.experian.responsevalidation.OutputClass.ResultMap;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MainController {

	@RequestMapping(value = "responsevalidation", method = RequestMethod.POST)
	@ResponseBody
	public ResultMap trigger(@RequestBody MainRequest mainRequest) {

		HashMap<String, Object> validationMap = new HashMap<String, Object>();
		
		
		  String responseContent = (String) mainRequest.input.get("ResponseContent");
		 
		  validationMap=(HashMap<String, Object>) mainRequest.input.get("ValidationCriteria");
		  String responseType = (String) mainRequest.input.get("ResponseType");
			if(responseType==null)
			{
				responseType="xml";
			}
		JSONObject inputJson = ConstructInputJsonObject(responseContent,validationMap,responseType);
		PCOValidations objPcoValidation = new PCOValidations();

		ResultMap objResultMap = objPcoValidation.ReturnOutputBasedOnResponseValidation(inputJson);

		return objResultMap;

	}

	private JSONObject ConstructInputJsonObject(String responseContent, HashMap<String, Object> validationMap, String responseType) {

		
		JSONObject rootJson = new JSONObject();
		JSONObject inputJson = new JSONObject();
		try {
			inputJson.put("ResponseContent", responseContent);
			
				inputJson.put("ValidationCriteria", validationMap);		
				inputJson.put("ResponseType", responseType);
		
				

			rootJson.accumulate("input", inputJson);
			System.out.println(rootJson.toString());
		//	WritetoOutputFile(rootJson.toString(),"C:\\Users\\C51575A\\Work\\PCOMicroServices\\PCOResponseValidation\\Output.txt");
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return inputJson;
	}
	
	/* Write To Output Json */
	private void WritetoOutputFile(String outputJson, String outputFilePath) {
		ObjectMapper mapper = new ObjectMapper();
		Object json;
		try {
			json = mapper.readValue(outputJson, Object.class);
			outputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

			File file = new File(outputFilePath);
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

		System.out.println("Json Response File Created ,File Path : " + outputFilePath);
	}

	@RequestMapping(value = "test", method = RequestMethod.POST)
	public String test(@RequestBody MainRequest mainRequest) {
		System.out.println(mainRequest.toString());

		// JsonResponseTableMapping
		return mainRequest.toString();
	}
}