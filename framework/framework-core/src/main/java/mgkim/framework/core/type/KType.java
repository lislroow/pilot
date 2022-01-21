package mgkim.framework.core.type;

import com.fasterxml.jackson.annotation.JsonValue;

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
			for (TAumthType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum TAuthType {
		NOAUTH("noauth"), BEARER("bearer"), APIKEY("apikey");
		private final String code;
		public String code() {
			return code;
		}
		private TAuthType(String code) {
			this.code = code;
		}
		@Override
		public String toString() {
			return code;
		}
	}
	
	public enum TCryptoType {
		RSA("rsa"), AES("aes");
		private final String code;
		public String code() {
			return code;
		}
		private TCryptoType(String code) {
			this.code = code;
		}
	}
	
	public enum TEncodingType {
		EUCKR("euc-kr"), UTF8("utf-8"), ISO88591("iso-8859-1");
		private final String code;
		public String code() {
			return code;
		}
		private TEncodingType(String code) {
			this.code = code;
		}
	}
	
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
			for (TExecType item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return SYSTEM;
		}
	}
	
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
			for (TJwtType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum TRequestType {
		JSON("json"), FILE("file"), FORM_DATA("form-data");
		private final String code;
		public String code() {
			return code;
		}
		private TRequestType(String code) {
			this.code = code;
		}
		public static TRequestType get(String code) {
			TRequestType[] values = TRequestType.values();
			for (TRequestType item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum TResponseType {
		JSON("json"), FILE("file");
		private final String code;
		public String code() {
			return code;
		}
		private TResponseType(String code) {
			this.code = code;
		}
		public static TResponseType get(String code) {
			TResponseType[] values = TResponseType.values();
			for (TResponseType item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum TSqlType {
		ORIGIN_SQL("origin-sql"),
		PAGING_SQL("paging-sql"),
		COUNT_SQL1("count-sql1"),
		COUNT_SQL2("count-sql2"),
		;
		private final String code;
		public String code() {
			return code;
		}
		private TSqlType(String code) {
			this.code = code;
		}
	}
	
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
			for (TSsStcdType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum TSysType {
		LOC, DEV, STA, PROD;
		public String label() {
			return this.name().toLowerCase();
		}
		public static TSysType get(String val) {
			if (val == null || "".equals(val)) {
				return null;
			}
			val = val.toLowerCase();
			TSysType[] list = TSysType.values();
			for (TSysType item : list) {
				String label = item.label();
				String label_c1 = label.substring(0, 1);
				if (label.equals(val) || label_c1.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum TUserType {
		API("api");
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
			for (TUserType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return TUserType.API;
		}
	}
	
	public enum TUuidType {
		GUID("g", 13), SSID("s", 13), TXID("t", 13), APIKEY("ak", 13),
		FILEID("f", 13), FGRPID("fg", 13);
		private final String prefix;
		private final int length;
		public String prefix() {
			return prefix;
		}
		public int length() {
			return length;
		}
		private TUuidType(String prefix, int length) {
			this.prefix = prefix;
			this.length = length;
		}
	}
	
}
