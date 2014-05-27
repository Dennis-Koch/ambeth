package de.osthus.ambeth.transfer;

public interface IServiceToken<T>
{

	T getValue();

	IToken getToken();

	void setToken(IToken token);

}
