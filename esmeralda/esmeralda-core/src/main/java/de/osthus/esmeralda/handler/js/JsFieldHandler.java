package de.osthus.esmeralda.handler.js;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.tree.JCTree.JCLiteral;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IFieldHandler;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;

public class JsFieldHandler implements IFieldHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IJsHelper languageHelper;

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		Field field = context.getField();
		ExpressionTree initializer = ((FieldInfo) field).getInitializer();

		boolean privateStatic = field.isPrivate() && field.isStatic();

		languageHelper.newLineIndent();
		if (!privateStatic)
		{
			writeJsonField(field, initializer, writer);
		}
		else
		{
			writeField(field, initializer, writer);
		}
	}

	protected void writeJsonField(Field field, ExpressionTree initializer, IWriter writer)
	{
		writer.append('"');
		languageHelper.writeVariableName(field.getName());
		writer.append("\": ");
		if (initializer != null && initializer instanceof JCLiteral)
		{
			languageHelper.writeExpressionTree(initializer);
		}
		else
		{
			writer.append("undefined");
		}
		languageHelper.writeMetadata(field);
	}

	protected void writeField(Field field, ExpressionTree initializer, IWriter writer)
	{
		writer.append("var ").append(field.getName());
		if (initializer != null)
		{
			writer.append(" = ");
			languageHelper.writeExpressionTree(initializer);
		}
		writer.append(";");
	}
}
