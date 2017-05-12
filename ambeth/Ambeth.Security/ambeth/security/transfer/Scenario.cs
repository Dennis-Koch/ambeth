using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using System.Collections.ObjectModel;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Security.Transfer
{
    //[DataContract(Namespace = "http://schemas.oerlikon.com/Texis.Masterdata")]
    public partial class Scenario : Observable
    {
        protected string nameField;
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

        protected string descField;
        [DataMember]
        virtual public string Description
        {
            get { return descField; }
            set
            {
                if (!object.ReferenceEquals(descField, value))
                {
                    descField = value;
                    RaisePropertyChanged("Description");
                }
            }
        }
        protected IList<Role> roles;
        [DataMember]
        public virtual IList<Role> Roles
        {
            get { return roles; }
            set
            {
                if (!object.ReferenceEquals(roles, value))
                {
                    roles = value;
                    RaisePropertyChanged("Roles");
                }
            }
        }

        protected IList<UseCase> useCases;
        [DataMember]
        [Cascade(Load = CascadeLoadMode.EAGER)]
        public virtual IList<UseCase> UseCases
        {
            get { return useCases; }
            set
            {
                if (!object.ReferenceEquals(useCases, value))
                {
                    useCases = value;
                    RaisePropertyChanged("UseCases");
                }
            }
        }

        #region Propertie Method
        //Technish UseCases
        public virtual bool AddUseCaseIntern(UseCase usecase)
        {
            if (UseCases == null)
            {
                UseCases = new ObservableCollection<UseCase>();
            }

            if (UseCases.Contains(usecase))
            {
                return false;
            }
            UseCases.Add(usecase);
            return true;
        }

        public virtual void AddUseCase(UseCase usecase)
        {
            if (AddUseCaseIntern(usecase))
            {
                usecase.AddScenarioIntern(this);
            }
        }

        public virtual void RemoveUseCaseIntern(UseCase usecase)
        {
            if (UseCases != null)
                UseCases.Remove(usecase);
        }

        public virtual void RemoveUseCase(UseCase usecase)
        {
            RemoveUseCaseIntern(usecase);
            usecase.RemoveScenarioIntern(this);
        }

        // Roles
        public virtual void AddRole(Role role)
        {
            if (AddRoleIntern(role))
            {
                role.AddScenarioIntern(this);
            }
        }

        public virtual void RemoveRole(Role role)
        {
            RemoveRoleIntern(role);
            role.RemoveScenarioIntern(this);
        }

        public virtual bool AddRoleIntern(Role role)
        {
            if (Roles == null)
            {
                Roles = new ObservableCollection<Role>();
            }
            if (Roles.Contains(role))
            {
                return false;
            }
            Roles.Add(role);
            return true;
        }

        public virtual void RemoveRoleIntern(Role role)
        {
            if (Roles != null)
            {
                Roles.Remove(role);
            }
        }
        #endregion

    }
}
