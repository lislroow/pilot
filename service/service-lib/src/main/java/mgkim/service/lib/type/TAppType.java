package mgkim.service.lib.type;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TAppType {
	
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
	
	private TAppType(String code) {
		this.code = code;
		this.label = this.name().toLowerCase();
	}
	
	public static TAppType get(String val) {
		TAppType[] list = TAppType.values();
		for (TAppType item : list) {
			if (item.code.equals(val) || item.label.equals(val)) {
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
