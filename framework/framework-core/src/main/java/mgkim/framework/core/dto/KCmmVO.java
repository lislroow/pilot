package mgkim.framework.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModelProperty;

@JsonSerialize
public class KCmmVO {

	private String _appCd;
	private String _ssuserId;
	private String _aumthTpcd;
	private String _ssid;
	private String _txid;

	private Integer _rowcount;
	private Integer _startrow;
	private Integer _endrow;

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String get_appCd() {
		return this._appCd;
	}
	public void set_appCd(String _appCd) {
		this._appCd = _appCd;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String get_ssuserId() {
		return this._ssuserId;
	}
	public void set_ssuserId(String _ssuserId) {
		this._ssuserId = _ssuserId;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String get_aumthTpcd() {
		return this._aumthTpcd;
	}
	public void set_aumthTpcd(String _aumthTpcd) {
		this._aumthTpcd = _aumthTpcd;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String get_ssid() {
		return this._ssid;
	}
	public void set_ssid(String _ssid) {
		this._ssid = _ssid;
	}

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	public String get_txid() {
		return this._txid;
	}
	public void set_txid(String _txid) {
		this._txid = _txid;
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
