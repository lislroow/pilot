package mgkim.online.api.adm.initdata.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.online.api.adm.initdata.service.InitDataLoadService;
import mgkim.online.com.dto.KInDTO;
import mgkim.online.com.dto.KOutDTO;
import mgkim.online.com.env.KConstant;

@Api( tags = { KConstant.SWG_SYSTEM_MANAGEMENT } )
@RestController
public class InitDataLoadController {

	@Autowired(required = false)
	private InitDataLoadService initDataLoadService;


	// ##############
	// 데이터 일괄 적재
	// ##############
	@ApiOperation(value = "(초기데이터) 데이터 일괄 적재")
	@RequestMapping(value = "/api/adm/initdata/loadAll", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<String> loadAll(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<String> outDTO = new KOutDTO<String>();
		initDataLoadService.loadRole();
		initDataLoadService.loadRgrp();
		initDataLoadService.loadRoleRgrp();
		initDataLoadService.loadUriRaw();
		initDataLoadService.loadUri();
		initDataLoadService.loadUriRgrp();
		initDataLoadService.loadUserRaw();
		initDataLoadService.loadUser();
		initDataLoadService.loadUserRpgr();
		outDTO.setBody("successful");
		return outDTO;
	}


	// ##############
	// 1) 권한
	// ##############
	@ApiOperation(value = "(초기데이터) 1.1 권한")
	@RequestMapping(value = "/api/adm/initdata/loadRole", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadRole(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadRole();
		return outDTO;
	}

	@ApiOperation(value = "(초기데이터) 1.2 권한그룹")
	@RequestMapping(value = "/api/adm/initdata/loadRgrp", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadRgrp(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadRgrp();
		return outDTO;
	}

	@ApiOperation(value = "(초기데이터) 1.3 권한그룹설정")
	@RequestMapping(value = "/api/adm/initdata/loadRoleRgrp", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadRoleRgrp(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadRoleRgrp();
		return outDTO;
	}


	// ##############
	// 2) 자원
	// ##############
	@ApiOperation(value = "(초기데이터) 2.1 uri(raw)")
	@RequestMapping(value = "/api/adm/initdata/loadUriRaw", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadUriRaw(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadUriRaw();
		return outDTO;
	}

	@ApiOperation(value = "(초기데이터) 2.2 uri")
	@RequestMapping(value = "/api/adm/initdata/loadUri", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadUri(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadUri();
		return outDTO;
	}

	@ApiOperation(value = "(초기데이터) 2.3 uri권한")
	@RequestMapping(value = "/api/adm/initdata/loadUriRgrp", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadUriRgrp(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadUriRgrp();
		return outDTO;
	}


	// ##############
	// 3) 사용자
	// ##############
	@ApiOperation(value = "(초기데이터) 3.1 사용자(raw)")
	@RequestMapping(value = "/api/adm/initdata/loadUserRaw", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadUserRaw(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadUserRaw();
		return outDTO;
	}

	@ApiOperation(value = "(초기데이터) 3.2 사용자")
	@RequestMapping(value = "/api/adm/initdata/loadUser", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadUser(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadUser();
		return outDTO;
	}

	@ApiOperation(value = "(초기데이터) 3.3 사용자권한")
	@RequestMapping(value = "/api/adm/initdata/loadUserRpgr", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<?> loadUserRpgr(@RequestBody KInDTO<?> inDTO) throws Exception {
		KOutDTO<?> outDTO = new KOutDTO<>();
		initDataLoadService.loadUserRpgr();
		return outDTO;
	}

}
