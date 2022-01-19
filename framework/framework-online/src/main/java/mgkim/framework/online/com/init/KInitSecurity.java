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

import mgkim.framework.core.env.KConstant;
import mgkim.framework.online.com.filter.KFilterApi;
import mgkim.framework.online.com.filter.KFilterPublic;
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
		
		// 1) public
		Filter[] filterPublic = new Filter[] {ctx.getBean(KFilterPublic.class)};
		KConstant.PUBLIC_URI.stream().forEach(item -> filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(item), filterPublic)));
		
		// 2) api
		Filter[] filterApi = new Filter[] { ctx.getBean(SecurityContextPersistenceFilter.class), ctx.getBean(KFilterApi.class)};
		filterChains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(KConstant.API_URI), filterApi));
		
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
		//web.ignoring().antMatchers("/public/**");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
	}

}