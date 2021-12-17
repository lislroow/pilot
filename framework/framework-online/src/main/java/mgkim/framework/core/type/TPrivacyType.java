package mgkim.framework.core.type;

public enum TPrivacyType {

	SQL("10"), URI("20");

	private String code;
	public String code() {
		return code;
	}
	private TPrivacyType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}
}
