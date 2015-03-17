using System.Reflection.Emit;
using System.Reflection;
using System;
using System.Runtime.InteropServices;
using System.Diagnostics.SymbolStore;
using System.Text;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Bytecode.Behavior;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class TraceILGenerator : IILGenerator
    {
        protected readonly ILGenerator gen;

        protected readonly StringBuilder sb;

        protected HashMap<Label, int> labelToIndexMap = new HashMap<Label, int>();

        public TraceILGenerator(ILGenerator gen, StringBuilder sb)
        {
            this.gen = gen;
            this.sb = sb;
        }

        public int ILOffset
        {
            get
            {
#if SILVERLIGHT
                return 0;
#else
                return gen.ILOffset;
#endif
            }
        }

        public void BeginCatchBlock(Type exceptionType)
        {
            sb.Append("\r\n\t\tBeginCatchBlock");
            gen.BeginCatchBlock(exceptionType);
        }

        public void BeginExceptFilterBlock()
        {
            sb.Append("\r\n\t\tBeginExceptFilterBlock"); gen.BeginExceptFilterBlock();
        }

        public Label BeginExceptionBlock() { sb.Append("\r\n\t\tBeginExceptionBlock"); return gen.BeginExceptionBlock(); }
        public void BeginFaultBlock() { sb.Append("\r\n\t\tBeginFaultBlock"); gen.BeginFaultBlock(); }
        public void BeginFinallyBlock() { sb.Append("\r\n\t\tBeginFinallyBlock"); gen.BeginFinallyBlock(); }
        public void BeginScope() { sb.Append("\r\n\t\tBeginScope"); gen.BeginScope(); }
        public LocalBuilder DeclareLocal(Type localType) { sb.Append("\r\n\t\tDeclareLocal"); return gen.DeclareLocal(localType); }
        public LocalBuilder DeclareLocal(Type localType, bool pinned) { sb.Append("\r\n\t\tDeclareLocal"); return gen.DeclareLocal(localType, pinned); }

        public Label DefineLabel()
        {
            Label label = gen.DefineLabel();
            labelToIndexMap.Put(label, labelToIndexMap.Count);
            return label;
        }

        public void Emit(OpCode opcode) { sb.Append("\r\n\t\t" + opcode); gen.Emit(opcode); }
        public void Emit(OpCode opcode, byte arg) { sb.Append("\r\n\t\t" + opcode + " " + arg); gen.Emit(opcode, arg); }
        public void Emit(OpCode opcode, ConstructorInfo con) { sb.Append("\r\n\t\t" + opcode + " " + new ConstructorInstance(con)); gen.Emit(opcode, con); }
        public void Emit(OpCode opcode, double arg) { sb.Append("\r\n\t\t" + opcode + " " + arg); gen.Emit(opcode, arg); }
        public void Emit(OpCode opcode, FieldInfo field) { sb.Append("\r\n\t\t" + opcode + " " + new FieldInstance(field)); gen.Emit(opcode, field); }
        public void Emit(OpCode opcode, float arg) { sb.Append("\r\n\t\t" + opcode + " " + arg); gen.Emit(opcode, arg); }
        public void Emit(OpCode opcode, int arg) { sb.Append("\r\n\t\t" + opcode + " " + arg); gen.Emit(opcode, arg); }
        public void Emit(OpCode opcode, Label label) { sb.Append("\r\n\t\t" + opcode + " L" + labelToIndexMap.Get(label)); gen.Emit(opcode, label); }
        public void Emit(OpCode opcode, Label[] labels) { sb.Append("\r\n\t\t" + opcode + " " + labels); gen.Emit(opcode, labels); }
        public void Emit(OpCode opcode, LocalBuilder local) { sb.Append("\r\n\t\t" + opcode + " " + local); gen.Emit(opcode, local); }
        public void Emit(OpCode opcode, long arg) { sb.Append("\r\n\t\t" + opcode + " " + arg); gen.Emit(opcode, arg); }

        public void Emit(OpCode opcode, MethodInfo meth)
        {
            IBytecodeBehaviorState state = BytecodeBehaviorState.State;
            MethodInstance mi = null;
            foreach (MethodInstance methodOnNewType in state.GetAlreadyImplementedMethodsOnNewType())
            {
                if (Object.ReferenceEquals(methodOnNewType.Method, meth))
                {
                    mi = methodOnNewType;
                    break;
                }
            }
            if (mi == null)
            {
                mi = new MethodInstance(meth);
            }
            sb.Append("\r\n\t\t" + opcode + " " + mi);
            gen.Emit(opcode, meth);
        }

        public void Emit(OpCode opcode, sbyte arg) { sb.Append("\r\n\t\t" + opcode + " " + arg); gen.Emit(opcode, arg); }

        public void Emit(OpCode opcode, short arg) { sb.Append("\r\n\t\t" + opcode + " " + arg); gen.Emit(opcode, arg); }

        public void Emit(OpCode opcode, SignatureHelper signature)
        {
#if SILVERLIGHT
            throw new NotSupportedException();
#else
            sb.Append("\r\n\t\t" + opcode + " " + signature);
            gen.Emit(opcode, signature);
#endif
        }

        public void Emit(OpCode opcode, string str)
        {
            sb.Append("\r\n\t\t" + opcode + " " + str);
            gen.Emit(opcode, str);
        }

        public void Emit(OpCode opcode, Type cls) { sb.Append("\r\n\t\t" + opcode + " " + cls); gen.Emit(opcode, cls); }

        public void EmitCall(OpCode opcode, MethodInfo methodInfo, Type[] optionalParameterTypes) { sb.Append("\r\n\t\tEmitCall"); gen.EmitCall(opcode, methodInfo, optionalParameterTypes); }

        public void EmitCalli(OpCode opcode, CallingConvention unmanagedCallConv, Type returnType, Type[] parameterTypes)
        {
#if SILVERLIGHT
            throw new NotSupportedException();
#else
            sb.Append("\r\n\t\tEmitCalli");
            gen.EmitCalli(opcode, unmanagedCallConv, returnType, parameterTypes);
#endif
        }

        public void EmitCalli(OpCode opcode, CallingConventions callingConvention, Type returnType, Type[] parameterTypes, Type[] optionalParameterTypes)
        {
#if SILVERLIGHT
            throw new NotSupportedException();
#else
            sb.Append("\r\n\t\tEmitCalli"); 
            gen.EmitCalli(opcode, callingConvention, returnType, parameterTypes, optionalParameterTypes);
#endif
        }

        public void EmitWriteLine(FieldInfo fld) { sb.Append("\r\n\t\tEmitWriteLine"); gen.EmitWriteLine(fld); }
        public void EmitWriteLine(LocalBuilder localBuilder) { sb.Append("\r\n\t\tEmitWriteLine"); gen.EmitWriteLine(localBuilder); }
        public void EmitWriteLine(string value) { sb.Append("\r\n\t\tEmitWriteLine"); gen.EmitWriteLine(value); }
        public void EndExceptionBlock() { sb.Append("\r\n\t\tEndExceptionBlock"); gen.EndExceptionBlock(); }
        public void EndScope() { sb.Append("\r\n\t\tEndScope"); gen.EndScope(); }
        public void MarkLabel(Label loc)
        {
            sb.Append("\r\n\tL" + labelToIndexMap.Get(loc));
            gen.MarkLabel(loc);
        }
        public void MarkSequencePoint(ISymbolDocumentWriter document, int startLine, int startColumn, int endLine, int endColumn) { sb.Append("\r\n\t\tMarkSequencePoint"); gen.MarkSequencePoint(document, startLine, startColumn, endLine, endColumn); }
        public void ThrowException(Type excType) { sb.Append("\r\n\t\tThrow " + excType.FullName); gen.ThrowException(excType); }
        public void UsingNamespace(string usingNamespace) { sb.Append("\r\n\t\tUsingNamespace"); gen.UsingNamespace(usingNamespace); }
    }
}