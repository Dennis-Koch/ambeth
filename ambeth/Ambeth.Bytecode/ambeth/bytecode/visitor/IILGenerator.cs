using System.Reflection.Emit;
using System.Reflection;
using System;
using System.Runtime.InteropServices;
using System.Diagnostics.SymbolStore;
using System.Text;

namespace De.Osthus.Ambeth.Visitor
{
    public interface IILGenerator
    {
        int ILOffset { get; }

		void BeginCatchBlock(Type exceptionType);

		void BeginExceptFilterBlock();

		Label BeginExceptionBlock();

		void BeginFaultBlock();

		void BeginFinallyBlock();

		void BeginScope();

		LocalBuilder DeclareLocal(Type localType);

		LocalBuilder DeclareLocal(Type localType, bool pinned);

		Label DefineLabel();

		void Emit(OpCode opcode);

		void Emit(OpCode opcode, byte arg);

        void Emit(OpCode opcode, ConstructorInfo con);

        void Emit(OpCode opcode, double arg);

		void Emit(OpCode opcode, FieldInfo field);

        void Emit(OpCode opcode, float arg);

		void Emit(OpCode opcode, int arg);

		void Emit(OpCode opcode, Label label);

		void Emit(OpCode opcode, Label[] labels);

		void Emit(OpCode opcode, LocalBuilder local);

		void Emit(OpCode opcode, long arg);

        void Emit(OpCode opcode, MethodInfo meth);

        void Emit(OpCode opcode, sbyte arg);

		void Emit(OpCode opcode, short arg);

		void Emit(OpCode opcode, SignatureHelper signature);

		void Emit(OpCode opcode, string str);

        void Emit(OpCode opcode, Type cls);

        void EmitCall(OpCode opcode, MethodInfo methodInfo, Type[] optionalParameterTypes);

		void EmitCalli(OpCode opcode, CallingConvention unmanagedCallConv, Type returnType, Type[] parameterTypes);

        void EmitCalli(OpCode opcode, CallingConventions callingConvention, Type returnType, Type[] parameterTypes, Type[] optionalParameterTypes);

        void EmitWriteLine(FieldInfo fld);

        void EmitWriteLine(LocalBuilder localBuilder);

        void EmitWriteLine(string value);

		void EndExceptionBlock();

		void EndScope();

		void MarkLabel(Label loc);

		void MarkSequencePoint(ISymbolDocumentWriter document, int startLine, int startColumn, int endLine, int endColumn);

        void ThrowException(Type excType);

		void UsingNamespace(string usingNamespace);
    }
}
