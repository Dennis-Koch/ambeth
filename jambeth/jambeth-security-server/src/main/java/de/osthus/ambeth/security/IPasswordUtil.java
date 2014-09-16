package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;

public interface IPasswordUtil
{
	void fillNewPassword(char[] clearTextPassword, IPassword newEmptyPassword, IUser user);

	String generateNewPassword(IPassword newEmptyPassword, IUser user);

	byte[] hashClearTextPassword(char[] clearTextPassword, IPassword password);

	ICheckPasswordResult checkClearTextPassword(char[] clearTextPassword, IPassword password);

	void rehashPassword(char[] clearTextPassword, IPassword existingPassword);

	void reencryptAllSalts(byte[] newSaltBinaryPassword, Class<? extends IPassword> passwordEntityType);
}