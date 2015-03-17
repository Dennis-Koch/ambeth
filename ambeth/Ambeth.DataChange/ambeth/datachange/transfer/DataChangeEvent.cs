using System;
using System.Net;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Ambeth.Datachange.Transfer
{
    [DataContract(Name = "DataChangeEvent", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class DataChangeEvent : IDataChange
    {
        protected static readonly IList<IDataChangeEntry> EmptyList = new IDataChangeEntry[0];


        public static DataChangeEvent Create(int insertCount, int updateCount, int deleteCount)
        {
            DataChangeEvent dce = new DataChangeEvent();
            dce.IsLocalSource = true;
            dce.ChangeTime = DateTime.Now;
            dce.Deletes = CreateList(deleteCount);
            dce.Updates = CreateList(updateCount);
            dce.Inserts = CreateList(insertCount);
            return dce;
        }

        protected static IList<IDataChangeEntry> CreateList(int size)
        {
            if (size == -1)
            {
                return new List<IDataChangeEntry>();
            }
            else if (size == 0)
            {
                return EmptyList;
            }
            return new List<IDataChangeEntry>(size);
        }

        [DataMember]
        public DateTime ChangeTime { get; set; }

        [IgnoreDataMember]
        protected IList<IDataChangeEntry> deletes;

        [DataMember]
        public IList<IDataChangeEntry> Deletes
        {
            get
            {
                if (deletes == null)
                {
                    deletes = new List<IDataChangeEntry>(0);
                }
                return deletes;
            }
            set
            {
                deletes = value;
            }
        }

        [IgnoreDataMember]
        protected IList<IDataChangeEntry> updates;

        [DataMember]
        public IList<IDataChangeEntry> Updates
        {
            get
            {
                if (updates == null)
                {
                    updates = new List<IDataChangeEntry>(0);
                }
                return updates;
            }
            set
            {
                updates = value;
            }
        }

        [IgnoreDataMember]
        protected IList<IDataChangeEntry> inserts;

        [DataMember]
        public IList<IDataChangeEntry> Inserts
        {
            get
            {
                if (inserts == null)
                {
                    inserts = new List<IDataChangeEntry>(0);
                }
                return inserts;
            }
            set
            {
                inserts = value;
            }
        }

        [IgnoreDataMember]
        public bool IsLocalSource { get; set; }

        [IgnoreDataMember]
        protected IList<IDataChangeEntry> all;

        [IgnoreDataMember]
        public IList<IDataChangeEntry> All
        {
            get
            {
                if (all == null)
                {
                    List<IDataChangeEntry> allList = new List<IDataChangeEntry>();
                    allList.AddRange(Inserts);
                    allList.AddRange(Updates);
                    allList.AddRange(Deletes);
                    all = allList;
                }
                return all;
            }
        }

        [IgnoreDataMember]
        public bool IsEmpty
        {
            get
            {
                return Deletes.Count == 0 && Updates.Count == 0 && Inserts.Count == 0;
            }
        }
        
        public DataChangeEvent()
        {
            // Intended blank
        }

        public DataChangeEvent(IList<IDataChangeEntry> inserts, IList<IDataChangeEntry> updates, IList<IDataChangeEntry> deletes, DateTime changeTime,
            bool isLocalSource)
        {
            this.Inserts = inserts;
            this.Updates = updates;
            this.Deletes = deletes;
            this.ChangeTime = changeTime;
            this.IsLocalSource = isLocalSource;
        }

        public bool IsEmptyByType(Type entityType)
	    {
		    IList<IDataChangeEntry> entries = Inserts;
		    for (int a = entries.Count; a-- > 0;)
		    {
			    if (entries[a].EntityType.IsAssignableFrom(entityType))
			    {
				    return false;
			    }
		    }
		    entries = Updates;
            for (int a = entries.Count; a-- > 0; )
		    {
                if (entries[a].EntityType.IsAssignableFrom(entityType))
			    {
				    return false;
			    }
		    }
		    entries = Deletes;
            for (int a = entries.Count; a-- > 0; )
		    {
                if (entries[a].EntityType.IsAssignableFrom(entityType))
			    {
				    return false;
			    }
		    }
		    return true;
	    }

        public IDataChange Derive(params Type[] interestedEntityTypes)
        {
            return DeriveIntern(interestedEntityTypes, false);
        }

        public IDataChange DeriveNot(params Type[] uninterestingEntityTypes)
        {
            return DeriveIntern(uninterestingEntityTypes, false);
        }

        public IDataChange Derive(params Object[] interestedEntityIds)
        {
            ISet<Object> interestedEntityIdsSet = new HashSet<Object>();
            interestedEntityIdsSet.UnionWith(interestedEntityIds);

            IList<IDataChangeEntry> derivedInserts = BuildDerivedIds(Inserts, interestedEntityIdsSet);
            IList<IDataChangeEntry> derivedUpdates = BuildDerivedIds(Updates, interestedEntityIdsSet);
            IList<IDataChangeEntry> derivedDeletes = BuildDerivedIds(Deletes, interestedEntityIdsSet);

            return new DataChangeEvent(derivedInserts, derivedUpdates, derivedDeletes, ChangeTime, IsLocalSource);
        }

        protected IDataChange DeriveIntern(Type[] entityTypes, bool reverse)
        {
            ISet<Type> entityTypesSet = new HashSet<Type>(entityTypes);

            IList<IDataChangeEntry> derivedInserts = BuildDerivedTypes(inserts, entityTypesSet, reverse);
            IList<IDataChangeEntry> derivedUpdates = BuildDerivedTypes(updates, entityTypesSet, reverse);
            IList<IDataChangeEntry> derivedDeletes = BuildDerivedTypes(deletes, entityTypesSet, reverse);

            return new DataChangeEvent(derivedInserts, derivedUpdates, derivedDeletes, ChangeTime, IsLocalSource);
        }

        protected IList<IDataChangeEntry> BuildDerivedTypes(IList<IDataChangeEntry> sourceEntries, ISet<Type> entityTypes, bool reverse)
        {
            List<IDataChangeEntry> targetEntries = new List<IDataChangeEntry>();
            foreach (IDataChangeEntry entry in sourceEntries)
            {
                Type currentType = entry.EntityType;
                while (currentType != null)
                {
                    bool include = entityTypes.Contains(currentType) ^ reverse;
                    if (include)
                    {
                        targetEntries.Add(entry);
                        break;
                    }
                    currentType = currentType.BaseType;
                    if (reverse && currentType.Equals(typeof(Object)))
                    {
                        break;
                    }
                }
            }
            return targetEntries;
        }

        protected IList<IDataChangeEntry> BuildDerivedIds(IList<IDataChangeEntry> sourceEntries, ISet<Object> interestedEntityIds)
        {
            List<IDataChangeEntry> targetEntries = new List<IDataChangeEntry>();
            foreach (IDataChangeEntry entry in sourceEntries)
            {
                if (interestedEntityIds.Contains(entry.Id))
                {
                    targetEntries.Add(entry);
                }
            }
            return targetEntries;
        }
    }
}
