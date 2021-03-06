package mgkim.framework.core.exception;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.env.KSqlContext;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.KType.RespType;
import mgkim.framework.core.type.KType.TEncodingType;
import mgkim.framework.core.util.KExceptionUtil;

@KBean
public class KExceptionHandler extends ExceptionHandlerExceptionResolver {
	
	private static final Logger log = LoggerFactory.getLogger(KExceptionHandler.class);

	@Override
	protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response,
			HandlerMethod handlerMethod, Exception e) {
		KException ke = null;
		// 2022.01.22 spring 에서 발생한 오류 여부 판단
		if (e.getClass().getName().startsWith("org.springframework.web")) {
			ke = new KSysException(KMessage.E7010, e, e.getMessage());
			ke = KExceptionHandler.translate(ke);
		} else {
			ke = KExceptionHandler.translate(e);
		}
		log.error(KLogMarker.error, "[{}] {}", ke.getId(), ke.getText(), e);
		response(response, ke);
		return new ModelAndView();
		//return null;	// 2022.01.20 null 일 경우 
						// HandlerExceptionResolverComposite.resolveException(..) 에서 null 이 아닐때까지
						// ExceptionHandlerExceptionResolver 를 상속받은 doResolveException(..) 호출
						// DefaultHandlerExceptionResolver.doResolveException(..) 까지 호출되어 불필요한 처리가 발생함
	}
	
	public static void response(HttpServletResponse response, Exception ex) {
		// 1) `exception` 분석
		{
			KExceptionHandler.translate(ex);
		}
		
		// 2) `KContext` 정보 가져오기
		String code = KContext.getT(AttrKey.RESULT_CODE);
		String message = KContext.getT(AttrKey.RESULT_MESSAGE);
		String text = KContext.getT(AttrKey.RESULT_TEXT);  // `KExceptionHandler.resolve(ex);` 에서 이미 String 으로 변환 되어있음
		String txid = KContext.getT(AttrKey.TXID);
		
		// 3) `outDTO` 생성
		KOutDTO<?> outDTO = null;
		{
			outDTO = new KOutDTO<>();
			Map<String, Object> headerMap = new LinkedHashMap<String, Object>();
			headerMap.put(KConstant.TXID, txid);
			headerMap.put(KConstant.RESULT_CODE, code);
			headerMap.put(KConstant.RESULT_MESSAGE, message);
			headerMap.put(KConstant.RESULT_TEXT, text);
			headerMap.put(KConstant.APP_VER, KProfile.APP_VER);
			outDTO.setHeader(headerMap);
		}
		
		// 4) `response` 설정
		{
			int sc = 500;
			if (code.startsWith("E6")) {
				sc = 403;
			} else if (code.startsWith("E7")) {
				sc = 404;
			}
			response.setStatus(sc);
			response.setCharacterEncoding(TEncodingType.UTF8.code());
		}
		
		// 5) `response` 응답
		{
			ObjectMapper objectMapper = new ObjectMapper();
			MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
			jsonConverter.setPrettyPrint(true);
			MediaType mediaType = MediaType.APPLICATION_JSON;
			if (jsonConverter.canWrite(outDTO.getClass(), mediaType)) {
				try {
					jsonConverter.write(outDTO, mediaType, new ServletServerHttpResponse(response));
				} catch (HttpMessageNotWritableException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				KContext.set(AttrKey.RESPONSE_TYPE, RespType.JSON);
			}
		}
	}
	
	
	public static KException translate(Exception ex) {
		/*
		- 예외 객체를 분류하고 KExeption 형태로 반환
		- KException 에는 분류에 의해 추가된 KMessage 가 포함되어 있음
		- 예외 정보를 출력하지 않은 것은 예외처리의 마지막 클래스에서 항상 출력되므로
		  분류만 해야할 경우에는 중복으로 출력되기 때문에 출력하지 않음
		*/
		
		// 1) `exception` 매핑: 알려진 예외 클래스
		KException exception = null;
		{
			// 1.1) `exception` 클래스명 확인
			String clazzName = ex.getClass().getName();
			
			// 1.2) `exception` 매핑: 알려진 예외 클래스
			if ("mgkim.framework.core.exception.KException".equals(clazzName)) {
				exception = (KException) ex;
			} else if ("mgkim.framework.core.exception.KSqlException".equals(clazzName)) {
				exception = (KException) ex;
			} else if ("mgkim.framework.core.exception.KSysException".equals(clazzName)) {
				exception = (KException) ex;
				if ("".equals(exception.getId())) {
					exception = new KException(KMessage.E9998);
				}
			} else if ("org.springframework.http.converter.HttpMessageNotReadableException".equals(clazzName)) {
				exception = new KException(KMessage.E9001, ex);
			} else if ("org.springframework.http.converter.HttpMessageNotWritableException".equals(clazzName)) {
				exception = new KException(KMessage.E9001, ex);
			} else if ("org.springframework.web.bind.MethodArgumentNotValidException".equals(clazzName)) {
				exception = new KException(KMessage.E9002, ex);
			} else if ("org.springframework.jdbc.UncategorizedSQLException".equals(clazzName)) {
				exception = new KSqlException(KMessage.E8001, ex, KContext.getT(AttrKey.SQL_ID), KContext.getT(AttrKey.SQL_TEXT), KContext.getT(AttrKey.SQL_FILE));
			} else if ("java.sql.SQLSyntaxErrorException".equals(clazzName)) {
				exception = new KSqlException(KMessage.E8002, ex, KContext.getT(AttrKey.SQL_ID), KContext.getT(AttrKey.SQL_TEXT), KContext.getT(AttrKey.SQL_FILE));
			} else if ("org.springframework.jdbc.BadSqlGrammarException".equals(clazzName)) {
				exception = new KSqlException(KMessage.E8003, ex, KContext.getT(AttrKey.SQL_ID), KContext.getT(AttrKey.SQL_TEXT), KContext.getT(AttrKey.SQL_FILE));
			} else if ("org.springframework.dao.DuplicateKeyException".equals(clazzName)) {
				exception = new KSqlException(KMessage.E8004, ex, KContext.getT(AttrKey.SQL_ID), KContext.getT(AttrKey.SQL_TEXT), KContext.getT(AttrKey.SQL_FILE));
			} else if ("org.apache.ibatis.binding.BindingException".equals(clazzName)) {
				exception = new KSqlException(KMessage.E8005, ex, KContext.getT(AttrKey.SQL_ID), KContext.getT(AttrKey.SQL_TEXT), KContext.getT(AttrKey.SQL_FILE));
			} else if ("org.mybatis.spring.MyBatisSystemException".equals(clazzName)) {
				String causeClazzName = ex.getCause().getClass().getName();
				if ("org.apache.ibatis.reflection.ReflectionException".equals(causeClazzName)) {
					String sqlId = KContext.getT(AttrKey.SQL_ID);
					String sqlFile = KSqlContext.getSqlFile(sqlId);
					exception = new KSqlException(KMessage.E8006, ex, KContext.getT(AttrKey.SQL_ID), ex.getCause().getMessage(), sqlFile);
				}
			}
			
			// 1.3) `exception` 매핑: `ORA-` 메시지가 포함된 예외 메시지 분석
			if (exception == null) {
				String causeMessage = KExceptionUtil.getCauseMessage(ex);
				if (causeMessage.startsWith("ORA-")) {
					exception = new KSqlException(KMessage.E8002, ex, KContext.getT(AttrKey.SQL_ID), KContext.getT(AttrKey.SQL_TEXT), KContext.getT(AttrKey.SQL_FILE));
				}
			}
			
			// 1.4) `exception` 매핑: 그 밖에 예외
			if (exception == null) {
				exception = new KSysException(KMessage.E9999, ex, ex.getMessage());
			}
		}
		
		// 2) `exception` KContext 저장
		Object resultException = null;
		{
			KContext.set(AttrKey.RESULT_CODE, exception.getId());
			KContext.set(AttrKey.RESULT_MESSAGE, exception.getText());
			resultException = exception.cause();
			if (resultException instanceof Throwable) {
				resultException = KExceptionUtil.getTrace((Throwable)resultException, false);
			}
			KContext.set(AttrKey.RESULT_TEXT, resultException);
		}
		
		return exception;
	}
}
