package com.koch.ambeth.server.helloworld.security;

import java.util.Calendar;

import com.koch.ambeth.security.model.IPBEConfiguration;
import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.IUser;

public class PojoPassword implements IPassword
{
	private char[] value;
	private Calendar changeAfter;
	private String algorithm;
	private int iterationCount;
	private int keySize;
	private char[] salt;
	private Integer saltLength;

	private IUser user;

	@Override
	public IUser getHistoryUser()
	{
		return null;
	}

	@Override
	public IUser getUser()
	{
		return user;
	}

	public void setUser(IUser user)
	{
		this.user = user;
	}

	@Override
	public char[] getValue()
	{
		return value;
	}

	@Override
	public void setValue(char[] value)
	{
		this.value = value;
	}

	@Override
	public Calendar getChangeAfter()
	{
		return changeAfter;
	}

	@Override
	public void setChangeAfter(Calendar changeAfter)
	{
		this.changeAfter = changeAfter;
	}

	@Override
	public String getAlgorithm()
	{
		return algorithm;
	}

	@Override
	public void setAlgorithm(String algorithm)
	{
		this.algorithm = algorithm;
	}

	@Override
	public int getIterationCount()
	{
		return iterationCount;
	}

	@Override
	public void setIterationCount(int iterationCount)
	{
		this.iterationCount = iterationCount;
	}

	@Override
	public int getKeySize()
	{
		return keySize;
	}

	@Override
	public void setKeySize(int keySize)
	{
		this.keySize = keySize;
	}

	@Override
	public char[] getSalt()
	{
		return salt;
	}

	@Override
	public void setSalt(char[] salt)
	{
		this.salt = salt;
	}

	@Override
	public Integer getSaltLength()
	{
		return saltLength;
	}

	@Override
	public void setSaltLength(Integer saltLength)
	{
		this.saltLength = saltLength;
	}

	@Override
	public IPBEConfiguration getSaltPBEConfiguration()
	{
		return null;
	}
}
