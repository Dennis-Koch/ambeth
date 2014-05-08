using System;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Util;

 namespace De.Osthus.Ambeth.Annotation
{
    abstract public class AnnotationCache<T> where T : Attribute
    {
        protected readonly Dictionary<MemberInfo, AnnotationEntry<T>> typeToCacheContextMap = new Dictionary<MemberInfo, AnnotationEntry<T>>();

        protected readonly Lock readLock, writeLock;

        public AnnotationCache()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        public T GetAnnotation(MemberInfo type)
        {
            AnnotationEntry<T> entry = GetAnnotationEntry(type);
            if (entry == null)
            {
                return null;
            }            
            return entry.Annotation;
        }

        public AnnotationEntry<T> GetAnnotationEntry(MemberInfo annotatedElement)
        {
            readLock.Lock();
            try
            {
                if (typeToCacheContextMap.ContainsKey(annotatedElement))
                {
                    return typeToCacheContextMap[annotatedElement];
                }
            }
            finally
            {
                readLock.Unlock();
            }
            Object[] attributes = annotatedElement.GetCustomAttributes(typeof(T), true);
            T annotation = attributes != null && attributes.Length > 0 ? (T)attributes[0] : null;
            MemberInfo declaringAnnotatedElement = null;
            if (annotation == null)
            {
                if (annotatedElement is Type)
                {
                    Type type = (Type)annotatedElement;
                    Type[] interfaces = type.GetInterfaces();
                    for (int a = interfaces.Length; a-- > 0; )
                    {
                        attributes = interfaces[a].GetCustomAttributes(typeof(T), true);
                        T interfaceAnnotation = attributes != null && attributes.Length > 0 ? (T)attributes[0] : null;
                        if (interfaceAnnotation == null)
                        {
                            continue;
                        }
                        if (annotation == null)
                        {
                            annotation = interfaceAnnotation;
                            declaringAnnotatedElement = interfaces[a];
                            continue;
                        }
                        if (!AnnotationEquals(annotation, interfaceAnnotation))
                        {
                            throw new Exception("Ambiguous annotation on type " + type.ToString() + " with " + typeof(T).FullName
                                    + " based on multiple implementing interfaces with DIFFERENT " + typeof(T).FullName + " values");
                        }
                    }
                }
            }
            else
            {
                declaringAnnotatedElement = annotatedElement;
            }
            AnnotationEntry<T> entry = annotation != null ? new AnnotationEntry<T>(annotation, declaringAnnotatedElement) : null;
            writeLock.Lock();
            try
            {
                typeToCacheContextMap[annotatedElement] = entry;
            }
            finally
            {
                writeLock.Unlock();
            }
            return entry;
        }

        abstract protected bool AnnotationEquals(T left, T right);
    }
}
