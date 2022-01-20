package mgkim.framework.online.api.cmm.vo;

import mgkim.framework.core.dto.KCmmVO;

public class InitLoadMockVO extends KCmmVO {

	private String roleId;
	private String roleNm;
	private String rgrpId;
	private String rgrpNm;
	private String userId;
	private String userNm;
	private String email;
	private String aumthTpcdList;
	private String dloginAlowYn;
	private String inactvYn;
	private String lockYn;
	private int loginFailCnt;
	private String pwchgDt;
	private int ssvaldSec;
	private String uriId;


	public static class Builder {
		private String roleId;
		private String roleNm;
		private String rgrpId;
		private String rgrpNm;
		private String userId;
		private String userNm;
		private String email;
		private String aumthTpcdList;
		private String dloginAlowYn;
		private String inactvYn;
		private String lockYn;
		private int loginFailCnt;
		private String pwchgDt;
		private int ssvaldSec;
		private String uriId;

		public Builder() { }

		public Builder roleId(String roleId) {
			this.roleId = roleId;
			return this;
		}
		public Builder roleNm(String roleNm) {
			this.roleNm = roleNm;
			return this;
		}
		public Builder rgrpId(String rgrpId) {
			this.rgrpId = rgrpId;
			return this;
		}
		public Builder rgrpNm(String rgrpNm) {
			this.rgrpNm = rgrpNm;
			return this;
		}
		public Builder userId(String userId) {
			this.userId = userId;
			return this;
		}
		public Builder userNm(String userNm) {
			this.userNm = userNm;
			return this;
		}
		public Builder email(String email) {
			this.email = email;
			return this;
		}
		public Builder aumthTpcdList(String aumthTpcdList) {
			this.aumthTpcdList = aumthTpcdList;
			return this;
		}
		public Builder dloginAlowYn(String dloginAlowYn) {
			this.dloginAlowYn = dloginAlowYn;
			return this;
		}
		public Builder inactvYn(String inactvYn) {
			this.inactvYn = inactvYn;
			return this;
		}
		public Builder lockYn(String lockYn) {
			this.lockYn = lockYn;
			return this;
		}
		public Builder loginFailCnt(int loginFailCnt) {
			this.loginFailCnt = loginFailCnt;
			return this;
		}
		public Builder pwchgDt(String pwchgDt) {
			this.pwchgDt = pwchgDt;
			return this;
		}
		public Builder ssvaldSec(int ssvaldSec) {
			this.ssvaldSec = ssvaldSec;
			return this;
		}
		public Builder uriId(String uriId) {
			this.uriId = uriId;
			return this;
		}

		public InitLoadMockVO build() {
			return new InitLoadMockVO(roleId, roleNm, rgrpId, rgrpNm, userId, userNm, email,
					aumthTpcdList, dloginAlowYn, inactvYn, lockYn, loginFailCnt, pwchgDt, ssvaldSec,
					uriId);
		}
	}

	public InitLoadMockVO(String roleId, String roleNm, String rgrpId, String rgrpNm, String userId, String userNm,
			String email, String aumthTpcdList, String dloginAlowYn, String inactvYn, String lockYn, int loginFailCnt, String pwchgDt,
			int ssvaldSec, String uriId) {
		super();
		this.roleId = roleId;
		this.roleNm = roleNm;
		this.rgrpId = rgrpId;
		this.rgrpNm = rgrpNm;
		this.userId = userId;
		this.userNm = userNm;
		this.email = email;
		this.aumthTpcdList = aumthTpcdList;
		this.dloginAlowYn = dloginAlowYn;
		this.inactvYn = inactvYn;
		this.lockYn = lockYn;
		this.loginFailCnt = loginFailCnt;
		this.pwchgDt = pwchgDt;
		this.ssvaldSec = ssvaldSec;
		this.uriId = uriId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleNm() {
		return roleNm;
	}

	public void setRoleNm(String roleNm) {
		this.roleNm = roleNm;
	}

	public String getRgrpId() {
		return rgrpId;
	}

	public void setRgrpId(String rgrpId) {
		this.rgrpId = rgrpId;
	}

	public String getRgrpNm() {
		return rgrpNm;
	}

	public void setRgrpNm(String rgrpNm) {
		this.rgrpNm = rgrpNm;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserNm() {
		return userNm;
	}

	public void setUserNm(String userNm) {
		this.userNm = userNm;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAumthTpcdList() {
		return aumthTpcdList;
	}

	public void setAumthTpcdList(String aumthTpcdList) {
		this.aumthTpcdList = aumthTpcdList;
	}

	public String getDloginAlowYn() {
		return dloginAlowYn;
	}

	public String getInactvYn() {
		return inactvYn;
	}

	public void setInactvYn(String inactvYn) {
		this.inactvYn = inactvYn;
	}

	public String getLockYn() {
		return lockYn;
	}

	public void setLockYn(String lockYn) {
		this.lockYn = lockYn;
	}

	public int getLoginFailCnt() {
		return loginFailCnt;
	}

	public void setLoginFailCnt(int loginFailCnt) {
		this.loginFailCnt = loginFailCnt;
	}

	public String getPwchgDt() {
		return pwchgDt;
	}

	public void setPwchgDt(String pwchgDt) {
		this.pwchgDt = pwchgDt;
	}

	public int getSsvaldSec() {
		return ssvaldSec;
	}

	public void setSsvaldSec(int ssvaldSec) {
		this.ssvaldSec = ssvaldSec;
	}

	public String getUriId() {
		return uriId;
	}

	public void setUriId(String uriId) {
		this.uriId = uriId;
	}
}
