package mgkim.framework.online.com.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TUserType {

	API("api"), OPENAPI("openapi"), INTERAPI("interapi"), ORGAPI("orgapi"), TEMPORARY("temporary");

	private final String code;

	@JsonValue
	private final String label;

	public String code() {
		return code;
	}

	public String label() {
		return label;
	}

	private TUserType(String code) {
		this.code = code;
		this.label = code;
	}

	public static TUserType get(String val) {
		TUserType[] list = TUserType.values();
		for(TUserType item : list) {
			if(item.code.equals(val) || item.label.equals(val)) {
				return item;
			}
		}
		return TUserType.API;
	}

	@Override
	public String toString() {
		return code;
	}

}
