package mgkim.framework.online.com.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.type.TApiType;
import mgkim.framework.core.type.TAuthType;
import mgkim.framework.core.type.TRequestType;
import mgkim.framework.core.type.TResponseType;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.cmm.CmmUserToken;
import mgkim.framework.online.com.mgr.ComSessionStatusMgr;
import mgkim.framework.online.com.mgr.ComUriListMgr;
import mgkim.framework.online.com.mgr.ComUserSessionMgr;
import mgkim.framework.online.com.mgr.ComUserTokenMgr;
import mgkim.framework.online.com.scheduler.CmmApiTxLogScheduler;
import mgkim.framework.online.com.scheduler.ComSessionStatusMngScheduler;

@KBean(name = "api 필터")
public class KFilterApi extends KFilter {
	
	private static final Logger log = LoggerFactory.getLogger(KFilterApi.class);
	
	final String BEAN_NAME = KObjectUtil.name(KFilterApi.class);
	
	@Autowired(required = true)
	private ComUriListMgr comUriListMgr;
	@Autowired(required = true)
	private CmmApiTxLogScheduler cmmApiTxLogScheduler;
	@Autowired(required = true)
	private ComUserTokenMgr comUserTokenMgr;
	@Autowired(required = true)
	private CmmUserToken cmmUserToken;
	@Autowired(required = true)
	private ComSessionStatusMgr comSessionStatusMgr;
	@Autowired(required = true)
	private ComUserSessionMgr comUserSessionMgr;
	@Autowired(required = false)
	private ComSessionStatusMngScheduler comSessionStatusMngScheduler;
	
	@Override
	public void afterPropertiesSet() throws ServletException {
		if (cmmUserToken == null) {
			log.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmUserToken.class)));
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
				throw new KSysException(KMessage.E7001);
			}
			// `REQUEST_TYPE` 결정
			String contentType = KStringUtil.nvl(request.getContentType());
			if (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
				KContext.set(AttrKey.REQUEST_TYPE, TRequestType.JSON);
			} else {
				KContext.set(AttrKey.REQUEST_TYPE, TRequestType.FILE);
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
		TRequestType requestType = KContext.getT(AttrKey.REQUEST_TYPE);
		KReadableRequestWrapper requestWrapper = null;
		try {
			if (requestType == TRequestType.JSON) {
				requestWrapper = new KReadableRequestWrapper(request);
				String body = requestWrapper.getBodyString();
				String header = KStringUtil.toJson(KHttpUtil.getHeaders());
				if (KStringUtil.isJson(body)) {
					log.trace(KLogMarker.request, "\nrequest-header = {}\nrequest-body = {}", header, body);
				} else {
					log.warn(KLogMarker.request, "request-body 가 json 형식이 아닙니다. request-body={}", body);
				}
			} else {
				//ServletFileUpload upload = new ServletFileUpload();
				//upload.setSizeMax(KInitRoot.MAX_UPLOAD_SIZE);
				//FileItemIterator iterator = null;
				//try {
				//	iterator = upload.getItemIterator(request);
				//} catch (FileUploadException e) {
				//	e.printStackTrace();
				//}
				//while (iterator != null && iterator.hasNext()) {
				//	FileItemStream item = null;
				//	try {
				//		item = iterator.next();
				//	} catch (FileUploadException e) {
				//		e.printStackTrace();
				//	}
				//	if (KStringUtil.isEmpty(item.getContentType())) {
				//		InputStream in = item.openStream();
				//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
				//		String a = br.readLine();
				//		System.out.println(a);
				//	}
				//}
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
			TApiType apiType = KContext.getT(AttrKey.API_TYPE);
			TAuthType authType = KContext.getT(AttrKey.AUTH_TYPE);
			
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
			if (requestType == TRequestType.JSON) {
				chain.doFilter(requestWrapper, responseWrapper);
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
			TResponseType responseType = KContext.getT(AttrKey.RESPONSE_TYPE);
			long contentSize = responseWrapper.getContentSize();
			if (responseType == TResponseType.JSON) {
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
						log.trace(KLogMarker.response, "[{}] {} (bytes={})\nresponse-body = {}", resultCode, resultMessage, contentSize, buf.toString());
					} else {
						log.info(KLogMarker.response, "[{}] {} (bytes={})", resultCode, resultMessage, contentSize);
					}
				} finally {
					if (br != null) {
						br.close();
					}
				}
			} else if (responseType == TResponseType.FILE) {
				String filename = KContext.getT(AttrKey.DOWN_FILE);
				log.info(KLogMarker.response, "download file=`{}` (`{}` bytes)", filename, contentSize);
			} else {
				log.info("분류되지 않은 응답 형태 입니다.");
				log.info(KLogMarker.response, "Content-Type=`{}` (`{}` bytes)", response.getContentType(), contentSize);
			}
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7008, e, "reponse-logging", "응답");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		} finally {
			// `api처리로그 스케줄러` 가 활성화 상태일 경우 실행
			cmmApiTxLogScheduler.addLog();
			responseWrapper.copyBodyToResponse(); // copy를 하지 않으면 빈 문자열을 response 하게 됩니다.
		}
	}
}

class KReadableRequestWrapper extends HttpServletRequestWrapper {

	private static final Logger log = LoggerFactory.getLogger(KReadableRequestWrapper.class);

	private Charset encoding;
	private byte[] rawData;
	private String bodyString;

	public KReadableRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);

		if (request.getContentType() != null && request.getContentType().contains(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
			return;
		}

		String reqEncoding = request.getCharacterEncoding();
		this.encoding = StringUtils.isBlank(request.getCharacterEncoding()) ? StandardCharsets.UTF_8 : Charset.forName(reqEncoding);

		BufferedReader reader = null;
		try {
			InputStream in = request.getInputStream();
			this.rawData = IOUtils.toByteArray(in);
			reader = this.getReader();
			bodyString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.rawData);
		return new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(this.getInputStream(), this.encoding));
	}

	public String getBodyString() {
		return bodyString;
	}

	public void setBodyString(String bodyString) {
		this.bodyString = bodyString;
	}
}
