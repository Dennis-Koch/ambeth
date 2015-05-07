package de.osthus.ambeth.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.ISignAndVerify;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.util.ParamChecker;

public class SignatureUtil implements IInitializingBean, ISignatureUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPBEncryptor pbEncryptor;

	@Property(name = SecurityServerConfigurationConstants.SignatureAlgorithmName, defaultValue = "SHA1withECDSA")
	protected String algorithm;

	@Property(name = SecurityServerConfigurationConstants.SignatureKeyAlgorithmName, defaultValue = "EC")
	protected String keyFactoryAlgorithm;

	@Property(name = SecurityServerConfigurationConstants.SignatureKeySize, defaultValue = "384")
	protected int keySize;

	protected KeyPairGenerator keyGen;

	protected SecureRandom random;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		keyGen = KeyPairGenerator.getInstance(keyFactoryAlgorithm);
		random = SecureRandom.getInstance("SHA1PRNG");
	}

	@Override
	public void generateNewSignature(ISignature newEmptySignature, char[] clearTextPassword)
	{
		ParamChecker.assertParamNotNull(clearTextPassword, "clearTextPassword");
		try
		{
			newEmptySignature.getSignAndVerify().setSignatureAlgorithm(algorithm);
			// important that the keyFactoryAlgorithm matches the keyGenerator algorithm here
			newEmptySignature.getSignAndVerify().setKeyFactoryAlgorithm(keyFactoryAlgorithm);

			keyGen.initialize(keySize, random);
			KeyPair pair = keyGen.generateKeyPair();

			byte[] unencryptedPrivateKey = pair.getPrivate().getEncoded();
			byte[] encryptedPrivateKey = pbEncryptor.encrypt(newEmptySignature.getPBEConfiguration(), clearTextPassword, unencryptedPrivateKey);

			newEmptySignature.setPublicKey(Base64.encodeBytes(pair.getPublic().getEncoded()).toCharArray());
			newEmptySignature.setPrivateKey(Base64.encodeBytes(encryptedPrivateKey).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void reencryptSignature(ISignature signature, char[] oldClearTextPassword, char[] newClearTextPassword)
	{
		try
		{
			byte[] encryptedPrivateKey = Base64.decode(signature.getPrivateKey());
			byte[] decryptedPrivateKey = pbEncryptor.decrypt(signature.getPBEConfiguration(), oldClearTextPassword, encryptedPrivateKey);
			encryptedPrivateKey = pbEncryptor.encrypt(signature.getPBEConfiguration(), newClearTextPassword, decryptedPrivateKey);
			signature.setPrivateKey(Base64.encodeBytes(encryptedPrivateKey).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Signature createSignatureHandle(ISignAndVerify signAndVerify, byte[] privateKey)
	{
		try
		{
			// use the private key to create the signature handle
			PKCS8EncodedKeySpec decryptedPrivateKeySpec = new PKCS8EncodedKeySpec(privateKey);
			KeyFactory keyFactory = KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm());
			PrivateKey privateKeyHandle = keyFactory.generatePrivate(decryptedPrivateKeySpec);
			Signature jSignature = java.security.Signature.getInstance(signAndVerify.getSignatureAlgorithm());
			jSignature.initSign(privateKeyHandle, random);
			return jSignature;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Signature createVerifyHandle(ISignAndVerify signAndVerify, byte[] publicKey)
	{
		try
		{
			// // decode the public key from base64
			// byte[] decodedPublicKey = Base64.decode(signature.getPublicKey());

			// use the public key to create the signature handle
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
			KeyFactory keyFactory = KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm());
			PublicKey publicKeyHandle = keyFactory.generatePublic(keySpec);
			Signature jSignature = java.security.Signature.getInstance(signAndVerify.getSignatureAlgorithm());
			jSignature.initVerify(publicKeyHandle);
			return jSignature;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
