package mgkim.service.adm.cmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.cmm.online.CmmDtoLog;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.dto.KInDTO;

@KBean
public class CmmDtoLogImpl implements CmmDtoLog {
	
	private static final Logger log = LoggerFactory.getLogger(CmmDtoLogImpl.class);

	@Override
	public void logging(KInDTO<?> inDTO) throws Exception {
		//Object bodyVO = KObjectUtil.getValue(inDTO, "body");
	}

}
