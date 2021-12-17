package mgkim.framework.core.type;

public enum TExecType {

	REQUEST("request"), SCHEDULE("schedule"), SYSTEM("system");

	private final String code;

	public String code() {
		return code;
	}

	private TExecType(String code) {
		this.code = code;
	}

	public static TExecType get(String code) {
		TExecType[] values = TExecType.values();
		for(TExecType item : values) {
			if(item.code().equals(code)) {
				return item;
			}
		}
		return SYSTEM;
	}

}
