package mgkim.framework.core.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.crypto.codec.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mgkim.framework.core.type.KType.TUuidType;

public class KStringUtil {

	public static final String EMPTY = "";


	public static String createUuid(boolean isShorten) {
		String result = null;
		String uuid = UUID.randomUUID().toString();
		if (isShorten) {
			String str = Long.toString(ByteBuffer.wrap(uuid.getBytes()).getLong(), Character.MAX_RADIX);
			result = KStringUtil.lpad(str, 13, "0"); // 생성된 uuid를 13자로 맞추기 위해 좌측에 0을 채우도록 함
		}
		return result;
	}

	public static String createUuid() {
		return createUuid(true);
	}

	public static String createUuid(boolean isShorten, TUuidType type) {
		String result = null;
		String uuid = UUID.randomUUID().toString();
		if (isShorten) {
			String str = Long.toString(ByteBuffer.wrap(uuid.getBytes()).getLong(), Character.MAX_RADIX);
			result = type.prefix() + KStringUtil.lpad(str, type.length(), "0"); // 생성된 uuid를 13자로 맞추기 위해 좌측에 0을 채우도록 함
		}
		return result;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String remove(String str, char remove) {
		if (isEmpty(str) || str.indexOf(remove) == -1) {
			return str;
		}
		char[] chars = str.toCharArray();
		int pos = 0;
		for (int i=0; i<chars.length; i++) {
			if (chars[i] != remove) {
				chars[pos++] = chars[i];
			}
		}
		return new String(chars, 0, pos);
	}

	public static String removeMinusChar(String str) {
		return remove(str, '-');
	}

	public static String decode(String sourceStr, String compareStr, String returnStr, String defaultStr) {
		if (sourceStr == null && compareStr == null) {
			return returnStr;
		}
		if (sourceStr == null && compareStr != null) {
			return defaultStr;
		}
		if (sourceStr.trim().equals(compareStr)) {
			return returnStr;
		}
		return defaultStr;
	}

	private static String toJson(Object obj, boolean isPretty) {
		String json = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (isPretty) {
				json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
			} else {
				json = mapper.writer().writeValueAsString(obj);
				json = json.replaceAll("\\\\r|\\\\n|\\\\|\\t", "");
				json = json.substring(1, json.length()-1);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static String toJson(Object obj) {
		String json = toJson(obj, true);
		return json;
	}

	public static String toJsonNoPretty(Object obj) {
		String json = toJson(obj, false);
		return json;
	}

	public static String toJson2(Object obj) {
		String json = null;
		try {
			json = new JSONObject(obj+"").toString(2); // pretty json string, indent size 2
		} catch (JSONException e) {
			return obj+"";
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseJson(String json) {
		Map<String, Object> result = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			result = (Map<String, Object>) mapper.readValue(json, Map.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isJson(String text) {
		if (!KStringUtil.isEmpty(text) && text.length() >= 2) {
			String firstChar = text.substring(0, 1);
			String lastChar = text.substring(text.length()-1);
			if (firstChar.equals("{") && lastChar.equals("}")) {
				return true;
			}
			if (firstChar.equals("[") && lastChar.equals("]")) {
				return true;
			}
		}
		return false;
	}


	public static Boolean toBoolean(String value) {
		if (value == null) {
			return false;
		}
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y")) {
			return true;
		} else {
			return false;
		}
	}

	public static String[] splitByWholeSeparator(String str) {
		return org.apache.commons.lang3.StringUtils.splitByWholeSeparator(str, null, 0);
	}

	public static String nvl(Object object) {
		return nvl(object, EMPTY);
	}

	public static String nvl(Object object, String def) {
		String string = EMPTY;
		if (object != null) {
			string = object.toString().trim();
		}
		if (EMPTY.equals(string)) {
			string = def;
		}
		return string;
	}

	public static String nvl2(Object object) {
		String result = nvl2(object, EMPTY);
		return result;
	}

	public static String nvl2(Object object, String def) {
		String result = nvl(object, EMPTY);
		if ("null".equalsIgnoreCase(result)) {
			result = def;
		}
		return result;
	}

	public static int nvl(Object object, int def) {
		int result = 0;
		try {
			result = Integer.parseInt(object.toString());
		} catch (Exception ex) {
			result = def;
		}
		return result;
	}


	public static String unscript(String str) {
		if (str == null || str.trim().equals(EMPTY)) {
			return EMPTY;
		}
		String ret = str;
		ret = ret.replaceAll("<(S|s)(C|c)(R|r)(I|i)(P|p)(T|t)", "&lt;script");
		ret = ret.replaceAll("</(S|s)(C|c)(R|r)(I|i)(P|p)(T|t)", "&lt;/script");
		ret = ret.replaceAll("<(O|o)(B|b)(J|j)(E|e)(C|c)(T|t)", "&lt;object");
		ret = ret.replaceAll("</(O|o)(B|b)(J|j)(E|e)(C|c)(T|t)", "&lt;/object");
		ret = ret.replaceAll("<(A|a)(P|p)(P|p)(L|l)(E|e)(T|t)", "&lt;applet");
		ret = ret.replaceAll("</(A|a)(P|p)(P|p)(L|l)(E|e)(T|t)", "&lt;/applet");
		ret = ret.replaceAll("<(E|e)(M|m)(B|b)(E|e)(D|d)", "&lt;embed");
		ret = ret.replaceAll("</(E|e)(M|m)(B|b)(E|e)(D|d)", "&lt;embed");
		ret = ret.replaceAll("<(F|f)(O|o)(R|r)(M|m)", "&lt;form");
		ret = ret.replaceAll("</(F|f)(O|o)(R|r)(M|m)", "&lt;form");
		return ret;
	}

	public static String replaceEmptyLine(String str) {
		if (str == null || str.trim().equals(EMPTY)) {
			return EMPTY;
		}
		String ret = str;
		ret = Pattern.compile("([\r|\n])[\\s|\\t]*[\r|\n]").matcher(ret).replaceAll("$1");
		return ret;
	}

	public static String replaceWhiteSpace(String str) {
		if (str == null || str.trim().equals(EMPTY)) {
			return EMPTY;
		}
		String ret = str;
		ret = ret.replaceAll("(\\s|\\n|\\t)+", " ");
		return ret;
	}

	public static String toCommaNum(String str) {
		if (str == null || EMPTY.equals(str))
			return str;
		else {
			str = str.replaceAll(",", EMPTY);
			DecimalFormat df = new DecimalFormat("#,##0");
			double number = Double.parseDouble(str);
			return df.format(number);
		}
	}

	public static String toCommaNum(Number val) {
		String str = EMPTY;
		str = EMPTY + val;

		if (str == null || EMPTY.equals(str))
			return str;
		else {
			DecimalFormat df = new DecimalFormat("#,##0");
			double number = Double.parseDouble(str);
			return df.format(number);
		}
	}

	public static String toCommaNum2(String str) {
		if (str == null || EMPTY.equals(str))
			return str;
		else {
			DecimalFormat df = new DecimalFormat("#,##0.00");
			double number = Double.parseDouble(str);
			str = df.format(number);
			str = str.replace(".000", EMPTY);
			return str;
		}
	}

	public static String toCommaNum2(Number val) {
		String str = EMPTY;
		str = EMPTY + val;

		if (str == null || EMPTY.equals(str))
			return str;
		else {
			DecimalFormat df = new DecimalFormat("#,##0.00");
			double number = Double.parseDouble(str);
			str = df.format(number);
			str = str.replace(".000", EMPTY);
			return str;
		}
	}

	public static String lpad(String str, int len, String sPad) {
		if (str == null) {
			return EMPTY;
		}
		if (str.length() >= len) {
			return str;
		}
		for (int i = str.length(); i < len; i++) {
			str = sPad + str;
		}
		return str;
	}

	public static String rpad(String str, int len, String sPad) {
		if (str == null)
			return EMPTY;
		if (str.length() >= len)
			return str;
		for (int i = str.length(); i < len; i++) {
			str = str + sPad;
		}
		return str;
	}

	public static int toInt(Object src) {
		return toInt(src, 0);
	}
	public static int toInt(Object src, int defVal) {
		if (src == null || isEmpty(src.toString())) {
			return defVal;
		} else {
			return Integer.parseInt(src.toString().trim());
		}
	}

	public static String toHex(String str) {
		String result = EMPTY;
		for (int i = 0; i < str.length(); i++) {
			result += String.format("%02X", (int) str.charAt(i));
		}
		return result;
	}

	public static String toHex0x(String str) {
		String result = EMPTY;

		for (int i = 0; i < str.length(); i++) {
			result += String.format("0x%02X", (int) str.charAt(i));
		}

		return result;
	}

	public static String cut(String str, long limit) {
		int koUnit = 3;
		boolean isAddDot = true;
		return cut(str, limit, koUnit, isAddDot);
	}

	public static String cut(String str, long limit, int koUnit, boolean isAddDot) {
		int strLen = str.length();
		int count = 0;
		int endIdx = 0;
		str = nvl(str);
		if (isAddDot) {
			limit -= 4;
		}
		boolean isOver = false;
		for (int i=0; i<strLen; i++) {
			String ch = str.substring(i, i+1);
			count += ch.getBytes().length >= 2 ? koUnit : 1;

			if (count > limit) {
				isOver = true;
				break;
			} else {
				endIdx = i+1;
			}
		}
		String result = str.substring(0, endIdx) + (isOver && isAddDot ? " ..." : "");
		return result;
	}

	public static String format(String formatStr, Object[] params) {
		String result = null;
		Formatter formatter = new Formatter();
		try {
			result = formatter.format(formatStr, params).toString();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			formatter.close();
		}
		return result;
	}

	public static byte[] decodeBase64(String str) {
		return Base64.decode(str.getBytes());
	}
}

