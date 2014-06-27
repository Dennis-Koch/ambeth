using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public interface IMethodVisitor
    {
        MethodInstance Method { get; }

        void ArrayLoad(Type componentType);

        void ArrayStore(Type componentType);

        void Box(Type unboxedType);

        void Unbox(Type unboxedType);

        void CallThisGetter(MethodInstance method);

        void CallThisGetter(PropertyInstance property);

        void CallThisSetter(MethodInstance method, Script script);

        void CallThisSetter(PropertyInstance property, Script script);

        void CheckCast(Type castedType);

        void Dup();

        void EndMethod();

        void GetField(FieldInstance field);

        void GetThisField(FieldInstance field);

        void GetStatic(FieldInstance field);

        void GoTo(Label label);

        void IfCmp(Type type, CompareOperator compareOperator, Label label);

        void IfNonNull(Label label);

        void IfNull(Label label);

        void IfThisInstanceOf(Type instanceOfType, Script loadValue, Script executeIfTrue, Script executeIfFalse);

        void IfZCmp(CompareOperator compareOperator, Label label);

        void InvokeConstructor(ConstructorInfo constructor);

        void InvokeConstructor(ConstructorInstance constructor);

        void InvokeSuper(MethodInstance method);

        void InvokeSuperOfCurrentMethod();

        void InvokeOnExactOwner(MethodInfo method);

        void InvokeOnExactOwner(MethodInstance method);

        void InvokeVirtual(MethodInstance method);

        void InvokeInterface(MethodInstance method);

        void InvokeSuperOf(MethodInfo method);

        void InvokeStatic(MethodInfo method);

        void InvokeStatic(MethodInstance method);

        void InvokeGetValue(ITypeInfoItem member, Script thisScript);

        void LoadArg(int argIndex);

        void LoadArgs();

        void LoadLocal(LocalVariableInfo localIndex);

        void LoadThis();

        void Mark(Label label);

        void NewArray(Type componentType);

        Label NewLabel();

        LocalVariableInfo NewLocal<T>();

        LocalVariableInfo NewLocal(NewType localVariableType);

        LocalVariableInfo NewLocal(Type localVariableType);

        void Pop();

        void PopIfReturnValue(MethodInstance method);

        void Push<T>();

        void Push(bool value);

        void Push(double value);

        void Push(float value);

        void Push(int value);

        void Push(long value);

        void Push(String value);

        void Push(Type value);

        void PushEnum(Object enumInstance);

        void PushNull();

        void PushNullOrZero(Type type);

        void PutField(FieldInstance field);

        void PutStatic(FieldInstance field);

        void PutThisField(FieldInstance field, Script script);

        void ReturnValue();

        void ReturnVoidOrThis();

        void StoreLocal(LocalVariableInfo localIndex);

        void Switch(int startIndex, int endIndex, Label defaultLabel, Label[] caseLabels);

        void ThrowException(Type exceptionType, String message);

        void TryFinally(Script tryScript, Script finallyScript);
    }
}
