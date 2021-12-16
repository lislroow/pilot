package mgkim.core.com.dto;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "header", "body"} )
public class KInDTO<BODY extends Object> {

	private KInHeaderVO header = new KInHeaderVO();

	@ApiModelProperty(position = 1)
	public KInHeaderVO getHeader() {
		return header;
	}
	public void setHeader(KInHeaderVO header) {
		this.header = header;
	}

	@Valid
	private BODY body;

	@ApiModelProperty(position = 3)
	public BODY getBody() {
		return body;
	}
	public void setBody(BODY body) {
		this.body = body;
	}
}
