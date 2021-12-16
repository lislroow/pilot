package mgkim.core.com.type;

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
		for(TResponseType item : values) {
			if(item.code().equals(code)) {
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
