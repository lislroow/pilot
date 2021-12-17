package mgkim.proto.www.cmm.dtohandler;

import org.springframework.stereotype.Component;

import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.type.TApiType;
import mgkim.framework.online.cmm.dtohandler.CmmDtoHandler;

@Component
public class CmmDtoHandlerImpl implements CmmDtoHandler {

	@Override
	public void preProcess(KInDTO<?> inDto) {
		{
			TApiType apiType = KContext.getT(AttrKey.API_TYPE);
		}
	}

	@Override
	public void postProcess(KOutDTO<?> outDto) {
		{
			TApiType apiType = KContext.getT(AttrKey.API_TYPE);
		}
	}
}
