package com.koch.ambeth.cache.transfer;

import com.koch.ambeth.util.transfer.IServiceToken;
import com.koch.ambeth.util.transfer.IToken;

// TODO [DataContract(IsReference = true)]
public class ServiceToken<T> implements IServiceToken<T>
{

	// TODO [DataMember]
	protected IToken token;

	// TODO [DataMember]
	protected T value;

	@Override
	public IToken getToken()
	{
		return token;
	}

	@Override
	public void setToken(IToken token)
	{
		this.token = token;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	public void setValue(T value)
	{
		this.value = value;
	}

}
