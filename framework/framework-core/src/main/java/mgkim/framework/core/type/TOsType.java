package mgkim.framework.core.type;

public enum TOsType {
	WIN, LINUX;
	
	public String label() {
		return this.name().toLowerCase();
	}
	
	public static TOsType get(String val) {
		if (val == null || "".equals(val)) {
			return null;
		}
		val = val.toLowerCase().substring(0, 1);
		TOsType[] list = TOsType.values();
		for (TOsType item : list) {
			String label = item.label();
			String label_c1 = label.substring(0, 1);
			if (label_c1.equals(val)) {
				return item;
			}
		}
		return null;
	}
}
