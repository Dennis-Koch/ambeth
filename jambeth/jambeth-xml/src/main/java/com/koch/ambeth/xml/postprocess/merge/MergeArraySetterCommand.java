package com.koch.ambeth.xml.postprocess.merge;

import java.lang.reflect.Array;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.pending.ArraySetterCommand;
import com.koch.ambeth.xml.pending.IObjectCommand;

public class MergeArraySetterCommand extends ArraySetterCommand implements IObjectCommand, IInitializingBean
{
	@Override
	public void execute(IReader reader)
	{
		Object value = objectFuture.getValue();
		if (IObjRef.class.isAssignableFrom(parent.getClass().getComponentType()))
		{
			// Happens in CUDResults in PostProcessing tags (<pp>)
			value = new DirectObjRef(value.getClass(), value);
		}
		Array.set(parent, index, value);
	}
}
