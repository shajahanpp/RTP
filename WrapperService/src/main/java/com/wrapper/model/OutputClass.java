package com.wrapper.model;

public class OutputClass {

	public static class ResultMap {
		private String runID;
		private String reportLocation;
		private String failedMessage;

		public String getReportLocation() {
			return reportLocation;
		}

		public void setReportLocation(String reportLocation) {
			this.reportLocation = reportLocation;
		}

		public String getRunID() {
			return runID;
		}

		public void setRunID(String runID) {
			this.runID = runID;
		}

		public String getFailedMessage() {
			return failedMessage;
		}

		public void setFailedMessage(String failedMessage) {
			this.failedMessage = failedMessage;
		}

	}
}
