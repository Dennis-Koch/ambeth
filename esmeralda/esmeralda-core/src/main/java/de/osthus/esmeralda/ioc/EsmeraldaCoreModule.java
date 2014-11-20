package de.osthus.esmeralda.ioc;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCUnary;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.CodeProcessor;
import de.osthus.esmeralda.ConversionManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.ConversionContextBean;
import de.osthus.esmeralda.handler.IExpressionHandler;
import de.osthus.esmeralda.handler.IExpressionHandlerExtendable;
import de.osthus.esmeralda.handler.INodeHandlerExtendable;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.handler.IStatementHandlerExtendable;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.AssignExpressionHandler;
import de.osthus.esmeralda.handler.csharp.AssignOpExpressionHandler;
import de.osthus.esmeralda.handler.csharp.BinaryExpressionHandler;
import de.osthus.esmeralda.handler.csharp.CsharpClassNodeHandler;
import de.osthus.esmeralda.handler.csharp.CsharpFieldNodeHandler;
import de.osthus.esmeralda.handler.csharp.CsharpHelper;
import de.osthus.esmeralda.handler.csharp.CsharpMethodNodeHandler;
import de.osthus.esmeralda.handler.csharp.CsharpStatementHandler;
import de.osthus.esmeralda.handler.csharp.FieldAccessExpressionHandler;
import de.osthus.esmeralda.handler.csharp.ICsharpHelper;
import de.osthus.esmeralda.handler.csharp.LiteralExpressionHandler;
import de.osthus.esmeralda.handler.csharp.MethodInvocationExpressionHandler;
import de.osthus.esmeralda.handler.csharp.NewArrayExpressionHandler;
import de.osthus.esmeralda.handler.csharp.NewClassExpressionHandler;
import de.osthus.esmeralda.handler.csharp.UnaryExpressionHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsBlockHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsExpressionHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsForEnhancedHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsForHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsReturnHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsVariableHandler;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.js.JsClassNodeHandler;
import de.osthus.esmeralda.handler.js.JsHelper;
import de.osthus.esmeralda.misc.EsmeFileUtil;
import de.osthus.esmeralda.misc.EsmeType;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import de.osthus.esmeralda.snippet.SnippetManagerFactory;

public class EsmeraldaCoreModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// beanContextFactory.registerBean(InterfaceConverter.class);
		beanContextFactory.registerBean(ConversionManager.class);
		beanContextFactory.registerBean(CodeProcessor.class).autowireable(CodeProcessor.class);
		beanContextFactory.registerBean(ConversionContextBean.class).autowireable(IConversionContext.class);

		beanContextFactory.registerBean(SnippetManagerFactory.class).autowireable(ISnippetManagerFactory.class);

		beanContextFactory.registerBean(EsmeFileUtil.class).autowireable(IEsmeFileUtil.class);

		beanContextFactory.registerBean(CsharpHelper.class).autowireable(ICsharpHelper.class, IExpressionHandlerExtendable.class);
		beanContextFactory.registerBean(JsHelper.class).autowireable(IJsHelper.class);

		// language elements
		// TODO think: remove extendable since the flexible part to be handled moved to the statement extensions
		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, INodeHandlerExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, INodeHandlerRegistry.class) //
				.autowireable(INodeHandlerExtendable.class, INodeHandlerRegistry.class);

		IBeanConfiguration csClassNodeHandler = beanContextFactory.registerBean(CsharpClassNodeHandler.class);
		beanContextFactory.link(csClassNodeHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.CLASS);
		IBeanConfiguration jsClassNodeHandler = beanContextFactory.registerBean(JsClassNodeHandler.class);
		beanContextFactory.link(jsClassNodeHandler).to(INodeHandlerExtendable.class).with(Lang.JS + EsmeType.CLASS);

		IBeanConfiguration csFieldNodeHandler = beanContextFactory.registerBean(CsharpFieldNodeHandler.class);
		beanContextFactory.link(csFieldNodeHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.FIELD);

		IBeanConfiguration csMethodNodeHandler = beanContextFactory.registerBean(CsharpMethodNodeHandler.class);
		beanContextFactory.link(csMethodNodeHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.METHOD);

		IBeanConfiguration csStatementHandler = beanContextFactory.registerBean(CsharpStatementHandler.class);
		beanContextFactory.link(csStatementHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.STATEMENT);

		// statements
		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IStatementHandlerExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IStatementHandlerRegistry.class) //
				.autowireable(IStatementHandlerExtendable.class, IStatementHandlerRegistry.class);

		registerStatementHandler(beanContextFactory, CsBlockHandler.class, Lang.C_SHARP + Kind.BLOCK);
		registerStatementHandler(beanContextFactory, CsForEnhancedHandler.class, Lang.C_SHARP + Kind.ENHANCED_FOR_LOOP);
		registerStatementHandler(beanContextFactory, CsForHandler.class, Lang.C_SHARP + Kind.FOR_LOOP);
		registerStatementHandler(beanContextFactory, CsExpressionHandler.class, Lang.C_SHARP + Kind.EXPRESSION_STATEMENT);
		registerStatementHandler(beanContextFactory, CsReturnHandler.class, Lang.C_SHARP + Kind.RETURN);
		registerStatementHandler(beanContextFactory, CsVariableHandler.class, Lang.C_SHARP + Kind.VARIABLE);

		// expressions
		registerExpressionHandler(beanContextFactory, LiteralExpressionHandler.class, JCLiteral.class, JCIdent.class);
		registerExpressionHandler(beanContextFactory, AssignExpressionHandler.class, JCAssign.class);
		registerExpressionHandler(beanContextFactory, AssignOpExpressionHandler.class, JCAssignOp.class);
		registerExpressionHandler(beanContextFactory, BinaryExpressionHandler.class, JCBinary.class);
		registerExpressionHandler(beanContextFactory, UnaryExpressionHandler.class, JCUnary.class);
		registerExpressionHandler(beanContextFactory, FieldAccessExpressionHandler.class, JCFieldAccess.class);
		registerExpressionHandler(beanContextFactory, MethodInvocationExpressionHandler.class, JCMethodInvocation.class);
		registerExpressionHandler(beanContextFactory, NewArrayExpressionHandler.class, JCNewArray.class);
		registerExpressionHandler(beanContextFactory, NewClassExpressionHandler.class, JCNewClass.class);
	}

	private void registerStatementHandler(IBeanContextFactory beanContextFactory, Class<? extends IStatementHandlerExtension<?>> statementHandlerType,
			String key)
	{
		IBeanConfiguration stmtHandler = beanContextFactory.registerBean(statementHandlerType);
		beanContextFactory.link(stmtHandler).to(IStatementHandlerExtendable.class).with(key);
	}

	protected void registerExpressionHandler(IBeanContextFactory beanContextFactory, Class<? extends IExpressionHandler> expressionHandlerType,
			Class<?>... expressionTypes)
	{
		IBeanConfiguration expressionHandler = beanContextFactory.registerBean(expressionHandlerType);
		for (Class<?> expressionType : expressionTypes)
		{
			beanContextFactory.link(expressionHandler).to(IExpressionHandlerExtendable.class).with(expressionType);
		}
	}
}
