package de.osthus.esmeralda.handler.csharp;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
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
	protected IConversionContext context;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Autowired
	protected INodeHandlerRegistry nodeHandlerRegistry;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void handle(Object astNode)
	{
		IConversionContext context = this.context.getCurrent();
		Field field = context.getField();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		languageHelper.writeAnnotations(field);
		languageHelper.newLineIntend();

		boolean annotatedWithAutowired = languageHelper.isAnnotatedWith(field, Autowired.class);
		boolean annotatedWithProperty = languageHelper.isAnnotatedWith(field, Property.class);

		boolean firstKeyWord;
		if (annotatedWithAutowired || annotatedWithProperty)
		{
			writer.append("public");
			firstKeyWord = false;
		}
		else
		{
			firstKeyWord = languageHelper.writeModifiers(field);
		}
		String[] fieldTypes = field.getFieldTypes().toArray(String.class);
		firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
		languageHelper.writeType(fieldTypes[0]);
		writer.append(' ');

		boolean finishWithSemicolon = true;

		if (annotatedWithAutowired || annotatedWithProperty)
		{
			String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
			// TODO remind changed name of the field for later access to the property get/set
			writer.append(name).append(" { protected get; set; }");
			finishWithSemicolon = false;
		}
		else if (languageHelper.isAnnotatedWith(field, LogInstance.class))
		{
			String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
			// TODO remind changed name of the field for later access to the property get/set
			writer.append(name).append(" { private get; set; }");
			finishWithSemicolon = false;
		}
		else
		{
			writer.append(field.getName());
		}
		ExpressionTree initializer = ((FieldInfo) field).getInitializer();
		if (initializer != null)
		{
			writer.append(" = ");
		}
		if (initializer instanceof JCLiteral || initializer instanceof JCIdent)
		{
			writer.append(initializer.toString());
		}
		else if (initializer instanceof JCNewClass)
		{
			languageHelper.writeNewInstance((JCNewClass) initializer);
		}
		else if (initializer instanceof JCFieldAccess)
		{
			JCExpression expression = ((JCFieldAccess) initializer).getExpression();
			if (expression instanceof JCIdent && ((JCIdent) expression).sym instanceof ClassSymbol)
			{
				writer.append("typeof(");
				writer.append(expression.toString());
				writer.append(')');
			}
			else
			{
				log.warn("Could not handle: " + initializer);
			}
		}
		else if (initializer != null)
		{
			log.warn("Could not handle: " + initializer);
		}
		if (finishWithSemicolon)
		{
			writer.append(';');
		}
	}
}
