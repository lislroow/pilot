package mgkim.online.api.adm.runtime.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpringBeansVO implements Comparable<SpringBeansVO> {

	@Override
	public int compareTo(SpringBeansVO o) {
		int result;
		if(o.getApplicationContextId() == null || this.getApplicationContextId() == null) {
			if(o.getBeanClass() == null || this.getBeanClass() == null) {
				result = 0;
				return result;
			}
		}
		if(this.getApplicationContextId().compareToIgnoreCase(o.getApplicationContextId()) == 0) {
			if(o.getBeanClass() == null || this.getBeanClass() == null) {
				result = 0;
				return result;
			} else {
				result = this.getBeanClass().compareToIgnoreCase(o.getBeanClass());
			}
		} else {
			result = this.getApplicationContextId().compareToIgnoreCase(o.getApplicationContextId());
		}
		return result;
	}

	private String applicationContextId;
	private String beanId;
	private String beanClass;
	private String swaggerTags;

	public String getApplicationContextId() {
		return applicationContextId;
	}
	public void setApplicationContextId(String applicationContextId) {
		this.applicationContextId = applicationContextId;
	}
	public String getBeanId() {
		return beanId;
	}
	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}
	public String getBeanClass() {
		return beanClass;
	}
	public void setBeanClass(String beanClass) {
		this.beanClass = beanClass;
	}
	public String getSwaggerTags() {
		return swaggerTags;
	}
	public void setSwaggerTags(String swaggerTags) {
		this.swaggerTags = swaggerTags;
	}
}
