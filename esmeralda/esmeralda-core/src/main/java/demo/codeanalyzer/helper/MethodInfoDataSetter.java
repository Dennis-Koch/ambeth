package demo.codeanalyzer.helper;

import static demo.codeanalyzer.common.util.CodeAnalyzerConstants.DEFAULT_CONSTRUCTOR_NAME;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor6;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.esmeralda.CodeVisitor;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.handler.Variable;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.LocationInfo;
import demo.codeanalyzer.common.model.MethodInfo;
import demo.codeanalyzer.common.util.CodeAnalyzerUtil;

/**
 * Helper class to set the properties of a method to the java class model
 * 
 * @author Seema Richard (Seema.Richard@ust-global.com)
 * @author Deepa Sobhana (Deepa.Sobhana@ust-global.com)
 */
public class MethodInfoDataSetter
{

	/**
	 * Set the attributes of the currently visiting method to the java class model
	 * 
	 * @param clazzInfo
	 *            The java class model
	 * @param methodTree
	 *            Curently visiting method tree
	 * @param path
	 *            tree path
	 * @param trees
	 *            trees
	 */
	public static void populateMethodInfo(JavaClassInfo clazzInfo, MethodTree methodTree, TreePath path, Trees trees)
	{

		final MethodInfo methodInfo = new MethodInfo();
		methodInfo.setMethodTree(methodTree);
		String methodName = methodTree.getName().toString();
		methodInfo.setOwningClass(clazzInfo);
		// Set modifier details
		Element e = trees.getElement(path);
		if (e == null)
		{
			return;
		}
		// Set the param type and return path
		visitExecutable(e, methodInfo);

		if (clazzInfo.isAnonymous())
		{
			TreePath parentTreePath = path.getParentPath();
			final ArrayList<Object> allVariables = new ArrayList<Object>();
			final ArrayList<VariableTree> allVariablesFromMethodSignature = new ArrayList<VariableTree>();
			for (VariableTree parameter : methodTree.getParameters())
			{
				allVariablesFromMethodSignature.add(parameter);
			}
			while (!(parentTreePath.getLeaf() instanceof JCCompilationUnit))
			{
				Tree leaf = parentTreePath.getLeaf();
				if (leaf instanceof JCVariableDecl)
				{
					allVariables.add(leaf);
				}
				else if (leaf instanceof JCClassDecl)
				{
					String className = NewClassExpressionHandler.findFqAnonymousName(parentTreePath);
					className = NewClassExpressionHandler.getFqNameFromAnonymousName(className);
					JavaClassInfo parentClassInfo = null;
					for (JavaClassInfo classInfoItem : CodeVisitor.getClassInfoStack())
					{
						if (classInfoItem.toString().equals(className))
						{
							parentClassInfo = classInfoItem;
							break;
						}
					}
					if (parentClassInfo == null)
					{
						throw new IllegalStateException();
					}
					for (Field field : parentClassInfo.getFields())
					{
						allVariables.add(field);
					}
				}
				else if (leaf instanceof JCMethodDecl)
				{
					for (JCVariableDecl parameter : ((JCMethodDecl) leaf).getParameters())
					{
						allVariables.add(parameter);
					}
				}
				parentTreePath = parentTreePath.getParentPath();
			}
			ArrayList<IVariable> allUsedVariables = new ArrayList<IVariable>();

			methodTree.getBody().accept(new TreeScanner<Object, List<IVariable>>()
			{
				@Override
				public Object visitIdentifier(IdentifierTree identifierTree, List<IVariable> allUsedVariables)
				{
					if (!(((JCIdent) identifierTree).sym instanceof VarSymbol))
					{
						return super.visitIdentifier(identifierTree, allUsedVariables);
					}
					String name = identifierTree.toString();
					// look for identifier on parent method stack or field of parent object
					if ("super".equals(name))
					{
						return super.visitIdentifier(identifierTree, allUsedVariables);
					}
					for (VariableTree variable : allVariablesFromMethodSignature)
					{
						JCVariableDecl variableDecl = (JCVariableDecl) variable;
						String variableName = variableDecl.getName().toString();
						if (variableName.equals(name))
						{
							// current identifier is a simple method argument
							return super.visitIdentifier(identifierTree, allUsedVariables);
						}
					}
					for (Object variable : allVariables)
					{
						if (variable instanceof JCVariableDecl)
						{
							JCVariableDecl variableDecl = (JCVariableDecl) variable;
							String variableName = variableDecl.getName().toString();
							if (variableName.equals(name))
							{
								// current identifier is declared in enclosing code
								allUsedVariables.add(new Variable(variableDecl));
								return super.visitIdentifier(identifierTree, allUsedVariables);
							}
						}
						else if (variable instanceof Field)
						{
							Field field = (Field) variable;
							String variableName = field.getName();
							if (variableName.equals(name))
							{
								// current identifier is declared in enclosing code
								allUsedVariables.add(new Variable(field));
								return super.visitIdentifier(identifierTree, allUsedVariables);
							}
						}
						else
						{
							throw new IllegalStateException();
						}
					}
					return super.visitIdentifier(identifierTree, allUsedVariables);
				}
			}, allUsedVariables);
			clazzInfo.addUsedVariables(allUsedVariables);
		}
		// set modifiers
		for (Modifier modifier : e.getModifiers())
		{
			DataSetterUtil.setModifiers(modifier.toString(), methodInfo);
		}

		// Check if the method is a default constructor
		if (methodName.equals(DEFAULT_CONSTRUCTOR_NAME))
		{
			methodInfo.setName(CodeAnalyzerUtil.getSimpleNameFromQualifiedName(clazzInfo.getName()));
			clazzInfo.addConstructor(methodInfo);
		}
		else
		{
			clazzInfo.addMethod(methodInfo);
			methodInfo.setName(methodName);
		}

		LocationInfo locationInfo = DataSetterUtil.getLocationInfo(trees, path, methodTree);
		methodInfo.setLocationInfo(locationInfo);
	}

	/**
	 * Visit the element passed to this method to extract the parameter types and return type of the method
	 * 
	 * @param e
	 *            Element being visited
	 * @param methodInfo
	 *            Model which holds method-level attributes
	 */
	private static void visitExecutable(Element e, MethodInfo methodInfo)
	{
		e.accept(new SimpleElementVisitor6<Object, MethodInfo>()
		{
			@Override
			public Object visitUnknown(Element e, MethodInfo p)
			{
				return super.visitUnknown(e, p);
			}

			@Override
			public Object visitExecutable(ExecutableElement element, MethodInfo mInfo)
			{
				for (VariableElement var : element.getParameters())
				{
					mInfo.addParameters(var);
				}
				mInfo.setReturnType(element.getReturnType().toString());
				return super.visitExecutable(element, mInfo);
			}

			@Override
			public Object visitVariable(VariableElement e, MethodInfo p)
			{
				return super.visitVariable(e, p);
			}
		}, methodInfo);
	}
}
