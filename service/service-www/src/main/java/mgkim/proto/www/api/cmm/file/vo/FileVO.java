package mgkim.proto.www.api.cmm.file.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import mgkim.online.com.dto.KCmmVO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileVO extends KCmmVO {

	private String fgrpId;
	private String fileId;
	private String orgFilenm;
	private String saveFilenm;
	private String saveFpath;
	private String fileTpcd;
	private String fileExt;
	private long fileSize;
	private long cksum;
	private String crptYn;
	private String zipYn;
	private String useYn;

	public static class Builder {

		public Builder() { }

		private String fgrpId;
		private String fileId;
		private String orgFilenm;
		private String saveFilenm;
		private String saveFpath;
		private String fileTpcd;
		private String fileExt;
		private long fileSize;
		private long cksum;
		private String crptYn;
		private String zipYn;
		private String useYn;

		public Builder fgrpId(String fgrpId) {
			this.fgrpId = fgrpId;
			return this;
		}
		public Builder fileId(String fileId) {
			this.fileId = fileId;
			return this;
		}
		public Builder orgFilenm(String orgFilenm) {
			this.orgFilenm = orgFilenm;
			return this;
		}
		public Builder saveFilenm(String saveFilenm) {
			this.saveFilenm = saveFilenm;
			return this;
		}
		public Builder saveFpath(String saveFpath) {
			this.saveFpath = saveFpath;
			return this;
		}
		public Builder fileTpcd(String fileTpcd) {
			this.fileTpcd = fileTpcd;
			return this;
		}
		public Builder fileExt(String fileExt) {
			this.fileExt = fileExt;
			return this;
		}
		public Builder fileSize(long fileSize) {
			this.fileSize = fileSize;
			return this;
		}
		public Builder cksum(long cksum) {
			this.cksum = cksum;
			return this;
		}
		public Builder crptYn(String crptYn) {
			this.crptYn = crptYn;
			return this;
		}
		public Builder zipYn(String zipYn) {
			this.zipYn = zipYn;
			return this;
		}
		public Builder useYn(String useYn) {
			this.useYn = useYn;
			return this;
		}

		public FileVO build() {
			return new FileVO(fgrpId, fileId, orgFilenm, saveFilenm, saveFpath, fileTpcd, fileExt, fileSize, cksum, crptYn, zipYn, useYn);
		}
	}

	public FileVO() { }

	public FileVO(String fgrpId, String fileId, String orgFilenm, String saveFilenm, String saveFpath, String fileTpcd, String fileExt,
			long fileSize, long cksum, String crptYn, String zipYn, String useYn) {
		this.fgrpId = fgrpId;
		this.fileId = fileId;
		this.orgFilenm = orgFilenm;
		this.saveFilenm = saveFilenm;
		this.saveFpath = saveFpath;
		this.fileTpcd = fileTpcd;
		this.fileExt = fileExt;
		this.fileSize = fileSize;
		this.cksum = cksum;
		this.crptYn = crptYn;
		this.zipYn = zipYn;
		this.useYn = useYn;
	}

	public String getFgrpId() {
		return fgrpId;
	}

	public void setFgrpId(String fgrpId) {
		this.fgrpId = fgrpId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getOrgFilenm() {
		return orgFilenm;
	}

	public void setOrgFilenm(String orgFilenm) {
		this.orgFilenm = orgFilenm;
	}

	public String getSaveFilenm() {
		return saveFilenm;
	}

	public void setSaveFilenm(String saveFilenm) {
		this.saveFilenm = saveFilenm;
	}

	public String getSaveFpath() {
		return saveFpath;
	}

	public void setSaveFpath(String saveFpath) {
		this.saveFpath = saveFpath;
	}

	public String getFileTpcd() {
		return fileTpcd;
	}

	public void setFileTpcd(String fileTpcd) {
		this.fileTpcd = fileTpcd;
	}

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getCksum() {
		return cksum;
	}

	public void setCksum(long cksum) {
		this.cksum = cksum;
	}

	public String getCrptYn() {
		return crptYn;
	}

	public void setCrptYn(String crptYn) {
		this.crptYn = crptYn;
	}

	public String getZipYn() {
		return zipYn;
	}

	public void setZipYn(String zipYn) {
		this.zipYn = zipYn;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}
}
