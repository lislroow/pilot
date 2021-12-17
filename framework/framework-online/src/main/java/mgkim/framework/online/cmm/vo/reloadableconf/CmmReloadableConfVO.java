package mgkim.framework.online.cmm.vo.reloadableconf;

import java.util.Date;

import mgkim.framework.core.dto.KCmmVO;

public class CmmReloadableConfVO extends KCmmVO {

	private String hostname;
	private String wasId;
	private String confId;
	private String confStcd;
	private String apliDttm;
	private String apliRdttm;
	private String firstRegirNo;
	private Date firstRegiDttm;
	private String lastModfrNo;
	private Date lastModfDttm;

	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getWasId() {
		return wasId;
	}
	public void setWasId(String wasId) {
		this.wasId = wasId;
	}
	public String getConfId() {
		return confId;
	}
	public void setConfId(String confId) {
		this.confId = confId;
	}
	public String getConfStcd() {
		return confStcd;
	}
	public void setConfStcd(String confStcd) {
		this.confStcd = confStcd;
	}
	public String getApliDttm() {
		return apliDttm;
	}
	public void setApliDttm(String apliDttm) {
		this.apliDttm = apliDttm;
	}
	public String getApliRdttm() {
		return apliRdttm;
	}
	public void setApliRdttm(String apliRdttm) {
		this.apliRdttm = apliRdttm;
	}
	public String getFirstRegirNo() {
		return firstRegirNo;
	}
	public void setFirstRegirNo(String firstRegirNo) {
		this.firstRegirNo = firstRegirNo;
	}
	public Date getFirstRegiDttm() {
		return firstRegiDttm;
	}
	public void setFirstRegiDttm(Date firstRegiDttm) {
		this.firstRegiDttm = firstRegiDttm;
	}
	public String getLastModfrNo() {
		return lastModfrNo;
	}
	public void setLastModfrNo(String lastModfrNo) {
		this.lastModfrNo = lastModfrNo;
	}
	public Date getLastModfDttm() {
		return lastModfDttm;
	}
	public void setLastModfDttm(Date lastModfDttm) {
		this.lastModfDttm = lastModfDttm;
	}
}