package de.osthus.esmeralda.ioc;

import com.sun.source.tree.Tree.Kind;

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
import de.osthus.esmeralda.handler.IExpressionHandlerRegistry;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionRegistry;
import de.osthus.esmeralda.handler.IStatementHandlerExtendable;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.CsClassHandler;
import de.osthus.esmeralda.handler.csharp.CsFieldHandler;
import de.osthus.esmeralda.handler.csharp.CsHelper;
import de.osthus.esmeralda.handler.csharp.CsMethodHandler;
import de.osthus.esmeralda.handler.csharp.ICsClassHandler;
import de.osthus.esmeralda.handler.csharp.ICsFieldHandler;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.handler.csharp.ICsMethodHandler;
import de.osthus.esmeralda.handler.csharp.expr.ArrayTypeExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.BinaryExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.InstanceOfExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.MethodInvocationExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.NewArrayExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import de.osthus.esmeralda.handler.csharp.expr.TypeCastExpressionHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsAssertHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsEnhancedForHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsReturnHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsSynchronizedHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsThrowHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsTryHandler;
import de.osthus.esmeralda.handler.csharp.stmt.CsVariableHandler;
import de.osthus.esmeralda.handler.js.IJsClassHandler;
import de.osthus.esmeralda.handler.js.IJsClasspathManager;
import de.osthus.esmeralda.handler.js.IJsFieldHandler;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.js.IJsMethodHandler;
import de.osthus.esmeralda.handler.js.IJsOverloadManager;
import de.osthus.esmeralda.handler.js.JsClassHandler;
import de.osthus.esmeralda.handler.js.JsClasspathManager;
import de.osthus.esmeralda.handler.js.JsFieldHandler;
import de.osthus.esmeralda.handler.js.JsHelper;
import de.osthus.esmeralda.handler.js.JsMethodHandler;
import de.osthus.esmeralda.handler.js.JsOverloadManager;
import de.osthus.esmeralda.handler.js.expr.JsArrayTypeExpressionHandler;
import de.osthus.esmeralda.handler.js.expr.JsBinaryExpressionHandler;
import de.osthus.esmeralda.handler.js.expr.JsInstanceOfExpressionHandler;
import de.osthus.esmeralda.handler.js.expr.JsMethodInvocationExpressionHandler;
import de.osthus.esmeralda.handler.js.expr.JsNewArrayExpressionHandler;
import de.osthus.esmeralda.handler.js.expr.JsNewClassExpressionHandler;
import de.osthus.esmeralda.handler.js.expr.JsTypeCastExpressionHandler;
import de.osthus.esmeralda.handler.js.stmt.JsAssertHandler;
import de.osthus.esmeralda.handler.js.stmt.JsEnhancedForHandler;
import de.osthus.esmeralda.handler.js.stmt.JsReturnHandler;
import de.osthus.esmeralda.handler.js.stmt.JsSynchronizedHandler;
import de.osthus.esmeralda.handler.js.stmt.JsThrowHandler;
import de.osthus.esmeralda.handler.js.stmt.JsTryHandler;
import de.osthus.esmeralda.handler.js.stmt.JsVariableHandler;
import de.osthus.esmeralda.handler.uni.expr.ArrayAccessExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.AssignExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.AssignOpExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.ConditionalExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.FieldAccessExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.LiteralExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.ParensExpressionHandler;
import de.osthus.esmeralda.handler.uni.expr.UnaryExpressionHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalBlockHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalBreakHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalContinueHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalDoWhileHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalExpressionHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalForHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalIfHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalLabeledStatementHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalSkipHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalSwitchHandler;
import de.osthus.esmeralda.handler.uni.stmt.UniversalWhileHandler;
import de.osthus.esmeralda.misc.EsmeFileUtil;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.IToDoWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.misc.ToDoWriter;
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

		beanContextFactory.registerBean(EsmeFileUtil.class).autowireable(IEsmeFileUtil.class);

		beanContextFactory.registerBean(ToDoWriter.class).autowireable(IToDoWriter.class);

		beanContextFactory.registerBean(ClassInfoFactory.class).autowireable(IClassInfoFactory.class);
		beanContextFactory.registerBean(CsHelper.class).autowireable(ICsHelper.class);
		beanContextFactory.registerBean(JsHelper.class).autowireable(IJsHelper.class);
		// TODO C# version
		beanContextFactory.registerBean(JsClasspathManager.class).autowireable(IJsClasspathManager.class);
		beanContextFactory.registerBean(IJsOverloadManager.STATIC, JsOverloadManager.class);
		beanContextFactory.registerBean(IJsOverloadManager.NON_STATIC, JsOverloadManager.class);

		// Method transformation
		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IMethodTransformerExtensionExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IMethodTransformerExtensionRegistry.class) //
				.autowireable(IMethodTransformerExtensionExtendable.class, IMethodTransformerExtensionRegistry.class);

		beanContextFactory.registerBean(CsMethodTransformationModule.class);
		beanContextFactory.registerBean(JsMethodTransformationModule.class);

		// language elements
		beanContextFactory.registerBean(CsClassHandler.class).autowireable(ICsClassHandler.class);
		beanContextFactory.registerBean(CsFieldHandler.class).autowireable(ICsFieldHandler.class);
		beanContextFactory.registerBean(CsMethodHandler.class).autowireable(ICsMethodHandler.class);

		beanContextFactory.registerBean(JsClassHandler.class).autowireable(IJsClassHandler.class);
		beanContextFactory.registerBean(JsFieldHandler.class).autowireable(IJsFieldHandler.class);
		beanContextFactory.registerBean(JsMethodHandler.class).autowireable(IJsMethodHandler.class);

		// statements
		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IStatementHandlerExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IStatementHandlerRegistry.class) //
				.autowireable(IStatementHandlerExtendable.class, IStatementHandlerRegistry.class);

		registerStatementHandler(beanContextFactory, UniversalBlockHandler.class, Kind.BLOCK).autowireable(UniversalBlockHandler.class);
		registerStatementHandler(beanContextFactory, UniversalBreakHandler.class, Kind.BREAK);
		registerStatementHandler(beanContextFactory, UniversalContinueHandler.class, Kind.CONTINUE);
		registerStatementHandler(beanContextFactory, UniversalDoWhileHandler.class, Kind.DO_WHILE_LOOP);
		registerStatementHandler(beanContextFactory, UniversalExpressionHandler.class, Kind.EXPRESSION_STATEMENT);
		registerStatementHandler(beanContextFactory, UniversalForHandler.class, Kind.FOR_LOOP);
		registerStatementHandler(beanContextFactory, UniversalIfHandler.class, Kind.IF);
		registerStatementHandler(beanContextFactory, UniversalLabeledStatementHandler.class, Kind.LABELED_STATEMENT);
		registerStatementHandler(beanContextFactory, UniversalSkipHandler.class, Kind.EMPTY_STATEMENT);
		registerStatementHandler(beanContextFactory, UniversalSwitchHandler.class, Kind.SWITCH);
		registerStatementHandler(beanContextFactory, UniversalWhileHandler.class, Kind.WHILE_LOOP);

		registerStatementHandler(beanContextFactory, CsAssertHandler.class, Lang.C_SHARP, Kind.ASSERT);
		registerStatementHandler(beanContextFactory, CsEnhancedForHandler.class, Lang.C_SHARP, Kind.ENHANCED_FOR_LOOP);
		registerStatementHandler(beanContextFactory, CsReturnHandler.class, Lang.C_SHARP, Kind.RETURN);
		registerStatementHandler(beanContextFactory, CsThrowHandler.class, Lang.C_SHARP, Kind.THROW);
		registerStatementHandler(beanContextFactory, CsTryHandler.class, Lang.C_SHARP, Kind.TRY);
		registerStatementHandler(beanContextFactory, CsVariableHandler.class, Lang.C_SHARP, Kind.VARIABLE);
		registerStatementHandler(beanContextFactory, CsSynchronizedHandler.class, Lang.C_SHARP, Kind.SYNCHRONIZED);

		registerStatementHandler(beanContextFactory, JsAssertHandler.class, Lang.JS, Kind.ASSERT);
		registerStatementHandler(beanContextFactory, JsEnhancedForHandler.class, Lang.JS, Kind.ENHANCED_FOR_LOOP);
		registerStatementHandler(beanContextFactory, JsReturnHandler.class, Lang.JS, Kind.RETURN);
		registerStatementHandler(beanContextFactory, JsThrowHandler.class, Lang.JS, Kind.THROW);
		registerStatementHandler(beanContextFactory, JsTryHandler.class, Lang.JS, Kind.TRY);
		registerStatementHandler(beanContextFactory, JsVariableHandler.class, Lang.JS, Kind.VARIABLE);
		registerStatementHandler(beanContextFactory, JsSynchronizedHandler.class, Lang.JS, Kind.SYNCHRONIZED);

		// expressions
		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IExpressionHandlerExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IExpressionHandlerRegistry.class) //
				.autowireable(IExpressionHandlerExtendable.class, IExpressionHandlerRegistry.class);

		registerExpressionHandler(beanContextFactory, ArrayAccessExpressionHandler.class, Kind.ARRAY_ACCESS);
		registerExpressionHandler(beanContextFactory, AssignExpressionHandler.class, Kind.ASSIGNMENT);
		registerExpressionHandler(beanContextFactory, AssignOpExpressionHandler.class, Kind.AND_ASSIGNMENT, Kind.DIVIDE_ASSIGNMENT, Kind.LEFT_SHIFT_ASSIGNMENT,
				Kind.MINUS_ASSIGNMENT, Kind.MULTIPLY_ASSIGNMENT, Kind.OR_ASSIGNMENT, Kind.PLUS_ASSIGNMENT, Kind.RIGHT_SHIFT_ASSIGNMENT, Kind.XOR_ASSIGNMENT);
		registerExpressionHandler(beanContextFactory, ConditionalExpressionHandler.class, Kind.CONDITIONAL_EXPRESSION);
		registerExpressionHandler(beanContextFactory, FieldAccessExpressionHandler.class, Kind.MEMBER_SELECT);
		registerExpressionHandler(beanContextFactory, LiteralExpressionHandler.class, Kind.INT_LITERAL, Kind.LONG_LITERAL, Kind.FLOAT_LITERAL,
				Kind.DOUBLE_LITERAL, Kind.BOOLEAN_LITERAL, Kind.CHAR_LITERAL, Kind.STRING_LITERAL, Kind.NULL_LITERAL, Kind.IDENTIFIER);
		registerExpressionHandler(beanContextFactory, ParensExpressionHandler.class, Kind.PARENTHESIZED);
		registerExpressionHandler(beanContextFactory, UnaryExpressionHandler.class, Kind.BITWISE_COMPLEMENT, Kind.LOGICAL_COMPLEMENT, Kind.POSTFIX_DECREMENT,
				Kind.POSTFIX_INCREMENT, Kind.PREFIX_DECREMENT, Kind.PREFIX_INCREMENT, Kind.UNARY_MINUS, Kind.UNARY_PLUS);

		registerExpressionHandler(beanContextFactory, ArrayTypeExpressionHandler.class, Lang.C_SHARP, Kind.ARRAY_TYPE);
		registerExpressionHandler(beanContextFactory, BinaryExpressionHandler.class, Lang.C_SHARP, Kind.DIVIDE, Kind.REMAINDER, Kind.LEFT_SHIFT,
				Kind.RIGHT_SHIFT, Kind.MINUS, Kind.MULTIPLY, Kind.AND, Kind.OR, Kind.XOR, Kind.CONDITIONAL_AND, Kind.CONDITIONAL_OR, Kind.PLUS, Kind.EQUAL_TO,
				Kind.NOT_EQUAL_TO, Kind.GREATER_THAN, Kind.LESS_THAN, Kind.GREATER_THAN_EQUAL, Kind.LESS_THAN_EQUAL, Kind.UNSIGNED_RIGHT_SHIFT);
		registerExpressionHandler(beanContextFactory, InstanceOfExpressionHandler.class, Lang.C_SHARP, Kind.INSTANCE_OF);
		registerExpressionHandler(beanContextFactory, MethodInvocationExpressionHandler.class, Lang.C_SHARP, Kind.METHOD_INVOCATION);
		registerExpressionHandler(beanContextFactory, NewArrayExpressionHandler.class, Lang.C_SHARP, Kind.NEW_ARRAY);
		registerExpressionHandler(beanContextFactory, NewClassExpressionHandler.class, Lang.C_SHARP, Kind.NEW_CLASS);
		registerExpressionHandler(beanContextFactory, TypeCastExpressionHandler.class, Lang.C_SHARP, Kind.TYPE_CAST);

		registerExpressionHandler(beanContextFactory, JsArrayTypeExpressionHandler.class, Lang.JS, Kind.ARRAY_TYPE);
		registerExpressionHandler(beanContextFactory, JsBinaryExpressionHandler.class, Lang.JS, Kind.DIVIDE, Kind.REMAINDER, Kind.LEFT_SHIFT, Kind.RIGHT_SHIFT,
				Kind.MINUS, Kind.MULTIPLY, Kind.AND, Kind.OR, Kind.XOR, Kind.CONDITIONAL_AND, Kind.CONDITIONAL_OR, Kind.PLUS, Kind.EQUAL_TO, Kind.NOT_EQUAL_TO,
				Kind.GREATER_THAN, Kind.LESS_THAN, Kind.GREATER_THAN_EQUAL, Kind.LESS_THAN_EQUAL, Kind.UNSIGNED_RIGHT_SHIFT);
		registerExpressionHandler(beanContextFactory, JsInstanceOfExpressionHandler.class, Lang.JS, Kind.INSTANCE_OF);
		registerExpressionHandler(beanContextFactory, JsMethodInvocationExpressionHandler.class, Lang.JS, Kind.METHOD_INVOCATION);
		registerExpressionHandler(beanContextFactory, JsNewArrayExpressionHandler.class, Lang.JS, Kind.NEW_ARRAY);
		registerExpressionHandler(beanContextFactory, JsNewClassExpressionHandler.class, Lang.JS, Kind.NEW_CLASS);
		registerExpressionHandler(beanContextFactory, JsTypeCastExpressionHandler.class, Lang.JS, Kind.TYPE_CAST);
	}

	protected static IBeanConfiguration registerMethodTransformerExtension(IBeanContextFactory beanContextFactory,
			Class<? extends IMethodTransformerExtension> methodTransformerType, Class<?> type)
	{
		IBeanConfiguration methodTransformer = beanContextFactory.registerBean(methodTransformerType)//
				.propertyRefs(DefaultMethodTransformerName, DefaultMethodParameterProcessor);
		beanContextFactory.link(methodTransformer).to(IMethodTransformerExtensionExtendable.class).with(type.getName());
		return methodTransformer;
	}

	protected static IBeanConfiguration registerStatementHandler(IBeanContextFactory beanContextFactory,
			Class<? extends IStatementHandlerExtension<?>> statementHandlerType, Kind kind)
	{
		IBeanConfiguration stmtHandler = beanContextFactory.registerBean(statementHandlerType);
		beanContextFactory.link(stmtHandler).to(IStatementHandlerExtendable.class).with(Lang.C_SHARP + kind);
		beanContextFactory.link(stmtHandler).to(IStatementHandlerExtendable.class).with(Lang.JS + kind);
		return stmtHandler;
	}

	protected static IBeanConfiguration registerStatementHandler(IBeanContextFactory beanContextFactory,
			Class<? extends IStatementHandlerExtension<?>> statementHandlerType, String lang, Kind kind)
	{
		IBeanConfiguration stmtHandler = beanContextFactory.registerBean(statementHandlerType);
		beanContextFactory.link(stmtHandler).to(IStatementHandlerExtendable.class).with(lang + kind);
		return stmtHandler;
	}

	protected static IBeanConfiguration registerExpressionHandler(IBeanContextFactory beanContextFactory,
			Class<? extends IExpressionHandler> expressionHandlerType, Kind... kinds)
	{
		return registerExpressionHandler(beanContextFactory, expressionHandlerType, null, kinds);
	}

	protected static IBeanConfiguration registerExpressionHandler(IBeanContextFactory beanContextFactory,
			Class<? extends IExpressionHandler> expressionHandlerType, String language, Kind... kinds)
	{
		IBeanConfiguration expressionHandler = beanContextFactory.registerBean(expressionHandlerType);
		for (Kind kind : kinds)
		{
			String name = kind.name();
			if (language == null)
			{
				beanContextFactory.link(expressionHandler).to(IExpressionHandlerExtendable.class).with(Lang.C_SHARP + name);
				beanContextFactory.link(expressionHandler).to(IExpressionHandlerExtendable.class).with(Lang.JS + name);
			}
			else
			{
				beanContextFactory.link(expressionHandler).to(IExpressionHandlerExtendable.class).with(language + name);
			}
		}
		return expressionHandler;
	}
}
