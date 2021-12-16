package mgkim.online.api.adm.runtime.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JavaEnvVariableVO implements Comparable<JavaEnvVariableVO> {

	@Override
	public int compareTo(JavaEnvVariableVO o) {
		int result;
		if(o.getSourceType() == null || this.getSourceType() == null) {
			if(o.getKey() == null || this.getKey() == null) {
				result = 0;
				return result;
			}
		}
		if(this.getSourceType().compareToIgnoreCase(o.getSourceType()) == 0) {
			if(o.getKey() == null || this.getKey() == null) {
				result = 0;
				return result;
			} else {
				result = this.getKey().compareToIgnoreCase(o.getKey());
			}
		} else {
			result = this.getSourceType().compareToIgnoreCase(o.getSourceType());
		}
		return result;
	}

	private String sourceType;
	private String key;
	private String value;

	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
