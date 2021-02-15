/**
 * 
 */
package com.wrapper.clientutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author U56911
 *
 */
public class WrapperUtility {

	/*
	 * Utility to extract all test cases from TestData.xls at a single go
	 */
	public static List<Object> extracted(String testDataPath) {

		RequestExcelUtility excelUtility = new RequestExcelUtility();
		HashMap<String, String> excelDataMap = new HashMap<String, String>();
		List<HashMap<String, String>> listAll = new ArrayList<>();

		excelUtility.setExcel(testDataPath, "Summary");

		List<String> keyList = new ArrayList<String>();
		List<String> valueList = new ArrayList<String>();
		String columnHeader = null;
		String columnValue = null;
		int cell = 0;
		int getsheetRowCount = 0;

		getsheetRowCount = excelUtility.getRowCount();

		do {

			columnHeader = excelUtility.getdata(0, cell);

			cell++;
			if (columnHeader != "") {

				keyList.add(columnHeader);

			}
		} while (columnHeader != "");

		String findKeyword = null;
		List<List<String>> fullcolumnList = new ArrayList<>();

//		List<List<String>> responseValidationsList = new ArrayList<>();
	//	List<List<String>> tableValidationsList = new ArrayList<>();
		
		for (int j = 1; j < getsheetRowCount; j++) {
		
			
			cell = 0;
			for (int k = 0; k < keyList.size(); k++) {

				columnValue = excelUtility.getdata(j, cell);

				valueList.add(columnValue);
				cell++;
			}

//			List<String> responseValidations = new ArrayList<>();
//			List<String> tableValidations = new ArrayList<>();
			
			for (int i = 0; i < keyList.size(); i++) {
				  String value = valueList.get(i);
				if (value != "") {
					excelDataMap.put(keyList.get(i), valueList.get(i));
					/*if(value.startsWith("ResponseValidation"))
					{
						responseValidations.add(value);
					}
					if(value.startsWith("DBTableValidation"))
					{
						tableValidations.add(value);
					}
					*/
				}

			}
			
			listAll.add((HashMap<String, String>) excelDataMap.clone());
		//	responseValidationsList.add(responseValidations);
		//	tableValidationsList.add(tableValidations);
			
			valueList.clear();
			excelDataMap.clear();
		}
		return Arrays.asList(listAll);
	//	return Arrays.asList(listAll, responseValidationsList, tableValidationsList);
	}
}

class Step {
	@JsonProperty("StepName")
	private String StepName;

	@JsonProperty("Status")
	private String Status;

	@JsonProperty("ExecutionTime")
	private String ExecutionTime;

	@JsonProperty("LogName")
	private String LogName;

	@JsonProperty("LogContent")
	private String LogContent;

	@JsonIgnore
	public String getStepName() {
		return StepName;
	}

	public void setStepName(String stepName) {
		StepName = stepName;
	}

	@JsonIgnore
	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	@JsonIgnore
	public String getExecutionTime() {
		return ExecutionTime;
	}

	public void setExecutionTime(String executionTime) {
		ExecutionTime = executionTime;
	}

	@JsonIgnore
	public String getLogName() {
		return LogName;
	}

	public void setLogName(String logName) {
		LogName = logName;
	}

	@JsonIgnore
	public String getLogContent() {
		return LogContent;
	}

	public void setLogContent(String logContent) {
		LogContent = logContent;
	}
}

class TestCase {
	@JsonProperty("TestCaseName")
	private String TestCaseName;
	@JsonProperty("TestCaseDescription")
	private String TestCaseDescription;
	@JsonProperty("ExecutionTime")
	private String ExecutionTime;
	@JsonProperty("TestExecutionStatus")
	private String TestExecutionStatus;
	@JsonProperty("Steps")
	private List<Step> Steps;

	@JsonIgnore
	public String getTestCaseName() {
		return TestCaseName;
	}

	public void setTestCaseName(String testCaseName) {
		TestCaseName = testCaseName;
	}

	@JsonIgnore
	public String getTestCaseDescription() {
		return TestCaseDescription;
	}

	public void setTestCaseDescription(String testCaseDescription) {
		TestCaseDescription = testCaseDescription;
	}

	@JsonIgnore
	public String getExecutionTime() {
		return ExecutionTime;
	}

	public void setExecutionTime(String executionTime) {
		ExecutionTime = executionTime;
	}

	@JsonIgnore
	public String getTestExecutionStatus() {
		return TestExecutionStatus;
	}

	public void setTestExecutionStatus(String testExecutionStatus) {
		TestExecutionStatus = testExecutionStatus;
	}

	@JsonIgnore
	public List<Step> getSteps() {
		return Steps;
	}

	public void setSteps(List<Step> steps) {
		Steps = steps;
	}
}

class ResultUpdation {
	@JsonProperty("TestSuiteName")
	private String TestSuiteName;

	@JsonProperty("TestCaseList")
	private List<TestCase> TestCaseList;

	@JsonIgnore
	public String getTestSuiteName() {
		return TestSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		TestSuiteName = testSuiteName;
	}

	@JsonIgnore
	public List<TestCase> getTestCaseList() {
		return TestCaseList;
	}

	public void setTestCaseList(List<TestCase> testCaseList) {
		TestCaseList = testCaseList;
	}
}

class ResultUpdationOBJECT {
	@JsonProperty("RunId")
	private String RunId;

	@JsonProperty("User")
	private String User;

	@JsonProperty("ResultUpdations")
	private List<ResultUpdation> ResultUpdations;

	@JsonIgnore
	public String getRunId() {
		return RunId;
	}

	public void setRunId(String runId) {
		RunId = runId;
	}

	@JsonIgnore
	public String getUser() {
		return User;
	}

	public void setUser(String user) {
		User = user;
	}

	@JsonIgnore
	public List<ResultUpdation> getResultUpdations() {
		return ResultUpdations;
	}

	public void setResultUpdations(List<ResultUpdation> resultUpdations) {
		ResultUpdations = resultUpdations;
	}
}

class ResultUpdationMain {

	@JsonProperty("input")
	private ResultUpdationOBJECT input;

	@JsonIgnore
	public ResultUpdationOBJECT getInput() {
		return input;
	}

	public void setInput(ResultUpdationOBJECT input) {
		this.input = input;
	}
}
