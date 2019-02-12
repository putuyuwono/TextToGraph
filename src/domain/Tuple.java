package domain;

public class Tuple {
	public String prefix;
	public String entity;
	public String suffix;
	
	public Tuple(){
		this.prefix = "";
		this.entity = "";
		this.suffix = "";
	}
	
	public Tuple(String prefix, String entity, String sufix){
		this.prefix = prefix;
		this.entity = entity;
		this.suffix = sufix;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Prefix: " + this.prefix + "\n");
		sb.append("Entity: " + this.entity + "\n");
		sb.append("Sufix: " + this.suffix + "\n");		
		return sb.toString();
	}
}
