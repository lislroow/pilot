package mgkim.online.com.mgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import mgkim.online.cmm.CmmUserSession;
import mgkim.online.com.annotation.KBean;
import mgkim.online.com.exception.KMessage;
import mgkim.online.com.exception.KSysException;
import mgkim.online.com.logging.KLogSys;
import mgkim.online.com.session.KSession;
import mgkim.online.com.session.KToken;
import mgkim.online.com.util.KObjectUtil;
import mgkim.online.com.util.KStringUtil;

@KBean(name = "사용자 session 관리")
public class ComUserSessionMgr implements InitializingBean {

	@Autowired(required = false)
	private CmmUserSession cmmUserSession;

	@Override
	public void afterPropertiesSet() throws Exception {
		if(cmmUserSession == null) {
			if(KObjectUtil.required(CmmUserSession.class)) {
				throw new KSysException(KMessage.E5001, KObjectUtil.name(CmmUserSession.class));
			} else {
				KLogSys.warn(KMessage.get(KMessage.E5003, KObjectUtil.name(ComUserSessionMgr.class), KObjectUtil.name(CmmUserSession.class)));
			}
		}
	}


	public KSession createUserSession(KToken token) throws Exception {
		// `principal` 생성
		KSession session = null;
		{
			session = cmmUserSession.selectUserSession(token);
			if(session == null) {
				throw new KSysException(KMessage.E6108);
			}
		}

		// `authorities` 생성
		final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		{
			List<String> roleList = null;
			roleList = cmmUserSession.selectUserAuthority(token);
			if(roleList != null) {
				roleList = roleList.stream().distinct().collect(Collectors.toList());
				Iterator<String> iter = roleList.iterator();
				while(iter.hasNext()) {
					String item = iter.next();
					if(KStringUtil.isEmpty(item)) {
						KLogSys.warn(KMessage.E6110.text());
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
