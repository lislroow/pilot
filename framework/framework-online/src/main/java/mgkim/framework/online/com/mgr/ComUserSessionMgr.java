package mgkim.framework.online.com.mgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.session.KSession;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.cmm.CmmUserSession;

@KBean(name = "사용자 session 관리")
public class ComUserSessionMgr implements InitializingBean {
	
	private static final Logger log = LoggerFactory.getLogger(ComUserSessionMgr.class);

	@Autowired(required = false)
	private CmmUserSession cmmUserSession;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (cmmUserSession == null) {
			if (KObjectUtil.required(CmmUserSession.class)) {
				throw new KSysException(KMessage.E5001, KObjectUtil.name(CmmUserSession.class));
			} else {
				log.warn(KMessage.get(KMessage.E5003, KObjectUtil.name(ComUserSessionMgr.class), KObjectUtil.name(CmmUserSession.class)));
			}
		}
	}


	public KSession createUserSession(Map<String, Object> claims) throws Exception {
		// `principal` 생성
		KSession session = null;
		{
			session = cmmUserSession.selectUserSession(claims);
			if (session == null) {
				throw new KSysException(KMessage.E6108);
			}
		}

		// `authorities` 생성
		final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		{
			List<String> roleList = null;
			roleList = cmmUserSession.selectUserAuthority(claims);
			if (roleList != null) {
				roleList = roleList.stream().distinct().collect(Collectors.toList());
				Iterator<String> iter = roleList.iterator();
				while (iter.hasNext()) {
					String item = iter.next();
					if (KStringUtil.isEmpty(item)) {
						log.warn(KMessage.E6110.text());
						continue;
					}
					authorities.add(new SimpleGrantedAuthority(item));
				}
			}
		}

		// `authenticationToken`(spring-security-session) 생성
		{
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(session, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		return session;
	}
}
