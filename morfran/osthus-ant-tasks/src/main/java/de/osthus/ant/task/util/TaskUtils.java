package de.osthus.ant.task.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class TaskUtils {

	public static File findYoungestFile(FileSet... fileSets) {
		
		if ( fileSets == null || fileSets.length == 0 )
			return null;
		
		File youngest = null, temp, result = null;
		
		for ( FileSet fs: fileSets ) {
			try {
				fs.getDirectoryScanner().scan();
				String[] fileNames = fs.getDirectoryScanner().getIncludedFiles();
				for ( String fileName: fileNames ) {
					temp = new File(fs.getDir() + File.separator + fileName);
					if ( youngest == null || temp.lastModified() > youngest.lastModified() ) {
						youngest = temp;
					}
				}
				if ( result == null || youngest.lastModified() > result.lastModified() ) {
					result = youngest;
				}
			} catch (Exception e) {
				// ignore
			}
		}
		
		return result;	
	}
	
	public static Map<String, Long> listFiles(Collection<FileSet> fileSets, Pattern matchPattern) {
		Map<String, Long> files = new HashMap<String, Long>();
		File temp;
		String[] fileNames;
		
		for ( FileSet fileSet: fileSets ) {
			try {
			fileSet.getDirectoryScanner().scan();
			fileNames = fileSet.getDirectoryScanner().getIncludedFiles();
			for ( String fileName: fileNames ) {				
				temp = new File(fileSet.getDir() + File.separator + fileName);				
				if ( temp.getName().matches(matchPattern.pattern()) ) {				
					temp.lastModified();				
					files.put(fileName, temp.lastModified());
				}
			}
			} catch (Exception e) {
				System.out.println("Skipping: " + e);
			}
		}
		return files;
	}	
	
	
	public static String generateVeryVeryComplexHash(File f) throws IOException {
		String hashSeparator= "/";
		String hashSHA = Files.hash(f, Hashing.sha256()).toString();
		String hashMD5 = Files.hash(f, Hashing.md5()).toString();
		return hashSHA + hashSeparator + hashMD5;
	}
	
	public static String getPropertyNameByValue(String value, Properties properties) {
		if ( properties.containsValue(value) ) {
			for ( Object key: properties.keySet() ) {
				if ( value.equals(properties.getProperty((String) key)) ) {
					return (String) key;
				}
			}
		}
		return null;		
	}	
	
	public static void main(String[] args) throws IOException {
		System.out.println(generateVeryVeryComplexHash(new File("D:/temp/comp/jAmbeth.Cache.File-0.0.5.jar")));
		System.out.println(generateVeryVeryComplexHash(new File("D:/temp/comp/jAmbeth.Cache.File-0.0.6.jar")));
	}
	
	
	public static void sleep(long millis) throws BuildException {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new BuildException(e);
		}
	}
	
	
	/**
	 * BEWARE: no deep copy!
	 * 
	 */
	public static Object copy(Object source) {
		try {
			Object target = Class.forName(source.getClass().getName()).newInstance();
			
			Field targetField;
			for ( Field sourceField: source.getClass().getDeclaredFields() ) {
				targetField = target.getClass().getDeclaredField(sourceField.getName());
				sourceField.setAccessible(true);
				targetField.setAccessible(true);
				targetField.set(target, sourceField.get(source));
			}
			return target;
		} catch (Exception e) {
			throw new RuntimeException("Cannot copy objects!", e);
		}
	}	
}
