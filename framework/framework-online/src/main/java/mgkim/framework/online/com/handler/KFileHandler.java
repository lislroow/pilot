package mgkim.framework.online.com.handler;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.ResourceHttpMessageConverter;

import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.type.TResponseType;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.logging.KLogSys;

public class KFileHandler extends ResourceHttpMessageConverter {

	@Override
	protected Resource readInternal(Class<? extends Resource> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {

		return super.readInternal(clazz, inputMessage);
	}

	@Override
	protected void writeInternal(Resource resource, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		// `RESPONSE_TYPE` 결정
		{
			KContext.set(AttrKey.RESPONSE_TYPE, TResponseType.FILE);
		}

		String filename = outputMessage.getHeaders().getContentDisposition().getFilename();
		if(KStringUtil.isEmpty(filename)) {
			filename = KDateUtil.now(KConstant.FMT_YYYYMMDDHHMMSS);
			outputMessage.getHeaders().set(KConstant.HK_CONTENT_DISPOSITION, KHttpUtil.getContentDisposition(filename));
			KLogSys.warn(KMessage.get(KMessage.E6301, filename));
		}
		KContext.set(AttrKey.DOWN_FILE, filename);
		long length = outputMessage.getHeaders().getContentLength();
		KLogSys.debug("filename={} length={}", filename, length);
		super.writeInternal(resource, outputMessage);
	}
}
