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
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.util.ParamChecker;

public class PasswordUtil implements IInitializingBean, IPasswordUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAlgorithmName, defaultValue = "PBKDF2WithHmacSHA1")
	protected String algorithm;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAlgorithmIterationCount, defaultValue = "8192")
	protected int iterationCount;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAlgorithmKeySize, defaultValue = "160")
	protected int keySize;

	@Property(name = SecurityServerConfigurationConstants.LoginSaltKeySpecName, defaultValue = "AES")
	protected String saltKeySpec;

	@Property(name = SecurityServerConfigurationConstants.LoginSaltAlgorithmName, defaultValue = "AES/CBC/PKCS7Padding")
	protected String saltAlgorithm;

	@Property(name = SecurityServerConfigurationConstants.LoginSaltLength, defaultValue = "16")
	protected int saltLength;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordGeneratedLength, defaultValue = "16")
	protected int generatedPasswordLength;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordLifetime, defaultValue = "30")
	protected int generatedPasswordLifetimeInDays;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordHistoryCount, defaultValue = "10")
	protected int passwordHistoryCount;

	@Property(name = IocConfigurationConstants.DebugModeActive, defaultValue = "false")
	protected boolean debugModeActive;

	@Property(name = SecurityServerConfigurationConstants.LoginSaltPassword, mandatory = false)
	protected char[] loginSaltPassword;

	protected SecretKeySpec decodedLoginSaltPassword;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertTrue(iterationCount > 0, SecurityServerConfigurationConstants.LoginPasswordAlgorithmIterationCount);
		ParamChecker.assertTrue(keySize > 0, SecurityServerConfigurationConstants.LoginPasswordAlgorithmKeySize);
		ParamChecker.assertTrue(saltLength > 0, SecurityServerConfigurationConstants.LoginSaltLength);
		ParamChecker.assertTrue(generatedPasswordLength > 0, SecurityServerConfigurationConstants.LoginPasswordGeneratedLength);
		ParamChecker.assertTrue(generatedPasswordLifetimeInDays > 0, SecurityServerConfigurationConstants.LoginPasswordLifetime);
		ParamChecker.assertTrue(passwordHistoryCount >= 0, SecurityServerConfigurationConstants.LoginPasswordHistoryCount);

		if (loginSaltPassword != null)
		{
			decodedLoginSaltPassword = new SecretKeySpec(Base64.decode(loginSaltPassword), saltKeySpec);
			if (log.isInfoEnabled())
			{
				log.info("ACTIVATED: password-based security for password salt");
			}
		}
		else
		{
			if (log.isInfoEnabled())
			{
				log.info("INACTIVE: password-based security for password salt");
			}
		}
	}

	protected RuntimeException createIllegalPasswordException()
	{
		RuntimeException e = new SecurityException(
				"New password does not meet the security criteria:\n1) ...\n2) ...\n3) Password has not already been used within the last "
						+ passwordHistoryCount + " changes");
		if (!debugModeActive)
		{
			e.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		}
		return e;
	}

	@Override
	public ICheckPasswordResult checkClearTextPassword(char[] clearTextPassword, IPassword password)
	{
		String givenPasswordString = Base64.encodeBytes(hashClearTextPassword(clearTextPassword, password));
		String expectedPasswordString = new String(password.getValue());
		if (!expectedPasswordString.equals(givenPasswordString))
		{
			return new CheckPasswordResult(false, false, false);
		}
		// password is correct. no check if we should rehash the password on-the-fly to ensure long-term security
		boolean changeRecommended = isChangeRecommended(password);
		boolean rehashRecommended = isRehashRecommended(password);
		return new CheckPasswordResult(true, changeRecommended, rehashRecommended);
	}

	@Override
	public void rehashPassword(char[] clearTextPassword, IPassword existingPassword)
	{
		String givenPasswordString = Base64.encodeBytes(hashClearTextPassword(clearTextPassword, existingPassword));
		String expectedPasswordString = new String(existingPassword.getValue());
		if (!expectedPasswordString.equals(givenPasswordString))
		{
			throw new IllegalArgumentException("Given clearTextPassword does not match for " + existingPassword);
		}
		// password should be rehashed, but it is the same clearTextPassword so we do NOT recalculate its changeAfter date
		fillPassword(clearTextPassword, existingPassword, false);
		mergeProcess.process(existingPassword, null, null, null);

	}

	protected boolean isChangeRecommended(IPassword password)
	{
		return Calendar.getInstance().after(password.getChangeAfter());
	}

	protected boolean isRehashRecommended(IPassword password)
	{
		if (!algorithm.equals(password.getAlgorithm()))
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (iterationCount != password.getIterationCount())
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (keySize != password.getKeySize())
		{
			// recommended algorithm configuration changed
			return true;
		}
		Integer saltLength = password.getSaltLength();
		if (saltLength == null || this.saltLength != saltLength.intValue())
		{
			// recommended algorithm configuration changed
			return true;
		}
		return false;
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
		while (passwordHistory.size() > passwordHistoryCount)
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
		fillPassword(clearTextPassword, password, true);
	}

	protected void fillPassword(char[] clearTextPassword, IPassword password, boolean assignNewChangeAfter)
	{
		if (assignNewChangeAfter)
		{
			Calendar changeAfter = Calendar.getInstance();
			changeAfter.add(Calendar.DAY_OF_MONTH, generatedPasswordLifetimeInDays);
			password.setChangeAfter(changeAfter);
		}
		password.setAlgorithm(algorithm);
		password.setIterationCount(iterationCount);
		password.setKeySize(keySize);
		password.setSaltLength(saltLength);
		encryptSalt(password, PasswordSalts.nextSalt(password.getSaltLength()));
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
			String saltAlgorithm = password.getSaltAlgorithm();
			if (saltAlgorithm == null)
			{
				// salt is considered as "not encrypted"
				return encryptedSalt;
			}
			if (decodedLoginSaltPassword == null)
			{
				throw new IllegalStateException("Property '" + SecurityServerConfigurationConstants.LoginSaltPassword
						+ "' specified but reading an encrypted salt from " + password);
			}
			Cipher cipher = Cipher.getInstance(saltAlgorithm);
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
				password.setSaltAlgorithm(null);
				;
				password.setSalt(Base64.encodeBytes(salt).toCharArray());
				return;
			}
			String saltAlgorithm = this.saltAlgorithm;
			Cipher cipher = Cipher.getInstance(saltAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, decodedLoginSaltPassword);
			byte[] encryptedSalt = cipher.doFinal(salt);
			password.setSaltAlgorithm(saltAlgorithm);
			password.setSalt(Base64.encodeBytes(encryptedSalt).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
