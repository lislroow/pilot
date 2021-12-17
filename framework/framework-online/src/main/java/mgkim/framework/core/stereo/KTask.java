package mgkim.framework.core.stereo;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.type.TUuidType;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.core.util.KStringUtil;

public abstract class KTask implements Runnable {

	private String lastExecutedTime;

	@Override
	public void run() {
		String execId = KStringUtil.createUuid(true, TUuidType.TXID);
		try {
			KContext.initSchedule();
			this.execute(execId);
		} catch(Exception e) {
			KException exception = KExceptionHandler.resolve(e);
			KExceptionHandler.print(exception);
		} finally {
			lastExecutedTime = KDateUtil.now(KConstant.FMT_YYYY_MM_DD_HH_MM_SS_SSS);
		}
	}

	protected abstract void execute(String execId) throws Exception;

	public String getLastExecutedTime() {
		return this.lastExecutedTime;
	}
}
