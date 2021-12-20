package mgkim.service.www.api.cmm.userlogin.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class UserLoginReqVO {

	private String userId;
	private String email;

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
