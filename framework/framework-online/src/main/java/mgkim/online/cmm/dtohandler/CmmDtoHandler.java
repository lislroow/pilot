package mgkim.online.cmm.dtohandler;

import mgkim.online.com.annotation.KModule;
import mgkim.online.com.dto.KInDTO;
import mgkim.online.com.dto.KOutDTO;

@KModule(name = "DTO 핸들러", required = true)
public interface CmmDtoHandler {

	public void preProcess(KInDTO<?> inDTO);

	public void postProcess(KOutDTO<?> outDTO);

}
