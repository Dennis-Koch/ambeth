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
    public partial class Role: Observable
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

        protected IList<User> users;
        [DataMember]
        virtual public IList<User> Users
        {
            get { return users; }
            set
            {
                if (!object.ReferenceEquals(users, value))
                {
                    users = value;
                    RaisePropertyChanged("Users");
                }
            }
        }

        protected IList<UseCase> useCases;
        [DataMember]
        [Cascade(Load = CascadeLoadMode.EAGER)]
        virtual public IList<UseCase> UseCases
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

        protected IList<Scenario> scenarios;
        [DataMember]
        [Cascade(Load = CascadeLoadMode.EAGER)]
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

        // Users
        public virtual void AddUser(User user)
        {
            if (AddUserIntern(user))
            {
                user.AddRoleIntern(this);
            }
        }

        public virtual void RemoveUser(User user)
        {
            RemoveUserIntern(user);
            user.RemoveRoleIntern(this);
        }

        public virtual bool AddUserIntern(User user)
        {
            if (Users == null)
            {
                Users = new ObservableCollection<User>();
            }
            if (Users.Contains(user))
            {
                return false;
            }
            Users.Add(user);
            return true;
        }

        public virtual void RemoveUserIntern(User user)
        {
            if (Users != null)
            {
                Users.Remove(user);
            }
        }

        // Scenarios
        public virtual void AddScenario(Scenario scenario)
        {
            if (AddScenarioIntern(scenario))
            {
                //ToDo
            }
        }

        public bool AddScenarioIntern(Scenario scenario)
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
        public virtual void RemoveScenarioIntern(Scenario scenario)
        {
            if (Scenarios != null)
            {
                Scenarios.Remove(scenario);
            }
        }

        public virtual void RemoveScenario(Scenario scenario)
        {
            RemoveScenarioIntern(scenario);
            scenario.RemoveRoleIntern(this);
        }

        // UseCases
        public virtual void AddUseCase(UseCase useCase)
        {
            if (AddUseCaseIntern(useCase))
            {
                useCase.AddRoleIntern(this);
            }

        }

        public bool AddUseCaseIntern(UseCase useCase)
        {
            if (UseCases == null)
            {
                UseCases = new ObservableCollection<UseCase>();
            }
            if (UseCases.Contains(useCase))
            {
                return false;
            }
            UseCases.Add(useCase);
            return true;
        }

        public virtual void RemoveUseCaseIntern(UseCase useCase)
        {
            if (UseCases != null)
            {
                UseCases.Remove(useCase);
            }
        }

        public virtual void RemoveUseCase(UseCase useCase)
        {
            RemoveUseCaseIntern(useCase);
            useCase.RemoveRoleIntern(this);
        }


        
        #endregion
    }
}
