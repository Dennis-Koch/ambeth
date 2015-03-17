package de.osthus.esmeralda;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IClassInfoFactory;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.MethodInvocationExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.SynthVariableElement;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.common.model.MethodInfo;

public class ClassInfoManager implements IClassInfoManager
{
	protected static final Pattern extendsFromPattern = Pattern.compile("\\s*\\?\\s+extends\\s+(.+)\\s*");

	protected static final Pattern variableName = Pattern.compile("[a-z]\\w*");

	protected static final HashSet<String> primitiveTypeNames = new HashSet<>(Arrays.asList("void", "boolean", "char", "byte", "short", "int", "long", "float",
			"double"));

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IClassInfoFactory classInfoFactory;

	@Autowired
	protected IConversionContext context;

	protected HashMap<String, Integer> calledMethods;

	protected HashSet<String> definedMethods;

	private final IMap<String, JavaClassInfo> fqNameToClassInfoMap = new HashMap<String, JavaClassInfo>();

	private final HashMap<AlreadyTriedKey, JavaClassInfo> alreadyTriedAndFailedMap = new HashMap<AlreadyTriedKey, JavaClassInfo>();

	private final IdentityHashMap<Tree, Object[]> expressionToTreePathMap = new IdentityHashMap<Tree, Object[]>();

	private final ArrayList<JavaClassInfo> cyclicBoundCheck = new ArrayList<JavaClassInfo>();

	@Override
	public void init(java.util.List<JavaClassInfo> classInfos)
	{
		for (JavaClassInfo classInfo : classInfos)
		{
			String fqName = classInfo.getFqName();
			if (!fqNameToClassInfoMap.putIfNotExists(fqName, classInfo))
			{
				throw new IllegalStateException("Full qualified name is not unique: " + fqName);
			}
			String nonGenericFqName = astHelper.extractNonGenericType(classInfo.getFqName());
			if (!nonGenericFqName.equals(fqName) && !fqNameToClassInfoMap.putIfNotExists(nonGenericFqName, classInfo))
			{
				throw new IllegalStateException("Full qualified name is not unique: " + nonGenericFqName);
			}
		}
		IConversionContext context = this.context.getCurrent();
		for (JavaClassInfo classInfo : classInfos)
		{
			JavaClassInfo oldClassInfo = context.getClassInfo();
			context.setClassInfo(classInfo);
			try
			{
				classInfo.fillTypeArgumentsIfNecessary();
			}
			finally
			{
				context.setClassInfo(oldClassInfo);
			}
		}
	}

	protected String getResolveGenericFqTypeName(String fqTypeName)
	{
		// Method method = getMethod();
		// for (VariableElement parameter : method.getParameters())
		// {
		// {
		// TypeMirror asType = parameter.asType();
		// if (asType instanceof ArrayType)
		// {
		// ArrayType arrayType = (ArrayType) asType;
		// if (arrayType.elemtype instanceof TypeVar)
		// {
		// Type upperBound = arrayType.elemtype.getUpperBound();
		//
		// }
		// }
		// if (asType.toString().equals(fqTypeName))
		// {
		//
		// }
		// }
		return fqTypeName;
		// }
	}

	@Override
	public JavaClassInfo resolveClassInfo(String fqTypeName)
	{
		return resolveClassInfo(fqTypeName, false);
	}

	@Override
	public JavaClassInfo resolveClassInfo(String fqTypeName, boolean tryOnly)
	{
		return resolveClassInfo(fqTypeName, tryOnly, true);
	}

	protected JavaClassInfo classInfoResolved(String fqTypeName, JavaClassInfo resolvedClassInfo)
	{
		AlreadyTriedKey alreadyTriedKey = new AlreadyTriedKey(context.getClassInfo().getTreePath(), fqTypeName);
		alreadyTriedAndFailedMap.put(alreadyTriedKey, resolvedClassInfo);
		return resolvedClassInfo;
	}

