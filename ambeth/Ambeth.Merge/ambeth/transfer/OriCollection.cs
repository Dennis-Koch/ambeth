using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "OriCollection", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class OriCollection : IOriCollection
    {
#if !SILVERLIGHT
        [NonSerialized]
#endif
        [IgnoreDataMember]
        protected IDictionary<Type, IList<IObjRef>> typeToOriDict;

        [DataMember]
        public IList<IObjRef> AllChangeORIs { get; set; }

        [DataMember(IsRequired = false)]
        public long? ChangedOn { get; set; }

        [DataMember(IsRequired = false)]
        public String ChangedBy { get; set; }

        [DataMember(IsRequired = false)]
        public long[] AllChangedOn { get; set; }

        [DataMember(IsRequired = false)]
        public String[] AllChangedBy { get; set; }

        [IgnoreDataMember]
        public Object HardRefs { get; set; }

        [IgnoreDataMember]
        public IDataChange PendingDataChange { get; set; }

        public OriCollection()
        {
        }

        public OriCollection(IList<IObjRef> oriList)
        {
            this.AllChangeORIs = oriList;
        }

        public IList<IObjRef> GetChangeRefs(Type type)
        {
            if (typeToOriDict != null)
            {
                return DictionaryExtension.ValueOrDefault(typeToOriDict, type);
            }
            typeToOriDict = new Dictionary<Type, IList<IObjRef>>();

            for (int a = AllChangeORIs.Count; a-- > 0; )
            {
                IObjRef ori = AllChangeORIs[a];
                Type realType = ori.RealType;
                IList<IObjRef> modList = DictionaryExtension.ValueOrDefault(typeToOriDict, realType);
                if (modList == null)
                {
                    modList = new List<IObjRef>();
                    typeToOriDict.Add(realType, modList);
                }
                modList.Add(ori);
            }
            return DictionaryExtension.ValueOrDefault(typeToOriDict, type);
        }
    }
}
