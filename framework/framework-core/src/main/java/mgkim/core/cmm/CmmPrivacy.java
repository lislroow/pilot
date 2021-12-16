package mgkim.core.cmm;

import java.util.List;

import org.springframework.beans.BeansException;

import mgkim.core.cmm.vo.privacy.CmmPrivacyLogVO;
import mgkim.core.cmm.vo.privacy.CmmPrivacyMngVO;
import mgkim.core.com.annotation.KModule;

@KModule(name = "개인정보접근이력 관리", required = false)
public interface CmmPrivacy {

	public void init() throws BeansException;

	public List<CmmPrivacyMngVO> selectMngAll(CmmPrivacyMngVO vo) throws Exception;

	public void insertLog(CmmPrivacyLogVO vo) throws Exception;

}
