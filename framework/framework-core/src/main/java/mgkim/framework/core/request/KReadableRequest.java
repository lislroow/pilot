package mgkim.framework.core.request;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KReadableRequest extends HttpServletRequestWrapper {

	private static final Logger log = LoggerFactory.getLogger(KReadableRequest.class);

	private Charset encoding;
	private byte[] rawData;
	private String bodyString;

	public KReadableRequest(HttpServletRequest request) throws IOException {
		super(request);

		if (request.getContentType() != null && request.getContentType().contains(org.apache.http.entity.ContentType.MULTIPART_FORM_DATA.getMimeType())) {
			return;
		}

		String reqEncoding = request.getCharacterEncoding();
		this.encoding = org.apache.commons.lang3.StringUtils.isBlank(request.getCharacterEncoding()) ? StandardCharsets.UTF_8 : Charset.forName(reqEncoding);

		BufferedReader reader = null;
		try {
			InputStream in = request.getInputStream();
			this.rawData = org.apache.commons.compress.utils.IOUtils.toByteArray(in);
			reader = this.getReader();
			bodyString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.rawData);
		return new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(this.getInputStream(), this.encoding));
	}

	public String getBodyString() {
		return bodyString;
	}

	public void setBodyString(String bodyString) {
		this.bodyString = bodyString;
	}
}
