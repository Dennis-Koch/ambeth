package de.osthus.ambeth.xml.postprocess.merge;

import java.lang.reflect.Array;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.pending.ArraySetterCommand;
import de.osthus.ambeth.xml.pending.IObjectCommand;

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
