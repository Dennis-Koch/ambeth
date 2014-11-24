package de.osthus.esmeralda.handler.csharp;

import com.sun.source.tree.Tree;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;

public class CsStatementHandler implements INodeHandlerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected INodeHandlerRegistry nodeHandlerRegistry;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void handle(Tree astNode)
	{
		// TODO: Obsolete code?
		throw new UnsupportedOperationException("Looks like not so obsolete code?");
		// IConversionContext context = this.context.getCurrent();
		// Field field = context.getField();
		// IWriter writer = context.getWriter();
		//
		// languageHelper.writeAnnotations(field);
		// languageHelper.newLineIntend();
		//
		// boolean annotatedWithAutowired = languageHelper.isAnnotatedWith(field, Autowired.class);
		// boolean annotatedWithProperty = languageHelper.isAnnotatedWith(field, Property.class);
		//
		// boolean firstKeyWord;
		// if (annotatedWithAutowired || annotatedWithProperty)
		// {
		// writer.append("public");
		// firstKeyWord = false;
		// }
		// else if (languageHelper.isAnnotatedWith(field.getOwningClass(), ConfigurationConstants.class) && field.isPublic() && field.isStatic()
		// && field.isFinal())
		// {
		// // constants here must be a C# "const" instead of static readonly because of the time of value resolving (static instead of dynamic)
		// writer.append("public const");
		// firstKeyWord = false;
		// }
		// else
		// {
		// firstKeyWord = languageHelper.writeModifiers(field);
		// }
		// String fieldType = field.getFieldType();
		// firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
		// languageHelper.writeType(fieldType);
		// writer.append(' ');
		//
		// boolean finishWithSemicolon = true;
		//
		// if (annotatedWithAutowired || annotatedWithProperty)
		// {
		// String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
		// // TODO remind changed name of the field for later access to the property get/set
		// writer.append(name).append(" { protected get; set; }");
		// finishWithSemicolon = false;
		// }
		// else if (languageHelper.isAnnotatedWith(field, LogInstance.class))
		// {
		// String name = StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
		// // TODO remind changed name of the field for later access to the property get/set
		// writer.append(name).append(" { private get; set; }");
		// finishWithSemicolon = false;
		// }
		// else
		// {
		// writer.append(field.getName());
		// }
		// ExpressionTree initializer = ((FieldInfo) field).getInitializer();
		// if (initializer != null)
		// {
		// writer.append(" = ");
		// languageHelper.writeExpressionTree(initializer);
		// }
		// if (finishWithSemicolon)
		// {
		// writer.append(';');
		// }
	}
}
