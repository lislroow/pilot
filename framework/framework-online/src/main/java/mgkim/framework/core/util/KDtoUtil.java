package mgkim.framework.core.util;

import java.util.Iterator;
import java.util.List;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.session.KSession;
import mgkim.framework.core.session.KToken;
import mgkim.framework.core.type.TApiType;

public class KDtoUtil {

	public static boolean setSysValues(Object obj) {
		if(java.util.List.class.isInstance(obj)) {
			Iterator iter = ((List)obj).iterator();
			while(iter.hasNext()) {
				Object item = iter.next();
				if(setSysValues(item)) {
					continue;
				} else {
					break;
				}
			}
			return false;
		} else if(KCmmVO.class.isInstance(obj)) {
			KCmmVO cmmVO = (KCmmVO) obj;
			cmmVO.setSiteTpcd(KProfile.SITE.code());

			if(TApiType.PUBLIC == KContext.getT(AttrKey.API_TYPE)) {
				return true;
			}

			KSession session = KSessionUtil.getSession();
			if(session != null) {
				cmmVO.setSsuserId(session.getUserId());
			}
			KToken token = KContext.getT(AttrKey.TOKEN);
			if(token != null) {
				cmmVO.setAumthTpcd(token.getAumthTpcd());
			}
			cmmVO.setSsid(KContext.getT(AttrKey.SSID));
			cmmVO.setTxid(KContext.getT(AttrKey.TXID));
			return true;
		} else {
			return false;
		}
	}
}
