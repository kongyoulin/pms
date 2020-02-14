package com.kingghost.pms.service;

import org.springframework.stereotype.Service;

import com.kingghost.pms.annotation.RunAnalyseAnno;

@RunAnalyseAnno
@Service
public class TestServiceImpl implements TestService {

	@Override
	public void test(String a) throws InterruptedException {
		int b = 3;
		System.out.println(20 / b);
	}

	
}
