package mgkim.core.com.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.core.cmm.CmmApiTxLog;
import mgkim.core.cmm.vo.apitxlog.CmmApiTxLogVO;
import mgkim.core.com.annotation.KTaskSchedule;
import mgkim.core.com.env.KConstant;
import mgkim.core.com.env.KContext;
import mgkim.core.com.env.KContext.AttrKey;
import mgkim.core.com.env.KProfile;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.stereo.KScheduler;
import mgkim.core.com.stereo.KTask;
import mgkim.core.com.util.KDateUtil;
import mgkim.core.com.util.KObjectUtil;

@KTaskSchedule(name = "api처리로그 관리", interval = 1000, manage = true)
public class CmmApiTxLogScheduler extends KScheduler {

	private final int MAX_QUEUQ_SIZE = 3;

	private Queue<CmmApiTxLogVO> queue = new ConcurrentLinkedQueue<CmmApiTxLogVO>();

	@Autowired(required = false)
	private CmmApiTxLog cmmApiTxLog;

	@Override
	protected void init() throws Exception {
		// enabled 설정
		{
			if(cmmApiTxLog == null) {
				enabled = false;
				if(KObjectUtil.required(CmmApiTxLog.class)) {
					throw new KSysException(KMessage.E5001, KObjectUtil.name(CmmApiTxLog.class));
				} else {
					KLogSys.warn(KMessage.get(KMessage.E5003, KObjectUtil.name(CmmApiTxLogScheduler.class), KObjectUtil.name(CmmApiTxLog.class)));
				}
				return;
			}
		}
	}

	@Override
	protected KTask task() throws Exception {
		if(!enabled) {
			return null;
		}

		KTask task = new KTask() {
			@Override
			protected void execute(String execId) throws Exception {
				if(org.springframework.util.ObjectUtils.isEmpty(queue)) {
					return;
				}
				List<CmmApiTxLogVO> list = new ArrayList<CmmApiTxLogVO>();
				while(queue.size() > 0 && list.size() < 500) {  // 설정
					list.add(queue.poll());
				}
				cmmApiTxLog.insertLog(list);
			}
		};
		return task;
	}

	public void addLog() {
		if(!enabled) {
			return;
		}

		while(queue.size() >= MAX_QUEUQ_SIZE) {
			queue.poll();
		}

		CmmApiTxLogVO vo = new CmmApiTxLogVO();
		vo.setSiteTpcd(KProfile.SITE.code());
		vo.setUserId(KContext.getT(AttrKey.USER_ID));
		vo.setIp(KContext.getT(AttrKey.IP));
		vo.setSsid(KContext.getT(AttrKey.SSID));
		vo.setTxid(KContext.getT(AttrKey.TXID));
		vo.setUriVal(KContext.getT(AttrKey.URI));
		vo.setHostname(KProfile.HOSTNAME);
		vo.setWasId(KProfile.getWasId());
		vo.setRsltcd(KContext.getT(AttrKey.RESULT_CODE));
		vo.setRsltmsg(KContext.getT(AttrKey.RESULT_MESSAGE));
		vo.setErrtxt(KContext.getT(AttrKey.RESULT_TEXT));
		vo.setToken(KContext.getT(AttrKey.BEARER));

		queue.add(vo);
	}

	public int archive(long secondsAgo) throws Exception {
		long timestamp = System.currentTimeMillis() - (secondsAgo * KConstant.MSEC);
		String archiveDttm = KDateUtil.convert(timestamp, KConstant.FMT_YYYY_MM_DD_HH_MM_SS);
		int cnt = cmmApiTxLog.archive(archiveDttm);
		KLogSys.warn(KMessage.get(KMessage.E1001, cnt));
		return cnt;
	}
}
