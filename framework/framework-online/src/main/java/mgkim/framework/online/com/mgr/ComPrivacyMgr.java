package mgkim.framework.online.com.mgr;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.type.TPrivacyType;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.online.cmm.CmmPrivacy;
import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyLogVO;
import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyMngVO;

@KBean(name = "개인정보관리")
public class ComPrivacyMgr implements InitializingBean {

	final String BEAN_NAME = KObjectUtil.name(ComPrivacyMgr.class);

	@Autowired(required = false)
	private CmmPrivacy cmmPrivacy;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock w = rwl.writeLock();

	private Map<String, CmmPrivacyMngVO> mngList = new LinkedHashMap<String, CmmPrivacyMngVO>();

	@Override
	public void afterPropertiesSet() throws Exception {
		if (cmmPrivacy == null) {
			KLogSys.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmPrivacy.class)));
			return;
		}

		refresh();
	}

	public void refresh() throws Exception {
		List<CmmPrivacyMngVO> list = null;
		try {
			CmmPrivacyMngVO vo = new CmmPrivacyMngVO();
			vo.setMngtgTpcd(TPrivacyType.SQL.code());

			list = cmmPrivacy.selectMngAll(vo);
		} catch (Exception e) {
			throw e;
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

		KLogSys.info("개인정보관리 대상 sql {}개 가 로드 되었습니다.", mngList.size());
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
