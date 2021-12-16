package mgkim.online.api.adm.runtime.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpringUriVO implements Comparable<SpringUriVO> {

	@Override
	public int compareTo(SpringUriVO o) {
		if(o.getUri() == null || this.getUri() == null) {
			return 0;
		}
		return this.getUri().compareToIgnoreCase(o.getUri());
	}

	private String uri;
	private String className;
	private String swaggerValue;
	private String swaggerHttpMethod;
	private String securityRole;

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}

	public String getSwaggerValue() {
		return swaggerValue;
	}
	public void setSwaggerValue(String swaggerValue) {
		this.swaggerValue = swaggerValue;
	}

	public String getSwaggerHttpMethod() {
		return swaggerHttpMethod;
	}
	public void setSwaggerHttpMethod(String swaggerHttpMethod) {
		this.swaggerHttpMethod = swaggerHttpMethod;
	}

	public String getSecurityRole() {
		return securityRole;
	}
	public void setSecurityRole(String securityRole) {
		this.securityRole = securityRole;
	}


}
