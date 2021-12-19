package mgkim.framework.online.com.init;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import mgkim.framework.core.env.KConfig;
import mgkim.framework.online.com.filter.KFilterAccessDeny;
import mgkim.framework.online.com.filter.KFilterAccessLog;
import mgkim.framework.online.com.filter.KFilterCreateUserSession;
import mgkim.framework.online.com.filter.KFilterDecodeToken;
import mgkim.framework.online.com.filter.KFilterPreflight;
import mgkim.framework.online.com.filter.KFilterResolveUri;
import mgkim.framework.online.com.filter.KFilterSecurity;
import mgkim.framework.online.com.filter.KFilterSpringAuthorizeHandle;
import mgkim.framework.online.com.filter.KFilterUpdateSessionExpire;
import mgkim.framework.online.com.filter.KFilterVerifySession;
import mgkim.framework.online.com.filter.KFilterVerifyToken;
import mgkim.framework.online.com.mgr.ComUriAuthorityMgr;

@Configuration
@EnableWebSecurity
public class KInitSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	protected DataSource dataSource;

	static final boolean REJECT_PUBLIC_INVOCATIONS = false;

	@Bean("springSecurityFilterChain")
	public FilterChainProxy filterChainProxy() throws Exception {
		ApplicationContext ctx = getApplicationContext();

		List<SecurityFilterChain> filterChains = new ArrayList<SecurityFilterChain>();

		// ** denyapi
		for (String uriPattern : KConfig.SECURITY_FILTER_DENYAPI) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterAccessDeny.class)
				}));
		}

		// ** hiddenapi
		for (String uriPattern : KConfig.FILTER_HIDDENAPI) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterPreflight.class)
				}));
		}

		// ** public
		for (String uriPattern : KConfig.FILTER_PUBLIC) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterResolveUri.class)
					, ctx.getBean(KFilterPreflight.class)
					, ctx.getBean(KFilterSecurity.class)
					, ctx.getBean(KFilterAccessLog.class)
					, ctx.getBean(KFilterSpringAuthorizeHandle.class)
				}));
		}

		// ** interapi
		for (String uriPattern : KConfig.FILTER_INTERAPI) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterResolveUri.class)
					, ctx.getBean(KFilterPreflight.class)
					, ctx.getBean(KFilterSecurity.class)
					, ctx.getBean(KFilterAccessLog.class)
					, ctx.getBean(SecurityContextPersistenceFilter.class)
					, ctx.getBean(KFilterDecodeToken.class)
					, ctx.getBean(KFilterCreateUserSession.class)
					, ctx.getBean(KFilterSpringAuthorizeHandle.class)
				}));
		}

		// ** orgapi
		for (String uriPattern : KConfig.FILTER_ORGAPI) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterResolveUri.class)
					, ctx.getBean(KFilterPreflight.class)
					, ctx.getBean(KFilterSecurity.class)
					, ctx.getBean(KFilterAccessLog.class)
					, ctx.getBean(SecurityContextPersistenceFilter.class)
					, ctx.getBean(KFilterDecodeToken.class)
					, ctx.getBean(KFilterCreateUserSession.class)
					, ctx.getBean(KFilterSpringAuthorizeHandle.class)
				}));
		}

		// ** openapi
		for (String uriPattern : KConfig.FILTER_OPENAPI) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterResolveUri.class)
					, ctx.getBean(KFilterPreflight.class)
					, ctx.getBean(KFilterSecurity.class)
					, ctx.getBean(KFilterAccessLog.class)
					, ctx.getBean(SecurityContextPersistenceFilter.class)
					, ctx.getBean(KFilterDecodeToken.class)
					, ctx.getBean(KFilterCreateUserSession.class)
					, ctx.getBean(KFilterSpringAuthorizeHandle.class)
				}));
		}

		// ** logout
		for (String uriPattern : KConfig.FILTER_API3) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterResolveUri.class)
					, ctx.getBean(KFilterPreflight.class)
					, ctx.getBean(KFilterSecurity.class)
					, ctx.getBean(KFilterAccessLog.class)
					, ctx.getBean(KFilterDecodeToken.class)
					, ctx.getBean(KFilterCreateUserSession.class)
				}));
		}

		// ** file upload & download
		for (String uriPattern : KConfig.FILTER_API2) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterResolveUri.class)
					, ctx.getBean(KFilterPreflight.class)
					, ctx.getBean(KFilterSecurity.class)
					, ctx.getBean(KFilterAccessLog.class)
					, ctx.getBean(SecurityContextPersistenceFilter.class)
					, ctx.getBean(KFilterDecodeToken.class)
					, ctx.getBean(KFilterVerifySession.class)
					, ctx.getBean(KFilterCreateUserSession.class)
					, ctx.getBean(KFilterUpdateSessionExpire.class)
					, ctx.getBean(KFilterSpringAuthorizeHandle.class)
					}));
		}

		// ** api
		for (String uriPattern : KConfig.FILTER_API) {
			filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(uriPattern), new Filter[] {
					  ctx.getBean(KFilterResolveUri.class)
					, ctx.getBean(KFilterPreflight.class)
					, ctx.getBean(KFilterSecurity.class)
					, ctx.getBean(KFilterAccessLog.class)
					, ctx.getBean(SecurityContextPersistenceFilter.class)
					, ctx.getBean(KFilterDecodeToken.class)
					, ctx.getBean(KFilterVerifySession.class)
					, ctx.getBean(KFilterVerifyToken.class)
					, ctx.getBean(KFilterCreateUserSession.class)
					, ctx.getBean(KFilterUpdateSessionExpire.class)
					, ctx.getBean(KFilterSpringAuthorizeHandle.class)
					}));
		}

		FilterChainProxy bean = new FilterChainProxy(filterChains);
		return bean;
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}

	@Bean
	public AffirmativeBased affirmativeBased() {
		RoleVoter roleVoter = new RoleVoter();
		roleVoter.setRolePrefix("");
		AuthenticatedVoter authenticatedVoter = new AuthenticatedVoter();
		List<AccessDecisionVoter<? extends Object>> decisionVoters = new ArrayList<AccessDecisionVoter<? extends Object>>();
		decisionVoters.add(roleVoter);
		decisionVoters.add(authenticatedVoter);
		AffirmativeBased bean = new AffirmativeBased(decisionVoters);
		return bean;
	}

	@Bean
	public FilterSecurityInterceptor filterSecurityInterceptor(ComUriAuthorityMgr comUriAuthorityMgr) throws Exception {
		FilterSecurityInterceptor bean = new FilterSecurityInterceptor();
		bean.setAuthenticationManager(this.authenticationManager());
		bean.setAccessDecisionManager(affirmativeBased());
		bean.setSecurityMetadataSource(comUriAuthorityMgr);
		// [중요] api에 권한이 등록되지 않은 상태에서 호출 가능 여부를 설정합니다.
		//   true: 호출이 불가능함
		//   false: 호출이 가능함
		bean.setRejectPublicInvocations(REJECT_PUBLIC_INVOCATIONS);
		return bean;
	}

	@Bean
	public SecurityContextPersistenceFilter securityContextPersistenceFilter() {
		SecurityContextPersistenceFilter bean = new SecurityContextPersistenceFilter();
		return bean;
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
		web.ignoring().antMatchers("/public/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
	}

}