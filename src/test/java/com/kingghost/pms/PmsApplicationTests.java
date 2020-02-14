package com.kingghost.pms;

import java.util.Arrays;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.kingghost.pms.entity.TCMAInfo;
import com.kingghost.pms.repository.TCMAInfoRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PmsApplication.class })
class PmsApplicationTests {

	@Autowired
	DataSource datasource;

	@Autowired
	KafkaTemplate<String, TCMAInfo> kafkaTemplate;

	@Autowired
	Gson gson;

	@Autowired
	TCMAInfoRepository tCMAInfoRepository;

	@Test
	void testJpaRepository() {

		TCMAInfo tcmaInfo = new TCMAInfo();
		{
			tcmaInfo.setArgs("args");
			tcmaInfo.setThreadName("thread name");
			tcmaInfo.setClassName("class name");
			tcmaInfo.setMethodName("method name");

			tcmaInfo.setChildren(new LinkedList<TCMAInfo>() {
				{
					add(new TCMAInfo() {
						{
							setArgs("args1");
							setThreadName("thread name1");
							setClassName("class name1");
							setMethodName("method name1");
						}
					});
					add(new TCMAInfo() {
						{
							setArgs("args2");
							setThreadName("thread name2");
							setClassName("class name2");
							setMethodName("method name2");
						}
					});
				}
			});

		};

		tCMAInfoRepository.saveAndFlush(tcmaInfo);
		System.out.println(tCMAInfoRepository);
	}

	@Test
	void contextLoads() throws Exception {
		System.out.println(datasource.getConnection().getClass().getName());
	}

	@Test
	void testKafkaTemplate() {
		System.out.println(kafkaTemplate);
		kafkaTemplate.send("test", new TCMAInfo() {
			{
				setThreadName(Thread.currentThread().getName());
				setClassName("class name");
				setMethodName("method name");
				setArgs(Arrays.toString(new int[] { 1, 2, 3, 4 }));
				setFlag("start");
			}
		});
	}

}
