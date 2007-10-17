package helperClasses;

public class HashCode {
	private int hashCode;
	
	public HashCode(int i)
	{
		this.hashCode = i;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final HashCode other = (HashCode) obj;
		if (this.hashCode != other.hashCode())
			return false;
		return true;
	}
	
}