	protected JavaClassInfo resolveClassInfo(String fqTypeName, boolean tryOnly, boolean cascadeSearch)
	{
		if (fqTypeName == null || fqTypeName.length() == 0 || "<none>".equals(fqTypeName) || isVariableName(fqTypeName))
		{
			return null;
		}
		for (int a = cyclicBoundCheck.size(); a-- > 0;)
		{
			JavaClassInfo pendingClassInfo = cyclicBoundCheck.get(a);
			if (cyclicBoundCheck.get(a).getFqName().equals(fqTypeName))
			{
				return pendingClassInfo;
			}
		}
		IConversionContext context = this.context.getCurrent();
		Method currentMethod = context.getMethod();
		JavaClassInfo currentClassInfo = context.getClassInfo();
		TreePath classTreePath = currentClassInfo.getTreePath();
		TreePath keyTreePath = (currentMethod != null && currentMethod.getPath() != null) ? currentMethod.getPath() : classTreePath;
		AlreadyTriedKey alreadyTriedKey = new AlreadyTriedKey(keyTreePath, fqTypeName);
		JavaClassInfo scopedClassInfo = alreadyTriedAndFailedMap.get(alreadyTriedKey);
		if (scopedClassInfo != null)
		{
			return scopedClassInfo;
		}
		if (alreadyTriedAndFailedMap.containsKey(alreadyTriedKey))
		{
			return null;
		}
		Matcher extendsFromMatcher = extendsFromPattern.matcher(fqTypeName);
		if (extendsFromMatcher.matches())
		{
			String extendsFrom = extendsFromMatcher.group(1);
			JavaClassInfo extendsFromCI = resolveClassInfo(extendsFrom, tryOnly, cascadeSearch);
			if (extendsFromCI == null)
			{
				return null;
			}
			return classInfoResolved(fqTypeName, makeGenericClassInfoExtendsFrom(extendsFromCI, "?"));
		}
		String originalFqTypeName = fqTypeName;
		String[] parsedGenericType = astHelper.parseGenericType(fqTypeName);
		if (parsedGenericType.length == 2)
		{
			String nonGenericType = parsedGenericType[0];
			String genericTypeArguments = parsedGenericType[1];
			JavaClassInfo nonGenericClassInfo = resolveClassInfo(nonGenericType, true);
			if (nonGenericClassInfo == null)
			{
				return classInfoResolved(originalFqTypeName, nonGenericClassInfo);
			}
			if (nonGenericClassInfo.getFqName().endsWith(fqTypeName))
			{
				return nonGenericClassInfo;
			}
			return classInfoResolved(originalFqTypeName, makeGenericClassInfo(nonGenericClassInfo, genericTypeArguments));
		}
		if (currentMethod != null && currentMethod.getPath() != null)
		{
			TreePath methodTreePath = currentMethod.getPath();
			for (JCTypeParameter typeParameter : ((JCMethodDecl) methodTreePath.getLeaf()).typarams)
			{
				String typeName = typeParameter.type.tsym.name.toString();
				if (typeName.equals(fqTypeName))
				{
					List<JCExpression> bounds = typeParameter.bounds;
					if (bounds.size() == 0)
					{
						// extends from java.lang.Object
						JavaClassInfo boundClassInfo = resolveClassInfo(Object.class.getName());
						JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(boundClassInfo, typeName);
						return classInfoResolved(fqTypeName, classInfo);
					}
					if (bounds.size() != 1)
					{
						throw new IllegalStateException(typeParameter.toString());
					}
					Type boundType = bounds.get(0).type;
					return resolveBoundedClassInfo(boundType, typeName, fqTypeName);
				}
			}
		}
		JCClassDecl leaf = (JCClassDecl) classTreePath.getLeaf();
		if (leaf.getSimpleName().contentEquals(""))
		{
			JCTree extendsClause = leaf.getExtendsClause();
			if (extendsClause instanceof JCTypeApply)
			{
				for (Type type : ((JCTypeApply) extendsClause).type.allparams())
				{
					String typeName = type.tsym.name.toString();
					if (typeName.equals(fqTypeName))
					{
						Type bound = ((TypeVar) type).bound;
						JavaClassInfo boundClassInfo = resolveClassInfo(bound.toString());
						JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(boundClassInfo, typeName);
						return classInfoResolved(fqTypeName, classInfo);
					}
				}
			}
		}
		else
		{
			for (JCTypeParameter typeParameter : leaf.getTypeParameters())
			{
				String typeName = typeParameter.type.tsym.name.toString();
				if (!typeName.equals(fqTypeName))
				{
					continue;
				}
				List<JCExpression> bounds = typeParameter.bounds;
				if (bounds.size() == 0)
				{
					// extends from java.lang.Object
					JavaClassInfo boundClassInfo = resolveClassInfo(Object.class.getName());
					JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(boundClassInfo, typeName);
					return classInfoResolved(fqTypeName, classInfo);
				}
				Type boundType = bounds.get(0).type;
				return resolveBoundedClassInfo(boundType, typeName, fqTypeName);
			}
		}
		fqTypeName = NewClassExpressionHandler.getFqNameFromAnonymousName(fqTypeName);
		fqTypeName = getResolveGenericFqTypeName(fqTypeName);
		JavaClassInfo classInfo = fqNameToClassInfoMap.get(fqTypeName);
		if (classInfo != null)
		{
			return classInfoResolved(originalFqTypeName, classInfo);
		}
		boolean isSimpleName = (fqTypeName.indexOf('.') == -1);
		if (isSimpleName && cascadeSearch)
		{
			if (currentClassInfo != null)
			{
				// if it is not a variable symbol is can be a simpleName of a class in our current package or in our import scope
				classInfo = resolveClassInfo(currentClassInfo.getFqName() + "." + fqTypeName, true, false);
				if (classInfo != null)
				{
					return classInfo;
				}
				classInfo = resolveClassInfo(currentClassInfo.getPackageName() + "." + fqTypeName, true, false);
				if (classInfo != null)
				{
					return classInfo;
				}
			}
		}
		if (cascadeSearch)
		{
			String currentClassInfoFqName = currentClassInfo.getFqName();
			if (!fqTypeName.startsWith(currentClassInfoFqName))
			{
				String internClassFqTypeName = currentClassInfoFqName + "$" + fqTypeName;
				classInfo = resolveClassInfo(internClassFqTypeName, true, false);
				if (classInfo != null)
				{
					return classInfo;
				}
			}
			String javaLangFqTypeName = "java.lang." + fqTypeName;
			classInfo = resolveClassInfo(javaLangFqTypeName, true, false);
			if (classInfo != null)
			{
				return classInfoResolved(originalFqTypeName, classInfo);
			}
		}
		// TODO ClassLoader skip generation?
		// if (fqTypeName.equals(ClassLoader.class.getName()))
		// {
		// throw new SkipGenerationException();
		// }
		classInfo = classInfoFactory.createClassInfo(fqTypeName);
		if (classInfo == null && isSimpleName && cascadeSearch)
		{
			TreePath currTreePath = classTreePath;
			while (!(currTreePath.getLeaf() instanceof JCCompilationUnit))
			{
				currTreePath = currTreePath.getParentPath();
			}
			for (JCImport importItem : ((JCCompilationUnit) currTreePath.getLeaf()).getImports())
			{
				JCFieldAccess fa = (JCFieldAccess) importItem.getQualifiedIdentifier();
				String simpleNameOfImport = fa.getIdentifier().toString();
				if ("*".equals(simpleNameOfImport))
				{
					// try the basePackage with the simpleName
					classInfo = resolveClassInfo(fa.getExpression().toString() + "." + fqTypeName, true, false);
					if (classInfo != null)
					{
						return classInfoResolved(originalFqTypeName, classInfo);
					}
				}
				else if (fqTypeName.equals(simpleNameOfImport))
				{
					classInfo = resolveClassInfo(fa.toString(), true, false);
					if (classInfo != null)
					{
						return classInfoResolved(originalFqTypeName, classInfo);
					}
				}
			}
		}
		if (classInfo == null)
		{
			if (tryOnly)
			{
				return classInfoResolved(originalFqTypeName, null);
			}
			throw new TypeResolveException(fqTypeName);
		}
		if (!fqNameToClassInfoMap.putIfNotExists(fqTypeName, classInfo))
		{
			log.warn("Duplicate type registration: " + fqTypeName);
			classInfo = fqNameToClassInfoMap.get(fqTypeName);
		}
		fqNameToClassInfoMap.putIfNotExists(classInfo.getFqName(), classInfo); // may be duplicated intentionally
		return classInfoResolved(originalFqTypeName, classInfo);
	}

