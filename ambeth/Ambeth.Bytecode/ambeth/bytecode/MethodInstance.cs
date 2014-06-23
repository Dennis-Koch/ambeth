using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode
{
    public class MethodInstance
    {
        public static MethodInstance FindByTemplate(MethodInfo methodTemplate)
        {
            return FindByTemplate(methodTemplate, false);
        }

        public static MethodInstance FindByTemplate(MethodInfo methodTemplate, bool tryOnly)
        {
            if (methodTemplate == null && tryOnly)
            {
                return null;
            }
            return FindByTemplate(new MethodInstance(methodTemplate), tryOnly);
        }

        public static MethodInstance FindByTemplate(MethodInstance methodTemplate, bool tryOnly)
        {
            if (methodTemplate == null && tryOnly)
            {
                return null;
            }
            return FindByTemplate(tryOnly, methodTemplate.ReturnType, methodTemplate.Name, methodTemplate.Parameters);
        }

        public static MethodInstance FindByTemplate(bool tryOnly, Type returnType, String methodName, params Type[] parameters)
	    {
		    return FindByTemplate(tryOnly, NewType.GetType(returnType), methodName, TypeUtil.GetClassesToTypes(parameters));
	    }

        public static MethodInstance FindByTemplate(bool tryOnly, NewType returnType, String methodName, params NewType[] parameters)
        {
            IBytecodeBehaviorState state = BytecodeBehaviorState.State;
            foreach (MethodInstance methodOnNewType in state.GetAlreadyImplementedMethodsOnNewType())
            {
                if (!methodOnNewType.Name.Equals(methodName))
                {
                    continue;
                }
                NewType[] paramsOnNewType = methodOnNewType.Parameters;
                if (paramsOnNewType.Length != parameters.Length)
                {
                    continue;
                }
                bool paramsEqual = true;
                for (int a = paramsOnNewType.Length; a-- > 0; )
                {
                    if (!paramsOnNewType[a].Equals(parameters[a]))
                    {
                        paramsEqual = false;
                        break;
                    }
                }
                if (paramsEqual)
                {
                    return methodOnNewType;
                }
                if (returnType == null || methodOnNewType.ReturnType.Equals(returnType))
                {
                    return methodOnNewType;
                }
            }
            Type currType = state.CurrentType;
            if (!currType.IsInterface)
            {
                while (currType != null && currType != typeof(Object))
                {
                    MethodInfo method = ReflectUtil.GetDeclaredMethod(true, currType, returnType, methodName, parameters);
                    if (method != null)
                    {
                        if (method.Attributes.HasFlag(MethodAttributes.Abstract))
                        {
                            // Method found but it is abstract. So it is not a callable instance
                            break;
                        }
                        return new MethodInstance(method);
                    }
                    currType = currType.BaseType;
                }
            }
            if (tryOnly)
            {
                return null;
            }
            throw new Exception("No method found on class hierarchy: " + methodName + ". Start type: " + state.NewType);
        }

        protected static NewType GetReturnType(MethodBase method)
        {
            return method is MethodInfo ? NewType.GetType(((MethodInfo)method).ReturnType) : NewType.VOID_TYPE;
        }

        protected static MethodAttributes CheckAbstract(NewType owner, MethodAttributes access)
        {
            if (BytecodeBehaviorState.State.NewType.Equals(owner))
            {
                access &= ~MethodAttributes.Abstract;
            }
            return access;
        }

        protected static MethodAttributes CheckPublic(MethodBase method)
        {
            MethodAttributes access = method.Attributes;
            if (method.IsPublic)
            {
                access = access | MethodAttributes.Public;
            }
            else if (method.IsFamily)
            {
                access |= MethodAttributes.Family;
            }
            else
            {
                access |= MethodAttributes.Private;
            }
            return access; 
        }

        protected MethodBase method;

        protected readonly NewType owner;

        protected readonly String name;

        protected readonly NewType returnType;

        protected readonly NewType[] parameters;

        protected readonly MethodAttributes access;

        public MethodInstance(NewType owner, Type declaringTypeOfMethod, Type returnType, String methodName, params Type[] parameters)
            : this(owner != null ? owner : NewType.GetType(declaringTypeOfMethod), ReflectUtil.GetDeclaredMethod(false, declaringTypeOfMethod, returnType, methodName,
                parameters))
        {
            // Intended blank
        }

        public MethodInstance(MethodBase method)
            : this(NewType.GetType(method.DeclaringType), method)
        {
            // Intended blank
        }

        public MethodInstance(NewType owner, MethodBase method)
            : this(owner, CheckPublic(method), GetReturnType(method), method.Name, TypeUtil.GetClassesToTypes(method.GetParameters()))
        {
            this.method = method;
        }
        
        public MethodInstance(NewType owner, MethodBase method, params NewType[] parameters)
            : this(owner, CheckPublic(method), GetReturnType(method), method.Name, parameters)
        {
            this.method = method;
        }

        public MethodInstance(Type owner, MethodAttributes access, Type returnType, String name, params Type[] parameters)
            : this(owner != null ? NewType.GetType(owner) : null, access, NewType.GetType(returnType), name, TypeUtil.GetClassesToTypes(parameters))
        {
            // Intended blank
        }

        //public MethodInstance(NewType owner, int access, String name, String signature, String desc) : this(owner, access, new Method(name, desc), signature)
        //{
        //    // Intended blank
        //}

        //public MethodInstance(NewType owner, int access, Method method, String signature)
        //{
        //    this.owner = owner;
        //    this.access = access;
        //    this.method = method;
        //    this.signature = signature;
        //}

        public MethodInstance(NewType owner, MethodAttributes access, NewType returnType, String name, params NewType[] parameters)
        {
            this.owner = owner;
            this.access = access;
            this.name = name;
            this.returnType = returnType;
            this.parameters = parameters;
        }

        public MethodInstance DeriveOwner()
        {
            return new MethodInstance(BytecodeBehaviorState.State.NewType, CheckAbstract(BytecodeBehaviorState.State.NewType, Access), ReturnType, Name, Parameters);
        }

        public MethodInstance DeriveName(String methodName)
        {
            return new MethodInstance(Owner, Access, ReturnType, methodName, Parameters);
        }

        public MethodBase Method
        {
            get
            {
                return method;
            }
        }

        public NewType Owner
        {
            get
            {
                return owner;
            }
        }

        public MethodAttributes Access
        {
            get
            {
                return access;
            }
        }

        public NewType ReturnType
        {
            get
            {
                return returnType;
            }
        }

        public NewType[] Parameters
        {
            get
            {
                return parameters;
            }
        }

        public String Name
        {
            get
            {
                return name;
            }
        }

        public bool EqualsSignature(MethodInstance method)
        {
            return Name.Equals(method.Name) && Arrays.Equals(Parameters, method.Parameters);
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            if (access.HasFlag(MethodAttributes.Public))
            {
                sb.Append("public ");
            }
            else if (access.HasFlag(MethodAttributes.Family))
            {
                sb.Append("protected ");
            }
            else if (access.HasFlag(MethodAttributes.Private))
            {
                sb.Append("private ");
            }
            if (access.HasFlag(MethodAttributes.Static))
            {
                sb.Append("static ");
            }
            if (access.HasFlag(MethodAttributes.Final))
            {
                sb.Append("final ");
            }
            sb.Append(returnType.ClassName).Append(' ');
            if (owner != null)
            {
                sb.Append(owner.ClassName).Append('.');
            }
            sb.Append(name).Append('(');
            NewType[] parameters = this.parameters;
            for (int a = 0, size = parameters.Length; a < size; a++)
            {
                if (a > 0)
                {
                    sb.Append(',');
                }
                sb.Append(parameters[a].ClassName);
            }
            sb.Append(')');
            return sb.ToString();
        }
    }
}