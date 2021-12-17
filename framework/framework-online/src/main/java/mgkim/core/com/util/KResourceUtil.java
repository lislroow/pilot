package mgkim.core.com.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.AntPathMatcher;

import mgkim.core.com.logging.KLogSys;

public class KResourceUtil {

	private static AntPathMatcher antPathMatcher = new AntPathMatcher();

	public static Resource[] getResourceInJar(File rootFile, List<String> jarPattern, String jarEntryPattern) throws IOException {
		KLogSys.debug("PATH_WEBINF_LIB={}", rootFile.getAbsolutePath());

		String _jarEntryPattern = jarEntryPattern.replaceFirst("classpath:", "");
		if(!jarEntryPattern.equals(_jarEntryPattern)) {
			KLogSys.debug("jarEntryPattern에 classpath:문자열을 제거합니다. {} => {}", jarEntryPattern, _jarEntryPattern);
			jarEntryPattern = _jarEntryPattern;
		}


		Set<Resource> result = new LinkedHashSet<Resource>();
		for(File file : rootFile.listFiles()) {
			String filePath = file.getAbsolutePath();
			String fileRelativePath = filePath.substring(rootFile.getAbsolutePath().length()+File.pathSeparator.length());

			boolean isMatched = false;
			for(String p : jarPattern) {
				if(antPathMatcher.match(p, fileRelativePath)) {
					isMatched = true;
					break;
				}
			}
			if(isMatched) {
				KLogSys.info("[matched][jar-relative-path] {}", fileRelativePath);
				String urlPath = String.format("jar:file:%s!/", filePath);
				URL urlFile = new URL(urlPath);
				URLConnection con = urlFile.openConnection();
				JarURLConnection jarCon = (JarURLConnection) con;
				JarFile jarFile = jarCon.getJarFile();
				for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
					JarEntry entry = entries.nextElement();
					String entryPath = entry.getName();
					if(antPathMatcher.match(jarEntryPattern, entryPath)) {
						UrlResource resource = new UrlResource(urlPath+entryPath);
						result.add(resource);
						KLogSys.debug("[matched][jar-entry] {}", resource.getURL());
					}
				}
			} else {
				KLogSys.debug("[skipped][jar-relative-path] {}", fileRelativePath);
			}
		}
		return result.toArray(new Resource[result.size()]);
	}
}
