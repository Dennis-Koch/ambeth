using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public interface IFieldVisitor
    {
        void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments);

        void VisitEnd();
    }
}