	protected JavaClassInfo resolveBoundedClassInfo(Type boundType, String typeName, String fqTypeName)
	{
		JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(resolveClassInfo(Object.class.getName()), typeName);

		IConversionContext context = this.context.getCurrent();
		cyclicBoundCheck.add(classInfo);
		try
		{
			JavaClassInfo boundClassInfo;
			Method oldMethod = context.getMethod();
			context.setMethodIntern(null);
			try
			{
				boundClassInfo = resolveClassInfo(boundType.toString());
			}
			finally
			{
				context.setMethodIntern(oldMethod);
			}
			makeGenericClassInfoExtendsFromPostProcess(boundClassInfo, classInfo);
			return classInfoResolved(fqTypeName, classInfo);
		}
		finally
		{
			cyclicBoundCheck.popLastElement();
		}
	}

	protected boolean isVariableName(String fqTypeName)
	{
		return !primitiveTypeNames.contains(fqTypeName) && variableName.matcher(fqTypeName).matches();
	}

	protected String replaceTypeInstances(String fqTypeName, IMap<String, String> templateToTypeInstanceMap)
	{
		if (fqTypeName == null)
		{
			return null;
		}
		return replaceTypeInstancesIntern(fqTypeName, templateToTypeInstanceMap).toString();
	}

	protected CharSequence replaceTypeInstancesIntern(String fqTypeName, IMap<String, String> templateToTypeInstanceMap)
	{
		String[] parsedFqTypeName = astHelper.parseGenericType(fqTypeName);
		if (parsedFqTypeName.length == 1)
		{
			String typeInstanceName = templateToTypeInstanceMap.get(parsedFqTypeName[0]);
			if (typeInstanceName != null)
			{
				return typeInstanceName;
			}
			return parsedFqTypeName[0];
		}
		String[] typeArgumentsOfFqTypeName = astHelper.splitTypeArgument(parsedFqTypeName[1]);
		CharSequence[] replacements = new CharSequence[typeArgumentsOfFqTypeName.length];
		for (int a = typeArgumentsOfFqTypeName.length; a-- > 0;)
		{
			replacements[a] = replaceTypeInstances(typeArgumentsOfFqTypeName[a], templateToTypeInstanceMap);
		}
		int arrayDimCount = 0;
		String replacedFqTypeName = parsedFqTypeName[0];
		for (int a = replacedFqTypeName.length() - 2; a > 0; a -= 2)
		{
			if (replacedFqTypeName.charAt(a) == '[')
			{
				arrayDimCount++;
			}
			else
			{
				break;
			}
		}
		if (arrayDimCount > 0)
		{
			replacedFqTypeName = replacedFqTypeName.substring(0, replacedFqTypeName.length() - arrayDimCount * 2);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(replacedFqTypeName);
		sb.append('<');
		boolean firstGenericArgument = true;
		for (CharSequence replacement : replacements)
		{
			if (firstGenericArgument)
			{
				firstGenericArgument = false;
			}
			else
			{
				sb.append(',');
			}
			sb.append(replacement);
		}
		sb.append('>');
		for (int a = arrayDimCount; a-- > 0;)
		{
			sb.append("[]");
		}
		return sb;
	}

	protected JavaClassInfo makeGenericClassInfoExtendsFrom(JavaClassInfo extendsFromClassInfo, String symbolName)
	{
		JavaClassInfo classInfo = new JavaClassInfo(extendsFromClassInfo.classInfoManager);
		classInfo.setName(symbolName);
		classInfo.setNonGenericName(astHelper.extractNonGenericType(classInfo.getName()));
		classInfo.setPackageName(null);
		classInfo.setNameOfSuperClass(null);

		makeGenericClassInfoExtendsFromPostProcess(extendsFromClassInfo, classInfo);
		return classInfo;
	}

	protected void makeGenericClassInfoExtendsFromPostProcess(JavaClassInfo extendsFromClassInfo, JavaClassInfo classInfo)
	{
		classInfo.setExtendsFrom(extendsFromClassInfo);

		classInfo.setPrivateFlag(extendsFromClassInfo.isPrivate());
		classInfo.setProtectedFlag(extendsFromClassInfo.isProtected());
		classInfo.setPublicFlag(extendsFromClassInfo.isPublic());
		classInfo.setFinalFlag(extendsFromClassInfo.isFinal());
	}

	protected JavaClassInfo makeGenericClassInfo(JavaClassInfo classInfo, String genericTypeArguments)
	{
		// JavaClassInfo superClassInfo = null;
		// String nameOfSuperClass = classInfo.getNameOfSuperClass();
		// if (nameOfSuperClass != null)
		// {
		// superClassInfo = resolveClassInfo(nameOfSuperClass);
		// }
		ClassTree classTree = classInfo.getClassTree();
		String[] typeArgumentsSplit = astHelper.splitTypeArgument(genericTypeArguments);
		String genericFqName;
		String nonGenericSimpleName;
		JavaClassInfo[] typeArgumentsOfClassInfo;
		int arrayDimCount = 0;
		if (classTree == null)
		{
			JavaClassInfo currClassInfo = classInfo;
			while (currClassInfo.isArray())
			{
				arrayDimCount++;
				currClassInfo = currClassInfo.getComponentType();
			}
			genericFqName = currClassInfo.getFqName();
			nonGenericSimpleName = currClassInfo.getNonGenericName();
			typeArgumentsOfClassInfo = classInfo.getTypeArguments();
		}
		else
		{
			// java.util.List<? extends TypeParameterTree> typeParameters = classTree.getTypeParameters();
			genericFqName = NewClassExpressionHandler.getFqName((JCClassDecl) classTree);
			nonGenericSimpleName = classInfo.getNonGenericName();
			typeArgumentsOfClassInfo = classInfo.getTypeArguments();
		}
		JavaClassInfo[] typeArgumentCIs = new JavaClassInfo[typeArgumentsSplit.length];
		StringBuilder sb = new StringBuilder();
		String[] genericTypeOfClassInfo = astHelper.parseGenericType(genericFqName);
		// JavaClassInfo[] typeArgumentsOfClassInfo = genericTypeOfClassInfo.length == 2 ? astHelper.splitTypeArgument(genericTypeOfClassInfo[1]) : null;
		HashMap<String, String> templateToTypeInstanceMap = new HashMap<String, String>();
		sb.append(nonGenericSimpleName);
		boolean first = true;
		// if (typeParameters.size() != typeArgumentsSplit.length)
		// {
		// throw new IllegalStateException("Type argument count does not match. Expected: " + typeParameters.size() + ", Given: " + typeArgumentsSplit.length
		// + " when creating a generic definition of " + classInfo.getFqName());
		// }
		for (int a = 0, length = typeArgumentsSplit.length; a < length; a++)
		{
			if (first)
			{
				sb.append('<');
				first = false;
			}
			else
			{
				sb.append(',');
			}
			String typeArgumentSplit = typeArgumentsSplit[a];
			typeArgumentSplit = MethodInvocationExpressionHandler.trimCaptureOfPattern.matcher(typeArgumentSplit).replaceAll("");

			JavaClassInfo typeArgumentCI = null;
			if ("?".equals(typeArgumentSplit))
			{
				typeArgumentCI = resolveClassInfo(Object.class.getName(), false, false);
			}
			if (typeArgumentCI == null)
			{
				Matcher extendsFromMatcher = extendsFromPattern.matcher(typeArgumentSplit);
				if (extendsFromMatcher.matches())
				{
					String extendsFromType = extendsFromMatcher.group(1);
					typeArgumentCI = resolveClassInfo(extendsFromType);
				}
			}
			if (typeArgumentCI == null)
			{
				typeArgumentCI = resolveClassInfo(typeArgumentSplit, true);
			}
			if (typeArgumentCI == null)
			{
				if (!typeArgumentSplit.contains("."))
				{
					sb.append(typeArgumentSplit);
					if (typeArgumentsOfClassInfo != null)
					{
						JavaClassInfo typeArgumentOfClassInfo = typeArgumentsOfClassInfo[a];
						if (!typeArgumentOfClassInfo.getFqName().equals(typeArgumentSplit))
						{
							templateToTypeInstanceMap.put(typeArgumentOfClassInfo.getFqName(), typeArgumentSplit);
						}
					}
					continue;
				}
			}
			if (typeArgumentCI == null)
			{
				throw new TypeResolveException(typeArgumentSplit);
			}
			typeArgumentCIs[a] = typeArgumentCI;
			sb.append(typeArgumentCI.getFqName());
			if (typeArgumentsOfClassInfo != null)
			{
				JavaClassInfo typeArgumentOfClassInfo = typeArgumentsOfClassInfo[a];
				if (!typeArgumentOfClassInfo.getFqName().equals(typeArgumentSplit))
				{
					templateToTypeInstanceMap.put(typeArgumentOfClassInfo.getFqName(), typeArgumentSplit);
				}
			}
		}
		if (!first)
		{
			sb.append('>');
		}
		for (int a = arrayDimCount; a-- > 0;)
		{
			sb.append("[]");
		}
		JavaClassInfo genericClassInfo = new JavaClassInfo(classInfo.classInfoManager);
		genericClassInfo.setName(sb.toString());
		genericClassInfo.setNonGenericName(astHelper.extractNonGenericType(genericClassInfo.getName()));
		genericClassInfo.setPackageName(classInfo.getPackageName());
		genericClassInfo.setTypeArguments(typeArgumentCIs);

		if (classInfo.isArray())
		{
			genericClassInfo.setComponentType(resolveClassInfo(classInfo.getComponentType().getNonGenericFqName() + "<" + genericTypeArguments + ">"));
		}
		genericClassInfo.setPrivateFlag(classInfo.isPrivate());
		genericClassInfo.setProtectedFlag(classInfo.isProtected());
		genericClassInfo.setPublicFlag(classInfo.isPublic());
		genericClassInfo.setFinalFlag(classInfo.isFinal());
		genericClassInfo.setNameOfSuperClass(replaceTypeInstances(classInfo.getNameOfSuperClass(), templateToTypeInstanceMap));

		for (String nameOfInterface : classInfo.getNameOfInterfaces())
		{
			genericClassInfo.addNameOfInterface(nameOfInterface);
		}
		for (Annotation annotation : classInfo.getAnnotations())
		{
			genericClassInfo.addAnnotation(annotation);
		}
		for (Field field : classInfo.getFields())
		{
			genericClassInfo.addField(makeGenericFieldInfo((FieldInfo) field, genericClassInfo, templateToTypeInstanceMap));
		}
		for (Method method : classInfo.getMethods())
		{
			genericClassInfo.addMethod(makeGenericMethodInfo((MethodInfo) method, genericClassInfo, templateToTypeInstanceMap));
		}
		return genericClassInfo;
	}

	protected Field makeGenericFieldInfo(FieldInfo fieldTemplate, JavaClassInfo classInstance, HashMap<String, String> templateToTypeInstanceMap)
	{
		FieldInfo field = new FieldInfo();
		field.setName(fieldTemplate.getName());
		field.setAbstractFlag(fieldTemplate.isAbstract());

		field.setFieldType(replaceTypeInstances(fieldTemplate.getFieldType(), templateToTypeInstanceMap));

		field.setFinalFlag(fieldTemplate.isFinal());
		field.setLocationInfo(fieldTemplate.getLocationInfo());
		field.setModuleType(fieldTemplate.getModuleType());
		field.setNativeFlag(fieldTemplate.isNative());
		field.setOwningClass(classInstance);
		field.setPrivateFlag(fieldTemplate.isPrivate());
		field.setProtectedFlag(fieldTemplate.isProtected());
		field.setPublicFlag(fieldTemplate.isPublic());
		field.setStaticFlag(fieldTemplate.isStatic());
		for (Annotation annotation : fieldTemplate.getAnnotations())
		{
			field.addAnnotation(annotation);
		}
		return field;
	}

	protected Method makeGenericMethodInfo(MethodInfo methodTemplate, JavaClassInfo classInstance, HashMap<String, String> templateToTypeInstanceMap)
	{
		MethodInfo method = new MethodInfo();
		method.setName(methodTemplate.getName());
		method.setAbstractFlag(methodTemplate.isAbstract());
		method.setFinalFlag(methodTemplate.isFinal());
		method.setLocationInfo(methodTemplate.getLocationInfo());
		method.setMethodTree(methodTemplate.getMethodTree());
		method.setModuleType(methodTemplate.getModuleType());
		method.setNativeFlag(methodTemplate.isNative());
		method.setOwningClass(classInstance);
		method.setPath(methodTemplate.getPath());
		method.setPrivateFlag(methodTemplate.isPrivate());
		method.setProtectedFlag(methodTemplate.isProtected());
		method.setPublicFlag(methodTemplate.isPublic());
		method.setReturnType(replaceTypeInstances(methodTemplate.getReturnType(), templateToTypeInstanceMap));
		method.setStaticFlag(methodTemplate.isStatic());
		for (Annotation annotation : methodTemplate.getAnnotations())
		{
			method.addAnnotation(annotation);
		}
		for (String exception : methodTemplate.getExceptions())
		{
			method.addException(exception);
		}
		for (VariableElement parameter : methodTemplate.getParameters())
		{
			String parameterTypeName = replaceTypeInstances(parameter.asType().toString(), templateToTypeInstanceMap);

			method.addParameters(new SynthVariableElement(parameter, parameterTypeName));
		}
		return method;
	}

	@Override
	public Object[] findPathToTree(Method method, Tree tree)
	{
		Object[] treePath = expressionToTreePathMap.get(tree);
		if (treePath != null)
		{
			return treePath;
		}
		ArrayList<Object> treePathList = new ArrayList<Object>();
		Tree body = method.getMethodTree().getBody();
		treePathList.add(body);
		if (findExpressionIntern(body, tree, treePathList))
		{
			treePath = treePathList.toArray();
			if (!expressionToTreePathMap.putIfNotExists(tree, treePath))
			{
				throw new IllegalStateException("Must never happen");
			}
			return treePath;
		}
		throw new IllegalStateException("Expression not found: " + tree + " on method '" + method.toString() + "'");
	}

	protected boolean findExpressionIntern(Tree currTree, Tree treeToFind, ArrayList<Object> treePath)
	{
		java.lang.reflect.Field[] fields = ReflectUtil.getDeclaredFieldsInHierarchy(currTree.getClass());
		for (int b = fields.length; b-- > 0;)
		{
			java.lang.reflect.Field field = fields[b];
			treePath.add(field.getName());
			try
			{
				if (List.class.isAssignableFrom(field.getType()))
				{
					List<?> childList = (List<?>) field.get(currTree);
					if (childList != null)
					{
						treePath.add(childList);
						for (int a = 0, size = childList.size(); a < size; a++)
						{
							Object item = childList.get(a);
							treePath.add(Integer.valueOf(a));
							if (handleItem(item, treeToFind, treePath))
							{
								return true;
							}
							treePath.popLastElement();
						}
						treePath.popLastElement();
					}
				}
				else if (Tree.class.isAssignableFrom(field.getType()) && handleItem(field.get(currTree), treeToFind, treePath))
				{
					return true;
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			treePath.popLastElement();
		}
		return false;
	}

	protected boolean handleItem(Object item, Tree treeToFind, ArrayList<Object> treePath)
	{
		if (!(item instanceof Tree))
		{
			return false;
		}
		Tree childTree = (Tree) item;
		treePath.add(childTree);
		if (item == treeToFind)
		{
			return true;
		}
		if (findExpressionIntern(childTree, treeToFind, treePath))
		{
			return true;
		}
		treePath.popLastElement();
		return false;
	}
}
