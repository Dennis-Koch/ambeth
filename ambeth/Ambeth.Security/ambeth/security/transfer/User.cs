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
    public partial class User : Observable
    {
        #region Properties
        protected string nameField;
        [DataMember]
        public virtual string Name
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

        public virtual string FirstName
        {
            get
            {
                if (Name == null)
                {
                    return null;
                }
                int index = Name.IndexOf(' ');
                if (index != -1)
                {
                    return Name.Substring(0, index);
                }
                return null;
            }
        }

        public virtual string LastNname
        {
            get
            {
                if (Name == null)
                {
                    return null;
                }
                int index = Name.IndexOf(' ');
                if (index != -1)
                {
                    return Name.Substring(index + 1);
                }
                return Name;
            }
        }

        public virtual string ShortName
        {
            get
            {
                String firstName = FirstName;
                String lastName = LastNname;
                return (!String.IsNullOrWhiteSpace(firstName) ? Char.ToUpper(firstName[0]) + "" : "")
                    + (!String.IsNullOrWhiteSpace(lastName) ? Char.ToUpper(lastName[0]) + "" : "");
            }
        }

        protected bool aktivUserField;
        [DataMember]
        public virtual bool AktivUser
        {
            get { return aktivUserField; }
            set
            {
                if (!object.ReferenceEquals(aktivUserField, value))
                {
                    aktivUserField = value;
                    RaisePropertyChanged("AktivUser");
                }
            }
        }

        protected bool systemUserFlag;
        [DataMember]
        public virtual bool SystemUserFlag
        {
            get { return systemUserFlag; }
            set
            {
                if (!object.ReferenceEquals(systemUserFlag, value))
                {
                    systemUserFlag = value;
                    RaisePropertyChanged("SystemUserFlag");
                }
            }
        }
        
        protected string sidField;
        [DataMember]
        public virtual string SID
        {
            get { return sidField; }
            set
            {
                if (!object.ReferenceEquals(sidField, value))
                {
                    sidField = value;
                    RaisePropertyChanged("SID");
                }
            }
        }

        protected string winuserField;
        [DataMember]
        public virtual string Winuser
        {
            get { return winuserField; }
            set
            {
                if (!object.ReferenceEquals(winuserField, value))
                {
                    winuserField = value;
                    RaisePropertyChanged("Winuser");
                }
            }
        }
        protected string emailField;
        [DataMember]
        public virtual string Email
        {
            get { return emailField; }
            set
            {
                if (!object.ReferenceEquals(emailField, value))
                {
                    emailField = value;
                    RaisePropertyChanged("Email");
                }
            }
        }

        protected IList<Role> roles;
        [DataMember]
        [Cascade(Load = CascadeLoadMode.EAGER)]
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

        protected IList<Site> sites;
        [DataMember]
        [Cascade(Load = CascadeLoadMode.EAGER)]
        public virtual IList<Site> Sites
        {
            get { return sites; }
            set
            {
                if (!object.ReferenceEquals(sites, value))
                {
                    sites = value;
                    RaisePropertyChanged("Sites");
                }
            }
        }

        protected IList<Country> countries;
        [DataMember]
        [Cascade(Load = CascadeLoadMode.EAGER)]
        public virtual IList<Country> Countries
        {
            get { return countries; }
            set
            {
                if (!object.ReferenceEquals(countries, value))
                {
                    countries = value;
                    RaisePropertyChanged("Countries");
                }
            }
        }
        #endregion

        #region Propertie Method

        #region Roles
        public virtual void AddRole(Role role)
        {
            if (AddRoleIntern(role))
            {
                role.AddUserIntern(this);
            }
        }

        public virtual void RemoveRole(Role role)
        {
            RemoveRoleIntern(role);
            role.RemoveUserIntern(this);
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

        #region Sites
        //public virtual void AddSite(Site site)
        //{
        //    if (AddSiteIntern(site))
        //    {
        //        site.AddUserIntern(this);
        //    }
        //}

        //public virtual void RemoveSite(Site site)
        //{
        //    RemoveSiteIntern(site);
        //    site.RemoveUserIntern(this);
        //}

        public virtual bool AddSiteIntern(Site site)
        {
            if (Sites == null)
            {
                Sites = new ObservableCollection<Site>();
            }
            if (Sites.Contains(site))
            {
                return false;
            }
            Sites.Add(site);
            return true;
        }

        public virtual void RemoveSiteIntern(Site site)
        {
            if (Sites != null)
            {
                Sites.Remove(site);
            }
        }
        #endregion

        #region Country

        public virtual bool AddCountryIntern(Country country)
        {
            if (Countries == null)
            {
                Countries = new ObservableCollection<Country>();
            }
            if (Countries.Contains(country))
            {
                return false;
            }
            Countries.Add(country);
            return true;
        }

        public virtual void RemoveCountryIntern(Country country)
        {
            if (Countries != null)
            {
                Countries.Remove(country);
            }
        }
        #endregion
        #endregion

    }
}
