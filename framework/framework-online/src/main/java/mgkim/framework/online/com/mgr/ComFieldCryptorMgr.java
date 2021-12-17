package mgkim.framework.online.com.mgr;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.annotation.KEncrypt;
import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogSys;
import mgkim.framework.core.session.KToken;
import mgkim.framework.core.util.KAesUtil;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KRsaUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.cmm.CmmFieldCryptor;
import mgkim.framework.online.cmm.vo.fieldcryptor.CmmFieldCryptoVO;

@KBean(name = "필드암호화키 관리")
public class ComFieldCryptorMgr implements InitializingBean {

	final String BEAN_NAME = KObjectUtil.name(ComFieldCryptorMgr.class);

	@Autowired(required = false)
	private CmmFieldCryptor cmmFieldCryptor;

	@Override
	public void afterPropertiesSet() throws Exception {
		if(cmmFieldCryptor == null) {
			String clazzName = CmmFieldCryptor.class.getSimpleName();
			throw new KSysException(KMessage.E5001, clazzName);
		}
	}

	public String createRsaKey(KToken token) throws Exception {
		String privateKey = null;
		String publicKey = null;
		try {
			KeyPair keyPair = KRsaUtil.generate(2048);
			PrivateKey privateKeyObj = keyPair.getPrivate();
			PublicKey publicKeyObj = keyPair.getPublic();
			privateKey = Base64.getEncoder().encodeToString(privateKeyObj.getEncoded());
			publicKey = Base64.getEncoder().encodeToString(publicKeyObj.getEncoded());
		} catch(Exception e) {
			throw new KSysException(KMessage.E6201, e);
		}

		try {
			CmmFieldCryptoVO vo = new CmmFieldCryptoVO();
			vo.setSsid(token.getSsid());
			vo.setSsuserId(token.getUserId());
			vo.setAumthTpcd(token.getAumthTpcd());
			vo.setPrivateKey(privateKey);
			vo.setPublicKey(publicKey);
			cmmFieldCryptor.saveRsaKey(vo);
		} catch(Exception e) {
			throw KExceptionHandler.resolve(e);
		}
		return publicKey;
	}

	public void saveSymKey(String clientSecretKey) throws Exception {
		CmmFieldCryptoVO vo = new CmmFieldCryptoVO();
		vo.setSymKey(clientSecretKey);
		try {
			cmmFieldCryptor.saveSymKey(vo);
		} catch(Exception e) {
			throw KExceptionHandler.resolve(e);
		}
	}

	// 암호화 필드 형식: ENC('...암호문...')
	public void decrypt(KInDTO<?> inDTO) throws Exception {
		Object obj = inDTO.getBody();
		if(hasSecuredField(obj)) {
			KToken token = KContext.getT(AttrKey.TOKEN);
			CmmFieldCryptoVO keyVO = cmmFieldCryptor.selectFieldCryptoKey(token);
			if(keyVO == null || KStringUtil.isEmpty(keyVO.getPrivateKey())) {
				throw new KSysException(KMessage.E6204);
			}
			decrypt(obj, keyVO.getPrivateKey());
		}
	}

	// 암호화 필드 형식: ENC('...암호문...')
	public void encrypt(KOutDTO<?> outDTO) throws Exception {
		Object obj = outDTO.getBody();
		if(hasSecuredField(obj)) {
			KToken token = KContext.getT(AttrKey.TOKEN);
			CmmFieldCryptoVO keyVO = cmmFieldCryptor.selectFieldCryptoKey(token);
			if(keyVO == null || KStringUtil.isEmpty(keyVO.getSymKey())) {
				throw new KSysException(KMessage.E6205);
			}
			encrypt(obj, keyVO.getSymKey());
		}
	}


