package multiplecomponentidentifiers;

import static org.junit.Assert.*;

import org.archiviststoolkit.plugin.ATPlugin;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import au.nla.gov.atplugin.multiplecomponentidentifiers.MultipleComponentIdentifiers;

public class MultipleComponentIdentifiersTest {
	
	MultipleComponentIdentifiers multipleComponentIdentifiers;
	
	@Before
	public void setup() {
		multipleComponentIdentifiers = new MultipleComponentIdentifiers();
	}
	
	@Test
	public void testPluginCategory() {
		assertEquals(ATPlugin.EMBEDDED_EDITOR_CATEGORY, multipleComponentIdentifiers.getCategory());
	}
	
	@Test
	public void testPluginEditorType() {
		assertTrue(multipleComponentIdentifiers.getEditorType().contains(ATPlugin.RESOURCE_EDITOR));
		assertTrue(multipleComponentIdentifiers.getEditorType().contains(ATPlugin.RESOURCE_COMPONENT_EDITOR));
	}
}
