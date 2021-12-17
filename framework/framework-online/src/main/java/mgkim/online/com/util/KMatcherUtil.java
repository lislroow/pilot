package mgkim.online.com.util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

public class KMatcherUtil {

	public static boolean matchByRegex(HttpServletRequest request, String urlPattern) {
		try {
			if(new RegexRequestMatcher(urlPattern, null).matches(request)) {
				return true;
			}
		} catch(Exception e) {
			return false;
		}
		return false;
	}

	public static boolean matchesByRegex(HttpServletRequest request, List<String> urlPatterns) {
		for(String pattern : urlPatterns) {
			try {
				if(new RegexRequestMatcher(pattern, null).matches(request)) {
					return true;
				}
			} catch(Exception e) {
				return false;
			}
		}
		return false;
	}

	public static boolean matchesByAnt(HttpServletRequest request, List<String> urlPatterns) {
		for(String pattern : urlPatterns) {
			try {
				if(new AntPathRequestMatcher(pattern).matches(request)) {
					return true;
				}
			} catch(Exception e) {
				return false;
			}
		}
		return false;
	}
}
