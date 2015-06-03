package de.osthus.ambeth.concept;

import java.io.IOException;

import org.junit.Test;

import de.osthus.ambeth.bundle.Core;
import de.osthus.ambeth.ioc.IocModule;

/**
 * Zusätzliche Vorschläge:<br>
 * <ul>
 * <li>@BootstrapModule wird depricated und wird durch @App(lication)Module ersetzt</li>
 * </ul>
 */
public class AmbethDemo
{
	/**
	 * Das Standardverhalten wäre<br>
	 * <ul>
	 * <li>(was Ambeth immer tut) nach einem Namen für die Properties-Datei und der Datei zu suchen</li>
	 * <li>(falls nicht in den Properties anders konfiguriert) den CoreClasspathScanner zu starten</li>
	 * <li>einen Kontext mit allen gefundenen Modulen zu bauen</li>
	 * </ul>
	 */
	@Test
	public void demoSimplest() throws IOException
	{
		{
			// create() ist nötig, da das statische start() nicht die Instanz-Methode überschreiben darf.
			IAmbethApplication ambethApplication = Ambeth.createDefault().start();
			try
			{
				@SuppressWarnings("unused")
				IServiceContext serviceContext = ambethApplication.getApplicationContext();
				// ..
			}
			finally
			{
				ambethApplication.close();
			}
		}

		// Java 7
		try (IAmbethApplication ambethApplication = Ambeth.createDefault().start())
		{
			// ..
		}

		// Mit args. Diese könnten auch den Namen der Properties-Datei enthalten
		String[] args = null; // aus main(String[] args)
		try (IAmbethApplication ambethApplication = Ambeth.createDefault().withArgs(args).start())
		{
			// ..
		}

		// Oder für ein Command Line Tool (der try-Block wäre leer, weil alles in afterStart() erledigt wird)
		// Hierbei wird das close() als Shutdown-Event in der JVM registriert, damit es trotz Exception im App-Code ausgeführt wird.
		Ambeth.createDefault().withArgs(args).startAndClose();

		// Der Service-Kontext könnte closeable sein. close() würde dann dispose() auf dem rootContext aufrufen.
		IServiceContext serviceContext = Ambeth.createDefault().start1();
		try
		{
			// ..
		}
		finally
		{
			serviceContext.close();
		}

		// Java 7
		try (IServiceContext context = Ambeth.createDefault().start1())
		{
			// ..
		}
	}

	@Test
	public void demoSimple() throws IOException
	{
		// Explizites setzen einer Property
		try (IAmbethApplication ambethApplication = Ambeth.createDefault().withProperty("file.property", "...").start())
		{
			// ..
		}

		// Zusätzliches Ambeth-Modul
		try (IAmbethApplication ambethApplication = Ambeth.createDefault().withAmbethModules(AnAmbethModule.class).start())
		{
			// ..
		}

		// Unannotiertes Anwendungsmodul (optional/Designentscheidung)
		try (IAmbethApplication ambethApplication = Ambeth.createDefault().withApplicationModules(AppModule.class, AppModule2.class).start())
		{
			// ..
		}

		// Und natürlich geht alles auf einmal...
		try (IAmbethApplication ambethApplication = Ambeth.createDefault().withProperty("file.property", "...")
				.withAmbethModules(AnAmbethModule.class, Oracle11Module.class).withApplicationModules(AppModule.class).start())
		{
			// ..
		}
	}

	@Test
	public void demoBundle() throws IOException
	{
		// Erzeugt (auch wenn das komplette Ambeth im Classpath liegt) nur einen Core-Kontext.
		// Der ClasspathScanner wird nur für die ApplicationModules benutzt.
		// Core.class ist ein IInitializingModule oder evtl. ein IBundleModule, um noch andere Dinge (z.B. in einer Serverumgebung den ClasspathScanner ändern)
		// tun zu können.
		try (IAmbethApplication ambethApplication = Ambeth.createBundle(Core.class).start())
		{
			// ..
		}

		try (IAmbethApplication ambethApplication = Ambeth.createBundle(ServerWithPersistence.class).withAmbethModules(Oracle11Module.class).start())
		{
			// ..
		}
	}

	@Test
	public void demoWithoutClasspathScanner() throws IOException
	{
		// Evtl. überflüssig, aber so wäre es konsistent umsetzbar.
		try (IAmbethApplication ambethApplication = Ambeth.createEmpty().withAmbethModules(IocModule.class, AnAmbethModule.class)
				.withApplicationModules(AppModule.class).start())
		{
			// ..
		}
	}
}
