package mgkim.framework.online.api.adm.runtime.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpringUri2VO implements Comparable<SpringUri2VO> {

	@Override
	public int compareTo(SpringUri2VO o) {
		if(o.getUri() == null || this.getUri() == null) {
			return 0;
		}
		return this.getUri().compareToIgnoreCase(o.getUri());
	}

	private String uri;
	private String swaggerValue;

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSwaggerValue() {
		return swaggerValue;
	}
	public void setSwaggerValue(String swaggerValue) {
		this.swaggerValue = swaggerValue;
	}

}
