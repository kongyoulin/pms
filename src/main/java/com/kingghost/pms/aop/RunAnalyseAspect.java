package com.kingghost.pms.aop;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.kingghost.pms.annotation.NotRunAnalyseAnno;
import com.kingghost.pms.annotation.RunAnalyseAnno;
import com.kingghost.pms.entity.TCMAInfo;
import com.kingghost.pms.repository.TCMAInfoRepository;
import com.kingghost.pms.utils.Pair;

@Aspect
@Component
@Controller
@RequestMapping("/run_analyse_aspect")
@NotRunAnalyseAnno
public class RunAnalyseAspect {

	@Value("${enableRunAnalyse:true}")
	private Boolean enableRunAnalyse;

	@Value("${enableOnlyRunAnalyse:true}")
	private Boolean enableOnlyRunAnalyse;

	@Autowired
	private Gson gson;

	@Autowired
	private TCMAInfoRepository tcmaInfoRepository;

	@Autowired
	private KafkaTemplate<String, TCMAInfo> kafkaTemplate;
	
	Map<String, Stack<TCMAInfo>> threadStackMap = new ConcurrentHashMap<String, Stack<TCMAInfo>>();
	Map<String, Map<TCMAInfo, Pair<Long, Double>>> threadMethodSignaturePairMap = new ConcurrentHashMap<String, Map<TCMAInfo, Pair<Long, Double>>>();
	Map<String, Map<TCMAInfo, Long>> threadMethodSignatureInnerUseTimeMap = new ConcurrentHashMap<String, Map<TCMAInfo, Long>>();
	Map<String, AtomicInteger> threadAtomicIntegerMap = new ConcurrentHashMap<String, AtomicInteger>();

