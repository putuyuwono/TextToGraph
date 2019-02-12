package extractor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import rest.RestAPI;
import rest.entity.RestNode;
import rest.entity.RestRelationship;
import utils.NeoRestConnector;
import domain.HRConstats;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import graphdb.MyRelationship;
import graphdb.RelTypes;

public class OntologyMerger {

	public static void merge(List<Long> nodeIDs, RestAPI restAPI) {
		StanfordLemmatizer stLemma = new StanfordLemmatizer();
		String path = HRConstats.wordnet_path;
		URL url = null;
		try {
			url = new URL("file", null, path);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		}

		IDictionary dict = new Dictionary(url);
		try {
			dict.open();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Started");
		Set<Long> synRel = new HashSet<Long>();
		List<String> phraseList = new ArrayList<String>();
		
		Set<Long> visitedNode = new HashSet<Long>();
		Set<Long> visitedRel = new HashSet<Long>();
		Set<Long> uniqueRel = new HashSet<Long>();
		for (Long id : nodeIDs) {
			RestNode node = restAPI.getNodeById(id);
			if (node != null) {
				String nodeName = node.getProperty("name").toString();
				Iterable<Relationship> relList = node.getRelationships(Direction.OUTGOING, RelTypes.ProcessedConnectedTo);
				Iterator<Relationship> relIterator = relList.iterator();
				while (relIterator.hasNext()) {
					Relationship r = relIterator.next();
					if (visitedRel.contains(r.getId())) {
						continue;
					}
					
					Node friendNode = r.getEndNode();
					String friendName = friendNode.getProperty("name").toString();
					
					//Iterate all of the relationships to a specific node.
					List<RestRelationship> friendRel = MyRelationship.restGetRelationshipBetweenNode(nodeName, friendName, restAPI);
					for(RestRelationship fr: friendRel){
						String phrase = stLemma.lemmatizeAsString(fr.getProperty("phrase").toString());
						if(phraseList.size()==0){							
							phraseList.add(phrase);
							uniqueRel.add(fr.getId());
						}else{
							if(!isExist(phrase, phraseList, dict)){				
								phraseList.add(phrase);
								uniqueRel.add(fr.getId());								
							}
						}
					}
					//rest phrase list
					phraseList = new ArrayList<String>();
					
					//mark as visited node and relationship
					visitedNode.add(friendNode.getId());
					visitedRel.add(r.getId());
				}
			}
		}

		/**
		 * Create New Relationship Type: FinalConnectedTo
		 */
		for (long id : uniqueRel) {
			RestRelationship rel = restAPI.getRelationshipById(id);
			Node node1 = rel.getStartNode();
			Node node2 = rel.getEndNode();
			String phrase = rel.getProperty("phrase").toString();
			Map<String, Object> relProp = new HashMap<String, Object>();
			relProp.put("phrase", phrase);
			restAPI.createRelationship(node1, node2, RelTypes.FinalConnectedTo,
					relProp);
		}
	}
	
	private static boolean isExist(String phrase, List<String> phraseList, IDictionary dict) {		
		for(String s: phraseList){	
			System.out.print("Checking: " + s + " vs " + phrase);
			if(isSynonym(s, phrase, dict)){
				System.out.println(" TRUE");
				return true;
			}
			System.out.println(" FALSE");
		}
		return true;
	}

	public static Set<IWord> getSynonyms(IDictionary dict_, String word_) {
		Set<IWord> allSenses = new HashSet<IWord>();

		IIndexWord idxWord = dict_.getIndexWord(word_, POS.VERB);
		if (idxWord == null) {
//			System.out.println(word_ + " Can't be Found in Dictionary");
			return null;
		}

		for (IWordID wordID : idxWord.getWordIDs()) {
			IWord word = dict_.getWord(wordID);
			allSenses.addAll(word.getSynset().getWords());
//			System.out.println("Synonym: " + word.getSynset().getWords().get(0));
		}

		// iterate over related words
		return allSenses;
	}
	
	public static boolean isSynonym(String word1, String word2, IDictionary dict){
		boolean status = false;

		IIndexWord idxWord = dict.getIndexWord(word1, POS.VERB);
		if (idxWord == null) {
			return false;
		}

		for (IWordID wordID : idxWord.getWordIDs()) {
			IWord word = dict.getWord(wordID);
			for(IWord w: word.getSynset().getWords()){
				if(w.getLemma().equalsIgnoreCase(word2)){
					status = true;
					break;
				}
			}
		}
		return status;
	}

	public static void main(String args[]) {
//		 NeoRestConnector restN4J = new NeoRestConnector();
//		 RestAPI restAPI = restN4J.getRestAPI();
//		 List<Long> nodeIDs = new ArrayList<Long>();
//		 nodeIDs.add((long) 1);
//		 nodeIDs.add((long) 563);
//		 nodeIDs.add((long) 561);
//		 OntologyMerger.merge(nodeIDs, restAPI);
		
		// Testing Dictionary
		String path = HRConstats.wordnet_path;
		URL url = null;
		try {
			url = new URL("file", null, path);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		}

		IDictionary dict = new Dictionary(url);
		try {
			dict.open();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		StanfordLemmatizer stLemma = new StanfordLemmatizer();
		String w1 = stLemma.lemmatizeAsString("visit");
		String w2 = stLemma.lemmatizeAsString("see");
		if(isSynonym(w1, w2, dict)){
			System.out.println("True");
		}else{
			System.out.println("False");
		}
	}
}
