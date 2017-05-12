using System;
using System.Net;
using De.Osthus.Ambeth.Config;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using System.Text.RegularExpressions;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Log
{
    public class LogTypesUtil
    {
        public static String PrintMethod(MethodInfo method, bool printShortStringNames)
        {
            StringBuilder sb = new StringBuilder();
            PrintMethod(method, printShortStringNames, sb);
            return sb.ToString();
        }

        public static void PrintMethod(MethodInfo method, bool printShortStringNames, StringBuilder sb)
        {
            if (printShortStringNames)
            {
                PrintType(method.ReturnType, printShortStringNames, sb);
                sb.Append(' ').Append(method.Name);
                sb.Append('(');
                PrintParameters(method.GetParameters(), printShortStringNames, sb);
                sb.Append(')');
                return;
            }
            sb.Append(method.ToString());
        }

        public static String PrintMethod(String methodName, ParameterInfo[] parameters, Type returnType, bool printShortStringNames)
        {
            StringBuilder sb = new StringBuilder();
            PrintMethod(methodName, parameters, returnType, printShortStringNames, sb);
            return sb.ToString();
        }

        public static void PrintMethod(String methodName, ParameterInfo[] parameters, Type returnType, bool printShortStringNames, StringBuilder sb)
        {
            if (returnType != null)
            {
                PrintType(returnType, printShortStringNames, sb);
                sb.Append(' ');
            }
            sb.Append(methodName);
            sb.Append('(');
            PrintParameters(parameters, printShortStringNames, sb);
            sb.Append(')');
        }

        public static String PrintMethod(String methodName, Type[] parameters, Type returnType, bool printShortStringNames)
        {
            StringBuilder sb = new StringBuilder();
            PrintMethod(methodName, parameters, returnType, printShortStringNames, sb);
            return sb.ToString();
        }

        public static void PrintMethod(String methodName, Type[] parameters, Type returnType, bool printShortStringNames, StringBuilder sb)
        {
            if (returnType != null)
            {
                PrintType(returnType, printShortStringNames, sb);
                sb.Append(' ');
            }
            sb.Append(methodName);
            sb.Append('(');
            PrintParameters(parameters, printShortStringNames, sb);
            sb.Append(')');
        }

        public static void PrintParameters(Type[] parameters, bool printShortStringNames, StringBuilder sb)
        {
            if (parameters != null)
            {
                for (int a = 0, size = parameters.Length; a < size; a++)
                {
                    if (a > 0)
                    {
                        sb.Append(',');
                    }
                    PrintType(parameters[a], printShortStringNames, sb);
                }
            }
        }

        public static void PrintParameters(ParameterInfo[] parameters, bool printShortStringNames, StringBuilder sb)
        {
            if (parameters != null)
            {
                for (int a = 0, size = parameters.Length; a < size; a++)
                {
                    if (a > 0)
                    {
                        sb.Append(',');
                    }
                    PrintType(parameters[a].ParameterType, printShortStringNames, sb);
                }
            }
        }

        public static String PrintType(Type type, bool printShortStringNames)
        {
            StringBuilder sb = new StringBuilder();
            PrintType(type, printShortStringNames, sb);
            return sb.ToString();
        }

        public static void PrintType(Type type, bool printShortStringNames, StringBuilder sb)
        {
            if (!printShortStringNames)
            {
                sb.Append(type.ToString());
                return;
            }
            int index = type.Name.IndexOf('`');
            if (index > 0)
            {
                sb.Append(type.Name, 0, index);
            }
            else
            {
                sb.Append(type.Name);
            }
            Type[] genericArguments = type.GetGenericArguments();
            if (genericArguments != null && genericArguments.Length > 0)
            {
                sb.Append('<');
                for (int a = 0, size = genericArguments.Length; a < size; a++)
                {
                    if (a > 0)
                    {
                        sb.Append(',');
                    }
                    PrintType(genericArguments[a], printShortStringNames, sb);
                }
                sb.Append('>');
            }
        }
    }
}
