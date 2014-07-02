package de.osthus.ambeth.security.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class SecurityServerConfigurationConstants
{
	/**
	 * Optional parameter.<br>
	 * Default = PBKDF2WithHmacSHA1
	 */
	@ConfigurationConstantDescription("Optional parameter. Default = PBKDF2WithHmacSHA1")
	public static final String LoginPasswordAlgorithmName = "security.login.password.algorithm.name";

	/**
	 * Optional parameter. Must be >0.<br>
	 * Default = 8192
	 */
	@ConfigurationConstantDescription("Optional parameter. Must be >0. Default = 8192")
	public static final String LoginPasswordAlgorithmIterationCount = "security.login.password.algorithm.iterationcount";

	/**
	 * Optional parameter. Must be >0.<br>
	 * Default = 160
	 */
	@ConfigurationConstantDescription("Optional parameter. Must be >0. Default = 160")
	public static final String LoginPasswordAlgorithmKeySize = "security.login.password.algorithm.keysize";

	/**
	 * Configures the feature to auto-rehash passwords on-the-fly to ensure long-term security of stored passwords.<br>
	 * Optional parameter.<br>
	 * Default = false
	 */
	@ConfigurationConstantDescription("Configures the feature to auto-rehash passwords on-the-fly to ensure long-term security of stored passwords. Optional parameter. Default = false")
	public static final String LoginPasswordAutoRehashActive = "security.login.password.autorehash.active";

	/**
	 * Configures the byte-length of generated passwords (e.g. for new user accounts).<br>
	 * Optional parameter. Must be >0.<br>
	 * Default = 16
	 */
	@ConfigurationConstantDescription("Configures the byte-length of generated passwords (e.g. for new user accounts). Optional parameter. Must be >0. Default = 16")
	public static final String LoginPasswordGeneratedLength = "security.login.password.generated.length";

	/**
	 * Configures the maximum lifetime (in days) after which a password should be changed.<br>
	 * Optional parameter. Must be >0.<br>
	 * Default = 30
	 */
	@ConfigurationConstantDescription("Configures the maximum lifetime (in days) after which a password should be changed. Optional parameter. Must be >0. Default = 30")
	public static final String LoginPasswordLifetime = "security.login.password.lifetime";

	/**
	 * Configures the maximum password history which should be kept to 'help' a user to do a 'real' password change.<br>
	 * Optional parameter. Must be >=0.<br>
	 * Default = 10
	 */
	@ConfigurationConstantDescription("Configures the maximum password history which should be kept to 'help' a user to do a 'real' password change. Optional parameter. Must be >=0. Default = 10")
	public static final String LoginPasswordHistoryCount = "security.login.password.history.count";

	/**
	 * Configures the internal 'secret' which is used to decrypt encrypted salts. If you define the parameter ensure that it is NOT hard-coded in our
	 * source-code and NOT stored at the same place as the password-entity (e.g. do NOT store it in the database). The recommended place is an external property
	 * file in the file-system of the deployed application.<br>
	 * Optional parameter (as long as no encrypted salts exist).<br>
	 * Default = null
	 */
	@ConfigurationConstantDescription("Configures the internal 'secret' which is used to decrypt encrypted salts. If you define the parameter ensure that it is NOT hard-coded in our source-code and NOT stored at the same place as the password-entity (e.g. do NOT store it in the database). The recommended place is an external property file in the file-system of the deployed application. Optional parameter (as long as no encrypted salts exist). Default = null")
	public static final String LoginSaltPassword = "security.login.salt.password";

	/**
	 * Configures the algorithm which is used to encrypt salts. Value itself is not considered unless a specific salt password is defined.<br>
	 * Optional parameter.<br>
	 * Default = AES/CBC/PKCS5Padding
	 */
	@ConfigurationConstantDescription("Configures the algorithm which is used to encrypt salts. Value itself is not considered unless a specific salt password is defined. Optional parameter. Default = AES/CBC/PKCS5Padding")
	public static final String LoginSaltAlgorithmName = "security.login.salt.algorithm.name";

	/**
	 * Configures the keyspec name which is used to encrypt salts. Must be compatible to the configured 'security.login.salt.algorithm.name'. Value itself is
	 * not considered unless a specific salt password is defined.<br>
	 * Optional parameter.<br>
	 * Default = AES
	 */
	@ConfigurationConstantDescription("Configures the keyspec name which is used to encrypt salts. Must be compatible to the configured 'security.login.salt.algorithm.name'. Value itself is not considered unless a specific salt password is defined. Optional parameter. Default = AES")
	public static final String LoginSaltKeySpecName = "security.login.salt.keyspec.name";

	/**
	 * Configures the byte-length of the generated random salt each time a password gets created or rehashed.<br>
	 * Optional parameter. Must be >0.<br>
	 * Default = 16
	 */
	@ConfigurationConstantDescription("Configures the byte-length of the generated random salt each time a password gets created or rehashed. Optional parameter. Must be >0. Default = 16")
	public static final String LoginSaltLength = "security.login.salt.length";

	private SecurityServerConfigurationConstants()
	{
		// Intended blank
	}
}
