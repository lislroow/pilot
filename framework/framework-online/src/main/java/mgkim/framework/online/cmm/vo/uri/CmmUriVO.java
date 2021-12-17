package mgkim.framework.online.cmm.vo.uri;

import mgkim.framework.core.dto.KCmmVO;

public class CmmUriVO extends KCmmVO implements Comparable<CmmUriVO> {

	private String uriId;
	private String uriVal;
	private String uriNm;
	private String uriPtrnYn;
	private String uriRespTpcd;
	private String useYn;

	public static class Builder {
		private String uriId;
		private String uriVal;
		private String uriNm;
		private String uriPtrnYn;
		private String uriRespTpcd;
		private String useYn;

		public Builder() { }

		public Builder uriId(String uriId) {
			this.uriId = uriId;
			return this;
		}
		public Builder uriVal(String uriVal) {
			this.uriVal = uriVal;
			return this;
		}
		public Builder uriNm(String uriNm) {
			this.uriNm = uriNm;
			return this;
		}
		public Builder uriPtrnYn(String uriPtrnYn) {
			this.uriPtrnYn = uriPtrnYn;
			return this;
		}
		public Builder uriRespTpcd(String uriRespTpcd) {
			this.uriRespTpcd = uriRespTpcd;
			return this;
		}
		public Builder useYn(String useYn) {
			this.useYn = useYn;
			return this;
		}

		public CmmUriVO build() {
			return new CmmUriVO(uriId, uriVal, uriNm, uriPtrnYn, uriRespTpcd, useYn);
		}
	}

	public CmmUriVO(String uriId, String uriVal, String uriNm, String uriPtrnYn, String uriRespTpcd, String useYn) {
		super();
		this.uriId = uriId;
		this.uriVal = uriVal;
		this.uriNm = uriNm;
		this.uriPtrnYn = uriPtrnYn;
		this.uriRespTpcd = uriRespTpcd;
		this.useYn = useYn;
	}

	public String getUriId() {
		return uriId;
	}
	public void setUriId(String uriId) {
		this.uriId = uriId;
	}
	public String getUriVal() {
		return uriVal;
	}
	public void setUriVal(String uriVal) {
		this.uriVal = uriVal;
	}
	public String getUriPtrnYn() {
		return uriPtrnYn;
	}
	public void setUriPtrnYn(String uriPtrnYn) {
		this.uriPtrnYn = uriPtrnYn;
	}
	public String getUriRespTpcd() {
		return uriRespTpcd;
	}
	public void setUriRespTpcd(String uriRespTpcd) {
		this.uriRespTpcd = uriRespTpcd;
	}
	public String getUriNm() {
		return uriNm;
	}
	public void setUriNm(String uriNm) {
		this.uriNm = uriNm;
	}
	public String getUseYn() {
		return useYn;
	}
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	@Override
	public int compareTo(CmmUriVO o) {
		int result;
		if(o.getUriVal() == null || this.getUriVal() == null) {
			if(o.getUriNm() == null || this.getUriNm() == null) {
				result = -1;
				return result;
			}
		}
		if(this.getUriVal().compareToIgnoreCase(o.getUriVal()) == 0) {
			if(o.getUriNm() == null || this.getUriNm() == null) {
				result = -1;
				return result;
			} else {
				result = this.getUriNm().compareToIgnoreCase(o.getUriNm());
			}
		} else {
			result = this.getUriVal().compareToIgnoreCase(o.getUriVal());
		}
		return result;
	}
}
