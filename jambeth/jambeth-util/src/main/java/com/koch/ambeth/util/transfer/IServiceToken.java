package com.koch.ambeth.util.transfer;

public interface IServiceToken<T>
{

	T getValue();

	IToken getToken();

	void setToken(IToken token);

}
