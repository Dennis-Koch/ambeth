package de.osthus.ambeth.security.model;

import de.osthus.ambeth.audit.model.Audited;

@Audited(false)
public interface ISignature
{
	public static final String Algorithm = "Algorithm";

	public static final String ChangeAfter = "ChangeAfter";

	public static final String KeySize = "KeySize";

	public static final String User = "User";

	public static final String Value = "Value";

	IUser getUser();

	char[] getPrivateKey();

	void setPrivateKey(char[] privateKey);

	char[] getPublicKey();

	void setPublicKey(char[] publicKey);

	IPBEConfiguration getPBEConfiguration();

	ISignAndVerify getSignAndVerify();
}
