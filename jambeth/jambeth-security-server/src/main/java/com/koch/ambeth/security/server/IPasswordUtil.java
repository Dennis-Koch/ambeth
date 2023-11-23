package com.koch.ambeth.security.server;

import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.util.state.IStateRollback;

public interface IPasswordUtil {
    void validatePassword(char[] clearTextPassword, IUser user);

    void assignNewPassword(char[] clearTextPassword, IUser user, char[] oldClearTextPassword);

    void assignNewPassword(IPassword password, IUser user);

    char[] assignNewRandomPassword(IUser user, char[] oldClearTextPassword);

    byte[] createRandomPassword();

    byte[] hashClearTextPassword(char[] clearTextPassword, IPassword password);

    ICheckPasswordResult checkClearTextPassword(char[] clearTextPassword, IPassword password);

    void rehashPassword(char[] clearTextPassword, IPassword existingPassword);

    void reencryptAllSalts(char[] newLoginSaltPassword);

    void encryptPassword(char[] newClearTextPassword, IPassword password, boolean assignNewChangeAfter);

    IStateRollback pushSuppressPasswordValidation();

    IStateRollback pushSuppressPasswordChangeRequired();

    IStateRollback pushSuppressAccounting();
}
