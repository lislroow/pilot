package mgkim.framework.cmm.online;

import mgkim.framework.core.annotation.KModule;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KOutDTO;

@KModule(name = "DTO 핸들러", required = true)
public interface CmmDtoHandler {

	public void preProcess(KInDTO<?> inDTO);

	public void postProcess(KOutDTO<?> outDTO);

}
