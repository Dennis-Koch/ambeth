package de.osthus.esmeralda.handler.csharp;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;

import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.ITransformedMemberAccess;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.TransformedMemberAccess;
import de.osthus.esmeralda.handler.TransformedMethod;
import de.osthus.esmeralda.misc.EsmeraldaWriter;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.NoOpWriter;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class MethodTransformer implements IMethodTransformer
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public ITransformedMethod transform(String owner, String methodName, List<JCExpression> parameterTypes)
	{
		String[] argTypes = parseArgumentTypes(parameterTypes);

		String nonGenericOwner = owner;
		Matcher genericTypeMatcher = ConversionContext.genericTypePattern.matcher(owner);
		if (genericTypeMatcher.matches())
		{
			nonGenericOwner = genericTypeMatcher.group(1);
		}
		// if (EnumSet.class.getName().equals(nonGenericOwner))
		// {
		// // if we handle the enums either as C# enums or as static readonly objects will be decided by the flags-annotation
		// // TODO: read integrity-xml of .NET and look whether the enum has this annotation
		// throw new RuntimeException("EnumSet not yet supported");
		// }
		// if (Condition.class.getName().equals(nonGenericOwner))
		// {
		// // TODO: handle java.concurrent.lock API
		// throw new RuntimeException("Condition not yet supported");
		// }
		// if (owner != null)
		// {
		// if (writeOwnerAsType)
		// {
		// languageHelper.writeType(owner);
		// }
		// else
		// {
		// writer.append(owner);
		// }
		// writer.append('.');
		// }
		// else if (writeMethodDot)
		// {
		// writer.append('.');
		// }

		String transformedOwner = owner;
		String formattedMethodName = StringConversionHelper.upperCaseFirst(objectCollector, methodName);
		boolean isPropertyInvocation = false;
		if (Class.class.getName().equals(nonGenericOwner))
		{
			transformedOwner = "System.Type";
			if ("getSimpleName".equals(methodName))
			{
				formattedMethodName = "Name";
				isPropertyInvocation = true;
			}
			else if ("getName".equals(methodName))
			{
				formattedMethodName = "FullName";
				isPropertyInvocation = true;
			}
		}
		if (PrintStream.class.getName().equals(nonGenericOwner))
		{
			transformedOwner = "System.Console";
			if ("println".equals(methodName))
			{
				formattedMethodName = "WriteLine";
			}
			else if ("print".equals(methodName))
			{
				formattedMethodName = "Write";
			}
		}
		return new TransformedMethod(transformedOwner, formattedMethodName, argTypes, isPropertyInvocation, false);
	}

	@Override
	public ITransformedMethod transform(Method method)
	{
		return null;
	}

	@Override
	public ITransformedMemberAccess transformFieldAccess(final String owner, final String name)
	{
		JavaClassInfo classInfo = context.resolveClassInfo(owner);
		Field field = classInfo.getField(name);

		return new TransformedMemberAccess(owner, name, field.getFieldType());
	}

	protected String[] parseArgumentTypes(List<JCExpression> parameterTypes)
	{
		IWriter oldWriter = context.getWriter();
		context.setWriter(new EsmeraldaWriter(new NoOpWriter()));
		try
		{
			String[] argTypes = new String[parameterTypes.size()];
			for (int a = 0, size = parameterTypes.size(); a < size; a++)
			{
				JCExpression arg = parameterTypes.get(a);
				languageHelper.writeExpressionTree(arg);
				String typeOnStack = context.getTypeOnStack();
				argTypes[a] = typeOnStack;
			}
			return argTypes;
		}
		finally
		{
			context.setWriter(oldWriter);
		}
	}
}
