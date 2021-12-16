package mgkim.proto.www.cmm.dtohandler;

import org.springframework.stereotype.Component;

import mgkim.core.cmm.dtohandler.CmmDtoHandler;
import mgkim.core.com.dto.KInDTO;
import mgkim.core.com.dto.KOutDTO;
import mgkim.core.com.env.KContext;
import mgkim.core.com.env.KContext.AttrKey;
import mgkim.core.com.type.TApiType;

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
