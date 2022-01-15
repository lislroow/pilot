package mgkim.service.adm.cmm;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.logging.KLog;
import mgkim.framework.core.logging.KLogSign;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.cmm.CmmDtoLog;

@KBean
public class CmmDtoLogImpl implements CmmDtoLog {
	
	private static final Logger log = LoggerFactory.getLogger(CmmDtoLogImpl.class);

	@Override
	public void logging(KInDTO<?> inDTO) throws Exception {
		try {
			Object bodyVO = KObjectUtil.getValue(inDTO, "body");
			boolean logging = false;//KStringUtil.toBoolean(KStringUtil.nvl(KObjectUtil.getValueByFieldName(bodyVO, "field"), "false"));
			if (logging) {
				// 시스템필드
				String guid = KContext.getT(AttrKey.SSID);
				String referer = KContext.getT(AttrKey.REFERER);
				String ip = KContext.getT(AttrKey.IP);

				// 업무필드
				String opcode = KStringUtil.nvl(KObjectUtil.getValue(bodyVO, "opcode"), "");
				String signData = KStringUtil.nvl(KObjectUtil.getValue(bodyVO, "signData"), "");
				String rvalue = KStringUtil.nvl(KObjectUtil.getValue(bodyVO, "rvalue"), "");
				String userId = KStringUtil.nvl(KObjectUtil.getValue(bodyVO, "userId"), "");

				// 서명로그 텍스트
				String text = MessageFormat.format("{0}|{1}|{2}|{3}|{4}|{5}|{6}"
						, guid, referer, ip, opcode
						, signData, rvalue, userId);

				if ("NORMAL_CRL_OCSP".equals(opcode) || "NORMAL_OCSP_ONLY".equals(opcode)) {
					// 전자서명
					String filePath = KLogSign.sign("NORMAL", text);
					KLog.info("cmm/sign", "전자서명 로그 저장 완료. 저장경로={}, 내용={}", filePath, text);
				} else if ("AUTHORIZE_CRL_OCSP".equals(opcode) || "AUTHORIZE_OCSP_ONLY".equals(opcode)) {
					// 본인확인
					String filePath = KLogSign.sign("AUTHORIZE", text);
					KLog.info("cmm/sign", "본인확인 로그 저장 완료. 저장경로={}, 내용={}", filePath, text);
				} else if ("CONTRACTION_NABR".equals(opcode) || "CONTRACTION_MYPASS".equals(opcode)) {
					// 축약서명
					String filePath = KLogSign.sign("CONTRACTION", text);
					KLog.info("cmm/sign", "축약서명 로그 저장 완료. 저장경로={}, 내용={}", filePath, text);
				} else {
					// 부인방지
					String filePath = KLogSign.sign("NR", KStringUtil.toJsonNoPretty(inDTO));
					KLog.info("cmm/sign", "부인방지 로그 저장 완료. 저장경로={}, 내용={}", filePath, KStringUtil.toJsonNoPretty(inDTO));
					KObjectUtil.setValue(bodyVO, "filePath", filePath);
				}
			}
		} catch(Exception e) {
			log.error("Dto 로그를 저장하는 중 오류가 발생했습니다.", e);
		}
	}

}
