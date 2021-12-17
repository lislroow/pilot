package mgkim.core.cmm;

import mgkim.core.com.annotation.KModule;
import mgkim.core.com.dto.KInDTO;

@KModule(name = "JSON 로깅 관리", required = false)
public interface CmmDtoLog {

	public void logging(KInDTO<?> inDTO) throws Exception;

}
