
package com.runid.java;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runid.model.OutputClass;

@SpringBootApplication
@RestController
public class RunIDCreationApplication {


	public static void main(String[] args) {
		SpringApplication.run(RunIDCreationApplication.class, args);
	}

	@GetMapping("/v1/getrunid")
	public OutputClass hello(@RequestParam String projectid) {
		
		DBValidation objDBValidation=new DBValidation();
		long runid=objDBValidation.AddEntriesToRunTable(Long.parseLong(projectid));
		OutputClass objResult=new OutputClass();
		objResult.setRunID(runid);
		return objResult;
	}

}
            