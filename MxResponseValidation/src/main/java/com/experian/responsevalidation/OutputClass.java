package com.experian.responsevalidation;


import java.util.List;


public class OutputClass {

	public static class ResultMap {

	
		private String validationStatus;
		private List<String> differenceList;
		
		public String getValidationStatus() {
			return validationStatus;
		}
		public void setValidationStatus(String validationStatus) {
			this.validationStatus = validationStatus;
		}
		public List<String> getDifferenceList() {
			return differenceList;
		}
		public void setDifferenceList(List<String> differenceList) {
			this.differenceList = differenceList;
		}
		
	}
}
