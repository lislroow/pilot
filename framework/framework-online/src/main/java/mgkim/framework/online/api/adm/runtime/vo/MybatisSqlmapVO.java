package mgkim.framework.online.api.adm.runtime.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MybatisSqlmapVO implements Comparable<MybatisSqlmapVO> {

	@Override
	public int compareTo(MybatisSqlmapVO o) {
		if (o.getFile() == null || this.getFile() == null) {
			return 0;
		}
		return this.getFile().compareToIgnoreCase(o.getFile());
	}

	private String file;

	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}

}
