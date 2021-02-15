package com.experian.responsevalidation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.experian.responsevalidation.OutputClass.ResultMap;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import net.minidev.json.JSONArray;

public class PCOValidations {

	public ResultMap ReturnOutputBasedOnResponseValidation(JSONObject inputJson) {
		ResultMap objResultMap = new ResultMap();
		InputStream responseinputStream = null;
		String responseContent = null;
		HashMap<String, String> validationMap = new HashMap<String, String>();
		List<String> differenceList = new ArrayList<String>();
		try {
			System.out.println("Method Started");
			boolean isFilePath = CheckStringisFilePath(inputJson.getString("ResponseContent"));
			if (isFilePath) {
				System.out.println("Response Content is a File Path");
				responseContent = ReadResponseContentFromFile(inputJson.getString("ResponseContent"));
			} else {
				System.out.println("Response Content is not a File Path");
				responseContent = inputJson.getString("ResponseContent");
			}

			System.out.println("Response Content");
			System.out.println(responseContent);
			if (inputJson.getString("ResponseType") == "xml") {

				JSONObject validationJson = inputJson.getJSONObject("ValidationCriteria");

				for (int i = 0; i < validationJson.names().length(); i++) {
					validationMap.put(validationJson.names().getString(i),
							(String) validationJson.get(validationJson.names().getString(i)));
				}

				for (Entry map : validationMap.entrySet()) {
					boolean validationFlag = false;
					responseinputStream = new ByteArrayInputStream(responseContent.getBytes(StandardCharsets.UTF_8));
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

					DocumentBuilder db;

					db = dbf.newDocumentBuilder();

					Document doc = db.parse(responseinputStream);
					IterateThroughDoc(doc.getDocumentElement());
					XPathFactory xPathFactory = XPathFactory.newInstance();
					XPath xpath = xPathFactory.newXPath();
					XPathExpression expr;
					Object actualValue = "";
					String xpathExpression = "//" + map.getKey().toString() + "/text()";

					System.out.println(xpathExpression);

					if (xpathExpression.contains("-")) {
						String cdataxpathExpression = xpathExpression.split("-")[0];
						expr = xpath.compile(cdataxpathExpression + "/text()");

						NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
						Node cdataNode = nl.item(0);
						String xml = cdataNode.getTextContent();
						System.out.println(xml);

						InputStream cdatainputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
						Document cdataDoc = null;
						try {
							cdataDoc = db.parse(cdatainputStream);
						} catch (Exception ex) {
						}
						if (cdataDoc != null) {

							if (xpathExpression.split("-")[1].contains("@")) {
								String xpathexprwithAttribute = xpathExpression.split("-")[1];
								xpathexprwithAttribute = xpathexprwithAttribute.replace(":", "=");
								expr = xpath.compile("//" + xpathexprwithAttribute);
							} else {
								expr = xpath.compile("//" + xpathExpression.split("-")[1]);
							}
							nl = (NodeList) expr.evaluate(cdataDoc, XPathConstants.NODESET);
							if (nl.getLength() > 0) {
								for (int index = 0; index < nl.getLength(); index++) {
									Node node = nl.item(index);
									actualValue = node.getTextContent();
									if (actualValue instanceof String) {
										String actual_Val = actualValue.toString();
										String exp_Val = map.getValue().toString();
										if (!actual_Val.contains(exp_Val)) {

										} else {
											validationFlag = true;
											break;
										}

									} else if (actualValue instanceof Integer) {
										Integer nodeValue_int = Integer.parseInt((String) map.getValue());
										if (!actualValue.equals(nodeValue_int)) {

										} else {
											validationFlag = true;
											break;
										}

									} else if (actualValue instanceof Double) {
										Double nodeValue_double = Double.parseDouble((String) map.getValue());
										if (!actualValue.equals(nodeValue_double)) {

										} else {
											validationFlag = true;
											break;
										}

									}

								}
								if (!validationFlag) {
									String difference = "Node : " + map.getKey().toString() + " Expected Value : "
											+ map.getValue().toString() + "  ; " + "Actual Value : " + actualValue;
									differenceList.add(difference);

								}
							} else {
								validationFlag = false;
								String difference = "Invalid Node Hierarchy : '" + map.getKey().toString() + "'";
								differenceList.add(difference);

							}

						} else {
							validationFlag = false;
							String difference = "Invalid Node Hierarchy : '" + map.getKey().toString() + "'";
							differenceList.add(difference);
						}

					} else {

						expr = xpath.compile("//" + map.getKey().toString() + "/text()");
						expr = xpath.compile(xpathExpression);
						NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

						if (nl.getLength() > 0) {
							for (int index = 0; index < nl.getLength(); index++) {
								Node node = nl.item(index);
								actualValue = node.getTextContent();
								if (actualValue instanceof String) {
									String actual_Val = actualValue.toString();
									String exp_Val = map.getValue().toString();
									if (!actual_Val.contains(exp_Val)) {

									} else {
										validationFlag = true;
										break;
									}

								} else if (actualValue instanceof Integer) {
									Integer nodeValue_int = Integer.parseInt((String) map.getValue());
									if (!actualValue.equals(nodeValue_int)) {

									} else {
										validationFlag = true;
										break;
									}

								} else if (actualValue instanceof Double) {
									Double nodeValue_double = Double.parseDouble((String) map.getValue());
									if (!actualValue.equals(nodeValue_double)) {

									} else {
										validationFlag = true;
										break;
									}

								}

							}
							if (!validationFlag) {
								String difference = "Node : " + map.getKey().toString() + " Expected Value : "
										+ map.getValue().toString() + "  ; " + "Actual Value : " + actualValue;
								differenceList.add(difference);

							}
						} else {
							validationFlag = false;
							String difference = "Invalid Node Hierarchy : '" + map.getKey().toString() + "'";
							differenceList.add(difference);

						}
					}
				}
				responseinputStream.close();
			} else {
				System.out.println(responseContent);
				DocumentContext parsedJSON = JsonPath.parse(responseContent);

				JSONObject validationJson = inputJson.getJSONObject("ValidationCriteria");
				System.out.println("ValidationCriteria" + validationJson.toString());

				for (int i = 0; i < validationJson.names().length(); i++) {
					validationMap.put(validationJson.names().getString(i),
							(String) validationJson.get(validationJson.names().getString(i)));
				}

				for (Entry<String, String> entry : validationMap.entrySet()) {
					boolean validationFlag = false;

					String xpathExpression = entry.getKey();
					JSONArray actualValueJSONArray = null;
					String actualValueString;
					Object actualValue = null;
					Object unknown;
					try {
						unknown = parsedJSON.read(xpathExpression);
						if (unknown instanceof JSONArray)
							if (((JSONArray) unknown).size() > 0)
								actualValue = ((JSONArray) unknown).get(0);
							else
								actualValue = unknown;
						else
							actualValue = unknown;
//						System.out.println("ACTUAL VALUE"+ actualValue);
					} catch (PathNotFoundException e) {
						System.out.println("JSON PATH Exception" + e);
					}

					if (actualValue != null) {

						if (actualValue instanceof String) {
							String actual_Val = actualValue.toString();
//							System.out.println("ACTUAL VALUE1"+ actual_Val);
							String exp_Val = entry.getValue().toString();
//							System.out.println("ACTUAL VALUE2"+ exp_Val);
							if (!actual_Val.contains(exp_Val.trim())) {

							} else {
								validationFlag = true;
								continue;
							}

						} else if (actualValue instanceof Integer) {
							Integer nodeValue_int = Integer.parseInt((String) entry.getValue());
							if (!actualValue.equals(nodeValue_int)) {

							} else {
								validationFlag = true;
								continue;
							}

						} else if (actualValue instanceof Double) {
							Double nodeValue_double = Double.parseDouble((String) entry.getValue());
							if (!actualValue.equals(nodeValue_double)) {

							} else {
								validationFlag = true;
								continue;
							}

						}

						if (!validationFlag) {
							String difference = "Node : " + entry.getKey() + " Expected Value : " + entry.getValue()
									+ "  ; " + "Actual Value : " + actualValue;
							differenceList.add(difference);

						}
					} else {
						validationFlag = false;
						String difference = "Invalid Node Hierarchy : '" + entry.getKey().toString() + "'";
						differenceList.add(difference);

					}
				}

			}

			if (differenceList.size() > 0) {
				objResultMap.setValidationStatus("FAILED");
				objResultMap.setDifferenceList(differenceList);
			} else {
				objResultMap.setValidationStatus("PASSED");
			}
		} catch (JSONException | XPathExpressionException | ParserConfigurationException | SAXException
				| IOException ex) {

			objResultMap.setValidationStatus(ex.toString());
			System.out.println(ex.toString());
		} finally {

		}

		return objResultMap;
	}

