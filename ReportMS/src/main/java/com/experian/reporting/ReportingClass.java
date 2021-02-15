package com.experian.reporting;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.experian.reporting.model.OutputClass.ResultMap;
import com.experian.utilities.Constants;

public class ReportingClass {

	public ResultMap TriggerReportJar(String runID) {
		
		DBValidation objDBValidation = new DBValidation();
		String projectName = objDBValidation.ReadProjectNameBasedOnRunId(Long.parseLong(runID));
		System.out.println("Test Project 28Jan:" + projectName);
		//String[] args = new String[] { runID, projectName };
		ReportUtil objReportUtil=new ReportUtil();
		String localFile = objReportUtil.GenerateReport(Long.parseLong(runID), projectName);
		System.out.println("before error");
		String filepath = "";
		ResultMap objResultMap = objDBValidation.ReadRunDetails(Long.parseLong(runID));
		System.out.println("objResultMap: " + objResultMap);
		System.out.println("after error");
		if (objResultMap.getReportLocation() != null) {
			filepath = objResultMap.getReportLocation();
			filepath = filepath.substring(30);
			String[] array = filepath.split("//");
			String append = "";
			for (String string : array) {
				append = append + string + "\\";
			}
			append = append.substring(0, append.length() - 1);
			filepath = Constants.BASE_RemoteServer_PATH + append;
			System.out.println(filepath);
			objResultMap.setReportLocation(filepath);
		}

		if (!objResultMap.getRunStatus().equalsIgnoreCase("Aborted")) {
			objResultMap.setStatusMessage("");
		} else {
			objResultMap.setReportLocation("There is no Reports Available");
		}

		if (objResultMap.getReportLocation().equalsIgnoreCase("null")) {
			objResultMap.setReportLocation("There is no Reports Available");
		}

		System.out.println("Report File Path : " + filepath);
		objResultMap.setLocalFileLocation(localFile);
		return objResultMap;
	}

}
