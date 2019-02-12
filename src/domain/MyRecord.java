package domain;

public class MyRecord {
	public MyEntity entity1;
	public MyEntity entity2;
	public String relationship;
	
	public MyRecord(MyEntity entity1, String relationship, MyEntity entity2){
		this.entity1 = entity1;
		this.relationship = relationship;
		this.entity2 = entity2;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(entity1.type + ":" + entity1.name + " [" + entity1.tag + "]");
		sb.append(" " + relationship + " ");
		sb.append(entity2.type + ":" + entity2.name + " [" + entity2.tag + "] ");
		return sb.toString();
	}
}
