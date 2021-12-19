package mgkim.framework.online.com.mgr;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.logging.KAnsi;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.stereo.KTask;
import mgkim.framework.core.util.KObjectUtil;

@KBean(name = "스케줄러 관리")
public class ComScheduleMgr implements InitializingBean, DisposableBean {

	protected boolean enabled = true;
	protected int interval = 2000;

	@Autowired(required = false)
	public List<KScheduler> scheduleList;

	public boolean startable;

	// org.springframework.scheduling.concurrent
	protected ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

	// java.util.concurrent
	//ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	protected ScheduledFuture<?> future;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			if (scheduleList == null) {
				return;
			}
			for (int i=0; i<scheduleList.size(); i++) {
				KScheduler scheduler = scheduleList.get(i);
				if (KObjectUtil.manage(scheduler.getClass())) {
					KLogSys.info(KMessage.get(KMessage.E5010, KAnsi.boldCyan(KObjectUtil.name(scheduler.getClass()))));
				} else {
					KLogSys.warn(KMessage.get(KMessage.E5009, KAnsi.boldRed(KObjectUtil.name(scheduler.getClass()))));
				}
			}
		} catch(Exception e) {
			KExceptionHandler.resolve(e);
		}
	}

	@EventListener
	public void contextInit(ContextRefreshedEvent event) {
		// 시작가능 여부
		boolean startable = false;
		{
			ApplicationContext ctx = event.getApplicationContext();
			if (ctx.getParent() != null) { // dispatcher-servlet context 초기화가 완료되었을 경우 실행되는 코드 블럭입니다.
				startable = true;
			}
		}

		// task 시작
		{
			if (startable) {
				try {
					start();
				} catch(Exception e) {
					KExceptionHandler.resolve(e);
				}
			}
		}
	}

	/*@EventListener
	public void contextDestroy(ContextClosedEvent event) {
		try {
			destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	protected void start() {
		try {
			// scheduler 초기화
			// org.springframework.scheduling.concurrent
			{
				scheduler.setPoolSize(1);
				scheduler.setThreadNamePrefix(this.getClass().getSimpleName()+"-thread");
				scheduler.initialize();
			}


			// scheduler 실행
			{
				KTask task = task();

				// org.springframework.scheduling.concurrent
				future = scheduler.scheduleWithFixedDelay(task, interval);

				// java.util.concurrent
				//scheduler.scheduleAtFixedRate(task, 0, interval, TimeUnit.MILLISECONDS);
			}

			// 관리 대상 scheduler 실행
			{
				startable = KConfig.SCHEDULE_ENABLE;
				scheduleList.forEach(item -> {
					boolean managed = KObjectUtil.manage(item.getClass());
					if (!managed && item.enabled) {
						// 관리 대상이 아니고 활성화일 경우 실행
						item.start();
					} else if (managed && KConfig.SCHEDULE_ENABLE) {
						// 관리 대상이고 스케줄 활성화일 경우 실행
						item.start();
					}
				});
			}
		} catch(Exception e) {
			KExceptionHandler.resolve(e);
		}
	}

	protected KTask task() throws Exception {
		KTask task = new KTask() {
			@Override
			protected void execute(String execId) throws Exception {
				try {
					if (scheduleList == null) {
						return;
					}
					KConfig.refreshable();
					if (startable != KConfig.SCHEDULE_ENABLE) {
						scheduleList.forEach(item -> {
							if (!KObjectUtil.manage(item.getClass())) {
								// 관리 대상이 아니면 start/stop 을 하지 않음
								return;
							}
							if (KConfig.SCHEDULE_ENABLE) {
								item.start();
							} else {
								item.stop();
							}
						});
						startable = KConfig.SCHEDULE_ENABLE;
					}
				} catch(Exception e) {
					KExceptionHandler.resolve(e);
				}
			}
		};
		return task;
	}

	@Override
	public void destroy() throws Exception {
		if (!enabled && future == null) {
			return;
		}
		future.cancel(true);
		scheduler.shutdown();
	}
}
