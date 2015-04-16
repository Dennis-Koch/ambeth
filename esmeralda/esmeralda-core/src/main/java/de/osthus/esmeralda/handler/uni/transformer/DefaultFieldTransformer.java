package de.osthus.esmeralda.handler.uni.transformer;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IClassInfoManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IFieldTransformerExtension;
import de.osthus.esmeralda.handler.ITransformedField;
import de.osthus.esmeralda.handler.TransformedMemberAccess;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class DefaultFieldTransformer implements IFieldTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IClassInfoManager classInfoManager;

	@Autowired
	protected IConversionContext context;

	@Override
	public final ITransformedField buildFieldTransformation(String owner, String name)
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo internalClassInfo = classInfoManager.resolveClassInfo(owner + "." + name, true);
		if (internalClassInfo != null)
		{
			return new TransformedMemberAccess(internalClassInfo.getFqName(), null, internalClassInfo.getFqName());
		}
		return buildFieldTransformation(owner, name, context);
	}

	protected ITransformedField buildFieldTransformation(String owner, String name, IConversionContext context)
	{
		JavaClassInfo classInfo = classInfoManager.resolveClassInfo(owner);
		Field field = classInfo.getField(name);
		return new TransformedMemberAccess(owner, name, field.getFieldType());
	}
}
