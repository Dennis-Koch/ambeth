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
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.esmeralda.CodeVisitor;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.handler.Variable;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.LocationInfo;
import demo.codeanalyzer.common.model.Method;
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
		methodInfo.setPath(path);
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
			IdentityHashSet<Tree> alreadyVisistedNodeSet = new IdentityHashSet<Tree>();
			for (VariableTree parameter : methodTree.getParameters())
			{
				allVariablesFromMethodSignature.add(parameter);
			}
			while (!(parentTreePath.getLeaf() instanceof JCCompilationUnit))
			{
				Tree leaf = parentTreePath.getLeaf();
				alreadyVisistedNodeSet.add(leaf);
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
				else if (leaf instanceof JCBlock)
				{
					// may contain final variable declarations which are used in a method body of the anonymous class
					for (JCStatement statement : ((JCBlock) leaf).getStatements())
					{
						if (statement instanceof JCVariableDecl && ((JCVariableDecl) statement).getModifiers().getFlags().contains(Modifier.FINAL))
						{
							allVariables.add(statement);
						}
						if (alreadyVisistedNodeSet.contains(statement))
						{
							// this is our child tree which we already visited. variables which are declared in this block but after the current child tree
							// are not considered because their are not referrable from within the anonymous class
							break;
						}

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
			String simpleName = CodeAnalyzerUtil.getSimpleNameFromQualifiedName(clazzInfo.getName());
			simpleName = simpleName.split("<")[0];
			methodInfo.setName(simpleName);
			clazzInfo.addConstructor(methodInfo);
		}
		else
		{
			methodInfo.setName(methodName);
		}

		LocationInfo locationInfo = DataSetterUtil.getLocationInfo(trees, path, methodTree);
		methodInfo.setLocationInfo(locationInfo);

		if (methodInfo.getOwningClass().isAnnotation())
		{
			clazzInfo.addMethod(methodInfo);
			return;
		}
		List<? extends TypeParameterTree> typeParameters = methodInfo.getMethodTree().getTypeParameters();
		if (typeParameters.size() == 0)
		{
			// nothing special for a non-generic method declaration
			clazzInfo.addMethod(methodInfo);
			return;
		}
		// check if it contains exactly 1 parameter with the generic type "java.util.Class<T>"
		ArrayList<VarSymbol> genericParametersToIgnoreOnMethod = new ArrayList<VarSymbol>();
		ArrayList<VarSymbol> parametersNeededGenericMethod = new ArrayList<VarSymbol>();
		ArrayList<VarSymbol> parametersNeededNongenericMethod = new ArrayList<VarSymbol>();
		if (methodInfo.getMethodTree().getReturnType() instanceof JCTypeApply)
		{
			// TODO: handle this
			clazzInfo.addMethod(methodInfo);
			return;
		}
		if (methodInfo.getMethodTree().getReturnType() instanceof JCPrimitiveTypeTree)
		{
			// TODO: handle this
			clazzInfo.addMethod(methodInfo);
			return;
		}
		if (methodInfo.getMethodTree().getReturnType() instanceof JCArrayTypeTree)
		{
			// TODO: handle this
			clazzInfo.addMethod(methodInfo);
			return;
		}
		// JCIdent returnTypeSymbol = (JCIdent) methodInfo.getMethodTree().getReturnType();
		// if (!(returnTypeSymbol.type instanceof TypeVar))
		// {
		// // TODO: handle this
		// clazzInfo.addMethod(methodInfo);
		// return;
		// }
		// TypeVar returnType = (TypeVar) returnTypeSymbol.type;

		for (VariableElement parameter : methodInfo.getParameters())
		{
			VarSymbol varSymbol = (VarSymbol) parameter;
			if (!varSymbol.type.tsym.toString().equals("java.lang.Class"))
			{
				parametersNeededGenericMethod.add(varSymbol);
				parametersNeededNongenericMethod.add(varSymbol);
				continue;
			}
			com.sun.tools.javac.util.List<Type> typeArguments = varSymbol.type.getTypeArguments();
			if (typeArguments.size() == 0)
			{
				parametersNeededGenericMethod.add(varSymbol);
				parametersNeededNongenericMethod.add(varSymbol);
				continue;
			}
			// Type typeArgumentClassParameter = typeArguments.get(0);
			parametersNeededNongenericMethod.add(varSymbol);
		}
		if (parametersNeededGenericMethod.size() == parametersNeededNongenericMethod.size())
		{
			clazzInfo.addMethod(methodInfo);
			return;
		}
		if (methodName.equals(DEFAULT_CONSTRUCTOR_NAME))
		{
			clazzInfo.addMethod(methodInfo);
			return;
		}
		// create a method signature for the genericParameters in addition to the "default" one
		// so we intentionally create 2 different C# methods for the given single java method
		// first we create the non-generic method with a System.Type as Argument and System.Object as result
		Method methodInfoNonGeneric = createMethodHandleNonGeneric(methodInfo, parametersNeededNongenericMethod);
		clazzInfo.addMethod(methodInfoNonGeneric);
		Method methodInfoGeneric = createMethodHandle(methodInfo, parametersNeededGenericMethod);
		clazzInfo.addMethod(methodInfoGeneric);
	}

	protected static Method createMethodHandleNonGeneric(Method methodTemplate, List<VarSymbol> parametersNeededNongenericMethod)
	{
		MethodInfo method = new MethodInfo();
		copyMethodAttributes(methodTemplate, method);

		method.setReturnType(Object.class.getName());

		List<? extends TypeParameterTree> typeParameters = method.getMethodTree().getTypeParameters();

		for (int a = 0, size = parametersNeededNongenericMethod.size(); a < size; a++)
		{
			VariableElement parameterTemplate = parametersNeededNongenericMethod.get(a);
			method.addParameters(parameterTemplate);
		}
		method.setTypeParameters(new TypeParameterTree[0]);
		// IList<VariableElement> parameters = methodTemplate.getParameters();
		// for (int a = 0, size = parameters.size(); a < size; a++)
		// {
		// VariableElement parameterTemplate = parameters.get(a);
		// method.addParameters(parameterTemplate);
		// for (VarSymbol genericParameterOfClass : genericParametersOfClass)
		// {
		// if (parameterTemplate == genericParameterOfClass)
		// {
		// method.addParameterIndexToEraseGenericType(a);
		// break;
		// }
		// }
		// }
		// VarSymbol vs = (VarSymbol) parameterTemplate;
		// ClassType givenClassType = (ClassType) vs.type;
		// ClassType classType = new ClassType(givenClassType.supertype_field, new ArrayList<Type>(), givenClassType.tsym);
		// // replace the genericParameterOfClass with a Class<?> parameter
		// VariableElement newVarElement = new VarSymbol(vs.flags_field, vs.name, classType, vs.owner);
		// method.addParameters(newVarElement);
		// }

		return method;
	}

	protected static void copyMethodAttributes(Method sourceMethod, MethodInfo targetMethod)
	{
		targetMethod.setName(sourceMethod.getName());
		targetMethod.setOwningClass(sourceMethod.getOwningClass());
		targetMethod.setAbstractFlag(sourceMethod.isAbstract());
		targetMethod.setFinalFlag(sourceMethod.isFinal());
		targetMethod.setNativeFlag(sourceMethod.isNative());
		targetMethod.setPrivateFlag(sourceMethod.isPrivate());
		targetMethod.setProtectedFlag(sourceMethod.isProtected());
		targetMethod.setPublicFlag(sourceMethod.isPublic());
		targetMethod.setMethodTree(sourceMethod.getMethodTree());
		targetMethod.setPath(sourceMethod.getPath());
		for (Annotation annotation : sourceMethod.getAnnotations())
		{
			targetMethod.addAnnotation(annotation);
		}
		for (String exception : sourceMethod.getExceptions())
		{
			targetMethod.addException(exception);
		}
	}

	protected static Method createMethodHandle(Method methodTemplate, List<VarSymbol> parametersNeededGenericMethod)
	{
		MethodInfo method = new MethodInfo();
		copyMethodAttributes(methodTemplate, method);

		method.setReturnType(methodTemplate.getReturnType());

		for (int a = 0, size = parametersNeededGenericMethod.size(); a < size; a++)
		{
			VariableElement parameterTemplate = parametersNeededGenericMethod.get(a);
			method.addParameters(parameterTemplate);
		}
		// IList<VariableElement> parameters = methodTemplate.getParameters();
		// for (int a = 0, size = parameters.size(); a < size; a++)
		// {
		// VariableElement parameterTemplate = parameters.get(a);
		// boolean isToDelete = false;
		// for (VarSymbol genericParameterOfClass : genericParametersOfClass)
		// {
		// if (parameterTemplate == genericParameterOfClass)
		// {
		// // skip the parameter of the generic class because we write this information in the methodName later
		// isToDelete = true;
		// break;
		// }
		// }
		// if (isToDelete)
		// {
		// method.addParameterIndexToDelete(a);
		// continue;
		// }
		// method.addParameters(parameterTemplate);
		// }
		return method;
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
