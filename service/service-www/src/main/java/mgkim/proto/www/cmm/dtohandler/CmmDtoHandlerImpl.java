package mgkim.proto.www.cmm.dtohandler;

import org.springframework.stereotype.Component;

import mgkim.framework.online.cmm.dtohandler.CmmDtoHandler;
import mgkim.framework.online.com.dto.KInDTO;
import mgkim.framework.online.com.dto.KOutDTO;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.type.TApiType;

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
