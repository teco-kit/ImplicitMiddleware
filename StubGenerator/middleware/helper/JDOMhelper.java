package middleware.helper;

import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;

public class JDOMhelper {
	public static String[] getAttributeValues(List attribList)
	{
		//Iterator<Attribute> attIt = attribList.iterator();
		Iterator attIt = attribList.iterator();
		String attributes[] = new String[attribList.size()];
		int i = 0;
		while (attIt.hasNext())
		{
			attributes[i] = ((Attribute)attIt.next()).getValue();
			i++;
		}
		return attributes;

	}

}
