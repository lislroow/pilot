package mgkim.framework.online.com.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.online.cmm.CmmSessionStatusMng;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;
import mgkim.framework.online.com.annotation.KTaskSchedule;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.exception.KMessage;
import mgkim.framework.online.com.exception.KSysException;
import mgkim.framework.online.com.session.KToken;
import mgkim.framework.online.com.stereo.KScheduler;
import mgkim.framework.online.com.stereo.KTask;
import mgkim.framework.online.com.util.KObjectUtil;

@KTaskSchedule(name = "session 관리 스케줄러", interval = 10000, manage = true)
public class ComSessionStatusMngScheduler extends KScheduler {

	private Queue<KToken> queue = new ConcurrentLinkedQueue<KToken>();

	@Autowired(required = false)
	private CmmSessionStatusMng cmmSessionStatusMng;

	@Override
	protected void init() throws Exception {
		// enabled 설정
		{
			if(cmmSessionStatusMng == null) {
				throw new KSysException(KMessage.E5001, KObjectUtil.name(CmmSessionStatusMng.class));
			}
		}
	}

	public boolean containsSsid(final List<KToken> list, String ssid) {
		boolean result = list.stream().filter(item -> ssid.equals(item.getSsid())).findFirst().isPresent();
		return result;
	}

	@Override
	protected KTask task() throws Exception {
		KTask task = new KTask() {
			@Override
			protected void execute(String execId) throws Exception {
				cmmSessionStatusMng.updateInvalidStatus();
				if(org.springframework.util.ObjectUtils.isEmpty(queue)) {
					return;
				}
				List<KToken> sessionList = new ArrayList<KToken>();
				while(queue.size() > 0 && sessionList.size() < 500) {  // 설정
					KToken item = queue.poll();
					if(containsSsid(sessionList, item.getSsid())) {
						continue;
					};
					sessionList.add(item);
				}
				CmmSessionMngListVO vo = new CmmSessionMngListVO();
				vo.setSessionList(sessionList);
				cmmSessionStatusMng.updateRefresh(vo);
			}
		};
		return task;
	}

	public void addSession() {
		KToken token = KContext.getT(AttrKey.TOKEN);
		queue.add(token);
	}
}
