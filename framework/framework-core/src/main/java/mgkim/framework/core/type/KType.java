package mgkim.framework.core.type;

import com.fasterxml.jackson.annotation.JsonValue;

public class KType {

	public enum OSType {
		WIN, LINUX;
		public String label() {
			return this.name().toLowerCase();
		}
		public static OSType get(String val) {
			if (val == null || "".equals(val)) {
				return null;
			}
			val = val.toLowerCase().substring(0, 1);
			OSType[] list = OSType.values();
			for (OSType item : list) {
				String label = item.label();
				String label_c1 = label.substring(0, 1);
				if (label_c1.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}

	public enum ApiType {
		API("api"), PUBLIC("public"), UNKNOWN("unknown");
		private final String code;
		public String code() {
			return code;
		}
		private ApiType(String code) {
			this.code = code;
		}
		public static ApiType get(String code) {
			ApiType[] values = ApiType.values();
			for (ApiType item : values) {
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
	
	public enum AumthType {
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
		private AumthType(String code) {
			this.code = code;
			this.label = this.name().toLowerCase();
		}
		public static AumthType get(String val) {
			AumthType[] list = AumthType.values();
			for (AumthType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum AuthType {
		NOAUTH("noauth"), BEARER("bearer"), APIKEY("apikey");
		private final String code;
		public String code() {
			return code;
		}
		private AuthType(String code) {
			this.code = code;
		}
		@Override
		public String toString() {
			return code;
		}
	}
	
	public enum CryptoType {
		RSA("rsa"), AES("aes");
		private final String code;
		public String code() {
			return code;
		}
		private CryptoType(String code) {
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
	
	public enum ExecType {
		REQUEST("request"), SCHEDULE("schedule"), SYSTEM("system");
		private final String code;
		public String code() {
			return code;
		}
		private ExecType(String code) {
			this.code = code;
		}
		public static ExecType get(String code) {
			ExecType[] values = ExecType.values();
			for (ExecType item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return SYSTEM;
		}
	}
	
	public enum JwtType {
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
		private JwtType(String code) {
			this.code = code;
			this.label = code;
		}
		public static JwtType get(String val) {
			JwtType[] list = JwtType.values();
			for (JwtType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum ReqType {
		JSON("json"), FILE("file"), FORM_DATA("form-data");
		private final String code;
		public String code() {
			return code;
		}
		private ReqType(String code) {
			this.code = code;
		}
		public static ReqType get(String code) {
			ReqType[] values = ReqType.values();
			for (ReqType item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum RespType {
		JSON("json"), FILE("file");
		private final String code;
		public String code() {
			return code;
		}
		private RespType(String code) {
			this.code = code;
		}
		public static RespType get(String code) {
			RespType[] values = RespType.values();
			for (RespType item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum SqlType {
		ORIGIN_SQL("origin-sql"),
		PAGING_SQL("paging-sql"),
		COUNT_SQL1("count-sql1"),
		COUNT_SQL2("count-sql2"),
		;
		private final String code;
		public String code() {
			return code;
		}
		private SqlType(String code) {
			this.code = code;
		}
	}
	
	public enum SsStcdType {
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
		private SsStcdType(String code) {
			this.code = code;
			this.label = this.name().toLowerCase();
		}
		public static SsStcdType get(String val) {
			SsStcdType[] list = SsStcdType.values();
			for (SsStcdType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum SysType {
		LOC, DEV, STA, PROD;
		public String label() {
			return this.name().toLowerCase();
		}
		public static SysType get(String val) {
			if (val == null || "".equals(val)) {
				return null;
			}
			val = val.toLowerCase();
			SysType[] list = SysType.values();
			for (SysType item : list) {
				String label = item.label();
				String label_c1 = label.substring(0, 1);
				if (label.equals(val) || label_c1.equals(val)) {
					return item;
				}
			}
			return null;
		}
	}
	
	public enum UserType {
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
		private UserType(String code) {
			this.code = code;
			this.label = code;
		}
		public static UserType get(String val) {
			UserType[] list = UserType.values();
			for (UserType item : list) {
				if (item.code.equals(val) || item.label.equals(val)) {
					return item;
				}
			}
			return UserType.API;
		}
	}
	
	public enum UuidType {
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
		private UuidType(String prefix, int length) {
			this.prefix = prefix;
			this.length = length;
		}
	}
	
}
