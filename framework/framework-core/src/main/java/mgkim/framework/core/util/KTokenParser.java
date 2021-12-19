package mgkim.framework.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KTokenParser {

	public static <T> String format(T obj) throws Exception {
		String json = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw e;
		}
		return json;
	}

	public static <T> T parse(Class<T> clazz, String json) throws Exception {
		T tokenClaim = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			tokenClaim = (T)mapper.readValue(json, clazz);
		} catch (Exception e) {
			throw e;
		}
		return tokenClaim;
	}
}
