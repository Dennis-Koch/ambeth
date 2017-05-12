using De.Osthus.Ambeth.Collections;
using System;
#if SILVERLIGHT
using System.Linq.Expressions;
#endif
using System.Reflection;
using System.Reflection.Emit;
using System.Security;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Typeinfo
{
    public delegate void MemberCallDelegate(Object obj);

    public delegate Object MemberGetDelegate(Object obj);

    public delegate void MemberSetDelegate(Object obj, Object value);

    public delegate Object MemberStaticGetDelegate();

    public delegate void MemberStaticSetDelegate(Object value);

    public class TypeUtility
    {
        public class GetDelegateKey
        {
            protected readonly Type type;

            protected readonly String memberName;

            public GetDelegateKey(Type type, String memberName)
            {
                this.type = type;
                this.memberName = memberName;
            }

            public override int GetHashCode()
            {
                return type.GetHashCode() ^ memberName.GetHashCode();
            }

            public override bool Equals(Object obj)
            {
                if (Object.ReferenceEquals(this, obj))
                {
                    return true;
                }
                if (!(obj is GetDelegateKey))
                {
                    return false;
                }
                GetDelegateKey other = (GetDelegateKey)obj;
                return Object.Equals(type, other.type) && Object.Equals(memberName, other.memberName);
            }
        }

        private static readonly SmartCopyMap<GetDelegateKey, MemberCallDelegate> _memberCallDelegates = new SmartCopyMap<GetDelegateKey, MemberCallDelegate>();
        
        private static readonly SmartCopyMap<GetDelegateKey, MemberGetDelegate> _memberGetDelegates = new SmartCopyMap<GetDelegateKey, MemberGetDelegate>();

        private static readonly SmartCopyMap<GetDelegateKey, MemberSetDelegate> _memberSetDelegates = new SmartCopyMap<GetDelegateKey, MemberSetDelegate>();

        public static MemberGetDelegate GetMemberGetDelegate(Type type, String memberName, bool tryOnly = false)
        {
            GetDelegateKey key = new GetDelegateKey(type, memberName);
            MemberGetDelegate value = _memberGetDelegates.Get(key);
            if (value != null)
            {
                return value;
            }
            MemberGetDelegate delegates = GetMemberGetDelegateIntern(type, memberName, tryOnly);
            if (delegates == null)
            {
                return null;
            }
            _memberGetDelegates.Put(key, delegates);
            return delegates;
        }

        public static MemberCallDelegate GetMemberCallDelegate(Type type, String memberName, bool tryOnly = false)
        {
            GetDelegateKey key = new GetDelegateKey(type, memberName);
            MemberCallDelegate value = _memberCallDelegates.Get(key);
            if (value != null)
            {
                return value;
            }
            MemberCallDelegate delegates = GetMemberCallDelegateIntern(type, memberName, tryOnly);
            if (delegates == null)
            {
                return null;
            }
            _memberCallDelegates.Put(key, delegates);
            return delegates;
        }

        public static MemberSetDelegate GetMemberSetDelegate(Type type, String memberName, bool tryOnly = false)
        {
            GetDelegateKey key = new GetDelegateKey(type, memberName);
            MemberSetDelegate value = _memberSetDelegates.Get(key);
            if (value != null)
            {
                return value;
            }
            MemberSetDelegate delegates = GetMemberSetDelegateIntern(type, memberName, tryOnly);
            if (delegates == null)
            {
                return null;
            }
            _memberSetDelegates.Put(key, delegates);
            return delegates;
        }

        [SecurityCritical]
        protected static DynamicMethod CreateCallMethodDM(Type objectType, MethodInfo callMI, String memberName)
        {
#if SILVERLIGHT
            DynamicMethod getDm = new DynamicMethod("Get" + memberName,
                        typeof(Object), new Type[] { typeof(Object) });// SL 4 only: , objectType);
#else
            DynamicMethod getDm = new DynamicMethod("Get" + memberName,
                        typeof(void), new Type[] { typeof(Object) }, objectType);
#endif
            ILGenerator getGen = getDm.GetILGenerator();
            // Load the instance of the object (argument 0) onto the stack
            getGen.Emit(OpCodes.Ldarg_0);
            // Load the value of the object's field (fi) onto the stack
            getGen.Emit(OpCodes.Callvirt, callMI);
            // return the value on the top of the stack
            getGen.Emit(OpCodes.Ret);
            return getDm;
        }

        [SecurityCritical]
        protected static DynamicMethod CreateGetMethodDM(Type objectType, MethodInfo getMI, String memberName)
        {
#if SILVERLIGHT
            DynamicMethod getDm = new DynamicMethod("Get" + memberName,
                        typeof(Object), new Type[] { typeof(Object) });// SL 4 only: , objectType);
#else
            DynamicMethod getDm = new DynamicMethod("Get" + memberName,
                        typeof(Object), new Type[] { typeof(Object) }, objectType);
#endif
            ILGenerator getGen = getDm.GetILGenerator();
            // Load the instance of the object (argument 0) onto the stack
            getGen.Emit(OpCodes.Ldarg_0);
            // Load the value of the object's field (fi) onto the stack
            getGen.Emit(OpCodes.Callvirt, getMI);
            if (getMI.ReturnType.IsValueType)
            {
                getGen.Emit(OpCodes.Box, getMI.ReturnType);
            }
            // return the value on the top of the stack
            getGen.Emit(OpCodes.Ret);
            return getDm;
        }

        protected static DynamicMethod CreateSetMethodDM(Type objectType, MethodInfo setMI, String memberName)
        {
#if SILVERLIGHT
            DynamicMethod setDm = new DynamicMethod("Set" + memberName,
                       typeof(void), new Type[] { typeof(Object), typeof(Object) });// SL 4 only: , objectType);
#else
            DynamicMethod setDm = new DynamicMethod("Set" + memberName,
                       typeof(void), new Type[] { typeof(Object), typeof(Object) }, objectType);
#endif
            ILGenerator setGen = setDm.GetILGenerator();
            // Load the instance of the object (argument 0) onto the stack
            setGen.Emit(OpCodes.Ldarg_0);
            setGen.Emit(OpCodes.Ldarg_1);
            Type propertyType = setMI.GetParameters()[0].ParameterType;
            if (propertyType.IsValueType)
            {
                setGen.Emit(OpCodes.Unbox_Any, propertyType);
            }
            // Load the value of the object's field (fi) onto the stack
            setGen.Emit(OpCodes.Callvirt, setMI);
            setGen.Emit(OpCodes.Ret);
            return setDm;
        }

        protected static MemberCallDelegate GetMemberCallDelegateIntern(Type objectType, String memberName, bool tryOnly)
        {
            PropertyInfo pi = objectType.GetProperty(memberName);
            if (pi != null)
            {
#if SILVERLIGHT
                if (!pi.CanRead)
                    throw new ArgumentException(string.Format("The property {0} in type {1} not have getter.",
                                                                pi.PropertyType, objectType));

                ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                MemberExpression propertyExp;
                if (objectType != typeof(Object))
                {
                    propertyExp = Expression.Property(Expression.Convert(objExp, objectType), pi);
                }
                else
                {
                    propertyExp = Expression.Property(objExp, pi);
                }
                return Expression.Lambda<MemberCallDelegate>(Expression.Convert(propertyExp, typeof(Object)), objExp).Compile();
#else
                // Member is a Property...
                MethodInfo getMi = pi.GetGetMethod();

                if (getMi != null)
                {
                    DynamicMethod getDm = CreateCallMethodDM(objectType, getMi, memberName);

                    return (MemberCallDelegate)getDm.CreateDelegate(typeof(MemberCallDelegate));
                }
                if (tryOnly)
                {
                    return null;
                }
                throw new Exception(String.Format(
                    "Property: '{0}' of Type: '{1}' does" +
                    " not have a Public Get accessor and a Public Set accessor",
                    memberName, objectType.Name));
#endif
            }
            FieldInfo fi = objectType.GetField(memberName);
            if (fi != null)
            {
                // Member is a Field...
#if SILVERLIGHT
                ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                MemberExpression propertyExp;
                if (objectType != typeof(Object))
                {
                    propertyExp = Expression.Field(Expression.Convert(objExp, objectType), fi);
                }
                else
                {
                    propertyExp = Expression.Field(objExp, fi);
                }
                return Expression.Lambda<MemberCallDelegate>(Expression.Convert(propertyExp, typeof(Object)), objExp).Compile();
#else
                DynamicMethod getDm = new DynamicMethod("Call" + memberName,
                    typeof(Object), new Type[] { typeof(Object) }, objectType);
                ILGenerator getGen = getDm.GetILGenerator();
                // Load the instance of the object (argument 0) onto the stack
                getGen.Emit(OpCodes.Ldarg_0);
                // Load the value of the object's field (fi) onto the stack
                getGen.Emit(OpCodes.Ldfld, fi);
                if (fi.FieldType.IsValueType)
                {
                    getGen.Emit(OpCodes.Box, fi.FieldType);
                }
                // return the value on the top of the stack
                getGen.Emit(OpCodes.Ret);

                return (MemberCallDelegate)getDm.CreateDelegate(typeof(MemberCallDelegate));
#endif
            }
            MethodInfo method = objectType.GetMethod(memberName);
            if (method != null)
            {
                if (method.GetParameters().Length == 0)
                {
#if SILVERLIGHT
                    ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                    Expression methodExp = Expression.Call(Expression.Convert(objExp, objectType), method);
                    return Expression.Lambda<MemberCallDelegate>(methodExp, objExp).Compile();
#else
                    DynamicMethod methodDm = CreateCallMethodDM(objectType, method, memberName);
                    return (MemberCallDelegate)methodDm.CreateDelegate(typeof(MemberCallDelegate));
#endif
                }
            }
            if (tryOnly)
            {
                return null;
            }
            throw new Exception(String.Format(
                "Member: '{0}' is not a Public Property or Field of Type: '{1}'",
                memberName, objectType.Name));
        }

        protected static MemberGetDelegate GetMemberGetDelegateIntern(Type objectType, String memberName, bool tryOnly)
        {
            PropertyInfo pi = objectType.GetProperty(memberName);
            if (pi != null)
            {
#if SILVERLIGHT
                if (!pi.CanRead)
                    throw new ArgumentException(string.Format("The property {0} in type {1} not have getter.",
                                                                pi.PropertyType, objectType));

                ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                MemberExpression propertyExp;
                if (objectType != typeof(Object))
                {
                    propertyExp = Expression.Property(Expression.Convert(objExp, objectType), pi);
                }
                else
                {
                    propertyExp = Expression.Property(objExp, pi);
                }
                return Expression.Lambda<MemberGetDelegate>(Expression.Convert(propertyExp, typeof(Object)), objExp).Compile();
#else
                // Member is a Property...
                MethodInfo getMi = pi.GetGetMethod();

                if (getMi != null)
                {
                    DynamicMethod getDm = CreateGetMethodDM(objectType, getMi, memberName);

                    return (MemberGetDelegate)getDm.CreateDelegate(typeof(MemberGetDelegate));
                }
                if (tryOnly)
                {
                    return null;
                }
                throw new Exception(String.Format(
                    "Property: '{0}' of Type: '{1}' does" +
                    " not have a Public Get accessor and a Public Set accessor",
                    memberName, objectType.Name));
#endif
            }
            FieldInfo fi = objectType.GetField(memberName);
            if (fi != null)
            {
                // Member is a Field...
#if SILVERLIGHT
                ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                MemberExpression propertyExp;
                if (objectType != typeof(Object))
                {
                    propertyExp = Expression.Field(Expression.Convert(objExp, objectType), fi);
                }
                else
                {
                    propertyExp = Expression.Field(objExp, fi);
                }
                return Expression.Lambda<MemberGetDelegate>(Expression.Convert(propertyExp, typeof(Object)), objExp).Compile();
#else
                DynamicMethod getDm = new DynamicMethod("Get" + memberName,
                    typeof(Object), new Type[] { typeof(Object) }, objectType);
                ILGenerator getGen = getDm.GetILGenerator();
                // Load the instance of the object (argument 0) onto the stack
                getGen.Emit(OpCodes.Ldarg_0);
                // Load the value of the object's field (fi) onto the stack
                getGen.Emit(OpCodes.Ldfld, fi);
                if (fi.FieldType.IsValueType)
                {
                    getGen.Emit(OpCodes.Box, fi.FieldType);
                }
                // return the value on the top of the stack
                getGen.Emit(OpCodes.Ret);

                return (MemberGetDelegate)getDm.CreateDelegate(typeof(MemberGetDelegate));
#endif
            }
            MethodInfo method = objectType.GetMethod(memberName, new Type[0]);
            if (method != null)
            {
                if (method.GetParameters().Length == 0)
                {
#if SILVERLIGHT
                    if (method.IsStatic)
                    {
                        Expression methodExp = Expression.Call(method);
                        MemberStaticGetDelegate msGetDelegate = Expression.Lambda<MemberStaticGetDelegate>(Expression.Convert(methodExp, typeof(Object))).Compile();
                        return new MemberGetDelegate(delegate(Object obj)
                            {
                                return msGetDelegate();
                            });
                    }
                    else
                    {
                        ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                        Expression methodExp = Expression.Call(Expression.Convert(objExp, objectType), method);
                        return Expression.Lambda<MemberGetDelegate>(Expression.Convert(methodExp, typeof(Object)), objExp).Compile();
                    }
#else
                    DynamicMethod methodDm = CreateGetMethodDM(objectType, method, memberName);
                    return (MemberGetDelegate)methodDm.CreateDelegate(typeof(MemberGetDelegate));
#endif
                }
            }
            if (tryOnly)
            {
                return null;
            }
            throw new Exception(String.Format(
                "Member: '{0}' is not a Public Property or Field of Type: '{1}'",
                memberName, objectType.Name));
        }

        protected static MemberSetDelegate GetMemberSetDelegateIntern(Type objectType, String memberName, bool tryOnly)
        {
            PropertyInfo pi = objectType.GetProperty(memberName);
            if (pi != null)
            {
#if SILVERLIGHT
                if (!pi.CanWrite)
                    throw new ArgumentException(string.Format("The property {0} in type {1} not have setter.",
                                                                pi.PropertyType, objectType));

                ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                ParameterExpression valueExp = Expression.Parameter(typeof(Object), "value");
                UnaryExpression convertedValue = Expression.Convert(valueExp, pi.PropertyType);

                Expression bodyExpression;
                if (objectType != typeof(Object))
                {
                    bodyExpression = Expression.Assign(Expression.Property(Expression.Convert(objExp, objectType), pi), convertedValue);
                }
                else
                {
                    bodyExpression = Expression.Assign(Expression.Property(objExp, pi), convertedValue);
                }
                return Expression.Lambda<MemberSetDelegate>(bodyExpression, objExp, valueExp).Compile();
#else
                // Member is a Property...
                MethodInfo setMi = pi.GetSetMethod();

                if (setMi != null)
                {
                    DynamicMethod setDm = CreateSetMethodDM(objectType, setMi, memberName);

                    return (MemberSetDelegate)setDm.CreateDelegate(typeof(MemberSetDelegate));
                }
                if (tryOnly)
                {
                    return null;
                }
                throw new Exception(String.Format(
                    "Property: '{0}' of Type: '{1}' does" +
                    " not have a Public Set accessor",
                    memberName, objectType.Name));
#endif
            }
            FieldInfo fi = objectType.GetField(memberName);
            if (fi != null)
            {
                // Member is a Field...
#if SILVERLIGHT
                ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                ParameterExpression valueExp = Expression.Parameter(typeof(Object), "value");
                UnaryExpression convertedValue = Expression.Convert(valueExp, fi.FieldType);

                Expression bodyExpression;
                if (objectType != typeof(Object))
                {
                    bodyExpression = Expression.Assign(Expression.Field(Expression.Convert(objExp, objectType), fi), convertedValue);
                }
                else
                {
                    bodyExpression = Expression.Assign(Expression.Field(objExp, fi), convertedValue);
                }
                return Expression.Lambda<MemberSetDelegate>(bodyExpression, objExp, valueExp).Compile();
#else
                DynamicMethod setDm = new DynamicMethod("Set" + memberName,
                    typeof(void), new Type[] { typeof(Object), typeof(Object) }, objectType);
                ILGenerator setGen = setDm.GetILGenerator();
                // Load the instance of the object (argument 0) onto the stack
                setGen.Emit(OpCodes.Ldarg_0);
                setGen.Emit(OpCodes.Ldarg_1);
                if (fi.FieldType.IsValueType)
                {
                    setGen.Emit(OpCodes.Unbox_Any, fi.FieldType);
                }
                setGen.Emit(OpCodes.Stfld, fi);
                // return the value on the top of the stack
                setGen.Emit(OpCodes.Ret);

                return (MemberSetDelegate)setDm.CreateDelegate(typeof(MemberSetDelegate));
#endif
            }
            MethodInfo method;
            try
            {
                method = objectType.GetMethod(memberName);
            }
            catch (Exception)
            {
                throw;
            }
            if (method != null)
            {
                if (method.GetParameters().Length == 1)
                {
#if SILVERLIGHT
                    ParameterInfo[] paramsInfo = method.GetParameters();
                    ParameterExpression objExp = Expression.Parameter(typeof(Object), "obj");
                    ParameterExpression valueExp = Expression.Parameter(typeof(Object), "value");
                    //ParameterExpression argsExp = Expression.Parameter(typeof(Object[]), "args");
                    Expression[] paramsExp = new Expression[paramsInfo.Length];

                    //IList<ParameterExpression> localVariables = new List<ParameterExpression>();
                    //ParameterExpression result = Expression.Variable(typeof(Object));
                    //localVariables.Add(result);

                    //pick each arg from the params array 
                    //and create a typed expression of them
                    for (int i = 0; i < paramsInfo.Length; i++)
                    {
                        Expression index = Expression.Constant(i);
                        Type pType = paramsInfo[i].ParameterType;

                        //Expression paramAccessorExp = Expression.ArrayIndex(argsExp, index);
                        Expression paramCastExp = Expression.Convert(valueExp, pType);
                        paramsExp[i] = paramCastExp;
                    }
                    if (method.IsStatic)
                    {
                        Expression methodExp = Expression.Call(method, paramsExp[0]);
                        MemberStaticSetDelegate msSetDelegate = Expression.Lambda<MemberStaticSetDelegate>(methodExp, valueExp).Compile();
                        return new MemberSetDelegate(delegate(Object obj, Object value)
                            {
                                msSetDelegate(value);
                            });
                    }
                    else
                    {
                        Expression methodExp = Expression.Call(Expression.Convert(objExp, objectType), method, paramsExp[0]);

                        //bool isVoid = method.ReturnType == typeof(void);
                        //List<Expression> body = new List<Expression>();
                        //if (isVoid)
                        //{
                        //    body.Add(Expression.Assign(result, Expression.Constant(null)));
                        //}
                        //else if (methodExp is MethodCallExpression)
                        //{
                        //    methodExp = Expression.Assign(result, Expression.Convert(methodExp, typeof(Object)));
                        //}
                        //body.Add(methodExp);
                        //body.Add(result);
                        //methodExp = Expression.Block(localVariables, body);

                        return Expression.Lambda<MemberSetDelegate>(methodExp, objExp, valueExp).Compile();
                    }
#else
                    DynamicMethod methodDm = CreateSetMethodDM(objectType, method, memberName);
                    return (MemberSetDelegate) methodDm.CreateDelegate(typeof(MemberSetDelegate));
#endif
                }
            }
            if (tryOnly)
            {
                return null;
            }
            throw new Exception(String.Format(
                "Member: '{0}' is not a Public Property or Field of Type: '{1}'",
                memberName, objectType.Name));
        }
    }
}