	@Around(value = "execution(* com.kingghost.pms..*.*(..))")
	public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Date startDate = new Date();
		Boolean logRunAnalyse = false;
		if (enableRunAnalyse && (!enableOnlyRunAnalyse || (enableOnlyRunAnalyse && (proceedingJoinPoint.getTarget().getClass().getAnnotation(RunAnalyseAnno.class) != null || proceedingJoinPoint.getTarget().getClass()
				.getMethod(proceedingJoinPoint.getSignature().getName(), ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterTypes()).getAnnotation(RunAnalyseAnno.class) != null)))) {
			logRunAnalyse = true;
		}
		if(proceedingJoinPoint.getTarget().getClass()
				.getMethod(proceedingJoinPoint.getSignature().getName(), ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterTypes()).getAnnotation(NotRunAnalyseAnno.class) != null) {
			logRunAnalyse = false;
		}
		try {
			if (logRunAnalyse) {
				kafkaTemplate.send("kingghostpmstopic", new TCMAInfo() {
					{
						setThreadName(Thread.currentThread().getName());
						setClassName(proceedingJoinPoint.getTarget().getClass().getName());
						setMethodName(proceedingJoinPoint.getSignature().getName());
						setArgs(Arrays.toString(proceedingJoinPoint.getArgs()));
						setFlag("start");
					}
				});
			}

			Object returnObj = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
			Date endDate = new Date();
			if (logRunAnalyse) {
				kafkaTemplate.send("kingghostpmstopic", new TCMAInfo() {
					{
						setThreadName(Thread.currentThread().getName());
						setClassName(proceedingJoinPoint.getTarget().getClass().getName());
						setMethodName(proceedingJoinPoint.getSignature().getName());
						setArgs(Arrays.toString(proceedingJoinPoint.getArgs()));
						setFlag("end");
						setTotalTime(endDate.getTime() - startDate.getTime());
					}
				});
			}
			return returnObj;
		} catch (Throwable e) {
			if (logRunAnalyse) {
				kafkaTemplate.send("kingghostpmstopic", new TCMAInfo() {
					{
						setThreadName(Thread.currentThread().getName());
						setClassName(proceedingJoinPoint.getTarget().getClass().getName());
						setMethodName(proceedingJoinPoint.getSignature().getName());
						setArgs(Arrays.toString(proceedingJoinPoint.getArgs()));
						setFlag("error");
					}
				});
			}
			throw e;
		}
	}

	@RequestMapping(value = "/getEnableRunAnalyse.json", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public Boolean getEnableRunAnalyse() {
		return this.enableRunAnalyse;
	}

	@RequestMapping(value = "/setEnableRunAnalyse.json", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public void setEnableRunAnalyse(@RequestParam(required = true, name = "enableRunAnalyse") Boolean enableRunAnalyse) {
		this.enableRunAnalyse = enableRunAnalyse;
	}

	@RequestMapping(value = "/getEnableOnlyRunAnalyse.json", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public Boolean getEnableOnlyRunAnalyse() {
		return this.enableOnlyRunAnalyse;
	}

	@RequestMapping(value = "/setEnableOnlyRunAnalyse.json", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public void setEnableOnlyRunAnalyse(@RequestParam(required = true, name = "enableOnlyRunAnalyse") Boolean enableOnlyRunAnalyse) {
		this.enableOnlyRunAnalyse = enableOnlyRunAnalyse;
	}

	@KafkaListener(topics = { "kingghostpmstopic" })
	public void receiveRecord(ConsumerRecord<String, String> record) {
		Optional<TCMAInfo> optional = Optional.ofNullable(gson.fromJson(record.value(), TCMAInfo.class));
		if (optional.isPresent()) {
			TCMAInfo tcmaInfo = optional.get();
			String threadName = tcmaInfo.getThreadName();
			String flag = tcmaInfo.getFlag();

			if (flag.equals("error")) {
				clearByThreadName(threadName);
			} else if (flag.equals("start")) {
				if (threadStackMap.containsKey(threadName)) {
					tcmaInfo.setOrderNo(threadAtomicIntegerMap.get(threadName).incrementAndGet());
					threadStackMap.get(threadName).push(tcmaInfo);
				} else {
					AtomicInteger atomicInteger = new AtomicInteger();
					tcmaInfo.setOrderNo(atomicInteger.incrementAndGet());

					threadAtomicIntegerMap.put(threadName, atomicInteger);

					Stack<TCMAInfo> stack = new Stack<TCMAInfo>();
					stack.push(tcmaInfo);
					threadStackMap.put(threadName, stack);
				}
			} else if (flag.equals("end")) {
				final Long totalTime = tcmaInfo.getTotalTime();

				TCMAInfo currentStartTCMAInfo = threadStackMap.get(threadName).pop();
				tcmaInfo.setOrderNo(currentStartTCMAInfo.getOrderNo());
				tcmaInfo.setChildren(currentStartTCMAInfo.getChildren());

				if (threadMethodSignatureInnerUseTimeMap.containsKey(threadName) && threadMethodSignatureInnerUseTimeMap.get(threadName).containsKey(tcmaInfo)) {
					final Long currentUsedTime = totalTime - threadMethodSignatureInnerUseTimeMap.get(threadName).get(tcmaInfo);
					if (threadMethodSignaturePairMap.containsKey(threadName)) {
						if (threadMethodSignaturePairMap.get(threadName).containsKey(tcmaInfo)) {
							threadMethodSignaturePairMap.get(threadName).put(tcmaInfo, new Pair<Long, Double>(threadMethodSignaturePairMap.get(threadName).get(tcmaInfo).getHead() + currentUsedTime, null));
						} else {
							threadMethodSignaturePairMap.get(threadName).put(tcmaInfo, new Pair<Long, Double>(currentUsedTime, null));
						}
					} else {
						threadMethodSignaturePairMap.put(threadName, new HashMap<TCMAInfo, Pair<Long, Double>>() {
							{
								put(tcmaInfo, new Pair<Long, Double>(currentUsedTime, null));
							}
						});
					}
					threadMethodSignatureInnerUseTimeMap.get(threadName).remove(tcmaInfo);
				} else {
					if (threadMethodSignaturePairMap.containsKey(threadName)) {
						if (threadMethodSignaturePairMap.get(threadName).containsKey(tcmaInfo)) {
							threadMethodSignaturePairMap.get(threadName).put(tcmaInfo, new Pair<Long, Double>(threadMethodSignaturePairMap.get(threadName).get(tcmaInfo).getHead() + totalTime, null));
						} else {
							threadMethodSignaturePairMap.get(threadName).put(tcmaInfo, new Pair<Long, Double>(totalTime, null));
						}
					} else {
						threadMethodSignaturePairMap.put(threadName, new HashMap<TCMAInfo, Pair<Long, Double>>() {
							{
								put(tcmaInfo, new Pair<Long, Double>(totalTime, null));
							}
						});
					}
				}

				if (threadStackMap.get(threadName).empty()) {

					for (Map.Entry<TCMAInfo, Pair<Long, Double>> entry : threadMethodSignaturePairMap.get(threadName).entrySet()) {
						if(totalTime.intValue() == 0) {
							entry.getKey().setUseRatio(0D);
						}else {
							entry.getKey().setUseRatio(entry.getValue().getHead().doubleValue() / totalTime.doubleValue() * 100);
						}
						
						entry.getKey().setUseTime(entry.getValue().getHead().longValue());

//						entry.getValue().setTail(entry.getValue().getHead().doubleValue() / usedTime.doubleValue() * 100);
					}
					
					tcmaInfoRepository.saveAndFlush(tcmaInfo);

					/*
					 * List<Map.Entry<TCMAInfo, Pair<Long, Double>>> list = new
					 * LinkedList<Map.Entry<TCMAInfo, Pair<Long,
					 * Double>>>(threadMethodSignaturePairMap.get(threadName).entrySet());
					 * 
					 * Collections.sort(list, new Comparator<Map.Entry<TCMAInfo, Pair<Long,
					 * Double>>>() {
					 * 
					 * @Override public int compare(Entry<TCMAInfo, Pair<Long, Double>> o1,
					 * Entry<TCMAInfo, Pair<Long, Double>> o2) { return
					 * o2.getValue().getHead().intValue() - o1.getValue().getHead().intValue(); }
					 * });
					 * 
					 * System.out.println(threadName + ":"); for (Map.Entry<TCMAInfo, Pair<Long,
					 * Double>> element : list) { System.out.println("\t" +
					 * element.getKey().getUseTime() + "\t" + String.format("%.3f",
					 * element.getKey().getUseRatio()) + "%\t" + element.getKey()); }
					 * 
					 * System.out.println();
					 */

					clearByThreadName(threadName);
				} else {

					if (threadStackMap.get(threadName).peek().getChildren() == null) {
						threadStackMap.get(threadName).peek().setChildren(new LinkedList<TCMAInfo>() {
							{
								add(tcmaInfo);
							}
						});

					} else {
						threadStackMap.get(threadName).peek().getChildren().add(tcmaInfo);
					}

					final TCMAInfo lastMethodSignature = threadStackMap.get(threadName).peek();
					if (threadMethodSignatureInnerUseTimeMap.containsKey(threadName)) {
						if (threadMethodSignatureInnerUseTimeMap.get(threadName).containsKey(lastMethodSignature)) {
							threadMethodSignatureInnerUseTimeMap.get(threadName).put(lastMethodSignature, threadMethodSignatureInnerUseTimeMap.get(threadName).get(lastMethodSignature) + totalTime);
						} else {
							threadMethodSignatureInnerUseTimeMap.get(threadName).put(lastMethodSignature, totalTime);
						}
					} else {
						threadMethodSignatureInnerUseTimeMap.put(threadName, new HashMap<TCMAInfo, Long>() {
							{
								put(lastMethodSignature, totalTime);
							}
						});
					}
				}
			}
		}
	}

	private void clearByThreadName(String threadName) {
		threadStackMap.remove(threadName);
		threadMethodSignaturePairMap.remove(threadName);
		threadMethodSignatureInnerUseTimeMap.remove(threadName);
		threadAtomicIntegerMap.remove(threadName);
	}
}
