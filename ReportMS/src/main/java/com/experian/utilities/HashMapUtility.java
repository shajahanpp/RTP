package com.experian.utilities;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HashMapUtility {

	public static class RunMap {
		private long id;
		private String projectName;
		private HashMap<Long, List<TestSuiteMap>> hashmapTestSuite;

		public HashMap<Long, List<TestSuiteMap>> getHashmapTestSuite() {
			return hashmapTestSuite;
		}

		public void setHashmapTestSuite(HashMap<Long, List<TestSuiteMap>> testsuiteHashMap) {
			this.hashmapTestSuite = testsuiteHashMap;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}
	}

	/* Test Suite Class */
	public static class TestSuiteMap {
		private long id;
		// private String testsuiteId;
		private String testSuiteDesc;
		private String testsuiteName;
		private String executionStatus;
		private String testBrowser;
		private String testUrl;
		private HashMap<Long, List<TestCaseMap>> hashmapTestCase;
		private TestRun testRun;
		private int totalTestCount;
		private String projectName;

		public TestSuiteMap() {
			testRun = new TestRun();
		}

		/*
		 * public String getTestsuiteId() { return testsuiteId; }
		 * 
		 * public void setTestsuiteId(String testsuiteId) { this.testsuiteId =
		 * testsuiteId; }
		 */
		public String getTestBrowser() {
			return testBrowser;
		}

		public void setTestBrowser(String testBrowser) {
			this.testBrowser = testBrowser;
		}

		public String getTestUrl() {
			return testUrl;
		}

		public void setTestUrl(String testUrl) {
			this.testUrl = testUrl;
		}

		public HashMap<Long, List<TestCaseMap>> getHashmapTestCase() {
			return hashmapTestCase;
		}

		public void setHashmapTestCase(HashMap<Long, List<TestCaseMap>> hashmapTestCase) {
			this.hashmapTestCase = hashmapTestCase;
		}

		public TestRun getTestRun() {
			return testRun;
		}

		public void setTestRun(TestRun testRun) {
			this.testRun = testRun;
		}

		public String getTestsuiteName() {
			return testsuiteName;
		}

		public void setTestsuiteName(String testsuiteName) {
			this.testsuiteName = testsuiteName;
		}

		public int getTotalTestCount() {
			return totalTestCount;
		}

		public void setTotalTestCount(int totalTestCount) {
			this.totalTestCount = totalTestCount;
		}

		public String getExecutionStatus() { // whether the suite has been executed
			return executionStatus;
		}

		public void setExecutionStatus(String executionStatus) {
			this.executionStatus = executionStatus;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getTestSuiteDesc() {
			return testSuiteDesc;
		}

		public void setTestSuiteDesc(String testSuiteDesc) {
			this.testSuiteDesc = testSuiteDesc;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}
	}

	/* Test Case Class */
	public static class TestCaseMap {
		private long id;
		// private String testCaseId;
		private String testCaseName;
		private String testCaseDesc;
		private Boolean testExecutionFlag;
		private long testcase_ExecutionId;
		private String testExecutionStatus;
		private Date testExecutionTime;
		private List<String> testInfos;
		private String testBrowser;
		private String nodeUrl;
		// private String testSuiteId;
		private Boolean isExecute;
		private int testCount;
		private List<String> testcaseLogs;

		private HashMap<Long, List<KeywordMap>> keywordHashMap;

		/*
		 * public String getTestCaseId() { return testCaseId; }
		 * 
		 * public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId;
		 * }
		 */

		public String getTestCaseName() {
			return testCaseName;
		}

		public void setTestCaseName(String testCaseName) {
			this.testCaseName = testCaseName;
		}

		public Boolean getTestExecutionFlag() {
			return testExecutionFlag;
		}

		public void setTestExecutionFlag(Boolean testExecutionFlag) {
			this.testExecutionFlag = testExecutionFlag;
		}

		public HashMap<Long, List<KeywordMap>> getKeywordHashMap() {
			return keywordHashMap;
		}

		public void setKeywordHashMap(HashMap<Long, List<KeywordMap>> keywordHashMap) {
			this.keywordHashMap = keywordHashMap;
		}

		public int getTestCount() {
			return testCount;
		}

		public void setTestCount(int testCount) {
			this.testCount = testCount;
		}

		public String getTestExecutionStatus() {
			return testExecutionStatus;
		}

		public void setTestExecutionStatus(String testExecutionStatus) {
			this.testExecutionStatus = testExecutionStatus;
		}

		public Date getTestExecutionTime() {
			return testExecutionTime;
		}

		public void setTestExecutionTime(Date testExecutionTime) {
			this.testExecutionTime = testExecutionTime;
		}

		public List<String> getTestInfos() {
			return testInfos;
		}

		public void setTestInfos(List<String> testInfos) {
			this.testInfos = testInfos;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getTestCaseDesc() {
			return testCaseDesc;
		}

		public void setTestCaseDesc(String testCaseDesc) {
			this.testCaseDesc = testCaseDesc;
		}

		public String getTestBrowser() {
			return testBrowser;
		}

		public void setTestBrowser(String testBrowser) {
			this.testBrowser = testBrowser;
		}

		public String getNodeUrl() {
			return nodeUrl;
		}

		public void setNodeUrl(String nodeUrl) {
			this.nodeUrl = nodeUrl;
		}

		public Boolean getIsExecute() {
			return isExecute;
		}

		public void setIsExecute(Boolean isExecute) {
			this.isExecute = isExecute;
		}

		public long getTestcase_ExecutionId() {
			return testcase_ExecutionId;
		}

		public void setTestcase_ExecutionId(long testcase_ExecutionId) {
			this.testcase_ExecutionId = testcase_ExecutionId;
		}

		public List<String> getTestcaseLogs() {
			return testcaseLogs;
		}

		public void setTestcaseLogs(List<String> testcaseLogs) {
			this.testcaseLogs = testcaseLogs;
		}

		/*
		 * public String getTestSuiteId() { return testSuiteId; }
		 * 
		 * public void setTestSuiteId(String testSuitId) { this.testSuiteId =
		 * testSuitId; }
		 */

	}

	/* Keyword Class */
	public static class KeywordMap {
		private Long keywordId;
		private String keyword;
		private String applicationType;
		private String className;
		private HashMap<Long, List<DataInputMap>> dataInputHashMap;
		private String keywordExecutionStatus;
		private Date keywordExecutionTime;
		private String failedScreenshotName;
		private String failedScreenshotDet;
		private String folderName;
		private List<String> dataInputHeaderNames;
		private HashMap<String, String> inputFiles;
		private String logs;
		private Long maxorder;
		private Long execordermax;

		private String runIdtcId;

		public long getKeywordId() {
			return keywordId;
		}

		public void setKeywordId(long keywordId) {
			this.keywordId = keywordId;
		}

		public String getKeyword() {
			return keyword;
		}

		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}

		public String getApplicationType() {
			return applicationType;
		}

		public void setApplicationType(String applicationType) {
			this.applicationType = applicationType;
		}

		public HashMap<Long, List<DataInputMap>> getDataInputHashMap() {
			return dataInputHashMap;
		}

		public void setDataInputHashMap(HashMap<Long, List<DataInputMap>> dataInputHashMap) {
			this.dataInputHashMap = dataInputHashMap;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getKeywordExecutionStatus() {
			return keywordExecutionStatus;
		}

		public void setKeywordExecutionStatus(String keywordExecutionStatus) {
			this.keywordExecutionStatus = keywordExecutionStatus;
		}

		public Date getKeywordExecutionTime() {
			return keywordExecutionTime;
		}

		public void setKeywordExecutionTime(Date keywordExecutionTime) {
			this.keywordExecutionTime = keywordExecutionTime;
		}

		public String getPassOrFailedScreenshotName() {
			return failedScreenshotName;
		}

		public void setPassOrFailedScreenshotName(String failedScreenshotName) {
			this.failedScreenshotName = failedScreenshotName;
		}

		public String getFolderName() {
			return folderName;
		}

		public void setFolderName(String folderName) {
			this.folderName = folderName;
		}

		public List<String> getDataInputHeaderNames() {
			return dataInputHeaderNames;
		}

		public void setDataInputHeaderNames(List<String> dataInputHeaderNames) {
			this.dataInputHeaderNames = dataInputHeaderNames;
		}

		public String getFailedScreenshotDet() {
			return failedScreenshotDet;
		}

		public void setFailedScreenshotDet(String failedScreenshotDet) {
			this.failedScreenshotDet = failedScreenshotDet;
		}

		public HashMap<String, String> getInputFiles() {
			return inputFiles;
		}

		public void setInputFiles(HashMap<String, String> inputFiles) {
			this.inputFiles = inputFiles;
		}

		public String getLogs() {
			return logs;
		}

		public void setLogs(String logs) {
			this.logs = logs;
		}

		public Long getMaxorder() {
			return maxorder;
		}

		public void setMaxorder(Long maxorder) {
			this.maxorder = maxorder;
		}

		public Long getExecordermax() {
			return execordermax;
		}

		public void setExecordermax(Long execordermax) {
			this.execordermax = execordermax;
		}

		public String getRunIdtcId() {
			return runIdtcId;
		}

		public void setRunIdtcId(String runIdtcId) {
			this.runIdtcId = runIdtcId;
		}

		/*
		 * public String getTestSuiteID() { return testSuiteID; }
		 * 
		 * public void setTestSuiteID(String testSuiteID) { this.testSuiteID =
		 * testSuiteID; }
		 */
	}

	/* DataInput Class */
	public static class DataInputMap {
		private String dataInputValue;

		public String getDataInputValue() {
			return dataInputValue;
		}

		public void setDataInputValue(String dataInputValue) {
			this.dataInputValue = dataInputValue;
		}
	}

	/* Class for storing Test Suite Execution Details */
	public static class TestRun {
		private long runId;
		private String testRunStatus;
		private Date testRunDate;

		public String getTestRunStatus() {
			return testRunStatus;
		}

		public void setTestRunStatus(String testRunStatus) {
			this.testRunStatus = testRunStatus;
		}

		public Date getTestRunDate() {
			return testRunDate;
		}

		public void setTestRunDate(Date testRunDate) {
			this.testRunDate = testRunDate;
		}

		public long getRunId() {
			return runId;
		}

		public void setRunId(long runId) {
			this.runId = runId;
		}

	}

	public static class TestExecutionDetails {
		private int runId;
		private String suiteId;
		private HashMap<String, List<TestCaseList>> testCaseList;

		public int getRunId() {
			return runId;
		}

		public void setRunId(int runId) {
			this.runId = runId;
		}

		public String getSuiteId() {
			return suiteId;
		}

		public void setSuiteId(String suiteId) {
			this.suiteId = suiteId;
		}

		public HashMap<String, List<TestCaseList>> getTestCaseList() {
			return testCaseList;
		}

		public void setTestCaseList(HashMap<String, List<TestCaseList>> testCaseList) {
			this.testCaseList = testCaseList;
		}

	}

	public static class TestCaseList {
		private String testcaseId;
		private int testcaseExecutionId;
		private String testCaseExecutionStatus;
		private HashMap<Integer, List<KeywordList>> keywordList;

		public String getTestcaseId() {
			return testcaseId;
		}

		public void setTestcaseId(String testcaseId) {
			this.testcaseId = testcaseId;
		}

		public int getTestcaseExecutionId() {
			return testcaseExecutionId;
		}

		public void setTestcaseExecutionId(int testcaseExecutionId) {
			this.testcaseExecutionId = testcaseExecutionId;
		}

		public String getTestCaseExecutionStatus() {
			return testCaseExecutionStatus;
		}

		public void setTestCaseExecutionStatus(String testCaseExecutionStatus) {
			this.testCaseExecutionStatus = testCaseExecutionStatus;
		}

		public HashMap<Integer, List<KeywordList>> getKeywordList() {
			return keywordList;
		}

		public void setKeywordList(HashMap<Integer, List<KeywordList>> keywordList) {
			this.keywordList = keywordList;
		}

	}

	public static class KeywordList {
		private int keywordId;
		private int keywordExecutionId;
		private String keywordExecutionStatus;

		public int getKeywordId() {
			return keywordId;
		}

		public void setKeywordId(int keywordId) {
			this.keywordId = keywordId;
		}

		public int getKeywordExecutionId() {
			return keywordExecutionId;
		}

		public void setKeywordExecutionId(int keywordExecutionId) {
			this.keywordExecutionId = keywordExecutionId;
		}

		public String getKeywordExecutionStatus() {
			return keywordExecutionStatus;
		}

		public void setKeywordExecutionStatus(String keywordExecutionStatus) {
			this.keywordExecutionStatus = keywordExecutionStatus;
		}

	}

}
