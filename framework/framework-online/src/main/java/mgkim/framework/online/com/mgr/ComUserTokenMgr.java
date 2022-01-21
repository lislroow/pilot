package mgkim.framework.online.com.mgr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.dto.KOAuthToken;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.type.KType.UuidType;
import mgkim.framework.core.util.KStringUtil;

@KBean(name = "사용자 토큰 관리")
public class ComUserTokenMgr {

	private static final byte[] SECRETKEY = "helloworld".getBytes();
	private static final io.jsonwebtoken.SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS256;
	private static final java.security.Key SIGNER = new javax.crypto.spec.SecretKeySpec(
			SECRETKEY, ALGORITHM.getJcaName()
		);
	
	public static final String JWT_HEADER_TYPE = "typ";
	public static final String JWT_HEADER_EXPIRATION = "exp";
	
	public enum FwJwtType {
		accessToken, refreshToken;
	}
	
	public static int ACCESS_TOKEN_EXPIRE_SEC_DEFAULT = 7200;
	public static int ACCESS_TOKEN_EXPIRE_SEC = 7200;
	public static int REFRESH_TOKEN_EXPIRE_SEC_DEFAULT = 86400;
	public static int REFRESH_TOKEN_EXPIRE_SEC = 86400;
	
	public static final String API_TOKEN_CLAIMS = "{\"ssid\": \"\", \"ip\": \"\", \"userId\": \"\", \"userType\": \"\", \"elvtBizSysTpcd\": \"10\", \"loginCertTpcd\": \"I\", \"elvtConnTpcd\": \"01\", \"elvtShrNo\": \"\", \"agentCustno\": -1, \"autoCertYn\": \"N\", \"linkSeccoCd\": \"000\"}";
	public static final String OPENAPI_TOKEN_CLAIMS = "{\"ssid\": \"\", \"ip\": \"\", \"userId\": \"\", \"userType\": \"\", \"aesKey\": \"\", \"orgCd\": \"\", \"orgNm\": \"\"}";
	
	public KOAuthToken createOauthToken(Map<String, Object> claims) throws Exception {
		KOAuthToken kOAuthToken = new KOAuthToken();
		
		// token `header` 설정
		Map header = new HashMap();
		long curr = System.currentTimeMillis();
		
		// 토큰ID(`jti`) 생성: access-token 과 refresh-token 의 jti 는 같도록 설정함
		claims.put(KConstant.SSID, KStringUtil.createUuid(true, UuidType.SSID));
		
		// access-token 생성
		header.put(JWT_HEADER_TYPE, FwJwtType.accessToken);
		header.put(JWT_HEADER_EXPIRATION, new Date(curr + ACCESS_TOKEN_EXPIRE_SEC * 1000L).getTime());
		kOAuthToken.setAccessToken(createToken(header, claims));
		
		// refresh-token 생성
		header.clear();
		header.put(JWT_HEADER_TYPE, FwJwtType.refreshToken);
		header.put(JWT_HEADER_EXPIRATION, new Date(curr + REFRESH_TOKEN_EXPIRE_SEC * 1000L).getTime());
		kOAuthToken.setRefreshToken(createToken(header, claims));
		
		return kOAuthToken;
	}
	
	public static io.jsonwebtoken.Jwt parsetoken(String accessToken) throws Exception {
		// access-token parsing
		io.jsonwebtoken.Jwt jwtAccessToken = null;
		try {
			jwtAccessToken = parse(accessToken);
		} catch (MalformedJwtException e) {
			//
		} catch (SignatureException e) {
			//
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map headerAccessToken = (Map) jwtAccessToken.getHeader();
		
		// access-token 검증
		{
			// `typ` 값이 `accessToken` 인지 확인
			if (resolveType(headerAccessToken) != FwJwtType.accessToken) {
				//
			}
			
			// `exp` 값이 현재보다 큰 값인지 확인
			if (checkExpired(headerAccessToken)) {
				//
			}
		}
		return jwtAccessToken;
	}
	
	public static String refreshtoken(String refreshToken) throws Exception {
		String accessToken = null;
		
		// access-token `header` 설정
		Map headerAccessToken = new HashMap();
		long curr = System.currentTimeMillis();
		headerAccessToken.put(JWT_HEADER_TYPE, FwJwtType.accessToken);
		headerAccessToken.put(JWT_HEADER_EXPIRATION, new Date(curr + ACCESS_TOKEN_EXPIRE_SEC * 1000L).getTime());
		
		// refresh-token parsing
		Jwt jwtRefreshToken = null;
		try {
			jwtRefreshToken = parse(refreshToken);
		} catch (MalformedJwtException e) {
			//
		} catch (SignatureException e) {
			//
		}
		Map headerRefreshToken = (Map) jwtRefreshToken.getHeader();
		Map claims = (Map) jwtRefreshToken.getBody();
		
		// refresh-token 검증
		{
			// `typ` 값이 `refreshToken` 인지 확인
			if (resolveType(headerRefreshToken) != FwJwtType.refreshToken) {
				//
			}
			
			// `exp` 값이 현재보다 큰 값인지 확인
			if (checkExpired(headerRefreshToken)) {
				//
			}
		}
		
		// access-token `encoded string` 생성
		accessToken = createToken(headerAccessToken, claims);
		
		return accessToken;
	}
	
	public static FwJwtType resolveType(Map tokenHeader) throws Exception {
		String typ = KStringUtil.nvl(tokenHeader.get(JWT_HEADER_TYPE), "");
		FwJwtType[] values = FwJwtType.values();
		for (FwJwtType type : values) {
			if (type.toString().equals(typ)) {
				return type;
			}
		}
		return null;
	}
	
	public static boolean checkExpired(Map tokenHeader) throws Exception {
		// token 검증
		long curr = System.currentTimeMillis();
		String exp = KStringUtil.nvl(tokenHeader.get(JWT_HEADER_EXPIRATION), "");
		// verifying-token-expired: `exp` 값이 현재보다 큰 값인지 확인
		if ((curr - Long.parseLong(exp)) > 0) {
			return true;
		}
		return false;
	}
	
	private static String createId(boolean isShorten) {
		String result = null;
		String uuid = UUID.randomUUID().toString();
		
		// isShorten 일 경우 32 bytes 를 13 bytes 로 변환
		// 13 bytes 는 36 진수로 변환 시 최대 bytes 가 되며, fixed-length 를 유지하기 위해
		// 오른쪽에 '0'을 붙이도록 함
		if (isShorten) {
			String str = Long.toString(
					java.nio.ByteBuffer.wrap(uuid.getBytes()).getLong()
					, Character.MAX_RADIX);
			result = KStringUtil.rpad(str, 13, "0");
		}
		return result;
	}
	
	private static String createId() {
		return createId(true);
	}
	
	private static String createToken(Map header, Map body) {
		String result = null;
		{
			// header 예시 {"typ":"accessToken","exp":1637892840616,"alg":"HS256"}
			// claim(body) 예시 {"user_id":"dit_mg01","ssid":"s162puasd0110"}
			JwtBuilder builder = Jwts.builder()
					.setHeader(header)
					.setClaims(body)
					.signWith(ALGORITHM, SIGNER);
			result = builder.compact();
		}
		return result;
	}
	
	private static Jwt parse(String token) throws ExpiredJwtException, MalformedJwtException, SignatureException {
		Jwt jwt = Jwts.parser()
				.setSigningKey(SECRETKEY)
				.parseClaimsJws(token);
		return jwt;
	}
}
