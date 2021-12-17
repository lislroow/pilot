package mgkim.framework.core.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TJwtType {

	REFRESH_TOKEN("refreshToken"), ACCESS_TOKEN("accessToken");

	private final String code;

	@JsonValue
	private final String label;

	public String code() {
		return code;
	}

	public String label() {
		return label;
	}

	private TJwtType(String code) {
		this.code = code;
		this.label = code;
	}

	public static TJwtType get(String val) {
		TJwtType[] list = TJwtType.values();
		for(TJwtType item : list) {
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
