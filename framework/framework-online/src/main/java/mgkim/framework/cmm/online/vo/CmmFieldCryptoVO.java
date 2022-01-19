package mgkim.framework.cmm.online.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import mgkim.framework.core.dto.KCmmVO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CmmFieldCryptoVO extends KCmmVO {

	private String appCd;
	private String privateKey;
	private String publicKey;
	private String symKey;

	public String getAppCd() {
		return appCd;
	}
	public void setAppCd(String appCd) {
		this.appCd = appCd;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getSymKey() {
		return symKey;
	}
	public void setSymKey(String symKey) {
		this.symKey = symKey;
	}
}
