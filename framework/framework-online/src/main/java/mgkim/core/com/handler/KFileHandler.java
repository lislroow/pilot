package mgkim.core.com.handler;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.ResourceHttpMessageConverter;

import mgkim.core.com.env.KConstant;
import mgkim.core.com.env.KContext;
import mgkim.core.com.env.KContext.AttrKey;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.type.TResponseType;
import mgkim.core.com.util.KDateUtil;
import mgkim.core.com.util.KHttpUtil;
import mgkim.core.com.util.KStringUtil;

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
