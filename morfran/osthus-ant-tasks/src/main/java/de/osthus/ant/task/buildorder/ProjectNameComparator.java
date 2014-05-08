package de.osthus.ant.task.buildorder;

import java.util.Comparator;

public class ProjectNameComparator implements Comparator<String> {
	
	private static String DATA   = "(?i).*data";
	private static String CORE   = "(?i)(?:core|sharedlib).*";
	private static String CLIENT = "(?i).*client";
	
	
	@Override
	public int compare(String projectA, String projectB) {
		String a = projectA.toLowerCase();
		String b = projectB.toLowerCase();
		
		
//		if ( (a.matches(DATA) && b.matches(DATA)) || (a.matches(CORE) && b.matches(CORE)) || (a.matches(CLIENT) && b.matches(CLIENT)) ) {
//			return 0;
//		} else 
		if ( a.matches(DATA) ^ b.matches(DATA) ) {
			return b.matches(DATA) ? 1 : -1;
		} else if ( a.matches(CORE) ^ b.matches(CORE) ) {
			return b.matches(CORE) ? 1 : -1;
		} else if ( a.matches(CLIENT) ^ b.matches(CLIENT) ) {
			return b.matches(CLIENT) ? 1 : -1;
		}
		
		return a.compareTo(b);
	}

}
