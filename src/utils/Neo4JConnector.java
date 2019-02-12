package utils;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;
/**
 * This class will be used for connecting our application, to a local Graph Database Server.
 * @author hduser
 *
 */
public class Neo4JConnector {
	private String DB_PATH = "/home/hduser/data2/graph.db";
	private GraphDatabaseService graphDB;

	public Neo4JConnector(){
		this.initDB();
	}
	
	public Neo4JConnector(String dbPath) {
		this.DB_PATH = dbPath;
		this.initDB();
	}

	public void initDB() {
		graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
		registerShutdownHook(graphDB);
	}
	
	public GraphDatabaseService getGraphDBService(){
		return graphDB;
	}

	public void clearDB() {
		try {
			FileUtils.deleteRecursively(new File(DB_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void shutDown() {
		System.out.println();
		System.out.println("Shutting down database ...");
		// START SNIPPET: shutdownServer
		graphDB.shutdown();
		// END SNIPPET: shutdownServer
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
