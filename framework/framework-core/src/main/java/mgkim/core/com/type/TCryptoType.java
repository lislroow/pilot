package mgkim.core.com.type;

public enum TCryptoType {

	RSA("rsa"), AES("aes");

	private final String code;

	public String code() {
		return code;
	}

	private TCryptoType(String code) {
		this.code = code;
	}
}
