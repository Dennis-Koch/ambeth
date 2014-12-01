package de.osthus.esmeralda.ioc;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCUnary;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.CodeProcessor;
import de.osthus.esmeralda.ConversionContextBean;
import de.osthus.esmeralda.ConversionManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.handler.ClassInfoFactory;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IClassInfoFactory;
import de.osthus.esmeralda.handler.IExpressionHandler;
import de.osthus.esmeralda.handler.IExpressionHandlerExtendable;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.INodeHandlerExtendable;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.handler.IStatementHandlerExtendable;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.CsClassHandler;
import de.osthus.esmeralda.handler.csharp.CsFieldHandler;
import de.osthus.esmeralda.handler.csharp.CsHelper;
import de.osthus.esmeralda.handler.csharp.CsMethodHandler;
import de.osthus.esmeralda.handler.csharp.CsStatementHandler;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.handler.csharp.MethodTransformer;
import de.osthus.esmeralda.handler.csharp.expr.ArrayAccessExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.ArrayTypeExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.AssignExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.AssignOpExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.BinaryExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.ConditionalExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.FieldAccessExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.InstanceOfExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.LiteralExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.MethodInvocationExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.NewArrayExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.ParensExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.TypeCastExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.UnaryExpressionHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsBlockHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsBreakHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsContinueHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsDoWhileHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsEnhancedForHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsExpressionHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsForHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsIfHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsReturnHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsSkipHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsSwitchHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsThrowHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsTryHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsVariableHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsWhileHandler;
import de.osthus.esmeralda.handler.csharp.stmt.ICsBlockHandler;
import de.osthus.esmeralda.handler.csharp.stmt.SynchronizedHandler;
import de.osthus.esmeralda.handler.csharp.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.handler.csharp.transformer.DefaultMethodParameterProcessor;
import de.osthus.esmeralda.handler.csharp.transformer.DefaultMethodTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaIoPrintstreamTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangClassTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangObjectTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangReflectFieldTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilListTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.StackTraceElementTransformer;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.js.JsClassHandler;
import de.osthus.esmeralda.handler.js.JsHelper;
import de.osthus.esmeralda.misc.EsmeFileUtil;
import de.osthus.esmeralda.misc.EsmeType;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import de.osthus.esmeralda.snippet.SnippetManagerFactory;

public class EsmeraldaCoreModule implements IInitializingModule
{
	public static final String DefaultMethodTransformerName = "defaultMethodTransformer";

	public static final String DefaultMethodParameterProcessor = "defaultMethodParameterProcessor";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// beanContextFactory.registerBean(InterfaceConverter.class);
		beanContextFactory.registerBean(ASTHelper.class).autowireable(IASTHelper.class);

		beanContextFactory.registerBean(ConversionManager.class);
		beanContextFactory.registerBean(CodeProcessor.class).autowireable(CodeProcessor.class);
		beanContextFactory.registerBean(ConversionContextBean.class).autowireable(IConversionContext.class);

		beanContextFactory.registerBean(SnippetManagerFactory.class).autowireable(ISnippetManagerFactory.class);

		IBeanConfiguration defaultMethodParameterProcessor = beanContextFactory.registerBean(DefaultMethodParameterProcessor,
				DefaultMethodParameterProcessor.class);

		IBeanConfiguration defaultMethodTransformer = beanContextFactory.registerBean(DefaultMethodTransformerName, DefaultMethodTransformer.class)//
				.propertyRef(defaultMethodParameterProcessor)//
				.ignoreProperties(AbstractMethodTransformerExtension.defaultMethodTransformerExtensionProp);

		beanContextFactory.registerBean(MethodTransformer.class)//
				.propertyRef(defaultMethodTransformer)//
				.autowireable(IMethodTransformer.class, IMethodTransformerExtensionExtendable.class);

		registerMethodTransformerExtension(beanContextFactory, JavaLangClassTransformer.class, java.lang.Class.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangObjectTransformer.class, java.lang.Object.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangReflectFieldTransformer.class, java.lang.reflect.Field.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilListTransformer.class, java.util.List.class);
		registerMethodTransformerExtension(beanContextFactory, JavaIoPrintstreamTransformer.class, java.io.PrintStream.class);
		registerMethodTransformerExtension(beanContextFactory, StackTraceElementTransformer.class, java.lang.StackTraceElement.class);

		beanContextFactory.registerBean(EsmeFileUtil.class).autowireable(IEsmeFileUtil.class);

		beanContextFactory.registerBean(ClassInfoFactory.class).autowireable(IClassInfoFactory.class);
		beanContextFactory.registerBean(CsHelper.class).autowireable(ICsHelper.class, IExpressionHandlerExtendable.class);
		beanContextFactory.registerBean(JsHelper.class).autowireable(IJsHelper.class);

