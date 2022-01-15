package mgkim.service.www.api.cmm.userlogin.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModelProperty;

@JsonSerialize
public class UserLoginReqVO {

	private String userId;
	private String email;
	
	@ApiModelProperty(name = "userId", position = 1, dataType = "string", example = "1000000001")
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
