package mgkim.framework.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import mgkim.framework.core.session.KSession;

public class KSessionUtil {

	public static KSession getSession() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		if (KSession.class.isInstance(authentication.getPrincipal())) {
			return (KSession) authentication.getPrincipal();
		} else {
			return null;
		}
	}

	public static List<String> getAuthorities() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Collections.emptyList();
		}
		List<String> authorities = new ArrayList<String>();
		Iterator<? extends GrantedAuthority> iter = authentication.getAuthorities().iterator();
		iter.forEachRemaining(item -> {
			authorities.add(item.getAuthority());
		});
		return authorities;
	}
}