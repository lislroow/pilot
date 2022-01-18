package mgkim.framework.core.exception;

import java.text.MessageFormat;

public class KException extends Exception {

	private static final long serialVersionUID = 1L;

	private String id;
	private String text;
	private Object[] args;
	private Object additional;

	// `exception` 미포함
	public KException(String id) {
		super(id);
		this.id = id;
	}
	public KException(String id, Object ... args) {
		super(id);
		this.id = id;
		this.args = args;
	}
	public KException(String id, Object additional, Object ... args) {
		super(id);
		this.id = id;
		this.args = args;
		this.additional = additional;
	}
	// -- `exception` 미포함

	// `exception` 포함
	public KException(String id, Exception ex) {
		super(id, ex);
		this.id = id;
	}
	public KException(String id, Exception ex, Object ... args) {
		super(id, ex);
		this.id = id;
		this.args = args;
	}
	public KException(String id, Exception ex, Object additional, Object ... args) {
		super(id, ex);
		this.id = id;
		this.args = args;
		this.additional = additional;
	}
	// -- `exception` 포함

	// `exception` KExceptionMapper 클래스에서 생성
	KException(KMessage object) {
		super(String.format("[%s] %s", object.name(), object.text()));
		this.id = object.name();
		this.text = object.text();
	}
	KException(KMessage object, Object ... args) {
		super(String.format("[%s] %s", object.name(), MessageFormat.format(object.text(), args)));
		this.id = object.name();
		this.text = MessageFormat.format(object.text(), args);
	}
	KException(KMessage object, Exception ex) {
		super(String.format("[%s] %s", object.name(), object.text()), ex);
		this.id = object.name();
		this.text = object.text();
	}
	KException(KMessage object, Exception ex, Object ... args) {
		super(String.format("[%s] %s", object.name(), MessageFormat.format(object.text(), args)), ex);
		this.id = object.name();
		this.text = MessageFormat.format(object.text(), args);
	}
	// -- `exception` KExceptionMapper 클래스에서 생성

	public Object cause() {
		if (this.getCause() != null) {
			return this.getCause();
		}
		return this;
	}
	public String getId() {
		return id;
	}
	public String getText() {
		return text != null ? text.replaceAll("\\R", "") : text;
	}
	public Object[] getArgs() {
		return args;
	}
	public Object getAdditional() {
		return additional;
	}
}
