package mgkim.framework.core.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "header", "page", "body"} )
public class KInPageDTO<BODY extends Object> extends KInDTO<BODY> {

	private KInPageVO page;

	@ApiModelProperty(position = 2)
	public KInPageVO getPage() {
		return page;
	}
	public void setPage(KInPageVO page) {
		this.page = page;
	}
}
