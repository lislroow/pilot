package mgkim.framework.online.com.mgr;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import mgkim.framework.cmm.online.vo.CmmUriVO;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.util.KStringUtil;

@KBean(name = "url-목록 관리")
public class ComUriListMgr {
	
	private static final Logger log = LoggerFactory.getLogger(ComUriListMgr.class);

	RequestMappingHandlerMapping requestMapping;
	List<CmmUriVO> uriList = new ArrayList<CmmUriVO>();

	@EventListener
	public void refreshed(ContextRefreshedEvent event) {
		ApplicationContext ctx = event.getApplicationContext();
		requestMapping = (RequestMappingHandlerMapping) ctx.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
		init();
	}

	public void init() {
		if (requestMapping == null) {
			return;
		}
		Map<RequestMappingInfo, HandlerMethod> map = requestMapping.getHandlerMethods();

		map.forEach((key, value) -> {
			CmmUriVO uriVO = new CmmUriVO.Builder().build();
			{
				Class returnType = value.getMethod().getReturnType();
				if (returnType == null) {
					uriVO.setUriRespTpcd("08"); // void
				} else {
					if (returnType.getName().equals(KOutDTO.class.getName())) {
						uriVO.setUriRespTpcd("01"); // application/json
					} else if (returnType.getName().equals(ResponseEntity.class.getName())) {
						uriVO.setUriRespTpcd("02"); // application/octet-stream
					} else {
						uriVO.setUriRespTpcd("09"); // uncategorised
					}
				}
			}
			for (int i=0; i<value.getMethod().getAnnotations().length; i++) {
				Annotation[] list = value.getMethod().getAnnotations();
				Annotation item = list[i];
				if (org.springframework.web.bind.annotation.RequestMapping.class.isInstance(item)) {
					RequestMapping annotations = (RequestMapping)item;
					if (!Arrays.asList(annotations.method()).contains(RequestMethod.POST)) {
						return;
					}
					if (annotations.value().length == 0) {
						return;
					}
					try {
						if (KStringUtil.isEmpty(annotations.value()[0])) {
							return;
						}
						uriVO.setUriVal(annotations.value()[0]);
					} catch(Exception e) {
						e.printStackTrace();
					}
					if (uriVO.getUriVal().equals(Matcher.quoteReplacement(uriVO.getUriVal()))) {
						uriVO.setUriPtrnYn("N");
					} else {
						uriVO.setUriPtrnYn("Y");
					}
				}
				//else if (ApiOperation.class.isInstance(item)) {
				//	ApiOperation annotations = (ApiOperation)item;
				//	uriVO.setUriNm(annotations.value());
				//}
			};
			uriList.add(uriVO);
		});

		log.debug("api 목록 = {} ", KStringUtil.toJson(uriList).replaceAll(",", KConstant.LINE).replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", ""));
		log.info("api 목록 {}개 가 로드 되었습니다.", uriList.size());
	}

	public HandlerMethod getHandlerMethod(HttpServletRequest request) throws Exception {
		HandlerExecutionChain handler = requestMapping.getHandler(request);
		if (handler == null) {
			return null;
		}
		HandlerMethod method = (HandlerMethod) handler.getHandler();
		return method;
	}

	public List<CmmUriVO> getUriList() {
		init();
		return uriList;
	}
}
