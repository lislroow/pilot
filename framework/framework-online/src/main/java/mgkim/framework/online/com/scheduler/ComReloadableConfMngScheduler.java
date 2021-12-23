package mgkim.framework.online.com.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KTaskSchedule;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.stereo.KTask;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.online.cmm.CmmReloadableConfMng;

@KTaskSchedule(name = "reloadable-config 관리 스케줄러", interval = 1000, manage = true)
public class ComReloadableConfMngScheduler extends KScheduler {

	@Autowired(required = false)
	private CmmReloadableConfMng cmmReloadableConfMng;

	@Override
	protected void init() throws Exception {
		// enabled 설정
		{
			if (cmmReloadableConfMng == null) {
				enabled = false;
				if (KObjectUtil.required(CmmReloadableConfMng.class)) {
					throw new KSysException(KMessage.E5001, KObjectUtil.name(CmmReloadableConfMng.class));
				} else {
					KLogSys.warn(KMessage.get(KMessage.E5003, KObjectUtil.name(ComReloadableConfMngScheduler.class), KObjectUtil.name(CmmReloadableConfMng.class)));
				}
				return;
			}
			cmmReloadableConfMng.resetAll();
		}
	}

	@Override
	protected KTask task() throws Exception {
		KTask task = new KTask() {
			@Override
			protected void execute(String execId) throws Exception {
				try {
					cmmReloadableConfMng.check();
				} catch(Exception e) {
					KExceptionHandler.resolve(e);
				}
			}
		};
		return task;
	}
}