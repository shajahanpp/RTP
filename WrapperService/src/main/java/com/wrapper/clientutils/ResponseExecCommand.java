package com.wrapper.clientutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ResponseExecCommand {
	private Semaphore outputSem;
	private String output;
	private Semaphore errorSem;
	private String error;
	private Process p;

	private class InputWriter extends Thread {
		private String input;

		public InputWriter(String input) {
			this.input = input;
		}

		public void run() {
			PrintWriter pw = new PrintWriter(p.getOutputStream());
			pw.println(input);
			pw.flush();
		}
	}

	private class OutputReader extends Thread {
		public OutputReader() {
			try {
				outputSem = new Semaphore(1);
				outputSem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				StringBuffer readBuffer = new StringBuffer();
				BufferedReader isr = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String buff = new String();
				while ((buff = isr.readLine()) != null) {
					readBuffer.append(buff);
					System.out.println(buff);
				}
				output = readBuffer.toString();
				outputSem.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ErrorReader extends Thread {
		public ErrorReader() {
			try {
				errorSem = new Semaphore(1);
				errorSem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			BufferedReader isr = null;
			try {
				StringBuffer readBuffer = new StringBuffer();
				isr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String buff = new String();
				while ((buff = isr.readLine()) != null) {
					readBuffer.append(buff);
				}
				error = readBuffer.toString();
				errorSem.release();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (isr != null) {
					try {
						isr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (error.length() > 0)
				System.out.println(error);
		}
	}

	public ResponseExecCommand(String projectName) {
		// this.triggerCommand(projectName);
		this(projectName, null);
	}

	public ResponseExecCommand(String projectName, Long runId) {
		this.triggerCommand(projectName, runId);
	}

	public void triggerCommand(String projectName, Long runId) {
		System.out.println("ProjectName is " + projectName);
		System.out.println("RunId is " + runId);
		if (projectName.matches("[a-zA-Z0-9-_ ]*")) {
			String filePath = new StringBuilder("C:/testautomation/projects/NUnit.ConsoleRunner.3.9.0/tools/")
					.toString();
			File dirFile = new File(filePath);

			List<String> command = new ArrayList<String>();
			command.add("cmd.exe");
			command.add("/C");
			command.add("CSharp_Executer_UI_TEST.bat");
			command.add(projectName);
			command.add(String.valueOf(runId));

			if (runId != null) {
				command.add(runId.toString());
			}

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(dirFile);
			try {
				p = builder.start();// Runtime.getRuntime().exec(makeArray(command));
				new OutputReader().start();
				new ErrorReader().start();
				// p.waitFor(10, TimeUnit.SECONDS);
				p.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void TriggerJarFile(String runID, String projectName) {

		String pathToJar = System.getProperty("user.dir") + "\\Jar\\";

		File dirFile = new File(pathToJar);

		List<String> command = new ArrayList<String>();
		command.add("java.exe");
		command.add("-jar");
		command.add("ReportJar.jar");
		command.add(runID);
		command.add(projectName);

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(dirFile);
		try {
			p = builder.start();// Runtime.getRuntime().exec(makeArray(command));
			new OutputReader().start();
			new ErrorReader().start();
			// p.waitFor(10, TimeUnit.SECONDS);
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void TriggerJar() {

		String pathToJar = "C:\\Users\\C51575A\\Work\\PCOMicroServices\\PCOResponseValidationJar\\";

		File dirFile = new File(pathToJar);
		String responseContent = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
				"   <soap:Body>\r\n" + 
				"      <ns2:ougWSResponse xmlns:ns2=\"http://ougwebcomponent.components.oug.osgi.scorex.com/\">\r\n" + 
				"         <data_flux>\r\n" + 
				"            <item>requestTime</item>\r\n" + 
				"            <item>20200612-154058986</item>\r\n" + 
				"         </data_flux>\r\n" + 
				"         <data_flux>\r\n" + 
				"            <item>return status</item>\r\n" + 
				"            <item>OK</item>\r\n" + 
				"         </data_flux>\r\n" + 
				"         <data_flux>\r\n" + 
				"            <item>return code</item>\r\n" + 
				"            <item>0</item>\r\n" + 
				"         </data_flux>\r\n" + 
				"         <data_flux>\r\n" + 
				"            <item>workflow_name</item>\r\n" + 
				"            <item>DecisionUpdateApplication</item>\r\n" + 
				"         </data_flux>\r\n" + 
				"         <data_flux>\r\n" + 
				"            <item>bps.output</item>\r\n" + 
				"            <item><![CDATA[<DuOutput>\r\n" + 
				"		<ApplicationDetails>\r\n" + 
				"		<PowerCurveID>0000000000025206</PowerCurveID>\r\n" + 
				"		<PowerCurveUUID>272bad39-7b9a-4d71-909d-a451612e8e67</PowerCurveUUID>\r\n" + 
				"		<ApplicationDate>2020-06-12</ApplicationDate>\r\n" + 
				"		<ApplicationTime>15:40:50</ApplicationTime>\r\n" + 
				"		<DateApplicationUpdated>2020-06-12</DateApplicationUpdated>\r\n" + 
				"		<TimeApplicationUpdated>15:40:58</TimeApplicationUpdated>\r\n" + 
				"	</ApplicationDetails>\r\n" + 
				"	<DecisionDetails>\r\n" + 
				"		<Decision>A</Decision>\r\n" + 
				"		<DecisionText>Accept</DecisionText>\r\n" + 
				"		<TopReasonCode>RRRR      </TopReasonCode>\r\n" + 
				"		<OverrideJustification>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</OverrideJustification>\r\n" + 
				"		<DecisioningComponent>Decision Update</DecisioningComponent>\r\n" + 
				"		<DecisionMakerOrUserID>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</DecisionMakerOrUserID>\r\n" + 
				"	</DecisionDetails>\r\n" + 
				"	<Error>\r\n" + 
				"	</Error>\r\n" + 
				"</DuOutput>]]></item>\r\n" + 
				"         </data_flux>\r\n" + 
				"      </ns2:ougWSResponse>\r\n" + 
				"   </soap:Body>\r\n" + 
				"</soap:Envelope>";
		List<String> command = new ArrayList<String>();
		command.add("java.exe");
		command.add("-jar");
		command.add("ResponseValidationClient.jar");
		command.add(responseContent);
		command.add("C:\\Users\\C51575A\\Work\\PCOMicroServices\\PCOResponseValidationJar\\TestData.xlsx");
		command.add("ResponseValidation_4");
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(dirFile);
		try {
			p = builder.start();// Runtime.getRuntime().exec(makeArray(command));
			new OutputReader().start();
			new ErrorReader().start();
			// p.waitFor(10, TimeUnit.SECONDS);
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getOutput() {
		try {
			outputSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String value = output;
		outputSem.release();
		return value;
	}

	public String getError() {
		try {
			errorSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String value = error;
		errorSem.release();
		return value;
	}

	private String[] makeArray(String command) {
		ArrayList<String> commandArray = new ArrayList<String>();
		String buff = "";
		boolean lookForEnd = false;
		for (int i = 0; i < command.length(); i++) {
			if (lookForEnd) {
				if (command.charAt(i) == '\"') {
					if (buff.length() > 0)
						commandArray.add(buff);
					buff = "";
					lookForEnd = false;
				} else {
					buff += command.charAt(i);
				}
			} else {
				if (command.charAt(i) == '\"') {
					lookForEnd = true;
				} else if (command.charAt(i) == ' ') {
					if (buff.length() > 0)
						commandArray.add(buff);
					buff = "";
				} else {
					buff += command.charAt(i);
				}
			}
		}
		if (buff.length() > 0)
			commandArray.add(buff);

		String[] array = new String[commandArray.size()];
		for (int i = 0; i < commandArray.size(); i++) {
			array[i] = commandArray.get(i);
		}

		return array;
	}
}
