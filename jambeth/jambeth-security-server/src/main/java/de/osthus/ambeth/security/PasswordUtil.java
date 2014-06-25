package de.osthus.ambeth.security;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.util.ParamChecker;

public class PasswordUtil implements IInitializingBean, IPasswordUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	// TODO: Configurable by env property
	@Property(mandatory = false)
	protected String algorithm = "PBKDF2WithHmacSHA1";

	// TODO: Configurable by env property
	@Property(mandatory = false)
	protected int iterationCount = 8192;

	// TODO: Configurable by env property
	@Property(mandatory = false)
	protected int keySize = 160;

	// TODO: Configurable by env property
	@Property(mandatory = false)
	protected int saltLength = 16;

	// TODO: Configurable by env property
	@Property(mandatory = false)
	protected int generatedPasswordLength = 16;

	// TODO: Configurable by env property
	@Property(mandatory = false)
	protected int generatedPasswordLifetimeInDays = 30;

	// TODO: Configurable by env property
	@Property(mandatory = false)
	protected int passwordHistorySize = 10;

	@Property(name = IocConfigurationConstants.DebugModeActive, defaultValue = "false")
	protected boolean debugModeActive;

	@Property(name = SecurityServerConfigurationConstants.LoginSaltPassword, mandatory = false)
	protected char[] loginSaltPassword;

	protected SecretKeySpec decodedLoginSaltPassword;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (loginSaltPassword != null)
		{
			decodedLoginSaltPassword = new SecretKeySpec(Base64.decode(loginSaltPassword), "AES");
		}
	}

	protected RuntimeException createIllegalPasswordException()
	{
		RuntimeException e = new SecurityException(
				"New password does not meet the security criteria:\n1) ...\n2) ...\n3) Password has not already been used within the last "
						+ passwordHistorySize + " changes");
		if (!debugModeActive)
		{
			e.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		}
		return e;
	}

	@Override
	public byte[] hashClearTextPassword(char[] clearTextPassword, IPassword password)
	{
		ParamChecker.assertParamNotNull(clearTextPassword, "clearTextPassword");
		ParamChecker.assertParamNotNull(password, "password");
		try
		{
			return Passwords.hashPassword(clearTextPassword, decryptSalt(password), password.getAlgorithm(), password.getIterationCount(),
					password.getKeySize());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void fillNewPassword(char[] clearTextPassword, IPassword newEmptyPassword, IUser user)
	{
		ParamChecker.assertParamNotNull(clearTextPassword, "clearTextPassword");
		ParamChecker.assertParamNotNull(newEmptyPassword, "newEmptyPassword");
		ParamChecker.assertParamNotNull(user, "user");
		List<IPassword> passwordHistory = buildPasswordHistory(user);
		if (passwordHistory != null && passwordHistory.contains(newEmptyPassword))
		{
			throw new IllegalArgumentException("Given newEmptyPassword must be a new (unused) instance of a password");
		}
		if (isPasswordUsedInHistory(clearTextPassword, passwordHistory))
		{
			throw createIllegalPasswordException();
		}
		fillPassword(clearTextPassword, newEmptyPassword);
		setNewPasswordIntern(user, newEmptyPassword);
	}

	@Override
	public void generateNewPassword(IPassword newEmptyPassword, IUser user)
	{
		ParamChecker.assertParamNotNull(newEmptyPassword, "newEmptyPassword");
		ParamChecker.assertParamNotNull(user, "user");
		List<IPassword> passwordHistory = buildPasswordHistory(user);
		if (passwordHistory != null && passwordHistory.contains(newEmptyPassword))
		{
			throw new IllegalArgumentException("Given newEmptyPassword must be a new (unused) instance of a password");
		}
		char[] clearTextPassword = null;
		while (true)
		{
			// we use the secure salt implementation as our random "clearTextPassword"
			clearTextPassword = Base64.encodeBytes(PasswordSalts.nextSalt(generatedPasswordLength)).toCharArray();

			if (!isPasswordUsedInHistory(clearTextPassword, passwordHistory))
			{
				break;
			}
		}
		fillPassword(clearTextPassword, newEmptyPassword);
		setNewPasswordIntern(user, newEmptyPassword);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setNewPasswordIntern(IUser user, IPassword password)
	{
		IPassword existingPassword = user.getPassword();
		user.setPassword(password);
		if (existingPassword != null)
		{
			// ugly hack because of generics
			((Collection<IPassword>) ((Collection) user.getPasswordHistory())).add(existingPassword);
		}
		cleanupPasswordHistory(user);
	}

	protected void cleanupPasswordHistory(IUser user)
	{
		Collection<? extends IPassword> passwordHistory = user.getPasswordHistory();
		while (passwordHistory.size() > passwordHistorySize)
		{
			ArrayList<IPassword> passwordHistoryList = new ArrayList<IPassword>(passwordHistory);
			Collections.sort(passwordHistoryList, new Comparator<IPassword>()
			{
				@Override
				public int compare(IPassword o1, IPassword o2)
				{
					return o1.getChangeAfter().compareTo(o2.getChangeAfter());
				}
			});
			IPassword passwordToRemove = passwordHistoryList.get(passwordHistoryList.size() - 1);
			passwordHistory.remove(passwordToRemove);
		}
	}

	protected List<IPassword> buildPasswordHistory(IUser user)
	{
		ArrayList<IPassword> passwordHistory = new ArrayList<IPassword>(user.getPasswordHistory());
		if (user.getPassword() != null)
		{
			passwordHistory.add(user.getPassword());
		}
		return passwordHistory;
	}

	protected boolean isPasswordUsedInHistory(char[] newPassword, Collection<? extends IPassword> passwordHistory)
	{
		if (passwordHistory == null)
		{
			return false;
		}
		for (IPassword password : passwordHistory)
		{
			try
			{
				if (hashedEquals(newPassword, password))
				{
					// the same password has been used before
					return true;
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return false;
	}

	protected boolean hashedEquals(char[] clearTextPassword, IPassword password)
	{
		try
		{
			byte[] hashedClearTextPassword = hashClearTextPassword(clearTextPassword, password);
			return Arrays.equals(hashedClearTextPassword, Base64.decode(password.getValue()));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void fillPassword(char[] clearTextPassword, IPassword password)
	{
		Calendar validBefore = Calendar.getInstance();
		validBefore.add(Calendar.DAY_OF_MONTH, generatedPasswordLifetimeInDays);

		password.setAlgorithm(algorithm);
		password.setIterationCount(iterationCount);
		password.setKeySize(keySize);
		password.setChangeAfter(validBefore);
		encryptSalt(password, PasswordSalts.nextSalt(saltLength));
		try
		{
			byte[] hashedPassword = hashClearTextPassword(clearTextPassword, password);
			password.setValue(Base64.encodeBytes(hashedPassword).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected byte[] decryptSalt(IPassword password)
	{
		try
		{
			byte[] encryptedSalt = Base64.decode(password.getSalt());
			if (!password.isSaltEncrypted())
			{
				return encryptedSalt;
			}
			if (decodedLoginSaltPassword == null)
			{
				throw new IllegalStateException("Property '" + SecurityServerConfigurationConstants.LoginSaltPassword
						+ "' specified but reading an encrypted salt from " + password);
			}
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.DECRYPT_MODE, decodedLoginSaltPassword);
			return cipher.doFinal(encryptedSalt);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void encryptSalt(IPassword password, byte[] salt)
	{
		try
		{
			if (decodedLoginSaltPassword == null)
			{
				password.setSaltEncrypted(false);
				password.setSalt(Base64.encodeBytes(salt).toCharArray());
				return;
			}
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.ENCRYPT_MODE, decodedLoginSaltPassword);
			byte[] encryptedSalt = cipher.doFinal(salt);
			password.setSaltEncrypted(true);
			password.setSalt(Base64.encodeBytes(encryptedSalt).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