	private boolean CheckStringisFilePath(String responseContent) {
		if (responseContent.contains("\\\\10.188.31.79\\"))
			/*
			 * File f = new File(responseContent);
			 * 
			 * if (f.isFile() && !f.isDirectory())
			 */
			return true;
		else
			return false;
	}

	private String ReadResponseContentFromFile(String responseContentPath) {

		String fileName = "";
		StringBuffer responseContent = new StringBuffer();
		String[] fileSplit = responseContentPath.split("\\\\");
		fileName = fileSplit[fileSplit.length - 1];

		responseContentPath = "testautomation//projects//";
		int count = 1;
		for (String filePath : fileSplit) {
			if (count <= 3) {
				count++;
				continue;
			}

			responseContentPath = responseContentPath + filePath + "//";

		}
		responseContentPath = responseContentPath.substring(0, responseContentPath.length() - 2);
		System.out.println(responseContentPath);
		SSHUtil objSshutil = new SSHUtil();
		ChannelSftp channel = null;
		try {
			channel = objSshutil.setupJsch();
			channel.connect();
			System.out.println(channel.pwd());
			channel.cd("..");
			channel.cd("..");
			System.out.println(channel.pwd());
			InputStream stream = channel.get(responseContentPath);
			System.out.println("Got InputStream");
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));

			char[] buf = new char[1024];
			int numRead = 0;
			try {
				while ((numRead = br.read(buf)) != -1) {
					String readData = String.valueOf(buf, 0, numRead);
					responseContent.append(readData);
				}
				br.close();
			} catch (IOException e) {
				System.out.println("Exception while copying remote file into local  -- Step1");
				e.printStackTrace();
			}

			System.out.println("Successfull");

		} catch (JSchException | SftpException e1) {
			System.out.println("Exception while copying remote file into local  -- Step2");

			e1.printStackTrace();
		}
		return responseContent.toString();

	}

	private void IterateThroughDoc(Node node) {
		NodeList nodeList = null;
		Node currentNode = null;
		System.out.println(node.getNodeName());
		System.out.println(node.getNodeValue());
		System.out.println(node.getParentNode().getNodeName());
		System.out.println(node.getParentNode().getNodeValue());
		if (node.hasChildNodes()) {
			nodeList = node.getChildNodes();
			currentNode = nodeList.item(0);
			System.out.println(currentNode.getNodeName());
			System.out.println(currentNode.getNodeValue());
			System.out.println(currentNode.getParentNode().getNodeName());
			System.out.println(currentNode.getParentNode().getNodeValue());

		}
		nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			currentNode = nodeList.item(i);

			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				IterateThroughDoc(currentNode);
			}
		}
	}
}
