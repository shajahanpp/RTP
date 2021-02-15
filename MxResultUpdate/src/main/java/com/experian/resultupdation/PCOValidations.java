package com.experian.resultupdation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.experian.resultupdation.OutputClass.ResultMap;
import com.experian.resultupdation.OutputClass.TableResultMap;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

public class PCOValidations {

	public ResultMap ReturnOutputBasedOnResultUpdation(JSONObject inputJson) throws IOException {

		String runID;
		String user;
		long featureID;
		long scenarioID;
		long stepID;
		long scenarioExecId;

		try {
			runID = inputJson.getString("RunId");
			user = inputJson.getString("User");
			JSONArray array_feature = inputJson.getJSONArray("ResultUpdations");
			String arry = array_feature.toString();
			arry = arry.substring(1, arry.length() - 1);
			array_feature = new JSONArray(arry);
			System.out.println(arry);
DBValidation objDBValidation = new DBValidation();
			for (int i = 0; i < array_feature.length(); i++) {

				JSONObject obj_feature = array_feature.getJSONObject(i);
				System.out.println(obj_feature);

				String featureName = obj_feature.getString("TestSuiteName");
				JSONArray array_scenario = obj_feature.getJSONArray("TestCaseList");
				
				try {
					objDBValidation.updateUserinRunTable(runID,user);
					featureID = objDBValidation.AddEntriesToFeatureTable(featureName, Long.parseLong(runID));
					for (int j = 0; j < array_scenario.length(); j++) {
						JSONObject obj_scenario = array_scenario.getJSONObject(j);
						String scenarioName = obj_scenario.getString("TestCaseName");
						String scenarioDescription = obj_scenario.getString("TestCaseDescription");
						String testcaseExecutiontimestamp = obj_scenario.getString("ExecutionTime");
						String testExecutionStatus = "PASSED";

						scenarioID = objDBValidation.AddEntriesToScenarioTable(scenarioName, scenarioDescription, null,
								featureID);
						scenarioExecId = objDBValidation.AddEntriesToScenarioExecutionTable(Long.parseLong(runID),
								featureID, scenarioID, testcaseExecutiontimestamp);
						
						JSONArray array_steps = obj_scenario.getJSONArray("Steps");
						for (int k = 0; k < array_steps.length(); k++) {
							JSONObject obj_step = array_steps.getJSONObject(k);
							String stepName = obj_step.getString("StepName");
							String stepStatus = obj_step.getString("Status");
							if(stepStatus.equalsIgnoreCase("FAILED"))
							{
								testExecutionStatus="FAILED";
							}
							String stepTimestamp = obj_step.getString("ExecutionTime");
							String stepLogName = obj_step.getString("LogName");
							String stepLogContent = obj_step.getString("LogContent");
							stepID = objDBValidation.AddEntriesToStepTable(stepName, scenarioID);
							String projectName = objDBValidation.ReadProjectNameBasedOnRunId(Long.parseLong(runID));
							List<String> directoryList = new ArrayList<String>();
							directoryList.add(runID);
							directoryList.add(featureName);
							directoryList.add(scenarioName);
							directoryList.add(stepName);
							String remoteFilePath = "";
							if (!stepLogName.equals("")) {
								remoteFilePath = Constants.BASE_Local_PATH + runID + "\\" + featureName + "\\"
										+ scenarioName + "\\" + stepName + "\\" + stepLogName + ".json";

								SaveLog(projectName, stepLogName, stepLogContent, directoryList, remoteFilePath);
							}


							long stepExec_Id = objDBValidation.AddEntriesToStepExecutionTable(scenarioExecId, stepID,
									stepStatus);
									System.out.println("Test Logs Updation Started");
							objDBValidation.UpdateTestLogsToStepExecution(stepExec_Id, remoteFilePath);
							System.out.println("Test Logs Updation Completed");
						}
						
						
						
						System.out.println("TestExecution Table Status Updation Started");
						objDBValidation.UpdateTestExecutionStatus(scenarioID, testExecutionStatus);
						System.out.println("TestExecution Table Status Updation Completed");

					}

				} catch (NumberFormatException | SQLException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Run Table Status Updation Started");
			objDBValidation.updateRunTableStatus("Completed", Long.parseLong(runID));
			System.out.println("Run Table Status Updation Completed");
		} catch (JSONException | NumberFormatException | SQLException e) {
			e.printStackTrace();
		}

System.out.println("End of the method");
		return null;
	}

	/* Save Log File To Cloud Server */
	private void SaveLog(String projectName, String stepLogName, String stepLogContent, List<String> directoryList,
			String remoteFilePath) throws IOException {
		
		//System.out.println("Setup SSh Started");
	//	SSHClient sshClient = setupSshj();   
      	
      
      
      //	System.out.println("Setup SSh Completed");

	//	SFTPClient sftp = null;
		File localfile =null;
		File destFile=new File(remoteFilePath);
		String localFile = Constants.LocalFilePath + stepLogName + ".json";
		try {
			localfile = new File(localFile);
			localfile.getParentFile().mkdirs();
			localfile.createNewFile();
			FileOutputStream fooStream = null;
			fooStream = new FileOutputStream(localfile, false);
			byte[] myBytes = stepLogContent.getBytes();
			fooStream.write(myBytes);
			fooStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("Contents copied into Local File");
		try {
			System.out.println("SFTPClient To Start");
		//	sftp = sshClient.newSFTPClient();
			System.out.println("SFTPClient Instance Created");
			String directoryPath = Constants.BASE_Local_PATH;
			for (String directoryName : directoryList) {
				directoryPath = directoryPath + directoryName;
				try {
					File theDir = new File(directoryPath);
					if (!theDir.exists()){
					    theDir.mkdirs();
					}
					//sftp.mkdir(directoryPath);
				} catch (Exception ex) {
					System.out.println(ex.toString());
				}
				directoryPath = directoryPath + "\\";
			}
			
			System.out.println(directoryPath);
			Files.copy(localfile.toPath(), destFile.toPath());
		//	sftp.put(localFile, remoteFilePath);
			System.out.println("Copied Local file to Remote File Using SftpClient");
		

		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			
            System.out.println("Hit in finally block");
		}

	}

	/* Set Up Sshj */
	private SSHClient setupSshj() {
		String remoteHost = Constants.RemoteHost;
		String userName = Constants.UserName;
		String password = Constants.Password;
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		System.out.println("ADDHost Verified");
		try {
			client.connect(remoteHost);
			System.out.println("SSH Connected ");
			client.authPassword(userName, password);
			System.out.println("SSH Auth Credentials ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client;
	}
}
