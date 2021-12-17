package mgkim.framework.online.cmm.vo.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import mgkim.framework.core.dto.KCmmVO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CmmOpenapiTokenVO extends KCmmVO {

	private String aprvYn;
	private String openapiUserId;
	private String orgCd;
	private String orgNm;
	private int issuSeq;
	private String apikey;
	private String tokenContent;
	private String issuDttm;
	private String beginDttm;
	private String expryDttm;
	private int validTermDays;
	private String ipAddr;
	private String disuYn;
	private String useYn;
	private String aesKey;

	public String getAprvYn() {
		return aprvYn;
	}
	public void setAprvYn(String aprvYn) {
		this.aprvYn = aprvYn;
	}
	public String getOpenapiUserId() {
		return openapiUserId;
	}
	public void setOpenapiUserId(String openapiUserId) {
		this.openapiUserId = openapiUserId;
	}
	public String getOrgCd() {
		return orgCd;
	}
	public void setOrgCd(String orgCd) {
		this.orgCd = orgCd;
	}
	public String getOrgNm() {
		return orgNm;
	}
	public void setOrgNm(String orgNm) {
		this.orgNm = orgNm;
	}
	public int getIssuSeq() {
		return issuSeq;
	}
	public void setIssuSeq(int issuSeq) {
		this.issuSeq = issuSeq;
	}
	public String getApikey() {
		return apikey;
	}
	public void setApikey(String apikey) {
		this.apikey = apikey;
	}
	public String getTokenContent() {
		return tokenContent;
	}
	public void setTokenContent(String tokenContent) {
		this.tokenContent = tokenContent;
	}
	public String getIssuDttm() {
		return issuDttm;
	}
	public void setIssuDttm(String issuDttm) {
		this.issuDttm = issuDttm;
	}
	public String getBeginDttm() {
		return beginDttm;
	}
	public void setBeginDttm(String beginDttm) {
		this.beginDttm = beginDttm;
	}
	public String getExpryDttm() {
		return expryDttm;
	}
	public void setExpryDttm(String expryDttm) {
		this.expryDttm = expryDttm;
	}
	public int getValidTermDays() {
		return validTermDays;
	}
	public void setValidTermDays(int validTermDays) {
		this.validTermDays = validTermDays;
	}
	public String getIpAddr() {
		return ipAddr;
	}
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	public String getDisuYn() {
		return disuYn;
	}
	public void setDisuYn(String disuYn) {
		this.disuYn = disuYn;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
	public String getAesKey() {
		return aesKey;
	}
	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}
}
