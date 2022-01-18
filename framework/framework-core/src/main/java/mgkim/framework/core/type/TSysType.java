package mgkim.framework.core.type;

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
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
