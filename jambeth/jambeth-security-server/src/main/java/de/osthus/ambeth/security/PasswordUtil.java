package de.osthus.ambeth.security;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.MergeFinishedCallback;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.IAuthentication.PasswordType;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.util.ParamChecker;

public class PasswordUtil implements IInitializingBean, IPasswordUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IPrivilegeProvider privilegeProvider;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Autowired(optional = true)
	protected IUserIdentifierProvider userIdentifierProvider;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAlgorithmName, defaultValue = "PBKDF2WithHmacSHA1")
	protected String algorithm;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAlgorithmIterationCount, defaultValue = "8192")
	protected int iterationCount;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAlgorithmKeySize, defaultValue = "160")
	protected int keySize;

	@Property(name = SecurityServerConfigurationConstants.LoginSaltKeySpecName, defaultValue = "AES")
	protected String saltKeySpec;

	@Property(name = SecurityServerConfigurationConstants.LoginSaltAlgorithmName, defaultValue = "AES/CBC/PKCS5Padding")
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

	protected final SmartCopyMap<String, Reference<SecretKeyFactory>> algorithmToSecretKeyFactoryMap = new SmartCopyMap<String, Reference<SecretKeyFactory>>(
			0.5f);

	protected final Lock saltReencryptionLock = new ReentrantLock();

	protected volatile SecretKeySpec decodedLoginSaltPassword;

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
			decodedLoginSaltPassword = createKeySpecFromPassword(loginSaltPassword);
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

	protected SecretKeyFactory getSecretKeyFactory(String algorithm)
	{
		Reference<SecretKeyFactory> secretKeyFactoryR = algorithmToSecretKeyFactoryMap.get(algorithm);
		SecretKeyFactory secretKeyFactory = secretKeyFactoryR != null ? secretKeyFactoryR.get() : null;
		if (secretKeyFactory == null)
		{
			try
			{
				secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			algorithmToSecretKeyFactoryMap.put(algorithm, new WeakReference<SecretKeyFactory>(secretKeyFactory));
		}
		return secretKeyFactory;
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
			if (debugModeActive)
			{
				log.info("The given hash of the current authentication is: '" + givenPasswordString + "'. Expected hash: '" + expectedPasswordString + "'");
			}
			return new CheckPasswordResult(false, false, false);
		}
		// password is correct. now check if we should rehash the password on-the-fly to ensure long-term security
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
		fillPassword(clearTextPassword, existingPassword, null, false);
		mergeProcess.process(existingPassword, null, null, null);
	}

	public SecretKeySpec createKeySpecFromPassword(char[] encodedClearTextPassword)
	{
		try
		{
			return createSaltKeyFromPassword(Base64.decode(encodedClearTextPassword));
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected SecretKeySpec createSaltKeyFromPassword(byte[] newSaltBinaryPassword)
	{
		// byte[] dummySalt = { 0 };
		// PBEKeySpec spec = new PBEKeySpec(newSaltPassword, dummySalt, 1, 160);
		// SecretKeyFactory factory = getSecretKeyFactory(password.getAlgorithm());
		// byte[] decodedSaltPassword = Base64.decode(newSaltPassword);
		// if (decodedSaltPassword.length < 16)
		// {
		// byte[] paddedDecodedSaltPassword = new byte[16];
		// System.arraycopy(decodedSaltPassword, 0, paddedDecodedSaltPassword, 0, decodedSaltPassword.length);
		// decodedSaltPassword = paddedDecodedSaltPassword;
		// }
		// return new SecretKeySpec(decodedSaltPassword, saltKeySpec);
		return new SecretKeySpec(newSaltBinaryPassword, saltKeySpec);
	}

	@Override
	public void reencryptAllSalts(byte[] newSaltBinaryPassword, Class<? extends IPassword> passwordEntityType)
	{
		ParamChecker.assertParamNotNull(newSaltBinaryPassword, "newSaltBinaryPassword");
		ParamChecker.assertParamNotNull(passwordEntityType, "passwordEntityType");
		ITypePrivilege privilegeOnPasswordType = privilegeProvider.getPrivilegeByType(passwordEntityType);
		if (!Boolean.TRUE.equals(privilegeOnPasswordType.isReadAllowed()) || !Boolean.TRUE.equals(privilegeOnPasswordType.isUpdateAllowed()))
		{
			// if the current user has no general right to modify ALL password entities we can not change the password salt consistently!
			throw new SecurityException("Current user has no right to read & update all instances of " + passwordEntityType.getName());
		}
		saltReencryptionLock.lock();
		try
		{
			final SecretKeySpec newDecodedLoginSaltPassword = createSaltKeyFromPassword(newSaltBinaryPassword);
			if (log.isInfoEnabled())
			{
				log.info("Reencrypt all salts with new salt-password...");
			}
			IList<? extends IPassword> allPasswords = queryBuilderFactory.create(passwordEntityType).build().retrieve();
			ArrayList<IPassword> changedPasswords = new ArrayList<IPassword>(allPasswords.size());
			for (IPassword password : allPasswords)
			{
				if (!isReencryptSaltRecommended(password))
				{
					continue;
				}
				byte[] decryptedSalt = decryptSalt(password);
				password.setSaltAlgorithm(saltAlgorithm);
				encryptSalt(password, decryptedSalt, newDecodedLoginSaltPassword);
				changedPasswords.add(password);
			}
			mergeProcess.process(changedPasswords, null, null, new MergeFinishedCallback()
			{
				@Override
				public void invoke(boolean success)
				{
					decodedLoginSaltPassword = newDecodedLoginSaltPassword;
					if (log.isInfoEnabled())
					{
						log.info("Reencryption of all salts finished successfully!");
					}
				}
			});
		}
		finally
		{
			saltReencryptionLock.unlock();
		}
	}

	protected boolean isChangeRecommended(IPassword password)
	{
		Calendar changeAfter = password.getChangeAfter();
		return changeAfter == null || Calendar.getInstance().after(changeAfter);
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
		return isReencryptSaltRecommended(password);
	}

	protected boolean isReencryptSaltRecommended(IPassword password)
	{
		if (decodedLoginSaltPassword != null && !saltAlgorithm.equals(password.getSaltAlgorithm()))
		{
			// recommended algorithm configuration changed
			return true;
		}
		if (decodedLoginSaltPassword != null && !saltKeySpec.equals(password.getSaltKeySpec()))
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
			PBEKeySpec spec = new PBEKeySpec(clearTextPassword, decryptSalt(password), password.getIterationCount(), password.getKeySize());
			SecretKeyFactory factory = getSecretKeyFactory(password.getAlgorithm());
			return factory.generateSecret(spec).getEncoded();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void assignNewPassword(char[] clearTextPassword, IPassword newEmptyPassword, IUser user)
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
		fillPassword(clearTextPassword, newEmptyPassword, user, true);
		setNewPasswordIntern(user, newEmptyPassword);
	}

	@Override
	public String assignNewRandomPassword(IPassword newEmptyPassword, IUser user)
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
		fillPassword(clearTextPassword, newEmptyPassword, user, true);
		setNewPasswordIntern(user, newEmptyPassword);
		return new String(clearTextPassword);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setNewPasswordIntern(IUser user, IPassword password)
	{
		IPassword existingPassword = user.getPassword();
		user.setPassword(password);
		if (existingPassword != null)
		{
			Collection<? extends IPassword> passwordHistory = user.getPasswordHistory();
			if (passwordHistory != null)
			{
				// ugly hack because of generics
				((Collection<IPassword>) ((Collection) passwordHistory)).add(existingPassword);
			}
		}
		cleanupPasswordHistory(user);
	}

	protected void cleanupPasswordHistory(IUser user)
	{
		Collection<? extends IPassword> passwordHistory = user.getPasswordHistory();
		if (passwordHistory == null)
		{
			return;
		}
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
		Collection<? extends IPassword> passwordHistory = user.getPasswordHistory();
		if (passwordHistory == null)
		{
			return EmptyList.getInstance();
		}
		ArrayList<IPassword> passwordHistoryList = new ArrayList<IPassword>(passwordHistory);
		if (user.getPassword() != null)
		{
			passwordHistoryList.add(user.getPassword());
		}
		return passwordHistoryList;
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

	protected void fillPassword(char[] clearTextPassword, IPassword password, IUser user, boolean assignNewChangeAfter)
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
		encryptSalt(password, PasswordSalts.nextSalt(password.getSaltLength()), decodedLoginSaltPassword);
		try
		{
			byte[] hashedPassword = hashClearTextPassword(clearTextPassword, password);
			password.setValue(Base64.encodeBytes(hashedPassword).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (user == null)
		{
			return;
		}
		ISignature signature = user.getSignature();
		if (signature != null)
		{
			if (userIdentifierProvider == null)
			{
				throw new IllegalStateException("No instanceof of " + IUserIdentifierProvider.class + " found to create a new signature due to password change");
			}
			// create a NEW signature because we can not decrypt the now invalid private key of the signature
			// without knowing the previous password in clear-text. As a result we need a new instance for a signature
			// because of a potential audit-trail functionality we can NOT reuse the existing signature entity
			signature = entityFactory.createEntity(ISignature.class);
			signatureUtil.generateNewSignature(signature, clearTextPassword);
			user.setSignature(signature);
			ISecurityContext context = securityContextHolder.getContext();
			IAuthorization authorization = context != null ? context.getAuthorization() : null;
			if (authorization != null)
			{
				// check whether we changed our own current authentication and refresh this information
				// this is due to the fact that the usage of the newly generated signature with the newly assigned password
				// can only be decrypted if the current authentication contains this newly assigned password from now on
				String sid = userIdentifierProvider.getSID(user);
				if (authorization.getSID().equals(sid))
				{
					context.setAuthentication(new DefaultAuthentication(context.getAuthentication().getUserName(), clearTextPassword, PasswordType.PLAIN));
				}
			}
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
			cipher.init(Cipher.DECRYPT_MODE, decodedLoginSaltPassword, new IvParameterSpec(new byte[cipher.getBlockSize()]));
			return cipher.doFinal(encryptedSalt);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void encryptSalt(IPassword password, byte[] salt, SecretKeySpec decodedLoginSaltPassword)
	{
		try
		{
			if (decodedLoginSaltPassword == null)
			{
				password.setSaltAlgorithm(null);
				password.setSalt(Base64.encodeBytes(salt).toCharArray());
				return;
			}
			String saltAlgorithm = this.saltAlgorithm;
			Cipher cipher = Cipher.getInstance(saltAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, decodedLoginSaltPassword, new IvParameterSpec(new byte[cipher.getBlockSize()]));
			byte[] encryptedSalt = cipher.doFinal(salt);
			password.setSaltAlgorithm(saltAlgorithm);
			password.setSaltKeySpec(saltKeySpec);
			password.setSalt(Base64.encodeBytes(encryptedSalt).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
