package de.osthus.esmeralda.handler.csharp;

import com.sun.source.tree.ExpressionTree;

import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IFieldHandler;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;

public class CsFieldHandler implements IFieldHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		Field field = context.getField();
		IWriter writer = context.getWriter();

		languageHelper.writeAnnotations(field);
		languageHelper.newLineIndent();

		boolean annotatedWithAutowired = astHelper.isAnnotatedWith(field, Autowired.class);
		boolean annotatedWithProperty = astHelper.isAnnotatedWith(field, Property.class);
		boolean annotatedWithLogInstance = astHelper.isAnnotatedWith(field, LogInstance.class);

		boolean firstKeyWord;
		if (annotatedWithAutowired || annotatedWithProperty || annotatedWithLogInstance)
		{
			writer.append("public");
			firstKeyWord = false;
		}
		else if (astHelper.isAnnotatedWith(field.getOwningClass(), ConfigurationConstants.class) && field.isPublic() && field.isStatic() && field.isFinal())
		{
			// constants here must be a C# "const" instead of static readonly because of the time of value resolving (static instead of dynamic)
			writer.append("public const");
			firstKeyWord = false;
		}
		else
		{
			firstKeyWord = languageHelper.writeModifiers(field);
		}
		String fieldType = field.getFieldType();
		firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
		languageHelper.writeType(fieldType);
		writer.append(' ');

		boolean finishWithSemicolon = true;

		if (annotatedWithAutowired || annotatedWithProperty)
		{
			String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
			context.mapSymbolTransformation(field.getName(), name);
			writer.append(name).append(" { protected get; set; }");
			finishWithSemicolon = false;
		}
		else if (annotatedWithLogInstance)
		{
			String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
			context.mapSymbolTransformation(field.getName(), name);
			writer.append(name).append(" { private get; set; }");
			finishWithSemicolon = false;
		}
		else
		{
			languageHelper.writeVariableName(field.getName());
		}
		ExpressionTree initializer = ((FieldInfo) field).getInitializer();
		if (initializer != null)
		{
			writer.append(" = ");
			languageHelper.writeExpressionTree(initializer);
		}
		if (finishWithSemicolon)
		{
			writer.append(';');
		}
	}
}