		// language elements
		// TODO think: remove extendable since the flexible part to be handled moved to the statement extensions
		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, INodeHandlerExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, INodeHandlerRegistry.class) //
				.autowireable(INodeHandlerExtendable.class, INodeHandlerRegistry.class);

		IBeanConfiguration csClassHandler = beanContextFactory.registerBean(CsClassHandler.class);
		beanContextFactory.link(csClassHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.CLASS);
		IBeanConfiguration jsClassHandler = beanContextFactory.registerBean(JsClassHandler.class);
		beanContextFactory.link(jsClassHandler).to(INodeHandlerExtendable.class).with(Lang.JS + EsmeType.CLASS);

		IBeanConfiguration csFieldHandler = beanContextFactory.registerBean(CsFieldHandler.class);
		beanContextFactory.link(csFieldHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.FIELD);

		IBeanConfiguration csMethodHandler = beanContextFactory.registerBean(CsMethodHandler.class);
		beanContextFactory.link(csMethodHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.METHOD);

		IBeanConfiguration csStatementHandler = beanContextFactory.registerBean(CsStatementHandler.class);
		beanContextFactory.link(csStatementHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.STATEMENT);

		// statements
		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IStatementHandlerExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IStatementHandlerRegistry.class) //
				.autowireable(IStatementHandlerExtendable.class, IStatementHandlerRegistry.class);

		registerStatementHandler(beanContextFactory, CsBlockHandler.class, Lang.C_SHARP + Kind.BLOCK).autowireable(ICsBlockHandler.class);
		registerStatementHandler(beanContextFactory, CsBreakHandler.class, Lang.C_SHARP + Kind.BREAK);
		registerStatementHandler(beanContextFactory, CsContinueHandler.class, Lang.C_SHARP + Kind.CONTINUE);
		registerStatementHandler(beanContextFactory, CsDoWhileHandler.class, Lang.C_SHARP + Kind.DO_WHILE_LOOP);
		registerStatementHandler(beanContextFactory, CsEnhancedForHandler.class, Lang.C_SHARP + Kind.ENHANCED_FOR_LOOP);
		registerStatementHandler(beanContextFactory, CsForHandler.class, Lang.C_SHARP + Kind.FOR_LOOP);
		registerStatementHandler(beanContextFactory, CsExpressionHandler.class, Lang.C_SHARP + Kind.EXPRESSION_STATEMENT);
		registerStatementHandler(beanContextFactory, CsIfHandler.class, Lang.C_SHARP + Kind.IF);
		registerStatementHandler(beanContextFactory, CsReturnHandler.class, Lang.C_SHARP + Kind.RETURN);
		registerStatementHandler(beanContextFactory, CsSkipHandler.class, Lang.C_SHARP + Kind.EMPTY_STATEMENT);
		registerStatementHandler(beanContextFactory, CsSwitchHandler.class, Lang.C_SHARP + Kind.SWITCH);
		registerStatementHandler(beanContextFactory, CsThrowHandler.class, Lang.C_SHARP + Kind.THROW);
		registerStatementHandler(beanContextFactory, CsTryHandler.class, Lang.C_SHARP + Kind.TRY);
		registerStatementHandler(beanContextFactory, CsVariableHandler.class, Lang.C_SHARP + Kind.VARIABLE);
		registerStatementHandler(beanContextFactory, CsWhileHandler.class, Lang.C_SHARP + Kind.WHILE_LOOP);
		registerStatementHandler(beanContextFactory, SynchronizedHandler.class, Lang.C_SHARP + Kind.SYNCHRONIZED);

		// expressions
		registerExpressionHandler(beanContextFactory, ArrayAccessExpressionHandler.class, JCArrayAccess.class);
		registerExpressionHandler(beanContextFactory, ArrayTypeExpressionHandler.class, JCArrayTypeTree.class);
		registerExpressionHandler(beanContextFactory, AssignExpressionHandler.class, JCAssign.class);
		registerExpressionHandler(beanContextFactory, AssignOpExpressionHandler.class, JCAssignOp.class);
		registerExpressionHandler(beanContextFactory, BinaryExpressionHandler.class, JCBinary.class);
		registerExpressionHandler(beanContextFactory, ConditionalExpressionHandler.class, JCConditional.class);
		registerExpressionHandler(beanContextFactory, FieldAccessExpressionHandler.class, JCFieldAccess.class);
		registerExpressionHandler(beanContextFactory, LiteralExpressionHandler.class, JCLiteral.class, JCIdent.class);
		registerExpressionHandler(beanContextFactory, InstanceOfExpressionHandler.class, JCInstanceOf.class);
		registerExpressionHandler(beanContextFactory, MethodInvocationExpressionHandler.class, JCMethodInvocation.class);
		registerExpressionHandler(beanContextFactory, NewArrayExpressionHandler.class, JCNewArray.class);
		registerExpressionHandler(beanContextFactory, NewClassExpressionHandler.class, JCNewClass.class);
		registerExpressionHandler(beanContextFactory, ParensExpressionHandler.class, JCParens.class);
		registerExpressionHandler(beanContextFactory, TypeCastExpressionHandler.class, JCTypeCast.class);
		registerExpressionHandler(beanContextFactory, UnaryExpressionHandler.class, JCUnary.class);
	}

	public static IBeanConfiguration registerMethodTransformerExtension(IBeanContextFactory beanContextFactory,
			Class<? extends IMethodTransformerExtension> methodTransformerType, Class<?> type)
	{
		IBeanConfiguration methodTransformer = beanContextFactory.registerBean(methodTransformerType)//
				.propertyRefs(DefaultMethodTransformerName, DefaultMethodParameterProcessor);
		beanContextFactory.link(methodTransformer).to(IMethodTransformerExtensionExtendable.class).with(type.getName());
		return methodTransformer;
	}

	public static IBeanConfiguration registerStatementHandler(IBeanContextFactory beanContextFactory,
			Class<? extends IStatementHandlerExtension<?>> statementHandlerType, String key)
	{
		IBeanConfiguration stmtHandler = beanContextFactory.registerBean(statementHandlerType);
		beanContextFactory.link(stmtHandler).to(IStatementHandlerExtendable.class).with(key);
		return stmtHandler;
	}

	public static IBeanConfiguration registerExpressionHandler(IBeanContextFactory beanContextFactory,
			Class<? extends IExpressionHandler> expressionHandlerType, Class<?>... expressionTypes)
	{
		IBeanConfiguration expressionHandler = beanContextFactory.registerBean(expressionHandlerType);
		for (Class<?> expressionType : expressionTypes)
		{
			beanContextFactory.link(expressionHandler).to(IExpressionHandlerExtendable.class).with(expressionType);
		}
		return expressionHandler;
	}
}
