package mgkim.proto.www.cmm;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.cmm.CmmReloadableConfMng;
import mgkim.framework.online.cmm.vo.reloadableconf.CmmReloadableConfVO;
import mgkim.framework.online.com.mgr.ComUriAuthorityMgr;
import mgkim.proto.www.cmm.mapper.CmmReloadableConfMngMapper;

@KBean
public class CmmReloadableConfMngImpl implements CmmReloadableConfMng {

	@Autowired
	private ComUriAuthorityMgr comUriAuthorityMgr;

	@Autowired
	private CmmReloadableConfMngMapper cmmReloadableConfMngMapper;

	@Override
	public void check() throws Exception {
		List<CmmReloadableConfVO> list = null;
		CmmReloadableConfVO vo = new CmmReloadableConfVO();
		vo.setHostname(KProfile.getHostname());
		vo.setWasId(KProfile.getWasId());

		list = cmmReloadableConfMngMapper.selectReloadStateList(vo);

		for (CmmReloadableConfVO item : list) {
			TConfStcd confStcdType = TConfStcd.get(item.getConfStcd());
			if (confStcdType == null) {
				KLogSys.warn("환경설정(CONF_ID=`{}`)의 상태코드를 확인해주세요. (CONFIG_STCD=`{}`)"
						, item.getConfId()
						, item.getConfStcd());
				continue;
			}
			switch(confStcdType) {
			case RELOADABLE:
				reload(item); // ★`환경설정` 갱신
				break;
			case RESERVED:
				if (KStringUtil.isEmpty(item.getApliRdttm())) {
					KLogSys.warn("환경설정(CONF_ID=`{}`) 갱신이 예약상태일 경우 예약시각은 null 이 될 수 없습니다.", item.getConfId());
					continue;
				}
				Date apliRdttm = KDateUtil.toDate(item.getApliRdttm(), KConstant.FMT_YYYYMMDDHHMMSS);
				long currDttm = System.currentTimeMillis();
				if (currDttm > apliRdttm.getTime()) {
					reload(item); // ★`환경설정` 갱신
				} else {
					long remainTime = (apliRdttm.getTime() / 1000) - (currDttm / 1000);
					KLogSys.warn("환경설정(CONF_ID=`{}`) 갱신이 예약 되어 있습니다. `{}초` 후에 갱신이 됩니다.", item.getConfId(), remainTime);
				}
				break;
			default:
				break;
			}
		}
	}

	// ★`환경설정` 갱신
	private void reload(CmmReloadableConfVO item) throws Exception {
		boolean isSuccess = true;
		KLogSys.warn("`환경설정`({})을 갱신합니다.", item.getConfId());
		TReloadableConf confIdType = TReloadableConf.get(item.getConfId());
		switch(confIdType) {
		case ComUriAuthorityMgr:
			try {
				comUriAuthorityMgr.reload();
			} catch(Exception e) {
				isSuccess = false;
				KLogSys.error("환경설정(CONF_ID=`{}`)을 갱신하는 중 오류가 발생했습니다.", confIdType.code(), e);
			}
			break;
		case KConfig:
			try {
				KConfig.reload();
			} catch(Exception e) {
				isSuccess = false;
				KLogSys.error("환경설정(CONF_ID=`{}`)을 갱신하는 중 오류가 발생했습니다.", confIdType.code(), e);
			}
			break;
		}

		if (isSuccess) {
			KLogSys.warn("환경설정(CONF_ID=`{}`) 갱신을 완료 했습니다.", confIdType.code());
			item.setConfStcd(TConfStcd.DONE.code());
		} else {
			item.setConfStcd(TConfStcd.FAIL.code());
		}
		KLogSys.warn("`환경설정`({})을 적용 시간을 갱신합니다.", item.getConfId());
		cmmReloadableConfMngMapper.updateReloadState(item);
	}

	@Override
	public void resetAll() throws Exception {
		CmmReloadableConfVO item = new CmmReloadableConfVO();
		item.setHostname(KProfile.getHostname());
		item.setWasId(KProfile.getWasId());
		KLogSys.warn("`모든환경설정`의 상태코드를 reset 합니다.");
		cmmReloadableConfMngMapper.deleteReloadStateForReset(item);
		for (TReloadableConf confId : TReloadableConf.values()) {
			item.setConfId(confId.code());
			cmmReloadableConfMngMapper.updateReloadStateForReset(item);
		}
	}
}
