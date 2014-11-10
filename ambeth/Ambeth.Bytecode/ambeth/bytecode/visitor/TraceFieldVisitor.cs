using System;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class TraceFieldVisitor : IFieldVisitor
    {
        protected readonly IFieldVisitor fv;

        protected readonly StringBuilder sb;

        public TraceFieldVisitor(IFieldVisitor fv, StringBuilder sb)
        {
            this.fv = fv;
            this.sb = sb;
        }

        public void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments)
        {
            if (fv != null)
            {
                fv.VisitAnnotation(annotationConstructor, arguments);
            }
        }

        public void VisitEnd()
        {
            if (fv != null)
            {
                fv.VisitEnd();
            }
        }
    }
}
