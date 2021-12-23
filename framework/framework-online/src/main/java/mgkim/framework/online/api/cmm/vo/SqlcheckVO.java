package mgkim.framework.online.api.cmm.vo;

import java.util.List;

import mgkim.framework.core.dto.KCmmVO;

public class SqlcheckVO extends KCmmVO {
	
	private List<String> strList = null;

	public List<String> getStrList() {
		return strList;
	}
	public void setStrList(List<String> strList) {
		this.strList = strList;
	}
}
