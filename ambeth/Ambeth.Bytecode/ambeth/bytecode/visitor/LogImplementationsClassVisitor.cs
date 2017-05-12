using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Core;
using De.Osthus.Ambeth.Util;
using System;
using System.Diagnostics;
using System.Reflection;
using System.Threading;

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
        
        protected String ExtractCallingVisitor(String fromMethodName)
	    {
		    StackFrame[] stes = new StackTrace().GetFrames();
		    StackFrame ste = null;
		    for (int index = 0, size = stes.Length; index < size; index++)
		    {
                //if (stes[index].GetMethod().DeclaringType.Equals(typeof(Thread)))
                //{
                //    continue;
                //}
                //if (stes[index].getClassName().equals(ClassGenerator.class.getName()))
                //{
                //    continue;
                //}
                //if (stes[index].getClassName().equals(LogImplementationsClassVisitor.class.getName()))
                //{
                //    continue;
                //}
			    if (stes[index].GetMethod().Name.Equals(fromMethodName))
			    {
				    continue;
			    }
			    ste = stes[index];
			    break;
		    }
		    return ste.GetMethod().DeclaringType.FullName + "#" + ste.GetMethod().Name;
	    }

        public override IMethodVisitor VisitMethod(MethodAttributes access, String name, NewType returnType, params NewType[] parameters)
        {
            IBytecodeBehaviorState state = State;
            if (state != null)
            {
                MethodInstance method = new MethodInstance(state.NewType, access, returnType, name, parameters);
                ((BytecodeBehaviorState)state).MethodImplemented(method);
            }
            IMethodVisitor mv = base.VisitMethod(access, name, returnType, parameters);
            mv.VisitAnnotation(typeof(ByVisitor).GetConstructor(new Type[] { typeof(String) }), ExtractCallingVisitor("VisitMethod"));
		    return mv;
        }
    }
}