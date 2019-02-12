package extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.StringUtils;
import domain.MyEntity;
import domain.MyRecord;

/**
 * This class will be used for convertin pronoun to be real object name
 * @author hduser
 *
 */
public class PronounHandler {
	public List<MyRecord> processedRecords;
	String prevPerson, prevOrg, prevLoc, prevObj, prevPluralObj;
	public PronounHandler(List<MyRecord> recordList){
		processedRecords = new ArrayList<MyRecord>();
		this.processedRecords.addAll(recordList);
	}
	
	public List<MyRecord> convertPronoun(){
		for(MyRecord rec: processedRecords){
			checkEntityName(rec.entity1);
			checkEntityName(rec.entity2);
		}
		return processedRecords;
	}
	
	private void checkEntityName(MyEntity entity){
		String name = entity.name;
		String type = entity.type;
		
		switch (type) {
		case "PERSON":
			prevPerson = name;
			//System.out.println("PERSON Noted: " + name);
			break;
		case "ORGANIZATION":
			prevOrg = name;
			//System.out.println("ORGANIZATION Noted: " + name);
			break;
		case "LOCATION":
			prevLoc = name;
			//System.out.println("LOCATION Noted: " + name);
			break;
		case "UNKNOWN":
			//System.out.print("Gotta Changes: " + name + " into ");
			if(name.equalsIgnoreCase("he") || name.equalsIgnoreCase("she")){
				if(!StringUtils.isNullOrEmpty(prevPerson)){
					entity.name = prevPerson;
					//System.out.print("PERSON ");
				}else{
					//System.out.print("NULL-PERSON ");
				}
			}else if(name.equalsIgnoreCase("it")){
				if(!StringUtils.isNullOrEmpty(prevOrg)){
					entity.name = prevOrg;
					//System.out.print("ORG ");
				}else if(!StringUtils.isNullOrEmpty(prevObj)){
					entity.name = prevObj;
					//System.out.print("OBJ ");
				}else{
					//System.out.print("NULL-OBJ ");
				}
			}else if(name.equalsIgnoreCase("they")){
				if(!StringUtils.isNullOrEmpty(prevPluralObj)){
					entity.name = prevPluralObj;
					System.out.print("PLU-OBJ ");
				}else if(!StringUtils.isNullOrEmpty(prevOrg)){
					entity.name = prevOrg;
					System.out.print("ORG ");
				}else if(!StringUtils.isNullOrEmpty(prevObj)){
					entity.name = prevObj;
					System.out.print("OBJ ");
				}else{
					System.out.print("NULL-ORG ");
				}
			}else if(name.equalsIgnoreCase("there")){
				if(!StringUtils.isNullOrEmpty(prevLoc)){
					entity.name = prevLoc;
					//System.out.print("LOC ");
				}else{
					//System.out.print("NULL-LOC ");
				}
			}else{
				//do nothing, just keep it remains the same
				//System.out.print("Nothing ");
				if(name.contains(" and ")){
					prevPluralObj = name;
				}
				prevObj = name;
			}
			break;				
		default:
			break;
		}	
	}

	public static void main(String args[]) throws IOException{
		String input = "test.txt";
		TextParser tp = new TextParser(input);
		tp.doProcess();
		List<MyRecord> list = tp.getRecordList();
		System.out.println("Before..");
		for(MyRecord rec: list){
			System.out.println(rec);
		}
		PronounHandler ph = new PronounHandler(list);
		List<MyRecord> phList = ph.convertPronoun();
		System.out.println("After..");
		for(MyRecord rec: phList){
			System.out.println(rec);
		}
	}
}
