package mgkim.framework.online.com.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.util.ContentCachingResponseWrapper;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.request.KReadableRequest;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.type.KType.ApiType;
import mgkim.framework.core.type.KType.AuthType;
import mgkim.framework.core.type.KType.ReqType;
import mgkim.framework.core.type.KType.RespType;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.mgr.ComSessionStatusMgr;
import mgkim.framework.online.com.mgr.ComUriListMgr;
import mgkim.framework.online.com.mgr.ComUserSessionMgr;
import mgkim.framework.online.com.mgr.ComUserTokenMgr;
import mgkim.framework.online.com.scheduler.ComApiTxLogScheduler;
import mgkim.framework.online.com.scheduler.ComSessionStatusMngScheduler;

@KBean(name = "authenticate 필터")
public class KAuthenticateFilter extends KFilter {
	
	private static final Logger log = LoggerFactory.getLogger(KAuthenticateFilter.class);
	
	final String BEAN_NAME = KObjectUtil.name(KAuthenticateFilter.class);
	
	@Autowired(required = true)
	private ComUriListMgr comUriListMgr;
	@Autowired(required = true)
	private ComApiTxLogScheduler comApiTxLogScheduler;
	@Autowired(required = true)
	private ComUserTokenMgr comUserTokenMgr;
	@Autowired(required = true)
	private ComSessionStatusMgr comSessionStatusMgr;
	@Autowired(required = true)
	private ComUserSessionMgr comUserSessionMgr;
	@Autowired(required = false)
	private ComSessionStatusMngScheduler comSessionStatusMngScheduler;
	
