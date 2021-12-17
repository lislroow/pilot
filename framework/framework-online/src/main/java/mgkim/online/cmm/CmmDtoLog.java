package mgkim.online.cmm;

import mgkim.online.com.annotation.KModule;
import mgkim.online.com.dto.KInDTO;

@KModule(name = "JSON 로깅 관리", required = false)
public interface CmmDtoLog {

	public void logging(KInDTO<?> inDTO) throws Exception;

}
