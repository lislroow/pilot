package mgkim.online.com.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import mgkim.online.com.env.KConstant;
import mgkim.online.com.env.KContext;
import mgkim.online.com.env.KContext.AttrKey;
import mgkim.online.com.logging.KLogSys;

public class KOnlineBatch {

	public static final String BATCH_URL = "";

	@SuppressWarnings("unchecked")
	public static boolean exec(String jobId, Map<String, Object> param) throws Exception {
		Map<String, Object> inDTO = new HashMap<String, Object>();
		String guid = KContext.getT(AttrKey.GUID);
		String txid = KContext.getT(AttrKey.TXID);
		inDTO.putAll(param);
		KLogSys.info(KStringUtil.toJson(inDTO));
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(KConstant.GUID, guid);
		headers.add(KConstant.TXID, txid);
		HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(inDTO, headers);
		java.util.regex.Matcher matcher = null;
		if(KStringUtil.isEmpty(jobId)) {
			KLogSys.error("jobid가 null 입니다. jobid={}", jobId);
			return false;
		}
		try {
			matcher = java.util.regex.Pattern.compile("([^0-9]+)").matcher(jobId);
		} catch(Exception e) {
			KLogSys.error("jobid 가 올바른 형식이 아닙니다. jobid={}", jobId);
			return false;
		}
		if(!matcher.find()) {
			KLogSys.error("jobId 가 규칙에 맞지 않습니다. jobId={}", jobId);
			return false;
		}
		String bizId = matcher.group(0).toLowerCase();
		String url = String.format("%s/batch/%s/%s", BATCH_URL, bizId, jobId);
		Map<String, String> outDTO = null;
		KLogSys.info("배치 jobId={}", jobId);
		KLogSys.info("배치 url={}", url);
		try {
			outDTO = rest.postForObject(url, request, Map.class);
		} catch(RestClientException ex) {
			if(ex instanceof HttpClientErrorException) {
				int httpStatusCode = ((HttpClientErrorException) ex).getRawStatusCode();
				switch(httpStatusCode) {
				case 404:
					KLogSys.error("bat 컨테이너에 job을 실행하는 @Controller(@RequestMapping)가 없습니다. http-status: {}, url={}", httpStatusCode, url, ex);
					break;
				case 400:
					KLogSys.error("bat 컨테이너에 job을 실행하는 @Controller의 @RequestBody에 정의된 파라미터 타입(int, boolean 등)을 확인해주세요. http-status: {}, param={}", httpStatusCode, KStringUtil.toJson(inDTO), ex);
					break;
				default:
					KLogSys.error("bat 컨테이너에 job을 실행하는 @Controller(@RequestMapping)를 호출할 수 없습니다. http-status: {}, url={}", httpStatusCode, url, ex);
					break;
				}
				return false;
			} else {
				KLogSys.error("bat 컨테이너에 접속할 수 없습니다.", ex);
			}
		}


		// job 실행 결과
		if(outDTO != null) {
			return true;
		} else {
			return false;
		}
	}
}
