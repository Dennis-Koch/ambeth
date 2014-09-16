using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Util
{
    public class ReflectUtil
    {
        public static FieldInfo GetDeclaredField(Type type, String fieldName)
        {
            FieldInfo[] fields = type.GetFields(BindingFlags.DeclaredOnly | BindingFlags.Static | BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public);
            foreach (FieldInfo field in fields)
            {
                if (field.Name.Equals(fieldName))
                {
                    return field;
                }
            }
            return null;
        }

        public static FieldInfo[] GetDeclaredFieldInHierarchy(Type type, String fieldName)
	    {
            FieldInfo[] fields = GetDeclaredFieldsInHierarchy(type);
            List<FieldInfo> selectedField = new List<FieldInfo>(1);
            foreach (FieldInfo field in fields)
            {
                if (field.Name.Equals(fieldName))
                {
                    selectedField.Add(field);
                }
            }
            return selectedField.ToArray();
        }

        public static FieldInfo[] GetDeclaredFieldsInHierarchy(Type type)
	    {
            return type.GetFields(BindingFlags.FlattenHierarchy | BindingFlags.Static | BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public);
	    }

        public static MethodInfo[] GetMethods(Type type)
	    {
            return type.GetMethods(BindingFlags.FlattenHierarchy | BindingFlags.Static | BindingFlags.Instance | BindingFlags.Public);
        }

        public static MethodInfo[] GetDeclaredMethods(Type type)
        {
            return type.GetMethods(BindingFlags.FlattenHierarchy | BindingFlags.Static | BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public);
        }

        public static MethodInfo GetDeclaredMethod(bool tryOnly, Type type, Type returnType, String methodName, params Type[] parameters)
        {
            return GetDeclaredMethod(tryOnly, type, returnType != null ? NewType.GetType(returnType) : null, methodName, TypeUtil.GetClassesToTypes(parameters));
        }

        public static MethodInfo GetDeclaredMethod(bool tryOnly, Type type, NewType returnType, String methodName, NewType[] parameters)
        {
            foreach (MethodInfo method in GetDeclaredMethods(type))
            {
                if (!method.Name.Equals(methodName))
                {
                    continue;
                }
                if (returnType != null && !NewType.GetType(method.ReturnType).Equals(returnType))
                {
                    continue;
                }
                if (parameters == null)
                {
                    return method;
                }
                ParameterInfo[] currentParameters = method.GetParameters();
                if (currentParameters.Length != parameters.Length)
                {
                    continue;
                }
                bool sameParameters = true;
                for (int a = currentParameters.Length; a-- > 0; )
                {
                    if (parameters[a] != null && !NewType.GetType(currentParameters[a].ParameterType).Equals(parameters[a]))
                    {
                        sameParameters = false;
                        break;
                    }
                }
                if (sameParameters)
                {
                    return method;
                }
            }
            if (tryOnly)
            {
                return null;
            }
            String propertyName = methodName;
            if (methodName.ToLowerInvariant().StartsWith("set")
                || methodName.ToLowerInvariant().StartsWith("get"))
            {
                propertyName = StringBuilderUtil.UpperCaseFirst(methodName.Substring(3));
            }
            PropertyInfo propertyInfo;
            BindingFlags flags = BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Static | BindingFlags.FlattenHierarchy;
            if (returnType != null)
            {
                try
                {
                    propertyInfo = type.GetProperty(propertyName, flags, null, returnType.Type, Type.EmptyTypes, new ParameterModifier[0]);
                }
                catch (Exception)
                {
                    throw;
                }
            }
            else
            {
                propertyInfo = type.GetProperty(propertyName, flags);
            }
            if (propertyInfo != null)
            {
                if (methodName.ToLowerInvariant().StartsWith("set") && propertyInfo.GetSetMethod() != null)
                {
                    return propertyInfo.GetSetMethod();
                }
                else if (propertyInfo.GetGetMethod() != null)
                {
                    return propertyInfo.GetGetMethod();
                }
            }
            throw new ArgumentException("No matching method found: " + methodName);
        }

        public static ConstructorInfo[] GetDeclaredConstructors(Type type)
        {
            return type.GetConstructors();
        }

        public static ConstructorInfo GetDeclaredConstructor(bool tryOnly, Type type, NewType[] parameters)
        {
            foreach (ConstructorInfo method in GetDeclaredConstructors(type))
            {
                ParameterInfo[] currentParameters = method.GetParameters();
                if (currentParameters.Length != parameters.Length)
                {
                    continue;
                }
                bool sameParameters = true;
                for (int a = currentParameters.Length; a-- > 0; )
                {
                    if (!NewType.GetType(currentParameters[a].ParameterType).Equals(parameters[a]))
                    {
                        sameParameters = false;
                        break;
                    }
                }
                if (sameParameters)
                {
                    return method;
                }
            }
            if (tryOnly)
            {
                return null;
            }
            throw new ArgumentException("No matching constructor found");
        }

	    private ReflectUtil()
	    {
		    // Intended blank
	    }
    }
}
