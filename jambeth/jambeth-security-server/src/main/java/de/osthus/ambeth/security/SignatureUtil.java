package de.osthus.ambeth.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.util.ParamChecker;

public class SignatureUtil implements IInitializingBean, ISignatureUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPasswordUtilIntern passwordUtilIntern;

	@Property(name = SecurityServerConfigurationConstants.SignatureAlgorithmName, defaultValue = "SHA1withECDSA")
	protected String algorithm;

	@Property(name = SecurityServerConfigurationConstants.SignatureKeyAlgorithmName, defaultValue = "EC")
	protected String keyFactoryAlgorithm;

	@Property(name = SecurityServerConfigurationConstants.SignatureKeySize, defaultValue = "384")
	protected int keySize;

	@Property(name = SecurityServerConfigurationConstants.SignaturePaddedKeyAlgorithmName, defaultValue = "PBKDF2WithHmacSHA1")
	protected String paddedKeyAlgorithm;

	@Property(name = SecurityServerConfigurationConstants.SignaturePaddedKeySize, defaultValue = "128")
	protected int paddedKeySize;

	@Property(name = SecurityServerConfigurationConstants.SignaturePaddedKeyIterationCount, defaultValue = "16")
	protected int paddedKeyIterations;

	@Property(name = SecurityServerConfigurationConstants.SignatureEncryptionKeySpecName, defaultValue = "AES")
	protected String encryptionKeySpec;

	@Property(name = SecurityServerConfigurationConstants.SignatureEncryptionAlgorithmName, defaultValue = "AES/CBC/PKCS5Padding")
	protected String encryptionAlgorithm;

	protected KeyPairGenerator keyGen;

	protected SecureRandom random;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		keyGen = KeyPairGenerator.getInstance(keyFactoryAlgorithm);
		random = SecureRandom.getInstance("SHA1PRNG");
	}

	@Override
	public void updateSignature(ISignature newEmptySignature, char[] clearTextPassword, IUser user)
	{
		ParamChecker.assertParamNotNull(newEmptySignature, "newEmptySignature");
		ParamChecker.assertParamNotNull(user, "user");

		try
		{
			newEmptySignature.setPaddedKeyAlgorithm(paddedKeyAlgorithm);
			newEmptySignature.setPaddedKeyIterations(paddedKeyIterations);
			newEmptySignature.setPaddedKeySalt(Base64.encodeBytes(PasswordSalts.nextSalt(paddedKeySize / 8)).toCharArray());
			newEmptySignature.setPaddedKeySize(paddedKeySize);
			newEmptySignature.setEncryptionKeySpec(encryptionKeySpec);
			newEmptySignature.setEncryptionAlgorithm(encryptionAlgorithm);
			newEmptySignature.setAlgorithm(algorithm);
			// important that the keyFactoryAlgorithm matches the keyGenerator algorithm here
			newEmptySignature.setKeyFactoryAlgorithm(keyFactoryAlgorithm);

			keyGen.initialize(keySize, random);
			KeyPair pair = keyGen.generateKeyPair();

			byte[] encryptedPrivateKey = encryptPrivateKey(newEmptySignature, pair.getPrivate().getEncoded(), clearTextPassword);

			newEmptySignature.setPublicKey(Base64.encodeBytes(pair.getPublic().getEncoded()).toCharArray());
			newEmptySignature.setPrivateKey(Base64.encodeBytes(encryptedPrivateKey).toCharArray());

			user.setSignature(newEmptySignature);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected byte[] doPaddingForPassword(ISignature signature, char[] clearTextPassword)
	{
		try
		{
			byte[] salt = Base64.decode(signature.getPaddedKeySalt());

			SecretKeyFactory f = SecretKeyFactory.getInstance(signature.getPaddedKeyAlgorithm());
			KeySpec ks = new PBEKeySpec(clearTextPassword, salt, signature.getPaddedKeyIterations(), signature.getPaddedKeySize());
			SecretKey s = f.generateSecret(ks);

			return s.getEncoded();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected byte[] encryptPrivateKey(ISignature signature, byte[] privateKey, char[] clearTextPassword)
	{
		try
		{
			// padd the password to match the required length for encryption
			byte[] paddedPassword = doPaddingForPassword(signature, clearTextPassword);

			// now we encrypt the private key with the password of the user - this is the reason why we can only generate signatures either
			// during a login of a user or during new user account creation.
			SecretKeySpec keySpec = new SecretKeySpec(paddedPassword, signature.getEncryptionKeySpec());
			Cipher cipher = Cipher.getInstance(signature.getEncryptionAlgorithm());

			byte[] initVector = PasswordSalts.nextSalt(cipher.getBlockSize());

			signature.setEncryptionKeyIV(Base64.encodeBytes(initVector).toCharArray());

			cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(Base64.decode(signature.getEncryptionKeyIV())));
			return cipher.doFinal(privateKey);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected byte[] decryptPrivateKey(ISignature signature, char[] clearTextPassword)
	{
		try
		{
			// padd the password to match the required length for decryption
			byte[] paddedPassword = doPaddingForPassword(signature, clearTextPassword);

			// decrypt the private key
			SecretKeySpec keySpec = new SecretKeySpec(paddedPassword, signature.getEncryptionKeySpec());
			Cipher cipher = Cipher.getInstance(signature.getEncryptionAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(Base64.decode(signature.getEncryptionKeyIV())));
			return cipher.doFinal(Base64.decode(signature.getPrivateKey()));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Signature createSignatureHandle(ISignature signature, char[] clearTextPassword)
	{
		try
		{
			byte[] decryptedPrivateKey = decryptPrivateKey(signature, clearTextPassword);

			// use the private key to create the signature handle
			PKCS8EncodedKeySpec decryptedPrivateKeySpec = new PKCS8EncodedKeySpec(decryptedPrivateKey);
			KeyFactory keyFactory = KeyFactory.getInstance(signature.getKeyFactoryAlgorithm());
			PrivateKey privateKeyHandle = keyFactory.generatePrivate(decryptedPrivateKeySpec);
			Signature jSignature = java.security.Signature.getInstance(signature.getAlgorithm());
			jSignature.initSign(privateKeyHandle, random);
			return jSignature;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Signature createVerifyHandle(ISignature signature)
	{
		try
		{
			// decode the public key from base64
			byte[] decodedPublicKey = Base64.decode(signature.getPublicKey());

			// use the public key to create the signature handle
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);
			KeyFactory keyFactory = KeyFactory.getInstance(signature.getKeyFactoryAlgorithm());
			PublicKey publicKeyHandle = keyFactory.generatePublic(keySpec);
			Signature jSignature = java.security.Signature.getInstance(signature.getAlgorithm());
			jSignature.initVerify(publicKeyHandle);
			return jSignature;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
