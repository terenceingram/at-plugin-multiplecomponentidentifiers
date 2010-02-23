package au.gov.nla.atplugin.multiplecomponentidentifiers.mydomain;

import org.archiviststoolkit.mydomain.*;
import org.archiviststoolkit.model.*;
import org.archiviststoolkit.swing.StandardEditor;
import org.archiviststoolkit.exceptions.ObjectNotRemovedException;
import org.archiviststoolkit.exceptions.DomainEditorCreationException;
import org.archiviststoolkit.ApplicationFrame;
import org.archiviststoolkit.plugin.ATPlugin;
import org.archiviststoolkit.plugin.ATPluginFactory;
import org.archiviststoolkit.dialog.ErrorDialog;

import au.gov.nla.atplugin.multiplecomponentidentifiers.editor.ArchDescComponentIdentifiersFields;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Common functionality shared between Resources and ResoucesComponets tabs.
 * 
 * @author tingram
 *
 */
public abstract class NLADomainEditorFields extends DomainEditorFields {
	
	private static final long serialVersionUID = 7048825913960121458L;
	
	protected DomainEditorFields editorField;
	
	/**
	 * The "Add Identifier" button is clicked on the panel.
	 * 
	 * @param identifierTable
	 */
	protected void addIdentifierActionPerformed(DomainSortableTable identifierTable, Resources model) {
		ArchDescComponentIdentifiers newArchDescComponentIdentifier;
		DomainEditor dialog = new DomainEditor(ArchDescComponentIdentifiers.class, editorField.getParentEditor(), "Add Identifier", new ArchDescComponentIdentifiersFields());
		dialog.setNavigationButtonListeners((ActionListener)editorField.getParentEditor());
		dialog.setNewRecord(true);
				
		boolean done = false;
		int returnStatus;
		while (!done) {
			newArchDescComponentIdentifier = new ArchDescComponentIdentifiers(model);
			dialog.setModel(newArchDescComponentIdentifier, null);
			returnStatus = dialog.showDialog();
			
			if (returnStatus == JOptionPane.OK_OPTION) {
				model.addArchDescComponentIdentifier(newArchDescComponentIdentifier);
				identifierTable.updateCollection(model.getArchDescComponentIdentifiers());
				done = true;
			} else if (returnStatus == StandardEditor.OK_AND_ANOTHER_OPTION) {
				model.addArchDescComponentIdentifier(newArchDescComponentIdentifier);
				identifierTable.updateCollection(model.getArchDescComponentIdentifiers());
			} else {
				done = true;
			}
		}	
	}
	
	protected void addIdentifierActionPerformed(DomainSortableTable identifierTable, ResourcesComponents model) {
		ArchDescComponentIdentifiers newArchDescComponentIdentifier;
		DomainEditor dialog = new DomainEditor(ArchDescComponentIdentifiers.class, editorField.getParentEditor(), "Add Identifier", new ArchDescComponentIdentifiersFields());
		dialog.setNavigationButtonListeners((ActionListener)editorField.getParentEditor());
		dialog.setNewRecord(true);
				
		boolean done = false;
		int returnStatus;
		while (!done) {
			newArchDescComponentIdentifier = new ArchDescComponentIdentifiers(model);
			dialog.setModel(newArchDescComponentIdentifier, null);
			returnStatus = dialog.showDialog();
			
			if (returnStatus == JOptionPane.OK_OPTION) {
				model.addArchDescComponentIdentifier(newArchDescComponentIdentifier);
				identifierTable.updateCollection(model.getArchDescComponentIdentifiers());
				done = true;
			} else if (returnStatus == StandardEditor.OK_AND_ANOTHER_OPTION) {
				model.addArchDescComponentIdentifier(newArchDescComponentIdentifier);
				identifierTable.updateCollection(model.getArchDescComponentIdentifiers());
			} else {
				done = true;
			}
		}
		
	}
	
