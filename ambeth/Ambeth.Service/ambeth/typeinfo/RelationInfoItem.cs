using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Config;
using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public abstract class RelationInfoItem : TypeInfoItem, IRelationInfoItem
    {
        protected CascadeLoadMode cascadeLoadMode = CascadeLoadMode.DEFAULT;

        protected bool manyTo;

        protected bool toMany;

        public void Configure(IProperties properties)
        {
            toMany = !ElementType.Equals(RealType);

            CascadeAttribute cascadeAnnotation = GetAnnotation<CascadeAttribute>();
            if (cascadeAnnotation != null)
            {
                cascadeLoadMode = cascadeAnnotation.Load;
            }
            if (CascadeLoadMode.DEFAULT.Equals(cascadeLoadMode))
            {
                cascadeLoadMode = (CascadeLoadMode)Enum.Parse(typeof(CascadeLoadMode), properties.GetString(toMany ? ServiceConfigurationConstants.ToManyDefaultCascadeLoadMode
                        : ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, CascadeLoadMode.DEFAULT.ToString()), false);
            }
            if (CascadeLoadMode.DEFAULT.Equals(cascadeLoadMode))
            {
                cascadeLoadMode = toMany ? CascadeLoadMode.LAZY : CascadeLoadMode.EAGER_VERSION;
            }
        }

        public CascadeLoadMode CascadeLoadMode
        {
            get
            {
                return cascadeLoadMode;
            }
        }

        public bool IsManyTo
        {
            get
            {
                throw new NotSupportedException();
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public bool IsToMany
        {
            get
            {
                return toMany;
            }
        }
    }
}