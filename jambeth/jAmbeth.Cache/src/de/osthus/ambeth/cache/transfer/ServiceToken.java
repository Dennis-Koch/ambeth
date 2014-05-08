package de.osthus.ambeth.cache.transfer;

import de.osthus.ambeth.transfer.IServiceToken;
import de.osthus.ambeth.transfer.IToken;

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
