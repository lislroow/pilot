package mgkim.framework.cmm.online;

import mgkim.framework.core.annotation.KModule;

@KModule(name = "reloadable-config 관리", required = false)
public interface CmmReloadableConfMng {

	public void check() throws Exception;

	public void resetAll() throws Exception;

	public enum TReloadableConf {
		ComUriAuthorityMgr("ComUriAuthorityMgr");
		private final String code;
		public String code() {
			return code;
		}
		private TReloadableConf(String code) {
			this.code = code;
		}
		public static TReloadableConf get(String code) {
			TReloadableConf[] values = TReloadableConf.values();
			for (TReloadableConf item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return null;
		}
	}

	public enum TConfStcd {
		DONE("00"), RELOADABLE("01"), RESERVED("02"), FAIL("09");
		private final String code;
		public String code() {
			return code;
		}
		private TConfStcd(String code) {
			this.code = code;
		}
		public static TConfStcd get(String code) {
			TConfStcd[] values = TConfStcd.values();
			for (TConfStcd item : values) {
				if (item.code().equals(code)) {
					return item;
				}
			}
			return null;
		}
	}
}