	public void removeRelatedTableRow(DomainGlazedListTable relatedTable, DomainObject model) throws ObjectNotRemovedException {
		int selectedRow = relatedTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "You must select a row to remove.", "warning", JOptionPane.WARNING_MESSAGE);
		} else {
           int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + relatedTable.getSelectedRows().length + " record(s)",
                    "Delete records", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.OK_OPTION) {
                ArrayList<DomainObject> relatedObjects = relatedTable.removeSelectedRows();
                for (DomainObject relatedObject: relatedObjects) {
                    model.removeRelatedObject(relatedObject);
                }
                int rowCount = relatedTable.getRowCount();
                if (rowCount == 0) {
                    // do nothing
                } else if (selectedRow >= rowCount) {
                    relatedTable.setRowSelectionInterval(rowCount - 1, rowCount - 1);
                } else {
                    relatedTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
				//set record to dirty
				ApplicationFrame.getInstance().setRecordDirty();
            }
        }
	}

	protected int editRelatedRecord(DomainGlazedListTable table, Class clazz, Boolean buffered, DomainEditor domainEditor) {
		int selectedRow = table.getSelectedRow();
		if (selectedRow != -1) {
			DomainObject domainObject = table.getSortedList().get(selectedRow);
			if (domainEditor == null) {
				try {
					domainEditor = DomainEditorFactory.getInstance()
						.createDomainEditorWithParent(clazz, editorField.getParentEditor(), false);
					domainEditor.setCallingTable(table);
				} catch (UnsupportedTableModelException e1) {
					new ErrorDialog(editorField.getParentEditor(), "Error creating editor for " + clazz.getSimpleName(), e1).showDialog();
				} catch (DomainEditorCreationException e) {
					new ErrorDialog(editorField.getParentEditor(), "Error creating editor for " + clazz.getSimpleName(), e).showDialog();
				}
			}
			domainEditor.setBuffered(buffered);
			domainEditor.setSelectedRow(selectedRow);
			domainEditor.setNavigationButtons();
			domainEditor.setModel(domainObject, null);
			int returnValue =  domainEditor.showDialog();
			if (domainEditor.getBuffered()) {
				if (returnValue == JOptionPane.CANCEL_OPTION) {
					domainEditor.editorFields.cancelEdit();
				} else {
					domainEditor.editorFields.acceptEdit();
                    ApplicationFrame.getInstance().setRecordDirty(); // ok an edit was made, so set the record dirty
				}
			}
			return returnValue;
		} else {
			return JOptionPane.CANCEL_OPTION;
		}
	}

	protected int editRelatedRecord(DomainGlazedListTable table, Class clazz, Boolean buffered) {
		return editRelatedRecord(table, clazz, buffered, null);
	}

	public DomainEditorFields getEditorField() {
		return editorField;
	}

	public void setEditorField(DomainEditorFields editorField) {
		this.editorField = editorField;
	}


	/**
	 * Method to load a custom plugin domain editor for viewing the record. Usefull if someone want to
	 * implemment an editor that is more suited for their workflow or to load a read only viewer for the
	 * record.
	 *
	 * @param domainObject The record to edit
	 * @return Whether any plugin editors where found
	 */
	protected boolean usePluginDomainEditor(boolean newInstance, DomainObject domainObject, DomainSortableTable callingTable) {
		ATPlugin plugin = ATPluginFactory.getInstance().getEditorPlugin(domainObject);

		if (plugin == null) { // just return false and so that the built in domain object can be used
			return false;
		}

		// set the calling table and editor
		plugin.setEditorField(this);
		plugin.setCallingTable(callingTable);

		if(!newInstance) { // this means that it is a record being edited, so may have to do something special
			plugin.setModel(domainObject, null);
		} else { // its a new record to just set the model
			plugin.setModel(domainObject, null);
		}

		// set the main program application frame and display it
		plugin.setApplicationFrame(ApplicationFrame.getInstance());
		plugin.showPlugin(getParentEditor());

		return true;
	}
	
	protected void setOtherLevelEnabledDisabled(JComboBox levelComboBox, JLabel otherLevelLable, JTextField otherLevelTextField) {
		if (levelComboBox.getSelectedItem() != null) {
			String level = ((LookupListItems) levelComboBox.getSelectedItem()).getListItem();
			if (level.equalsIgnoreCase(Resources.LEVEL_VALUE_OTHERLEVEL)) {
				otherLevelLable.setEnabled(true);
				otherLevelTextField.setEnabled(true);
			} else {
				otherLevelLable.setEnabled(false);
				otherLevelTextField.setEnabled(false);
				otherLevelTextField.setText("");
			}
		} else {
			otherLevelLable.setEnabled(false);
			otherLevelTextField.setEnabled(false);
			otherLevelTextField.setText("");
		}
	}
	
	protected void addDatesToNewDigitalInstance(ArchDescriptionDigitalInstances digitalInstance, ResourcesCommon resorceCommon) {
		ArchDescriptionDates newDate;
		for (ArchDescriptionDates date: resorceCommon.getArchDescriptionDates()) {
			newDate = new ArchDescriptionDates(digitalInstance.getDigitalObject());
			newDate.setIsoBulkDateBegin(date.getIsoBulkDateBegin());
			newDate.setIsoBulkDateEnd(date.getIsoBulkDateBegin());
			newDate.setIsoDateBegin(date.getIsoDateBegin());
			newDate.setIsoDateEnd(date.getIsoDateEnd());
			newDate.setDateExpression(date.getDateExpression());
			newDate.setDateType(date.getDateType());
			newDate.setCalendar(date.getCalendar());
			newDate.setEra(date.getEra());
			newDate.setCertainty(date.getCertainty());
			digitalInstance.getDigitalObject().addArchdescriptionDate(newDate);
		}
	}

}
