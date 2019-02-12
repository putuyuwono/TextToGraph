package extractor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import utils.StringUtils;
import domain.MyEntity;
import domain.MyRecord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.maxent.Experiments;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

public class SentenceParser {
	private List<String> verbList;
	private List<MyEntity> entityList;
	
	private String sentence;
	private NERParser nerParser;
	
	public SentenceParser(String sentence){
		verbList = new ArrayList<String>();
		entityList = new ArrayList<MyEntity>();
		this.sentence = sentence;
	}
	
	private void parse(){
		//NER Parser
		nerParser = new NERParser(sentence);
		
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords2 = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
		Tree theTree = lp.apply(rawWords2);
		String noun;
		int readNoun = 0;
		for (Tree subtree : theTree) {
			if (subtree.label().value().equals("NP") && subtree.depth() <= 2) {
				noun = serializeToString(subtree.yieldWords());
				String entityType = this.getEntityTypeAsString(noun, nerParser);
				if(!StringUtils.isNullOrEmpty(entityType)){
					entityList.add(new MyEntity(noun, subtree.label().value(), entityType));
				}else{
					MyEntity me = new MyEntity(noun, subtree.label().value(), "UNKNOWN");
					/**
					 * @Yoh
					 * 
					 * Begin of -CompositeEntitySeparator-
					 */
					
					boolean isComposite = false;
					for(Tree subsubtree : subtree.children())
					{
						if(subsubtree.label().value().matches("N+P"))
						{
							StringBuffer listString = new StringBuffer();
							for (Word w : subsubtree.yieldWords())
							{
							    listString.append(w.word()).append(" ");
							}
							me.separatedNodesCandidate().add(listString.toString().trim());
							isComposite = true;
						}
					}
					me.setIsComposite(isComposite);
					
					/**
					 * End of -CompositeEntitySeparator-
					 */
					entityList.add(me);
				}
				readNoun++;
			
			//getting the verb of the triplet here
//			}else if (subtree.label().value().matches("VB\\w*")) {
//				verbList.add(serializeToString(subtree.yieldWords()));
//				readNoun--;
//			}
			}else if (subtree.label().value().matches("VB\\w*") && subtree.depth() <= 2) {
				verbList.add(serializeToString(subtree.yieldWords()));
				readNoun--;
			}
			//Too few Verb
			if(readNoun > 1){
				String verb = getVerbByForce();
				if(!StringUtils.isNullOrEmpty(verb)){
					verbList.add(getVerbByForce());
					readNoun--;
				}
			}//Too many Verb
			else if(readNoun < 0){
				verbList.remove(verbList.size() - 2);
				readNoun++;
			}
		}		
	}
	
	private String getEntityTypeAsString(String entityName, NERParser ner){
		String result = null;
		if(ner!=null){
			for(String person: ner.perList){
				if(person.equalsIgnoreCase(entityName)){
					result = "PERSON";
					break;
				}
			}
			for(String org: ner.orgList){
				if(org.equalsIgnoreCase(entityName)){
					result = "ORGANIZATION";
					break;
				}
			}
			for(String loc: ner.locList){
				if(loc.equalsIgnoreCase(entityName)){
					result = "LOCATION";
					break;
				}
			}
		}
		return result;
	}
	
	public List<MyRecord> buildRecord(){
		List<MyRecord> resultList = new ArrayList<MyRecord>();
		this.parse();
		//First, make sure that #ofNoun - #ofVerb = 1
		if(entityList.size() <= verbList.size()) return resultList;
		//System.out.println("Now, building the records");
		//Next, iterate the verb list, and build the record by combining 2 noun and 1 verb		
		MyRecord rec;
		for(int i=0;i<verbList.size();i++){
			String verb = verbList.get(i);
			if(verb.contains(",")) continue;
			MyEntity n1 = entityList.get(i);
			MyEntity n2 = entityList.get(i+1);
			rec = new MyRecord(n1, verb, n2);
			resultList.add(rec);
		}
//		for(int i=0;i<=entityList.size()-2;i++){
//			String verb = verbList.get(i);
//			if(verb.contains(",")) continue;
//			MyEntity n1 = entityList.get(i);
//			MyEntity n2 = entityList.get(i+1);
//			rec = new MyRecord(n1, verb, n2);
//			resultList.add(rec);
//		}
		return resultList;
	}
	
	public String getVerbByForce(){
		if(entityList.size() < 2) return null;
		
		String n1 = entityList.get(entityList.size()-2).name;
		String n2 = entityList.get(entityList.size()-1).name;
		int startIndex = this.sentence.indexOf(n1);
		int endIndex = this.sentence.indexOf(n2);
		if(startIndex < 0 || endIndex < 0) return null;
		String result = null;
		try{
			result = sentence.substring(startIndex + n1.length(), endIndex-1).trim();
		}catch(Exception e){
			
		}
		//System.out.println("Getting verb forcedly: " + result);
		return result;
	}
	
	public void printAll(List<String> listString){
		for(String s: listString){
			System.out.println(s);
		}
	}
	
	public void printAllEntities(List<MyEntity> listEntity){
		for(MyEntity s: listEntity){
			System.out.println(s);
		}
	}
	
	private String serializeToString(ArrayList<Word> words){
		StringBuilder sb = new StringBuilder();
		for(Word w: words){
			sb.append(w.toString() + " ");
		}
		return sb.toString().trim();
	}
	
	public static void main(String args[]) {
		String input = "He later travelled to Italy to join his family in Pavia.";
		
		System.out.println("Parsing: " + input);
		SentenceParser sp = new SentenceParser(input);
		List<MyRecord> recList = sp.buildRecord();
		
		System.out.println("Noun List: ");
		sp.printAllEntities(sp.entityList);
		System.out.println("\nVerb List: ");
		sp.printAll(sp.verbList);
		
		for(MyRecord rec: recList){
			System.out.println(rec);
		}
		System.out.println("Rec Count: " + recList.size());
	}
}
