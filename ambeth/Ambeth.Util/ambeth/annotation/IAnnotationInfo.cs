using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Annotation
{
    public interface IAnnotationInfo<A> : IAnnotationInfo where A : Attribute
    {
	    new A Annotation { get; }
    }

    public interface IAnnotationInfo
    {
        Attribute Annotation { get; }

        MemberInfo AnnotatedElement { get; }
    }
}
