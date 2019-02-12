package utils;

public class StringUtils {
	public static boolean isNullOrEmpty(String s){
		boolean result = true;
		if(s != null && !s.isEmpty()){
			result = false;
		}
		
		return result;
	}
}
