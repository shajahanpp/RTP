package com.experian.responsevalidation.controller;

import java.util.HashMap;

public class MainRequest {

    private String jsonFilePath;
    public HashMap<String, Object> input=new HashMap<String, Object>();

    @Override
    public String toString() {
	return "MainRequest [jsonFilePath=" + jsonFilePath + ", input=" + input + "]";
    }

    public String getJsonFilePath() {
	return jsonFilePath;
    }

    public void setJsonFilePath(String jsonFilePath) {
	this.jsonFilePath = jsonFilePath;
    }
}