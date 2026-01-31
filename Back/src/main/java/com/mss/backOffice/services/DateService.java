package com.mss.backOffice.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class DateService {
	
	
	   public long getDatePart(Date date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    cal.set(Calendar.HOUR_OF_DAY, 0);
		    cal.set(Calendar.MINUTE, 0);
		    cal.set(Calendar.SECOND, 0);
		    cal.set(Calendar.MILLISECOND, 0);
		    return cal.getTimeInMillis();
		}
	   
	   public long getDatePartExpRenewel(Date date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		  
		    return cal.getTimeInMillis();
		}
	   
	   public Date getPreDate(LocalDate date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
		    int firstDay= cal.getActualMinimum(Calendar.DAY_OF_MONTH);
		    cal.set(Calendar.DAY_OF_MONTH, firstDay);
		    cal.set(Calendar.HOUR_OF_DAY, 0);
		    cal.set(Calendar.MINUTE, 0);
		    cal.set(Calendar.SECOND, 0);
		    cal.set(Calendar.MILLISECOND, 0);
		    return cal.getTime();
		}
		public Date expireCard(LocalDate date) {
		    Calendar calendar = Calendar.getInstance();
		    calendar.setTime(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
		    
		    int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		    calendar.set(Calendar.DAY_OF_MONTH, lastDay);
		    
		    calendar.set(Calendar.HOUR_OF_DAY, 23);
		    calendar.set(Calendar.MINUTE, 59);
		    calendar.set(Calendar.SECOND, 59);
		    return calendar.getTime();
		}
		public Date expireCard(Date currentDatePlusLifeCycle) {
		    Calendar calendar = Calendar.getInstance();
		    calendar.setTime(currentDatePlusLifeCycle);
		    
		    int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		    calendar.set(Calendar.DAY_OF_MONTH, lastDay);
		    
		    calendar.set(Calendar.HOUR_OF_DAY, 23);
		    calendar.set(Calendar.MINUTE, 59);
		    calendar.set(Calendar.SECOND, 59);
		    return calendar.getTime();
		}

		public String generateDateString() {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
			String currentDate = now.format(formatter);
			return currentDate;
		}

		public String generateDateAndTime() {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");
			String currentDate = now.format(formatter);
			return currentDate;
		}
}
