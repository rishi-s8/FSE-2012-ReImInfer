package encryption;

public class Conversion {
	
	public static Encryption rnd = new Random();
	public static Encryption ope = new OrderPreserving();
	public static Encryption ah = new Homomorphic();
	public static Encryption det = new Deterministic();

	public static byte[] encrypt(int ptext, String to) {
		Encryption toType = createEncryption(to);
		byte[] e = toType.encrypt(ptext);
		return e;
	}
	
	public static byte[][] encryptSpe(int ptext, String to) {
		String s = Integer.toString(ptext);
		return createEncryption(to).encrypt(s);
	}
	
	public static byte[][] encrypt(String ptext, String to) {
		Encryption toType = createEncryption(to);
		return toType.encrypt(ptext);
	}

	public static int decrypt(byte[] ctext, String from) {
		Encryption fromType = createEncryption(from);
		int e = fromType.decrypt(ctext);
		return e;
	}
	
	public static String decrypt(byte[][] ctext, String from) {
		Encryption fromType = createEncryption(from);
		return fromType.decrypt(ctext);
	}

	public static byte[] convert(byte[] ctext, String from, String to) {
		int ptext = decrypt(ctext, from);
		return encrypt(ptext, to);
	}
	
	public static byte[][] convertSpe(byte[] ctext, String from, String to) {
		int ptext = decrypt(ctext, from);
		String s = String.valueOf(ptext);
		return encrypt(s, to);
	}

	public static byte[][] convert(byte[][] ctext, String from, String to) {
		String ptext = decrypt(ctext, from);
		return encrypt(ptext, to);
	}

	private static Encryption createEncryption(String type) {
		switch (type) {
		case "RND":
			return rnd;
		case "OPE":
			return ope;
		case "AH":
			return ah;
		default:
			assert (type.equals("DET"));
			return det;
		}
	}

}
