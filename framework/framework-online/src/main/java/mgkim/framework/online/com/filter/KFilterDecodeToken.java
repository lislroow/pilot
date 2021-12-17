package mgkim.framework.online.com.filter;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.type.TApiType;
import mgkim.framework.core.type.TAuthType;
import mgkim.framework.online.cmm.CmmUserToken;
import mgkim.framework.online.cmm.vo.token.CmmOpenapiTokenVO;
import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KProfile;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.logging.KLogSys;
import mgkim.framework.online.com.mgr.ComUserTokenMgr;
import mgkim.framework.online.com.session.KToken;
import mgkim.framework.online.com.util.KDateUtil;
import mgkim.framework.online.com.util.KObjectUtil;
import mgkim.framework.online.com.util.KStringUtil;

@KBean(name = "token decode 필터")
public class KFilterDecodeToken extends KFilter implements InitializingBean {

	final String BEAN_NAME = KObjectUtil.name(KFilterDecodeToken.class);

	@Autowired(required = true)
	private ComUserTokenMgr comUserTokenMgr;

	@Autowired(required = true)
	private CmmUserToken cmmUserToken;

	@Override
	public void afterPropertiesSet() throws ServletException {
		if(cmmUserToken == null) {
			KLogSys.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmUserToken.class)));
			return;
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			String bearer = null;
			TApiType apiType = KContext.getT(AttrKey.API_TYPE);
			TAuthType authType = KContext.getT(AttrKey.AUTH_TYPE);

			// api타입별 유효한 인증타입(apikey, bearer) 검증
			{

				if(apiType == TApiType.OPENAPI) {
					if(authType != TAuthType.BEARER) {
						throw new KSysException(KMessage.E6014);
					}
				} else if(apiType == TApiType.INTERAPI) {
					if(authType != TAuthType.APIKEY) {
						throw new KSysException(KMessage.E6001);
					}
				}
			}

			// 인증타입별 accessToken 획득 (apikey 일 경우 DB에서 accessToken 조회 후 부가적인 검증 처리)
			{
				switch(authType) {
				case BEARER:
					bearer = KContext.getT(AttrKey.BEARER);
					break;
				case APIKEY:
					if(cmmUserToken == null) {
						throw new KSysException(KMessage.E6002);
					}
					String apikey = KContext.getT(AttrKey.APIKEY);
					CmmOpenapiTokenVO tokenVO = cmmUserToken.selectOpenapiToken(apikey);
					// token 문자열이 비어있는지 체크
					if(tokenVO == null || KStringUtil.isEmpty(tokenVO.getTokenContent())) {
						throw new KSysException(KMessage.E6003);
					}
					// 사용자 승인여부 체크
					if(!"Y".equals(tokenVO.getAprvYn())) {
						throw new KSysException(KMessage.E6004);
					}
					// IP 체크
					String ipAddr = KContext.getT(AttrKey.IP);
					if(!ipAddr.startsWith(KStringUtil.nvl(tokenVO.getIpAddr()))) {
						switch(KProfile.SYS) {
						case LOC:
							// 로컬 환경에서는 IP가 127.0.0.1 로 나오기 때문에 체크하지 않도록 함
							break;
						case DEV:
						case TEST:
						case PROD:
							throw new KSysException(KMessage.E6005);
						}
					}
					// apikey 폐기 여부 체크
					if("Y".equals(tokenVO.getDisuYn())) {
						throw new KSysException(KMessage.E6006);
					}

					Long beginDttm = null;
					Long expryDttm = null;
					try {
						beginDttm = KDateUtil.toDate(tokenVO.getBeginDttm(), KConstant.FMT_YYYYMMDDHHMMSS).getTime();
					} catch(ParseException e) {
						throw new KSysException(KMessage.E6007, "시작일시", e);
					}
					try {
						expryDttm = KDateUtil.toDate(tokenVO.getExpryDttm(), KConstant.FMT_YYYYMMDDHHMMSS).getTime();
					} catch(ParseException e) {
						throw new KSysException(KMessage.E6007, "종료일시", e);
					}

					// token 유효기간 체크
					long current = System.currentTimeMillis();
					if(!((current > beginDttm) && (current < expryDttm))) {
						throw new KSysException(KMessage.E6008
								, KDateUtil.convert(beginDttm, KConstant.FMT_YYYY_MM_DD_HH_MM_SS)
								, KDateUtil.convert(expryDttm, KConstant.FMT_YYYY_MM_DD_HH_MM_SS));
					}
					bearer = tokenVO.getTokenContent();
					KContext.set(AttrKey.BEARER, bearer);
					break;
				case NOAUTH:
					break;
				}
			}

			// accessToken 확인
			{
				if(KStringUtil.isEmpty(bearer)) {
					throw new KSysException(KMessage.E6009);
				}
			}



			// token 정보 저장 (KContext)
			{
				KToken token = comUserTokenMgr.convertToken(bearer);
				KContext.initToken(token);
			}
		} catch(KException e) {
			KExceptionHandler.response(response, e);
			return;
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7007, e, BEAN_NAME));
			return;
		}
		chain.doFilter(request, response);
	}
}
