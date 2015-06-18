using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class Modifier
    {
		public static readonly int PUBLIC = 0x1;

		public static readonly int PRIVATE = 0x2;

		public static readonly int PROTECTED = 0x4;

		public static readonly int STATIC = 0x8;

		public static readonly int FINAL = 0x10;

		public static readonly int VOLATILE = 0x40;

		public static readonly int TRANSIENT = 0x80;

		public static readonly int ABSTRACT = 0x400;

		public static bool IsPublic(int modifiers)
		{
			return (modifiers & PUBLIC) != 0;
		}

		public static bool IsProtected(int modifiers)
		{
			return (modifiers & PROTECTED) != 0;
		}

		public static bool IsPrivate(int modifiers)
		{
			return (modifiers & PRIVATE) != 0;
		}

		public static bool IsStatic(int modifiers)
		{
			return (modifiers & STATIC) != 0;
		}

		public static bool IsFinal(int modifiers)
		{
			return (modifiers & FINAL) != 0;
		}

		public static bool IsVolatile(int modifiers)
		{
			return (modifiers & VOLATILE) != 0;
		}

		public static bool IsTransient(int modifiers)
		{
			return (modifiers & TRANSIENT) != 0;
		}

		public static bool IsAbstract(int modifiers)
		{
			return (modifiers & ABSTRACT) != 0;
		}
	}
}
