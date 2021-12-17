package mgkim.proto.www.api.cmm.userlogin.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.type.TAumthType;
import mgkim.framework.core.type.TUserType;
import mgkim.framework.online.com.mgr.ComFieldCryptorMgr;
import mgkim.framework.online.com.mgr.ComSessionStatusMgr;
import mgkim.framework.online.com.mgr.ComUserTokenMgr;
import mgkim.proto.www.api.cmm.userlogin.service.UserLoginService;
import mgkim.proto.www.api.cmm.userlogin.vo.UserLoginReqVO;
import mgkim.proto.www.api.cmm.userlogin.vo.UserLoginResVO;
import mgkim.proto.www.cmm.vo.CmmUserLoginPolicyVO;
import mgkim.proto.www.com.env.CmmConstant;
import mgkim.proto.www.com.token.CmmTokenApi;

//@Api( tags = { KConstant.SWG_SERVICE_COMMON } )
@RestController
public class UserLoginController {
	public UserLoginController() {
		System.out.println("");
	}

	@Autowired(required = true)
	private ComUserTokenMgr comUserTokenMgr;

	@Autowired(required = true)
	private ComSessionStatusMgr comSessionStatusMgr;

	@Autowired(required = true)
	private ComFieldCryptorMgr comFieldCryptorMgr;

	@Autowired
	private UserLoginService userLoginService;

	@ApiOperation(value = "(로그인) ID로그인")
	@RequestMapping(value = "/public/cmm/user/idlogin", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody KOutDTO<UserLoginResVO> idlogin(@RequestBody KInDTO<UserLoginReqVO> inDTO) throws Exception {
		KOutDTO<UserLoginResVO> outDTO = new KOutDTO<UserLoginResVO>();
		UserLoginReqVO inVO = inDTO.getBody();

		// `token 생성`
		CmmTokenApi token;
		{
			token = new CmmTokenApi();
			// `필수 정의`
			token.setSiteTpcd(KProfile.SITE.code());
			token.setUserTpcd(TUserType.API.code());
			token.setAumthTpcd(TAumthType.IDLOGIN.code());
			token.setUserId(inVO.getUserId());

			// `사용자 정의`
			//token.setEmail(inVO.getEmail());

			// `사용자 데이터` 존재 여부 확인
			boolean isExist = userLoginService.selectUserExist(token);
			if(!isExist) {
				throw new KSysException(KMessage.E6108);
			}

			// `로그인 정책 조회`
			CmmUserLoginPolicyVO cmmUserLoginPolicyVO = userLoginService.selectUserLoginPolicy(token);

			// (로그인 정책) `세션유효시간 설정`
			int ssvaldSec;
			if(cmmUserLoginPolicyVO == null) {
				ssvaldSec = CmmConstant.USER_SESIONVALIDATY_SEC;
			} else {
				ssvaldSec = cmmUserLoginPolicyVO.getSsvaldSec();
			}
			token.setSsvaldSec(ssvaldSec);
		}

		// `jwt 생성`
		OAuth2AccessToken jwt;
		{
			try {
				jwt = comUserTokenMgr.newJwt(token);
			} catch(Exception e) {
				throw e;
			}
		}

		// `session 생성`
		{
			try {
				comSessionStatusMgr.insertNewStatus(token);
			} catch(Exception e) {
				throw e;
			}
		}

		// `필드암호화 키 생성` 서버 rsa 키 생성
		String publicKey;
		{
			try {
				publicKey = comFieldCryptorMgr.createRsaKey(token);
			} catch(Exception e) {
				throw e;
			}
		}

		// `결과 처리`
		UserLoginResVO outVO;
		{
			outVO = new UserLoginResVO();
			outVO.setJwt(jwt);
			outVO.setToken(token);
			outVO.setPublicKey(publicKey);
			outDTO.setBody(outVO);
		}

		return outDTO;
	}

	@ApiOperation(value = "refreshAccessToken")
	@RequestMapping(value = "/api/cmm/user/refreshAccessToken", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<UserLoginResVO> refreshAccessToken(@RequestBody KInDTO<String> inDTO) throws Exception {
		KOutDTO<UserLoginResVO> outDTO = new KOutDTO<UserLoginResVO>();
		String encodedRefreshToken = inDTO.getBody();

		// `token 변환`
		CmmTokenApi token;
		{
			try {
				token = comUserTokenMgr.convertToken(encodedRefreshToken);
			} catch(Exception e) {
				throw e;
			}
		}

		// `session 상태 검증`
		{
			// `현재 session` 의 ssid 로 등록된 세션이 있는지 확인
			boolean isLogin = comSessionStatusMgr.isLoginStatus(token);
			if(isLogin == false) {
				throw new KSysException(KMessage.E6103);
			}
		}

		// `jwt 생성`
		OAuth2AccessToken jwt;
		{
			try {
				jwt = comUserTokenMgr.refreshAccessToken(token, encodedRefreshToken);
			} catch(Exception e) {
				throw e;
			}
		}

		// `결과 처리`
		UserLoginResVO outVO;
		{
			outVO = new UserLoginResVO();
			outVO.setJwt(jwt);
			outVO.setToken(token);
			outDTO.setBody(outVO);
		}
		return outDTO;
	}

	@ApiOperation(value = "(로그인) symmetric-key 저장")
	@RequestMapping(value = "/api/cmm/user/saveSymKey", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> saveSymKey(@RequestBody KInDTO<String> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		String symKey = inDTO.getBody();
		comFieldCryptorMgr.saveSymKey(symKey);
		return outDTO;
	}
}
