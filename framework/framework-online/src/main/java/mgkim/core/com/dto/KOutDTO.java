package mgkim.core.com.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "header", "body"} )
public class KOutDTO<BODY extends Object> {

	private Map<String, Object> header = null;

	@ApiModelProperty(position = 1)
	public Map<String, Object> getHeader() {
		return header;
	}
	public void setHeader(Map<String, Object> header) {
		this.header = header;
	}

	private BODY body;

	@ApiModelProperty(position = 3)
	public BODY getBody() {
		return body;
	}
	public void setBody(BODY body) {
		this.body = body;
	}
}
