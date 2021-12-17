package mgkim.framework.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.util.StopWatch;

import mgkim.framework.online.com.logging.KLogSys;

public class KRsaUtil {

	public static KeyPair generate(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
		SecureRandom random = new SecureRandom();
		//KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SunJSSE");
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

		generator.initialize(keySize, random);
		StopWatch watch = new StopWatch("generateRSAKey");
		watch.start();
		KeyPair pair = generator.generateKeyPair();
		watch.stop();
		KLogSys.info("RSA KEY SIZE={}, {}", keySize, watch.shortSummary());
		KLogSys.info("구간암호화 키 생성. rsa key size={}, {} sec elapsed.", keySize, watch.getTotalTimeSeconds());
		Key pubKey = pair.getPublic();
		Key privKey = pair.getPrivate();

		KLogSys.info("====================== B64 STRING ======================");
		KLogSys.info("pubKeyB64:"+Base64.getEncoder().encodeToString(pubKey.getEncoded()));
		KLogSys.info("privKeyB64:"+Base64.getEncoder().encodeToString(privKey.getEncoded()));

		return pair;
	}

	public static final String encrypt(final String plainText, final String pubKeyStr) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		//Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding", "SunJCE");
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding");

		X509EncodedKeySpec ukeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyStr));
		KeyFactory ukeyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = null;
		try {
			publicKey = ukeyFactory.generatePublic(ukeySpec);
			KLogSys.info("pubKeyB64:"+Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		} catch (InvalidKeySpecException e) {
			KLogSys.error("", e);
		}

		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

		return Base64.getEncoder().encodeToString(encrypted);
	}

	public static final String decrypt(final String cipherText, final String privKeyStr) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException {
		//Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding", "SunJCE");
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding");

		PKCS8EncodedKeySpec rkeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privKeyStr));
		KeyFactory rkeyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = null;
		try {
			privateKey = rkeyFactory.generatePrivate(rkeySpec);
			KLogSys.info("privKeyB64:"+Base64.getEncoder().encodeToString(privateKey.getEncoded()));
		} catch (InvalidKeySpecException e) {
			KLogSys.error("", e);
		}

		// 개인키를 가지고있는쪽에서 복호화
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] raw = Base64.getDecoder().decode(cipherText);
		byte[] stringBytes = cipher.doFinal(raw);
		String clearText = new String(stringBytes, "UTF8");

		return clearText;
	}
}