package mgkim.framework.core.dto;

public class OauthToken {
	
	private String refreshToken;
	
	private String accessToken;

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public String toString() {
		return "OauthToken [\n    refreshToken=" + refreshToken + ", \n    accessToken=" + accessToken + "\n]";
	}
}
