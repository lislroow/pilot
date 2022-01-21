package mgkim.service.lib.type;

import com.fasterxml.jackson.annotation.JsonValue;

public class CmmType {

	public enum AppType {
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
		private AppType(String code) {
			this.code = code;
			this.label = this.name().toLowerCase();
		}
		public static AppType get(String val) {
			AppType[] list = AppType.values();
			for (AppType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
}
