package mgkim.framework.core.type;

public enum TUuidType {

	GUID("G-", 13), SSID("S-", 13), TXID("T-", 13), APIKEY("AK-", 13),
	FILEID("F", 13), FGRPID("FG", 13);

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
