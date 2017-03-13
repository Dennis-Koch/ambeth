package com.koch.ambeth.security;


public class AuthenticationResult implements IAuthenticationResult
{
	private final String sid;

	private final boolean changePasswordRecommended;

	private final boolean rehashPasswordRecommended;

	public AuthenticationResult(String sid, boolean changePasswordRecommended, boolean rehashPasswordRecommended)
	{
		this.sid = sid;
		this.changePasswordRecommended = changePasswordRecommended;
		this.rehashPasswordRecommended = rehashPasswordRecommended;
	}

	@Override
	public String getSID()
	{
		return sid;
	}

	@Override
	public boolean isChangePasswordRecommended()
	{
		return changePasswordRecommended;
	}

	@Override
	public boolean isRehashPasswordRecommended()
	{
		return rehashPasswordRecommended;
	}
}
