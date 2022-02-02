package mgkim.service.www.api.cmm.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.dto.KOAuthToken;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.type.KType.AumthType;
import mgkim.framework.core.type.KType.UserType;
import mgkim.framework.online.com.mgr.ComFieldCryptorMgr;
import mgkim.framework.online.com.mgr.ComSessionStatusMgr;
import mgkim.framework.online.com.mgr.ComUserTokenMgr;
import mgkim.service.online.cmm.CmmUserLoginPolicyVO;
import mgkim.service.www.com.env.CmmConstant;

//@Api( tags = { KConstant.SWG_SERVICE_COMMON } )
@RestController
public class UserLoginController {

	@Autowired(required = true)
	private ComUserTokenMgr comUserTokenMgr;

	@Autowired(required = true)
	private ComSessionStatusMgr comSessionStatusMgr;

	@Autowired(required = true)
	private ComFieldCryptorMgr comFieldCryptorMgr;

	@Autowired
	private UserLoginService userLoginService;
	
	@ApiOperation(value = "(로그인) ID로그인")
	@RequestMapping(value = "/public/cmm/user/id-login", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<Map<String, Object>> idlogin(
			@KRequestMap HashMap<String, Object> inMap,
			@RequestParam(required = true) String userId) throws Exception {
		KOutDTO<Map<String, Object>> outDTO = new KOutDTO<Map<String, Object>>();
		// `token 생성`
		Map<String, Object> claims = new HashMap<String, Object>();
		{
			// `필수 정의`
			claims.put("appCd", KProfile.APP_CD);
			claims.put("userTpcd", UserType.API.code());
			claims.put("aumthTpcd", AumthType.IDLOGIN.code());
			claims.put("userId", userId);

			// `사용자 정의`
			//token.setEmail(inVO.getEmail());

			// `사용자 데이터` 존재 여부 확인
			boolean isExist = userLoginService.selectUserExist(claims);
			if (!isExist) {
				throw new KSysException(KMessage.E6108);
			}

			// `로그인 정책 조회`
			CmmUserLoginPolicyVO cmmUserLoginPolicyVO = userLoginService.selectUserLoginPolicy(claims);

			// (로그인 정책) `세션유효시간 설정`
			int ssvaldSec;
			if (cmmUserLoginPolicyVO == null) {
				ssvaldSec = CmmConstant.USER_SESIONVALIDATY_SEC;
			} else {
				ssvaldSec = cmmUserLoginPolicyVO.getSsvaldSec();
			}
			claims.put("ssvaldSec", ssvaldSec);
		}

		// `jwt 생성`
		KOAuthToken jwt;
		{
			try {
				jwt = comUserTokenMgr.createOauthToken(claims);
			} catch(Exception e) {
				throw e;
			}
		}

		// `session 생성`
		{
			try {
				comSessionStatusMgr.insertNewStatus(claims);
			} catch(Exception e) {
				throw e;
			}
		}

		// `필드암호화 키 생성` 서버 rsa 키 생성
		String publicKey;
		{
			try {
				publicKey = comFieldCryptorMgr.createRsaKey(claims);
			} catch(Exception e) {
				throw e;
			}
		}

		// `결과 처리`
		Map<String, Object> outBody = new HashMap<String, Object>();
		{
			outBody.put("jwt", jwt);
			outBody.put("claims", claims);
			outBody.put("publicKey", publicKey);
			
			outDTO.setBody(outBody);
		}
		
		return outDTO;
	}

	@ApiOperation(value = "refresh-access-token")
	@RequestMapping(value = "/api/cmm/user/refresh-access-token", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<Map<String, Object>> refreshAccessToken(
			@KRequestMap HashMap<String, Object> inMap,
			@RequestParam(required = true, name = "encoded-refresh-token") String encodedRefreshToken) throws Exception {
		KOutDTO<Map<String, Object>> outDTO = new KOutDTO<Map<String, Object>>();

		// `token 변환`
		Map<String, Object> claims = null;
		String accessToken;
		{
			try {
				accessToken = comUserTokenMgr.refreshtoken(encodedRefreshToken);
			} catch(Exception e) {
				throw e;
			}
			
			io.jsonwebtoken.Jwt jwt = comUserTokenMgr.parsetoken(accessToken);
			claims = (Map<String, Object>) jwt.getBody();
		}
		
		// `session 상태 검증`
		{
			// `현재 session` 의 ssid 로 등록된 세션이 있는지 확인
			boolean isLogin = comSessionStatusMgr.isLoginStatus(claims);
			if (isLogin == false) {
				throw new KSysException(KMessage.E6103);
			}
		}
		
		// `jwt 생성`
		KOAuthToken jwt = new KOAuthToken();
		{
			jwt.setAccessToken(accessToken);
			jwt.setRefreshToken(encodedRefreshToken);
		}

		// `결과 처리`
		Map<String, Object> outBody = new HashMap<String, Object>();
		{
			outBody.put("jwt", jwt);
			outBody.put("claims", claims);
			
			outDTO.setBody(outBody);
		}
		return outDTO;
	}

	@ApiOperation(value = "(로그인) symmetric-key 저장")
	@RequestMapping(value = "/api/cmm/user/save-symmetric-key", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> saveSymKey(
			@KRequestMap HashMap<String, Object> inMap,
			@RequestParam(required = true, name = "symmetric-key") String symKey) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		comFieldCryptorMgr.saveSymKey(symKey);
		return outDTO;
	}
}
