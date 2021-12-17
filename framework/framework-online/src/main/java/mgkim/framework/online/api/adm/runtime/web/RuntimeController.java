package mgkim.framework.online.api.adm.runtime.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.online.api.adm.runtime.service.RuntimeService;
import mgkim.framework.online.api.adm.runtime.vo.JavaEnvVariableVO;
import mgkim.framework.online.api.adm.runtime.vo.JdbcDatasourceVO;
import mgkim.framework.online.api.adm.runtime.vo.SpringBeansVO;
import mgkim.framework.online.api.adm.runtime.vo.SpringSecurityUriVO;
import mgkim.framework.online.api.adm.runtime.vo.SpringUri2VO;
import mgkim.framework.online.api.adm.runtime.vo.SpringUriVO;
import mgkim.framework.online.com.dto.KOutDTO;
import mgkim.framework.online.com.env.KConstant;

@Api( tags = { KConstant.SWG_SYSTEM_MANAGEMENT } )
@RestController
public class RuntimeController {
	public RuntimeController() {
		System.out.println("asaaa");
	}
	@Autowired
	private RuntimeService runtimeService;

	@ApiOperation(value = "(실행환경) spring-bean 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/spring-beans", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<SpringBeansVO>> springBeans() throws Exception {
		KOutDTO<List<SpringBeansVO>> outDTO = new KOutDTO<List<SpringBeansVO>>();
		List<SpringBeansVO> listVO = runtimeService.getBeans();
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) mybatis-mapper 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/mybatis-mapper", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<String>> mybatisMapper() throws Exception {
		KOutDTO<List<String>> outDTO = new KOutDTO<List<String>>();
		List<String> listVO = runtimeService.mapperList();
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) mybatis-mapper-sqltext 조회")
	@RequestMapping(value = "/api/adm/runtime/mybatis-mapper-sqltext", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<byte[]> mybatisMapperSqltext() throws Exception {
		ResponseEntity<byte[]> result = null;
		StringBuffer sbuf = runtimeService.mapperSqltext();
		try {
			HttpHeaders headers = new HttpHeaders();
			//headers.setContentType(MediaType.TEXT_PLAIN);
			headers.set("Content-Type", "text/plain; charset=utf-8");
			result = new ResponseEntity<byte[]>(sbuf.toString().getBytes(), headers, HttpStatus.OK);
		} catch(Exception e) {
			result = new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

	@ApiOperation(value = "(실행환경) spring-uri 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/spring-uri", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<SpringUriVO>> springUri() throws Exception {
		KOutDTO<List<SpringUriVO>> outDTO = new KOutDTO<List<SpringUriVO>>();
		List<SpringUriVO> listVO = runtimeService.getUri();
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) spring-uri 목록 조회2")
	@RequestMapping(value = "/api/adm/runtime/spring-uri2", method = RequestMethod.GET)
	public @ResponseBody List<SpringUri2VO> springUri2() throws Exception {
		List<SpringUri2VO> listVO = runtimeService.getUri2();
		return listVO;
	}

	@ApiOperation(value = "(실행환경) spring-security-uri 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/spring-security-uri", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<SpringSecurityUriVO>> springSecurityUri() throws Exception {
		KOutDTO<List<SpringSecurityUriVO>> outDTO = new KOutDTO<List<SpringSecurityUriVO>>();
		List<SpringSecurityUriVO> listVO = runtimeService.getRoleAndUri();
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) java-env-variable 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/java-env-variable", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<JavaEnvVariableVO>> javaEnvVariable() throws Exception {
		KOutDTO<List<JavaEnvVariableVO>> outDTO = new KOutDTO<List<JavaEnvVariableVO>>();
		List<JavaEnvVariableVO> listVO = runtimeService.getEnv();
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) jdbc-datasource 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/jdbc-datasource", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<JdbcDatasourceVO>> jdbcDatasource() throws Exception {
		KOutDTO<List<JdbcDatasourceVO>> outDTO = new KOutDTO<List<JdbcDatasourceVO>>();
		List<JdbcDatasourceVO> listVO = runtimeService.getDataSource();
		outDTO.setBody(listVO);
		return outDTO;
	}
}
