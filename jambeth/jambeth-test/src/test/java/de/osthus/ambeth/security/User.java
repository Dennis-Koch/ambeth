package de.osthus.ambeth.security;

import java.util.Collection;

import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.security.model.IUser;

public interface User extends IAbstractEntity, IUser
{
	public static final String SID = "SID";

	public static final String Roles = "Roles";

	public static final String Password = "Password";

	public static final String PasswordHistory = "PasswordHistory";

	String getName();

	void setName(String name);

	String getSurName();

	void setSurName(String surName);

	String getSID();

	void setSID(String sid);

	String getDisplayname();

	void setDisplayname(String displayname);

	boolean isActive();

	void setActive(boolean active);

	@Override
	Password getPassword();

	void setPassword(Password password);

	@Override
	Collection<Password> getPasswordHistory();
}
