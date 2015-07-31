namespace De.Osthus.Ambeth.Ioc.Annotation
{
	public class AutowiredTestBean
	{
		[Autowired]
		private IServiceContext beanContextPrivate = null;

		[Autowired]
		protected IServiceContext beanContextProtected;

		[Autowired]
		public IServiceContext beanContextPublic;

		private IServiceContext beanContextPrivateSetter;

		private IServiceContext beanContextProtectedSetter;

		private IServiceContext beanContextPublicSetter;

		private IServiceContext beanContextProtectedSetterAutowired;

		private IServiceContext beanContextPrivateSetterAutowired;

		private void setBeanContextPrivateSetter(IServiceContext beanContextPrivateSetter)
		{
			this.beanContextPrivateSetter = beanContextPrivateSetter;
		}

		[Autowired]
		protected void setBeanContextPrivateSetterAutowired(IServiceContext beanContextPrivateSetterAutowired)
		{
			this.beanContextPrivateSetterAutowired = beanContextPrivateSetterAutowired;
		}

		protected void setBeanContextProtectedSetter(IServiceContext beanContextProtectedSetter)
		{
			this.beanContextProtectedSetter = beanContextProtectedSetter;
		}

		[Autowired]
		protected void setBeanContextProtectedSetterAutowired(IServiceContext beanContextProtectedSetterAutowired)
		{
			this.beanContextProtectedSetterAutowired = beanContextProtectedSetterAutowired;
		}

		public void setBeanContextPublicSetter(IServiceContext beanContextPublicSetter)
		{
			this.beanContextPublicSetter = beanContextPublicSetter;
		}

		public IServiceContext getBeanContextPrivate()
		{
			return beanContextPrivate;
		}

		public IServiceContext getBeanContextProtected()
		{
			return beanContextProtected;
		}

		public IServiceContext getBeanContextPublic()
		{
			return beanContextPublic;
		}

		public IServiceContext getBeanContextPrivateSetter()
		{
			return beanContextPrivateSetter;
		}

		public IServiceContext getBeanContextPrivateSetterAutowired()
		{
			return beanContextPrivateSetterAutowired;
		}

		public IServiceContext getBeanContextProtectedSetter()
		{
			return beanContextProtectedSetter;
		}

		public IServiceContext getBeanContextProtectedSetterAutowired()
		{
			return beanContextProtectedSetterAutowired;
		}

		public IServiceContext getBeanContextPublicSetter()
		{
			return beanContextPublicSetter;
		}
	}
}