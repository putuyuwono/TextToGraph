package extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
import domain.MyRecord;

/**
 * First, This class will parse all of File's content into several sentence.
 * Then, It will construct record for building graph (MyRecord)
 * @author hduser
 *
 */
public class TextParser {	
	private String fileInputPath;
	private String modelPath = "model/";
	private List<MyRecord> recordList;
	
	public TextParser(String inputPath){
		this.fileInputPath = inputPath;
		recordList = new ArrayList<MyRecord>();
	}
	
	private String[] sentenceDetect(String text) throws InvalidFormatException, IOException {
		InputStream is = new FileInputStream(modelPath + "en-sent.bin");
		SentenceModel model = new SentenceModel(is);
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		String sentences[] = sdetector.sentDetect(text);
		is.close();
		return sentences;
	}
	
	private String read(String filename) throws IOException {
		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner = new Scanner(new FileInputStream(filename));
		try {
			while (scanner.hasNextLine()) {
				text.append(scanner.nextLine() + NL);
			}
		} finally {
			scanner.close();
		}
		// System.out.println("Text read in: " + text);
		return text.toString();
	}
	
	private void extractSentencesFromFile(String inputPath) throws IOException{
		String text = this.read(inputPath);
		String sentences[] = this.sentenceDetect(text);
		for(String s: sentences){
			System.out.println("Parsing: " + s);
			SentenceParser sp = new SentenceParser(s);
			List<MyRecord> resultList = sp.buildRecord();
			for(MyRecord rec: resultList){
				recordList.add(rec);
				System.out.println(rec.toString());
			}
			System.out.println("Done. Result: " + resultList.size());
		}
	}
	
	public void doProcess() throws IOException{
		this.extractSentencesFromFile(fileInputPath);
	}
	
	public List<MyRecord> getRecordList(){
		return this.recordList;
	}
}
