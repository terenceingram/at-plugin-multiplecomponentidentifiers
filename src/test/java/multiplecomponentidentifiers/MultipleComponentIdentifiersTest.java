package multiplecomponentidentifiers;

import static org.junit.Assert.*;

import org.archiviststoolkit.plugin.ATPlugin;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import au.gov.nla.atplugin.multiplecomponentidentifiers.PluginImpl;

public class MultipleComponentIdentifiersTest {
	
	PluginImpl multipleComponentIdentifiers;
	
	@Before
	public void setup() {
		multipleComponentIdentifiers = new PluginImpl();
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
