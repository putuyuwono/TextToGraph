package graphdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

import rest.RestAPI;
import rest.entity.RestNode;
import utils.StringUtils;

public class MyNode {
	protected Node thisNode;
	protected RestNode thisRestNode;
	protected GraphDatabaseService graphDB;

	protected String name;

	public MyNode(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public Node getNode() {
		return this.thisNode;
	}

	public RestNode getRestNode() {
		return this.thisRestNode;
	}

	public boolean saveToDB(GraphDatabaseService graphDB) {
		boolean status = false;
		Transaction tx = graphDB.beginTx();
		try {
			thisNode = graphDB.createNode();
			thisNode.setProperty("name", this.name);
			tx.success();
			status = true;
		} finally {
			tx.finish();
		}
		return status;
	}

	public void restSaveToDB(RestAPI restAPI) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("name", this.name);
		thisRestNode = restAPI.createNode(props);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isNullOrEmpty(name)) {
			sb.append(" name: " + name + "\n");
		}
		return sb.toString();
	}

	public static Node getNodeByName(String name, GraphDatabaseService graphDB) {
		String startClause = "START n=node(*) ";
		String whereClause = "WHERE has(n.name) AND n.name = '" + name + "' ";
		String returnClause = "RETURN n;";
		String query = startClause + whereClause + returnClause;

		List<Node> nodeList = new ArrayList<Node>();
		ExecutionResult result = new ExecutionEngine(graphDB).execute(query);
		Iterator<Node> n_column = result.columnAs("n");
		for (Node node : IteratorUtil.asIterable(n_column)) {
			nodeList.add(node);
			break;
		}

		return nodeList.size() > 0 ? nodeList.get(0) : null;
	}

	public static RestNode restGetNodeByName(String name, RestAPI restAPI) {
		long id = restGetNodeIDByName(name, restAPI);
		RestNode node = null;
		if (id > -1) {
			node = restAPI.getNodeById(id);
		}
		return node;
	}

	public static long restGetNodeIDByName(String name, RestAPI restAPI) {
		String startClause = "START n=node(*) ";
		String whereClause = "WHERE has(n.name) AND n.name = '" + name + "' ";
		String returnClause = "RETURN ID(n);";
		String query = startClause + whereClause + returnClause;
		Map<?, ?> result = restAPI.query(query, null);
		List<List<Object>> rows = (List<List<Object>>) result.get("data");
		long id = -1;
		if (rows != null) {
			for (List<Object> row : rows) {
				id = Long.parseLong(row.get(0).toString());
				break;
			}
		}
		return id;
	}
}
