using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class ClassEntry<V> : HashMap<Type, Object> 
    {
        public readonly HashMap<Type, Object> typeToDefEntryMap = new HashMap<Type, Object>(0.5f);

        public readonly HashMap<StrongKey<V>, List<DefEntry<V>>> definitionReverseMap = new HashMap<StrongKey<V>, List<DefEntry<V>>>(0.5f);

        public ClassEntry() : base(0.5f)
        {
            // Intended blank
        }
    }

    public class ClassExtendableContainer<V> : MapExtendableContainer<Type, V>
    {
        public static readonly int NO_VALID_DISTANCE = -1;

        protected static readonly Object alreadyHandled = new Object();

        public static int GetDistanceForType(Type existingRequestedType, Type type)
        {
            // If a converter handles A (strong registration)
            // It implicitily handles X extends A (weak registration)
            if (existingRequestedType == null || !type.IsAssignableFrom(existingRequestedType))
            {
                return NO_VALID_DISTANCE;
            }
            if (existingRequestedType.Equals(type))
            {
                // Type matched exactly - 'strong' registration
                return 0;
            }
            if (existingRequestedType.IsArray)
            {
                if (!type.GetElementType().IsAssignableFrom(existingRequestedType.GetElementType()))
                {
                    return NO_VALID_DISTANCE;
                }
            }
            int bestDistance = Int32.MaxValue;
            Type[] currInterfaces = existingRequestedType.GetInterfaces();

            foreach (Type currInterface in currInterfaces)
            {
                int idistance = GetDistanceForType(currInterface, type);
                if (idistance < 0)
                {
                    continue;
                }
                idistance += 10000;
                if (idistance < bestDistance)
                {
                    bestDistance = idistance;
                }
            }
            Type baseType = existingRequestedType.BaseType;
            if (baseType == null)
            {
                baseType = typeof(Object);
            }
            int distance = GetDistanceForType(baseType, type);
            if (distance >= 0)
            {
                distance++;
                if (distance < bestDistance)
                {
                    bestDistance = distance;
                }
            }
            if (bestDistance == Int32.MaxValue)
            {
                if (Nullable.GetUnderlyingType(type) != null)
                {
                    return NO_VALID_DISTANCE;
                }
                throw new Exception("Must never happen");
            }
            return bestDistance;
        }

        protected volatile ClassEntry<V> classEntry = new ClassEntry<V>();

        public ClassExtendableContainer(String message, String keyMessage)
            : this(message, keyMessage, false)
        {
            // Intended blank
        }

        public ClassExtendableContainer(String message, String keyMessage, bool multiValue)
            : base(message, keyMessage, multiValue)
        {
            // Intended blank		
        }

        public override V GetExtension(Type key)
        {
            if (key == null)
            {
                return default(V);
            }
            Object extension = this.classEntry.Get(key);
            if (extension == null)
            {
                Object writeLock = GetWriteLock();
                lock (writeLock)
                {
                    extension = this.classEntry.Get(key);
                    if (extension == null)
                    {
                        ClassEntry<V> classEntry = CopyStructure();

                        classEntry.Put(key, alreadyHandled);
                        classEntry.typeToDefEntryMap.Put(key, alreadyHandled);
                        CheckToWeakRegisterExistingExtensions(key, classEntry);
                        this.classEntry = classEntry;

                        extension = classEntry.Get(key);
                        if (extension == null)
                        {
                            return default(V);
                        }
                    }
                }
            }
            if (Object.ReferenceEquals(extension, alreadyHandled))
            {
                // Already tried
                return default(V);
            }
            return (V)extension;
        }

        protected ClassEntry<V> CopyStructure()
        {
            ClassEntry<V> newClassEntry = new ClassEntry<V>();
            HashMap<Type, Object> newTypeToDefEntryMap = newClassEntry.typeToDefEntryMap;
            HashMap<StrongKey<V>, List<DefEntry<V>>> newDefinitionReverseMap = newClassEntry.definitionReverseMap;
            IdentityHashMap<DefEntry<V>, DefEntry<V>> originalToCopyMap = new IdentityHashMap<DefEntry<V>, DefEntry<V>>();
            {
                foreach (Entry<Type, Object> entry in classEntry.typeToDefEntryMap)
                {
                    Type key = entry.Key;
                    Object value = entry.Value;

                    if (Object.ReferenceEquals(value, alreadyHandled))
                    {
                        newTypeToDefEntryMap.Put(key, alreadyHandled);
                    }
                    else
                    {
                        InterfaceFastList<DefEntry<V>> list = (InterfaceFastList<DefEntry<V>>)value;

                        InterfaceFastList<DefEntry<V>> newList = new InterfaceFastList<DefEntry<V>>();

                        IListElem<DefEntry<V>> pointer = list.First;
                        while (pointer != null)
                        {
                            DefEntry<V> defEntry = pointer.ElemValue;
                            DefEntry<V> newDefEntry = new DefEntry<V>(defEntry.extension, defEntry.type, defEntry.distance);
                            originalToCopyMap.Put(defEntry, newDefEntry);

                            newList.PushLast(newDefEntry);
                            pointer = pointer.Next;
                        }
                        newTypeToDefEntryMap.Put(key, newList);
                    }
                    TypeToDefEntryMapChanged(newClassEntry, key);
                }
            }
            foreach (Entry<StrongKey<V>, List<DefEntry<V>>> entry in classEntry.definitionReverseMap)
            {
                List<DefEntry<V>> defEntries = entry.Value;
                List<DefEntry<V>> newDefEntries = new List<DefEntry<V>>(defEntries.Count);

                for (int a = 0, size = defEntries.Count; a < size; a++)
                {
                    DefEntry<V> newDefEntry = originalToCopyMap.Get(defEntries[a]);
                    if (newDefEntry == null)
                    {
                        throw new Exception("Must never happen");
                    }
                    newDefEntries.Add(newDefEntry);
                }
                newDefinitionReverseMap.Put(entry.Key, newDefEntries);
            }
            return newClassEntry;
        }

        protected bool CheckToWeakRegisterExistingExtensions(Type type, ClassEntry<V> classEntry)
        {
            bool changesHappened = false;
            foreach (Entry<StrongKey<V>, List<DefEntry<V>>> entry in classEntry.definitionReverseMap)
            {
                StrongKey<V> strongKey = entry.Key;
                Type registeredStrongType = strongKey.strongType;
                int distance = GetDistanceForType(type, registeredStrongType);
                if (distance == NO_VALID_DISTANCE)
                {
                    continue;
                }
                List<DefEntry<V>> defEntries = entry.Value;
                for (int a = defEntries.Count; a-- > 0; )
                {
                    DefEntry<V> defEntry = defEntries[a];
                    changesHappened |= AppendRegistration(registeredStrongType, type, defEntry.extension, distance, classEntry);
                }
            }
            return changesHappened;
        }

        protected bool CheckToWeakRegisterExistingTypes(Type type, V extension, ClassEntry<V> classEntry)
        {
            bool changesHappened = false;
            foreach (Entry<Type, Object> entry in classEntry.typeToDefEntryMap)
            {
                Type existingRequestedType = entry.Key;
                int priorityForExistingRequestedType = GetDistanceForType(existingRequestedType, type);
                if (priorityForExistingRequestedType == NO_VALID_DISTANCE)
                {
                    continue;
                }
                changesHappened |= AppendRegistration(type, existingRequestedType, extension, priorityForExistingRequestedType, classEntry);
            }
            return changesHappened;
        }

        public override void Register(V extension, Type key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                base.Register(extension, key);

                ClassEntry<V> classEntry = CopyStructure();
                AppendRegistration(key, key, extension, 0, classEntry);
                CheckToWeakRegisterExistingTypes(key, extension, classEntry);
                CheckToWeakRegisterExistingExtensions(key, classEntry);
                this.classEntry = classEntry;
            }
        }

        public override void Unregister(V extension, Type key)
        {
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                base.Unregister(extension, key);

                ClassEntry<V> classEntry = CopyStructure();
                HashMap<StrongKey<V>, List<DefEntry<V>>> definitionReverseMap = classEntry.definitionReverseMap;
                List<DefEntry<V>> weakEntriesOfStrongType = definitionReverseMap.Remove(new StrongKey<V>(extension, key));
                if (weakEntriesOfStrongType == null)
                {
                    return;
                }
                HashMap<Type, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
                for (int a = weakEntriesOfStrongType.Count; a-- > 0; )
                {
                    DefEntry<V> defEntry = weakEntriesOfStrongType[a];
                    Type registeredType = defEntry.type;

                    Object value = typeToDefEntryMap.Get(registeredType);
                    InterfaceFastList<DefEntry<V>> list = (InterfaceFastList<DefEntry<V>>)value;
                    list.Remove(defEntry);
                    if (list.Count == 0)
                    {
                        typeToDefEntryMap.Remove(registeredType);
                    }
                    TypeToDefEntryMapChanged(classEntry, registeredType);
                }
                this.classEntry = classEntry;
            }
        }

        protected void TypeToDefEntryMapChanged(ClassEntry<V> classEntry, Type key)
	    {
		    Object obj = classEntry.typeToDefEntryMap.Get(key);
		    if (obj == null)
		    {
			    classEntry.Remove(key);
			    return;
		    }
		    if (obj == alreadyHandled)
		    {
			    classEntry.Put(key, alreadyHandled);
			    return;
		    }
		    if (obj is DefEntry<V>)
		    {
			    classEntry.Put(key, ((DefEntry<V>) obj).extension);
			    return;
		    }
		    DefEntry<V> firstDefEntry = ((InterfaceFastList<DefEntry<V>>) obj).First.ElemValue;
		    classEntry.Put(key, firstDefEntry.extension);
	    }

        protected bool AppendRegistration(Type strongType, Type type, V extension, int distance, ClassEntry<V> classEntry)
        {
            HashMap<Type, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
            Object fastList = typeToDefEntryMap.Get(type);
            if (fastList != null && !Object.ReferenceEquals(fastList, alreadyHandled))
            {
                IListElem<DefEntry<V>> pointer = ((InterfaceFastList<DefEntry<V>>)fastList).First;
                while (pointer != null)
                {
                    DefEntry<V> existingDefEntry = pointer.ElemValue;
                    if (Object.ReferenceEquals(existingDefEntry.extension, extension) && existingDefEntry.distance == distance)
                    {
                        // DefEntry already exists with same distance
                        return false;
                    }
                    pointer = pointer.Next;
                }
            }
            if (fastList == null || Object.ReferenceEquals(fastList, alreadyHandled))
            {
                fastList = new InterfaceFastList<DefEntry<V>>();
                typeToDefEntryMap.Put(type, fastList);
            }
            DefEntry<V> defEntry = new DefEntry<V>(extension, type, distance);

            HashMap<StrongKey<V>, List<DefEntry<V>>> definitionReverseMap = classEntry.definitionReverseMap;
            StrongKey<V> strongKey = new StrongKey<V>(extension, strongType);
            List<DefEntry<V>> typeEntries = definitionReverseMap.Get(strongKey);
            if (typeEntries == null)
            {
                typeEntries = new List<DefEntry<V>>();
                definitionReverseMap.Put(strongKey, typeEntries);
            }
            typeEntries.Add(defEntry);

            InterfaceFastList<DefEntry<V>>.InsertOrdered((InterfaceFastList<DefEntry<V>>)fastList, defEntry);
            TypeToDefEntryMapChanged(classEntry, type);
            return true;
        }
    }
}