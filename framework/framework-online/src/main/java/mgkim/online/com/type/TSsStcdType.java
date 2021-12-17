package mgkim.online.com.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TSsStcdType {

	LOGIN("00"), DUP_LOGIN("10"), EXPIRED("20"), LOGOUT("30");

	private final String code;

	@JsonValue
	private final String label;

	public String code() {
		return code;
	}

	public String label() {
		return label;
	}

	private TSsStcdType(String code) {
		this.code = code;
		this.label = this.name().toLowerCase();
	}

	public static TSsStcdType get(String val) {
		TSsStcdType[] list = TSsStcdType.values();
		for(TSsStcdType item : list) {
			if(item.code.equals(val) || item.label.equals(val)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.code;
	}
}
