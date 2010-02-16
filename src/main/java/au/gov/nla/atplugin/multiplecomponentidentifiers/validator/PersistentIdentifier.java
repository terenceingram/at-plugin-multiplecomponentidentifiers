package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.gov.nla.atplugin.multiplecomponentidentifiers.PluginImpl;

/**
 * Loads a text file with regular expressions for valid patterns for Persistent Identifiers.
 * main usage will be validate.
 * 
 * @author tingram
 *
 */
public class PersistentIdentifier {
	
	private File patternFile;
	private List<PiStruct> piStructList = null;
	private final String CONFIG_FILE = "/resources/persistentIdentifierPatterns.txt";
	
	public PersistentIdentifier() {
		
		try {
			ClassLoader classLoader = PluginImpl.class.getClassLoader();
			URL resource = classLoader.getResource(CONFIG_FILE);
			InputStream inputStream = resource.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			loadPatterns(br);
		} catch (Exception e) {
		      System.err.println("Unable to load config file: " + CONFIG_FILE + " " + e);
		}
	}
	
	/**
	 * Validates the persistentIdentifier against the default patterns from 
	 * the piPatterns.txt file. Returns true if valid, otherwise false.
	 * 
	 * @return boolean
	 */
	public boolean validate(String persistentIdentifier) {
	    PiStruct piStruct = null;
	    for (PiStruct ps : piStructList) {
	      if (ps.pattern.matcher(persistentIdentifier).matches()) return true;
	    }
	    return false;
	}
	
	private void loadPatterns(BufferedReader br) throws IOException, Exception {
		List<PiStruct> lst = new ArrayList<PiStruct>();
	    String regexp = "^([^\t]+)\\t(\\d+)$";
	    Pattern pattern = Pattern.compile(regexp);
	    String line;

	    while ((line = br.readLine()) != null) {
	      line = line.trim();
	      if (!(line.equals("") || line.startsWith("#"))) {   // Comments
	        Matcher matcher = pattern.matcher(line);
	        if (matcher.matches()) {
	          lst.add(new PiStruct(matcher.group(1), Integer.parseInt(matcher.group(2))));
	        } else {
	          br.close();
	          throw new Exception("Error reading pattern file line: " + line);
	        }
	      }
	    }
	    br.close();

	    if (lst.size() > 0) {
	      piStructList = lst;
	    } else {
	      throw new Exception("Error reading pattern file: no entries.");
	    }
	}
	
	class PiStruct {
	    String patternStr;
	    int dirLevel;
	    Pattern pattern;

	    public PiStruct(String patternStr, int dirLevel) {
	      this.patternStr = patternStr;
	      this.pattern = Pattern.compile(patternStr);
	      this.dirLevel = dirLevel;
	    }
	  }
}
