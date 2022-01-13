package mgkim.framework.core.type;

public enum TSysType {

	LOC("loc"), DEV("dev"), STAGING("staging"), PROD("prod");

	private final String code;

	public String code() {
		return code;
	}

	private TSysType(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
