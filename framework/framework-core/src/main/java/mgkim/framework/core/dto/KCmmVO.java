package mgkim.framework.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModelProperty;

@JsonSerialize
public class KCmmVO {

	private String appCd;
	private String ssuserId;
	private String aumthTpcd;
	private String ssid;
	private String txid;

	private Integer _rowcount;
	private Integer _startrow;
	private Integer _endrow;

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String getAppCd() {
		return appCd;
	}
	public void setAppCd(String appCd) {
		this.appCd = appCd;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String getSsuserId() {
		return ssuserId;
	}
	public void setSsuserId(String ssuserId) {
		this.ssuserId = ssuserId;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String getAumthTpcd() {
		return aumthTpcd;
	}
	public void setAumthTpcd(String aumthTpcd) {
		this.aumthTpcd = aumthTpcd;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public Integer get_rowcount() {
		return _rowcount;
	}
	public void set_rowcount(Integer _rowcount) {
		this._rowcount = _rowcount;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public Integer get_startrow() {
		return _startrow;
	}
	public void set_startrow(Integer _startrow) {
		this._startrow = _startrow;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public Integer get_endrow() {
		return _endrow;
	}
	public void set_endrow(Integer _endrow) {
		this._endrow = _endrow;
	}
}
