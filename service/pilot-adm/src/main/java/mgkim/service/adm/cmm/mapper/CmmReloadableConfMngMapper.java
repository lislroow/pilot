package mgkim.service.adm.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.framework.cmm.online.vo.CmmReloadableConfVO;

@Mapper
public interface CmmReloadableConfMngMapper {

	public List<CmmReloadableConfVO> selectReloadStateList(CmmReloadableConfVO vo) throws Exception;

	public int updateReloadState(CmmReloadableConfVO vo) throws Exception;

	public int deleteReloadStateForReset(CmmReloadableConfVO vo) throws Exception;

	public int updateReloadStateForReset(CmmReloadableConfVO vo) throws Exception;

}
