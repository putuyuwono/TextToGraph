package graphdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.IteratorUtil;

import rest.RestAPI;
import rest.entity.RestRelationship;
import utils.StringUtils;
/**
 * This class handles how to create relationship between nodes.
 * @author hduser
 *
 */
public class MyRelationship {
	protected String id;
	protected String type;
	protected Node sourceNode;
	protected Node targetNode;
	protected Relationship thisRelationship;
	protected GraphDatabaseService graphDB;
	protected Index<Node> index;
	
	public MyRelationship(String id, String type, GraphDatabaseService graphDB){
		this.id = id;
		this.type = type;
		
		this.graphDB = graphDB;
	}
	
	public String getID(){
		return this.id;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(!StringUtils.isNullOrEmpty(id)){
			sb.append("Relationship ID: " +id + " Type: " + type + "\n");
		}
		return sb.toString();
	}
	
	public static Relationship createRelationship(Node source, Node target, RelTypes relationship, GraphDatabaseService graphDB){
		Relationship rel = null;
		Transaction tx = graphDB.beginTx();
		try{
			rel = source.createRelationshipTo(target, relationship);
			tx.success();
		}finally{
			tx.finish();
		}
		return rel;
	}
	
	public static void setProperty(Relationship rel, String propName, Object propValue, GraphDatabaseService graphDB){
		Transaction tx = graphDB.beginTx();
		try{
			rel.setProperty(propName, propValue);
			tx.success();
		}finally{
			tx.finish();
		}
	}

	public static Relationship getRelationshipBetweenNode(String nameNode1, String nameNode2, GraphDatabaseService graphDB){
		String startClause = "START n=node(*) ";
		String matchClause = "MATCH n-[e]-m ";				
		String whereClause = "WHERE has(n.name) AND has(m.name) AND n.name = '" + nameNode1 + "' AND m.name = '" + nameNode2 + "' ";
		String returnClause = "RETURN e;";
		String query = startClause + matchClause + whereClause + returnClause;
		
		List<Relationship> relList = new ArrayList<Relationship>();
		ExecutionResult result = new ExecutionEngine(graphDB).execute(query);
		Iterator<Relationship> n_column = result.columnAs("e");
		for (Relationship rel : IteratorUtil.asIterable(n_column)) {
			relList.add(rel);
			break;
		}
		
		return relList.size()>0 ? relList.get(0) : null;
	}
	
	public static List<RestRelationship> restGetRelationshipBetweenNode(String nameNode1, String nameNode2, RestAPI restAPI){
		List<Long> listID = restGetRelIDByNodeName(nameNode1, nameNode2, restAPI);
		List<RestRelationship> relList = new ArrayList<RestRelationship>();
		for(Long id: listID){
			RestRelationship rel = restAPI.getRelationshipById(id);
			relList.add(rel);
		}
		return relList;
	}
	
	public static List<Long> restGetRelIDByNodeName(String nameNode1, String nameNode2, RestAPI restAPI){
		String startClause = "START n=node(*) ";
		String matchClause = "MATCH n-[e]-m ";				
		String whereClause = "WHERE has(n.name) AND has(m.name) AND n.name = '" + nameNode1 + "' AND m.name = '" + nameNode2 + "' ";
		String returnClause = "RETURN ID(e);";
		String query = startClause + matchClause + whereClause + returnClause;
		Map<?,?> result = restAPI.query(query, null);
        List<List<Object>> rows = (List<List<Object>>) result.get("data");
        long id = -1;
        List<Long> listID = new ArrayList<Long>();
        for (List<Object> row : rows) {
            id = Long.parseLong(row.get(0).toString());
            listID.add(id);
        }
        
        return listID;
	}
}
