package mgkim.core.com.util;

import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
//import org.springframework.security.oauth2.common.util.JacksonJsonParser;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.util.ClassUtils;

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

class JsonParserFactory {

	public static JsonParser create() {
		if(ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", null)) {
			return new Jackson2JsonParser();
		}
		/*if(ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper", null)) {
			return new JacksonJsonParser();
		}*/
		throw new IllegalStateException("No Jackson parser found. Please add Jackson to your classpath.");
	}

}
