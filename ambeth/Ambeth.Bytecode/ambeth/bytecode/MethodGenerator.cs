//using De.Osthus.Ambeth.Bytecode.Behavior;
//using De.Osthus.Ambeth.Exceptions;
//using De.Osthus.Ambeth.Typeinfo;
//using De.Osthus.Ambeth.Util;
//using De.Osthus.Ambeth.Visitor;
//using System;
//using System.Reflection;

//namespace De.Osthus.Ambeth.Bytecode
//{
//    public class MethodGenerator : GeneratorAdapter
//    {
//        protected readonly MethodInstance method;
//        protected readonly ClassGenerator cg;

//        //protected readonly Printer methodPrinter = new Textifier();

//        public MethodGenerator(ClassGenerator cg, MethodVisitor mv, NewType owner, int access, String name, String signature, NewType returnType, params NewType[] parameters)
//            : this(cg, mv, new MethodInstance(owner, access, name, signature, returnType, parameters))
//        {
//            // Intended blank
//        }

//        public MethodGenerator(ClassGenerator cg, MethodVisitor mv, NewType owner, MethodInfo method)
//            : this(cg, mv, new MethodInstance(method))
//        {
//            // Intended blank
//        }

//        public MethodGenerator(ClassGenerator cg, MethodVisitor mv, MethodInstance method)
//            : base(new TraceMethodVisitor(mv, new Textifier()), method.Access, method.Name, method.getDescriptor())
//        {
//            this.cg = cg;
//            this.method = method;
//        }

//        public MethodInstance Method
//        {
//            get
//            {
//                return method;
//            }
//        }

//        public ClassGenerator ClassGenerator
//        {
//            get
//            {
//                return cg;
//            }
//        }

//        /**
//         * Generates the instructions to jump to a label based on the comparison of the top two stack values.
//         * 
//         * @param type
//         *            the type of the top two stack values.
//         * @param mode
//         *            how these values must be compared. One of EQ, NE, LT, GE, GT, LE.
//         * @param label
//         *            where to jump if the comparison result is <tt>true</tt>.
//         */
//        public void IfCmp(Type type, int mode, Label label)
//        {
//            IfCmp(NewType.GetType(type), mode, label);
//        }

//        public override String ToString()
//        {
//            if (!(mv is TraceMethodVisitor))
//            {
//                return super.toString();
//            }
//            StringWriter sw = new StringWriter();

//            PrintWriter pw = new PrintWriter(sw);

//            ((TraceMethodVisitor)mv).p.print(pw);

//            return sw.toString();
//        }

//        //public void println(CharSequence text)
//        //{
//        //    Type type = Type.getType(PrintStream.class);
//        //    getStatic(Type.getType(System.class), "out", type);
//        //    MethodInstance m_println = new MethodInstance(type, PrintStream.class, "println", String.class);
//        //    push(text.toString());
//        //    invokeVirtual(m_println);
//        //}

        

//        public void EndMethod()
//        {
//            try
//            {
//                base.EndMethod();
//            }
//            catch (Exception e)
//            {
//                throw RuntimeExceptionUtil.Mask(e, "Error occured while finishing method: " + Method + "\n" + ToString());
//            }
//        }
//    }
//}