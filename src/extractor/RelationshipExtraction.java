package extractor;

import graphdb.MyNode;
import graphdb.MyRelationship;
import graphdb.RelTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import rest.RestAPI;
import rest.entity.RestNode;
import rest.entity.RestRelationship;
import utils.NeoRestConnector;
import domain.MyEntity;
import domain.MyRecord;
import domain.Tuple;

public class RelationshipExtraction {
	private List<Tuple> listTuple;
	private List<MyRecord> listRecord;
	private GraphDatabaseService graphDB;
	private RestAPI restAPI;
	private String inputPath;
	private TextParser textParser;
	private List<MyRecord> prematureRecList;
	private List<MyRecord> processedRecList;

	public RelationshipExtraction(String inputFilePath,
			GraphDatabaseService graphDB) {
		listTuple = new ArrayList<Tuple>();
		listRecord = new ArrayList<MyRecord>();
		this.inputPath = inputFilePath;
		this.graphDB = graphDB;
		this.textParser = new TextParser(inputPath);
	}

	public RelationshipExtraction(String inputFilePath, RestAPI restAPI) {
		listTuple = new ArrayList<Tuple>();
		listRecord = new ArrayList<MyRecord>();
		this.inputPath = inputFilePath;
		this.restAPI = restAPI;
		this.textParser = new TextParser(inputPath);
	}

	public List<Long> restBuildPrematureGraph() {
		List<Long> newNodeIDs = new ArrayList<Long>();
		if (restAPI != null) {
			try {
				textParser.doProcess();
			} catch (IOException e) {
			}
			prematureRecList = textParser.getRecordList();
			newNodeIDs = this.restSaveToGraph(prematureRecList,
					RelTypes.PrematureConnectedTo);
		}
		return newNodeIDs;
	}

	public List<Long> restBuildProcessedGraph() {
		List<Long> newNodeIDs = new ArrayList<Long>();
		if (restAPI != null) {
			PronounHandler ph = new PronounHandler(prematureRecList);
			processedRecList = ph.convertPronoun();
			newNodeIDs = this.restSaveToGraph(processedRecList, RelTypes.ProcessedConnectedTo);
		}
		return newNodeIDs;
	}

	private List<Long> restSaveToGraph(List<MyRecord> recList, RelTypes relType) {
		List<Long> newNodeIDs = new ArrayList<Long>();
		RestNode node1 = null, node2;
		MyNode myNode;
		boolean newRelations = false;
		String entity1, entity2;
		for (MyRecord rec : recList) {
			entity1 = rec.entity1.name;
			node1 = MyNode.restGetNodeByName(entity1, restAPI);
			if (node1 == null) {
				myNode = new MyNode(entity1);
				myNode.restSaveToDB(restAPI);
				node1 = myNode.getRestNode();
				newRelations = true;
			}
			if(checkDuplicate(node1.getId(), newNodeIDs))newNodeIDs.add(node1.getId());

			entity2 = rec.entity2.name;
			node2 = MyNode.restGetNodeByName(entity2, restAPI);
			if (node2 == null) {
				myNode = new MyNode(entity2);
				myNode.restSaveToDB(restAPI);
				node2 = myNode.getRestNode();
				newRelations = true;
			}
			if(checkDuplicate(node2.getId(), newNodeIDs))newNodeIDs.add(node2.getId());

			if (!newRelations) {
				// must check the phrase inside the relationship property,
				// whether the phrase is already exist or not.
				List<RestRelationship> relList = MyRelationship.restGetRelationshipBetweenNode(entity1, entity2, restAPI);
				boolean found = false;
				for (RestRelationship restRel : relList) {
					String phrs = restRel.getProperty("phrase").toString();
					if (phrs.equalsIgnoreCase(rec.relationship) && restRel.getType() == relType) {
						found = true;
						break;
					}
				}
				if (!found)
					newRelations = true;
			}

			if (newRelations) {
				Map<String, Object> relProp = new HashMap<String, Object>();
				relProp.put("phrase", rec.relationship);
				restAPI.createRelationship(node1, node2, relType, relProp);
			}
		}
		
		//Composite Entity Handler
		if(relType == RelTypes.ProcessedConnectedTo){
			MyEntity myEntity1, myEntity2;
			RestNode parentNode, childNode;
			for(MyRecord record: recList){
				myEntity1 = record.entity1;
				if(myEntity1.isComposite()){
					parentNode = MyNode.restGetNodeByName(myEntity1.name, restAPI);
					//split entity
					for(String ent: myEntity1.separatedNodesCandidate()){
						String e = ent;
						childNode = MyNode.restGetNodeByName(e, restAPI);
						if (childNode == null) {
							myNode = new MyNode(e);
							myNode.restSaveToDB(restAPI);
							childNode = myNode.getRestNode();
							newRelations = true;
						}
						if(checkDuplicate(childNode.getId(), newNodeIDs))newNodeIDs.add(childNode.getId());
						splitNode(childNode, parentNode, restAPI);
					}
					deleteNode(parentNode, restAPI);
					if(parentNode!=null)newNodeIDs.remove(parentNode.getId());
				}
				

				myEntity2 = record.entity2;
				if(myEntity2.isComposite()){
					parentNode = MyNode.restGetNodeByName(myEntity2.name, restAPI);
					//split entity
					for(String ent: myEntity2.separatedNodesCandidate()){
						String e = ent;
						childNode = MyNode.restGetNodeByName(e, restAPI);
						if (childNode == null) {
							myNode = new MyNode(e);
							myNode.restSaveToDB(restAPI);
							childNode = myNode.getRestNode();
							newRelations = true;
						}
						if(checkDuplicate(childNode.getId(), newNodeIDs))newNodeIDs.add(childNode.getId());
						splitNode(childNode, parentNode, restAPI);
					}
					deleteNode(parentNode, restAPI);
					if(parentNode!=null)newNodeIDs.remove(parentNode.getId());
				}				
			}
		}
		return newNodeIDs;
	}
	
