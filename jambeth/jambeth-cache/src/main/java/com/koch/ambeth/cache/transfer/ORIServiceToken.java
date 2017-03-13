package com.koch.ambeth.cache.transfer;

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.transfer.IServiceToken;

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
