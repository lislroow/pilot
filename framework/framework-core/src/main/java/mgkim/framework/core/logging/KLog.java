package mgkim.framework.core.logging;

public interface KLog {
	
	void print(Object v);
	
	void print(String label, String msg);
	
	void print(String label, String format, Object ... msgv);
	
}
