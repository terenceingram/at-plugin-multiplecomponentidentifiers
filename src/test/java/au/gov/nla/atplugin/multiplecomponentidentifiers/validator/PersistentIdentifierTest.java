package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import au.gov.nla.atplugin.multiplecomponentidentifiers.validator.PersistentIdentifier;


public class PersistentIdentifierTest {

	private PersistentIdentifier persistentIdentifier;
	
	@Before
	public void setup() throws Exception {
		persistentIdentifier = new PersistentIdentifier();
		persistentIdentifier.CONFIG_FILE = "persistentIdentifierPatterns.txt";
		persistentIdentifier.loadConfigFile();
	}
	
	@Test
	public void testValidate() {
		assertTrue("Expected Persistent Identifier to pass validation", persistentIdentifier.validate("nla.ms-ms9551"));
		assertFalse(persistentIdentifier.validate("nla.ms-mc9551"));
	}
	
}
