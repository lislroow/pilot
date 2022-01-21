package mgkim.framework.core.type;

public class KType {

	public enum OsType {
		WIN, LINUX;
		public String label() {
			return this.name().toLowerCase();
		}
		public static OsType get(String val) {
			if (val == null || "".equals(val)) {
				return null;
			}
			val = val.toLowerCase().substring(0, 1);
			OsType[] list = OsType.values();
			for (OsType item : list) {
				String label = item.label();
				String label_c1 = label.substring(0, 1);
				if (label_c1.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}

	public enum TApiType {
		API("api"), PUBLIC("public"), UNKNOWN("unknown");
		private final String code;
		public String code() {
			return code;
		}
		private TApiType(String code) {
			this.code = code;
		}
		public static TApiType get(String code) {
			TApiType[] values = TApiType.values();
			for (TApiType item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return null;
		}
		@Override
		public String toString() {
			return code;
		}
	}

}
