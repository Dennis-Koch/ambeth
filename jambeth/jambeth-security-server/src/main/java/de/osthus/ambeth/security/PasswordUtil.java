package de.osthus.ambeth.security;

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

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.exceptions.PasswordConstraintException;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.MergeFinishedCallback;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPBEConfiguration;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.util.ParamChecker;

public class PasswordUtil implements IInitializingBean, IPasswordUtil, IPasswordValidationExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuthenticatedUserHolder authenticatedUserHolder;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IPBEncryptor pbEncryptor;

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

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Property(name = SecurityServerConfigurationConstants.SignatureActive, defaultValue = "false")
	protected boolean signatureActive;

	protected final SmartCopyMap<String, Reference<SecretKeyFactory>> algorithmToSecretKeyFactoryMap = new SmartCopyMap<String, Reference<SecretKeyFactory>>(
			0.5f);

	protected final DefaultExtendableContainer<IPasswordValidationExtension> extensions = new DefaultExtendableContainer<IPasswordValidationExtension>(
			IPasswordValidationExtension.class, "passwordValidadtionExtension");

	protected final Lock saltReencryptionLock = new ReentrantLock();

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
			if (log.isInfoEnabled())
			{
				log.info("ACTIVATED: password-based security for password salt");
			}
		}
		else if (securityActive && log.isInfoEnabled())
		{
			log.info("INACTIVE: password-based security for password salt");
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
		fillPassword(clearTextPassword, clearTextPassword, existingPassword, null, false);
		mergeProcess.process(existingPassword.getUser(), null, null, null); // important to merge the user because of the relation to signature
	}

	@Override
	public void reencryptAllSalts(final char[] newLoginSaltPassword)
	{
		ParamChecker.assertParamNotNull(newLoginSaltPassword, "newLoginSaltPassword");
		ITypePrivilege privilegeOnPasswordType = privilegeProvider.getPrivilegeByType(IPassword.class);
		if (!Boolean.TRUE.equals(privilegeOnPasswordType.isReadAllowed()) || !Boolean.TRUE.equals(privilegeOnPasswordType.isUpdateAllowed()))
		{
			// if the current user has no general right to modify ALL password entities we can not change the password salt consistently!
			throw new SecurityException("Current user has no right to read & update all instances of " + IPassword.class.getName());
		}
		saltReencryptionLock.lock();
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("Reencrypt all salts with new salt-password...");
			}
			IList<IPassword> allPasswords = queryBuilderFactory.create(IPassword.class).build().retrieve();
			ArrayList<IPassword> changedPasswords = new ArrayList<IPassword>(allPasswords.size());
			for (IPassword password : allPasswords)
			{
				byte[] decryptedSalt = decryptSalt(password);
				encryptSalt(password, decryptedSalt, newLoginSaltPassword);
				changedPasswords.add(password);
			}
			mergeProcess.process(changedPasswords, null, null, new MergeFinishedCallback()
			{
				@Override
				public void invoke(boolean success)
				{
					if (success)
					{
						loginSaltPassword = newLoginSaltPassword;
						if (log.isInfoEnabled())
						{
							log.info("Reencryption of all salts finished successfully!");
						}
					}
					else
					{
						log.error("Reencryption of all salts failed");
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
		if (loginSaltPassword != null)
		{
			if (pbEncryptor.isReencryptionRecommended(password.getSaltPBEConfiguration()))
			{
				return true;
			}
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
	public void assignNewPassword(char[] clearTextPassword, IUser user, char[] oldClearTextPassword)
	{
		ParamChecker.assertParamNotNull(clearTextPassword, "clearTextPassword");
		ParamChecker.assertParamNotNull(user, "user");
		StringBuilder validationErrorSB = null;
		for (IPasswordValidationExtension extension : extensions.getExtensions())
		{
			String validationError = extension.validatePassword(clearTextPassword);
			if (validationError != null)
			{
				if (validationErrorSB == null)
				{
					validationErrorSB = new StringBuilder();
				}
				else
				{
					validationErrorSB.append('\n');
				}
				validationErrorSB.append(validationError);
			}
		}
		if (validationErrorSB != null)
		{
			throw new PasswordConstraintException(validationErrorSB.toString());
		}
		List<IPassword> passwordHistory = buildPasswordHistory(user);
		if (isPasswordUsedInHistory(clearTextPassword, passwordHistory))
		{
			throw createIllegalPasswordException();
		}
		IPassword newEmptyPassword = entityFactory.createEntity(IPassword.class);
		fillPassword(clearTextPassword, oldClearTextPassword, newEmptyPassword, user, true);
		setNewPasswordIntern(user, newEmptyPassword);
	}

	@Override
	public char[] assignNewRandomPassword(IUser user, char[] oldClearTextPassword)
	{
		ParamChecker.assertParamNotNull(user, "user");
		List<IPassword> passwordHistory = buildPasswordHistory(user);
		char[] newClearTextPassword = null;
		while (true)
		{
			// we use the secure salt implementation as our random "clearTextPassword"
			newClearTextPassword = Base64.encodeBytes(PasswordSalts.nextSalt(generatedPasswordLength)).toCharArray();

			if (!isPasswordUsedInHistory(newClearTextPassword, passwordHistory))
			{
				break;
			}
		}
		IPassword newEmptyPassword = entityFactory.createEntity(IPassword.class);
		fillPassword(newClearTextPassword, oldClearTextPassword, newEmptyPassword, user, true);
		setNewPasswordIntern(user, newEmptyPassword);
		return newClearTextPassword;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setNewPasswordIntern(IUser user, IPassword password)
	{
		IPassword existingPassword = user.getPassword();
		user.setPassword(password);
		IEntityMetaData passwordMetaData = ((IEntityMetaDataHolder) password).get__EntityMetaData();
		passwordMetaData.getMemberByName(IPassword.User).setValue(password, user);
		if (existingPassword != null)
		{
			passwordMetaData.getMemberByName(IPassword.User).setValue(existingPassword, null);
			Collection<? extends IPassword> passwordHistory = user.getPasswordHistory();
			if (passwordHistory != null)
			{
				// ugly hack because of generics
				((Collection<IPassword>) ((Collection) passwordHistory)).add(existingPassword);
				passwordMetaData.getMemberByName(IPassword.HistoryUser).setValue(existingPassword, user);
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
			IEntityMetaData passwordMetaData = entityMetaDataProvider.getMetaData(passwordToRemove.getClass());
			passwordMetaData.getMemberByName(IPassword.HistoryUser).setValue(passwordToRemove, null);
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

	protected void fillPassword(char[] newClearTextPassword, char[] oldClearTextPassword, IPassword password, IUser user, boolean assignNewChangeAfter)
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
		encryptSalt(password, PasswordSalts.nextSalt(password.getSaltLength()), loginSaltPassword);
		try
		{
			byte[] hashedPassword = hashClearTextPassword(newClearTextPassword, password);
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
		if (oldClearTextPassword == null)
		{
			String currentSid = authenticatedUserHolder.getAuthenticatedSID();
			String sid = userIdentifierProvider.getSID(user);
			if (currentSid != null && currentSid.equals(sid))
			{
				IAuthentication authentication = securityContextHolder.getContext().getAuthentication();
				oldClearTextPassword = authentication.getPassword();
			}
		}
		boolean isNewSignature = false;
		ISignature signature = user.getSignature();
		if (signature == null || oldClearTextPassword == null)
		{
			if (!signatureActive)
			{
				return;
			}
			IEntityMetaData signatureMetaData = entityMetaDataProvider.getMetaData(ISignature.class);
			Member userMember = signatureMetaData.getMemberByName(ISignature.User);
			if (signature != null)
			{
				userMember.setValue(signature, null);
			}
			signature = entityFactory.createEntity(ISignature.class);
			user.setSignature(signature);
			userMember.setValue(signature, user);
			isNewSignature = true;
		}
		if (userIdentifierProvider == null)
		{
			throw new IllegalStateException("No instanceof of " + IUserIdentifierProvider.class.getName()
					+ " found to create a new signature due to password change");
		}
		if (isNewSignature || ((IEntityMetaDataHolder) signature).get__EntityMetaData().getIdMember().getValue(signature, false) == null)
		{
			signatureUtil.generateNewSignature(signature, newClearTextPassword);
		}
		else
		{
			signatureUtil.reencryptSignature(signature, oldClearTextPassword, newClearTextPassword);
		}
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
				context.setAuthentication(new DefaultAuthentication(context.getAuthentication().getUserName(), newClearTextPassword, PasswordType.PLAIN));
			}
		}
	}

	protected byte[] decryptSalt(IPassword password)
	{
		try
		{
			byte[] encryptedSalt = Base64.decode(password.getSalt());
			String saltAlgorithm = password.getSaltPBEConfiguration().getEncryptionAlgorithm();
			if (saltAlgorithm == null)
			{
				// salt is considered as "not encrypted"
				return encryptedSalt;
			}
			if (loginSaltPassword == null)
			{
				throw new IllegalStateException("Property '" + SecurityServerConfigurationConstants.LoginSaltPassword
						+ "' is not specified but reading an encrypted salt from " + password);
			}
			return pbEncryptor.decrypt(password.getSaltPBEConfiguration(), loginSaltPassword, encryptedSalt);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void encryptSalt(IPassword password, byte[] salt, char[] loginSaltPassword)
	{
		try
		{
			IPBEConfiguration saltPBEConfiguration = password.getSaltPBEConfiguration();
			if (loginSaltPassword == null)
			{
				if (saltPBEConfiguration != null)
				{
					saltPBEConfiguration.setEncryptionAlgorithm(null);
					saltPBEConfiguration.setEncryptionKeySpec(null);
					saltPBEConfiguration.setEncryptionKeyIV(null);
					saltPBEConfiguration.setPaddedKeyAlgorithm(null);
					saltPBEConfiguration.setPaddedKeyIterations(0);
					saltPBEConfiguration.setPaddedKeySize(0);
				}
				password.setSalt(Base64.encodeBytes(salt).toCharArray());
				return;
			}
			byte[] encryptedSalt = pbEncryptor.encrypt(saltPBEConfiguration, false, loginSaltPassword, salt);
			password.setSalt(Base64.encodeBytes(encryptedSalt).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void registerPasswordValidationExtension(IPasswordValidationExtension passwordValidationExtension)
	{
		extensions.register(passwordValidationExtension);
	}

	@Override
	public void unregisterPasswordValidationExtension(IPasswordValidationExtension passwordValidationExtension)
	{
		extensions.unregister(passwordValidationExtension);
	}
}
