package mgkim.framework.online.com.dto;

import io.swagger.annotations.ApiModelProperty;

public class KOutHeaderVO {

	private String txid;

	@ApiModelProperty(position = 1)
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}
}
