using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;

namespace De.Osthus.Minerva.Core
{
    public class ObjectRelationClientFilter<RelationType, FilterType> : IClientFilter<FilterType>, IInitializingBean, IDisposableBean
    {
        protected ITypeInfoItem typeInfoItem;

        public event EventHandler ClientFilterChanged;

        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }

        public Type EntityType { protected get; set; }

        public String FilterMember { protected get; set; }

        protected IModelSingleContainer<RelationType> modelContainer;
        public virtual IModelSingleContainer<RelationType> ModelContainer
        {
            get
            {
                return modelContainer;
            }
            set
            {
                if (Object.ReferenceEquals(value, ModelContainer))
                {
                    return;
                }
                if (modelContainer != null)
                {
                    modelContainer.PropertyChanged -= SelectedEntityChanged;
                }
                modelContainer = value;
                if (modelContainer != null)
                {
                    modelContainer.PropertyChanged += SelectedEntityChanged;
                }
                SelectedEntityChanged(modelContainer, null);
            }
        }

        [Autowired]
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        protected Object filterValue;
        public virtual Object FilterValue
        {
            get
            {
                return filterValue;
            }
            set
            {
                if (Object.ReferenceEquals(filterValue, value))
                {
                    return;
                }
                filterValue = value;
                OnClientFilterChanged();
            }
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(EntityType, "EntityType");
            ParamChecker.AssertNotNull(FilterMember, "FilterMember");

            // It is intentended to have no null check for ModelContainer, because the class can be used without it!

            typeInfoItem = TypeInfoProvider.GetHierarchicMember(EntityType, FilterMember);
        }

        public void Destroy()
        {
            if (modelContainer != null)
            {
                modelContainer.PropertyChanged -= SelectedEntityChanged;
            }
        }

        protected virtual void OnClientFilterChanged()
        {
            if (ClientFilterChanged != null)
            {
                ClientFilterChanged.Invoke(this, EventArgs.Empty);
            }
        }

        public virtual IList<FilterType> Filter(IList<FilterType> bOsToFilter)
        {
            if (FilterValue == null)
            {
                return bOsToFilter;
            }
            IList<FilterType> result = new List<FilterType>(bOsToFilter.Count);

            PrefetchHelper.CreatePrefetch().Add(EntityType, FilterMember).Build().Prefetch(bOsToFilter);

            // Now we are safe to use potential valueholders within a loop without a performance hit
            foreach (FilterType bO in bOsToFilter)
            {
                Object relation = typeInfoItem.GetValue(bO);
                if (Object.Equals(relation, FilterValue))
                {
                    result.Add(bO);
                }
            }

            return result;
        }

        public virtual void SelectedEntityChanged(Object sender, PropertyChangedEventArgs e)
        {
            IModelSingleContainer<RelationType> container = (IModelSingleContainer<RelationType>)sender;

            if (container != null)
            {
                FilterValue = container.Value;
            }
            else
            {
                FilterValue = null;
            }
        }
    }
}
