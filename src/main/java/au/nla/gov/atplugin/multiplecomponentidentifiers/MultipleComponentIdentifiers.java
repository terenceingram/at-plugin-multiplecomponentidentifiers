package au.nla.gov.atplugin.multiplecomponentidentifiers;

import org.java.plugin.Plugin;
import org.archiviststoolkit.plugin.ATPlugin;
import org.archiviststoolkit.ApplicationFrame;
import org.archiviststoolkit.hibernate.SessionFactory;
import org.archiviststoolkit.util.UserPreferences;
import org.archiviststoolkit.dialog.ErrorDialog;
import org.archiviststoolkit.dialog.ATFileChooser;
import org.archiviststoolkit.model.*;
import org.archiviststoolkit.editor.ArchDescriptionFields;
import org.archiviststoolkit.swing.InfiniteProgressPanel;
import org.archiviststoolkit.swing.ATProgressUtil;
import org.archiviststoolkit.mydomain.*;
 
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
* Interface to amend the ResourceFields and ResourceComponentFields.
*
* @author: tingram
* Date: Jan 12, 2010
*/
 
public class MultipleComponentIdentifiers extends Plugin implements ATPlugin {
	
	
	protected Resources resources;
	protected ResourcesComponents resourcesComponents;
	protected DomainObject currentDomainObject;
	
	protected ApplicationFrame mainFrame;
	//private ATPluginDemo1Frame frame;
	protected ArchDescriptionFields parentEditorFields;
	 
	//protected YaleAnalogInstancesFields editorFields;
	private DomainEditor analogInstanceEditor;
	private JTable callingTable;
	private int selectedRow;
	protected ArchDescriptionAnalogInstances analogInstance;
	    
    public MultipleComponentIdentifiers() { }
    
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
		panels.put("Component Identifiers", new ComponentIdentifiersPanel(currentDomainObject));
		return panels;
	}
	 
	public void setEditorField(DomainEditorFields domainEditorFields) {
	}
	
	// This will be called by the ResourcesFields and ResourcesComponentFields
	// Method to set the editor field component
	public void setEditorField(ArchDescriptionFields editorField) {
	}
	 
	/**
	* DomainObject model expecting either the:
	* - org.archiviststoolkit.model.Resources
	* - org.archiviststoolkit.model.ResourcesComponents
	*/
	public void setModel(DomainObject domainObject, InfiniteProgressPanel monitor) {
		if (domainObject instanceof Resources) {
			resources = (Resources)domainObject;
		} else if (domainObject instanceof ResourcesComponents) {
			resourcesComponents = (ResourcesComponents)domainObject;
		}
		this.currentDomainObject = domainObject;
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
    	MultipleComponentIdentifiers demo = new MultipleComponentIdentifiers();
        demo.showPlugin();
        
        
    }
	
}