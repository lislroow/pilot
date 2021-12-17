package mgkim.framework.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.online.com.env.KConstant;

public class KDateUtil {

	public static String convert(String sDate, String orgFormat, String resFormat) throws KSysException {
		String result = null;
		Date date = null;
		try {
			date = KDateUtil.toDate(sDate, orgFormat);
		} catch(Exception e) {
			throw new KSysException(KMessage.E9003, e, sDate, orgFormat);
		}
		result = KDateUtil.toString(date, resFormat);
		return result;
	}

	public static String convert(long unixTimestamp, String resFormat) throws KSysException {
		String result = null;
		Date date = new Date(unixTimestamp);
		result = KDateUtil.toString(date, resFormat);
		return result;
	}

	public static Date toDate(String sDate, String sFormat) throws ParseException {
		Date result = null;
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		try {
			result = sdf.parse(sDate);
		} catch (ParseException e) {
			throw e;
		}
		return result;
	}

	public static String toString(Date date, String sFormat) {
		String result = null;
		if(date == null) {
			return result;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		result = sdf.format(date);
		return result;
	}

	public static String today() {
		return today(KConstant.FMT_YYYYMMDD);
	}

	public static String today(String sFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.format(new Date());
	}

	public static String now() {
		return now(KConstant.FMT_HH_MM_SS);
	}

	public static String now(String sFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.format(new Date());
	}

	public static String formatDateTime(String sDate, String separatorDate, String separatorTime) {
		return formatDateTime(sDate, separatorDate, separatorTime, 14);
	}

	public static String formatDateTime(String sDate, String separatorDate, String separatorTime, int nResultLen) {
		String yyyy = "";
		String mm = "";
		String dd = "";
		String hh24 = "";
		String mi = "";
		String ss = "";
		if(sDate == null) {
			return "";
		} else {
			sDate = sDate.replaceAll("\\D", "");
		}
		if(nResultLen > 0 && sDate.length() > nResultLen) {
			sDate = sDate.substring(0, nResultLen);
		}
		if(sDate.length() == 14) {
			yyyy = sDate.substring(0, 4);
			mm = sDate.substring(4, 6);
			dd = sDate.substring(6, 8);
			hh24 = sDate.substring(8, 10);
			mi = sDate.substring(10, 12);
			ss = sDate.substring(12, 14);
			return yyyy + separatorDate + mm + separatorDate + dd + " " + hh24 + separatorTime + mi + separatorTime + ss;
		} else if(sDate.length() == 12) {
			yyyy = sDate.substring(0, 4);
			mm = sDate.substring(4, 6);
			dd = sDate.substring(6, 8);
			hh24 = sDate.substring(8, 10);
			mi = sDate.substring(10, 12);
			return yyyy + separatorDate + mm + separatorDate + dd + " " + hh24 + separatorTime + mi;
		} else if(sDate.length() == 10) {
			yyyy = sDate.substring(0, 4);
			mm = sDate.substring(4, 6);
			dd = sDate.substring(6, 8);
			hh24 = sDate.substring(8, 10);
			return yyyy + separatorDate + mm + separatorDate + dd + " " + hh24;
		} else if(sDate.length() == 8) {
			yyyy = sDate.substring(0, 4);
			mm = sDate.substring(4, 6);
			dd = sDate.substring(6, 8);
			return yyyy + separatorDate + mm + separatorDate + dd;
		} else if(sDate.length() == 6) {
			yyyy = sDate.substring(0, 2);
			mm = sDate.substring(2, 4);
			dd = sDate.substring(4, 6);
			return yyyy + separatorDate + mm;
		} else {
			return "";
		}
	}

	public static String formatDate(String sDate, String separatorDate) {
		String dateStr = sDate.replaceAll("\\D", "");
		String str = dateStr.trim();
		String yyyy = "";
		String mm = "";
		String dd = "";
		if(str.length() == 14) {
			yyyy = str.substring(0, 4);
			if(yyyy.equals("0000")) {
				return "";
			}
			mm = str.substring(4, 6);
			if(mm.equals("00")) {
				return yyyy;
			}
			dd = str.substring(6, 8);
			if(dd.equals("00")) {
				return yyyy + separatorDate + mm;
			}
			return yyyy + separatorDate + mm + separatorDate + dd;
		} else if(str.length() == 8) {
			yyyy = str.substring(0, 4);
			if(yyyy.equals("0000")) {
				return "";
			}
			mm = str.substring(4, 6);
			if(mm.equals("00")) {
				return yyyy;
			}
			dd = str.substring(6, 8);
			if(dd.equals("00")) {
				return yyyy + separatorDate + mm;
			}
			return yyyy + separatorDate + mm + separatorDate + dd;
		} else if(str.length() == 6) {
			yyyy = str.substring(0, 4);
			if(yyyy.equals("0000")) {
				return "";
			}
			mm = str.substring(4, 6);
			if(mm.equals("00")) {
				return yyyy;
			}
			return yyyy + separatorDate + mm;
		} else if(str.length() == 4) {
			yyyy = str.substring(0, 4);
			if(yyyy.equals("0000")) {
				return "";
			} else {
				return yyyy;
			}
		} else {
			return sDate;
		}
	}

	public static String formatTime(String sTime, String separatorTime) {
		if(sTime != null && sTime.length() == 6) {
			return sTime.substring(0, 2) + separatorTime + sTime.substring(2, 4) + separatorTime + sTime.substring(4, 6);
		} else if(sTime.length() == 14) {
			return sTime.substring(8, 10) + separatorTime + sTime.substring(10, 12) + separatorTime + sTime.substring(12, 14);
		} else {
			return sTime;
		}
	}

	public static String formatDate(String sDate, String sTime, String sFormat) {
		String dateStr = removeFormatDate(sDate);
		String timeStr = removeFormatTime(sTime);
		Calendar cal = null;
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(dateStr.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(4, 6)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
		cal.set(Calendar.MINUTE, Integer.parseInt(timeStr.substring(2, 4)));
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.format(cal.getTime());
	}

	public static String removeFormatDate(String sFormatDate) throws IllegalArgumentException {
		if(sFormatDate == null || !(sFormatDate.trim().length() == 8 || sFormatDate.trim().length() == 10)) {
			throw new IllegalArgumentException("Invalid date format: " + sFormatDate);
		}
		if(sFormatDate.length() == 10) {
			sFormatDate = KStringUtil.removeMinusChar(sFormatDate);
		}
		return sFormatDate;
	}

	public static String removeFormatTime(String sFormatTime) throws IllegalArgumentException {
		if(sFormatTime.length() == 5) {
			sFormatTime = KStringUtil.remove(sFormatTime, ':');
		}
		if(sFormatTime == null || !(sFormatTime.trim().length() == 4)) {
			throw new IllegalArgumentException("Invalid time format: " + sFormatTime);
		}
		return sFormatTime;
	}

	public static String substrDay(String yyyyMMdd) {
		String d = KStringUtil.lpad(yyyyMMdd, 2, "0");
		if(yyyyMMdd.length() == 8) {
			d = d.substring(6, 8);
		}
		return d;
	}

	public static String substrMonth(String yyyyMMdd) {
		String m = KStringUtil.lpad(yyyyMMdd, 2, "0");
		if(yyyyMMdd.length() == 8) {
			m = m.substring(4, 6);
		}
		return m;
	}

	public static String substrYear(String yyyyMMdd) {
		String y = KStringUtil.lpad(yyyyMMdd, 4, "0");
		if(yyyyMMdd.length() == 8) {
			y = y.substring(0, 4);
		}
		return y;
	}

	private static String addDate(String sDate, int nYear, int nMonth, int nDay) {
		String dateStr = removeFormatDate(sDate);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(KConstant.FMT_YYYYMMDD, Locale.getDefault());
		try {
			cal.setTime(sdf.parse(dateStr));
		} catch(ParseException e) {
			throw new IllegalArgumentException("Invalid date format: " + dateStr);
		}
		if(nYear != 0) {
			cal.add(Calendar.YEAR, nYear);
		}
		if(nMonth != 0) {
			cal.add(Calendar.MONTH, nMonth);
		}
		if(nDay != 0) {
			cal.add(Calendar.DATE, nDay);
		}
		return sdf.format(cal.getTime());
	}

	public static String addYear(String sDate, int nYear) {
		return addDate(sDate, nYear, 0, 0);
	}

	public static String addMonth(String sDate, int nMonth) {
		return addDate(sDate, 0, nMonth, 0);
	}

	public static String addDay(String sDate, int nDay) {
		return addDate(sDate, 0, 0, nDay);
	}


	public static boolean isLeapYear(int nYear) {
		GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
		c.set(GregorianCalendar.YEAR, nYear);
		return c.isLeapYear(nYear);
	}

	public static boolean isLeapYear(String sYear) {
		int year = Integer.parseInt(sYear);
		return isLeapYear(year);
	}

	public static boolean isValidDate(String sDate) {
		String dateStr = removeFormatDate(sDate);

		Calendar cal;
		boolean ret = false;

		cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, Integer.parseInt(dateStr.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(4, 6)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));

		String year = String.valueOf(cal.get(Calendar.YEAR));
		String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

		String pad4Str = "0000";
		String pad2Str = "00";

		String retYear =(pad4Str + year).substring(year.length());
		String retMonth =(pad2Str + month).substring(month.length());
		String retDay =(pad2Str + day).substring(day.length());

		String retYMD = retYear + retMonth + retDay;

		if(sDate.equals(retYMD)) {
			ret = true;
		}

		return ret;
	}

	public static boolean isValidTime(String sTime) {
		String timeStr = removeFormatTime(sTime);

		Calendar cal;
		boolean ret = false;

		cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
		cal.set(Calendar.MINUTE, Integer.parseInt(timeStr.substring(2, 4)));

		String HH = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		String MM = String.valueOf(cal.get(Calendar.MINUTE));

		String pad2Str = "00";

		String retHH =(pad2Str + HH).substring(HH.length());
		String retMM =(pad2Str + MM).substring(MM.length());

		String retTime = retHH + retMM;

		if(sTime.equals(retTime)) {
			ret = true;
		}

		return ret;
	}


	public static String monthToEng(String sNumMonth) {
		String retStr = null;

		String m = KStringUtil.lpad(sNumMonth, 2, "0");

		if(sNumMonth.length() == 8) {
			m = m.substring(4, 6);
		}

		if("01".equals(m)) {
			retStr = "JAN";
		} else if("02".equals(m)) {
			retStr = "FEB";
		} else if("03".equals(m)) {
			retStr = "MAR";
		} else if("04".equals(m)) {
			retStr = "APR";
		} else if("05".equals(m)) {
			retStr = "MAY";
		} else if("06".equals(m)) {
			retStr = "JUN";
		} else if("07".equals(m)) {
			retStr = "JUL";
		} else if("08".equals(m)) {
			retStr = "AUG";
		} else if("09".equals(m)) {
			retStr = "SEP";
		} else if("10".equals(m)) {
			retStr = "OCT";
		} else if("11".equals(m)) {
			retStr = "NOV";
		} else if("12".equals(m)) {
			retStr = "DEC";
		}

		return retStr;
	}

	public static String weekToKor(String sEngWeek) {
		String retStr = null;

		if(sEngWeek.equals("SUN")) {
			retStr = "일요일";
		} else if(sEngWeek.equals("MON")) {
			retStr = "월요일";
		} else if(sEngWeek.equals("TUE")) {
			retStr = "화요일";
		} else if(sEngWeek.equals("WED")) {
			retStr = "수요일";
		} else if(sEngWeek.equals("THR")) {
			retStr = "목요일";
		} else if(sEngWeek.equals("FRI")) {
			retStr = "금요일";
		} else if(sEngWeek.equals("SAT")) {
			retStr = "토요일";
		}
		return retStr;
	}
}
