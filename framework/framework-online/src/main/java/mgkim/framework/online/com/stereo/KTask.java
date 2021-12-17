package mgkim.framework.online.com.stereo;

import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.exception.KException;
import mgkim.framework.online.com.exception.KExceptionHandler;
import mgkim.framework.online.com.type.TUuidType;
import mgkim.framework.online.com.util.KDateUtil;
import mgkim.framework.online.com.util.KStringUtil;

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
