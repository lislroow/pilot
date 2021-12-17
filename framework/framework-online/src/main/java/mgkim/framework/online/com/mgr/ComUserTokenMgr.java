package mgkim.framework.online.com.mgr;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.type.TJwtType;
import mgkim.framework.core.type.TSiteType;
import mgkim.framework.core.type.TUuidType;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.core.util.KTokenParser;
import mgkim.framework.online.cmm.CmmUserToken;
import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KProfile;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.session.KToken;

@KBean(name = "사용자 토큰 관리")
public class ComUserTokenMgr {

	private Signer signer;
	private SignatureVerifier verifier;

	public static final long ACCESSTOKEN_VALIDITY = 43200 * KConstant.MSEC;
	public static final long REFRESHTOKEN_VALIDITY = 86400 * KConstant.MSEC;
	public static final String API_TOKEN_CLAIMS = "{\"ssid\": \"\", \"ip\": \"\", \"userId\": \"\", \"userType\": \"\", \"elvtBizSysTpcd\": \"10\", \"loginCertTpcd\": \"I\", \"elvtConnTpcd\": \"01\", \"elvtShrNo\": \"\", \"agentCustno\": -1, \"autoCertYn\": \"N\", \"linkSeccoCd\": \"000\"}";
	public static final String OPENAPI_TOKEN_CLAIMS = "{\"ssid\": \"\", \"ip\": \"\", \"userId\": \"\", \"userType\": \"\", \"aesKey\": \"\", \"orgCd\": \"\", \"orgNm\": \"\"}";

	@Autowired(required = true)
	private CmmUserToken cmmUserToken;

	public ComUserTokenMgr() {
		String algorithm = "HMACSHA512";
		String secretKey = readSecreteKey();
		signer = new MacSigner(algorithm, new SecretKeySpec(secretKey.getBytes(), algorithm));
		verifier = new MacSigner(algorithm, new SecretKeySpec(secretKey.getBytes(), algorithm));
	}

	private String readSecreteKey() {
		String secretKey = null;
		switch(KProfile.SYS) {
		case DEV:
		case TEST:
		case PROD:
		case LOC:
		}
		if(KStringUtil.isEmpty(secretKey)) {
			secretKey = "1";
			//KLogSys.warn(KMessage.get(KMessage.E5006, "토큰 암호화키", secretKey));
		}
		return secretKey;
	}



	public OAuth2AccessToken newJwt(KToken token) throws Exception {
		// 기본값 설정
		long currTime = System.currentTimeMillis();
		{
			token.setCreateTime(currTime);
			token.setSiteTpcd(KProfile.SITE.code());
			token.setGuid(KContext.getT(AttrKey.GUID));
			token.setSsid(KStringUtil.createUuid(true, TUuidType.SSID));
			token.setIp(KContext.getT(AttrKey.IP));
			token.setBrowsInf(KHttpUtil.getHeader(KConstant.HK_USER_AGENT));
		}

		// refreshToken 생성
		DefaultOAuth2RefreshToken refreshToken = null;
		{
			// refreshToken `claim 세팅`
			{
				token.setExpireTime(System.currentTimeMillis()+REFRESHTOKEN_VALIDITY);
				token.setJwtTpcd(TJwtType.REFRESH_TOKEN.code());
			}
			try {
				String json = KTokenParser.format(token);
				String encoded = JwtHelper.encode(json, signer).getEncoded();
				refreshToken = new DefaultOAuth2RefreshToken(encoded);
			} catch(Exception e) {
				throw new KSysException(KMessage.E6022, e, TJwtType.REFRESH_TOKEN.label());
			}
		}

		// accessToken 생성
		DefaultOAuth2AccessToken accessToken = null;
		{
			// accessToken `claim 세팅`
			{
				token.setExpireTime(currTime+ACCESSTOKEN_VALIDITY);
				token.setJwtTpcd(TJwtType.ACCESS_TOKEN.code());
			}
			try {
				String json = new KTokenParser().format(token);
				String encoded = JwtHelper.encode(json, signer).getEncoded();
				accessToken = new DefaultOAuth2AccessToken(encoded);
				accessToken.setRefreshToken(refreshToken);
			} catch(Exception e) {
				throw new KSysException(KMessage.E6022, e, TJwtType.ACCESS_TOKEN.label());
			}
		}
		return accessToken;
	}

	public OAuth2AccessToken refreshAccessToken(KToken token, String encodedRefreshToken) throws Exception {
		// 기본값 설정
		long currTime = System.currentTimeMillis();

		// refreshToken 생성
		DefaultOAuth2RefreshToken refreshToken = null;
		{
			// `refreshToken 검증`
			{
				TJwtType jwtType = TJwtType.get(token.getJwtTpcd());
				if(jwtType != TJwtType.REFRESH_TOKEN) {
					throw new KSysException(KMessage.E6017, TJwtType.REFRESH_TOKEN.label());
				}

				if(currTime > token.getExpireTime()) {
					throw new KSysException(KMessage.E6018, TJwtType.REFRESH_TOKEN.label());
				}

				TSiteType siteType = TSiteType.get(token.getSiteTpcd());
				if(KProfile.SITE != siteType) {
					throw new KSysException(KMessage.E6020, siteType.label(), TJwtType.REFRESH_TOKEN.label());
				}
			}
			try {
				String json = KTokenParser.format(token);
				String encoded = JwtHelper.encode(json, signer).getEncoded();
				if(!encoded.equals(encodedRefreshToken)) {
					throw new KSysException(KMessage.E6021);
				}
				refreshToken = new DefaultOAuth2RefreshToken(encoded);
			} catch(Exception e) {
				throw new KSysException(KMessage.E6022, e, TJwtType.REFRESH_TOKEN.label());
			}
		}

		// accessToken 생성
		DefaultOAuth2AccessToken accessToken = null;
		{
			// accessToken `claim 세팅`
			{
				token.setCreateTime(currTime);
				token.setExpireTime(currTime+ACCESSTOKEN_VALIDITY);
				token.setJwtTpcd(TJwtType.ACCESS_TOKEN.code());
			}
			try {
				String json = new KTokenParser().format(token);
				String encoded = JwtHelper.encode(json, signer).getEncoded();
				accessToken = new DefaultOAuth2AccessToken(encoded);
				accessToken.setRefreshToken(refreshToken);
			} catch(Exception e) {
				throw new KSysException(KMessage.E6022, e, TJwtType.ACCESS_TOKEN.label());
			}
		}
		return accessToken;
	}


	public <T extends KToken> T convertToken(String encodedToken) throws Exception {
		// token decode 처리 (token > json)
		String json;
		{
			try {
				Jwt jwt = JwtHelper.decodeAndVerify(encodedToken, verifier);
				json = jwt.getClaims();
			} catch(Exception e) {
				throw new KSysException(KMessage.E6013, e, "token");
			}
		}

		T token = (T) cmmUserToken.parseToken(json);
		return token;
	}

	public void verifySignature(String tokenValue) throws KSysException {
		try {
			JwtHelper.decodeAndVerify(tokenValue, verifier);
		} catch(Exception e) {
			throw new KSysException(KMessage.E6013, e);
		}
	}

	public boolean verifyExpiration(KToken token) throws Exception {
		if(System.currentTimeMillis() > token.getExpireTime()) {
			TJwtType jwtType = TJwtType.get(token.getJwtTpcd());
			throw new KSysException(KMessage.E6018, jwtType);

		}
		return true;
	}
}
