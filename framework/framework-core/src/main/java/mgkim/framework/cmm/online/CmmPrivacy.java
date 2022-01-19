package mgkim.framework.cmm.online;

import java.util.List;

import org.springframework.beans.BeansException;

import mgkim.framework.cmm.online.vo.CmmPrivacyLogVO;
import mgkim.framework.cmm.online.vo.CmmPrivacyMngVO;
import mgkim.framework.core.annotation.KModule;

@KModule(name = "개인정보접근이력 관리", required = false)
public interface CmmPrivacy {

	public void init() throws BeansException;

	public List<CmmPrivacyMngVO> selectMngAll(CmmPrivacyMngVO vo) throws Exception;

	public void insertLog(CmmPrivacyLogVO vo) throws Exception;

}
