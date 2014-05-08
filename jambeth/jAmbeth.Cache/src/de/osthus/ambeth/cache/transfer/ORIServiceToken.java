package de.osthus.ambeth.cache.transfer;

import java.util.List;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.transfer.IServiceToken;

// TODO [DataContract(IsReference = true)]
public class ORIServiceToken extends ServiceToken<List<IObjRef>> implements IServiceToken<List<IObjRef>>
{

	// TODO [DataMember]
	@Override
	public List<IObjRef> getValue()
	{
		return super.value;
	}

	@Override
	public void setValue(List<IObjRef> value)
	{
		super.value = value;
	}

}
