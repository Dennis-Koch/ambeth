package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Field;

public class NewArrayExpressionHandler extends AbstractExpressionHandler<JCNewArray>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCNewArray newArray)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		writer.append("new ");
		if (newArray.elemtype instanceof JCPrimitiveTypeTree)
		{
			languageHelper.writeType(((JCPrimitiveTypeTree) newArray.elemtype).type.toString());
		}
		else
		{
			Field field = context.getField();
			if (field != null)
			{
				Type fieldType = field.getFieldType();
				String fieldTypeString = fieldType.toString();
				while (fieldTypeString.endsWith("[]"))
				{
					fieldTypeString = fieldTypeString.substring(0, fieldTypeString.length() - 2);
				}
				languageHelper.writeType(fieldTypeString);
			}
			else
			{
				languageHelper.writeType(((JCIdent) newArray.elemtype).sym.toString());
			}
		}
		for (JCExpression dimension : newArray.getDimensions())
		{
			writer.append('[');
			writer.append(dimension.toString());
			writer.append(']');
		}
		if (newArray.getInitializers() != null)
		{
			// TODO: handle array initializers
			log.warn("Array initializer not yet supported: " + newArray.getInitializers());
			for (JCExpression initializer : newArray.getInitializers())
			{
			}
		}
		context.setTypeOnStack(newArray.type.toString());
	}
}
