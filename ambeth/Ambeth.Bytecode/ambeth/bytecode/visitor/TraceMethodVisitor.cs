using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class TraceMethodVisitor : MethodVisitor
    {
        protected readonly StringBuilder sb;

        public TraceMethodVisitor(IMethodVisitor mv, StringBuilder sb)
            : base(mv)
        {
            this.sb = sb;
        }

        public override void ArrayLoad(Type componentType)
        {
            throw new NotImplementedException();
        }

        public override void ArrayStore(Type componentType)
        {
            throw new NotImplementedException();
        }

        public override void Box(Type unboxedType)
        {
            throw new NotImplementedException();
        }

        public override void Unbox(Type unboxedType)
        {
            throw new NotImplementedException();
        }

        public override void CallThisGetter(MethodInstance method)
        {
            throw new NotImplementedException();
        }

        public override void CallThisGetter(PropertyInstance property)
        {
            throw new NotImplementedException();
        }

        public override void CallThisSetter(MethodInstance method, Script script)
        {
            throw new NotImplementedException();
        }

        public override void CallThisSetter(PropertyInstance property, Script script)
        {
            throw new NotImplementedException();
        }

        public override void CheckCast(Type castedType)
        {
            throw new NotImplementedException();
        }

        public override void Dup()
        {
            throw new NotImplementedException();
        }

        public override void EndMethod()
        {
            throw new NotImplementedException();
        }

        public override void GetField(FieldInstance field)
        {
            throw new NotImplementedException();
        }

        public override void GetThisField(FieldInstance field)
        {
            throw new NotImplementedException();
        }

        public override void GetStatic(FieldInstance field)
        {
            throw new NotImplementedException();
        }

        public override void GoTo(Label label)
        {
            throw new NotImplementedException();
        }

        public override void IfCmp(Type type, CompareOperator compareOperator, Label label)
        {
            throw new NotImplementedException();
        }

        public override void IfNonNull(Label label)
        {
            throw new NotImplementedException();
        }

        public override void IfNull(Label label)
        {
            throw new NotImplementedException();
        }

        public override void IfThisInstanceOf(Type instanceOfType, Script loadValue, Script executeIfTrue, Script executeIfFalse)
        {
            throw new NotImplementedException();
        }

        public override void IfZCmp(CompareOperator compareOperator, Label label)
        {
            throw new NotImplementedException();
        }

        public override void InvokeConstructor(ConstructorInfo constructor)
        {
            throw new NotImplementedException();
        }

        public override void InvokeConstructor(ConstructorInstance constructor)
        {
            throw new NotImplementedException();
        }

        public override void InvokeSuper(MethodInstance method)
        {
            throw new NotImplementedException();
        }

        public override void InvokeSuperOfCurrentMethod()
        {
            throw new NotImplementedException();
        }

        public override void InvokeOnExactOwner(MethodInfo method)
        {
            throw new NotImplementedException();
        }

        public override void InvokeOnExactOwner(MethodInstance method)
        {
            throw new NotImplementedException();
        }

        public override void InvokeVirtual(MethodInstance method)
        {
            throw new NotImplementedException();
        }

        public override void InvokeInterface(MethodInstance method)
        {
            throw new NotImplementedException();
        }

        public override void InvokeSuperOf(MethodInfo method)
        {
            throw new NotImplementedException();
        }

        public override void InvokeStatic(MethodInfo method)
        {
            throw new NotImplementedException();
        }

        public override void InvokeStatic(MethodInstance method)
        {
            throw new NotImplementedException();
        }

        public override void LoadArg(int argIndex)
        {
            throw new NotImplementedException();
        }

        public override void LoadArgs()
        {
            throw new NotImplementedException();
        }

        public override void LoadLocal(LocalVariableInfo localIndex)
        {
            throw new NotImplementedException();
        }

        public override void LoadThis()
        {
            throw new NotImplementedException();
        }

        public override void Mark(Label label)
        {
            throw new NotImplementedException();
        }

        public override void NewArray(Type componentType)
        {
            throw new NotImplementedException();
        }

        public override Label NewLabel()
        {
            throw new NotImplementedException();
        }

        public override LocalVariableInfo NewLocal<T>()
        {
            throw new NotImplementedException();
        }

        public override LocalVariableInfo NewLocal(NewType localVariableType)
        {
            throw new NotImplementedException();
        }

        public override LocalVariableInfo NewLocal(Type localVariableType)
        {
            throw new NotImplementedException();
        }

        public override void Pop()
        {
            throw new NotImplementedException();
        }

        public override void PopIfReturnValue(MethodInstance method)
        {
            throw new NotImplementedException();
        }

        public override void Push<T>()
        {
            throw new NotImplementedException();
        }

        public override void Push(bool value)
        {
            throw new NotImplementedException();
        }

        public override void Push(double value)
        {
            throw new NotImplementedException();
        }

        public override void Push(float value)
        {
            throw new NotImplementedException();
        }

        public override void Push(int value)
        {
            throw new NotImplementedException();
        }

        public override void Push(long value)
        {
            throw new NotImplementedException();
        }

        public override void Push(string value)
        {
            throw new NotImplementedException();
        }

        public override void Push(Type value)
        {
            throw new NotImplementedException();
        }

        public override void PushEnum(object enumInstance)
        {
            throw new NotImplementedException();
        }

        public override void PushNull()
        {
            throw new NotImplementedException();
        }

        public override void PushNullOrZero(Type type)
        {
            throw new NotImplementedException();
        }

        public override void PutField(FieldInstance field)
        {
            throw new NotImplementedException();
        }

        public override void PutStatic(FieldInstance field)
        {
            throw new NotImplementedException();
        }

        public override void PutThisField(FieldInstance field, Script script)
        {
            throw new NotImplementedException();
        }

        public override void ReturnValue()
        {
            throw new NotImplementedException();
        }

        public override void ReturnVoidOrThis()
        {
            throw new NotImplementedException();
        }

        public override void StoreLocal(LocalVariableInfo localIndex)
        {
            throw new NotImplementedException();
        }

        public override void Switch(int startIndex, int endIndex, Label defaultLabel, Label[] caseLabels)
        {
            throw new NotImplementedException();
        }

        public override void ThrowException(Type exceptionType, string message)
        {
            throw new NotImplementedException();
        }

        public override void TryFinally(Script tryScript, Script finallyScript)
        {
            throw new NotImplementedException();
        }

        public override void ValueOf(Type type)
        {
            throw new NotImplementedException();
        }
    }
}
