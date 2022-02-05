package mgkim.framework.online.v1;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.logging.KLog;
import mgkim.framework.core.logging.KLoggerFactory;
import mgkim.framework.core.util.KStringUtil;

@Api( tags = { KConstant.SWG_V1 } )
@RestController
public class ExamController {
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	KLog klog = KLoggerFactory.getLogger(KLog.class, ExamController.class);
	
	@Autowired
	private ApplicationContext springContext;
	
	@ApiOperation(value = "(exam)")
	@RequestMapping(value = "/v1/exam", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<Map> idlogin(
			@KRequestMap HashMap<String, Object> inMap,
			@RequestParam(required = true) String param1) throws Exception {
		KOutDTO<Map> outDTO = new KOutDTO<Map>();
		
		{
			klog.debug("my-label{10}", KStringUtil.toJsonNoPretty(outDTO));
		}
		
		return outDTO;
	}
}
