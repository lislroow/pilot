package mgkim.online.api.adm.scheduler.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "clazz", "interval", "managed", "enabled", "running", "uptime", "lastStartedTime", "lastStoppedTime", "lastExecutedTime" } )
public class KSchedulerStatusVO {

	private String name;
	private String clazz;
	private int interval;
	private boolean managed;
	private boolean enabled;
	private boolean running;
	private Long uptime;
	private String lastStartedTime;
	private String lastStoppedTime;
	private String lastExecutedTime;

	public static class Builder {
		public Builder() { }

		private String name;
		private String clazz;
		private int interval;
		private boolean managed;
		private boolean enabled;
		private boolean running;
		private Long uptime;
		private String lastStartedTime;
		private String lastStoppedTime;
		private String lastExecutedTime;

		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder clazz(String clazz) {
			this.clazz = clazz;
			return this;
		}
		public Builder interval(int interval) {
			this.interval = interval;
			return this;
		}
		public Builder managed(boolean managed) {
			this.managed = managed;
			return this;
		}
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}
		public Builder running(boolean running) {
			this.running = running;
			return this;
		}
		public Builder uptime(Long uptime) {
			this.uptime = uptime;
			return this;
		}
		public Builder lastStartedTime(String lastStartedTime) {
			this.lastStartedTime = lastStartedTime;
			return this;
		}
		public Builder lastStoppedTime(String lastStoppedTime) {
			this.lastStoppedTime = lastStoppedTime;
			return this;
		}
		public Builder lastExecutedTime(String lastExecutedTime) {
			this.lastExecutedTime = lastExecutedTime;
			return this;
		}

		public KSchedulerStatusVO build() {
			return new KSchedulerStatusVO(name, clazz, interval, managed, enabled, running, uptime, lastStartedTime, lastStoppedTime, lastExecutedTime);
		}
	}

	public KSchedulerStatusVO(String name, String clazz, int interval, boolean managed, boolean enabled,
			boolean running, Long uptime, String lastStartedTime, String lastStoppedTime, String lastExecutedTime) {
		super();
		this.name = name;
		this.clazz = clazz;
		this.interval = interval;
		this.managed = managed;
		this.enabled = enabled;
		this.running = running;
		this.uptime = uptime;
		this.lastStartedTime = lastStartedTime;
		this.lastStoppedTime = lastStoppedTime;
		this.lastExecutedTime = lastExecutedTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public boolean isManaged() {
		return managed;
	}
	public void setManaged(boolean managed) {
		this.managed = managed;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	public Long getUptime() {
		return uptime;
	}
	public void setUptime(Long uptime) {
		this.uptime = uptime;
	}
	public String getLastStartedTime() {
		return lastStartedTime;
	}
	public void setLastStartedTime(String lastStartedTime) {
		this.lastStartedTime = lastStartedTime;
	}
	public String getLastStoppedTime() {
		return lastStoppedTime;
	}
	public void setLastStoppedTime(String lastStoppedTime) {
		this.lastStoppedTime = lastStoppedTime;
	}
	public String getLastExecutedTime() {
		return lastExecutedTime;
	}
	public void setLastExecutedTime(String lastExecutedTime) {
		this.lastExecutedTime = lastExecutedTime;
	}
}
