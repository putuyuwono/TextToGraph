package domain;

import java.util.ArrayList;
import java.util.List;

public class MyEntity {
	public String name;
	public String tag;
	public String type;
	private List<String> separatedNodesCandidate;
	private boolean isCompositeEntity;

	public MyEntity(String name, String tag, String type) {
		this.name = name;
		this.tag = tag;
		this.type = type;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: " + name + " Tag: " + tag + " Type: " + type);
		return sb.toString();
	}

	public List<String> separatedNodesCandidate() {
		if (separatedNodesCandidate == null)
			separatedNodesCandidate = new ArrayList<String>();
		return separatedNodesCandidate;
	}

	public void setIsComposite(boolean isComposite) {
		this.isCompositeEntity = isComposite;
	}

	public boolean isComposite() {
		return isCompositeEntity;
	}
}
