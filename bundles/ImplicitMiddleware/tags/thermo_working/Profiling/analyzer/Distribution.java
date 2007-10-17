package analyzer;

import helperClasses.ClassDescNotFoundException;
import helperClasses.HashCode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.ClassDesc;
import model.IgnoredClasses;
import model.Machine;

import org.jdom.Element;

/**
 * This class provides methods to map classes to their machines and to generate all possible distributions 
 * for the classes that are not mapped
 * @author JT
 *
 */
public class Distribution {
	boolean initialized = false;
	
	Map<HashCode, ClassDesc> classes;
	List<ClassDesc> dynamicMapping = new LinkedList<ClassDesc>();
	
	public Distribution(Map<HashCode, ClassDesc> classes)
	{
		this.classes = classes;
	}
	
	@SuppressWarnings("unchecked")
	public void map(Element config)
	{
		List<Element> mappings = config.getChild("mappings").getChildren();
		
		Iterator<ClassDesc> classIt = classes.values().iterator();
		while (classIt.hasNext())
		{
			ClassDesc current = classIt.next();
			Iterator<Element> mapsIt = mappings.iterator();
			if (IgnoredClasses.isIgnored(current.getName()))
				current.setMachine(Machine.localMachine);
			while(mapsIt.hasNext())
			{
				Element mapping = mapsIt.next();
				if (mapping.getAttributeValue("className").equals(current.getName()))
				{
					Machine machine = Machine.getMachineByName(mapping.getAttributeValue("machineName"));
					current.setMachine(machine);
					mapsIt.remove();
					break;
				}
			}
			if (current.getMachine() == null)
				dynamicMapping.add(current);
		}
	}
	
	private void initDistribution()
	{
		Iterator<ClassDesc> it = dynamicMapping.iterator();
		while (it.hasNext())
		{
			it.next().setMachine(Machine.getFirstMachine());
		}
		initialized = true;
	}
	
	public boolean makeNextDistribution()
	{
		if (initialized == false)
		{
			initDistribution();
			return true;
		}
		else
			return makeNextDistri();
	}
	
	
	private boolean makeNextDistri() {
		Iterator<ClassDesc> it = dynamicMapping.iterator();
		while (it.hasNext())
		{
			ClassDesc classDesc = it.next();
			Machine machine = classDesc.getMachine();
			if (Machine.hasNextMachine(machine))
			{
				classDesc.setMachine(Machine.getNextMachine(machine));
				return true;
			}
			else
			{
				classDesc.setMachine(Machine.getFirstMachine());
			}
		}
		return false;
		
	}

	public ClassDesc getClassDescByName(String name) throws ClassDescNotFoundException
	{
		Iterator<ClassDesc> it = classes.values().iterator();
		while (it.hasNext())
		{
			ClassDesc current = it.next();
			if (current.getName().equals(name))
			{
				return current;
			}
		}
		throw new ClassDescNotFoundException(name);
	}
	
	public void printCurrentDistribution()
	{
		if (initialized)
		{
			System.out.println("Printing current distribution");
			Iterator<ClassDesc> it = classes.values().iterator();
			while (it.hasNext()) 
			{
				ClassDesc current = it.next();
				System.out.println(current.getName() + " - " + current.getMachine().getMachineID() + "  " + current.getMachine().getMachineName());
			}
		}
		else
		{
			System.err.println("Error: could not print distribution since no distribution has been generated!");
		}
	}

}
