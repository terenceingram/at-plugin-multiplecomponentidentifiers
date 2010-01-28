package au.gov.nla.atplugin.multiplecomponentidentifiers;

import org.java.plugin.Plugin;
import org.archiviststoolkit.plugin.ATPlugin;
import org.archiviststoolkit.ApplicationFrame;
import org.archiviststoolkit.hibernate.SessionFactory;
import org.archiviststoolkit.util.UserPreferences;
import org.archiviststoolkit.dialog.ErrorDialog;
import org.archiviststoolkit.dialog.ATFileChooser;
import org.archiviststoolkit.model.*;
import org.archiviststoolkit.editor.AccessionFields;
import org.archiviststoolkit.editor.ArchDescriptionFields;
import org.archiviststoolkit.editor.ResourceComponentsFields;
import org.archiviststoolkit.editor.ResourceFields;
import org.archiviststoolkit.swing.InfiniteProgressPanel;
import org.archiviststoolkit.swing.ATProgressUtil;
import org.archiviststoolkit.mydomain.*;

import au.gov.nla.atplugin.multiplecomponentidentifiers.panel.ResourceBasicInfoPanel;
import au.gov.nla.atplugin.multiplecomponentidentifiers.panel.ResourceComponentBasicInfoPanel;
 
import javax.swing.*;

import java.awt.*;
import java.util.HashMap;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.io.*;
 
/**
* Embedded Plugin.
* 
* Plugin to amend the ResourceFields and ResourceComponentFields.
*
* @author: tingram
* Date: Jan 12, 2010
*/
 
public class PluginImpl extends Plugin implements ATPlugin {
	
	
	protected Resources resources;
	protected ResourcesComponents resourcesComponents;
	
	protected DomainEditorFields editorField;
	private ResourceBasicInfoPanel resourceBasicInfoPanel = null;
	private ResourceComponentBasicInfoPanel resourceComponentBasicInfoPanel = null;
	
	protected ApplicationFrame mainFrame;
	private JTable callingTable;
	private int selectedRow;
	    
    public PluginImpl() { }
    
    /**
     * Returns the name of the plugin.
     */
    public String getName() {
        return "Mulitple Component Identifiers";
    }
    
    /**
     * Returns plugin category = ATPlugin.EMBEDDED_EDITOR_CATEGORY
     */
    public String getCategory() {
        return ATPlugin.EMBEDDED_EDITOR_CATEGORY;
    }
    
   /**
    * Returns plugin EditorType = ATPlugin.RESOURCE_EDITOR and ATPlugin.RESOURCE_COMPONENT_EDITOR
    */
    public String getEditorType() {
    	return ATPlugin.RESOURCE_EDITOR + " " + ATPlugin.RESOURCE_COMPONENT_EDITOR;
    }
 
    // Method to set the main frame
    public void setApplicationFrame(ApplicationFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
 
    // Method that display the window
    public void showPlugin() {
    }
 
    // method to display a plugin that needs a parent frame
    public void showPlugin(Frame owner) {
    }
 
    // method to display a plugin that needs a parent dialog
    public void showPlugin(Dialog owner) {
	}
	
    /**
     * Use this to create a JPanel for a tab
     * Method to return the jpanels for plugins that are in an AT editor.
     */
	public HashMap getEmbeddedPanels() {
		HashMap<String, JPanel> panels = new HashMap<String,JPanel>();
		//panels.put("Component Identifiers", new ComponentIdentifiersPanel(currentDomainObject));
		
		if(editorField != null) {
			if (editorField instanceof ResourceFields) {
				System.out.println(editorField);
				if (resourceBasicInfoPanel == null) {
					System.out.println("Panel null so creating new one" + "\n");
					resourceBasicInfoPanel = new ResourceBasicInfoPanel(editorField.detailsModel);
				}
				resourceBasicInfoPanel.setEditorField(editorField);
				panels.put("Basic Information::0::yes", resourceBasicInfoPanel);

			} else if (editorField instanceof ResourceComponentsFields) {
				System.out.println(editorField);
				if (resourceComponentBasicInfoPanel == null) {
					System.out.println("Panel null so creating new one" + "\n");
					resourceComponentBasicInfoPanel = new ResourceComponentBasicInfoPanel(editorField.detailsModel);
				}
				resourceComponentBasicInfoPanel.setEditorField(editorField);
				panels.put("Basic Information::0::yes", resourceComponentBasicInfoPanel);
			}
		}
		
		return panels;
	}
	 
	public void setEditorField(DomainEditorFields domainEditorFields) {
		this.editorField = domainEditorFields;
	}
	
	// This will be called by the ResourcesFields and ResourcesComponentFields
	// Method to set the editor field component
	public void setEditorField(ArchDescriptionFields archDescriptionFields) {
		this.editorField = archDescriptionFields;
	}
	 
	/**
	* DomainObject model expecting either the:
	* - org.archiviststoolkit.model.Resources
	* - org.archiviststoolkit.model.ResourcesComponents
	*/
	public void setModel(DomainObject domainObject, InfiniteProgressPanel monitor) {
		
		System.out.println("setModel: " + Integer.toHexString(System.identityHashCode(domainObject)));
		
		if (domainObject instanceof Resources) {
			resources = (Resources)domainObject;
			System.out.println("Resource: " + Integer.toHexString(System.identityHashCode(resources)));
		} else if (domainObject instanceof ResourcesComponents) {
			resourcesComponents = (ResourcesComponents)domainObject;
			System.out.println("Component: " + resourcesComponents);
			resourceComponentBasicInfoPanel.setModel(resourcesComponents);
		}
	}
	 
	/**
	* Method to get the table from which the record was selected
	* @param callingTable The table containing the record
	*/
	public void setCallingTable(JTable callingTable) {
		this.callingTable = callingTable;
	}
	 
	/**
	* Method to set the selected row of the calling table
	* @param selectedRow
	*/
	public void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
	}
	 
	/**
	* Method to set the current record number along with the total number of records
	* @param recordNumber The current record number
	* @param totalRecords The total number of records
	*/
	public void setRecordPositionText(int recordNumber, int totalRecords) { }
	 
    // Method to do a specific task in the plugin
    public void doTask(String task) {
        //frame = new ATPluginDemo1Frame(this);
        //frame.textArea1.setText("Doing Task : " + task + "\n\n");
        //frame.setApplicationFrame(mainFrame);
        //frame.setVisible(true);
    }
 
    // Method to get the list of specific task the plugin can perform
    public String[] getTaskList() {
        return null;
    }
 
    // code that is executed when plugin starts. not used here
    protected void doStart() { }
 
    // code that is executed after plugin stops. not used here
    protected void doStop() { }
 
    // main method for testing only
    public static void main(String[] args) {
    }
	
}