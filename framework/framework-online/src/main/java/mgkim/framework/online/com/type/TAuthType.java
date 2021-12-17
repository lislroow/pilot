package mgkim.framework.online.com.type;

public enum TAuthType {

	NOAUTH("noauth"), BEARER("bearer"), APIKEY("apikey");

	private final String code;

	public String code() {
		return code;
	}

	private TAuthType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}
}
