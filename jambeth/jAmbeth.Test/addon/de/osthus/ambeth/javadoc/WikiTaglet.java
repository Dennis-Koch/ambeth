package de.osthus.ambeth.javadoc;

import java.util.HashMap;
import java.util.Map;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * A sample Taglet representing @wiki. This tag can be used in any kind of {@link com.sun.javadoc.Doc}. It is an inline
 * tag. The text is displayed as a link to the article in the referenced wiki. The referenced wiki must be known by this
 * taglet, there is no configuration possible in this implementation. For example, "Read more about {@wiki
 * wikipedia_en Javadoc}." would be shown as:<br>
 * Read more about <a href="http://en.wikipedia.org/wiki/Javadoc">Javadoc</a>.
 * <p>
 * 
 * Tokens in this tag are separated by whitespaces. The first token after "@wiki" is the wiki name as known to this
 * taglet. Next follows the article name as required in the URL. The text until the tag end is used as link text. If the
 * article name is the last thing in the tag it is used as link text as can be seen in the first example. This example "
 * {@wiki wikipedia_en Software_architecture Software architecture}" contains a link text and would be shown as:<br>
 * <a href="http://en.wikipedia.org/wiki/Software_architecture">Software architecture</a>
 * <p>
 * Currently known wiki names: <tt>wikipedia_en</tt>, <tt>wikipedia_de</tt>, <tt>osthus</tt>
 * 
 * @author Jochen Hormes
 */
public class WikiTaglet implements Taglet
{
	private static final String NAME = "wiki";

	private static final HashMap<String, String> WIKIS = new HashMap<String, String>();

	static
	{
		WIKIS.put("wikipedia_en", "http://en.wikipedia.org/wiki/");
		WIKIS.put("wikipedia_de", "http://de.wikipedia.org/wiki/");
		WIKIS.put("osthus", "http://wiki.member.osthus.de/wiki/index.php?title=");
	}

	/**
	 * Return the name of this custom tag.
	 */
	@Override
	public String getName()
	{
		return NAME;
	}

	/**
	 * Will return true since <code>@wiki</code> can be used in field documentation.
	 * 
	 * @return true since <code>@wiki</code> can be used in field documentation and false otherwise.
	 */
	@Override
	public boolean inField()
	{
		return true;
	}

	/**
	 * Will return true since <code>@wiki</code> can be used in constructor documentation.
	 * 
	 * @return true since <code>@wiki</code> can be used in constructor documentation and false otherwise.
	 */
	@Override
	public boolean inConstructor()
	{
		return true;
	}

	/**
	 * Will return true since <code>@wiki</code> can be used in method documentation.
	 * 
	 * @return true since <code>@wiki</code> can be used in method documentation and false otherwise.
	 */
	@Override
	public boolean inMethod()
	{
		return true;
	}

	/**
	 * Will return true since <code>@wiki</code> can be used in method documentation.
	 * 
	 * @return true since <code>@wiki</code> can be used in overview documentation and false otherwise.
	 */
	@Override
	public boolean inOverview()
	{
		return true;
	}

	/**
	 * Will return true since <code>@wiki</code> can be used in package documentation.
	 * 
	 * @return true since <code>@wiki</code> can be used in package documentation and false otherwise.
	 */
	@Override
	public boolean inPackage()
	{
		return true;
	}

	/**
	 * Will return true since <code>@wiki</code> can be used in type documentation (classes or interfaces).
	 * 
	 * @return true since <code>@wiki</code> can be used in type documentation and false otherwise.
	 */
	@Override
	public boolean inType()
	{
		return true;
	}

	/**
	 * Will return true since <code>@wiki</code> is an inline tag.
	 * 
	 * @return true since <code>@wiki</code> is an inline tag.
	 */

	@Override
	public boolean isInlineTag()
	{
		return true;
	}

	/**
	 * Register this Taglet.
	 * 
	 * @param tagletMap
	 *            the map to register this tag to.
	 */
	public static void register(@SuppressWarnings("rawtypes") Map rawTagletMap)
	{
		@SuppressWarnings("unchecked")
		Map<String, Taglet> tagletMap = rawTagletMap;
		WikiTaglet tag = new WikiTaglet();
		String tagName = tag.getName();
		Taglet t = tagletMap.get(tagName);
		if (t != null)
		{
			tagletMap.remove(tagName);
		}
		tagletMap.put(tagName, tag);
	}

	/**
	 * Given the <code>Tag</code> representation of this custom tag, return its string representation.
	 * 
	 * @param tag
	 *            the <code>Tag</code> representation of this custom tag.
	 */
	@Override
	public String toString(Tag tag)
	{
		String text = tag.text();
		String[] parts = text.split("\\s", 3);
		String wikiUrl = WIKIS.get(parts[0]);
		String articleName = parts[1];
		String linkText = parts.length == 3 ? parts[2] : articleName;
		return "<a href=\"" + wikiUrl + articleName + "\">" + linkText + "</a>";
	}

	/**
	 * Given an array of <code>Tag</code>s representing this custom tag, return its string representation.
	 * 
	 * @param tags
	 *            the array of <code>Tag</code>s representing of this custom tag.
	 */
	@Override
	public String toString(Tag[] tags)
	{
		if (tags.length == 0)
		{
			return null;
		}
		String result = toString(tags[0]);
		for (int i = 1; i < tags.length; i++)
		{
			result += toString(tags[1]);
		}
		return result;
	}
}
