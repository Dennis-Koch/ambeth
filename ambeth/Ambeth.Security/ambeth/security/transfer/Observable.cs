using System.ComponentModel;
using System.Runtime.Serialization;
using System;

namespace De.Osthus.Ambeth.Security.Transfer
{
    //[DataContract(IsReference = true)]
    public class Observable : INotifyPropertyChanged
    {
        [DataMember]
        public virtual int Id { get; set; }

        [IgnoreDataMember]
        public virtual String CreatedBy { get; set; }

        [IgnoreDataMember]
        public virtual DateTime? CreatedOn { get; set; }

        [IgnoreDataMember]
        public virtual String UpdatedBy { get; set; }

        [IgnoreDataMember]
        public virtual DateTime? UpdatedOn { get; set; }

        [DataMember]
        public virtual int Version { get; set; }

        public virtual event PropertyChangedEventHandler PropertyChanged;

        protected void RaisePropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }
    }
}
