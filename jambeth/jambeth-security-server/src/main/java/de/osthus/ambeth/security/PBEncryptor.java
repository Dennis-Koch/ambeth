package de.osthus.ambeth.security;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPBEConfiguration;

public class PBEncryptor implements IPBEncryptor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = SecurityServerConfigurationConstants.EncryptionKeySpecName, defaultValue = "AES")
	protected String encryptionKeySpec;

	@Property(name = SecurityServerConfigurationConstants.EncryptionAlgorithmName, defaultValue = "AES/CBC/PKCS5Padding")
	protected String encryptionAlgorithm;

	@Property(name = SecurityServerConfigurationConstants.EncryptionPaddedKeyAlgorithmName, defaultValue = "PBKDF2WithHmacSHA1")
	protected String paddedKeyAlgorithm;

	@Property(name = SecurityServerConfigurationConstants.EncryptionPaddedKeyIterationCount, defaultValue = "8192")
	protected int paddedKeyIterations;

	@Property(name = SecurityServerConfigurationConstants.EncryptionPaddedKeySize, defaultValue = "128")
	protected int paddedKeySize;

	@Property(name = SecurityServerConfigurationConstants.EncryptionPaddedKeySaltSize, defaultValue = "128")
	protected int paddedKeySaltSize;

	@Override
	public byte[] doPaddingForPassword(IPBEConfiguration pbeConfiguration, char[] clearTextPassword)
	{
		try
		{
			byte[] salt = pbeConfiguration.getPaddedKeySalt() != null ? Base64.decode(pbeConfiguration.getPaddedKeySalt()) : new byte[1];

			SecretKeyFactory f = SecretKeyFactory.getInstance(pbeConfiguration.getPaddedKeyAlgorithm());
			KeySpec ks = new PBEKeySpec(clearTextPassword, salt, pbeConfiguration.getPaddedKeyIterations(), pbeConfiguration.getPaddedKeySize());
			SecretKey s = f.generateSecret(ks);

			return s.getEncoded();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean isReencryptionRecommended(IPBEConfiguration pbeConfiguration)
	{
		if (!encryptionAlgorithm.equals(pbeConfiguration.getEncryptionAlgorithm()))
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (!encryptionKeySpec.equals(pbeConfiguration.getEncryptionKeySpec()))
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (!paddedKeyAlgorithm.equals(pbeConfiguration.getPaddedKeyAlgorithm()))
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (paddedKeyIterations != pbeConfiguration.getPaddedKeyIterations())
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (paddedKeySize != pbeConfiguration.getPaddedKeySize())
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (pbeConfiguration.getPaddedKeySalt() != null)
		{
			if (paddedKeySaltSize != pbeConfiguration.getPaddedKeySaltSize())
			{
				// recommended algorithm configuration changed
				return true;
			}
		}
		return false;
	}

	@Override
	public byte[] decrypt(IPBEConfiguration pbeConfiguration, char[] clearTextPassword, byte[] dataToDecrypt)
	{
		try
		{
			// padd the password to match the required length for decryption
			byte[] paddedPassword = doPaddingForPassword(pbeConfiguration, clearTextPassword);

			// decrypt the private key
			SecretKeySpec keySpec = new SecretKeySpec(paddedPassword, pbeConfiguration.getEncryptionKeySpec());
			Cipher cipher = Cipher.getInstance(pbeConfiguration.getEncryptionAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(Base64.decode(pbeConfiguration.getEncryptionKeyIV())));
			return cipher.doFinal(dataToDecrypt);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Cipher prepareCipherForEncryption(IPBEConfiguration pbeConfiguration, boolean forceUseSalt, char[] clearTextPassword)
	{
		try
		{
			if (pbeConfiguration.getPaddedKeyAlgorithm() == null)
			{
				pbeConfiguration.setPaddedKeyAlgorithm(paddedKeyAlgorithm);
			}
			if (pbeConfiguration.getPaddedKeyIterations() == 0)
			{
				pbeConfiguration.setPaddedKeyIterations(paddedKeyIterations);
			}
			if (pbeConfiguration.getPaddedKeySize() == 0)
			{
				pbeConfiguration.setPaddedKeySize(paddedKeySize);
			}
			if (forceUseSalt)
			{
				if (pbeConfiguration.getPaddedKeySalt() == null)
				{
					byte[] salt = PasswordSalts.nextSalt(paddedKeySaltSize / 8);
					pbeConfiguration.setPaddedKeySaltSize(paddedKeySaltSize);
					pbeConfiguration.setPaddedKeySalt(Base64.encodeBytes(salt).toCharArray());
				}
			}
			else
			{
				pbeConfiguration.setPaddedKeySaltSize(0);
				pbeConfiguration.setPaddedKeySalt(null);
			}
			if (pbeConfiguration.getEncryptionAlgorithm() == null)
			{
				pbeConfiguration.setEncryptionAlgorithm(encryptionAlgorithm);
			}
			if (pbeConfiguration.getEncryptionKeySpec() == null)
			{
				pbeConfiguration.setEncryptionKeySpec(encryptionKeySpec);
			}
			// padd the password to match the required length for encryption
			byte[] paddedPassword = doPaddingForPassword(pbeConfiguration, clearTextPassword);

			// now we encrypt the private key with the password of the user - this is the reason why we can only generate signatures either
			// during a login of a user or during new user account creation.
			SecretKeySpec keySpec = new SecretKeySpec(paddedPassword, pbeConfiguration.getEncryptionKeySpec());
			Cipher cipher = Cipher.getInstance(pbeConfiguration.getEncryptionAlgorithm());

			if (pbeConfiguration.getEncryptionKeyIV() == null)
			{
				byte[] initVector = PasswordSalts.nextSalt(cipher.getBlockSize());
				pbeConfiguration.setEncryptionKeyIV(Base64.encodeBytes(initVector).toCharArray());
			}
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(Base64.decode(pbeConfiguration.getEncryptionKeyIV())));
			return cipher;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] encrypt(IPBEConfiguration pbeConfiguration, boolean forceUseSalt, char[] clearTextPassword, byte[] dataToEncrypt)
	{
		try
		{
			Cipher cipher = prepareCipherForEncryption(pbeConfiguration, forceUseSalt, clearTextPassword);
			return cipher.doFinal(dataToEncrypt);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
