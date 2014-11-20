package de.osthus.esmeralda.handler.csharp;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Type;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.annotation.ConfigurationConstants;
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
	public void handle(Tree astNode)
	{
		IConversionContext context = this.context.getCurrent();
		Field field = context.getField();
		IWriter writer = context.getWriter();

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
		else if (languageHelper.isAnnotatedWith(field.getOwningClass(), ConfigurationConstants.class) && field.isPublic() && field.isStatic()
				&& field.isFinal())
		{
			// constants here must be a C# "const" instead of static readonly because of the time of value resolving (static instead of dynamic)
			writer.append("public const");
			firstKeyWord = false;
		}
		else
		{
			firstKeyWord = languageHelper.writeModifiers(field);
		}
		Type fieldType = field.getFieldType();
		firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
		languageHelper.writeType(fieldType.toString());
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
			languageHelper.writeExpressionTree(initializer);
		}
		if (finishWithSemicolon)
		{
			writer.append(';');
		}
	}
}
