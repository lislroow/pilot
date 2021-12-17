package mgkim.core.cmm.vo.privacy;

import mgkim.core.com.dto.KCmmVO;

public class CmmPrivacyMngVO extends KCmmVO {

	public String mngtgId;
	public String mngtgTpcd;

	public String getMngtgId() {
		return mngtgId;
	}
	public void setMngtgId(String mngtgId) {
		this.mngtgId = mngtgId;
	}
	public String getMngtgTpcd() {
		return mngtgTpcd;
	}
	public void setMngtgTpcd(String mngtgTpcd) {
		this.mngtgTpcd = mngtgTpcd;
	}
}
