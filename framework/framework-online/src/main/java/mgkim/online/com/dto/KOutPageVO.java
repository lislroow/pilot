package mgkim.online.com.dto;

import mgkim.online.com.exception.KMessage;
import mgkim.online.com.exception.KSysException;
import mgkim.online.com.util.KSqlUtil;

public class KOutPageVO {

	// 입력값
	private Integer pageindex;
	private Integer rowunit;
	private Integer pageunit;
	private Integer rowcount;

	// 결과값
	private int pagecount;
	private int firstpage;
	private int lastpage;
	private int startpage;
	private int endpage;
	private int startrow;
	private int endrow;

	public static class Builder {
		public Builder() { }

		// 입력값
		private Integer pageindex;
		private Integer rowunit;
		private Integer pageunit;
		private Integer rowcount;

		// 결과값
		private int pagecount;
		private int firstpage;
		private int lastpage;
		private int startpage;
		private int endpage;
		private int startrow;
		private int endrow;

		public Builder pageindex(Integer pageindex) {
			this.pageindex = pageindex;
			return this;
		}
		public Builder rowunit(Integer rowunit) {
			this.rowunit = rowunit;
			return this;
		}
		public Builder pageunit(Integer pageunit) {
			this.pageunit = pageunit;
			return this;
		}
		public Builder rowcount(Integer rowcount) {
			this.rowcount = rowcount;
			return this;
		}

		public KOutPageVO build() throws KSysException {
			// 입력값 검증
			{
				if(pageindex == null || pageindex <= 0) {
					pageindex = 1;
				}
				if(rowunit == null || rowunit == 0) {
					rowunit = KSqlUtil.PAGING_RECORD_COUNT_PER_PAGE;
				}
				if(pageunit == null || pageunit == 0) {
					pageunit = KSqlUtil.PAGING_PAGE_SIZE;
				}
				if(rowcount == null) {
					throw new KSysException(KMessage.E8101);
				}
			}

			// 계산
			{
				// `pagecount` 전체 페이지 개수
				{
					if((rowcount - 1) == 0) {
						pagecount = 1;
					}
					pagecount = ((rowcount - 1) / rowunit) + 1;
				}

				// `firstpage`
				{
					firstpage = 1;
				}

				// `lastpage`
				{
					lastpage = pagecount;
				}

				// `startpage`
				{
					startpage = ((pageindex - 1) / pageunit) * pageunit + 1;
				}

				// `endpage`
				{
					endpage = startpage + pageunit - 1;
					if(endpage > pagecount) {
						endpage = pagecount;
					}
				}

				// `startrow`
				{
					startrow = (pageindex - 1) * rowunit + 1;
				}

				// `endrow`
				{
					endrow = pageindex * rowunit;
					if(endrow > rowcount) {
						endrow = rowcount;
					}
				}
			}

			// 결과
			return new KOutPageVO(pageindex, rowunit, pageunit, rowcount, pagecount, firstpage, lastpage, startpage, endpage, startrow, endrow);
		}
	}

	public KOutPageVO(Integer pageindex, Integer rowunit, Integer pageunit, Integer rowcount, int pagecount, int firstpage,
			int lastpage, int startpage, int endpage, int startrow, int endrow) {
		super();
		this.pageindex = pageindex;
		this.rowunit = rowunit;
		this.pageunit = pageunit;
		this.rowcount = rowcount;
		this.pagecount = pagecount;
		this.firstpage = firstpage;
		this.lastpage = lastpage;
		this.startpage = startpage;
		this.endpage = endpage;
		this.startrow = startrow;
		this.endrow = endrow;
	}

	public Integer getPageindex() {
		return pageindex;
	}

	public void setPageindex(Integer pageindex) {
		this.pageindex = pageindex;
	}

	public Integer getRowunit() {
		return rowunit;
	}

	public void setRowunit(Integer rowunit) {
		this.rowunit = rowunit;
	}

	public Integer getPageunit() {
		return pageunit;
	}

	public void setPageunit(Integer pageunit) {
		this.pageunit = pageunit;
	}

	public Integer getRowcount() {
		return rowcount;
	}

	public void setRowcount(Integer rowcount) {
		this.rowcount = rowcount;
	}

	public int getPagecount() {
		return pagecount;
	}

	public void setPagecount(int pagecount) {
		this.pagecount = pagecount;
	}

	public int getFirstpage() {
		return firstpage;
	}

	public void setFirstpage(int firstpage) {
		this.firstpage = firstpage;
	}

	public int getLastpage() {
		return lastpage;
	}

	public void setLastpage(int lastpage) {
		this.lastpage = lastpage;
	}

	public int getStartpage() {
		return startpage;
	}

	public void setStartpage(int startpage) {
		this.startpage = startpage;
	}

	public int getEndpage() {
		return endpage;
	}

	public void setEndpage(int endpage) {
		this.endpage = endpage;
	}

	public int getStartrow() {
		return startrow;
	}

	public void setStartrow(int startrow) {
		this.startrow = startrow;
	}

	public int getEndrow() {
		return endrow;
	}

	public void setEndrow(int endrow) {
		this.endrow = endrow;
	}
}
