package de.osthus.esmeralda.handler.csharp;

import java.util.EnumSet;
import java.util.concurrent.locks.Condition;
import java.util.regex.Matcher;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.TypeResolveException;

public class MethodInvocationExpressionHandler extends AbstractExpressionHandler<JCMethodInvocation>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCMethodInvocation methodInvocation)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		if (methodInvocation.meth == null)
		{
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		String methodName;
		String owner;
		boolean writeOwnerAsType = false;
		String typeOfOwner;
		if (methodInvocation.meth instanceof JCIdent)
		{
			JCIdent ident = (JCIdent) methodInvocation.meth;
			methodName = ident.name.toString();
			owner = null;
			typeOfOwner = context.getClassInfo().getPackageName() + "." + context.getClassInfo().getName();
		}
		else
		{
			JCFieldAccess meth = (JCFieldAccess) methodInvocation.meth;
			if (meth.selected instanceof JCLiteral)
			{
				owner = ((JCLiteral) meth.selected).value.toString();
				typeOfOwner = ((JCLiteral) meth.selected).type.toString();
			}
			else if (meth.selected instanceof JCFieldAccess)
			{
				JCFieldAccess fieldAccess = (JCFieldAccess) meth.selected;
				languageHelper.writeExpressionTree(fieldAccess);
				owner = null;
				if (fieldAccess.type == null)
				{// TODO: handle this case. Is this an error in the sources? Is there something missing?
					throw new TypeResolveException("No type in method invocation '" + methodInvocation + "'");
				}
				typeOfOwner = fieldAccess.type.toString();
			}
			else if (meth.selected instanceof JCMethodInvocation)
			{
				JCMethodInvocation mi = (JCMethodInvocation) meth.selected;
				languageHelper.writeExpressionTree(mi);
				owner = null;
				if (mi.type == null)
				{// TODO: handle this case. Code does not work with fluent APIs
					throw new TypeResolveException("No type in method invocation '" + methodInvocation + "'");
				}
				typeOfOwner = mi.type.toString();
			}
			else if (meth.selected instanceof JCNewClass)
			{
				JCNewClass newClass = (JCNewClass) meth.selected;
				languageHelper.writeExpressionTree(newClass);
				owner = null;
				typeOfOwner = newClass.type.toString();
			}
			else
			{
				JCIdent selected = (JCIdent) meth.selected;
				if (selected.sym instanceof VarSymbol)
				{
					owner = selected.sym.toString();
					typeOfOwner = selected.type.toString();
				}
				else if (selected.sym instanceof ClassSymbol)
				{
					owner = selected.type.toString();
					typeOfOwner = selected.type.toString();
					writeOwnerAsType = true;
				}
				else if (selected.sym == null)
				{
					owner = selected.toString();
					typeOfOwner = selected.toString();
					writeOwnerAsType = true;
				}
				else
				{
					throw new IllegalStateException("Unknown symbol type: " + selected.sym + " (" + selected.sym.getClass().getName() + ")");
				}
			}
			methodName = meth.name.toString();
		}
		String nonGenericTypeOfOwner = typeOfOwner;
		Matcher genericTypeMatcher = ConversionContext.genericTypePattern.matcher(nonGenericTypeOfOwner);
		if (genericTypeMatcher.matches())
		{
			nonGenericTypeOfOwner = genericTypeMatcher.group(1);
		}
		if (EnumSet.class.getName().equals(nonGenericTypeOfOwner))
		{
			// if we handle the enums either as C# enums or as static readonly objects will be decided by the flags-annotation
			// TODO: read integrity-xml of .NET and look whether the enum has this annotation
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		if (Condition.class.getName().equals(nonGenericTypeOfOwner))
		{
			// TODO: handle java.concurrent.lock API
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		if (owner != null)
		{
			if (writeOwnerAsType)
			{
				languageHelper.writeType(owner);
			}
			else
			{
				writer.append(owner);
			}
		}
		writer.append('.');

		methodName = StringConversionHelper.upperCaseFirst(objectCollector, methodName);
		boolean isPropertyInvocation = false;
		if (Class.class.getName().equals(nonGenericTypeOfOwner))
		{
			if ("GetSimpleName".equals(methodName))
			{
				methodName = "Name";
				isPropertyInvocation = true;
			}
			else if ("GetName".equals(methodName))
			{
				methodName = "FullName";
				isPropertyInvocation = true;
			}
		}
		writer.append(methodName);
		if (!isPropertyInvocation)
		{
			languageHelper.writeMethodArguments(methodInvocation.getArguments());
		}
		else if (methodInvocation.getArguments().size() > 0)
		{
			// C# will be an assignment to a property (setter-semantics)
			writer.append(" = ");
			boolean firstArgument = true;
			for (JCExpression argument : methodInvocation.getArguments())
			{
				firstArgument = languageHelper.writeStringIfFalse(", ", firstArgument);
				languageHelper.writeExpressionTree(argument);
			}
		}
	}
}
