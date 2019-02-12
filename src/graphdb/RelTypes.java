package graphdb;
import org.neo4j.graphdb.RelationshipType;
/**
 * Relationship types enumeration
 * @author hduser
 *
 */
public enum RelTypes implements RelationshipType
{
	PrematureConnectedTo,
	ProcessedConnectedTo,
	FinalConnectedTo,
}
