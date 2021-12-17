package mgkim.framework.online.cmm;

import mgkim.framework.core.annotation.KModule;
import mgkim.framework.core.dto.KInDTO;

@KModule(name = "JSON 로깅 관리", required = false)
public interface CmmDtoLog {

	public void logging(KInDTO<?> inDTO) throws Exception;

}
