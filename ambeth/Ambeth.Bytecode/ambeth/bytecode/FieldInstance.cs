using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode
{
    public class FieldInstance
	{
        protected FieldInfo field;

		protected readonly NewType owner;

		protected readonly FieldAttributes access;

        protected readonly String name;

		protected readonly NewType type;
        
        public FieldInstance(FieldInfo field)
            : this(NewType.GetType(field.DeclaringType), field.Attributes, field.Name, NewType.GetType(field.FieldType))
		{
            this.field = field;
		}

		public FieldInstance(Type owner, FieldAttributes access, String name, Type type) : this(NewType.GetType(owner), access, name, NewType.GetType(type))
		{
		    // Intended blank
		}
        
        public FieldInstance(NewType owner, FieldAttributes access, String name, Type type)
            : this(owner, access, name, NewType.GetType(type))
		{
		    // Intended blank
		}

        public FieldInstance(FieldAttributes access, String name, Type type) : this(BytecodeBehaviorState.State.NewType, access, name, type)
        {
            // Intended blank
        }

        public FieldInstance(NewType owner, FieldAttributes access, String name, NewType type)
		{
			this.owner = (owner != null ? owner : BytecodeBehaviorState.State.NewType);
			this.access = access;
			this.name = name;
			this.type = type;
		}

        public FieldInstance(FieldAttributes access, String name, NewType type)
            : this(BytecodeBehaviorState.State.NewType, access, name, type)
		{
		    // Intended blank
		}

        public FieldInfo Field
        {
            get
            {
                return field;
            }
        }
        
		public NewType Owner
		{
            get
            {
			    return owner;
            }
		}

        public FieldAttributes Access
		{
            get
            {
			    return access;
            }
		}

		public String Name
		{
            get
            {
			    return name;
            }
		}

		public NewType Type
		{
            get
            {
			    return type;
            }
		}
        
		public override String ToString()
		{
			StringBuilder sb = new StringBuilder();
            if (access.HasFlag(FieldAttributes.Public))
            {
                sb.Append("public ");
            }
            else if (access.HasFlag(FieldAttributes.Family))
            {
                sb.Append("protected ");
            }
            else if (access.HasFlag(FieldAttributes.Private))
            {
                sb.Append("private ");
            }
            if (access.HasFlag(FieldAttributes.Static))
            {
                sb.Append("static ");
            }
            if (access.HasFlag(FieldAttributes.InitOnly))
            {
                sb.Append("final ");
            }
            sb.Append(Type.ClassName).Append(' ');
			if (owner != null)
			{
				sb.Append(owner.ClassName).Append('.');
			}
			sb.Append(name);
			return sb.ToString();
		}
	}
}
