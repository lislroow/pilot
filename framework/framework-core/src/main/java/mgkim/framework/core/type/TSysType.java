package mgkim.framework.core.type;

public enum TSysType {

	LOC, DEV, STA, PROD;

	public String label() {
		return this.name().toLowerCase();
	}
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
