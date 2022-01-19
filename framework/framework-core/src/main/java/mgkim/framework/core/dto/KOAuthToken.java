package mgkim.framework.core.dto;

public class KOAuthToken {
	
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
		return "KOAuthToken [\n    refreshToken=" + refreshToken + ", \n    accessToken=" + accessToken + "\n]";
	}
}
