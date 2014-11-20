package de.osthus.esmeralda.handler.csharp;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;

public class FieldAccessExpressionHandler extends AbstractExpressionHandler<JCFieldAccess>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCFieldAccess fieldAccess)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		JCExpression expression = fieldAccess.getExpression();
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
			if (typeForTypeof != null)
			{
				writer.append("typeof(");
				languageHelper.writeType(typeForTypeof);
				writer.append(')');
				return;
			}
		}
		if (expression instanceof JCIdent && ((JCIdent) expression).sym instanceof ClassSymbol)
		{
			languageHelper.writeType(((JCIdent) expression).sym.toString());
			writer.append('.');
			writer.append(name);
			return;
		}
		languageHelper.writeExpressionTree(fieldAccess.getExpression());
	}
}
