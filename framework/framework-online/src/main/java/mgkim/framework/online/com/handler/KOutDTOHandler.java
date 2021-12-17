package mgkim.framework.online.com.handler;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import mgkim.framework.online.cmm.dtohandler.CmmDtoHandler;
import mgkim.framework.online.com.dto.KOutDTO;
import mgkim.framework.online.com.dto.KOutPageDTO;
import mgkim.framework.online.com.dto.KOutPageVO;
import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KProfile;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.exception.KException;
import mgkim.framework.online.com.exception.KExceptionHandler;
import mgkim.framework.online.com.exception.KMessage;
import mgkim.framework.online.com.exception.KSysException;
import mgkim.framework.online.com.logging.KLogSys;
import mgkim.framework.online.com.mgr.ComFieldCryptorMgr;
import mgkim.framework.online.com.type.TApiType;
import mgkim.framework.online.com.type.TAuthType;
import mgkim.framework.online.com.type.TResponseType;
import mgkim.framework.online.com.util.KObjectUtil;
import mgkim.framework.online.com.util.KStringUtil;

@RestControllerAdvice(basePackages=KProfile.GROUP)
public class KOutDTOHandler extends AbstractMappingJacksonResponseBodyAdvice implements InitializingBean {

	public static final boolean HIDEONERROR_ENABLED = false;
	public static final String HIDEONERROR_TEXT = "처리 중 오류가 발생했습니다.";
	public static final List<String> HIDEONERROR_LIST = Arrays.asList(
			  "E8001"
			, "E8002"
			, "E8003"
			, "E8004"
			, "E8005"
			, "E8006"
			);

	@Autowired(required = false)
	private CmmDtoHandler cmmDtoHandler;

	@Autowired(required = false)
	private ComFieldCryptorMgr comFieldCryptorMgr;

	@Override
	public void afterPropertiesSet() throws Exception {
		if(comFieldCryptorMgr == null) {
			throw new KSysException(KMessage.E5001, KObjectUtil.name(ComFieldCryptorMgr.class));
		}
		if(cmmDtoHandler == null) {
			KLogSys.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmDtoHandler.class)));
			return;
		}
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return super.supports(returnType, converterType);
	}

	@Override
	protected MappingJacksonValue getOrCreateContainer(Object body) {
		return super.getOrCreateContainer(body);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public KOutDTO<?> process(HttpServletRequest request, Exception ex) {
		KException exception = KExceptionHandler.resolve(ex);
		KExceptionHandler.print(exception);

		KOutDTO<?> outDTO = new KOutDTO<>();
		return outDTO;
	}

	@Override
	protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType,
			MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
		// `RESPONSE_TYPE` 결정
		{
			KContext.set(AttrKey.RESPONSE_TYPE, TResponseType.JSON);
		}

		Map<String, Object> headerMap = new LinkedHashMap<String, Object>();

		Object body = bodyContainer.getValue();
		if(body instanceof KOutDTO) {
			KOutDTO<?> outDTO = (KOutDTO<?>) body;

			// `page` 처리값 저장
			if(body instanceof KOutPageDTO) {
				KOutPageDTO<?> outPageDTO = (KOutPageDTO<?>)body;
				KOutPageVO outPageVO = KContext.getT(AttrKey.OUT_PAGE);
				outPageDTO.setPage(outPageVO);
			}

			outDTO.setHeader(headerMap);

			boolean error = false;

			// bodyVO 필드 암호화: bodyVO 에 `@KEncrypt` 가 선언된 필드를 암호화함
			{
				if(comFieldCryptorMgr != null) {
					try {
						TApiType apiType = KContext.getT(AttrKey.API_TYPE);
						switch(apiType) {
						case API:
						case INTERAPI:
						case OPENAPI:
						case API2:
							comFieldCryptorMgr.encrypt(outDTO);
							break;
						case PUBLIC:
						default:
							break;
						}
					} catch(Exception e) {
						KExceptionHandler.resolve(e);
						error = true;
					}
				}
			}

			// 업무서비스에서 정의한 컨트롤러 후처리 호출
			if(cmmDtoHandler != null && !error) {
				try {
					cmmDtoHandler.postProcess(outDTO);
				} catch(Exception e) {
					KExceptionHandler.resolve(e);
					error = true;
				}
			}


			String txid = KContext.getT(AttrKey.TXID);
			String code = KContext.getT(AttrKey.RESULT_CODE);
			String message = null;
			Object text = null;
			String bcode = KContext.getT(AttrKey.RESULT_BCODE);
			String bmessage = KContext.getT(AttrKey.RESULT_BMESSAGE);

			boolean isError;
			boolean isHideOnError = false; // @condition

			// headerVO 필드값 준비: `KContext` 에서 처리 결과 정보 가져오기
			{
				if(KStringUtil.isEmpty(code) || KMessage.E0000.name().equals(code)) {
					code = KMessage.E0000.name();
					message = KMessage.E0000.text();
					isError = false;
				} else {
					// 응답 headerVO에 resultMessage 를 숨길지에 대한 여부
					//isHideOnError = HIDEONERROR_ENABLED && HIDEONERROR_LIST.contains(code);
					isHideOnError = HIDEONERROR_ENABLED;
					if(isHideOnError) {
						message = HIDEONERROR_TEXT;
						text = null;
					} else {
						message = KContext.getT(AttrKey.RESULT_MESSAGE);
						text = KContext.getT(AttrKey.RESULT_TEXT);
					}
					isError = true;
				}
			}

			// 시스템 표준 로그에 `RES-INFO` 에 표시할 수 있도록 KContext 에 설정
			{
				KContext.set(AttrKey.RESULT_CODE, code);
				KContext.set(AttrKey.RESULT_MESSAGE, message);
			}


			// headerVO 필드값 설정: 페이징 여부에 따른 headerVO 객체 생성 및 처리 결과 정보 설정
			{
				// header.txid
				headerMap.put(KConstant.TXID, txid);

				// header.resultCode: 처리 결과 코드 `E0000`을 제외한 나머지는 에러가 발생한 것으로 처리합니다.
				// header.resultMessage: 처리 결과 코드의 메시지 입니다.
				headerMap.put(KConstant.RESULT_CODE, code);
				headerMap.put(KConstant.RESULT_MESSAGE, message);

				// header.resultException: 에러 발생 시 exception-stack-trace 정보가 포함됩니다.
				// header.validation: 발생된 에러 중 bodyVO의 필드가 validation 오류가 발생했을 경우 정보가 포함됩니다.
				if(isError) {
					//if(!isHideOnError && !KStringUtil.isEmpty(resultException)) {
					if(!isHideOnError && text != null) {
						headerMap.put(KConstant.RESULT_TEXT, text);
					}
					//if(validation != null) {
					//	headerMap.put("validation", validation);
					//}
				}

				if(!KStringUtil.isEmpty(bcode)) {
					headerMap.put(KConstant.RESULT_BCODE, bcode);
					headerMap.put(KConstant.RESULT_BMESSAGE, bmessage);
				}

				TAuthType authType = KContext.getT(AttrKey.AUTH_TYPE);
				switch(authType) {
				case APIKEY: // apikey 인증 방식
					break;
				case BEARER: // bearer 인증 방식
				case NOAUTH:
					break;
				}
			}
		}
	}
}
