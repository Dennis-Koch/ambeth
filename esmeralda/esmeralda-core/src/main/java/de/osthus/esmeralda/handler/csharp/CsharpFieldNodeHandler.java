package de.osthus.esmeralda.handler.csharp;

import java.io.Writer;
import java.util.List;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;

public class CsharpFieldNodeHandler implements INodeHandlerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Autowired
	protected INodeHandlerRegistry nodeHandlerRegistry;

	@Override
	public void handle(Object astNode, ConversionContext context, Writer writer) throws Throwable
	{
		Field field = context.getField();

		boolean firstKeyWord = true;
		languageHelper.newLineIntend(context, writer);
		languageHelper.newLineIntend(context, writer);
		if (field.isPrivate())
		{
			writer.append("private");
			firstKeyWord = false;
		}
		else if (field.isProtected())
		{
			writer.append("protected");
			firstKeyWord = false;
		}
		else if (field.isPublic())
		{
			writer.append("public");
			firstKeyWord = false;
		}
		if (field.isStatic())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("static");
		}
		if (field.isFinal())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("readonly");
		}
		String[] fieldTypes = field.getFieldTypes().toArray(String.class);
		if (firstKeyWord)
		{
			firstKeyWord = false;
		}
		else
		{
			writer.append(' ');
		}
		languageHelper.writeType(fieldTypes[0], context, writer).append(' ').append(field.getName());

		ExpressionTree initializer = ((FieldInfo) field).getInitializer();
		if (initializer instanceof JCNewClass)
		{
			JCNewClass newClass = ((JCNewClass) initializer);
			List<JCExpression> args = newClass.args;
			List<Type> genericTypeArguments = ((ClassType) newClass.type).allparams_field;
			List<Type> argumentTypes = ((MethodType) newClass.constructor.type).argtypes;
			String owner = ((ClassSymbol) newClass.constructor.owner).fullname.toString();

			writer.append(" = new ");
			languageHelper.writeType(owner, context, writer);

			if (genericTypeArguments.size() > 0)
			{
				writer.append('<');
				for (int a = 0, size = genericTypeArguments.size(); a < size; a++)
				{
					Type genericTypeArgument = genericTypeArguments.get(a);
					if (a > 0)
					{
						writer.append(", ");
					}
					languageHelper.writeType(genericTypeArgument.toString(), context, writer);
				}
				writer.append('>');
			}

			writer.append('(');
			for (int a = 0, size = args.size(); a < size; a++)
			{
				JCExpression arg = args.get(a);
				if (a > 0)
				{
					writer.append(", ");
				}
				writer.append(arg.toString());
			}
			writer.append(')');
		}
		else if (initializer != null)
		{
			System.out.println();
		}

		writer.append(';');
	}
}
