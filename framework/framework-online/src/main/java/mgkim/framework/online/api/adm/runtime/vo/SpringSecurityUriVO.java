package mgkim.framework.online.api.adm.runtime.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpringSecurityUriVO implements Comparable<SpringSecurityUriVO> {

	@Override
	public int compareTo(SpringSecurityUriVO o) {
		if (o.getUri() == null || this.getUri() == null) {
			return 0;
		}
		return this.getUri().compareToIgnoreCase(o.getUri());
	}

	private String uri;
	private String springSecurityRole;

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getSpringSecurityRole() {
		return springSecurityRole;
	}
	public void setSpringSecurityRole(String springSecurityRole) {
		this.springSecurityRole = springSecurityRole;
	}
}
