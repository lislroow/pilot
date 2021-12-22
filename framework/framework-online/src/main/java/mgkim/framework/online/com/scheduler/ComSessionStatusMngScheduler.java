package mgkim.framework.online.com.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KTaskSchedule;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.stereo.KTask;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.cmm.CmmSessionStatusMng;
import mgkim.framework.online.cmm.vo.sessionexpmng.CmmSessionMngListVO;

@KTaskSchedule(name = "session 관리 스케줄러", interval = 10000, manage = true)
public class ComSessionStatusMngScheduler extends KScheduler {

	private Queue<String> queue = new ConcurrentLinkedQueue<String>();

	@Autowired(required = false)
	private CmmSessionStatusMng cmmSessionStatusMng;

	@Override
	protected void init() throws Exception {
		// enabled 설정
		{
			if (cmmSessionStatusMng == null) {
				throw new KSysException(KMessage.E5001, KObjectUtil.name(CmmSessionStatusMng.class));
			}
		}
	}

	public boolean containsUserId(final List<String> list, String userId) {
		boolean result = list.stream().filter(item -> userId.equals(item)).findFirst().isPresent();
		return result;
	}

	@Override
	protected KTask task() throws Exception {
		KTask task = new KTask() {
			@Override
			protected void execute(String execId) throws Exception {
				cmmSessionStatusMng.updateInvalidStatus();
				if (org.springframework.util.ObjectUtils.isEmpty(queue)) {
					return;
				}
				List<String> sessionList = new ArrayList<String>();
				while (queue.size() > 0 && sessionList.size() < 500) {  // 설정
					String item = queue.poll();
					if (containsUserId(sessionList, KStringUtil.nvl(item))) {
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
		io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
		Map<String, Object> claims = (Map<String, Object>)token.getBody();
		queue.add(KStringUtil.nvl(claims.get("ssid")));
	}
}
