package mgkim.framework.core.logging;

public interface KLog {
	
	void debug(String label, String msg);
	
	void debug(String label, String format, Object ... msgv);
	
}
