using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Annotation
{
    public class AnnotationInfo<A> : IAnnotationInfo<A> where A : Attribute
    {
        protected A annotation;
        
        public MemberInfo AnnotatedElement { get; private set; }

        public AnnotationInfo(A annotation, MemberInfo annotatedElement)
        {
            this.annotation = annotation;
            this.AnnotatedElement = annotatedElement;
        }

        A IAnnotationInfo<A>.Annotation
        {
            get { return annotation; }
        }

        Attribute IAnnotationInfo.Annotation
        {
            get { return annotation; }
        }
    }
}
