package mgkim.framework.online.com.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.ResourceHttpMessageConverter;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.type.KType.RespType;
import mgkim.framework.core.util.KDateUtil;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;

public class KFileHandler extends ResourceHttpMessageConverter {
	
	private static final Logger log = LoggerFactory.getLogger(KFileHandler.class);

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
			KContext.set(AttrKey.RESPONSE_TYPE, RespType.FILE);
		}

		String filename = outputMessage.getHeaders().getContentDisposition().getFilename();
		if (KStringUtil.isEmpty(filename)) {
			filename = KDateUtil.now(KConstant.FMT_YYYYMMDDHHMMSS);
			outputMessage.getHeaders().set(KConstant.HK_CONTENT_DISPOSITION, KHttpUtil.getContentDisposition(filename));
			log.warn(KMessage.get(KMessage.E6301, filename));
		}
		KContext.set(AttrKey.DOWN_FILE, filename);
		long length = outputMessage.getHeaders().getContentLength();
		log.debug("filename={} length={}", filename, length);
		super.writeInternal(resource, outputMessage);
	}
}
