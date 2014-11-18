package de.osthus.esmeralda.handler.csharp;

import java.io.Writer;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;
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

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void handle(Object astNode, ConversionContext context, Writer writer) throws Throwable
	{
		Field field = context.getField();

		languageHelper.newLineIntend(context, writer);
		languageHelper.writeAnnotations(field, context, writer);
		languageHelper.newLineIntend(context, writer);

		boolean annotatedWithAutowired = languageHelper.isAnnotatedWith(field, Autowired.class, context);
		boolean annotatedWithProperty = languageHelper.isAnnotatedWith(field, Property.class, context);

		boolean firstKeyWord;
		if (annotatedWithAutowired || annotatedWithProperty)
		{
			writer.append("public");
			firstKeyWord = false;
		}
		else
		{
			firstKeyWord = languageHelper.writeModifiers(field, context, writer);
		}
		String[] fieldTypes = field.getFieldTypes().toArray(String.class);
		firstKeyWord = languageHelper.spaceIfFalse(firstKeyWord, context, writer);
		languageHelper.writeType(fieldTypes[0], context, writer).append(' ');

		boolean finishWithSemicolon = true;

		if (annotatedWithAutowired || annotatedWithProperty)
		{
			String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
			// TODO remind changed name of the field for later access to the property get/set
			writer.append(name).append(" { protected get; set; }");
			finishWithSemicolon = false;
		}
		else if (languageHelper.isAnnotatedWith(field, LogInstance.class, context))
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
		if (initializer instanceof JCNewClass)
		{
			languageHelper.writeNewInstance((JCNewClass) initializer, context, writer);
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
