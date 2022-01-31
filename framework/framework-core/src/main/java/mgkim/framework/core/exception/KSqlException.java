package mgkim.framework.core.exception;

import mgkim.framework.core.util.KExceptionUtil;
import mgkim.framework.core.util.KStringUtil;

public class KSqlException extends KException {

	private static final long serialVersionUID = 1L;

	private KMessage object;
	private String sqlId;
	private String sqlText;
	private String sqlFile;

	// `exception` 미포함
	public KSqlException(KMessage object) {
		super(object);
		this.object = object;
	}
	public KSqlException(KMessage object, String sqlId) {
		super(object, new Object[] {sqlId});
		this.object = object;
		this.sqlId = sqlId;
	}
	public KSqlException(KMessage object, String sqlId, String sqlText, String sqlFile) {
		super(object, new Object[] {sqlId});
		this.object = object;
		this.sqlId = sqlId;
		this.sqlText = sqlText;
		this.sqlFile = sqlFile;
	}
	// -- `exception` 미포함

	// `exception` 포함
	public KSqlException(KMessage object, Exception ex, String sqlId) {
		super(object, ex, new Object[] {ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()});
		this.object = object;
		this.sqlId = sqlId;
	}
	public KSqlException(KMessage object, Exception ex, String sqlId, String sqlText, String sqlFile) {
		super(object, ex, new Object[] {ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()});
		this.object = object;
		this.sqlId = sqlId;
		this.sqlText = sqlText;
		this.sqlFile = sqlFile;
	}
	//public KSqlException(KMessage object, Exception ex) {
	//	super(object, ex);
	//	this.object = object;
	//}
	//public KSqlException(KMessage object, Exception ex, Object ... args) {
	//	super(object, ex, args);
	//	this.object = object;
	//}
	// -- `exception` 포함

	public KMessage getObject() {
		return object;
	}
	public Object cause() {
		this.sqlFile = KStringUtil.nvl(this.sqlFile, "* 확인 불가* ");
		this.sqlId = KStringUtil.nvl(this.sqlId, "* 확인 불가* ");
		// sqlText 를 생성하기 전에 발생된 오류는 exception 객체로부터 메시지를 출력함
		this.sqlText = KStringUtil.nvl(this.sqlText, KExceptionUtil.getTrace(super.getCause(), true));
		return String.format("[ *** sql-error *** ] `%s`\r`%s`\r%s", this.sqlFile, this.sqlId, this.sqlText);  // `KSqlException`에서는 `Throwable` 객체가 아닌 `sqlText`을 반환
	}
	public String getSqlId() {
		return sqlId;
	}
	public String getSqlText() {
		return sqlText;
	}
}