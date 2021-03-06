package mgkim.framework.online.com.handler;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import mgkim.framework.cmm.online.CmmDtoHandler;
import mgkim.framework.cmm.online.CmmDtoLog;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KInPageDTO;
import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.KType.ApiType;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.mgr.ComFieldCryptorMgr;

@RestControllerAdvice(basePackages=KProfile.BASE_PACKAGE)
public class KInDTOHandler extends RequestBodyAdviceAdapter implements InitializingBean {
	
	private static final Logger log = LoggerFactory.getLogger(KInDTOHandler.class);

	@Autowired(required = false)
	private ComFieldCryptorMgr comFieldCryptorMgr;

	@Autowired(required = false)
	private CmmDtoLog cmmDtoLog;

	@Autowired(required = false)
	private CmmDtoHandler cmmDtoHandler;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (comFieldCryptorMgr == null) {
			throw new KSysException(KMessage.E5001, KObjectUtil.name(ComFieldCryptorMgr.class));
		}
		if (cmmDtoLog == null) {
			log.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmDtoLog.class)));
		}
		if (cmmDtoHandler == null) {
			log.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmDtoHandler.class)));
		}
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return methodParameter.hasParameterAnnotation(RequestBody.class) || methodParameter.hasParameterAnnotation(RequestParam.class);
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		if (body instanceof KInDTO) {
			log.info(KLogMarker.request, "\ninDTO = {}", KStringUtil.toJson(body));
			KInDTO<?> inDTO = (KInDTO<?>)body;

			// `page` ????????? ??????
			if (body instanceof KInPageDTO) {
				KInPageDTO<?> inPageDTO = (KInPageDTO<?>)body;
				KInPageVO inPageVO = inPageDTO.getPage();
				KContext.set(AttrKey.IN_PAGE, inPageVO);
			}

			// dto ??????: ?????? ????????? ????????? dto??? ?????? ??????
			{
				if (cmmDtoLog != null) {
					try {
						cmmDtoLog.logging(inDTO);
					} catch(Exception e) {
						KException exception = KExceptionHandler.translate(e);
						log.error(KLogMarker.error, "{} {}", exception.getId(), exception.getText(), e);
					}
				}
			}

			// bodyVO ?????? ?????????: bodyVO ??? `@KEncrypt` ??? ????????? ????????? ????????????
			if (comFieldCryptorMgr != null) {
				try {
					ApiType apiType = KContext.getT(AttrKey.API_TYPE);
					switch(apiType) {
					case API:
						comFieldCryptorMgr.decrypt(inDTO);
						break;
					case PUBLIC:
					default:
						break;
					}
				} catch(Exception e) {
					KException exception = KExceptionHandler.translate(e);
					log.error(KLogMarker.error, "{} {}", exception.getId(), exception.getText(), e);
				}
			}

			// ????????????????????? ????????? ???????????? ????????? ??????
			{
				try {
					if (cmmDtoHandler != null) {
						cmmDtoHandler.preProcess(inDTO);
					}
				} catch(Exception e) {
					KException exception = KExceptionHandler.translate(e);
					log.error(KLogMarker.error, "{} {}", exception.getId(), exception.getText(), e);
				}
			}
		}
		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}

}
