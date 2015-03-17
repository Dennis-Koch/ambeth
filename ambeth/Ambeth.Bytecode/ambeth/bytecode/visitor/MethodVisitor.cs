using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class MethodVisitor : IMethodVisitor
    {
        private readonly IMethodVisitor mv;

        public MethodVisitor() : this(null)
        {
            // Intended blank
        }

        public MethodVisitor(IMethodVisitor mv)
        {
            this.mv = mv;
        }

        public virtual MethodInstance Method
        {
            get
            {
                if (mv != null)
                {
                    return mv.Method;
                }
                throw new NotSupportedException();
            }
        }

        public virtual void ArrayLoad(Type itemType)
        {
            if (mv != null)
            {
                ArrayLoad(itemType);
            }
        }

        public virtual void ArrayStore(Type itemType)
        {
            if (mv != null)
            {
                ArrayStore(itemType);
            }
        }

        public virtual void Box(Type unboxedType)
        {
            if (mv != null)
            {
                mv.Box(unboxedType);
            }
        }
        
        public virtual void CallThisGetter(MethodInstance method)
        {
            if (mv != null)
            {
                mv.CallThisGetter(method);
            }
        }
        
        public virtual void CallThisGetter(PropertyInstance property)
        {
            if (mv != null)
            {
                mv.CallThisGetter(property);
            }
        }

        public virtual void CallThisSetter(MethodInstance method, Script script)
        {
            if (mv != null)
            {
                mv.CallThisSetter(method, script);
            }
        }

        public virtual void CallThisSetter(PropertyInstance property, Script script)
        {
            if (mv != null)
            {
                mv.CallThisSetter(property, script);
            }
        }

        public virtual void CheckCast(Type castedType)
        {
            if (mv != null)
            {
                mv.CheckCast(castedType);
            }
        }

        public virtual void CheckCast(NewType castedType)
        {
            if (mv != null)
            {
                mv.CheckCast(castedType);
            }
        }

        public virtual void Dup()
        {
            if (mv != null)
            {
                mv.Dup();
            }
        }

        public virtual void EndMethod()
        {
            if (mv != null)
            {
                mv.EndMethod();
            }
        }

        public virtual void GetField(FieldInstance field)
        {
            if (mv != null)
            {
                mv.GetField(field);
            }
        }

        public virtual void GetThisField(FieldInstance field)
        {
            if (mv != null)
            {
                mv.GetThisField(field);
            }
        }

        public virtual void GetStatic(FieldInstance field)
        {
            if (mv != null)
            {
                mv.GetStatic(field);
            }
        }

        public virtual void GoTo(Label label)
        {
            if (mv != null)
            {
                mv.GoTo(label);
            }
        }

        public virtual void IfCmp(Type type, CompareOperator compareOperator, Label label)
        {
            if (mv != null)
            {
                mv.IfCmp(type, compareOperator, label);
            }
        }

        public virtual void IfNonNull(Label label)
        {
            if (mv != null)
            {
                mv.IfNonNull(label);
            }
        }

        public virtual void IfNull(Label label)
        {
            if (mv != null)
            {
                mv.IfNull(label);
            }
        }

        public virtual void IfThisInstanceOf(Type instanceOfType, Script loadValue, Script executeIfTrue, Script executeIfFalse)
        {
            if (mv != null)
            {
                mv.IfThisInstanceOf(instanceOfType, loadValue, executeIfTrue, executeIfFalse);
            }
        }

        public virtual void IfZCmp(CompareOperator compareOperator, Label label)
        {
            if (mv != null)
            {
                mv.IfZCmp(compareOperator, label);
            }
        }

        public virtual void IfZCmp(NewType type, CompareOperator compareOperator, Label label)
        {
            if (mv != null)
            {
                mv.IfZCmp(type, compareOperator, label);
            }
        }

        public virtual void IfZCmp(Type type, CompareOperator compareOperator, Label label)
        {
            if (mv != null)
            {
                mv.IfZCmp(type, compareOperator, label);
            }
        }

        public virtual void InvokeConstructor(ConstructorInfo constructor)
        {
            if (mv != null)
            {
                mv.InvokeConstructor(constructor);
            }
        }

        public virtual void InvokeConstructor(ConstructorInstance constructor)
        {
            if (mv != null)
            {
                mv.InvokeConstructor(constructor);
            }
        }

        public virtual void InvokeSuper(MethodInstance method)
        {
            if (mv != null)
            {
                mv.InvokeSuper(method);
            }
        }

        public virtual void InvokeSuperOfCurrentMethod()
        {
            if (mv != null)
            {
                mv.InvokeSuperOfCurrentMethod();
            }
        }

        public virtual void InvokeOnExactOwner(MethodInfo method)
        {
            if (mv != null)
            {
                mv.InvokeOnExactOwner(method);
            }
        }

        public virtual void InvokeOnExactOwner(MethodInstance method)
        {
            if (mv != null)
            {
                mv.InvokeOnExactOwner(method);
            }
        }

        public virtual void InvokeVirtual(MethodInstance method)
        {
            if (mv != null)
            {
                mv.InvokeVirtual(method);
            }
        }

        public virtual void InvokeInterface(MethodInstance method)
        {
            if (mv != null)
            {
                mv.InvokeInterface(method);
            }
        }

        public virtual void InvokeSuperOf(MethodInfo method)
        {
            if (mv != null)
            {
                mv.InvokeSuperOf(method);
            }
        }

        public virtual void InvokeStatic(MethodInfo method)
        {
            if (mv != null)
            {
                mv.InvokeStatic(method);
            }
        }

        public virtual void InvokeStatic(MethodInstance method)
        {
            if (mv != null)
            {
                mv.InvokeStatic(method);
            }
        }

        public virtual void LoadArg(int argIndex)
        {
            if (mv != null)
            {
                mv.LoadArg(argIndex);
            }
        }

        public virtual void LoadArgs()
        {
            if (mv != null)
            {
                mv.LoadArgs();
            }
        }

        public virtual void LoadLocal(LocalVariableInfo localIndex)
        {
            if (mv != null)
            {
                mv.LoadLocal(localIndex);
            }
        }

        public virtual void LoadThis()
        {
            if (mv != null)
            {
                mv.LoadThis();
            }
        }

        public virtual void Mark(Label label)
        {
            if (mv != null)
            {
                mv.Mark(label);
            }
        }

        public virtual void NewArray(Type componentType)
        {
            if (mv != null)
            {
                mv.NewArray(componentType);
            }
        }

        public virtual void NewInstance(ConstructorInstance constructor, Script argumentsScript)
        {
            if (mv != null)
            {
                mv.NewInstance(constructor, argumentsScript);
            }
        }

        public virtual Label NewLabel()
        {
            if (mv != null)
            {
                return mv.NewLabel();
            }
            throw new NotSupportedException();
        }

        public virtual LocalVariableInfo NewLocal<T>()
        {
            if (mv != null)
            {
                return mv.NewLocal<T>();
            }
            throw new NotSupportedException();
        }
        
        public virtual LocalVariableInfo NewLocal(NewType localVariableType)
        {
            if (mv != null)
            {
                return mv.NewLocal(localVariableType);
            }
            throw new NotSupportedException();
        }

        public virtual LocalVariableInfo NewLocal(Type localVariableType)
        {
            if (mv != null)
            {
                return mv.NewLocal(localVariableType);
            }
            throw new NotSupportedException();
        }

        public virtual void Pop()
        {
            if (mv != null)
            {
                mv.Pop();
            }
        }

        public virtual void PopIfReturnValue(MethodInstance method)
        {
            if (mv != null)
            {
                mv.PopIfReturnValue(method);
            }
        }

        public virtual void Push<T>()
        {
            if (mv != null)
            {
                mv.Push<T>();
            }
        }

        public virtual void Push(bool value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void Push(bool? value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void Push(double value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void Push(float value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void Push(int value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void Push(long value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void Push(String value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void Push(Type value)
        {
            if (mv != null)
            {
                mv.Push(value);
            }
        }

        public virtual void PushEnum(Object enumInstance)
        {
            if (mv != null)
            {
                mv.PushEnum(enumInstance);
            }
        }

        public virtual void PushNull()
        {
            if (mv != null)
            {
                mv.PushNull();
            }
        }

        public virtual void PushNullOrZero(Type type)
        {
            if (mv != null)
            {
                mv.PushNullOrZero(type);
            }
        }

        public virtual void PushNullOrZero(NewType type)
        {
            if (mv != null)
            {
                mv.PushNullOrZero(type);
            }
        }

        public virtual void PutField(FieldInstance field)
        {
            if (mv != null)
            {
                mv.PutField(field);
            }
        }

        public virtual void PutStatic(FieldInstance field)
        {
            if (mv != null)
            {
                mv.PutStatic(field);
            }
        }

        public virtual void PutThisField(FieldInstance field, Script script)
        {
            if (mv != null)
            {
                mv.PutThisField(field, script);
            }
        }

        public virtual void ReturnValue()
        {
            if (mv != null)
            {
                mv.ReturnValue();
            }
        }

        public virtual void ReturnVoidOrThis()
        {
            if (mv != null)
            {
                mv.ReturnVoidOrThis();
            }
        }

        public virtual void Switch(int startIndex, int endIndex, Label defaultLabel, Label[] caseLabels)
        {
            if (mv != null)
            {
                mv.Switch(startIndex, endIndex, defaultLabel, caseLabels);
            }
        }

        public virtual void StoreLocal(LocalVariableInfo localIndex)
        {
            if (mv != null)
            {
                mv.StoreLocal(localIndex);
            }
        }

        public virtual void ThrowException(Type exceptionType, String message)
        {
            if (mv != null)
            {
                mv.ThrowException(exceptionType, message);
            }
        }

        public virtual void TryFinally(Script tryScript, Script finallyScript)
        {
            if (mv != null)
            {
                mv.TryFinally(tryScript, finallyScript);
            }
        }

        public virtual void Unbox(Type unboxedType)
        {
            if (mv != null)
            {
                mv.Unbox(unboxedType);
            }
        }

        public virtual void ValueOf(Type type)
        {
            if (mv != null)
            {
                mv.ValueOf(type);
            }
        }

        public virtual void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments)
        {
            if (mv != null)
            {
                mv.VisitAnnotation(annotationConstructor, arguments);
            }
        }

        public virtual void VisitTableSwitchInsn(int min, int max, Label dflt, params Label[] labels)
        {
            if (mv != null)
            {
                mv.VisitTableSwitchInsn(min, max, dflt, labels);
            }
        }
    }
}
