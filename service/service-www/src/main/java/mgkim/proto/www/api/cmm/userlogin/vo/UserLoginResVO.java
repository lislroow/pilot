package mgkim.proto.www.api.cmm.userlogin.vo;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mgkim.framework.core.dto.OauthToken;

@JsonSerialize
public class UserLoginResVO {

	private OauthToken jwt;
	private Map<String, Object> claims;
	private String publicKey;

	public OauthToken getJwt() {
		return jwt;
	}
	public void setJwt(OauthToken jwt) {
		this.jwt = jwt;
	}
	public Map<String, Object> getClaims() {
		return claims;
	}
	public void setClaims(Map<String, Object> claims) {
		this.claims = claims;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

}
