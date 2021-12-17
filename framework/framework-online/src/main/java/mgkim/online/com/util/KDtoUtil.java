package mgkim.online.com.util;

import java.util.Iterator;
import java.util.List;

import mgkim.online.com.dto.KCmmVO;
import mgkim.online.com.env.KContext;
import mgkim.online.com.env.KProfile;
import mgkim.online.com.env.KContext.AttrKey;
import mgkim.online.com.session.KSession;
import mgkim.online.com.session.KToken;
import mgkim.online.com.type.TApiType;

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
