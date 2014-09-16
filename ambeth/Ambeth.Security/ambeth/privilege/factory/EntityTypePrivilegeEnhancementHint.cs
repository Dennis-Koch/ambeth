using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Privilege.Model.Impl;
using System;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public class EntityTypePrivilegeEnhancementHint : IEnhancementHint, ITargetNameEnhancementHint
    {
        protected readonly Type entityType;

        protected readonly bool? create, read, update, delete, execute;

        public EntityTypePrivilegeEnhancementHint(Type entityType, bool? create, bool? read, bool? update, bool? delete, bool? execute)
        {
            this.entityType = entityType;
            this.create = create;
            this.read = read;
            this.update = update;
            this.delete = delete;
            this.execute = execute;
        }

        public Type EntityType
        {
            get
            {
                return entityType;
            }
        }

        public bool? Create
        {
            get
            {
                return create;
            }
        }

        public bool? Read
        {
            get
            {
                return read;
            }
        }

        public bool? Update
        {
            get
            {
                return update;
            }
        }

        public bool? Delete
        {
            get
            {
                return delete;
            }
        }

        public bool? Execute
        {
            get
            {
                return execute;
            }
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is EntityTypePrivilegeEnhancementHint))
            {
                return false;
            }
            EntityTypePrivilegeEnhancementHint other = (EntityTypePrivilegeEnhancementHint)obj;
            return EntityType.Equals(other.EntityType) && Create == other.Create && Read == other.Read && Update == other.Update
                    && Delete == other.Delete && Execute == other.Execute;
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ EntityType.GetHashCode() ^ TypePropertyPrivilegeImpl.ToBitValue(Create, Read, Update, Delete, Execute);
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public Object Unwrap(Type includedHintType)
        {
            if (typeof(EntityTypePrivilegeEnhancementHint).Equals(includedHintType))
            {
                return this;
            }
            return null;
        }

        public string GetTargetName(Type typeToEnhance)
        {
            return entityType.FullName + "$" + typeof(AbstractTypePrivilege).Name + "_" + AbstractPrivilege.upperOrLower(create, 'c')
                        + AbstractPrivilege.upperOrLower(read, 'r') + AbstractPrivilege.upperOrLower(update, 'u') + AbstractPrivilege.upperOrLower(delete, 'd')
                        + AbstractPrivilege.upperOrLower(execute, 'e');
        }
    }
}