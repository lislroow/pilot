package mgkim.framework.online.com.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
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
import org.springframework.web.util.ContentCachingResponseWrapper;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.type.TRequestType;
import mgkim.framework.core.type.TResponseType;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.scheduler.CmmApiTxLogScheduler;

@KBean(name = "access 로그 필터")
public class KFilterAccessLog  extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterAccessLog.class);

	@Autowired(required = true)
	private CmmApiTxLogScheduler cmmApiTxLogScheduler;


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		ReadableRequestWrapper requestWrapper = null;
		ContentCachingResponseWrapper responseWrapper = null;

		// `loggable` 이 아닌 경우에는 요청/응답에 대한 정보를 로깅하지 않음
		{
			boolean loggable = KContext.getT(AttrKey.LOGGABLE);
			if (!loggable) {
				chain.doFilter(request, response);
				return;
			}
		}

		// 1) request 로그
		TRequestType requestType = KContext.getT(AttrKey.REQUEST_TYPE);
		try {
			if (requestType == TRequestType.JSON) {
				requestWrapper = new ReadableRequestWrapper(request);
				String reqBody = requestWrapper.getBodyString();
				if (KStringUtil.isJson(reqBody)) {
					KLogSys.info("{} {}{} {}{}`InDTO` = {}{}`Authorization` = {}", KConstant.LT_REQ_BODY, KLogLayout.LINE, KConstant.LT_REQ_BODY, KContext.getT(AttrKey.URI), KLogLayout.LINE, reqBody, KLogLayout.LINE, KContext.getT(AttrKey.AUTHORIZATION));
				} else {
					KLogSys.warn("{} `{}`에서 request-body 가 json 이 아닙니다. request-body={}", KConstant.LT_SECURITY_FILTER, BEAN_NAME, reqBody);
				}
			} else {
				/*
				ServletFileUpload upload = new ServletFileUpload();
				upload.setSizeMax(KInitRoot.MAX_UPLOAD_SIZE);
				FileItemIterator iterator = null;
				try {
					iterator = upload.getItemIterator(request);
				} catch(FileUploadException e) {
					e.printStackTrace();
				}
				while (iterator != null && iterator.hasNext()) {
					FileItemStream item = null;
					try {
						item = iterator.next();
					} catch (FileUploadException e) {
						e.printStackTrace();
					}
					if (KStringUtil.isEmpty(item.getContentType())) {
						InputStream in = item.openStream();
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String a = br.readLine();
						System.out.println(a);
					}
				}
				*/
			}
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7008, e, BEAN_NAME, "요청"));
			return;
		} finally {
			responseWrapper = new ContentCachingResponseWrapper(response);
			if (requestType == TRequestType.JSON) {
				chain.doFilter(requestWrapper, responseWrapper);
			} else {
				chain.doFilter(request, responseWrapper);
			}
		}

		// 2) response 로그
		try {
			TResponseType responseType = KContext.getT(AttrKey.RESPONSE_TYPE);
			if (responseType == TResponseType.JSON) {
				String code = KContext.getT(AttrKey.RESULT_CODE);
				String rhMessage = KContext.getT(AttrKey.RESULT_MESSAGE);
				boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_REQ;
				if (isVerboss) {
					BufferedReader br = null;
					try {
						br = new BufferedReader(new InputStreamReader(responseWrapper.getContentInputStream()));
						String readLine = null;
						StringBuffer buf = new StringBuffer();
						while ((readLine = br.readLine()) != null) {
							buf.append(readLine);
						}
						KLogSys.info("{} [{}] {} (`{}` bytes){}{}{}`OutDTO` = {}", KConstant.LT_RES_INFO, code, rhMessage, responseWrapper.getContentSize(), KLogLayout.LINE, KConstant.LT_RES_VERBOSS, KLogLayout.LINE, buf.toString());
					} finally {
						if (br != null) {
							br.close();
						}
					}
				} else {
					String contentSize = MessageFormat.format("{0}", responseWrapper.getContentSize());
					KLogSys.info("{} [{}] {} (`{}` bytes)", KConstant.LT_RES_INFO, code, rhMessage, contentSize);
				}
			} else if (responseType == TResponseType.FILE) {
				String filename = KContext.getT(AttrKey.DOWN_FILE);
				String contentSize = MessageFormat.format("{0}", responseWrapper.getContentSize());
				KLogSys.info("{} download file=`{}` (`{}` bytes)", KConstant.LT_RES_INFO, filename, contentSize);
			} else {
				KLogSys.info("분류되지 않은 응답 형태 입니다.");
				KLogSys.info("{} Content-Type=`{}` (`{}` bytes)", KConstant.LT_RES_INFO, response.getContentType(), responseWrapper.getContentSize());
			}
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7008, e, BEAN_NAME, "응답"));
			return;
		} finally {
			// `api처리로그 스케줄러` 가 활성화 상태일 경우 실행
			{
				cmmApiTxLogScheduler.addLog();
			}
			responseWrapper.copyBodyToResponse(); // copy를 하지 않으면 빈 문자열을 response 하게 됩니다.
			//if (responseType == TResponseType.DTO) {
			//}
		}
	}

	@Override
	public void destroy() {
		/* nothing to do */
	}
}


class ReadableRequestWrapper extends HttpServletRequestWrapper {

	private static final Logger log = LoggerFactory.getLogger(ReadableRequestWrapper.class);

	private Charset encoding;
	private byte[] rawData;
	private String bodyString;

	public ReadableRequestWrapper(HttpServletRequest request) throws IOException {
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
		} catch(Exception e) {
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
