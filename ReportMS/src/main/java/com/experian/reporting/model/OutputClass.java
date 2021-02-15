package com.experian.reporting.model;




public class OutputClass {

	public static class ResultMap {

		private String reportLocation;
		private String runStatus;
		private String statusMessage;
		private String localFileLocation;

		public String getReportLocation() {
			return reportLocation;
		}

		public void setReportLocation(String reportLocation) {
			this.reportLocation = reportLocation;
		}

		public String getStatusMessage() {
			return statusMessage;
		}

		public void setStatusMessage(String statusMessage) {
			this.statusMessage = statusMessage;
		}

		public String getRunStatus() {
			return runStatus;
		}

		public void setRunStatus(String runStatus) {
			this.runStatus = runStatus;
		}

		public String getLocalFileLocation() {
			return localFileLocation;
		}

		public void setLocalFileLocation(String localFileLocation) {
			this.localFileLocation = localFileLocation;
		}
		
		
	}
}
