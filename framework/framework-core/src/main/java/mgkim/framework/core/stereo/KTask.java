package mgkim.framework.core.stereo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.KType.UuidType;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.core.util.KStringUtil;

public abstract class KTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(KTask.class);
	
	private String lastExecutedTime;

	@Override
	public void run() {
		String execId = KStringUtil.createUuid(true, UuidType.TXID);
		try {
			KContext.initSchedule();
			this.execute(execId);
		} catch(Exception e) {
			KException exception = KExceptionHandler.translate(e);
			log.error(KLogMarker.ERROR, "{} {}", exception.getId(), exception.getText(), e);
		} finally {
			lastExecutedTime = KDateUtil.now(KConstant.FMT_YYYY_MM_DD_HH_MM_SS_SSS);
		}
	}

	protected abstract void execute(String execId) throws Exception;

	public String getLastExecutedTime() {
		return this.lastExecutedTime;
	}
}
