package com.wrapper.clientutils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;

import java.util.HashMap;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseMain {

	public static String main(String[] args) throws XPathExpressionException {
		JSONObject inputObj = null ;
		System.out.println("Response Validation Client Side Started ");

		if (args.length != 0) {
			if (args.length != 1) {
				if (args.length != 2) {

					String jar_filepath = "";
					try {
						jar_filepath = new File(ResponseMain.class.getProtectionDomain().getCodeSource().getLocation().toURI())
								.getPath();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					String responseFilePath = args[0];
					String excelinputPath = args[1];
					String referenceID = args[2];
					String responseType = args[3];
					
					System.out.println("\nARGS : "+ "\n1. "+args[0]+"\n2. "+args[1]+"\n3. "+args[2]+"\n\n");
					
					String responseContent = responseFilePath;
					ResponseValidations objValidations = new ResponseValidations();
					jar_filepath = jar_filepath.substring(0, jar_filepath.lastIndexOf(File.separator));
					String referenceidFolder = objValidations.GetInputReferenceID(referenceID);
					String response_filepath = jar_filepath + "\\" + referenceidFolder + "\\" + referenceID + ".json";

					objValidations.DeleteFileIfExists(response_filepath);
					HashMap<String, String> excelDataMap = objValidations.ReadExcelData(excelinputPath, referenceID);

					 inputObj = objValidations.ConstructInputJson(excelDataMap, responseContent,responseType);

//					objValidations.WriteToFile(inputObj, referenceID, response_filepath);

					System.out.println("Response Validation Client Side Completed : " + referenceID + ".json");
					return inputObj.toString();

				} else {
					System.out.println("Parameter Expected; Input Reference Id");
				}
			} else {
				System.out.println("Parameter Expected; Test Data Sheet");
			}

		} else {

			System.out.println(
					"Parameters Expected; Please pass Response String, Test Input Data File Path and Input Reference Id");
		}
		return inputObj.toString();
	}

	/* Get Response Content from File Path */
	private static String GetResponseContent(String responseFilePath) {

		BufferedReader br;
		StringBuilder sb_template = new StringBuilder();
		try {

			br = new BufferedReader(new FileReader(responseFilePath));
			String sCurrentLine = "";
			while ((sCurrentLine = br.readLine()) != null) {

				sb_template.append(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String responseContent = null;

		responseContent = sb_template.toString();

		System.out.println(responseContent);

		return responseContent;
	}

}
