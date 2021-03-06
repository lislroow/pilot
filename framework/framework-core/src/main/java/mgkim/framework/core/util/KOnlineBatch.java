package mgkim.framework.core.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;

public class KOnlineBatch {
	
	private static final Logger log = LoggerFactory.getLogger(KOnlineBatch.class);

	public static final String BATCH_URL = "";

	@SuppressWarnings("unchecked")
	public static boolean exec(String jobId, Map<String, Object> param) throws Exception {
		Map<String, Object> inDTO = new HashMap<String, Object>();
		String guid = KContext.getT(AttrKey.GUID);
		String txid = KContext.getT(AttrKey.TXID);
		inDTO.putAll(param);
		log.info(KStringUtil.toJson(inDTO));
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(KConstant.GUID, guid);
		headers.add(KConstant.TXID, txid);
		HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(inDTO, headers);
		java.util.regex.Matcher matcher = null;
		if (KStringUtil.isEmpty(jobId)) {
			log.error("jobid가 null 입니다. jobid={}", jobId);
			return false;
		}
		try {
			matcher = java.util.regex.Pattern.compile("([^0-9]+)").matcher(jobId);
		} catch(Exception e) {
			log.error("jobid 가 올바른 형식이 아닙니다. jobid={}", jobId);
			return false;
		}
		if (!matcher.find()) {
			log.error("jobId 가 규칙에 맞지 않습니다. jobId={}", jobId);
			return false;
		}
		String bizId = matcher.group(0).toLowerCase();
		String url = String.format("%s/batch/%s/%s", BATCH_URL, bizId, jobId);
		Map<String, String> outDTO = null;
		log.info("배치 jobId={}", jobId);
		log.info("배치 url={}", url);
		try {
			outDTO = rest.postForObject(url, request, Map.class);
		} catch(RestClientException ex) {
			if (ex instanceof HttpClientErrorException) {
				int httpStatusCode = ((HttpClientErrorException) ex).getRawStatusCode();
				switch(httpStatusCode) {
				case 404:
					log.error("bat 컨테이너에 job을 실행하는 @Controller(@RequestMapping)가 없습니다. http-status: {}, url={}", httpStatusCode, url, ex);
					break;
				case 400:
					log.error("bat 컨테이너에 job을 실행하는 @Controller의 @RequestBody에 정의된 파라미터 타입(int, boolean 등)을 확인해주세요. http-status: {}, param={}", httpStatusCode, KStringUtil.toJson(inDTO), ex);
					break;
				default:
					log.error("bat 컨테이너에 job을 실행하는 @Controller(@RequestMapping)를 호출할 수 없습니다. http-status: {}, url={}", httpStatusCode, url, ex);
					break;
				}
				return false;
			} else {
				log.error("bat 컨테이너에 접속할 수 없습니다.", ex);
			}
		}


		// job 실행 결과
		if (outDTO != null) {
			return true;
		} else {
			return false;
		}
	}
}
