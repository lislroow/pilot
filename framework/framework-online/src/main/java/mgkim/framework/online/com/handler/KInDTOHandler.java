package mgkim.framework.online.com.handler;

import java.lang.reflect.Type;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.type.TApiType;
import mgkim.framework.online.cmm.CmmDtoLog;
import mgkim.framework.online.cmm.dtohandler.CmmDtoHandler;
import mgkim.framework.online.com.dto.KInDTO;
import mgkim.framework.online.com.dto.KInPageDTO;
import mgkim.framework.online.com.dto.KInPageVO;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KProfile;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.logging.KLogSys;
import mgkim.framework.online.com.mgr.ComFieldCryptorMgr;
import mgkim.framework.online.com.util.KObjectUtil;

@RestControllerAdvice(basePackages=KProfile.GROUP)
public class KInDTOHandler extends RequestBodyAdviceAdapter implements InitializingBean {

	@Autowired(required = false)
	private ComFieldCryptorMgr comFieldCryptorMgr;

	@Autowired(required = false)
	private CmmDtoLog cmmDtoLog;

	@Autowired(required = false)
	private CmmDtoHandler cmmDtoHandler;

	@Override
	public void afterPropertiesSet() throws Exception {
		if(comFieldCryptorMgr == null) {
			throw new KSysException(KMessage.E5001, KObjectUtil.name(ComFieldCryptorMgr.class));
		}
		if(cmmDtoLog == null) {
			KLogSys.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmDtoLog.class)));
		}
		if(cmmDtoHandler == null) {
			KLogSys.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmDtoHandler.class)));
		}
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return methodParameter.hasParameterAnnotation(RequestBody.class);
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		if(body instanceof KInDTO) {
			KInDTO<?> inDTO = (KInDTO<?>)body;

			// `page` 입력값 저장
			if(body instanceof KInPageDTO) {
				KInPageDTO<?> inPageDTO = (KInPageDTO<?>)body;
				KInPageVO inPageVO = inPageDTO.getPage();
				KContext.set(AttrKey.IN_PAGE, inPageVO);
			}

			// dto 저장: 특정 필드가 포함된 dto일 경우 저장
			{
				if(cmmDtoLog != null) {
					try {
						cmmDtoLog.logging(inDTO);
					} catch(Exception e) {
						KException exception = KExceptionHandler.resolve(e);
						KExceptionHandler.print(exception);
					}
				}
			}

			// bodyVO 필드 복호화: bodyVO 에 `@KEncrypt` 가 선언된 필드를 복호화함
			if(comFieldCryptorMgr != null) {
				try {
					String uri = KContext.getT(AttrKey.URI);

					TApiType apiType = KContext.getT(AttrKey.API_TYPE);
					switch(apiType) {
					case API:
					case API2:
					case INTERAPI:
					case OPENAPI:
						comFieldCryptorMgr.decrypt(inDTO);
						break;
					case PUBLIC:
					default:
						break;
					}
				} catch(Exception e) {
					KException exception = KExceptionHandler.resolve(e);
					KExceptionHandler.print(exception);
				}
			}

			// 업무서비스에서 정의한 컨트롤러 전처리 호출
			{
				try {
					if(cmmDtoHandler != null) {
						cmmDtoHandler.preProcess(inDTO);
					}
				} catch(Exception e) {
					KException exception = KExceptionHandler.resolve(e);
					KExceptionHandler.print(exception);
				}
			}
		}
		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}

}