	@Override
	public void afterPropertiesSet() throws ServletException {
		if (comApiTxLogScheduler == null) {
			log.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(ComApiTxLogScheduler.class)));
			return;
		}
	}
	
	final List<String> DEBUG_IP = Arrays.asList("172.28.", KHttpUtil.LOCAL_IPv4);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		String uri = KContext.getT(AttrKey.URI);
		
		// 1) resolve-uri
		try {
			HandlerMethod method = comUriListMgr.getHandlerMethod(request);
			if (method == null) {
				throw new KSysException(KMessage.E7001, uri);
			}
		} catch (HttpMediaTypeNotSupportedException e) {
			String contentType = request.getHeader("Content-Type");
			KException ke = new KSysException(KMessage.E7002, e, contentType);
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
		} catch (KException ke) {
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), ke.getCause());
			KExceptionHandler.response(response, ke);
			return;
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7007, e, "resolve-uri");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 2) preflight
		try {
			response.setHeader("Allow", "GET, POST, OPTIONS");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Headers", "ssid, txid, Origin, X-Requested-With, Content-Type, Accept, Access-Control-Allow-Method, Access-Control-Allow-Headers, Authorization, Access-Control-Max-Age");
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			response.setHeader("Access-Control-Expose-Headers", "Content-Dispostion");
			boolean isPreFlight = CorsUtils.isPreFlightRequest(request);
			if (isPreFlight) {
				return;
			}
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7007, e, "preflight");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 3) debug
		boolean debug = KContext.getT(AttrKey.DEBUG);
		try {
			// `debug` 여부 확인
			switch (KProfile.SYS) {
			case LOC:
				break;
			case DEV:
			case STA:
			case PROD:
			default:
				if (debug) {
					String ip = KContext.getT(AttrKey.IP);
					String matched = DEBUG_IP.stream()
							.filter(val -> ip.startsWith(val))
							.findFirst()
							.orElse(null);
					if (matched == null) {
						throw new KSysException(KMessage.E7009, KProfile.SYS);
					}
					log.warn(KLogMarker.security, "debug=Y");
				}
			}
		} catch (KException ke) {
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), ke.getCause());
			KExceptionHandler.response(response, ke);
			return;
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7007, e, "debug");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 4) request-logging
		ReqType reqType = KContext.getT(AttrKey.REQUEST_TYPE);
		KReadableRequest readableRequest = null;
		try {
			if (reqType == ReqType.JSON) {
				readableRequest = new KReadableRequest(request);
				String body = readableRequest.getBodyString();
				String header = KStringUtil.toJson(KHttpUtil.getHeaders());
				if (KStringUtil.isJson(body)) {
					log.trace(KLogMarker.request, "\nrequest-header = {}\nrequest-body = {}", header, body);
				} else {
					log.warn(KLogMarker.request, "request-body 가 json 형식이 아닙니다. request-body={}", body);
				}
			} else if (reqType == ReqType.QUERY) {
				String querystrings = KStringUtil.nvl(KHttpUtil.getRequest().getQueryString());
				Map<String, String> m = Arrays.stream(querystrings.split("&"))
						.collect(HashMap<String, String>::new,
								(map, qs) -> {
									if (qs.split("=").length == 2 ) {
										map.put(qs.split("=")[0], qs.split("=")[1]);
									}
								},
								HashMap<String, String>::putAll);
				String body = KStringUtil.toJson(m);
				String header = KStringUtil.toJson(KHttpUtil.getHeaders());
				log.trace(KLogMarker.request, "\nrequest-header = {}\nrequest-querystring(map) = {}", header, body);
			} else if (reqType == ReqType.FILE) {
			} else {
			}
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7008, e, "request-logging", "요청");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// `SecurityContextPersistenceFilter` 처리
		
		// 5) decode-token
		try {
			String bearer = null;
			ApiType apiType = KContext.getT(AttrKey.API_TYPE);
			AuthType authType = KContext.getT(AttrKey.AUTH_TYPE);
			
			// api타입별 유효한 인증타입(apikey, bearer) 검증
			
			// 인증타입별 accessToken 획득 (apikey 일 경우 DB에서 accessToken 조회 후 부가적인 검증 처리)
			switch (authType) {
			case BEARER:
				bearer = KContext.getT(AttrKey.BEARER);
				break;
			case APIKEY:
				break;
			case NOAUTH:
				break;
			}
			
			// accessToken 확인
			if (KStringUtil.isEmpty(bearer)) {
				throw new KSysException(KMessage.E6009);
			}
			
			// token 정보 저장 (KContext)
			io.jsonwebtoken.Jwt token = comUserTokenMgr.parsetoken(bearer);
			KContext.initToken(token);
		} catch (KException ke) {
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), ke.getCause());
			KExceptionHandler.response(response, ke);
			return;
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7007, e, "decode-token");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 6) verify-session
		try {
			if (!debug) {
				io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
				Map<String, Object> claims = (Map<String, Object>)token.getBody();
				boolean isLogin = comSessionStatusMgr.isLoginStatus(claims);
				if (isLogin == false) {
					throw new KSysException(KMessage.E6101);
				}
			}
		} catch (KException ke) {
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), ke.getCause());
			KExceptionHandler.response(response, ke);
			return;
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7008, e, "verify-session", "세션상태체크");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 7) verify-token
		String bearer = KContext.getT(AttrKey.BEARER);
		try {
			boolean isFileApi = uri.startsWith("/api/cmm/file/");
			if (!debug && !isFileApi) {
				io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
				comUserTokenMgr.checkExpired(token.getHeader());
				comUserTokenMgr.parsetoken(bearer);
			}
		} catch (KException ke) {
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), ke.getCause());
			KExceptionHandler.response(response, ke);
			return;
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7008, e, "verify-token", "token 체크");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 8) create-user-session
		try {
			io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
			Map<String, Object> claims = (Map<String, Object>)token.getBody();
			comUserSessionMgr.createUserSession(claims);
		} catch (KException ke) {
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), ke.getCause());
			KExceptionHandler.response(response, ke);
			return;
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7008, e, "create-user-session", "session 생성");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 9) update-session-expire
		try {
			if (!debug) {
				comSessionStatusMngScheduler.addSession();
			}
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7008, e, BEAN_NAME, "갱신대상추가");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 10) filter-chain
		ContentCachingResponseWrapper responseWrapper = null;
		try {
			responseWrapper = new ContentCachingResponseWrapper(response);
			if (reqType == ReqType.JSON) {
				chain.doFilter(readableRequest, responseWrapper);
			} else {
				chain.doFilter(request, responseWrapper);
			}
		} catch (AuthenticationCredentialsNotFoundException | AccessDeniedException e) {
			KException ke = new KSysException(KMessage.E6011, e);
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		} catch (MultipartException e) {
			KException ke = new KSysException(KMessage.E7005, e);
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E6011, e);
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		
		// 11) reponse-logging
		try {
			String elapsed = null;
			if (KContext.getT(AttrKey.API_TYPE) == ApiType.API) {
				long reqTime = KContext.getT(AttrKey.REQ_TIME);
				elapsed = String.format("%.3f", (System.currentTimeMillis() - reqTime) / 1000.0);
			}
			RespType respType = KContext.getT(AttrKey.RESPONSE_TYPE);
			long contentSize = responseWrapper.getContentSize();
			if (respType == RespType.JSON) {
				String resultCode = KContext.getT(AttrKey.RESULT_CODE);
				String resultMessage = KContext.getT(AttrKey.RESULT_MESSAGE);
				BufferedReader br = null;
				try {
					if (debug) {
						br = new BufferedReader(new InputStreamReader(responseWrapper.getContentInputStream()));
						String readLine = null;
						StringBuffer buf = new StringBuffer();
						while ((readLine = br.readLine()) != null) {
							buf.append(readLine);
						}
						log.trace(KLogMarker.response, "[{}] {} (elapsed={}, bytes={})\nresponse-body = {}", resultCode, resultMessage, elapsed, contentSize, buf.toString());
					} else {
						log.info(KLogMarker.response, "[{}] {} (elapsed={}, bytes={})", resultCode, resultMessage, elapsed, contentSize);
					}
				} finally {
					if (br != null) {
						br.close();
					}
				}
			} else if (respType == RespType.FILE) {
				String filename = KContext.getT(AttrKey.DOWN_FILE);
				log.info(KLogMarker.response, "download file=`{}` (elapsed={}, bytes={})", filename, elapsed, contentSize);
			} else {
				log.info("분류되지 않은 응답 형태 입니다.");
				log.info(KLogMarker.response, "Content-Type=`{}` (elapsed={}, bytes={})", response.getContentType(), elapsed, contentSize);
			}
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7008, e, "reponse-logging", "응답");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		} finally {
			// `api처리로그 스케줄러` 가 활성화 상태일 경우 실행
			comApiTxLogScheduler.addLog();
			responseWrapper.copyBodyToResponse(); // copy를 하지 않으면 빈 문자열을 response 하게 됩니다.
		}
	}
}
