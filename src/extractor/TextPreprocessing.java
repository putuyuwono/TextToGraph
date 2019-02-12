package extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
import domain.Tuple;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class TextPreprocessing {
	private static String prevPerson;
	private static String prevCompany;
	private static String prevLocation;

	private static AbstractSequenceClassifier<CoreLabel> classifier;
	static {
		String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
		classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
		prevPerson = "";
	}
	
	private static String modelPath = "model/";
	private static String[] sentenceDetect(String text)
			throws InvalidFormatException, IOException {
		InputStream is = new FileInputStream(modelPath + "en-sent.bin");
		SentenceModel model = new SentenceModel(is);
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		String sentences[] = sdetector.sentDetect(text);
		is.close();
		return sentences;
	}

	public static void main(String[] args) throws Exception {
		String path = "input.txt";
		RandomAccessFile raf = new RandomAccessFile(path, "r");
		String line = raf.readLine();
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		do {
			String[] sentences = sentenceDetect(line);
//			String[] sentences = line.split("[.]");
			line = raf.readLine();
			for (String sentnc : sentences) {
				tuples.addAll(constructTuple(sentnc));
				//System.out.println(sentnc);
			}

		} while (line != null);
		printAll(tuples);
	}
	
	public static ArrayList<Tuple> doProcess(String inputPath) throws IOException{
		RandomAccessFile raf = new RandomAccessFile(inputPath, "r");
		String line = raf.readLine();
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		do {
			String[] sentences = line.split("[.]");
			line = raf.readLine();
			for (String sentnc : sentences) {
				tuples.addAll(constructTuple(sentnc));
			}

		} while (line != null);
		return tuples;
		
	}

	private static void printAll(ArrayList<Tuple> tuples) {
		System.out.println("size : " + tuples.size());
		for (Tuple tuple : tuples) {
			System.out.println(tuple.prefix + " | " + tuple.entity + " | "
					+ tuple.suffix);
		}
	}

	private static ArrayList<Tuple> constructTuple(String stnc) {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		
		// cleaning
		stnc = stnc.replaceAll("\\[\\d*\\]", "").trim().replace("(", "").replace(")", "");
		if (stnc.trim().length() < 1)
			return result;

		// System.out.println(sentnc);
		stnc = stnc.replaceAll("^[([Ss]h)(H)h]e ", prevPerson + " ");
		
		stnc = stnc.replaceAll("^[Tt]hey ", prevCompany + " ");
		stnc = stnc.replaceAll("^[Tt]here ", prevLocation + " ");

		// String res = classifier.classifyWithInlineXML(stnc);
		String res = classifier.classifyToString(stnc);
		//System.out.println(res);
		
		String pref = "";
		String suff = "";
		String entt = "";
		
		String[] tokens = res.split("[\\s,]");
		for (String token : tokens) {
			if (token.contains("/O")) {
				// if found entity
				if (entt.length() > 0) {

					token = token.replaceAll("/.*", "");
					Tuple t = new Tuple();

					Pattern p = Pattern.compile("/\\w+");
					Matcher m = p.matcher(entt);

					if (m.find()) {
						// System.out.println("got : " + m.group(0));
						String tag = m.group(0).replace("/", "").toLowerCase();
						if(tag.compareTo("person")==0){
							prevPerson = entt.replaceFirst("/\\w+", "");
						}else if(tag.compareTo("location")==0){
							prevLocation = entt.replaceFirst("/\\w+", "");
						}else if(tag.compareTo("organization")==0){
							prevCompany = entt.replaceFirst("/\\w+", "");
						}
					}

					t.entity = entt.replaceAll("/\\w+", "");
					t.prefix = pref.replaceAll("/O", "");
					result.add(t);
					entt = "";
					pref = "";
				}
				pref += " " + token;
				// continue;
			} else {
				entt += " " + token;
			}
		}
		if (entt.length() > 0) {
			// word.set(entt);
			// context.write(word, one);
			Tuple t = new Tuple();
			t.entity = entt.replaceAll("/\\w+", "");
			pref = pref.replaceAll("/O", "");
			t.prefix = pref;
			// entt = "";
			// pref = "";
			result.add(t);
		} else if (pref.length() > 0) {
			suff = pref.replaceAll("/O", "");
		}
		// post proccess
		// / NF
		postProcess(result, suff);
		// System.out.println(result.size());
		return result;
	}

	private static void postProcess(ArrayList<Tuple> result, String suff) {
		for (int i = 1; i < result.size(); i++) {
			Tuple t1 = result.get(i - 1);
			Tuple t2 = result.get(i);
			t1.suffix = t2.prefix;
		}
		if (result.size() > 0)
			result.get(result.size() - 1).suffix = suff;
	}
}
