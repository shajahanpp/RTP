package com.experian.reporting;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experian.reporting.model.OutputClass.ResultMap;

@SpringBootApplication
@RestController
public class ReportingApplication {


	public static void main(String[] args) {
		SpringApplication.run(ReportingApplication.class, args);
	}

	@GetMapping("/v1/createextendreport")
	public ResultMap hello(@RequestParam String runid) {
		
		System.out.println("************************************Started**********************************************");
		ReportingClass objReporting = new ReportingClass();
		ResultMap objResultMap = objReporting.TriggerReportJar(runid);
		System.out.println("************************************Ended**********************************************");
		
		/*
		 * Response res = new Response(); res.setResponseObj(id);
		 */
		return objResultMap;
	}

}
            