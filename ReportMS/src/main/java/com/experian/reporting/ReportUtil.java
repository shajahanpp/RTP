package com.experian.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.experian.utilities.Constants;
import com.experian.utilities.CustomException;
import com.experian.utilities.DBDriver;
import com.experian.utilities.HashMapUtility.KeywordMap;
import com.experian.utilities.HashMapUtility.RunMap;
import com.experian.utilities.HashMapUtility.TestCaseMap;
import com.experian.utilities.HashMapUtility.TestSuiteMap;


import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;

public class ReportUtil {

	boolean ischeck = false;

	public String GenerateReport(long runId, String projectName) {
		String localFile = "";
		try {

			RunMap runMap = new RunMap();
			DBDriver dbDriver = new DBDriver();
			String test_approach = dbDriver.ReadTestApproachfromRunTable(runId);

			List<TestSuiteMap> suitemapList = dbDriver.readFeatureMaster(runId, projectName);
			localFile = CreateBDDReport(suitemapList, projectName, runId);

			dbDriver.CloseDBConnection();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CleanAllFiles();
			return localFile;
		}
	}

	private void CleanAllFiles() {

		File directory = new File(Constants.LocalFilePath);
		String listAllFiles[] = directory.list();
		
//		for (File file : directory.listFiles()) {
//			file.delete();
//		}
		
		for (String filePath : listAllFiles) {
			File file = new File(filePath);
			try {
				boolean result = Files.deleteIfExists(file.toPath());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private String CreateBDDReport(List<TestSuiteMap> suitemapList, String projectName, long runId) {
		Boolean check_report_flush = Boolean.valueOf(true);
		String localFile = "";
		try {
			String fileName;

			String timeStamp = new Timestamp(new Date().getTime()).toString().replace(' ', '_');
			fileName = ("ExtentReport_" + runId + "_" + timeStamp.replace(':', '.') + ".html");
			fileName = fileName.replaceAll(" ", "");
//			SSHUtil objSshutil = new SSHUtil();
//			SSHClient sshClient = objSshutil.setupSshj();

//			SFTPClient sftp = sshClient.newSFTPClient();

			localFile = Constants.LocalFilePath + fileName;

			File file = new File(localFile);

			file.getParentFile().mkdirs();
			file.createNewFile();
			String reportLoc = Constants.BASE_RemoteServer_PATH + projectName + "\\resource\\ExtendResults\\";
			ExtentHtmlReporter htmlReports = new ExtentHtmlReporter(localFile);
			ExtentReports extent = new ExtentReports();
			extent.attachReporter(htmlReports);
			htmlReports.config().setReportName("Regression Testing");
			htmlReports.config().setTheme(Theme.STANDARD);
			htmlReports.config().setDocumentTitle("HtmlReportsTestResults");
			
			ExtentTest logger_feature;
			String directoryPath = "";
			for (TestSuiteMap testSuiteMap : suitemapList) {
				logger_feature = extent.createTest((testSuiteMap.getTestsuiteName()));
				if (testSuiteMap.getHashmapTestCase() != null) {
					for (List<TestCaseMap> valueListTestcase : testSuiteMap.getHashmapTestCase().values()) {
						for (TestCaseMap testcase : valueListTestcase) {
							if (testcase.getTestExecutionStatus() != null) {
								ExtentTest logger_scenario = logger_feature.createNode(testcase.getTestCaseName());

								logger_scenario.log(Status.INFO, "Scenario : '" + testcase.getTestCaseDesc()
										+ "' Executed In '" + testcase.getTestBrowser() + "'");
								logger_scenario.log(Status.INFO, "Scenario : '" + testcase.getTestCaseDesc()
										+ "' Executed On '" + testcase.getTestExecutionTime() + "'");
								logger_scenario.log(Status.INFO, "Check Scenario Execution Details Listed Below: ");
								logger_scenario.log(Status.INFO,
										"Scenario Executed time : " + testcase.getTestExecutionTime());
								if (testcase.getTestExecutionStatus().equalsIgnoreCase("PASSED")) {
									logger_scenario.log(Status.PASS,
											"Scenario Execution Status : '" + testcase.getTestCaseName() + "' Passed");
								} else if (testcase.getTestExecutionStatus().equalsIgnoreCase("FAILED")) {
									logger_scenario.log(Status.FAIL,
											"Scenario Execution Status : '" + testcase.getTestCaseName()
													+ "' Failed, Please check the Scenario Steps for more details");
								} else if (testcase.getTestExecutionStatus().equalsIgnoreCase("SKIPPED")) {
									logger_scenario.log(Status.SKIP,
											"Test Execution Status : '" + testcase.getTestCaseName() + "' Skipped ");
								}
								if (!testcase.getTestExecutionStatus().equalsIgnoreCase("SKIPPED")) {
									int stepCount;
									if (testcase.getKeywordHashMap() != null) {
										for (List<KeywordMap> keywordList : testcase.getKeywordHashMap().values()) {
											stepCount = 1;
											String testCaseFunctionality;
											ExtentTest image;
											for (KeywordMap keyword : keywordList) {

//												directoryPath = CreateDirectories(projectName, String.valueOf(runId),
//														testSuiteMap.getTestsuiteName(), testcase.getTestCaseName(),
//														keyword.getKeyword(), sftp);

												testCaseFunctionality = keyword.getKeyword();
												logger_scenario.log(Status.INFO,
														"Test Step '" + stepCount + "' : '" + testCaseFunctionality);

												logger_scenario.log(Status.INFO,
														"Test Step '" + stepCount + "' : '" + testCaseFunctionality
																+ "' Functionality executed on '"
																+ keyword.getKeywordExecutionTime() + "'");

												if (keyword.getKeywordExecutionStatus().equalsIgnoreCase("PASSED")) {
													logger_scenario.log(Status.PASS,
															"'" + testCaseFunctionality + "' Passed");

												} else if (keyword.getKeywordExecutionStatus()
														.equalsIgnoreCase("FAILED")) {

													logger_scenario.log(Status.FAIL,
															"'" + testCaseFunctionality + "' Failed");

													if (keyword.getFailedScreenshotDet() != null) {
														if (!keyword.getFailedScreenshotDet().equals("")) {

															if (keyword.getFailedScreenshotDet().contains("//")) {
//																String imagePath = GetFileName(
//																		keyword.getFailedScreenshotDet(), ".png",
//																		projectName, sftp, directoryPath);
//																if (imagePath != "") {
//																	logger_scenario.log(Status.INFO,
//																			" Please check the Failed Screen shot Details");
//																	logger_scenario.fail("", MediaEntityBuilder
//																			.createScreenCaptureFromPath(imagePath)
//																			.build());
//																}

//																PrintBDDLogFile(keyword.getFailedScreenshotDet(),
//																		logger_scenario, projectName, sftp,
//																		directoryPath);

															} else {
																logger_scenario.log(Status.INFO,
																		keyword.getFailedScreenshotDet());
															}
														}
													}
												} else if (keyword.getKeywordExecutionStatus()
														.equalsIgnoreCase("SKIPPED")) {
													logger_scenario.log(Status.FAIL,
															"'" + testCaseFunctionality + "' Skipped");
												}
												if (keyword.getLogs() != null) {
//													PrintBDDTestLogs(logger_scenario, keyword.getLogs(), sftp,
//															directoryPath);
													
													PrintBDDTestLogs(logger_scenario, keyword.getLogs(),
														directoryPath);
												}
												stepCount = stepCount + 1;

											}
										}
//										PrintAPIFiles(logger_scenario, runId, projectName,
//												testSuiteMap.getTestsuiteName(), testcase.getTestCaseName());
										PrintLogFiles(logger_scenario, runId, projectName,
												testSuiteMap.getTestsuiteName(), testcase.getTestCaseName());
										PrintAPIFilesLegacy(logger_scenario, runId, projectName,
												testSuiteMap.getTestsuiteName(), testcase.getTestCaseName());

									} else {
										logger_scenario.log(Status.INFO,
												"There are no keywords added for the Test Case "
														+ testcase.getTestCaseName());
									}
								}
								// this.report.endTest(this.logger);
								extent.flush();
								check_report_flush = Boolean.valueOf(true);
							}
						}
					}
				}
			}
//			sftp.put(localFile, reportLoc + fileName);
			UpdateFileNameToDB(Constants.BASE_PROJECT_PATH + projectName + "//resource//ExtendResults//" + fileName,
					runId);

			String filePath = null;

			filePath = Constants.BASE_RemoteServer_PATH + projectName + "\\resource\\ExtendResults\\";

//			Thread.sleep(1000L);
			if (check_report_flush.booleanValue()) {
				// webDriver.get(filePath + fileName);
				System.out.println("Report FilePath is " + filePath + fileName);
				/*
				 * webDriver.close(); webDriver.quit();
				 */
			} else {
				// webDriver.close();
				// webDriver.quit();
				System.out.println("Reports are not generated properly");
			}
			// webDriver.quit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return localFile;
	}

	private void PrintBDDTestLogs(ExtentTest logger_scenario, String logsFilePath, String directoryPath) {
		
		
		  File f = new File(logsFilePath);
		  
		  if (f.isFile() && !f.isDirectory())
		 
		
	
		logger_scenario.log(Status.INFO, "<a href='" + f.getAbsolutePath() + "'> " + f.getName() + "</a>");
		
	}

	private String CreateDirectories(String projectName, String runID, String featureName, String scenarioName,
			String stepName, SFTPClient sftp) {
		List<String> directoryList = new ArrayList<String>();
		directoryList.add(projectName);
		directoryList.add(runID);
		directoryList.add(featureName);
		directoryList.add(scenarioName);
		directoryList.add(stepName);
		String directoryPath = Constants.BASE_PROJECT_PATH;
		for (String directoryName : directoryList) {
			directoryPath = directoryPath + directoryName;
//			File file = new File(directoryPath);

			directoryPath = directoryPath + "\\";
		}
		File dirFilePath = new File(directoryPath);
		dirFilePath.mkdirs();
		return directoryPath;

	}

	private void PrintBDDTestLogs(ExtentTest logger_scenario, String logFilePath, SFTPClient sftp,
			String directoryPath) {
		String remoteFilepath = GetFilesUsingSFTP(logFilePath, sftp, directoryPath, "TestLogs");

		String remoteFileName = GetFileNameFromFilePath(remoteFilepath);
	
		if(!remoteFileName.equals(""))
		logger_scenario.log(Status.INFO, "<a href='" + remoteFilepath + "'> " + remoteFileName + "</a>");

	}

	private String GetFilesUsingSFTP(String logFilePath, SFTPClient sftp, String directoryPath, String folderName) {
		boolean is_dir_exists = CreateLocalFileDirectory(directoryPath, folderName);
		if (is_dir_exists) {
			directoryPath = directoryPath + folderName + "\\";
		}
		File file = null;
		String fileName = GetFileNameFromFilePath(logFilePath);
		
		return logFilePath;
	}

	private boolean CreateLocalFileDirectory(String directoryPath, String folderName) {
		File file = new File(directoryPath + folderName);
		boolean isDir = file.mkdir();
		return isDir;
	}

	private String GetFileNameFromFilePath(String logFilePath) {
		String[] filesplit = logFilePath.split("\\\\");
		String fileName = filesplit[filesplit.length - 1];
		return fileName;
	}

	private void PrintLogFiles(ExtentTest logger_scenario, long runId, String projectName, String featureName,
			String scenarioName) throws IOException {
		String filepath = Constants.BASE_PROJECT_PATH + "//" + projectName + "//resource//Logs//" + runId + "//"
				+ featureName.split("\\.")[0] + "//" + scenarioName;

		filepath = filepath.substring(30);
		String[] array = filepath.split("//");
		String append = "";
		for (String string : array) {
			append = append + string + "\\";
		}
		append = append.substring(0, append.length() - 1);
		filepath = Constants.BASE_RemoteServer_PATH + append;

		File dir = new File(filepath);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			// logger_scenario.log(Status.INFO,
			// "List of Difference with Expected Output File is listed below :");
			for (File req_child : directoryListing) {

				InputStream is = new FileInputStream(req_child.getAbsolutePath());
				BufferedReader buf = new BufferedReader(new InputStreamReader(is));
				String line = buf.readLine();
				StringBuilder sb = new StringBuilder();
				while (line != null) {
					sb.append(line).append("\n");
					line = buf.readLine();
				}
				int flag = 1;
				String fileAsString = sb.toString();
				System.out.println("Contents : " + fileAsString);
				String[] details = fileAsString.split(";");
				if (details != null) {
					if (details.length > 1) {
						logger_scenario.log(Status.INFO,
								"List of Difference with Expected Output File is listed below :");
						for (String det : details) {
							if (!det.equals(""))

								logger_scenario.log(Status.INFO, det);
						}

					} else {
						//logger_scenario.log(Status.INFO,
						//		"There are no differences while comparing the legacy and the current responses");
					}
				}

			}
		}

	}

	private void PrintAPIFiles(ExtentTest logger_scenario, long runId, String projectName, String featureName,
			String scenarioName) {

		String request_file_path = Constants.BASE_PROJECT_PATH + "//" + projectName + "//resource//APIFiles//Request//"
				+ runId + "//" + featureName.split("\\.")[0] + "//" + scenarioName;
		String response_file_path = Constants.BASE_PROJECT_PATH + "//" + projectName

				+ "//resource//APIFiles//Response//" + runId + "//" + featureName.split("\\.")[0] + "//" + scenarioName;
		request_file_path = request_file_path.substring(30);
		String[] array = request_file_path.split("//");
		String append = "";
		for (String string : array) {
			append = append + string + "\\";
		}
		append = append.substring(0, append.length() - 1);
		request_file_path = Constants.BASE_RemoteServer_PATH + append;
		response_file_path = response_file_path.substring(30);
		array = response_file_path.split("//");
		append = "";
		for (String string : array) {
			append = append + string + "\\";
		}
		append = append.substring(0, append.length() - 1);
		response_file_path = Constants.BASE_RemoteServer_PATH + append;

		File dir = new File(request_file_path);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File req_child : directoryListing) {

				System.out.println(req_child.getAbsolutePath());
				logger_scenario.log(Status.INFO, "<a href='" + req_child.getAbsolutePath() + "'>  Request file</a>");
			}
		}

		dir = new File(response_file_path);
		directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File resp_child : directoryListing) {
				logger_scenario.log(Status.INFO, "<a href='" + resp_child.getAbsolutePath() + "'>  Response file</a>");
			}
		}

	}

	private void PrintAPIFilesLegacy(ExtentTest logger_scenario, long runId, String projectName, String featureName,
			String scenarioName) {

		String request_file_path = Constants.BASE_PROJECT_PATH + "//" + projectName + "//resource//APIFiles//Request//"
				+ runId + "//" + featureName.split("\\.")[0] + "//" + scenarioName;
		String response_file_path = Constants.BASE_PROJECT_PATH + "//" + projectName

				+ "//resource//Output//" + runId + "//" + featureName.split("\\.")[0] + "//" + scenarioName;
		request_file_path = request_file_path.substring(30);
		String[] array = request_file_path.split("//");
		String append = "";
		for (String string : array) {
			append = append + string + "\\";
		}
		append = append.substring(0, append.length() - 1);
		request_file_path = Constants.BASE_RemoteServer_PATH + append;
		response_file_path = response_file_path.substring(30);
		array = response_file_path.split("//");
		append = "";
		for (String string : array) {
			append = append + string + "\\";
		}
		append = append.substring(0, append.length() - 1);
		response_file_path = Constants.BASE_RemoteServer_PATH + append;

		File dir = new File(request_file_path);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File req_child : directoryListing) {

				System.out.println(req_child.getAbsolutePath());
				logger_scenario.log(Status.INFO,
						"<a href='" + req_child.getAbsolutePath() + "'>  Legacy Request file</a>");
			}
		}

		dir = new File(response_file_path);
		directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File resp_child : directoryListing) {
				logger_scenario.log(Status.INFO,
						"<a href='" + resp_child.getAbsolutePath() + "'>  Legacy Response file</a>");
			}
		}

	}

	private void PrintBDDLogFile(String logFilePath, ExtentTest logger_scenario, String projectName, SFTPClient sftp,
			String directoryPath) throws CustomException {

		logFilePath = logFilePath.substring(17);
		String[] array = logFilePath.split("//");
		String append = "";
		for (String string : array) {
			append = append + string + "\\";
		}
		append = append.substring(0, append.length() - 1);
		logFilePath = Constants.BASE_RemoteServer_PATH + append;
		System.out.println("Log File Path is " + logFilePath);
		String remoteFilePath = GetFilesUsingSFTP(logFilePath, sftp, directoryPath, "BDDLogs");
		
			logger_scenario.log(Status.INFO, "Please check the Log file");
			logger_scenario.log(Status.INFO, "<a href='" + remoteFilePath + "'>  Log  file</a>");
		
	}

	private String GetFileName(String failedScreenshotDet, String fileExt, String projectName, SFTPClient sftp,
			String directoryPath) throws CustomException {
		String screenshotFilePath = "";
		if (fileExt.equals(".png")) {
			String[] split_path = failedScreenshotDet.split("//");
			split_path = Arrays.copyOf(split_path, split_path.length - 1);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < split_path.length; i++) {
				builder.append(split_path[i]).append("//");
			}
			failedScreenshotDet = builder.toString();

			if (failedScreenshotDet != "" && failedScreenshotDet.contains("//")) {
				boolean is_dir_exists = CreateLocalFileDirectory(directoryPath, "FailedTestScreenshots");
				if (is_dir_exists) {
					directoryPath = directoryPath + "FailedTestScreenshots" + "\\";
				}
				try {
					sftp.symlink(failedScreenshotDet, directoryPath);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				File filesftp = null;

				try {
					sftp.get(failedScreenshotDet, directoryPath);

					filesftp = new File(directoryPath);

				} catch (IOException e) {
					e.printStackTrace();
				}
				File dir = new File(failedScreenshotDet);
				File[] files = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(fileExt);
					}
				});

				for (File file : files) {
					System.out.println(file);
					screenshotFilePath = file.getAbsolutePath();

					System.out.println(screenshotFilePath);

					screenshotFilePath = screenshotFilePath.substring(27);
					String[] array = screenshotFilePath.split("//");
					String append = "";
					for (String string : array) {
						append = append + string + "\\";
					}
					append = append.substring(0, append.length() - 1);
					screenshotFilePath = Constants.BASE_RemoteServer_PATH + append;

					System.out.println(screenshotFilePath);

				}
			}
		}
		return screenshotFilePath;
	}

	private void UpdateFileNameToDB(String reportFilePath, long run_id) {
		try {
			DBDriver dbDriver = new DBDriver();
			dbDriver.UpdateFileNameToDB(reportFilePath, Long.valueOf(run_id));
			dbDriver.updateRunTableStatus("Completed", run_id);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void GetApiInputFilesForEachKeyword(KeywordMap keyword, String projectName, ExtentTest logger_feature,
			long runId) {
		String filePath = "";
		String fileType = "";

		for (Map.Entry map : keyword.getInputFiles().entrySet()) {

			filePath = Constants.BASE_RemoteServer_PATH + projectName + "\\resource\\APIFiles\\"
					+ map.getKey().toString() + "\\" + runId + "\\" + map.getValue().toString();

			fileType = map.getKey().toString();
			logger_feature.log(Status.INFO, "Please check the '" + map.getKey().toString() + "' file");
			logger_feature.log(Status.INFO, "<a href='" + filePath + "'>" + map.getKey().toString() + "  file</a>");
		}
	}

	private String GetScreenshotForEachKeyword(String passOrFailedScreenshotName, String screenshotfolderName,
			String status, String applicationType, String projectName) throws IOException, CustomException {
		ExtentTest image = null;
		String screenshotPathnew = null;
		String screenshotPath;
		if (applicationType.equalsIgnoreCase("WEB")) {
			int len = (screenshotfolderName.split("-")[0] + '-' + screenshotfolderName.split("-")[1]).length();
			String suiteName = screenshotfolderName.split("-")[0];

			System.out
					.println("Folder Name: " + screenshotfolderName + " Length: " + len + " Suite Name: " + suiteName);

			System.out.println("check1");
			if (passOrFailedScreenshotName != null) {
				System.out.println("check2 is " + passOrFailedScreenshotName);
				screenshotPath = passOrFailedScreenshotName;

				System.out.println("check3");

				System.out.println("check4");
				screenshotPath = screenshotPath.substring(30);
				String[] array = screenshotPath.split("//");
				String append = "";
				for (String string : array) {
					append = append + string + "\\";
				}
				append = append.substring(0, append.length() - 1);
				screenshotPathnew = Constants.BASE_RemoteServer_PATH + append;
				System.out.println(screenshotPathnew);

			}
		}
		return screenshotPathnew;
	}

}
