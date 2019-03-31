package hk.siggi.rsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

/**
 * RSA utility for loading and saving keys.
 * 
 * @author Sigurdur Helgason
 */
public class RSAFileManager {
	/**
	 * Saves a keypair in a directory, with file names public.key and private.key.
	 */
	public static void save(File directory, KeyPair keyPair) throws Exception {
		savePublic(new File(directory, "public.key"), keyPair.getPublic());
		savePrivate(new File(directory, "private.key"), keyPair.getPrivate());
	}
	/**
	 * Saves a public key.
	 */
	public static void savePublic(File publicKeyFile, PublicKey publicKey) throws Exception {
		X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream out = new FileOutputStream(publicKeyFile);
		out.write(DatatypeConverter.printBase64Binary(publicSpec.getEncoded())
				.getBytes());
		out.close();
	}
	/**
	 * Saves a private key.
	 */
	public static void savePrivate(File privateKeyFile, PrivateKey privateKey) throws Exception {
		PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		FileOutputStream out = new FileOutputStream(privateKeyFile);
		out.write(DatatypeConverter.printBase64Binary(privateSpec.getEncoded())
				.getBytes());
		out.close();
	}
	/**
	 * Loads a keypair in a directory, with file names public.key and private.key.
	 */
	public static KeyPair load(File directory) throws Exception {
		return new KeyPair(loadPublic(new File(directory, "public.key")), loadPrivate(new File(directory, "private.key")));
	}
	/**
	 * Loads a public key.
	 */
	public static PublicKey loadPublic(File publicKeyFile) throws Exception {
		FileInputStream in = new FileInputStream(publicKeyFile);
		byte[] encodedPublicKey = new byte[(int) publicKeyFile.length()];
		in.read(encodedPublicKey);
		encodedPublicKey = DatatypeConverter.parseBase64Binary(new String(
				encodedPublicKey));
		in.close();

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		return publicKey;
	}
	/**
	 * Loads a private key.
	 */
	public static PrivateKey loadPrivate(File privateKeyFile) throws Exception {
		FileInputStream in = new FileInputStream(privateKeyFile);
		byte[] encodedPrivateKey = new byte[(int) privateKeyFile.length()];
		in.read(encodedPrivateKey);
		encodedPrivateKey = DatatypeConverter.parseBase64Binary(new String(
				encodedPrivateKey));
		in.close();

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return privateKey;
	}

}
