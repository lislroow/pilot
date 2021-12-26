package mgkim.framework.core.stereo;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.core.util.KObjectUtil;

public abstract class KScheduler implements InitializingBean, DisposableBean {
	
	private static final Logger log = LoggerFactory.getLogger(KScheduler.class);

	public boolean enabled = true;
	protected int interval = 1000;

	// org.springframework.scheduling.concurrent
	protected ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

	// java.util.concurrent
	//ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	protected ScheduledFuture<?> future;
	protected KTask task;

	private Long uptime;
	private String lastStartedTime;
	private String lastStoppedTime;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			// scheduler 초기화
			// org.springframework.scheduling.concurrent
			{
				scheduler.setPoolSize(1);
				scheduler.setThreadNamePrefix(this.getClass().getSimpleName()+"-thread");
				scheduler.initialize();
			}

			init();
			start();
		} catch(Exception e) {
			KExceptionHandler.resolve(e);
		}
	}

	protected abstract void init() throws Exception;

	protected abstract KTask task() throws Exception;

	public void start() {
		if (!enabled) {
			log.warn(KMessage.get(KMessage.E5014, KObjectUtil.name(this.getClass())));
			return;
		}
		
		// scheduler 실행 중인지 확인
		{
			if (isRunning()) {
				log.warn(KMessage.get(KMessage.E5015, KObjectUtil.name(this.getClass())));
				return;
			}
		}
		
		// scheduler 실행
		try {
			task = task();
			interval = KObjectUtil.interval(this.getClass());
			log.warn(KMessage.get(KMessage.E5011, KObjectUtil.name(this.getClass()), interval));
			
			// org.springframework.scheduling.concurrent
			//future = scheduler.scheduleWithFixedDelay(task, interval);
			future = scheduler.scheduleAtFixedRate(task, interval);
			
			// java.util.concurrent
			//future = scheduler.scheduleAtFixedRate(task, 0, interval, TimeUnit.MILLISECONDS);
		} catch(Exception e) {
			KExceptionHandler.resolve(e);
		} finally {
			uptime = System.currentTimeMillis();
			lastStartedTime = KDateUtil.now(KConstant.FMT_YYYY_MM_DD_HH_MM_SS_SSS);
		}
	}
	
	public void stop() {
		if (!enabled) {
			return;
		}
		if (future != null) {
			future.cancel(true);
			log.warn(KMessage.get(KMessage.E5012, KObjectUtil.name(this.getClass())));
		}
		
		scheduler.shutdown();
		
		uptime = null;
		lastStoppedTime = KDateUtil.now(KConstant.FMT_YYYY_MM_DD_HH_MM_SS_SSS);
	}
	
	public boolean isRunning() {
		if (future == null) {
			return false;
		}
		
		// cancelled 혹은 done 인 경우에는 실행 중이지 않은 상태로 `false` 를 반환
		return !(future.isCancelled() || future.isDone());
	}
	
	@Override
	public void destroy() throws Exception {
		if (!enabled) {
			return;
		}
		future.cancel(true);
		scheduler.shutdown();
	}
	
	public String getLastStartedTime() {
		return lastStartedTime;
	}
	
	public Long uptime() {
		if (uptime == null) {
			return null;
		}
		return (System.currentTimeMillis() - uptime) / KConstant.MSEC;
	}
	
	public String getLastStoppedTime() {
		return lastStoppedTime;
	}
	
	public String getLastExecutedTime() {
		if (task == null) {
			return null;
		}
		return task.getLastExecutedTime();
	}
}
