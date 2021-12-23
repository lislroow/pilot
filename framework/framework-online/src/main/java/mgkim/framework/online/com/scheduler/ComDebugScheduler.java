package mgkim.framework.online.com.scheduler;

import static mgkim.framework.core.env.KConstant.MDC_DEBUG_FILENAME;
import static mgkim.framework.core.env.KConstant.MDC_DEBUG_MODE_YN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KTaskSchedule;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.stereo.KTask;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.online.cmm.CmmDebug;
import mgkim.framework.online.cmm.vo.debug.CmmDebugVO;

@KTaskSchedule(name = "debug 스케줄러", interval = 3000, manage = true)
public class ComDebugScheduler extends KScheduler {

	private static List<CmmDebugVO> debugList = new ArrayList<CmmDebugVO>(); // 주의: debugList객체는 null 이 되지 않도록 주의가 필요합니다.

	@Autowired(required = false)
	private CmmDebug cmmDebug;

	@Override
	protected void init() throws Exception {
		// enabled 설정
		{
			if (cmmDebug == null) {
				enabled = false;
				if (KObjectUtil.required(CmmDebug.class)) {
					throw new KSysException(KMessage.E5001, KObjectUtil.name(CmmDebug.class));
				} else {
					KLogSys.warn(KMessage.get(KMessage.E5003, KObjectUtil.name(ComDebugScheduler.class), KObjectUtil.name(CmmDebug.class)));
				}
				return;
			}

			// @TODO enabled = @condition
			enabled = true;

			if (!enabled) {
				KLogSys.warn(KMessage.get(KMessage.E5004, KObjectUtil.name(ComDebugScheduler.class)));
			}
		}
	}

	@Override
	protected KTask task() throws Exception {
		KTask task = new KTask() {
			@Override
			protected void execute(String execId) throws Exception {
				long currTime = System.currentTimeMillis();
				switch(KProfile.SYS) {
				case LOC:
					KLogSys.debug("디버그 로컬 모드에서 처리됩니다.");
					boolean isTimeout = false;
					for (CmmDebugVO item : debugList) {
						long stopTime = item.stopTime.getTime();
						long remainTime = (stopTime - currTime) / KConstant.MSEC;
						if (remainTime < 0) {
							isTimeout = true;
						} else {
							KLogSys.warn("***** 디버깅 ON ***** file={}, remainTime={}", item.debugFilePath, remainTime);
						}
					}
					if (isTimeout) {
						debugList.clear();
					}
					break;
				case DEV:
				case TEST:
				case PROD:
					for (CmmDebugVO item : debugList) {
						long stopTime = item.stopTime.getTime();
						long remainTime = (stopTime - currTime) / KConstant.MSEC;
						if (remainTime < 0) {
							KLogSys.warn("***** 디버깅 OFF ***** file={}", item.debugFilePath);
							cmmDebug.stopDebug(item);
						} else {
							KLogSys.warn("***** 디버깅 ON ***** file={}, remainTime={}", item.debugFilePath, remainTime);
						}
					}
					List<CmmDebugVO> _debugList = cmmDebug.selectDebuggingList();
					debugList = _debugList == null ? Collections.emptyList() : _debugList;
					break;
				}
			}
		};
		return task;
	}

	public CmmDebugVO add(CmmDebugVO vo) throws KSysException {
		if (!enabled) {
			return null;
		}

		String guid = KContext.getT(AttrKey.SSID);
		CmmDebugVO debugVO = new CmmDebugVO();
		debugVO.ssid = guid;
		debugVO.userId = vo.getUserId();
		debugVO.duration = vo.getDuration();

		switch(KProfile.SYS) {
		case LOC:
			debugVO.startTime = new Date(System.currentTimeMillis());
			debugVO.stopTime = new Date(debugVO.startTime.getTime() + (debugVO.duration * KConstant.MSEC));
			debugVO.ipAddr = KContext.getT(AttrKey.IP);
			debugVO.debugFilePath = String.format("%s/%s_%s.log", debugVO.ipAddr, debugVO.userId, debugVO.ssid);
			debugList.add(debugVO);
			break;
		case DEV:
		case TEST:
		case PROD:
			try {
				debugVO = cmmDebug.startDebug(debugVO);
			} catch(KSysException e) {
				throw e;
			} catch(Exception e) {
				throw new KSysException(KMessage.E6105, e);
			}
			break;
		}

		KLogSys.warn("***** 디버깅이 시작 되었습니다. ***** ssid={}, userId={}, 디버그시간={}, 종료예정시각={}",
				debugVO.ssid, debugVO.userId, debugVO.duration, KDateUtil.toString(debugVO.stopTime, KConstant.FMT_YYYY_MM_DD_HH_MM_SS));
		return debugVO;
	}

	public static void check() {
		if (debugList.size() == 0) {
			return;
		}

		String ssid = KContext.getT(AttrKey.SSID);
		for (CmmDebugVO item : debugList) {
			KLogSys.info("ssid={}", item.ssid);
			if (ssid.equals(item.ssid)) {
				KLogSys.warn("***** 디버그 상태입니다. *****");
				MDC.put(MDC_DEBUG_MODE_YN, "Y");
				MDC.put(MDC_DEBUG_FILENAME, item.debugFilePath);
			}
		}
	}
}