	private void decrypt(Object obj, String privateKey) throws Exception {
		if(obj == null) {
			return;
		}
		if(obj instanceof java.util.List) {
			List list = (List) obj;
			Iterator iter = list.iterator();
			while(iter.hasNext()) {
				Object item = iter.next();
				if(item instanceof KCmmVO) {
					decrypt(item, privateKey);
				}
			}
		} else {
			java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
			for(int i=0; i<fields.length; i++) {
				if(fields[i].getType() == java.util.List.class) {
					List list = (List)KObjectUtil.getValue(obj, fields[i].getName());
					if(list == null) {
						continue;
					}
					Iterator iter = list.iterator();
					while(iter.hasNext()) {
						Object item = iter.next();
						if(item instanceof KCmmVO) {
							decrypt(item, privateKey);
						}
					}
				}
				try {
					String value = KStringUtil.nvl(KObjectUtil.getValue(obj, fields[i].getName()));
					KEncrypt annotation =  fields[i].getDeclaredAnnotation(KEncrypt.class);
					if(annotation != null) {
						if(KStringUtil.isEmpty(value)) {
							KLogSys.info("암호화 필드가 빈 문자열 입니다. : 필드명={}", fields[i].getName());
							continue;
						}
						if(!value.startsWith("ENC('")) {
							KLogSys.info("암호화 필드가 ENC('') 포맷이 아닙니다. : 필드명={}", fields[i].getName());
							continue;
						}
						if(KStringUtil.isEmpty(privateKey)) {
							throw new KSysException(KMessage.E6204);
						}
						String encValue = value.substring("ENC(\'".length(), value.length() - "')".length());
						KLogSys.debug("서버(개인키) : API_SVR_PRKY={}", privateKey);
						KLogSys.debug("암호문 : value={}", value);
						String decValue = KRsaUtil.decrypt(encValue, privateKey);
						KLogSys.debug("복호문 : decValue={}", decValue);
						KObjectUtil.setValue(obj, fields[i].getName(), decValue);
					}
				} catch(Exception e) {
					throw new KSysException(KMessage.E6207, e);
				}
			}
		}
	}


	private void encrypt(Object obj, String cryptoClntSyky) throws Exception {
		if(obj == null) {
			return;
		}
		if(obj instanceof java.util.List) {
			List list = (List) obj;
			Iterator iter = list.iterator();
			while(iter.hasNext()) {
				Object item = iter.next();
				if(item instanceof KCmmVO) {
					encrypt(item, cryptoClntSyky);
				}
			}
		} else {
			java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
			for(int i=0; i<fields.length; i++) {
				if(fields[i].getType() == java.util.List.class) {
					List list = (List)KObjectUtil.getValue(obj, fields[i].getName());
					if(list == null) {
						continue;
					}
					Iterator iter = list.iterator();
					while(iter.hasNext()) {
						Object item = iter.next();
						if(item instanceof KCmmVO) {
							encrypt(item, cryptoClntSyky);
						}
					}
				}
				if(fields[i].getDeclaringClass().getSimpleName().endsWith("VO")) {
					Object voObj = KObjectUtil.getValue(obj, fields[i].getName());
					if(voObj == null) {
						continue;
					}
					java.lang.reflect.Field[] voFields = voObj.getClass().getDeclaredFields();
					for(int j=0; j<voFields.length; j++) {
						Object voVal = KObjectUtil.getValue(voObj, voFields[j].getName());
						if(voVal == null) {
							continue;
						}
						encrypt(voVal, cryptoClntSyky);
					}
				}
				try {
					String value = KStringUtil.nvl(KObjectUtil.getValue(obj, fields[i].getName()));
					KEncrypt annotation =  fields[i].getDeclaredAnnotation(KEncrypt.class);
					if(annotation != null) {
						if(KStringUtil.isEmpty(value)) {
							KLogSys.info("암호화 필드가 빈 문자열 입니다. : 필드명={}", fields[i].getName());
							continue;
						}
						if(KStringUtil.isEmpty(cryptoClntSyky)) {
							throw new KSysException(KMessage.E6205);
						}

						String encValue = KAesUtil.encrypt(value, cryptoClntSyky);
						KLogSys.debug("클라이언트 암호화키(대칭키,AES) : CLNT_SYKY={}", cryptoClntSyky);
						KLogSys.debug("평문 : value={}", value);
						encValue = String.format("ENC('%s')", encValue);
						KLogSys.debug("암호문 : encValue={}", encValue);
						KObjectUtil.setValue(obj, fields[i].getName(), encValue);
					}
				} catch(Exception e) {
					throw new KSysException(KMessage.E6206, e);
				}
			}
		}
	}


	private boolean hasSecuredField(Object obj) throws Exception {
		if(obj == null) {
			return false;
		}

		if(obj instanceof java.util.List) {
			List list = (List) obj;
			if(list.size() > 0) {
				if(hasSecuredField(list.get(0))) {
					return true;
				}
			}
		}

		java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
		if(fields == null) {
			return false;
		}

		for(int i=0; i<fields.length; i++) {
			if(fields[i].getType() == java.util.List.class) {
				List list = (List)KObjectUtil.getValue(obj, fields[i].getName());
				if(list == null) {
					continue;
				}
				Iterator iter = list.iterator();
				while(iter.hasNext()) {
					if(hasSecuredField(iter.next())) {
						return true;
					}
				}
			}
			try {
				if(fields[i].getDeclaredAnnotation(KEncrypt.class) != null) {
					KLogSys.warn("암호화 필드가 있음 : 확인된 필드명={}", fields[i].getName());
					return true;
				}
			} catch(Exception e) {
				throw new KSysException(KMessage.E6208, e);
			}
		}
		return false;
	}
}
