package mgkim.framework.core.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.io.FilenameUtils;

public class KFileUtil {

	public static long cksum(String filePath) throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(filePath));
		Checksum crc32 = new CRC32();
		crc32.update(bytes, 0, bytes.length);
		return crc32.getValue();
	}
	
	public static String filename(String filepath) {
		return FilenameUtils.getName(filepath);
	}
	
	public static String removeExtension(String filepath) {
		return FilenameUtils.removeExtension(filepath);
	}
}
