using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class FieldVisitor : IFieldVisitor
    {
        protected readonly FieldBuilder fb;

        public FieldVisitor(FieldBuilder fb)
        {
            this.fb = fb;
        }

        public void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments)
        {
            fb.SetCustomAttribute(new CustomAttributeBuilder(annotationConstructor, arguments));
        }

        public void VisitEnd()
        {
            // Intended blank
        }
    }
}
