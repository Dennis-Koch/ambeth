package de.osthus.ambeth.helloworld.security;

import java.util.Calendar;

import de.osthus.ambeth.security.model.IPassword;

public class PojoPassword implements IPassword
{
	private char[] value;
	private Calendar changeAfter;
	private String algorithm;
	private int iterationCount;
	private int keySize;
	private char[] salt;
	private String saltAlgorithm;
	private String saltKeySpec;
	private Integer saltLength;

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
	public String getSaltKeySpec()
	{
		return saltKeySpec;
	}

	@Override
	public void setSaltKeySpec(String saltKeySpec)
	{
		this.saltKeySpec = saltKeySpec;
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
	public String getSaltAlgorithm()
	{
		return saltAlgorithm;
	}

	@Override
	public void setSaltAlgorithm(String saltAlgorithm)
	{
		this.saltAlgorithm = saltAlgorithm;
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
}
