package mgkim.framework.online.com.exception;

public class KSysException extends KException {

	private static final long serialVersionUID = 1L;

	private KMessage object;

	// `exception` 미포함
	public KSysException(KMessage object) {
		super(object);
		this.object = object;
	}
	public KSysException(KMessage object, Object ... args) {
		super(object, args);
		this.object = object;
	}
	// -- `exception` 미포함

	// `exception` 포함
	public KSysException(KMessage object, Exception ex) {
		super(object, ex);
		this.object = object;
	}
	public KSysException(KMessage object, Exception ex, Object ... args) {
		super(object, ex, args);
		this.object = object;
	}
	// -- `exception` 포함

	public KMessage getObject() {
		return object;
	}
}
