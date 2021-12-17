package mgkim.online.com.dto;

import io.swagger.annotations.ApiModelProperty;

public class KInPageVO {

	private Boolean paging;
	private Integer pageindex;
	private Integer rowunit;
	private Integer pageunit;

	@ApiModelProperty(position = 1, example = "true", dataType = "java.lang.Boolean")
	public Boolean getPaging() {
		if(paging == null) {
			paging = true;
		}
		return paging;
	}
	public void setPaging(Boolean paging) {
		this.paging = paging;
	}

	@ApiModelProperty(position = 2, example = "1", dataType = "java.lang.Integer")
	public Integer getPageindex() {
		return pageindex;
	}
	public void setPageindex(Integer pageindex) {
		this.pageindex = pageindex;
	}

	@ApiModelProperty(position = 3, example = "10", dataType = "java.lang.Integer")
	public Integer getRowunit() {
		return rowunit;
	}
	public void setRowunit(Integer rowunit) {
		this.rowunit = rowunit;
	}

	@ApiModelProperty(position = 4, example = "10", dataType = "java.lang.Integer")
	public Integer getPageunit() {
		return pageunit;
	}
	public void setPageunit(Integer pageunit) {
		this.pageunit = pageunit;
	}
}
