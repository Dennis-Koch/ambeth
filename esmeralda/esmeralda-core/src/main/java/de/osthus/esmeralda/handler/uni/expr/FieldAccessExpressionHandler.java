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
				languageHelper.writeExpressionTree(identityExpression);
				String typeOnStack = context.getTypeOnStack();
				JavaClassInfo classInfoOnStack = context.resolveClassInfo(typeOnStack);
				Field fieldOfNameOfStack = classInfoOnStack.getField(name);
				// String typeFromSymbolName = languageHelper.resolveTypeFromVariableName(name);
				writer.append('.').append(name);
				context.setTypeOnStack(fieldOfNameOfStack.getFieldType());
			}
			return;
		}
		languageHelper.writeExpressionTree(expression);

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
