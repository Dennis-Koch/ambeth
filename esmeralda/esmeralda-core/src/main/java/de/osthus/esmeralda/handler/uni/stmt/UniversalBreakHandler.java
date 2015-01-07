package de.osthus.esmeralda.handler.uni.stmt;

import javax.lang.model.element.Name;

import com.sun.source.tree.BreakTree;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class UniversalBreakHandler extends AbstractStatementHandler<BreakTree> implements IStatementHandlerExtension<BreakTree>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(BreakTree breakStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();
		Name label = breakStatement.getLabel();

		languageHelper.newLineIndent();
		writer.append("break");
		if (label != null)
		{
			writer.append(' ');
			writer.append(label);
		}
		writer.append(';');
	}
}
