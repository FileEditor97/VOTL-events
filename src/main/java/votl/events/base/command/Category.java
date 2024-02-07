package votl.events.base.command;

import java.util.Objects;

/**
 * To be used in {@link votl.events.base.command.Command Command}s as a means of
 * organizing commands into "Categories".
 *
 * @author John Grosh (jagrosh)
 */
public class Category
{
	private final String name;

	/**
	 * A Command Category containing a name.
	 *
	 * @param  name
	 *         The name of the Category
	 */
	public Category(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the name of the Category.
	 *
	 * @return The name of the Category
	 */
	public String getName()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Category))
			return false;
		Category other = (Category)obj;
		return Objects.equals(name, other.name);
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 17 * hash + Objects.hashCode(this.name);
		return hash;
	}
}
