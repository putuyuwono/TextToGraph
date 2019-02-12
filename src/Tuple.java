
public class Tuple {
	public String prefix;
	public String entity;
	public String sufix;
	
	public Tuple(){
		this.prefix = "";
		this.entity = "";
		this.sufix = "";
	}
	
	public Tuple(String prefix, String entity, String sufix){
		this.prefix = prefix;
		this.entity = entity;
		this.sufix = sufix;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Prefix: " + this.prefix + "\n");
		sb.append("Entity: " + this.entity + "\n");
		sb.append("Sufix: " + this.sufix + "\n");		
		return sb.toString();
	}
}
