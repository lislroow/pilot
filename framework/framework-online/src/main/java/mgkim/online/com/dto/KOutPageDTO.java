package mgkim.online.com.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "header", "page", "body"} )
public class KOutPageDTO<BODY extends Object> extends KOutDTO<BODY> {

	private KOutPageVO page;

	@ApiModelProperty(position = 2)
	public KOutPageVO getPage() {
		return page;
	}
	public void setPage(KOutPageVO page) {
		this.page = page;
	}
}
