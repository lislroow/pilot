package mgkim.framework.online.com.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TSiteType {

	WWW("10"), ADM("20"), BAT("30");

	private final String code;

	@JsonValue
	private final String label;

	public String code() {
		return code;
	}

	public String label() {
		return label;
	}

	private TSiteType(String code) {
		this.code = code;
		this.label = this.name().toLowerCase();
	}

	public static TSiteType get(String val) {
		TSiteType[] list = TSiteType.values();
		for(TSiteType item : list) {
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
