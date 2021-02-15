
package com.wrapper.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wrapper.clientutils.WrapperMain;
import com.wrapper.model.OutputClass;
import com.wrapper.model.OutputClass.ResultMap;

@SpringBootApplication
@RestController
public class WrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(WrapperApplication.class, args);
	}

	@GetMapping("/v1/getTestRun")
	public ResultMap hello() {

		WrapperMain wrapperMain = new WrapperMain();
		ResultMap objResultMap = null;
		try {
			objResultMap = wrapperMain.GetTestRunResults();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		OutputClass objResult = new OutputClass();

		return objResultMap;
	}

}
