package com.koch.ambeth.security.server;

import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.util.state.IStateRollback;

public interface IPasswordUtil {
	void assignNewPassword(char[] clearTextPassword, IUser user, char[] oldClearTextPassword);

	char[] assignNewRandomPassword(IUser user, char[] oldClearTextPassword);

	byte[] hashClearTextPassword(char[] clearTextPassword, IPassword password);

	ICheckPasswordResult checkClearTextPassword(char[] clearTextPassword, IPassword password);

	void rehashPassword(char[] clearTextPassword, IPassword existingPassword);

	void reencryptAllSalts(char[] newLoginSaltPassword);

	IStateRollback pushSuppressPasswordValidation(IStateRollback... rollbacks);
}
