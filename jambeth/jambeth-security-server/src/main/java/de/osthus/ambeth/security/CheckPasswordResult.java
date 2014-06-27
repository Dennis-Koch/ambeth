package de.osthus.ambeth.security;

public class CheckPasswordResult implements ICheckPasswordResult
{
	protected final boolean passwordCorrect, changeRecommended, rehashRecommended;

	public CheckPasswordResult(boolean passwordCorrect, boolean changeRecommended, boolean rehashRecommended)
	{
		this.passwordCorrect = passwordCorrect;
		this.changeRecommended = changeRecommended;
		this.rehashRecommended = rehashRecommended;
	}

	@Override
	public boolean isPasswordCorrect()
	{
		return passwordCorrect;
	}

	@Override
	public boolean isChangePasswordRecommended()
	{
		return changeRecommended;
	}

	@Override
	public boolean isRehashPasswordRecommended()
	{
		return rehashRecommended;
	}
}
