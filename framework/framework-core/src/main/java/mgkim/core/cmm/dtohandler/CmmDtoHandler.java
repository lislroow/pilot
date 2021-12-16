package mgkim.core.cmm.dtohandler;

import mgkim.core.com.annotation.KModule;
import mgkim.core.com.dto.KInDTO;
import mgkim.core.com.dto.KOutDTO;

@KModule(name = "DTO 핸들러", required = true)
public interface CmmDtoHandler {

	public void preProcess(KInDTO<?> inDTO);

	public void postProcess(KOutDTO<?> outDTO);

}
