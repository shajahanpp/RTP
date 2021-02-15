package com.experian.tablevalidation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OutputClass {

	public static class ResultMap {

		@JsonIgnore
		private String powercurveID;
		private String testStatus;

		private List<TableResultMap> tableResultList;

		public String getTestStatus() {
			return testStatus;
		}

		public void setTestStatus(String testStatus) {
			this.testStatus = testStatus;
		}

		public String getPowercurveID() {
			return powercurveID;
		}

		public void setPowercurveID(String powercurveID) {
			this.powercurveID = powercurveID;
		}

		public List<TableResultMap> getTableResultList() {
			return tableResultList;
		}

		public void setTableResultList(List<TableResultMap> tableResultList) {
			this.tableResultList = tableResultList;
		}

	}

	public static class TableResultMap {

		private String tableName;
		private String validationStatus;
		private List<String> differenceList;
		private String failedMessage;

		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public String getValidationStatus() {
			return validationStatus;
		}

		public void setValidationStatus(String validationStatus) {
			this.validationStatus = validationStatus;
		}

		

		public String getFailedMessage() {
			return failedMessage;
		}

		public void setFailedMessage(String failedMessage) {
			this.failedMessage = failedMessage;
		}

		public List<String> getDifferenceList() {
			return differenceList;
		}

		public void setDifferenceList(List<String> differenceList) {
			this.differenceList = differenceList;
		}

		
	}

	
}
