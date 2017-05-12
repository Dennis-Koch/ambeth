using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using System.Collections.ObjectModel;

namespace De.Osthus.Ambeth.Security.Transfer
{
    //[DataContract(Namespace = "http://schemas.oerlikon.com/Texis.Masterdata")]
    public partial class UseCase:Observable
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

        
        protected string typeField;
        [DataMember]
        virtual public string Type
        {
            get { return typeField; }
            set
            {
                if (!object.ReferenceEquals(typeField, value))
                {
                    descField = value;
                    RaisePropertyChanged("Type");
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

        protected IList<Scenario> scenarios;
        [DataMember]
        virtual public IList<Scenario> Scenarios
        {
            get { return scenarios; }
            set
            {
                if (!object.ReferenceEquals(scenarios, value))
                {
                    scenarios = value;
                    RaisePropertyChanged("Scenarios");
                }
            }
        }

        #region Propertie Method

        public virtual bool AddScenarioIntern(Scenario scenario)
        {
            if (Scenarios == null)
            {
                Scenarios = new ObservableCollection<Scenario>();
            }

            if (Scenarios.Contains(scenario))
            {
                return false;
            }
            Scenarios.Add(scenario);
            return true;
        }

        public virtual void AddScenario(Scenario scenario)
        {
            if (AddScenarioIntern(scenario))
            {
                scenario.AddUseCaseIntern(this);
            }
        }

        public virtual void RemoveScenarioIntern(Scenario scenario)
        {
            if (Scenarios != null)
                Scenarios.Remove(scenario);
        }

        public virtual void RemoveScenario(Scenario scenario)
        {
            RemoveScenarioIntern(scenario);
            scenario.RemoveUseCaseIntern(this);
        }

        // Roles
        public virtual void AddRole(Role role)
        {
            if (AddRoleIntern(role))
            {
                role.AddUseCaseIntern(this);
            }
        }

        public virtual void RemoveRole(Role role)
        {
            RemoveRoleIntern(role);
            role.RemoveUseCaseIntern(this);
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
