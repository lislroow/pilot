package mgkim.framework.online.cmm.dtohandler;

import mgkim.framework.core.annotation.KModule;
import mgkim.framework.online.com.dto.KInDTO;
import mgkim.framework.online.com.dto.KOutDTO;

@KModule(name = "DTO 핸들러", required = true)
public interface CmmDtoHandler {

	public void preProcess(KInDTO<?> inDTO);

	public void postProcess(KOutDTO<?> outDTO);

}