	public static void splitNode(RestNode childNode, RestNode parentNode, RestAPI restAPI){
		if(parentNode != null && childNode != null){
			Iterable<Relationship> inCommingRel = parentNode.getRelationships(Direction.INCOMING);
			if(inCommingRel != null){
				Iterator<Relationship> inCommingRelIterator = inCommingRel.iterator();
				while(inCommingRelIterator.hasNext()){
					Relationship rel = inCommingRelIterator.next();
					Map<String, Object> relProp = new HashMap<String, Object>();
					Iterator<String> propKeys = rel.getPropertyKeys().iterator();
					while(propKeys.hasNext()){
						String propKey = propKeys.next();
						String propVal = rel.getProperty(propKey).toString();
						relProp.put(propKey, propVal);
					}
					restAPI.createRelationship(rel.getStartNode(), childNode, rel.getType(), relProp);
				}
			}
			
			Iterable<Relationship> outCommingRel = parentNode.getRelationships(Direction.OUTGOING);
			if(outCommingRel != null){
				Iterator<Relationship> outCommingRelIterator = outCommingRel.iterator();
				while(outCommingRelIterator.hasNext()){
					Relationship rel = outCommingRelIterator.next();
					Map<String, Object> relProp = new HashMap<String, Object>();
					Iterator<String> propKeys = rel.getPropertyKeys().iterator();
					while(propKeys.hasNext()){
						String propKey = propKeys.next();
						String propVal = rel.getProperty(propKey).toString();
						relProp.put(propKey, propVal);
					}
					restAPI.createRelationship(childNode, rel.getEndNode(), rel.getType(), relProp);
				}
			}
		}
	}

	public static void deleteNode(RestNode node, RestAPI restAPI){
		if(node == null) return;
		Iterable<Relationship> rels = node.getRelationships();
		if(rels != null){
			Iterator<Relationship> relList = rels.iterator();
			while(relList.hasNext()){
				long rID = relList.next().getId();
				RestRelationship restRel = restAPI.getRelationshipById(rID);
				restAPI.deleteEntity(restRel);
			}
			restAPI.deleteEntity(node);
		}
	}
	
	public static void mergeNode(long sourceID, long destinationID,
			RestAPI restAPI) {
		RestNode node1 = restAPI.getNodeById(sourceID);
		RestNode node2 = restAPI.getNodeById(destinationID);
		if (node1 != null && node2 != null) {
			// Copy relationship to node1
			for (Relationship rel : node1.getRelationships(Direction.INCOMING)) {
				RestRelationship restRel = restAPI.getRelationshipById(rel
						.getId());
				Node sourceNode = rel.getStartNode();
				Map<String, Object> relProp = new HashMap<String, Object>();
				String phrase = rel.getProperty("phrase").toString();
				relProp.put("phrase", phrase);
				restAPI.createRelationship(sourceNode, node2, rel.getType(),
						relProp);
				System.out.println("Created Relationship-FROM "
						+ sourceNode.getProperty("name"));
				restAPI.deleteEntity(restRel);
			}

			// Copy relationship from node1
			for (Relationship rel : node1.getRelationships(Direction.OUTGOING)) {
				RestRelationship restRel = restAPI.getRelationshipById(rel
						.getId());
				Node destNode = rel.getEndNode();
				Map<String, Object> relProp = new HashMap<String, Object>();
				String phrase = rel.getProperty("phrase").toString();
				relProp.put("phrase", phrase);
				restAPI.createRelationship(node2, destNode, rel.getType(),
						relProp);
				System.out.println("Created Relationship-TO "
						+ destNode.getProperty("name"));
				restAPI.deleteEntity(restRel);
			}

			restAPI.deleteEntity(node1);
		}
	}

	private boolean checkDuplicate(Long id, List<Long> list) {
		HashSet<Long> set = new HashSet<Long>();
		set.addAll(list);
		boolean val = set.add(id);
		if (val == false) {
			return val;
		}
		return true;
	}

	public static void main(String args[]) throws IOException {

		NeoRestConnector restN4J = new NeoRestConnector();
		RestAPI restAPI = restN4J.getRestAPI();
		restN4J.clearDB();
		System.out.println("Start..");
		String path = "test.txt";
		RelationshipExtraction re = new RelationshipExtraction(path, restAPI);
		List<Long> IDs = re.restBuildPrematureGraph();
		System.out.println("Premature! ");
		System.out.println("ID List:");
		for (Long id : IDs) {
			System.out.print(id + ", ");
		}

		List<Long> ID2 = re.restBuildProcessedGraph();
		System.out.println("\nProcessed! ");
		System.out.println("ID List:");
		for (Long id : ID2) {
			System.out.print(id + ", ");
		}
		System.out.println("Done");
	}
}
