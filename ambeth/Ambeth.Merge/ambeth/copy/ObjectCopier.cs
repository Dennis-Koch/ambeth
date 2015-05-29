using System;
using System.Collections;
using System.Reflection;
using System.Threading;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Copy
{
    /// <summary>
    /// Reference implementation for the <code>IObjectCopier</code> interface. Provides an extension point to customize to copy behavior on specific object types.
    /// </summary>
    public class ObjectCopier : IObjectCopier, IObjectCopierExtendable, IThreadLocalCleanupBean
    {
        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        /// <summary>
        /// Save an instance of ObjectCopierState per-thread for performance reasons
        /// </summary>
        protected readonly ThreadLocal<ObjectCopierState> ocStateTL = new ThreadLocal<ObjectCopierState>();

        /// <summary>
        /// Saves the current instance of ocStateTL to recognize recursive calls to the same ObjectCopier
        /// </summary>
        protected readonly ThreadLocal<ObjectCopierState> usedOcStateTL = new ThreadLocal<ObjectCopierState>();

        protected readonly ClassExtendableContainer<IObjectCopierExtension> extensions = new ClassExtendableContainer<IObjectCopierExtension>("objectCopierExtension", "type");

        public void CleanupThreadLocal()
        {
            // Cleanup the TL variables. This is to be safe against memory leaks in thread pooling architectures
            ocStateTL.Value = null;
            usedOcStateTL.Value = null;
        }

        protected ObjectCopierState AcquireObjectCopierState()
        {
            // Creates automatically a valid instance if this thread does not already have one
            ObjectCopierState ocState = ocStateTL.Value;
            if (ocState == null)
            {
                ocState = new ObjectCopierState(this);
                ocStateTL.Value = ocState;
            }
            return ocState;
        }

        public T Clone<T>(T source)
        {
            // Don't clone a null object or immutable objects. Return the identical reference in these cases
            if (source == null || ImmutableTypeSet.IsImmutableType(source.GetType()))
            {
                return source;
            }
            // Try to access current "in-use" ObjectCopierState first
            ObjectCopierState ocState = usedOcStateTL.Value;
            if (ocState != null)
            {
                // Reuse TL instance. And do not bother with cleanup
                return CloneRecursive(source, ocState);
            }
            // No ObjectCopierState "in-use". So we set the TL instance "in-use" and clean it up in the end
            // because we are responsible for this in this case
            ocState = AcquireObjectCopierState();
            usedOcStateTL.Value = ocState;
            try
            {

                return CloneRecursive(source, ocState);
            }
            finally
            {
                // Clear "in-use" instance
                usedOcStateTL.Value = null;
                // Cleanup ObjectCopierState to allow reusage in the same thread later
                ocState.Clear();
            }
        }

        /// <summary>
        /// Gets called by the ObjectCopierState on custom / default behavior switches
        /// </summary>
        internal T CloneRecursive<T>(T source, ObjectCopierState ocState)
        {
            // Don't clone a null object or immutable objects. Return the identical reference in these cases
            if (source == null || ImmutableTypeSet.IsImmutableType(source.GetType()))
            {
                return source;
            }
            Type objType = source.GetType();
            IdentityDictionary<Object, Object> objectToCloneDict = ocState.objectToCloneDict;
            Object clone = DictionaryExtension.ValueOrDefault(ocState.objectToCloneDict, source);

            if (clone != null)
            {
                // Object has already been cloned. Cycle detected - we are finished here
                return (T)clone;
            }
            if (objType.IsArray)
            {
                return (T)CloneArray(source, ocState);
            }
            if (source is IEnumerable && !(source is String))
            {
                return (T)CloneCollection(source, ocState);
            }
            // Check whether the object will be copied by custom behavior
            IObjectCopierExtension extension = extensions.GetExtension(objType);
            if (extension != null)
            {
                clone = extension.DeepClone(source, ocState);
                objectToCloneDict.Add(source, clone);
                return (T)clone;
            }
            // Copy by default behavior
            return (T)CloneDefault(source, ocState);
        }

        protected Object CloneArray(Object source, ObjectCopierState ocState)
        {
            Type objType = source.GetType();
            Array array = (Array)(Object)source;
            Type elementType = objType.GetElementType();
            int length = array.Length;
            Array cloneArray = Array.CreateInstance(elementType, length);
            ocState.objectToCloneDict.Add(source, cloneArray);
            if (ImmutableTypeSet.IsImmutableType(elementType))
            {
                // Clone native array with native functionality for performance reasons
                Array.Copy(array, cloneArray, length);
            }
            else
            {
                for (int a = length; a-- > 0; )
                {
                    // Clone each item of the array
                    cloneArray.SetValue(CloneRecursive(array.GetValue(a), ocState), a);
                }
            }
            return cloneArray;
        }

        protected Object CloneCollection(Object source, ObjectCopierState ocState)
        {
            Type objType = source.GetType();
            IEnumerable cloneColl = (IEnumerable)Activator.CreateInstance(objType);
            ocState.objectToCloneDict.Add(source, cloneColl);

            // The problem is that .NET does not have a holistic interface to add items to a collection
            MethodInfo addMethod = cloneColl.GetType().GetMethod("Add");
            Object[] args = ocState.addArgs;
            try
            {
                foreach (Object item in (IEnumerable)source)
                {
                    // Clone each item of the IEnumerable
                    Object cloneItem = CloneRecursive(item, ocState);
                    args[0] = cloneItem;
                    addMethod.Invoke(cloneColl, args);
                }
                return cloneColl;
            }
            finally
            {
                args[0] = null;
            }
        }

        protected Object CloneDefault(Object source, ObjectCopierState ocState)
        {
            Type objType = source.GetType();
            Object clone = Activator.CreateInstance(objType);
            ocState.objectToCloneDict.Add(source, clone);
			DeepCloneProperties(source, clone, ocState);
            return clone;
        }

		internal void DeepCloneProperties(Object source, Object clone, ObjectCopierState ocState)
		{
			IPropertyInfo[] properties = PropertyInfoProvider.GetProperties(source.GetType());
			foreach (IPropertyInfo property in properties)
			{
				if (!property.IsFieldWritable)
				{
					continue;
				}
				Object objValue = property.GetValue(source);
				Object cloneValue = CloneRecursive(objValue, ocState);
				property.SetValue(clone, cloneValue);
			}
		}

        public void RegisterObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Type type)
        {
            // Delegate pattern to register the extension in the internal extension manager
            extensions.Register(objectCopierExtension, type);
        }

        public void UnregisterObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Type type)
        {
            // Delegate pattern to unregister the extension from the internal extension manager
            extensions.Unregister(objectCopierExtension, type);
        }
    }
}