package com.experian.utilities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchCookieException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

//import ReportingEngine.ExtentReport;

public class CustomException extends Exception {

	/* Variable Declaration */
	public String errorMessage;
	String exceptionMessage;
	String subString;
	int getindex;;

	public CustomException(String msg, Exception ex) {
		super(msg);

		if (ex instanceof InvocationTargetException) {
			if (ex.getCause() instanceof CustomException) {
				CustomException exception = (CustomException) ex.getCause();
				errorMessage = exception.errorMessage;

			} else {
				try {
					errorMessage = ((InvocationTargetException) ex).getTargetException().toString();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

		} else if (ex instanceof CustomException) {
			CustomException exception = (CustomException) ex;
			errorMessage = exception.errorMessage;
		} else {
			errorMessage = getExceptionTypeMessages(ex, msg);
			msg = errorMessage;
		}
	}

	public CustomException()
	{
		
	}
	public CustomException(String msg, AssertionError ex) {
		super(msg);
		errorMessage = getExceptionTypeMessages(ex, msg);
		msg = errorMessage;
	}

	public String getExceptionTypeMessages(AssertionError exceptionType, String message) {
		errorMessage = exceptionType.getMessage().toString();
		getindex = errorMessage.indexOf("(Session info");

		if (getindex != -1) {
			subString = errorMessage.substring(0, getindex);
			errorMessage = message + "," + subString;
		}

		exceptionMessage = "Assertion failed: ";
		System.out.println(exceptionMessage + errorMessage);

		return exceptionMessage + errorMessage;
	}

	public String getExceptionTypeMessages(Exception exceptionType, String message) {
		getCustomisedMessage(exceptionType, message);
		if (exceptionType instanceof NoSuchElementException) {
			exceptionMessage = "NoSuchElementException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);
		}

		else if (exceptionType instanceof NoAlertPresentException) {
			exceptionMessage = "NoAlertPresentException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);
		} else if (exceptionType instanceof InstantiationException) {
			exceptionMessage = "NoAlertPresentException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);
		}

		else if (exceptionType instanceof IllegalAccessException) {
			exceptionMessage = "NoAlertPresentException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);
		} else if (exceptionType instanceof NotFoundException) {
			exceptionMessage = "NotFoundException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof NoSuchWindowException) {
			exceptionMessage = "NoSuchWindowException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof NoSuchCookieException) {
			exceptionMessage = "NoSuchCookieException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof StaleElementReferenceException) {
			exceptionMessage = "StaleElementReferenceException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof ElementNotVisibleException) {
			exceptionMessage = "ElementNotVisibleException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof NoSuchSessionException) {
			exceptionMessage = "NoSuchSessionException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof TimeoutException) {
			exceptionMessage = "TimeoutException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof IOException) {
			exceptionMessage = "IOException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof NullPointerException) {
			exceptionMessage = "NullPointerException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);
		} else if (exceptionType instanceof WebDriverException) {
			exceptionMessage = "WebDriverException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof RuntimeException) {
			exceptionMessage = "RuntimeException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof SQLException) {
			exceptionMessage = "SQLException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);

		} else if (exceptionType instanceof ClassNotFoundException) {
			exceptionMessage = "ClassNotFoundException has occurred.There is no class named ";
			System.out.println(exceptionMessage + errorMessage);
		} else if (exceptionType instanceof NoSuchMethodException) {
			exceptionMessage = "NoSuchMethodException has occurred. .There is no method named ";
			System.out.println(exceptionMessage + errorMessage);
		} else if (exceptionType instanceof IndexOutOfBoundsException) {
			exceptionMessage = "IndexOutOfBoundsException has occurred.";
			System.out.println(exceptionMessage + errorMessage);
		} else if (exceptionType instanceof MalformedURLException) {
			exceptionMessage = "MalformedURLException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);
		} else if (exceptionType instanceof ElementNotInteractableException) {
			exceptionMessage = "ElementNotInteractableException has occurred. ";
			System.out.println(exceptionMessage + errorMessage);
		}

		errorMessage = exceptionMessage + errorMessage;

		// JSONException IOException SAXException ParserConfigurationException
		// XPathExpressionException FileNotFoundException ClientProtocolException
		return errorMessage;

	}

	/* Method for getting Customised Message from ExceptionType */
	private void getCustomisedMessage(Exception exceptionType, String message) {
		if (exceptionType != null) {
			if (exceptionType.getMessage() != null) {
				errorMessage = exceptionType.getMessage().toString();
				getindex = errorMessage.indexOf("(Session info");

				if (getindex != -1) {
					subString = errorMessage.substring(0, getindex);
					errorMessage = message + "," + subString;
				}
			} else {
				errorMessage = message;
			}
		}
	}

}
