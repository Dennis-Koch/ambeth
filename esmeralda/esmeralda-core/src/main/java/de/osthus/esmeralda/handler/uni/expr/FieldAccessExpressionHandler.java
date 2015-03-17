package de.osthus.esmeralda.handler.uni.expr;

import java.util.List;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.ITransformedField;
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
			JavaClassInfo classInfo = classInfoManager.resolveClassInfo(fqTypeName);
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
			JavaClassInfo typeErasureClassInfo = context.lookupTypeErasureHint(typeForTypeof);
			if (typeErasureClassInfo != null)
			{
				languageHelper.writeAsTypeOf(typeErasureClassInfo.getFqName());
			}
			else
			{
				languageHelper.writeAsTypeOf(typeForTypeof);
			}
			context.setTypeOnStack(java.lang.Class.class.getName());
			return;
		}
		if (expression instanceof JCIdent)
		{
			JCIdent identExpression = (JCIdent) expression;
			String fieldName = fieldAccess.getIdentifier().toString();
			if (identExpression.sym instanceof ClassSymbol)
			{
				ITransformedField transformedMemberAccess = methodTransformer.transformFieldAccess(identExpression.sym.toString(), fieldName);
				languageHelper.writeType(transformedMemberAccess.getOwner());
				writer.append('.').append(transformedMemberAccess.getName());
				context.setTypeOnStack(transformedMemberAccess.getReturnType());
				return;
			}
			if (identExpression.type != null)
			{
				JavaClassInfo classInfo = classInfoManager.resolveClassInfo(identExpression.type.toString(), true);
				if (classInfo != null)
				{
					Field field = classInfo.getField(fieldName, true);
					ITransformedField transformedMemberAccess = methodTransformer.transformFieldAccess(classInfo.getFqName(), fieldName);
					if (field != null && field.isStatic())
					{
						languageHelper.writeType(transformedMemberAccess.getOwner());
					}
					else
					{
						languageHelper.writeVariableNameAccess(identExpression.toString());
					}
					writer.append('.');
					languageHelper.writeVariableName(transformedMemberAccess.getName());
					context.setTypeOnStack(transformedMemberAccess.getReturnType());
					return;
				}
			}
			String ownerType = astHelper.resolveTypeFromVariableName(identExpression.toString());
			JavaClassInfo classInfo = classInfoManager.resolveClassInfo(ownerType);
			Field field = classInfo.getField(fieldName, true);
			ITransformedField transformedMemberAccess = methodTransformer.transformFieldAccess(ownerType, fieldName);
			if (field != null && field.isStatic())
			{
				languageHelper.writeType(transformedMemberAccess.getOwner());
			}
			else
			{
				languageHelper.writeVariableNameAccess(identExpression.toString());
			}
			writer.append('.');
			languageHelper.writeVariableName(transformedMemberAccess.getName());
			context.setTypeOnStack(transformedMemberAccess.getReturnType());
			return;

			// String typeOnStack = context.getTypeOnStack();
			// JavaClassInfo classInfoOnStack = context.resolveClassInfo(typeOnStack);
			// // FIXME With <anonymous de.osthus.ambeth.ioc.hierarchy.IBeanContextHolder<V>> onStack no field can be found
			// // Data for the actual (anonymous) implementation of the interface is missing
			// if (classInfoOnStack == null)
			// {
			// throw new SnippetTrigger("Missing details for anonymous class").setContext(typeOnStack);
			// }
			// // End of FIXME
			// Field fieldOfNameOfStack = classInfoOnStack.getField(name);
			// // String typeFromSymbolName = languageHelper.resolveTypeFromVariableName(name);
			// writer.append('.');
			//
			// languageHelper.writeVariableName(name);
			// context.setTypeOnStack(fieldOfNameOfStack.getFieldType());
			// return;
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

	protected JavaClassInfo resolveClassInfoOfVariable(Tree tree, CharSequence name)
	{
		IConversionContext context = this.context.getCurrent();
		if (tree instanceof JCVariableDecl)
		{
			JCVariableDecl varDecl = (JCVariableDecl) tree;
			if (varDecl.getName().contentEquals(name))
			{
				return classInfoManager.resolveClassInfo(varDecl.getType().toString());
			}
		}

		Object[] treePath = classInfoManager.findPathToTree(context.getMethod(), tree);

		for (int a = treePath.length; a-- > 0;)
		{
			Object treePathItem = treePath[a];
			if (treePathItem instanceof Number || treePathItem instanceof String)
			{
				continue;
			}
			if (treePathItem instanceof List)
			{
				Object indexR = a + 1 < treePath.length ? treePath[a + 1] : null;
				if (indexR instanceof Number)
				{
					List<?> list = (List<?>) treePathItem;
					int index = ((Integer) indexR).intValue();
					for (int b = index; b-- > 0;)
					{
						Object previousTree = list.get(b);
						if (previousTree instanceof JCVariableDecl)
						{
							JCVariableDecl varDecl = (JCVariableDecl) previousTree;
							if (varDecl.getName().contentEquals(name))
							{
								return classInfoManager.resolveClassInfo(varDecl.getType().toString());
							}
						}
						System.out.println();
					}
				}
			}
		}
		String type = astHelper.resolveTypeFromVariableName(name.toString());
		return classInfoManager.resolveClassInfo(type);
	}
}
