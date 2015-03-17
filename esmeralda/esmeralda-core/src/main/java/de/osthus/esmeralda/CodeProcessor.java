package de.osthus.esmeralda;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import demo.codeanalyzer.common.model.JavaClassInfo;

/**
 * The annotation processor class which processes java annotaions in the supplied source file(s). This processor supports v1.6 of java language and can
 * processes all annotation types.
 * 
 * @author Seema Richard (Seema.Richard@ust-global.com)
 * @author Deepa Sobhana (Deepa.Sobhana@ust-global.com)
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class CodeProcessor extends AbstractProcessor
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	protected final ArrayList<JavaClassInfo> classInfos = new ArrayList<JavaClassInfo>();

	private Trees trees;

	public ArrayList<JavaClassInfo> getClassInfos()
	{
		return classInfos;
	}

	@Override
	public void init(ProcessingEnvironment pe)
	{
		super.init(pe);
		trees = Trees.instance(pe);
	}

	/**
	 * Processes the annotation types defined for this processor.
	 * 
	 * @param annotations
	 *            the annotation types requested to be processed
	 * @param roundEnvironment
	 *            environment to get information about the current and prior round
	 * @return whether or not the set of annotations are claimed by this processor
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment)
	{
		CodeVisitor visitor = beanContext.registerBean(CodeVisitor.class).finish();
		// Scanner class to scan through various component elements

		Set<? extends Element> rootElements = roundEnvironment.getRootElements();
		int index = 0;
		long lastLog = System.currentTimeMillis();

		for (Element e : rootElements)
		{
			TreePath tp = trees.getPath(e);

			visitor.reset();
			// invoke the scanner
			visitor.scan(tp, trees);
			classInfos.addAll(visitor.getClassInfos());
			index++;

			if (System.currentTimeMillis() - lastLog < 1000)
			{
				continue;
			}
			parseProgress(index / (double) rootElements.size());
			lastLog = System.currentTimeMillis();
		}
		parseProgress(1);
		throw new FastBreakException();
	}

	protected void parseProgress(double ratio)
	{
		log.info("Parsed " + ((int) (ratio * 10000) / 100.0) + "% of java source");
	}
}
