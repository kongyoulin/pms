package com.kingghost.pms.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	/** 
	 * 正则表达式匹配两个指定字符串中间的内容 
	 * @param soap 
	 * @return 
	 */
	public static List<String> getSubUtil(String soap, String rgex) {
		List<String> list = new ArrayList<String>();
		Pattern pattern = Pattern.compile(rgex);// 匹配的模式  
		Matcher m = pattern.matcher(soap);
		while (m.find()) {
			int i = 1;
			list.add(m.group(i));
			i++;
		}
		return list;
	}

	/** 
	 * 返回单个字符串，若匹配到多个的话就返回第一个，方法与getSubUtil一样 
	 * @param soap 
	 * @param rgex 
	 * @return 
	 */
	public static String getSubUtilSimple(String soap, String rgex) {
		Pattern pattern = Pattern.compile(rgex);// 匹配的模式  
		Matcher m = pattern.matcher(soap);
		while (m.find()) {
			return m.group(1);
		}
		return "";
	}
	
	public static String removeHtmlTag(String removedStr){
		String regEx_html="<[^>]+>"; 
		Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
        Matcher m_html=p_html.matcher(removedStr); 
        return m_html.replaceAll(""); //过滤html标签 
	}

	public static void main(String[] args) {
		String str = "2019-08-07 16:37:43.745 ERROR 8656 --- [io-10503-exec-7] com.belle.scm.pp.aop.RunAnalyseAspect    : class:class com.belle.scm.pp.web.controller.BlMoTicketReportController method:reportMesTicket args:[org.apache.shiro.web.servlet.ShiroHttpServletRequest@73ef4785] start";
		String rgex = "\\[(.+?)\\]";
		System.out.println(getSubUtil(str, rgex));
		System.out.println(getSubUtilSimple(str, rgex));
	}
}
