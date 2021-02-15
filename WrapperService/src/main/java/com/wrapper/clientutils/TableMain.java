package com.wrapper.clientutils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONObject;


public class TableMain {

	/* Application Generation Client Side TableMain Class */
	public static String main(String[] args) {
		
		JSONObject inputObj = null;
		System.out.println("Table Validation Client Side Started ");

		if (args.length != 0) {
			if (args.length != 1) {

				System.out.println("\nARGS : "+ "\n1. "+args[0]+"\n2. "+args[1]+"\n3. "+args[2]+"\n4. "+args[3]+"\n\n");
				
				String jar_filepath = "";
				try {
					jar_filepath = new File(TableMain.class.getProtectionDomain().getCodeSource().getLocation().toURI())
							.getPath();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

				String excelinputPath = args[0];
				String referenceID = args[1];
				String masterTestDataPath = args[2];
				String masterTestDataId = args[3];

				/*
				 * String excelinputPath =
				 * "C:\\Users\\C51575A\\Work\\PCOMicroServices\\O2ScreenlessAutomation\\ScreenLessAutomation_O2\\TestData\\TestData.xls";
				 * String referenceID = "DBTableValidation_1"; String masterTestDataPath =
				 * "C:\\Users\\C51575A\\Work\\PCOMicroServices\\O2ScreenlessAutomation\\ScreenLessAutomation_O2\\TestData\\MasterTestDatasheet.xlsx";
				 * String masterTestDataId = "1";
				 */
				TableValidations objValidations = new TableValidations();

				jar_filepath = jar_filepath.substring(0, jar_filepath.lastIndexOf(File.separator));
				String referenceidFolder = objValidations.GetInputReferenceID(referenceID);
				String response_filepath = jar_filepath + "\\" + referenceidFolder + "\\" + referenceID + ".json";
				objValidations.DeleteFileIfExists(response_filepath);
				HashMap<String, String> excelDataMap = objValidations.ReadExcelData(excelinputPath, referenceID);
				String dbSheetName = "Configuration";
				String connectionString = objValidations.GetConnectionString(excelinputPath, dbSheetName);
//				System.out.println("Connection String"+ connectionString);
				
			    inputObj = objValidations.ConstructInputJson(excelDataMap, connectionString,
						masterTestDataPath, masterTestDataId, referenceidFolder, excelinputPath);

//				objValidations.WriteToFile(inputObj, referenceID, response_filepath);

				System.out.println("\n\nTable Validation Client Side Completed.  :  " + referenceID + ".json");

			} else {
				System.out.println("Parameter Expected; Input Reference Id");
			}

		} else {

			System.out.println("Parameters Expected; Please pass Test Input Data File Path and Input Reference Id");
		}
       return inputObj.toString();
	}

}
