package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;

public interface IPasswordUtil
{
	void assignNewPassword(char[] clearTextPassword, IUser user, char[] oldClearTextPassword);

	char[] assignNewRandomPassword(IUser user, char[] oldClearTextPassword);

	byte[] hashClearTextPassword(char[] clearTextPassword, IPassword password);

	ICheckPasswordResult checkClearTextPassword(char[] clearTextPassword, IPassword password);

	void rehashPassword(char[] clearTextPassword, IPassword existingPassword);

	void reencryptAllSalts(char[] newLoginSaltPassword);
}