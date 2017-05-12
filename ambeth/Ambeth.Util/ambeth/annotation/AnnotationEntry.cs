using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Annotation
{
    public class AnnotationEntry<T>
    {
        public T Annotation { get; protected set; }

        public MemberInfo DeclaringAnnotatedElement { get; protected set; }

        public AnnotationEntry(T annotation, MemberInfo declaringAnnotatedElement)
        {
            this.Annotation = annotation;
            this.DeclaringAnnotatedElement = declaringAnnotatedElement;
        }

        public Type DeclaringType { get { return (Type)DeclaringAnnotatedElement; } }
    }
}
