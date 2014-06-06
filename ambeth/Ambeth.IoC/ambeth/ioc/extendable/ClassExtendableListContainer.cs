using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using System;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class ClassExtendableListContainer<V> : ClassExtendableContainer<V>
    {
        public ClassExtendableListContainer(String message, String keyMessage)
            : base(message, keyMessage, true)
        {
            // Intended blank
        }

        public override V GetExtension(Type key)
        {
            return GetExtensions(key)[0];
        }

        public override IList<V> GetExtensions(Type key)
        {
            if (key == null)
            {
                return EmptyList.Empty<V>();
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
                            return EmptyList.Empty<V>();
                        }
                    }
                }
            }
            if (Object.ReferenceEquals(extension, alreadyHandled))
            {
                // Already tried
                return EmptyList.Empty<V>();
            }
            return (IList<V>)extension;
        }

        protected override void TypeToDefEntryMapChanged(ClassEntry<V> classEntry, Type key)
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
            Object existingItem = classEntry.Get(key);
            List<V> list = (List<V>)(existingItem == alreadyHandled ? null : existingItem);
            if (list == null)
            {
                list = new List<V>();
                classEntry.Put(key, list);
            }
            if (obj is DefEntry<V>)
            {
                V extension = ((DefEntry<V>)obj).extension;
                if (!list.Contains(extension))
                {
                    list.Add(extension);
                }
                return;
            }
            IListElem<DefEntry<V>> pointer = ((InterfaceFastList<DefEntry<V>>)obj).First;
            while (pointer != null)
            {
                V extension = pointer.ElemValue.extension;
                if (!list.Contains(extension))
                {
                    list.Add(extension);
                }
                pointer = pointer.Next;
            }
        }
    }
}