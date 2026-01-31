package com.mss.backOffice.services;

import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ToolsService {
	private static final Logger logger = LoggerFactory.getLogger(ToolsService.class);
	
	
	
	public static String maskCardNumber(String cardNumber) {
		

		int replacingStringLength = cardNumber.length() - 10;
		String replacingString = "";
		int index = 0;
		while (index < replacingStringLength) {
			replacingString = replacingString + "*";
			index++;
		}
		return maskString(cardNumber, replacingString, 6, cardNumber.length() - 4);
	}
	private static String maskString(String str, String replacingStr, int start, int end) {
		return StringUtils.overlay(str, replacingStr, start, end);
	}
	

	public String formatField(Object field, int length) {
		if (field == null) {
			return getSpace(length);
		}

		String value = field.toString().replace('.',',').trim();
		if (value.length() > length) {
			return value.substring(0, length);
		}

		return value + getSpace(length - value.length());
	}
	public String getSpace(int count) {

		String space="";

		for(int i=0;i<count;i++)

			space+=" ";

		return space;

	}
	public static void print(String x, String y) {
		if (!x.matches("0|formula 0"))
			logger.info(y + " " + x + " at date " + LocalDateTime.now());
	}
	public static String getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getClassName() + ": " + String.valueOf(Thread.currentThread().getStackTrace()[2].getLineNumber());
	}

}
