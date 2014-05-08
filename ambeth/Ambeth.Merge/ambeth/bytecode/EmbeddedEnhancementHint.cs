using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public sealed class EmbeddedEnhancementHint : ITargetNameEnhancementHint
    {
        public static bool HasMemberPath(IEnhancementHint hint)
	    {
		    return GetMemberPath(hint) != null;
	    }

	    public static Type GetParentObjectType(IEnhancementHint hint)
	    {
		    if (hint == null)
		    {
			    return null;
		    }
		    EmbeddedEnhancementHint unwrap = hint.Unwrap<EmbeddedEnhancementHint>();
		    if (unwrap == null)
		    {
			    return null;
		    }
		    return unwrap.ParentObjectType;
	    }

        public static Type GetRootEntityType(IEnhancementHint hint)
        {
            if (hint == null)
            {
                return null;
            }
            EmbeddedEnhancementHint unwrap = hint.Unwrap<EmbeddedEnhancementHint>();
            if (unwrap == null)
            {
                return null;
            }
            return unwrap.RootEntityType;
        }

	    public static String GetMemberPath(IEnhancementHint hint)
	    {
		    if (hint == null)
		    {
			    return null;
		    }
		    EmbeddedEnhancementHint unwrap = hint.Unwrap<EmbeddedEnhancementHint>();
		    if (unwrap == null)
		    {
			    return null;
		    }
		    return unwrap.MemberPath;
	    }

        private readonly Type parentObjectType;

        private readonly Type rootEntityType;

        private readonly String memberPath;

        public EmbeddedEnhancementHint(Type rootEntityType, Type parentObjectType, String memberPath)
        {
            this.rootEntityType = rootEntityType;
            this.parentObjectType = parentObjectType;
            this.memberPath = memberPath;
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public Object Unwrap(Type includedContextType)
        {
            if (typeof(EmbeddedEnhancementHint).IsAssignableFrom(includedContextType))
            {
                return this;
            }
            return null;
        }

        public Type ParentObjectType
        {
            get
            {
                return parentObjectType;
            }
        }

        public Type RootEntityType
        {
            get
            {
                return rootEntityType;
            }
        }


        public String MemberPath
        {
            get
            {
                return memberPath;
            }
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is EmbeddedEnhancementHint))
            {
                return false;
            }
            EmbeddedEnhancementHint other = (EmbeddedEnhancementHint)obj;
            return ParentObjectType.Equals(other.ParentObjectType) && MemberPath.Equals(other.MemberPath);
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ ParentObjectType.GetHashCode() ^ MemberPath.GetHashCode();
        }

        public String GetTargetName(Type typeToEnhance)
        {
            if (MemberPath != null && MemberPath.Length > 0)
            {
                return typeToEnhance.FullName + "_" + MemberPath;
            }
            return typeToEnhance.FullName;
        }

        public override string ToString()
        {
            return GetType().Name + " Root: " + RootEntityType.Name + ", Path: " + MemberPath + ", Parent: " + ParentObjectType.Name;
        }
    }
}
