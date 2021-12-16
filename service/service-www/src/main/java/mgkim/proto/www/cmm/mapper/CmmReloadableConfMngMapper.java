package mgkim.proto.www.cmm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import mgkim.core.cmm.vo.reloadableconf.CmmReloadableConfVO;

@Mapper
public interface CmmReloadableConfMngMapper {

	public List<CmmReloadableConfVO> selectReloadStateList(CmmReloadableConfVO vo) throws Exception;

	public int updateReloadState(CmmReloadableConfVO vo) throws Exception;

	public int deleteReloadStateForReset(CmmReloadableConfVO vo) throws Exception;

	public int updateReloadStateForReset(CmmReloadableConfVO vo) throws Exception;

}
