package mgkim.framework.online.com.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import mgkim.framework.core.annotation.KTaskSchedule;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.stereo.KTask;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.online.v1.ApiTxLogService;

@KTaskSchedule(name = "api처리로그 관리", interval = 1000, manage = true)
public class ComApiTxLogScheduler extends KScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(ComApiTxLogScheduler.class);
	
	@Value("${schedule.apitxlog.enabled:true}")
	private boolean enabled;
	
	private Queue<Map<String, Object>> queue = new ConcurrentLinkedQueue<Map<String, Object>>();
	
	@Autowired(required = false)
	private ApiTxLogService apiTxLogService;
	
	@Override
	protected void init() throws Exception {
		// enabled 설정
		if (apiTxLogService == null) {
			enabled = false;
			log.warn(KMessage.get(KMessage.E5003, KObjectUtil.name(ComApiTxLogScheduler.class), KObjectUtil.name(ApiTxLogService.class)));
			return;
		}
	}
	
	@Override
	protected KTask task() throws Exception {
		if (!enabled) {
			return null;
		}
		
		KTask task = new KTask() {
			@Override
			protected void execute(String execId) throws Exception {
				if (org.springframework.util.ObjectUtils.isEmpty(queue)) {
					return;
				}
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				while (queue.size() > 0 && list.size() < 500) {  // 설정
					list.add(queue.poll());
				}
				apiTxLogService.insertLog_map(list);
			}
		};
		return task;
	}

	public void addLog() {
		if (!enabled) {
			return;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("appCd", KProfile.APP_CD);
		map.put("userId", KContext.getT(AttrKey.USER_ID));
		map.put("ip", KContext.getT(AttrKey.IP));
		map.put("ssid", KContext.getT(AttrKey.SSID));
		map.put("txid", KContext.getT(AttrKey.TXID));
		map.put("uriVal", KContext.getT(AttrKey.URI));
		map.put("hostname", KProfile.HOSTNAME);
		map.put("appId", KProfile.APP_ID);
		map.put("rsltcd", KContext.getT(AttrKey.RESULT_CODE));
		map.put("rsltmsg", KContext.getT(AttrKey.RESULT_MESSAGE));
		map.put("errtxt", KContext.getT(AttrKey.RESULT_TEXT));
		map.put("token", KContext.getT(AttrKey.BEARER));
		queue.add(map);
	}
}
