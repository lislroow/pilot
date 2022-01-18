package mgkim.service.www.cmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.online.cmm.CmmDtoLog;

@KBean
public class CmmDtoLogImpl implements CmmDtoLog {
	
	private static final Logger log = LoggerFactory.getLogger(CmmDtoLogImpl.class);

	@Override
	public void logging(KInDTO<?> inDTO) throws Exception {
		//Object bodyVO = KObjectUtil.getValue(inDTO, "body");
	}

}
