package mgkim.framework.online.com.mgr;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import mgkim.framework.cmm.online.CmmPrivacy;
import mgkim.framework.cmm.online.vo.CmmPrivacyLogVO;
import mgkim.framework.cmm.online.vo.CmmPrivacyMngVO;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.type.TPrivacyType;
import mgkim.framework.core.util.KObjectUtil;

@KBean(name = "개인정보관리")
public class ComPrivacyMgr {
	
	private static final Logger log = LoggerFactory.getLogger(ComPrivacyMgr.class);

	final String BEAN_NAME = KObjectUtil.name(ComPrivacyMgr.class);

	@Autowired(required = false)
	private CmmPrivacy cmmPrivacy;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock w = rwl.writeLock();

	private Map<String, CmmPrivacyMngVO> mngList = new LinkedHashMap<String, CmmPrivacyMngVO>();

	@EventListener
	public void refreshed(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			if (cmmPrivacy == null) {
				log.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmPrivacy.class)));
				return;
			}
			refresh();
		}
	}

	public void refresh() {
		List<CmmPrivacyMngVO> list = null;
		try {
			CmmPrivacyMngVO vo = new CmmPrivacyMngVO();
			vo.setMngtgTpcd(TPrivacyType.SQL.code());

			list = cmmPrivacy.selectMngAll(vo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (list != null) {
			w.lock();
			try {
				Map<String, CmmPrivacyMngVO> tmpList = new LinkedHashMap<String, CmmPrivacyMngVO>();
				for (CmmPrivacyMngVO item : list) {
					tmpList.put(item.getMngtgId(), item);
				}
				mngList.clear();
				mngList.putAll(tmpList);
			} finally {
				w.unlock();
			}
		}

		log.info("개인정보관리 대상 sql {}개 가 로드 되었습니다.", mngList.size());
	}

	public void insertLog(CmmPrivacyLogVO vo) throws Exception {
		if (mngList.size() == 0) {
			return;
		}

		if (mngList.containsKey(vo.getMngtgId())) {
			cmmPrivacy.insertLog(vo);
		}
	}
}
