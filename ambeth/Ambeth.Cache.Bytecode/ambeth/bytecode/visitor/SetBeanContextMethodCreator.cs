using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
	public class SetBeanContextMethodCreator : ClassVisitor
	{
		private static readonly FieldAndSetterTemplate template = new FieldAndSetterTemplate(FieldAttributes.Family, "f_beanContext", ReflectUtil.GetDeclaredMethod(
				false, typeof(IBeanContextAware), typeof(void), "set_BeanContext", typeof(IServiceContext)));

		public static MethodInstance GetBeanContextSetter(IClassVisitor cg)
		{
			return template.GetSetter(cg);
		}

		public static FieldInstance GetBeanContextField(IClassVisitor cg)
		{
			return template.GetField(cg);
		}

		public SetBeanContextMethodCreator(IClassVisitor cv) : base(new InterfaceAdder(cv, typeof(IBeanContextAware)))
		{
			// Intended blank
		}

		public override void VisitEnd()
		{
			// force implementation
			template.GetField(this);

			base.VisitEnd();
		}
	}
}