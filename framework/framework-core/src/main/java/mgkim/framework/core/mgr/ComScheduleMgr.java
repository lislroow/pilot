package mgkim.framework.core.mgr;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.stereo.KTask;
import mgkim.framework.core.util.KObjectUtil;

@KBean(name = "스케줄러 관리")
public class ComScheduleMgr implements InitializingBean, DisposableBean {
	
	private static final Logger log = LoggerFactory.getLogger(ComScheduleMgr.class);
	
	protected boolean enabled = true;
	protected int interval = 2000;
	
	@Autowired(required = false)
	protected List<KScheduler> scheduleList;
	
	public List<KScheduler> getScheduleList() {
		return this.scheduleList;
	}
	
	protected boolean startable;
	
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
					log.info(KMessage.get(KMessage.E5010, KObjectUtil.name(scheduler.getClass())));
				} else {
					log.warn(KMessage.get(KMessage.E5009, KObjectUtil.name(scheduler.getClass())));
				}
			}
		} catch(Exception e) {
			KExceptionHandler.translate(e);
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
					KExceptionHandler.translate(e);
				}
			}
		}
	}

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
				startable = true;
				scheduleList.forEach(item -> {
					boolean managed = KObjectUtil.manage(item.getClass());
					if (managed) {
						// 관리 대상이고 스케줄 활성화일 경우 실행
						item.start();
					}
				});
			}
		} catch(Exception e) {
			KExceptionHandler.translate(e);
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
					scheduleList.forEach(item -> {
						if (!KObjectUtil.manage(item.getClass())) {
							// 관리 대상이 아니면 start/stop 을 하지 않음
							return;
						}
						item.start();
					});
				} catch(Exception e) {
					KExceptionHandler.translate(e);
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
