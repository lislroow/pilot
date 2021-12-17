package mgkim.framework.online.com.logging;

import static mgkim.framework.online.com.env.KConstant.CALLER;
import static mgkim.framework.online.com.env.KConstant.GUID;
import static mgkim.framework.online.com.env.KConstant.IP;
import static mgkim.framework.online.com.env.KConstant.TXID;
import static mgkim.framework.online.com.env.KConstant.URI;
import static mgkim.framework.online.com.env.KConstant.USER_ID;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;
import mgkim.framework.core.type.TApiType;
import mgkim.framework.core.type.TAuthType;
import mgkim.framework.core.type.TExecType;
import mgkim.framework.core.type.TSysType;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.env.KConfig;
import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KProfile;
import mgkim.framework.online.com.env.KContext.AttrKey;

public class KLogLayout extends LayoutBase<ILoggingEvent> {

	static SimpleDateFormat dateFormat;
	static SimpleDateFormat dateFormatSimple;
	public static String LINE;

	public static final String LEVEL_INFO  = "["+KAnsi.blue(String.format("%-5s", "INFO"))+"]";
	public static final String LEVEL_WARN  = "["+KAnsi.red(String.format("%-5s", "WARN"))+"]";
	public static final String LEVEL_ERROR = "["+KAnsi.boldRed(String.format("%-5s", "ERROR"))+"]";
	public static final String PROFILES;

	static {
		if(KProfile.SYS == null) {
			KProfile.SYS = TSysType.LOC;
		}
		LINE = System.getProperty("line.separator");
		dateFormat = new SimpleDateFormat(KConstant.FMT_YYYY_MM_DD_HH_MM_SS_SSS);
		switch (KProfile.SYS) {
		case LOC:
			dateFormatSimple = new SimpleDateFormat(KConstant.FMT_HH_MM_SS_SSS);
			break;
		case DEV:
		case TEST:
		case PROD:
			dateFormatSimple = new SimpleDateFormat(KConstant.FMT_YYYY_MM_DD_HH_MM_SS_SSS);
			break;
		default:
			break;
		}
		MDC.put("profiles", KAnsi.cyan(KProfile.PROFILES_STR));
		PROFILES = String.format("[%s]", KAnsi.cyan(KProfile.PROFILES_STR));
	}

	@Override
	public String doLayout(ILoggingEvent event) {
		String logger = event.getLoggerName();

		// logging 여부 확인
		{
			if(logger.startsWith("KLog")) {
				Boolean loggable = KContext.getT(AttrKey.LOGGABLE);
				if(loggable != true) {
					switch(event.getLevel().toInt()) {
					case ch.qos.logback.classic.Level.TRACE_INT:
					case ch.qos.logback.classic.Level.DEBUG_INT:
					case ch.qos.logback.classic.Level.INFO_INT:
						return KConstant.EMPTY;
					case ch.qos.logback.classic.Level.WARN_INT:
					case ch.qos.logback.classic.Level.ERROR_INT:
					default:
						break;
					}
				}
			}
		}

		// 메시지 생성
		StringBuffer messageBuf = null;
		{
			messageBuf = new StringBuffer(128);
			if(logger.startsWith("KLogApm")) {
				messageBuf.append(this.getTimestamp());
				messageBuf.append(this.getGuidTxid());
				messageBuf.append(this.getProfile());
				messageBuf.append(this.getRequestURI());
				messageBuf.append(event.getFormattedMessage());
				messageBuf.append(LINE);
				return messageBuf.toString();
			} else if(logger.startsWith("KLog")) {
				messageBuf.append(this.getTimestampSimple());
				messageBuf.append(this.getLevel(event.getLevel()));
				messageBuf.append(this.getProfile());
				messageBuf.append(this.getExecType());
				messageBuf.append(this.getGuidTxid());
				messageBuf.append(this.getApiType());
				messageBuf.append(this.getAuthType());
				messageBuf.append(this.getRequestURI());
				messageBuf.append(this.getCaller());
				messageBuf.append(this.getUserId());
				messageBuf.append(String.format(" ### %s", event.getFormattedMessage()));
				messageBuf.append(LINE);
				appendThrowable(event, messageBuf);
			} else {
				messageBuf.append(this.getTimestampSimple());
				messageBuf.append(this.getLevel(event.getLevel()));
				messageBuf.append(this.getProfile());
				messageBuf.append(this.getExecType());
				messageBuf.append(this.getGuidTxid());
				messageBuf.append(String.format("[%s:%d] ### %s",
						event.getLoggerName(),
						event.getCallerData()[0].getLineNumber(),
						event.getFormattedMessage().replaceAll("[\n\r]+", LINE))
					);
				messageBuf.append(LINE);
				appendThrowable(event, messageBuf);
			}
		}
		return this.getFullMessage(messageBuf);
	}

	private void appendThrowable(ILoggingEvent event, StringBuffer sbuf) {
		IThrowableProxy throwableProxy = event.getThrowableProxy();
		if(throwableProxy != null) {
			sbuf.append(ThrowableProxyUtil.asString(throwableProxy));
			sbuf.append(LINE);
		}
	}


