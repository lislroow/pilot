package mgkim.framework.core.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.session.KSession;
import mgkim.framework.core.type.TApiType;
import mgkim.framework.core.type.TExecType;

public class KDtoUtil {

	public static boolean setSysValues(Object obj) {
		if (java.util.List.class.isInstance(obj)) {
			Iterator iter = ((List)obj).iterator();
			while (iter.hasNext()) {
				Object item = iter.next();
				if (setSysValues(item)) {
					continue;
				} else {
					break;
				}
			}
			return false;
		} else if (KCmmVO.class.isInstance(obj)) {
			KCmmVO cmmVO = (KCmmVO) obj;
			cmmVO.setAppCd(KProfile.APP_CD);

			if (TApiType.PUBLIC == KContext.getT(AttrKey.API_TYPE)) {
				return true;
			}
			if (TExecType.REQUEST != KContext.getT(AttrKey.EXEC_TYPE)) {
				return true;
			}

			KSession session = KSessionUtil.getSession();
			if (session != null) {
				cmmVO.setSsuserId(session.getUserId());
				io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
				Map<String, Object> claims = (Map<String, Object>)token.getBody();
				if (token != null) {
					cmmVO.setAumthTpcd(KStringUtil.nvl(claims.get("aumthTpcd")));
				}
				cmmVO.setSsid(KContext.getT(AttrKey.SSID));
			}
			cmmVO.setTxid(KContext.getT(AttrKey.TXID));
			return true;
		} else {
			return false;
		}
	}
}
