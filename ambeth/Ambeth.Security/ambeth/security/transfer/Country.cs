using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using System.Collections.ObjectModel;

namespace De.Osthus.Ambeth.Security.Transfer
{
    //[DataContract(Namespace = "http://schemas.oerlikon.com/Texis.Masterdata")]
    public partial class Country : Observable
    {
        protected String nameField;
        [DataMember]
        virtual public string Name
        {
            get { return nameField; }
            set
            {
                if (!object.ReferenceEquals(nameField, value))
                {
                    nameField = value;
                    RaisePropertyChanged("Name");
                }
            }
        }

        protected String description;
        [DataMember]
        virtual public string Description
        {
            get { return description; }
            set
            {
                if (!object.ReferenceEquals(description, value))
                {
                    description = value;
                    RaisePropertyChanged("Description");
                }
            }
        }
    }
}