	String getTimestamp() {
		String str = null;
		str = String.format("[%s]", dateFormat.format(new Date(System.currentTimeMillis())));
		return str;
	}

	String getTimestampSimple() {
		String str = null;
		str = String.format("[%s]", dateFormatSimple.format(new Date(System.currentTimeMillis())));
		return str;
	}

	String getLevel(Level level) {
		String str = null;
		switch(level.toInt()) {
		case ch.qos.logback.classic.Level.INFO_INT:
			str = LEVEL_INFO;
			break;
		case ch.qos.logback.classic.Level.WARN_INT:
			str = LEVEL_WARN;
			break;
		case ch.qos.logback.classic.Level.ERROR_INT:
			str = LEVEL_ERROR;
			break;
		default:
			str = String.format("[%-5s]", level);
			break;
		}
		return str;
	}

	String getExecType() {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE, TExecType.SYSTEM);
		return KAnsi.blue(String.format("[%s]", KAnsi.blue(execType.code())));
	}

	String getProfile() {
		if("[]".equals(PROFILES)) {
			return KConstant.EMPTY;
		}
		switch(KProfile.SYS) {
		case LOC:
			//return KConstant.EMPTY;
		default:
			return PROFILES;
		}
	}

	String getApiType() {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE, TExecType.SYSTEM);
		if(execType != TExecType.REQUEST) {
			return KConstant.EMPTY;
		}
		TApiType apiType = KContext.getT(AttrKey.API_TYPE);
		if(apiType == null) {
			return KConstant.EMPTY;
		}
		return String.format("[%s]", KAnsi.red(apiType.code()));
	}

	String getAuthType() {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE, TExecType.SYSTEM);
		if(execType != TExecType.REQUEST) {
			return KConstant.EMPTY;
		}
		TAuthType authType = KContext.getT(AttrKey.AUTH_TYPE);
		if(authType == null) {
			return KConstant.EMPTY;
		}
		return String.format("[%s]", KAnsi.red(authType.code()));
	}

	String getRequestURI() {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE, TExecType.SYSTEM);
		if(execType != TExecType.REQUEST) {
			return KConstant.EMPTY;
		}
		if(MDC.get(IP) == null) {
			return KConstant.EMPTY;
		} else {
			return String.format("[%s]", MDC.get(URI));
		}
	}

	String getCaller() {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE, TExecType.SYSTEM);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_CALLER;
		switch(execType) {
		case REQUEST:
			if(!isVerboss) {
				return KConstant.EMPTY;
			}
			break;
		case SCHEDULE:
		default:
			if(!KConfig.VERBOSS_SCHEDULE) {
				return KConstant.EMPTY;
			}
			break;
		}
		String caller = MDC.get(CALLER);
		if(KStringUtil.isEmpty(caller)) {
			return KConstant.EMPTY;
		} else {
			return String.format("[%s]", caller);
		}
	}

	String getGuidTxid() {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE, TExecType.SYSTEM);
		String result = null;

		switch(execType) {
		case SYSTEM:
			result = KConstant.EMPTY;
			break;
		case SCHEDULE:
			result = KAnsi.green(String.format("[%s]",
					MDC.get(TXID)));
			break;
		case REQUEST:
			switch(KProfile.SYS) {
			case LOC:
				result = String.format("[%s|%s]", KAnsi.magenta(MDC.get(GUID)), KAnsi.green(MDC.get(TXID)));
				break;
			case DEV:
			case TEST:
				result = KAnsi.green(String.format("[%s|%s|%s]",
						MDC.get(IP),
						MDC.get(GUID), MDC.get(TXID)));
				/* @TODO
				if(@condition) {
					result = KAnsi.green(String.format("[%s-%s|%s|%s]",
							MDC.get(IP), KProperty.getString(MDC.get(IP)),
							MDC.get(GUID), MDC.get(TXID)));
				} else {
					result = KAnsi.green(String.format("[%s|%s|%s]",
							MDC.get(IP),
							MDC.get(GUID), MDC.get(TXID)));
				}*/
				break;
			case PROD:
			default:
				result = KAnsi.green(String.format("[%s|%s|%s]",
						MDC.get(IP),
						MDC.get(GUID), MDC.get(TXID)));
				break;
			}
		}
		return result;
	}

	String getUserId() {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE, TExecType.SYSTEM);
		if(execType != TExecType.REQUEST) {
			return KConstant.EMPTY;
		}
		if(MDC.get(IP) == null) {
			return KConstant.EMPTY;
		}
		String tokenUserId = KContext.getT(AttrKey.USER_ID);
		if(KStringUtil.isEmpty(tokenUserId)) {
			return KConstant.EMPTY;
		} else {
			return KAnsi.red(String.format("[%s]", MDC.get(USER_ID)));
		}
	}

	String getFullMessage(StringBuffer sbuf) {
		return sbuf.toString();
		/*if(condition) {
			return sbuf.toString().replaceAll("\\R", "") + LINE;
		} else {
			return sbuf.toString();
		}*/
	}

}
