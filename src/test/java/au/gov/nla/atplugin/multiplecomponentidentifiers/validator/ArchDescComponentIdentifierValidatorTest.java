package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import java.util.HashSet;

import org.archiviststoolkit.model.ArchDescComponentIdentifiers;
import org.archiviststoolkit.util.ATPropertyValidationSupport;
import org.junit.Test;

import com.jgoodies.validation.message.SimpleValidationMessage;

import static org.junit.Assert.*;


public class ArchDescComponentIdentifierValidatorTest {

	private ArchDescComponentIdentifierValidator validator;
	
	@Test
	public void getPiPrefixTest() {
		setComponentLevelValidatorBaseline();
		assertEquals("nla.ms-ms9915-1.2", validator.getPiPrefix("nla.ms-ms9915-1.2-27"));
		assertEquals("nla.ms-ms9915-1", validator.getPiPrefix("nla.ms-ms9915-1.2"));
	}
	
	@Test
	public void getChildNumberFromPiTest() {
		setComponentLevelValidatorBaseline();
		assertEquals(27, validator.getChildNumberFromPi("nla.ms-ms9915-1.2-27"));
		assertEquals(2, validator.getChildNumberFromPi("nla.ms-ms9915-1.2"));
	}
	
	@Test
	public void checkStartAndEndChildPairsTest() {
		
		setComponentLevelValidatorBaseline();
		validator.checkStartAndEndChildPairs();
		assertEquals(0, validator.support.getResult().size());
				
		setComponentLevelValidatorBaseline();
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1-6-25", "startchild", ""));
		validator.checkStartAndEndChildPairs();
		assertEquals(1, validator.support.getResult().size());
		String msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertEquals("Number of start child and end child identifiers does not match.", msg);
		
		setComponentLevelValidatorBaseline();
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1-6-24.1", "startchild", ""));
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1-6-25.1", "endchild", ""));
		validator.checkStartAndEndChildPairs();
		assertEquals(1, validator.support.getResult().size());
		msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertTrue(msg.contains("identifier patterns do not match."));
		
		setComponentLevelValidatorBaseline();
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1-6-2.10", "startchild", ""));
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1-6-2.9", "endchild", ""));
		validator.checkStartAndEndChildPairs();
		assertEquals(1, validator.support.getResult().size());
		msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertTrue(msg.contains("is not greater than or equal to corresponding start child"));
	}
	
	@Test
	public void checkOnlyOnePersistentIdentifierTest() {
		
		setComponentLevelValidatorBaseline();
		validator.checkOnlyOnePersistentIdentifier();
		assertEquals(0, validator.support.getResult().size());
				
		setComponentLevelValidatorBaseline();
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1", "persistent identifier", ""));
		validator.checkOnlyOnePersistentIdentifier();
		assertEquals(1, validator.support.getResult().size());
		String msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertEquals("Only one persistent identifier can be assigned per component.", msg);
	}
	
	@Test
	public void checkOnlyOneSubunitNumberTest() {
		
		setComponentLevelValidatorBaseline();
		validator.checkOnlyOneSubunitNumber();
		assertEquals(0, validator.support.getResult().size());
				
		setComponentLevelValidatorBaseline();
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1", "subunit number", "Folder"));
		validator.checkOnlyOneSubunitNumber();
		assertEquals(1, validator.support.getResult().size());
		String msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertEquals("Only one subunit number can be assigned per component.", msg);
	}
	
	
	@Test
	public void checkOnlyOneChildrangeTest() {
		
		setComponentLevelValidatorBaseline();
		validator.checkOnlyOneChildrange();
		assertEquals(0, validator.support.getResult().size());
				
		setComponentLevelValidatorBaseline();
		validator.identifiers.add(newArchDescComponentIdentifier("55 - 67", "childrange", "Folder"));
		validator.checkOnlyOneChildrange();
		assertEquals(1, validator.support.getResult().size());
		String msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertEquals("Only one childrange can be assigned per component.", msg);
	}
	
	@Test
	public void componentIdentifierIsMandatory() {
		validator = new ArchDescComponentIdentifierValidator();
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("nla.ms-ms9915", "persistent identifier", "");
		validator.componentIdentifierIsMandatory();
		assertEquals(0, validator.support.getResult().size());
		
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("", "persistent identifier", "");
		validator.componentIdentifierIsMandatory();
		assertEquals(1, validator.support.getResult().size());
		String msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertEquals("Identifier is mandatory.", msg);
	}
	
	@Test
	public void validatePersistentIdentifierPatternTest() {
		validator = new ArchDescComponentIdentifierValidator();
		validator.persistentIdentifier = new PersistentIdentifier();
		validator.persistentIdentifier.CONFIG_FILE = "persistentIdentifierPatterns.txt";
		validator.persistentIdentifier.loadConfigFile();
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("nla.ms-ms9915", "persistent identifier", "");
		validator.validatePersistentIdentifierPattern();
		
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("nla.ms-asasdas", "persistent identifier", "");
		validator.validatePersistentIdentifierPattern();
		assertEquals(1, validator.support.getResult().size());
	}
	
	@Test
	public void checkForSubunitNumberTest() {
		validator = new ArchDescComponentIdentifierValidator();
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("6", "subunit number", "Folder");
		validator.checkForSubunitNumber();
		
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("6", "subunit number", "");
		validator.checkForSubunitNumber();
		assertEquals(1, validator.support.getResult().size());
		String msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertEquals("Subunit number requires a label.", msg);
	}
	
	@Test
	public void checkForChildrangeTest() {
		validator = new ArchDescComponentIdentifierValidator();
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("6/1 - 6/34", "childrange", "items");
		validator.checkForChildrange();
		
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.model = newArchDescComponentIdentifier("6/1 - 6/34", "childrange", "");
		validator.checkForChildrange();
		assertEquals(1, validator.support.getResult().size());
		String msg = ((SimpleValidationMessage)validator.support.getResult().getMessages().get(0)).formattedText();
		assertEquals("Child range identifier requires a label.", msg);
	}
	
	// Convenience Methods
	//////////////////////
	private void setComponentLevelValidatorBaseline() {
		
		validator = new ArchDescComponentIdentifierValidator();
		validator.support = new ATPropertyValidationSupport(new ArchDescComponentIdentifiers(), "Mulitple Component Identifiers");
		validator.identifiers = new HashSet<ArchDescComponentIdentifiers>();
		
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915", "persistent identifier", ""));
		validator.identifiers.add(newArchDescComponentIdentifier("6", "subunit number", "Folder"));
		validator.identifiers.add(newArchDescComponentIdentifier("6/1 - 6/34", "childrange", "items"));
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1-6-25", "startchild", ""));
		validator.identifiers.add(newArchDescComponentIdentifier("nla.ms-ms9915-1-6-30", "endchild", ""));
	}

	private ArchDescComponentIdentifiers newArchDescComponentIdentifier(String componentIdentifier, String type, String label) {
		ArchDescComponentIdentifiers identifier;
		identifier = new ArchDescComponentIdentifiers();
		identifier.setComponentIdentifier(componentIdentifier);
		identifier.setIdentifierType(type);
		identifier.setIdentifierLabel(label);
		return identifier;
	}
	
}
