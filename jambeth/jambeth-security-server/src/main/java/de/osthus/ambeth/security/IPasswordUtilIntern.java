package de.osthus.ambeth.security;

import javax.crypto.spec.SecretKeySpec;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IPasswordUtilIntern
{

	SecretKeySpec createKeySpecFromPassword(char[] encodedClearTextPassword);

}