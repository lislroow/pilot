package mgkim.framework.core.type;

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
