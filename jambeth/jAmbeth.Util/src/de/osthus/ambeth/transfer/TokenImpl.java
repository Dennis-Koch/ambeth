package de.osthus.ambeth.transfer;

// TODO [DataContract(IsReference = true)]
public class TokenImpl implements IToken
{

	// TODO [DataMember]
	protected String value;

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public void setValue(String value)
	{
		this.value = value;
	}

}
