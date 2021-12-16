package mgkim.core.com.stereo;

import mgkim.core.com.env.KConstant;
import mgkim.core.com.env.KContext;
import mgkim.core.com.exception.KException;
import mgkim.core.com.exception.KExceptionHandler;
import mgkim.core.com.type.TUuidType;
import mgkim.core.com.util.KDateUtil;
import mgkim.core.com.util.KStringUtil;

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
