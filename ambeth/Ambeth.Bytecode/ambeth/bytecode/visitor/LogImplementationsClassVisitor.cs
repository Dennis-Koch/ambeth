using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class LogImplementationsClassVisitor : ClassVisitor
    {
        public LogImplementationsClassVisitor(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        //public override IFieldVisitor VisitField(FieldInstance field)
        //{
        //    IBytecodeBehaviorState state = State;
        //    if (state != null)
        //    {
        //        ((BytecodeBehaviorState)state).FieldImplemented(field);
        //    }
        //    return base.VisitField(field);
        //}

        //public override IMethodVisitor VisitMethod(MethodInstance method)
        //{
        //    IBytecodeBehaviorState state = State;
        //    if (state != null)
        //    {
        //        ((BytecodeBehaviorState)state).MethodImplemented(method);
        //    }
        //    return base.VisitMethod(method);
        //}

        public override IMethodVisitor VisitMethod(MethodAttributes access, String name, NewType returnType, params NewType[] parameters)
        {
            IBytecodeBehaviorState state = State;
            if (state != null)
            {
                MethodInstance method = new MethodInstance(state.NewType, access, returnType, name, parameters);
                ((BytecodeBehaviorState)state).MethodImplemented(method);
            }
            return base.VisitMethod(access, name, returnType, parameters);
        }
    }
}