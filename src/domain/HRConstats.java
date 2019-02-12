/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import java.io.FileInputStream;
import opennlp.tools.sentdetect.SentenceModel;

/**
 *
 * @author sym
 */
public class HRConstats {
    public static final String root ="/home/hduser/GraphReprocessing/"; 
    public static final String classifier_path=root+"classifiers/english.all.3class.distsim.crf.ser.gz";
    public static final String sentenceModel_path=root+"model/en-sent.bin";
    
    public static LexicalizedParser lexicalParser;
    public static final String wordnet_path = root+"dict/";
    
    // models
    public static AbstractSequenceClassifier<CoreLabel> classifier;
    public static SentenceModel sentenceModel;
    
    
    static{
         classifier = CRFClassifier.getClassifierNoExceptions(classifier_path);
         lexicalParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        try {
            FileInputStream fis =  new FileInputStream(sentenceModel_path);
            sentenceModel = new SentenceModel(fis);
            fis.close();
        } catch (Exception ex) {}
    }
		
    
    
    
    public static String rdb_url = "jdbc:mysql://164.125.50.92:3306/relations";
	public static String rdb_user = "bdrcrelation";
	public static String rdb_password = "dblab3535";
	
	public static String owl_type = "RDF/XML-ABBREV";
	public static String owl_path = root + "owl/humanrel.owl";
	
	
	public static String ONTO_URI = "http://164.125.50.92/humanrel/ontologies/2013/5/humanrel.owl#";
}