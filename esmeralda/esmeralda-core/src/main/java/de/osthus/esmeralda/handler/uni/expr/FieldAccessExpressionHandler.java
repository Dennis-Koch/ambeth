package de.osthus.esmeralda.handler.uni.expr;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.ITransformedMemberAccess;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.snippet.SnippetTrigger;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class FieldAccessExpressionHandler extends AbstractExpressionHandler<JCFieldAccess>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IMethodTransformer methodTransformer;

	@Override
	protected void handleExpressionIntern(JCFieldAccess fieldAccess)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		JCExpression expression = fieldAccess.getExpression();

		if (fieldAccess.sym instanceof ClassSymbol)
		{
			String fqTypeName = fieldAccess.toString();
			JavaClassInfo classInfo = context.resolveClassInfo(fqTypeName);
			fqTypeName = classInfo.getFqName();
			languageHelper.writeType(fqTypeName);
			context.setTypeOnStack(fqTypeName);
			return;
		}

		String name = fieldAccess.name.toString();
		if ("class".equals(name))
		{
			String typeForTypeof = null;
			if (expression instanceof JCIdent && ((JCIdent) expression).sym instanceof ClassSymbol)
			{
				typeForTypeof = ((JCIdent) expression).sym.toString();
			}
			else if (expression instanceof JCIdent && ((JCIdent) expression).sym == null)
			{
				typeForTypeof = ((JCIdent) expression).name.toString();
			}
			else if (expression instanceof JCPrimitiveTypeTree)
			{
				typeForTypeof = expression.toString();
			}
			else if (expression instanceof JCArrayTypeTree)
			{
				typeForTypeof = ((JCArrayTypeTree) expression).type.toString();
			}
			else
			{
				typeForTypeof = expression.toString();
			}
			languageHelper.writeAsTypeOf(typeForTypeof);
			context.setTypeOnStack(java.lang.Class.class.getName());
			return;
		}
		if (expression instanceof JCIdent)
		{
			JCIdent identityExpression = (JCIdent) expression;
			if (identityExpression.sym instanceof ClassSymbol)
			{
				ITransformedMemberAccess transformedMemberAccess = methodTransformer.transformFieldAccess(identityExpression.sym.toString(), fieldAccess
						.getIdentifier().toString());
				languageHelper.writeType(transformedMemberAccess.getOwner());
				writer.append('.').append(transformedMemberAccess.getName());
				context.setTypeOnStack(transformedMemberAccess.getReturnType());
			}
			else
			{
				// TODO Check if ok in C#
				try
				{
					JavaClassInfo classInfo = context.resolveClassInfo(identityExpression.toString(), true);
					if (classInfo != null)
					{
						String fqName = classInfo.getFqName();
						languageHelper.writeType(fqName);
						context.setTypeOnStack(fqName);
					}
					else
					{
						languageHelper.writeExpressionTree(identityExpression);
					}
				}
				catch (Throwable e)
				{
					languageHelper.writeExpressionTree(identityExpression);
				}
				String typeOnStack = context.getTypeOnStack();
				JavaClassInfo classInfoOnStack = context.resolveClassInfo(typeOnStack);
				// FIXME With <anonymous de.osthus.ambeth.ioc.hierarchy.IBeanContextHolder<V>> onStack no field can be found
				// Data for the actual (anonymous) implementation of the interface is missing
				if (classInfoOnStack == null)
				{
					throw new SnippetTrigger("Missing details for anonymous class").setContext(typeOnStack);
				}
				// End of FIXME
				Field fieldOfNameOfStack = classInfoOnStack.getField(name);
				// String typeFromSymbolName = languageHelper.resolveTypeFromVariableName(name);
				writer.append('.');
				if (classInfoOnStack.isArray() && name.equals("length"))
				{
					name = "Length";
				}
				languageHelper.writeVariableName(name);
				context.setTypeOnStack(fieldOfNameOfStack.getFieldType());
			}
			return;
		}

		languageHelper.writeExpressionTree(expression);
		writer.append('.').append(name);

		if (fieldAccess.type == null)
		{
			log.warn("No type in method invocation '" + fieldAccess + "'");
		}
		else
		{
			context.setTypeOnStack(fieldAccess.type.toString());
		}
	}
}
