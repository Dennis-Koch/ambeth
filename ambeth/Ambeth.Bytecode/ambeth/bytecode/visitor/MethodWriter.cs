using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;
using De.Osthus.Ambeth.Visitor;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class MethodWriter : IMethodVisitor
    {
        public static readonly MethodInstance m_getClass = new MethodInstance(ReflectUtil.GetDeclaredMethod(false, typeof(Object), typeof(Type), "GetType"));

        public static readonly MethodInstance m_isAssignableFrom = new MethodInstance(ReflectUtil.GetDeclaredMethod(false, typeof(Type), typeof(bool), "IsAssignableFrom",
            typeof(Type)));

        public static readonly MethodInfo getTypeFromHandleMI = typeof(Type).GetMethod("GetTypeFromHandle", new Type[] { typeof(RuntimeTypeHandle) });

        public static readonly MethodInfo getMethodFromHandleMI = typeof(MethodBase).GetMethod(
                    "GetMethodFromHandle", new Type[] { typeof(RuntimeMethodHandle) });

        private readonly IILGenerator gen;

        private readonly MethodInstance method;

        private readonly List<Type> indexToTypeList = new List<Type>();

        public MethodWriter(MethodBuilder mb, MethodInstance method, StringBuilder sb)
        {
            this.gen = new TraceILGenerator(mb.GetILGenerator(), sb);
            this.method = method;
        }

        public MethodWriter(ConstructorBuilder cb, MethodInstance method, StringBuilder sb)
        {
            this.gen = new TraceILGenerator(cb.GetILGenerator(), sb);
            this.method = method;
        }

        protected IBytecodeBehaviorState State
        {
            get
            {
                return BytecodeBehaviorState.State;
            }
        }

        public IILGenerator Gen
        {
            get
            {
                return gen;
            }
        }

        public MethodInstance Method
        {
            get
            {
                return method;
            }
        }

        public virtual void ArrayLoad(Type componentType)
        {
            gen.Emit(OpCodes.Ldelem, componentType);
        }

        public virtual void ArrayStore(Type componentType)
        {
            gen.Emit(OpCodes.Stelem, componentType);
        }

        public virtual void Box(Type unboxedType)
        {
            gen.Emit(OpCodes.Box, unboxedType);
        }

        public virtual void CallThisGetter(MethodInstance method)
        {
            ParamChecker.AssertParamNotNull(method, "method");
            MethodInstance existingMethodInstance = MethodInstance.FindByTemplate(method, false);
            if (!existingMethodInstance.Owner.Equals(method.Owner))
            {
                CallThisGetter(existingMethodInstance);
                return;
            }
            if (!method.Access.HasFlag(MethodAttributes.Static))
            {
                LoadThis();
                InvokeVirtual(method);
            }
            else
            {
                InvokeStatic(method);
            }
        }

        public virtual void CallThisGetter(PropertyInstance property)
        {
            CallThisGetter(property.Getter);
        }

        public virtual void CallThisSetter(MethodInstance method, Script script)
        {
            ParamChecker.AssertParamNotNull(method, "method");
            ParamChecker.AssertParamNotNull(script, "script");
            if (!method.Access.HasFlag(MethodAttributes.Static))
            {
                LoadThis();
                script.Invoke(this);
                InvokeVirtual(method);
            }
            else
            {
                script.Invoke(this);
                InvokeStatic(method);
            }
        }

        public virtual void CallThisSetter(PropertyInstance property, Script script)
        {
            CallThisSetter(property.Setter, script);
        }

        public virtual void CheckCast(Type castedType)
        {
            gen.Emit(OpCodes.Castclass, castedType);
        }

        public virtual void CheckCast(NewType castedType)
        {
            gen.Emit(OpCodes.Castclass, castedType.Type);
        }

        public virtual void Dup()
        {
            gen.Emit(OpCodes.Dup);
        }

        public virtual void EndMethod()
        {
            // Intended blank
        }

        public virtual void GoTo(Label label)
        {
            gen.Emit(OpCodes.Br, label);
        }

        public virtual void InvokeConstructor(ConstructorInfo constructor)
        {
            gen.Emit(OpCodes.Newobj, constructor);
        }

        public virtual void InvokeConstructor(ConstructorInstance constructor)
        {
            InvokeSuper(constructor);
        }

        public virtual void InvokeSuper(MethodInstance method)
        {
            NewType newType = BytecodeBehaviorState.State.NewType;
            if (newType.Equals(method.Owner))
            {
                // Given method is not a super method. We look in the existing class hierarchy for a method with the same signature
                if (ConstructorInstance.CONSTRUCTOR_NAME.Equals(method.Name))
                {
                    ConstructorInfo c_method = ReflectUtil.GetDeclaredConstructor(true, BytecodeBehaviorState.State.CurrentType, method.Parameters);

                    if (c_method == null)
                    {
                        throw new ArgumentException("Constructor has no super implementation: " + method);
                    }
                    method = new ConstructorInstance(c_method);
                }
                else
                {
                    MethodInfo r_method = ReflectUtil.GetDeclaredMethod(true, BytecodeBehaviorState.State.CurrentType, method.ReturnType, method.Name,
                            method.Parameters);
                    if (r_method == null)
                    {
                        throw new ArgumentException("Method has no super implementation: " + method);
                    }
                    method = new MethodInstance(r_method);
                }
            }
            InvokeOnExactOwner(method);
        }

        public virtual void InvokeSuperOfCurrentMethod()
        {
            InvokeSuper(Method);
        }

        public virtual void InvokeOnExactOwner(MethodInfo method)
        {
            if (method.IsStatic)
            {
                throw new ArgumentException("Given method is not virtual: " + method);
            }
            InvokeSuper(new MethodInstance(method));
        }

        public virtual void InvokeOnExactOwner(MethodInstance method)
        {
            if (method.Method == null)
            {
                method = MethodInstance.FindByTemplate(method, false);
            }
            if (method is ConstructorInstance)
            {
                gen.Emit(OpCodes.Call, (ConstructorInfo)method.Method);
            }
            else
            {
                gen.Emit(OpCodes.Call, (MethodInfo)method.Method);
            }
        }

        public virtual void InvokeVirtual(MethodInstance method)
        {
            if (method.Access.HasFlag(MethodAttributes.Static))
            {
                throw new ArgumentException("Given method is not virtual: " + method);
            }
            NewType owner = method.Owner;
            if (owner == null)
            {
                owner = BytecodeBehaviorState.State.NewType;
            }
            if (method is ConstructorInstance)
            {
                gen.Emit(OpCodes.Callvirt, (ConstructorInfo)method.Method);
            }
            else
            {
                gen.Emit(OpCodes.Callvirt, (MethodInfo)method.Method);
            }
        }

        public virtual void InvokeInterface(MethodInstance method)
        {
            if (method.Access.HasFlag(MethodAttributes.Static))
            {
                throw new ArgumentException("Given method is not virtual: " + method);
            }
            NewType owner = method.Owner;
            if (owner == null)
            {
                InvokeVirtual(method.DeriveOwner());
            }
            else
            {
                InvokeVirtual(method);
            }
        }

        public virtual void InvokeSuperOf(MethodInfo method)
        {
            IBytecodeBehaviorState state = BytecodeBehaviorState.State;
            MethodInfo superMethod = ReflectUtil.GetDeclaredMethod(false, state.CurrentType, NewType.GetType(method.ReturnType), method.Name, TypeUtil.GetClassesToTypes(method.GetParameters()));
            InvokeSuper(new MethodInstance(superMethod));
        }

        public virtual void InvokeStatic(MethodInfo method)
        {
            if (!method.IsStatic)
            {
                throw new ArgumentException("Given method is not static: " + method);
            }
            InvokeStatic(new MethodInstance(method));
        }

        public virtual void InvokeStatic(MethodInstance method)
        {
            if (!method.Access.HasFlag(MethodAttributes.Static))
            {
                throw new ArgumentException("Given method is not static: " + method);
            }
            InvokeOnExactOwner(method);
        }

        public virtual void GetField(FieldInstance field)
        {
            ParamChecker.AssertParamNotNull(field, "field");
            if (!field.Access.HasFlag(FieldAttributes.Static))
            {
                gen.Emit(OpCodes.Ldfld, field.Field);
            }
            else
            {
                GetStatic(field);
            }

        }

        public virtual void GetThisField(FieldInstance field)
        {
            if (!field.Access.HasFlag(FieldAttributes.Static))
            {
                LoadThis();
                GetField(field);
            }
            else
            {
                GetStatic(field);
            }
        }

        public virtual void GetStatic(FieldInstance field)
        {
            gen.Emit(OpCodes.Ldsfld, field.Field);
        }

        public virtual void IfCmp(Type type, CompareOperator compareOperator, Label label)
        {
            switch (compareOperator)
            {
                case CompareOperator.EQ:
                    gen.Emit(OpCodes.Beq, label);
                    break;
                case CompareOperator.NE:
                    gen.Emit(OpCodes.Bne_Un, label);
                    break;
                default:
                    throw RuntimeExceptionUtil.CreateEnumNotSupportedException(compareOperator);
            }
        }

        public virtual void IfNonNull(Label label)
        {
            gen.Emit(OpCodes.Brtrue, label);
        }

        public virtual void IfNull(Label label)
        {
            gen.Emit(OpCodes.Brfalse, label);
        }

        public virtual void IfThisInstanceOf(Type instanceOfType, Script loadValue, Script executeIfTrue, Script executeIfFalse)
        {
            if (executeIfTrue == null && executeIfFalse == null)
            {
                // nothing to do
                return;
            }
            Push(instanceOfType);
            loadValue(this);
            InvokeVirtual(m_getClass);
            InvokeVirtual(m_isAssignableFrom);

            if (executeIfTrue != null)
            {
                if (executeIfFalse != null)
                {
                    Label l_isFalse = NewLabel();
                    Label l_finish = NewLabel();
                    IfZCmp(CompareOperator.EQ, l_isFalse);
                    executeIfTrue(this);
                    GoTo(l_finish);
                    Mark(l_isFalse);
                    executeIfFalse(this);
                    Mark(l_finish);
                }
                else
                {
                    Label l_isFalse = NewLabel();
                    IfZCmp(CompareOperator.EQ, l_isFalse);
                    executeIfTrue(this);
                    Mark(l_isFalse);
                }
                return;
            }
            Label l_isTrue = NewLabel();
            IfZCmp(CompareOperator.NE, l_isTrue);
            executeIfFalse(this);
            Mark(l_isTrue);
        }

        public virtual void IfZCmp(NewType type, CompareOperator compareOperator, Label label)
        {
            IfZCmp(type.Type, compareOperator, label);
        }

        public virtual void IfZCmp(Type type, CompareOperator compareOperator, Label label)
        {
            if (typeof(double).Equals(type) || typeof(float).Equals(type) || typeof(long).Equals(type))
            {
                PushNullOrZero(type);
                IfCmp(type, compareOperator, label);
            }
            else
            {
                IfZCmp(compareOperator, label);
            }
        }

        public virtual void IfZCmp(CompareOperator compareOperator, Label label)
        {
            switch (compareOperator)
            {
                case CompareOperator.EQ:
                    gen.Emit(OpCodes.Brfalse, label);
                    break;
                case CompareOperator.NE:
                    gen.Emit(OpCodes.Brtrue, label);
                    break;
                default:
                    throw RuntimeExceptionUtil.CreateEnumNotSupportedException(compareOperator);
            }
        }

        public virtual void LoadArgs()
        {
            NewType[] parameters = Method.Parameters;
            for (int a = 0, size = parameters.Length; a < size; a++)
            {
                LoadArg(a);
            }
        }

        public virtual void LoadArg(int parameterIndex)
        {
            gen.Emit(OpCodes.Ldarg, parameterIndex + 1);
        }

        public void LoadLocal(LocalVariableInfo localIndex)
        {
            if (!localIndex.LocalType.IsPrimitive && localIndex.LocalType.IsValueType)
            {
                gen.Emit(OpCodes.Ldloca, localIndex.LocalIndex);
                return;
            }
            switch (localIndex.LocalIndex)
            {
                case 0:
                    gen.Emit(OpCodes.Ldloc_0);
                    break;
                case 1:
                    gen.Emit(OpCodes.Ldloc_1);
                    break;
                case 2:
                    gen.Emit(OpCodes.Ldloc_2);
                    break;
                case 3:
                    gen.Emit(OpCodes.Ldloc_3);
                    break;
                default:
                    gen.Emit(OpCodes.Ldloc, (LocalBuilder)localIndex);
                    break;
            }
        }

        public virtual void LoadThis()
        {
            gen.Emit(OpCodes.Ldarg_0);
        }

        public virtual void Mark(Label label)
        {
            gen.MarkLabel(label);
        }

        public virtual void NewArray(Type componentType)
        {
            gen.Emit(OpCodes.Newarr, componentType);
        }

        public virtual void NewInstance(ConstructorInstance constructor, Script argumentsScript)
        {
            gen.Emit(OpCodes.Newobj, constructor.Owner.Type);
            Dup();
            if (argumentsScript != null)
            {
                argumentsScript.Invoke(this);
            }
            InvokeConstructor(constructor);
        }

        public virtual Label NewLabel()
        {
            return gen.DefineLabel();
        }

        public virtual LocalVariableInfo NewLocal<T>()
        {
            return NewLocal(typeof(T));
        }

        public virtual LocalVariableInfo NewLocal(NewType localVariableType)
        {
            LocalBuilder lb = gen.DeclareLocal(localVariableType.Type);
            while (indexToTypeList.Count <= lb.LocalIndex)
            {
                indexToTypeList.Add(null);
            }
            indexToTypeList[lb.LocalIndex] = lb.LocalType;
            return lb;
        }

        public virtual LocalVariableInfo NewLocal(Type localVariableType)
        {
            if (localVariableType.IsEnum)
            {
                // in SL it is not allowed to create such a local variable at runtime
                // in .NET they are automatically treated as Int32 fields anyway
                localVariableType = typeof(int);
            }
            LocalBuilder lb = gen.DeclareLocal(localVariableType);
            while (indexToTypeList.Count <= lb.LocalIndex)
            {
                indexToTypeList.Add(null);
            }
            indexToTypeList[lb.LocalIndex] = lb.LocalType;
            return lb;
        }

        public virtual void Pop()
        {
            gen.Emit(OpCodes.Pop);
        }

        public virtual void PopIfReturnValue(MethodInstance method)
        {
            if (NewType.VOID_TYPE.Equals(method.ReturnType))
            {
                return;
            }
            Pop();
        }

        public virtual void Push<T>()
        {
            Push(typeof(T));
        }

        public virtual void Push(bool value)
        {
            if (value)
            {
                gen.Emit(OpCodes.Ldc_I4_1);
            }
            else
            {
                gen.Emit(OpCodes.Ldc_I4_0);
            }
        }

        public virtual void Push(bool? value)
        {
            if (!value.HasValue)
            {
                ConstructorInfo nullValueType = typeof(Nullable<bool>).GetConstructor(new Type[0]);
                NewInstance(new ConstructorInstance(nullValueType), null);
            }
            else
            {
                ConstructorInfo hasValueType = typeof(Nullable<bool>).GetConstructor(new Type[] { typeof(bool) });
                NewInstance(new ConstructorInstance(hasValueType), delegate(IMethodVisitor mv)
                {
                    mv.Push(value.Value);
                });
            }
        }

        public virtual void Push(double value)
        {
            gen.Emit(OpCodes.Ldc_R8, value);
        }

        public virtual void Push(float value)
        {
            gen.Emit(OpCodes.Ldc_R4, value);
        }

        public virtual void Push(int value)
        {
            switch (value)
            {
                case 0:
                    gen.Emit(OpCodes.Ldc_I4_0);
                    break;
                case 1:
                    gen.Emit(OpCodes.Ldc_I4_1);
                    break;
                case 2:
                    gen.Emit(OpCodes.Ldc_I4_2);
                    break;
                case 3:
                    gen.Emit(OpCodes.Ldc_I4_3);
                    break;
                case 4:
                    gen.Emit(OpCodes.Ldc_I4_4);
                    break;
                case 5:
                    gen.Emit(OpCodes.Ldc_I4_5);
                    break;
                case 6:
                    gen.Emit(OpCodes.Ldc_I4_6);
                    break;
                case 7:
                    gen.Emit(OpCodes.Ldc_I4_7);
                    break;
                case 8:
                    gen.Emit(OpCodes.Ldc_I4_8);
                    break;
                default:
                    gen.Emit(OpCodes.Ldc_I4, value);
                    break;
            }
        }

        public virtual void Push(long value)
        {
            gen.Emit(OpCodes.Ldc_I8, value);
        }

        public virtual void Push(String value)
        {
            if (value != null)
            {
                gen.Emit(OpCodes.Ldstr, value);
            }
            else
            {
                PushNull();
            }
        }

        public virtual void Push(Type value)
        {
            if (value != null)
            {
                gen.Emit(OpCodes.Ldtoken, value);
                gen.Emit(OpCodes.Call, getTypeFromHandleMI);
            }
            else
            {
                PushNull();
            }
        }

        public virtual void Push(FieldInfo field)
        {
            gen.Emit(OpCodes.Ldtoken, field);
        }

        public virtual void Push(MethodInfo method)
        {
            gen.Emit(OpCodes.Ldtoken, method);
            gen.Emit(OpCodes.Call, getMethodFromHandleMI);
            gen.Emit(OpCodes.Castclass, typeof(MethodInfo));
        }

        public virtual void PushEnum(Object enumInstance)
        {
            ParamChecker.AssertParamNotNull(enumInstance, "enumInstance");
            ParamChecker.AssertTrue(enumInstance.GetType().IsEnum, "enumInstance");
            Push((int)enumInstance);
        }

        public virtual void PushNull()
        {
            gen.Emit(OpCodes.Ldnull);
        }

        public virtual void PushNullOrZero(NewType type)
        {
            PushNullOrZero(type.Type);
        }

        public virtual void PushNullOrZero(Type type)
        {
            if (typeof(long).Equals(type))
            {
                Push((long)(0));
            }
            else if (typeof(double).Equals(type))
            {
                Push((double)0);
            }
            else if (typeof(float).Equals(type))
            {
                Push((float)0);
            }
            else if (typeof(bool).Equals(type))
            {
                Push(false);
            }
            else if (typeof(byte).Equals(type))
            {
                Push((byte)0);
            }
            else if (typeof(char).Equals(type))
            {
                Push((char)0);
            }
            else if (typeof(short).Equals(type))
            {
                Push((short)0);
            }
            else if (typeof(int).Equals(type))
            {
                Push((int)0);
            }
            else
            {
                PushNull();
            }
        }

        public virtual void PutField(FieldInstance field)
        {
            ParamChecker.AssertParamNotNull(field, "field");
            if (!field.Access.HasFlag(FieldAttributes.Static))
            {
                gen.Emit(OpCodes.Stfld, field.Field);
            }
            else
            {
                PutStatic(field);
            }
            
        }

        public virtual void PutStatic(FieldInstance field)
        {
            gen.Emit(OpCodes.Stsfld, field.Field);
        }

        public virtual void PutThisField(FieldInstance field, Script script)
        {
            if (!field.Access.HasFlag(FieldAttributes.Static))
            {
                LoadThis();
                script.Invoke(this);
                PutField(field);
            }
            else
            {
                script.Invoke(this);
                PutStatic(field);
            }
        }

        public virtual void ReturnValue()
        {
            gen.Emit(OpCodes.Ret);
        }

        public virtual void ReturnVoidOrThis()
        {
            if (!NewType.VOID_TYPE.Equals(method.ReturnType))
            {
                LoadThis();
            }
            ReturnValue();
        }

        public virtual void Switch(int startIndex, int endIndex, Label defaultLabel, Label[] caseLabels)
        {
            gen.Emit(OpCodes.Switch, caseLabels);
        }

        public virtual void StoreLocal(LocalVariableInfo localIndex)
        {
            switch (localIndex.LocalIndex)
            {
                case 0:
                    gen.Emit(OpCodes.Stloc_0);
                    break;
                case 1:
                    gen.Emit(OpCodes.Stloc_1);
                    break;
                case 2:
                    gen.Emit(OpCodes.Stloc_2);
                    break;
                case 3:
                    gen.Emit(OpCodes.Stloc_3);
                    break;
                default:
                    gen.Emit(OpCodes.Stloc, (LocalBuilder)localIndex);
                    break;
            }
        }

        public virtual void ThrowException(Type exceptionType, String message)
        {
            gen.ThrowException(exceptionType);
        }

        public virtual void TryFinally(Script tryScript, Script finallyScript)
        {
            Label tryLabel = NewLabel();
            Label catchLabel = NewLabel();
            Label successLabel = NewLabel();

            //TODO 

            tryScript.Invoke(this);

            finallyScript.Invoke(this);

            //VisitTryCatchBlock(tryLabel, catchLabel, catchLabel, null);

            //Mark(tryLabel);

            //tryScript.Invoke(this);
            //GoTo(successLabel);

            //mark(catchLabel);
            //int loc_throwable = NewLocal(typeof(Exception));
            //StoreLocal(loc_throwable);

            //finallyScript.Invoke(this);

            //LoadLocal(loc_throwable);
            //ThrowException();

            //Mark(successLabel);
            //finallyScript.Invoke(this);
        }

        public virtual void Unbox(Type unboxedType)
        {
            gen.Emit(OpCodes.Unbox_Any, unboxedType);
        }

        public virtual void ValueOf(Type type)
        {
            Box(type);
        }

        public virtual void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments)
        {
            ((MethodBuilder)Method.Method).SetCustomAttribute(new CustomAttributeBuilder(annotationConstructor, arguments));
        }

        public virtual void VisitTableSwitchInsn(int min, int max, Label defaultCaseLabel, params Label[] caseLabels)
        {
            gen.Emit(OpCodes.Switch, caseLabels);

            gen.Emit(OpCodes.Br, defaultCaseLabel);
        }
    }
}
