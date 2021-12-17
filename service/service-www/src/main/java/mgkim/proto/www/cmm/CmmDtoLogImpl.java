package mgkim.proto.www.cmm;

import java.text.MessageFormat;

import mgkim.online.cmm.CmmDtoLog;
import mgkim.online.com.annotation.KBean;
import mgkim.online.com.dto.KInDTO;
import mgkim.online.com.env.KContext;
import mgkim.online.com.env.KContext.AttrKey;
import mgkim.online.com.logging.KLog;
import mgkim.online.com.logging.KLogSign;
import mgkim.online.com.logging.KLogSys;
import mgkim.online.com.util.KObjectUtil;
import mgkim.online.com.util.KStringUtil;

@KBean
public class CmmDtoLogImpl implements CmmDtoLog {

	@Override
	public void logging(KInDTO<?> inDTO) throws Exception {
		try {
			Object bodyVO = KObjectUtil.getValue(inDTO, "body");
			boolean logging = false;//KStringUtil.toBoolean(KStringUtil.nvl(KObjectUtil.getValueByFieldName(bodyVO, "field"), "false"));
			if(logging) {
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

				if("NORMAL_CRL_OCSP".equals(opcode) || "NORMAL_OCSP_ONLY".equals(opcode)) {
					// 전자서명
					String filePath = KLogSign.sign("NORMAL", text);
					KLog.info("cmm/sign", "전자서명 로그 저장 완료. 저장경로={}, 내용={}", filePath, text);
				} else if("AUTHORIZE_CRL_OCSP".equals(opcode) || "AUTHORIZE_OCSP_ONLY".equals(opcode)) {
					// 본인확인
					String filePath = KLogSign.sign("AUTHORIZE", text);
					KLog.info("cmm/sign", "본인확인 로그 저장 완료. 저장경로={}, 내용={}", filePath, text);
				} else if("CONTRACTION_NABR".equals(opcode) || "CONTRACTION_MYPASS".equals(opcode)) {
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
			KLogSys.error("Dto 로그를 저장하는 중 오류가 발생했습니다.", e);
		}
	}

}
