package mgkim.framework.online.com.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class KFileUtil {

	public static long cksum(String filePath) throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(filePath));
		Checksum crc32 = new CRC32();
		crc32.update(bytes, 0, bytes.length);
		return crc32.getValue();
	}
}
