package com.wrapper.clientutils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.annotation.processing.FilerException;
import javax.json.JsonObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestValidations {

	public HashMap<String, String> ReadExcelData(String excelinputPath, String referenceID) {
		RequestExcelUtility excelUtility = new RequestExcelUtility();
		HashMap<String, String> excelDataMap = new HashMap<String, String>();
		try {
		
			String[] arr = referenceID.split("_");
			arr = Arrays.copyOf(arr, arr.length - 1);
			
			String inputData = "";
			for (String input : arr) {
				inputData += input;
			}
			excelUtility.setExcel(excelinputPath, arr[0]);

			List<String> keyList = new ArrayList<String>();
			List<String> valueList = new ArrayList<String>();
			String columnHeader = null;
			String columnValue = null;
			int cell = 1;
			int getsheetRowCount = 0;

			getsheetRowCount = excelUtility.getRowCount();

			do {

				columnHeader = excelUtility.getdata(0, cell);

				cell++;
				if (columnHeader != "") {

					keyList.add(columnHeader);

				}
			} while (columnHeader != "");
			cell = 1;
			String findKeyword = null;
			for (int j = 1; j < getsheetRowCount; j++) {

				findKeyword = excelUtility.getdata(j, 0);

				if (referenceID.equals(findKeyword)) {
					for (int k = 0; k < keyList.size(); k++) {

						columnValue = excelUtility.getdata(j, cell);

						valueList.add(columnValue);
						cell++;
					}
				}
			}

			for (int i = 0; i < keyList.size(); i++) {
				if (valueList.get(i) != "") {
					excelDataMap.put(keyList.get(i), valueList.get(i));
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return excelDataMap;
	}

	public String GetInputReferenceID(String referenceID) {
		String[] arr = referenceID.split("_");
		arr = Arrays.copyOf(arr, arr.length - 1);
		String inputData = "";
		for (String input : arr) {
			inputData += input;
		}

		return inputData;
	}

	public HashMap<String, String> GetDBConfigExcelDetails(String excelPath, String sheetName,
			HashMap<String, String> testDataMap) {

		RequestExcelUtility excelUtility = new RequestExcelUtility();
		excelUtility.setExcel(excelPath, sheetName);

		List<String> keyList = new ArrayList<String>();
		List<String> valueList = new ArrayList<String>();
		String columnHeader = null;
		int cell = 0;
		do {
			columnHeader = excelUtility.getdata(0, cell);
			cell++;
			if (columnHeader != "") {

				keyList.add(columnHeader);

			}
		} while (columnHeader != "");

		int cellcount = excelUtility.getcellCount();
		for (int column = 0; column < cellcount; column++) {
			valueList.add(excelUtility.getdata(1, column));

		}
		for (int i = 0; i < keyList.size(); i++) {
			if (valueList.get(i) != "") {
				testDataMap.put(keyList.get(i), valueList.get(i));
			}
		}
		return testDataMap;
	}

	public void WriteToFile(JSONObject inputObj, String referenceID, String response_filepath) {

		String outputJson = inputObj.toString();

		ObjectMapper mapper = new ObjectMapper();

		Object json;
		try {
			json = mapper.readValue(outputJson, Object.class);
			outputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

			File file = new File(response_filepath);
			file.getParentFile().mkdirs();
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
	}

	public String CreatePCORequestFile(HashMap<String, String> testDataMap, HashMap<String, String> dbDataMap,
			String testdataPath) {
		String requestBody = "";

		try {
			testdataPath = testdataPath.substring(0, testdataPath.lastIndexOf(File.separator));

			String templateFilePath = testdataPath + "\\TemplateFiles\\" + testDataMap.get("RequestTemplateFile");
		
			// Document docTemplate = fileToDocumentGenerator(templateFilePath);
			// IterateThroughNodesAndReplaceTemplate(docTemplate.getDocumentElement(),
			// dbDataMap);
			// requestBody = documentToStringGenerator(docTemplate);

			JSONParser h = new JSONParser();
		
			FileReader reader = new FileReader(templateFilePath);
			Scanner myReader = new Scanner(reader);
			while (myReader.hasNextLine()) {
				requestBody += myReader.nextLine();
			}
			myReader.close();

//			System.out.println("Scanner Output" + requestBody);

			for (Entry<String, String> entry : dbDataMap.entrySet()) {
				requestBody = requestBody.replace("$" + entry.getKey(), entry.getValue());
			}
//			System.out.println("Template Filled Output" + requestBody);
			
			ObjectMapper mapper = new ObjectMapper();

			Object json;
		
				try {
					json = mapper.readValue(requestBody, Object.class);

					requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
//			Object obj = new JSONParser().parse(requestBody);
//			requestBody = obj.toString();

//			System.out.println("REQUEST Generated :\n" + requestBody);

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		return requestBody;
	}

	public void WriteXmlstringToDom(String xmlSource, String requestFilepath) {

		try {

//			Document requestDoc = XmlStringToDocumentGenerator(xmlSource);
			File file = new File(requestFilepath);
			file.getParentFile().mkdirs();
//			try {
//				file.createNewFile();
//
//				DOMSource source = new DOMSource(requestDoc);
//				FileWriter writer1 = new FileWriter(requestFilepath);
//				StreamResult result = new StreamResult(writer1);
//
//				TransformerFactory transformerFactory = TransformerFactory.newInstance();
//				Transformer transformer = transformerFactory.newTransformer();
//				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//				transformer.transform(source, result);
//
//				java.io.FileWriter fw;
//
//				fw = new java.io.FileWriter(requestFilepath);
//				fw.write(xmlSource);
//				fw.close();
//			} catch (IOException | TransformerException e) {
//				System.out.println(e.toString());
//			}

			FileWriter filewriter = new FileWriter(requestFilepath);
			filewriter.write(xmlSource);
			filewriter.flush();
			filewriter.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

//	private void IterateThroughNodesAndReplaceTemplate(Node node, HashMap<String, String> testDataMap) {
//
//		try {
//			NodeList nodeList = null;
//			Node currentNode = null;
//			if (node.hasChildNodes()) {
//				nodeList = node.getChildNodes();
//				currentNode = nodeList.item(0);
//				if (currentNode.getNodeValue() != null) {
//
//					if ((currentNode.getNodeValue().contains("$"))
//							&& (!currentNode.getNodeName().contains("cdata-section"))) {
//						String getValueFromMap = IterateThroughHeaderMap(currentNode.getNodeValue(), testDataMap);
//						currentNode.setTextContent(getValueFromMap);
//					}
//
//					if (currentNode.getNodeName().contains("cdata-section")) {
//
//						Document doc_cdata_template = XmlStringToDocumentGenerator(currentNode.getNodeValue());
//
//						IterateThroughNodesAndReplaceCDATATemplate(doc_cdata_template.getDocumentElement(),
//								testDataMap);
//
//						String cdataupdatedcontent = documentToStringGenerator(doc_cdata_template);
//
//						currentNode.setNodeValue(cdataupdatedcontent);
//
//						currentNode.setTextContent(cdataupdatedcontent);
//					}
//				}
//			}
//
//			nodeList = node.getChildNodes();
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				currentNode = nodeList.item(i);
//
//				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
//					IterateThroughNodesAndReplaceTemplate(currentNode, testDataMap);
//				}
//			}
//		} catch (Exception ex) {
//			System.out.println(ex.toString());
//		}
//	}

//	private void IterateThroughNodesAndReplaceCDATATemplate(Node node, HashMap<String, String> testDataMap) {
//		try {
//			NodeList nodeList = null;
//			Node currentNode = null;
//			if (node.hasChildNodes()) {
//				nodeList = node.getChildNodes();
//				currentNode = nodeList.item(0);
//				if (currentNode.getNodeValue() != null) {
//
//					if (currentNode.getNodeValue().contains("$")) {
//						String getValueFromMap = IterateThroughHeaderMap(currentNode.getNodeValue(), testDataMap);
//						currentNode.setTextContent(getValueFromMap);
//					}
//
//				}
//			}
//
//			nodeList = node.getChildNodes();
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				currentNode = nodeList.item(i);
//
//				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
//					IterateThroughNodesAndReplaceCDATATemplate(currentNode, testDataMap);
//				}
//			}
//		} catch (Exception ex) {
//			System.out.println(ex.toString());
//		}
//
//	}

	/* Convert Xml String to Document */
	public Document XmlStringToDocumentGenerator(String xmlString) {
		Document doc_template = null;
		try {

			InputStream templateinputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc_template = db.parse(templateinputStream);

		} catch (Exception ex) {
		}
		return doc_template;
	}

	private String IterateThroughHeaderMap(String nodeText, HashMap<String, String> testDataMap) {
		String nodeValueFromMap = "";
		nodeText = nodeText.replaceAll("[\\[\\](){}$]", "");
		for (Entry map : testDataMap.entrySet()) {
			if (nodeText.equalsIgnoreCase(map.getKey().toString())) {
				nodeValueFromMap = map.getValue().toString();
			}
		}
		return nodeValueFromMap;
	}

	public void DeleteFileIfExists(String requestFilepath) {
		File file = new File(requestFilepath);
		try {
			boolean result = Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* Convert File to Document */
	public Document fileToDocumentGenerator(String templateFilePath) {
		Document doc_template = null;
		try {
			StringBuilder sb_template = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new FileReader(templateFilePath))) {
				String sCurrentLine = "";
				while ((sCurrentLine = br.readLine()) != null) {
					sb_template.append(sCurrentLine.trim());
				}
			}
			System.out.println("Template File Content");
			System.out.println(sb_template.toString());
			InputStream templateinputStream = new ByteArrayInputStream(
					sb_template.toString().getBytes(StandardCharsets.UTF_8));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc_template = db.parse(templateinputStream);

		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		return doc_template;
	}

	public HashMap<String, String> GetTestDataDetails(HashMap<String, String> excelDataMap, String testdatafilePath) {
		HashMap<String, String> testDataMap = new HashMap<String, String>();
		if (excelDataMap.get("TestDataFile") != null) {
			testdatafilePath = testdatafilePath.substring(0, testdatafilePath.lastIndexOf(File.separator));
			String testDataFilePath = testdatafilePath + "\\" + excelDataMap.get("TestDataFile");
			String testDataId = excelDataMap.get("TestDataID");

			RequestExcelUtility excelUtil = new RequestExcelUtility();

			excelUtil.setFirstSheet(testDataFilePath);
			List<String> keyList = new ArrayList<String>();
			List<String> valueList = new ArrayList<String>();
			String columnHeader = null;
			String columnValue = null;
			int cell = 1;
			String keyword = testDataId;
			int getsheetRowCount = excelUtil.getRowCount();

			do {
				columnHeader = excelUtil.getdata(0, cell);

				cell++;
				if (columnHeader != "") {

					keyList.add(columnHeader);

				}
			} while (columnHeader != "");
			cell = 1;
			String findKeyword = null;
			for (int j = 1; j < getsheetRowCount; j++) {
				findKeyword = excelUtil.getdata(j, 0);
			//	System.out.println(findKeyword);
				if (keyword.equals(findKeyword)) {
					for (int k = 0; k < keyList.size(); k++) {
						columnValue = excelUtil.getdata(j, cell);
						valueList.add(columnValue);
						cell++;
					}
				}
			}

			for (int i = 0; i < keyList.size(); i++) {

				testDataMap.put(keyList.get(i), valueList.get(i));

			}
		}
		return testDataMap;

	}

	/* Convert Document to String */
	public String documentToStringGenerator(Document doc_template)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc_template), new StreamResult(writer));
		String output = writer.getBuffer().toString().replaceAll("\n|\r", "");

		output = output.replaceAll(">\\s+<", "><");
		// soapRequestContent = soapRequestContent.replaceAll("space", " ");

		return output;
	}

}
