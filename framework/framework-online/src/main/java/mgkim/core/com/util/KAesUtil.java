package mgkim.core.com.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import mgkim.core.com.type.TEncodingType;

public class KAesUtil {

	private static volatile KAesUtil INST;

	static final String AES = "AES";
	static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

	public static KAesUtil getInstance() {
		if(INST == null) {
			synchronized(KAesUtil.class) {
				if(INST == null) {
					INST = new KAesUtil();
				}
			}
		}
		return INST;
	}

	public static String encrypt(String plainText, String b64Key) {
		byte[] keybuf = Base64.getDecoder().decode(b64Key.getBytes());
		byte[] ivbuf = new byte[16];
		new Random().nextBytes(ivbuf);
		String encrypted = null;
		try {
			encrypted = KAesUtil.encrypt(plainText,keybuf,ivbuf);
		} catch(InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidAlgorithmParameterException
				| IOException e) {
		}
		return encrypted;
	}

	private static final String encrypt(final String plainText, final byte[] keybuf, final byte[] ivbuf) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keybuf, AES), new IvParameterSpec(ivbuf));
		byte[] encrypted = cipher.doFinal(plainText.getBytes(TEncodingType.UTF8.code()));
		String encStr = Base64.getEncoder().encodeToString(ivbuf) + ":" + Base64.getEncoder().encodeToString(encrypted);
		return encStr;
	}

	public static String decrypt(String encrypted, String b64Key) {
		String data[] = encrypted.split(":");
		String iv = data[0];
		byte[] encryptedByteData = Base64.getDecoder().decode(data[1]);
		IvParameterSpec ivspec = new IvParameterSpec(Base64.getDecoder().decode(iv));
		Key key = new SecretKeySpec(Base64.getDecoder().decode(b64Key), AES);
		String decrypted = null;
		try {
			decrypted = KAesUtil.decrypt(Base64.getEncoder().encodeToString(encryptedByteData), key, ivspec);
		} catch(InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidAlgorithmParameterException
				| IOException e) {
		}
		return decrypted;
	}

	private static final String decrypt(final String encrypted, final Key key, final IvParameterSpec iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, key,iv);
		byte[] raw = Base64.getDecoder().decode(encrypted);
		byte[] stringBytes = cipher.doFinal(raw);
		String clearText = new String(stringBytes, TEncodingType.UTF8.code());
		return clearText;
	}
}