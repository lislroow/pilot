package mgkim.online.com.dto;

import io.swagger.annotations.ApiModelProperty;

public class KInHeaderVO {

	private String debug;

	@ApiModelProperty(hidden = true)
	public String getDebug() {
		return debug;
	}
	public void setDebug(String debug) {
		this.debug = debug;
	}
}
