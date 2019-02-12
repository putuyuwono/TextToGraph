package extractor;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class NERParser {
	private String rawText;
	public List<String> perList;
	public List<String> orgList;
	public List<String> locList;
	
	private AbstractSequenceClassifier<CoreLabel> classifier;
	
	public NERParser(String sentence){
		String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
		classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		this.rawText = classifier.classifyWithInlineXML(sentence);
		runAll();
	}
	
	private void runAll() {		
		perList = find("PERSON");
		orgList = find("ORGANIZATION");
		locList = find("LOCATION");
	}
	
	private List<String> find(String entityType) {
		List<String> listResult = new ArrayList<String>();
		String openTag = "<" + entityType.toUpperCase() + ">";
		String closeTag = "</" + entityType.toUpperCase() + ">";
		String result = "";
		int startIndex = 0, endIndex = 0;
		while (startIndex != -1) {
			// find opening tag
			startIndex = rawText.indexOf(openTag, startIndex);
			if (startIndex != -1) {
				startIndex += openTag.length();
				// find closing tag
				endIndex = rawText.indexOf(closeTag, startIndex);
				if(endIndex != -1){
					result = rawText.substring(startIndex, endIndex);
					listResult.add(result);					
					startIndex = endIndex;
				}
			}
		}

		return listResult;
	}
	
	public void printAll(List<String> listString){
		for(String s: listString){
			System.out.println(s);
		}
	}
	
	public static void main(String args[]){
		String inp = "Thomas and John make a bike";
		NERParser np = new NERParser(inp);
		System.out.println("Person List: ");
		np.printAll(np.perList);
		System.out.println("\nOrg List: ");
		np.printAll(np.orgList);
		System.out.println("\nLoc List: ");
		np.printAll(np.locList);
	}
}
