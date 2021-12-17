package mgkim.framework.online.api.adm.runtime.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JdbcDatasourceVO implements Comparable<JdbcDatasourceVO> {

	@Override
	public int compareTo(JdbcDatasourceVO o) {
		int result;
		if(o.getKey() == null || this.getKey() == null) {
			result = 0;
			return result;
		} else {
			result = this.getKey().compareToIgnoreCase(o.getKey());
		}
		return result;
	}

	private String key;
	private String value;

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
