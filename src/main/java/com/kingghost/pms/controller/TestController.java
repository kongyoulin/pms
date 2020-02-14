package com.kingghost.pms.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.kingghost.pms.annotation.RunAnalyseAnno;
import com.kingghost.pms.service.TestService;

@RestController
@RequestMapping("/test")
@RunAnalyseAnno
public class TestController {

	@Autowired
	private TestService testService;
	
	
	@RequestMapping("/test")
	@ResponseBody
	public Map<String, Object> test(HttpServletRequest request) throws Exception{
		testService.test("a");
		return new HashMap<String, Object>(){{
			put("datasource", "a");
		}};
	}
}
