using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using System.Text.RegularExpressions;
using System.IO;
using De.Osthus.Ambeth.Ioc.Extendable;

namespace De.Osthus.Ambeth.Util
{
    public class ImmutableTypeSet
    {
	    protected static readonly CHashSet<Type> immutableTypeSet = new CHashSet<Type>(0.5f);

        protected static readonly HashMap<Type, Type> wrapperTypesMap = new HashMap<Type, Type>(0.5f);

        private static readonly ClassExtendableContainer<Type> immutableSuperTypes = new ClassExtendableContainer<Type>("", "");

	    static ImmutableTypeSet()
	    {
            wrapperTypesMap.Put(typeof(Int64?), typeof(Int64));
            wrapperTypesMap.Put(typeof(UInt64?), typeof(UInt64));
            wrapperTypesMap.Put(typeof(Int32?), typeof(Int32));
            wrapperTypesMap.Put(typeof(UInt32?), typeof(UInt32));
            wrapperTypesMap.Put(typeof(Int16?), typeof(Int16));
            wrapperTypesMap.Put(typeof(UInt16?), typeof(UInt16));
            wrapperTypesMap.Put(typeof(Byte?), typeof(Byte));
            wrapperTypesMap.Put(typeof(SByte?), typeof(SByte));
            wrapperTypesMap.Put(typeof(Double?), typeof(Double));
            wrapperTypesMap.Put(typeof(Single?), typeof(Single));
            wrapperTypesMap.Put(typeof(Char?), typeof(Char));
            wrapperTypesMap.Put(typeof(Boolean?), typeof(Boolean));

            immutableTypeSet.Add(typeof(Int64));
            immutableTypeSet.Add(typeof(Int64?));
            immutableTypeSet.Add(typeof(Int32));
            immutableTypeSet.Add(typeof(Int32?));
            immutableTypeSet.Add(typeof(Int16));
            immutableTypeSet.Add(typeof(Int16?));
            immutableTypeSet.Add(typeof(UInt64));
            immutableTypeSet.Add(typeof(UInt64?));
            immutableTypeSet.Add(typeof(UInt32));
            immutableTypeSet.Add(typeof(UInt32?));
            immutableTypeSet.Add(typeof(UInt16));
            immutableTypeSet.Add(typeof(UInt16?));
            immutableTypeSet.Add(typeof(Byte));
            immutableTypeSet.Add(typeof(Byte?));
            immutableTypeSet.Add(typeof(SByte));
            immutableTypeSet.Add(typeof(SByte?));
            immutableTypeSet.Add(typeof(Char));
            immutableTypeSet.Add(typeof(Char?));
            immutableTypeSet.Add(typeof(Boolean));
            immutableTypeSet.Add(typeof(Boolean?));
            immutableTypeSet.Add(typeof(Double));
            immutableTypeSet.Add(typeof(Double?));
            immutableTypeSet.Add(typeof(Single));
            immutableTypeSet.Add(typeof(Single?));
            immutableTypeSet.Add(typeof(String));
            immutableTypeSet.Add(typeof(Type));
            immutableTypeSet.Add(typeof(void));

            immutableTypeSet.Add(typeof(Regex));
            immutableTypeSet.Add(typeof(Uri));
            // In Java also: URL
            immutableTypeSet.Add(typeof(File));
        }

	    public static void AddImmutableTypesTo(IList<Type> collection)
	    {
            foreach (Type valueType in immutableTypeSet)
            {
		        collection.Add(valueType);
            }
	    }

        public static void AddImmutableTypesTo(IICollection<Type> collection)
        {
            foreach (Type valueType in immutableTypeSet)
            {
                collection.Add(valueType);
            }
        }

	    public static bool IsImmutableType(Type type)
	    {
		    return type.IsPrimitive || type.IsValueType || type.IsEnum || immutableTypeSet.Contains(type) || typeof(IImmutableType).IsAssignableFrom(type) || "RuntimeType".Equals(type.Name)
                || immutableSuperTypes.GetExtension(type) != null;
	    }

        public static void FlushState()
        {
            immutableSuperTypes.ClearWeakCache();
        }

	    private ImmutableTypeSet()
	    {
		    // Intended blank
	    }

        public static Type GetUnwrappedType(Type wrapperType)
	    {
		    return wrapperTypesMap.Get(wrapperType);
	    }
    }
}
