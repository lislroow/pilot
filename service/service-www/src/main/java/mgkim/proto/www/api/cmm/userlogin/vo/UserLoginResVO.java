package mgkim.proto.www.api.cmm.userlogin.vo;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mgkim.framework.online.com.session.KToken;

@JsonSerialize
public class UserLoginResVO {

	private OAuth2AccessToken jwt;
	private KToken token;
	private String publicKey;

	public OAuth2AccessToken getJwt() {
		return jwt;
	}
	public void setJwt(OAuth2AccessToken jwt) {
		this.jwt = jwt;
	}
	public KToken getToken() {
		return token;
	}
	public void setToken(KToken token) {
		this.token = token;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

}
