package com.wrapper.clientutils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.org.apache.bcel.internal.classfile.Constant;
import com.wrapper.model.OutputClass.ResultMap;

import jdk.internal.org.jline.utils.Log;
import net.minidev.json.JSONArray;

public class WrapperMain {

	static String TEST_PATH;
	static String MASTERDATA_PATH;
	static String RESPONSE_MX = "http://localhost:8080/responsevalidation";
	static String TABLE_MX = "http://localhost:8083/tablevalidation";
	static String RUNID_MX = "http://localhost:8082/v1/getrunid?projectid={projectid}";
	static String RESULT_UPDATE_MX = "http://localhost:8084/resultupdation";
	static String EXTENDED_REPORT_MX = "http://localhost:8085//v1/createextendreport?runid={runid}";
	static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public ResultMap GetTestRunResults() throws Exception {

		TEST_PATH = "D:\\UST\\RTP\\Runner\\RTP_Files\\TestData.xls";// args[0];

		ResultMap result = new WrapperMain().rtpTestExecute();
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(result.getReportLocation()));
			} catch (IOException | URISyntaxException e) {

			}
		} 
		//else
			//throw new Exception("Browser Application is not supported");

		return result;

	}

	public RestTemplate restTemplate() {
		KeyStore clientStore = null;
		try {
			clientStore = KeyStore.getInstance("PKCS12");
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			clientStore.load(new FileInputStream("D:\\UST\\RTP\\Runner\\RTP_Files\\SecurityCertificates\\server.p12"),
					"abc".toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
		sslContextBuilder.useProtocol("TLS");
		try {
			sslContextBuilder.loadKeyMaterial(clientStore, "abc".toCharArray());
		} catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
		} catch (NoSuchAlgorithmException | KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SSLConnectionSocketFactory sslConnectionSocketFactory = null;
		try {
			sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		// requestFactory.setConnectTimeout(10000); // 10 seconds
		// requestFactory.setReadTimeout(10000); // 10 seconds return new
		return new RestTemplate(requestFactory);
	}

	public ResultMap rtpTestExecute() {

		String token = "";
		ResponseEntity<String> resultUpdateResponse = new ResponseEntity<>("Error Occured: Not processed",
				HttpStatus.INTERNAL_SERVER_ERROR);

		try {

//			FileOutputStream fooStreamR = new FileOutputStream(new File("MX Response_Validation.json"), false);
//			FileOutputStream fooStreamT = new FileOutputStream(new File("MX Table_Validation.json"), false);

			List<Object> objectList = new WrapperUtility().extracted(TEST_PATH);

			List<HashMap<String, String>> testCaseExcelList = (List<HashMap<String, String>>) objectList.get(0);
			// List<List<String>> responseValidationsList = (List<List<String>>)
			// objectList.get(1);
			// List<List<String>> tableValidationsList = (List<List<String>>)
			// objectList.get(2);
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT);
			List<TestCase> testCaseList = new ArrayList<>();
			RestTemplate restTemplate = restTemplate();
			ObjectMapper mapper = new ObjectMapper();
			HttpHeaders headers = new HttpHeaders();
			String demoRTPResponse = "";
			String requestCreation = "";
			int loop = 0;

			ResponseExcelUtility excel = new ResponseExcelUtility();
			excel.setExcel(TEST_PATH, "Configuration");
			String base_url = excel.getdata(1, 2);
			String api_base = excel.getdata(1, 3);
			MASTERDATA_PATH = excel.getdata(1, 5);
			// System.out.println(base_url + api_base);

			for (HashMap<String, String> testCaseExcel : (List<HashMap<String, String>>) testCaseExcelList) {
				if (testCaseExcel.get("ExecutionFlag").equals("No")) {
					loop++;
					continue;
				}

				TestCase testCase = new TestCase();
				testCase.setTestExecutionStatus("PASSED");
				List<Step> Steps = new ArrayList<>();

				for (int i = 1; i <= testCaseExcel.size(); i++) {
					Step step;
					String stepkey = testCaseExcel.get("Step" + i);
					String stepid = "Step" + i;
					if (stepkey == null)
						continue;
					if (stepkey.startsWith("ServiceCall")) {

						/* Calling Request Creation utility to get fulfilled template request */
						// requestCreation = new RequestMain().main((new String[] { TEST_PATH,
						// testCaseExcel.get("Step1") }));
						System.out.println(stepkey + "        :::  " + stepid);
						requestCreation = new RequestMain().main((new String[] { TEST_PATH, stepkey }));

						/* Get endpoint details for making request to RTP Demo App */
						DocumentContext parsedJSON = JsonPath.parse(requestCreation.split(":::")[0]);
						String method = parsedJSON.read("$.request.method");
						JSONArray jsonHeaders = parsedJSON.read("$.request.header");
						String body = new JSONObject(parsedJSON.read("$.request.body")).toString();
						String url = parsedJSON.read("$.request.url");
						url = url.replace("#base_url", base_url);
						url = url.replace("#api_base", api_base);

						/* Setting common & custom headers */
						headers = null;
						headers = new HttpHeaders();
						MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
						int custumHeader = 0;
						// headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
						// headers.setContentType(MediaType.APPLICATION_JSON);
						for (Object header : jsonHeaders) {
							String[] h = new JSONObject((Map) header).toString().split(":");
							System.out.println(h[1].substring(1).split("\"")[0]);

							if ((h[1].substring(1).split("\"")[0]).equals("x-www-form-urlencoded")) {
								// System.out.println("test");
								custumHeader = 1;
								// headers.setAccept(Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED));
								headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
							} else if ((h[0].substring(2).split("\"")[0]).contains("xform-")) {
								custumHeader = 2;
								postParameters.add((h[0].substring(2).split("\"")[0]).substring(6),
										h[1].substring(1).split("\"")[0]);
							} else {
								if ((h[1].substring(1).split("\"")[0]).equals("#TOKEN")) {
									if (token.length() < 10)
										token = constant.TOKEN;
									headers.add(h[0].substring(2).split("\"")[0], token);
								} else
									headers.add(h[0].substring(2).split("\"")[0], h[1].substring(1).split("\"")[0]);
								System.out.println("outer else");
							}

						}
						if (custumHeader == 0 && (method.equals("POST") || method.equals("PATCH"))) {
							headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
							headers.setContentType(MediaType.APPLICATION_JSON);
						}
						step = new Step();
						// step.setStepName(testCaseExcel.get("Step1"));
						// System.out.println("@@"+stepid+testCaseExcel.get(stepid));
						step.setStepName(testCaseExcel.get(stepid));
						step.setStatus("PASSED");
						step.setExecutionTime(LocalDateTime.now().format(dateFormat));
						/* Calling RTP Demo App to get Response */
						try {

							if (method.equals("GET")) {
								System.out.println(headers + "BODY  \n" + body + "\n");
								HttpEntity<String> entity = new HttpEntity<String>(headers);
								// System.out.println("RTPresponse1" );
								ResponseEntity<String> RTPresponse = restTemplate.exchange(url, HttpMethod.GET, entity,
										String.class);
								demoRTPResponse = RTPresponse.getBody();
								System.out.println("RTP Get response" + demoRTPResponse);
							} else if (method.equals("POST")) {
								// System.out.println(headers + "BODY \n" + body + "\n");

								if (custumHeader == 2) {
									HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(postParameters,
											headers);
									ResponseEntity<String> RTPresponse = restTemplate.exchange(url, HttpMethod.POST,
											entity, String.class);
									demoRTPResponse = RTPresponse.getBody();
									ObjectMapper objectMapper = new ObjectMapper();
									JsonNode jsonNode = objectMapper.readTree(demoRTPResponse);
									String content = jsonNode.get("access_token").textValue();
									token = "Bearer " + content;
									System.out.println("TKN" + token);

								} else {
									HttpEntity<String> entity = new HttpEntity<String>(body, headers);
									ResponseEntity<String> RTPresponse = restTemplate.exchange(url, HttpMethod.POST,
											entity, String.class);
									demoRTPResponse = RTPresponse.getBody();
								}
								// System.out.println("RTPresponse2" );

								// System.out.println("RTP POST response" + demoRTPResponse);
							} else if (method.equals("PUT")) {
								// System.out.println("BODY \n" + body + "\n");
								HttpEntity<String> entity = new HttpEntity<String>(body, headers);
								// System.out.println("RTPresponse2" );
								ResponseEntity<String> RTPresponse = restTemplate.exchange(url, HttpMethod.PUT, entity,
										String.class);
								demoRTPResponse = RTPresponse.getBody();
								// System.out.println("RTP POST response" + demoRTPResponse);
							} else if (method.equals("PATCH")) {
								// System.out.println("BODY \n" + body + "\n");
								HttpEntity<String> entity = new HttpEntity<String>(body, headers);
								// System.out.println("RTPresponse2" );
								ResponseEntity<String> RTPresponse = restTemplate.exchange(url, HttpMethod.PATCH,
										entity, String.class);
								demoRTPResponse = RTPresponse.getBody();
								// System.out.println("RTP POST response" + demoRTPResponse);
							}
						} catch (HttpClientErrorException e) {
							// System.out.println("\n H");
							demoRTPResponse = e.getResponseBodyAsString();
							// step.setStatus("FAILED");
							// System.out.println("\n H2"+ demoRTPResponse);
						} catch (HttpServerErrorException e) {
							System.out.println("\n H1");
							demoRTPResponse = e.getResponseBodyAsString();
							step.setStatus("FAILED");
						} catch (Exception e) {
							System.out.println("Error while getting response from RTP DEMO: " + e.getMessage());
							step.setStatus("FAILED");
						}
						step.setLogName(testCaseExcel.get(stepid));
						// System.out.println("@@"+stepid+testCaseExcel.get(stepid));

						step.setStepName(testCaseExcel.get(stepid));
						step.setLogContent(demoRTPResponse);
						Steps.add(step);

						headers = new HttpHeaders();
						headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
						headers.setContentType(MediaType.APPLICATION_JSON);
					} else if (stepkey.startsWith("ResponseValidation")) {

						// List<String> RvalidationTestCaseList = responseValidationsList.get(loop);
						// List<String> TvalidationTestCaseList = tableValidationsList.get(loop);
						// System.out.println("List Validation"+ RvalidationTestCaseList + "gg
						// "+TvalidationTestCaseList);

						// for (String responseValidation : RvalidationTestCaseList) {
						/* Creating payload for Response Validation Mx using Utility */
						step = new Step();
						String responseValidation = stepkey;
						try {
							// step = new Step();
							step.setExecutionTime(LocalDateTime.now().format(dateFormat));
							step.setStepName(responseValidation);

							String ResponseValidationPayload = new ResponseMain()
									.main(new String[] { demoRTPResponse, TEST_PATH, responseValidation, "json" });

							/* Calling Response Validation Mx */
							HttpEntity<String> responseEntity = new HttpEntity<String>(ResponseValidationPayload,
									headers);
							ResponseEntity<String> Rresponse = restTemplate.exchange(RESPONSE_MX, HttpMethod.POST,
									responseEntity, String.class);
							// System.out.println("check1");
							/* Printing to Console with Pretty Printer */
							Map<String, String> json = mapper.readValue(Rresponse.getBody(), Map.class);
							String ResponseMxOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
							// System.out.println("Response Mx Output : " + ResponseMxOutput);

							/* Writing to Files */
							// fooStreamR.write(ResponseMxOutput.getBytes());

							step.setStatus(json.get("validationStatus"));
							if (step.getStatus().equals("FAILED"))
								testCase.setTestExecutionStatus("FAILED");
							step.setLogContent(ResponseMxOutput);
							step.setLogName(responseValidation + ".log");

						} catch (Exception e) {
							System.out.println("Error while getting response validation : " + e.getMessage());
							step.setLogContent("Error while getting response validation : " + e.getMessage());
							step.setLogName(responseValidation + ".log");
							step.setStatus("FAILED");
						}
						Steps.add(step);

						// }
					} else if (stepkey.startsWith("DBTableValidation")) {
						String tableValidation = stepkey;
						// for (String tableValidation : TvalidationTestCaseList) {
						/* Creating payload for Table Validation Mx using Utility */
						step = new Step();
						try {
							// step = new Step();
							step.setExecutionTime(LocalDateTime.now().format(dateFormat));
							step.setStepName(tableValidation);

							String tableValidationPayload = new TableMain().main(new String[] { TEST_PATH,
									tableValidation, MASTERDATA_PATH, requestCreation.split(":::")[1] });

							/* Calling Table Validation Mx */
							HttpEntity<String> tableEntity = new HttpEntity<String>(tableValidationPayload, headers);
							ResponseEntity<String> response1 = restTemplate.exchange(TABLE_MX, HttpMethod.POST,
									tableEntity, String.class);

							/* Printing to Console with Pretty Printer */
							Map<String, String> json1 = mapper.readValue(response1.getBody(), Map.class);
							String TableMxOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json1);
							// System.out.println("\n Table Mx Output : " + TableMxOutput);

							/* Writing to Files */
							// fooStreamT.write(TableMxOutput.getBytes());

							step.setStatus(json1.get("testStatus"));
							if (step.getStatus().equals("FAILED"))
								testCase.setTestExecutionStatus("FAILED");
							step.setLogContent(TableMxOutput);
							step.setLogName(tableValidation + ".log");
						} catch (Exception e) {
							System.out.println("Error while getting tabel validation : " + e.getMessage());
							step.setLogContent("Error while getting table validation : " + e.getMessage());
							step.setLogName(tableValidation + ".log");
							step.setStatus("FAILED");
						}
						Steps.add(step);
						// }
					}

				}
				loop++;

				testCase.setSteps(Steps);
				testCase.setExecutionTime(LocalDateTime.now().format(dateFormat));
				testCase.setTestCaseName(testCaseExcel.get("TestCaseID"));
				testCase.setTestCaseDescription(testCaseExcel.get("TestCaseDescription"));
				testCaseList.add(testCase);
			}

			/* Preparing the Result Updation Microservice Payload */
			ResultUpdation resultUpdation = new ResultUpdation();
			resultUpdation.setTestCaseList(testCaseList);
			resultUpdation.setTestSuiteName("RTP_TEST_RESULTS");
			List<ResultUpdation> resultUpdationList = new ArrayList<>();
			resultUpdationList.add(resultUpdation);

			/* Populating RunID from call to RunId Microservice */
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> RTPresponse = restTemplate.exchange(RUNID_MX, HttpMethod.GET, entity, String.class,
					requestCreation.split(":::")[2]);
			ResultUpdationOBJECT resultUpdationObject = new ResultUpdationOBJECT();
			resultUpdationObject.setRunId(JsonPath.parse(RTPresponse.getBody()).read("$.runID").toString());
			System.out.println(resultUpdationObject.getRunId());
			resultUpdationObject.setUser("user1");
			resultUpdationObject.setResultUpdations(resultUpdationList);

			ResultUpdationMain resultUpdationMain = new ResultUpdationMain();
			resultUpdationMain.setInput(resultUpdationObject);

			/* Printing Result Updation Microservice Payload */
			String resultUpdationMxInput = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(resultUpdationMain);
			// System.out.println("\n RESULT_UPDATE INPUT :\n" + resultUpdationMxInput);

			/* Call the Result Updation Microservice */
			HttpEntity<String> resultUpdationEntity = new HttpEntity<String>(resultUpdationMxInput, headers);
//			System.out.println("result start");
			resultUpdateResponse = restTemplate.exchange(RESULT_UPDATE_MX, HttpMethod.POST, resultUpdationEntity,
					String.class);
//			System.out.println("\n RESULT_UPDATE OUTPUT : "+ resultUpdateResponse.getBody());
//			System.out.println("result executed");
//			fooStreamR.close();
//			fooStreamT.close();			

			/* Call the Extended Report Microservice */
			resultUpdateResponse = restTemplate.exchange(EXTENDED_REPORT_MX, HttpMethod.GET, entity, String.class,
					resultUpdationObject.getRunId());
			// System.out.println("\n EXTENDED_REPORT OUTPUT : "+
			// resultUpdateResponse.getBody());

			/* Reading the Local File Report and sending to Client */
		//	System.out.println(resultUpdationObject.getRunId());
		//	System.out.println(JsonPath.parse(resultUpdateResponse.getBody()).read("$.localFileLocation").toString());
			// return
			// JsonPath.parse(resultUpdateResponse.getBody()).read("$.localFileLocation").toString();

			ResultMap objResultMap = new ResultMap();
			objResultMap.setRunID(resultUpdationObject.getRunId());
			objResultMap.setReportLocation(
					JsonPath.parse(resultUpdateResponse.getBody()).read("$.localFileLocation").toString());
			return objResultMap;
		} catch (Exception e) {
			ResultMap objResultMap = new ResultMap();
			objResultMap.setFailedMessage("\n Wrapper Error Occured  :" + e.getMessage() + "<html><body><br><br><p>"
					+ resultUpdateResponse.getBody() + "<p> Status :" + resultUpdateResponse.getStatusCode()
					+ "</body></html>");
			return objResultMap;
		}

	}

}