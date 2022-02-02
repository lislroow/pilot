package mgkim.framework.online.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;

@Api( tags = { KConstant.SWG_V1 } )
@RestController
public class InitialController {

	@Autowired(required = false)
	private InitialService initialService;


	@ApiOperation(value = "(초기화) 데이터")
	@RequestMapping(value = "/v1/initial/data", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<String> data() throws Exception {
		KOutDTO<String> outDTO = new KOutDTO<String>();
//		initialService.loadRole();
//		initialService.loadRgrp();
//		initialService.loadRoleRgrp();
		initialService.loadUriRaw();
//		initialService.loadUri();
//		initialService.loadUriRgrp();
//		initialService.loadUserRaw();
//		initialService.loadUser();
//		initialService.loadUserRpgr();
		outDTO.setBody("successful");
		return outDTO;
	}

}
