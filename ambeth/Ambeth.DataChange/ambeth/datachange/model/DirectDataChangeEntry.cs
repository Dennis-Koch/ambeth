using System;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Ambeth.Datachange.Model
{
    public class DirectDataChangeEntry : IDataChangeEntry
    {
        public Object Entry { get; protected set; }

        public DirectDataChangeEntry(Object entry)
        {
            this.Entry = entry;
        }

        public Type EntityType
        {
            get
            {
                return Entry.GetType();
            }
        }

        public Object Id
        {
            get
            {
                return null;
            }
        }

        public sbyte IdNameIndex
        {
            get
            {
                return -1;
            }
        }

        public Object Version
        {
            get
            {
                return null;
            }
        }

        public String[] Topics
        {
            get
            {
                return null;
            }
            set
            {
            }
        }
    }
}
