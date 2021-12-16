package mgkim.core.com.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.core.cmm.CmmReloadableConfMng;
import mgkim.core.com.annotation.KTaskSchedule;
import mgkim.core.com.exception.KExceptionHandler;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.stereo.KScheduler;
import mgkim.core.com.stereo.KTask;
import mgkim.core.com.util.KObjectUtil;

@KTaskSchedule(name = "reloadable-config 관리 스케줄러", interval = 1000, manage = true)
public class ComReloadableConfMngScheduler extends KScheduler {

	@Autowired(required = false)
	private CmmReloadableConfMng cmmReloadableConfMng;

	@Override
	protected void init() throws Exception {
		// enabled 설정
		{
			if(cmmReloadableConfMng == null) {
				enabled = false;
				if(KObjectUtil.required(CmmReloadableConfMng.class)) {
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
