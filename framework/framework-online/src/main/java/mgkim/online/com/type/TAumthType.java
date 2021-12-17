package mgkim.online.com.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TAumthType {

	IDLOGIN("01"), EASYLOGIN("02"), NOLOGIN("10"), DEVLOGIN("90");

	private final String code;

	@JsonValue
	private final String label;

	public String code() {
		return code;
	}

	public String label() {
		return label;
	}

	private TAumthType(String code) {
		this.code = code;
		this.label = this.name().toLowerCase();
	}

	public static TAumthType get(String val) {
		TAumthType[] list = TAumthType.values();
		for(TAumthType item : list) {
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
