package au.gov.nla.atplugin.multiplecomponentidentifiers.panel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Collections;
import javax.swing.*;
import javax.swing.border.*;

import au.gov.nla.atplugin.multiplecomponentidentifiers.mydomain.NLADomainEditorFields;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import com.jgoodies.binding.PresentationModel;
import org.archiviststoolkit.mydomain.*;
import org.archiviststoolkit.swing.*;
import org.archiviststoolkit.model.*;
import org.archiviststoolkit.util.InLineTagsUtils;
import org.archiviststoolkit.util.LookupListUtils;
import org.archiviststoolkit.structure.ATFieldInfo;
import org.archiviststoolkit.dialog.ErrorDialog;
import org.archiviststoolkit.dialog.DigitalObjectLookup;
import org.archiviststoolkit.exceptions.ObjectNotRemovedException;
import org.archiviststoolkit.exceptions.DomainEditorCreationException;
import org.archiviststoolkit.ApplicationFrame;
import org.archiviststoolkit.editor.ArchDescriptionInstancesEditor;
//import edu.byu.plugins.editors.ArchDescriptionDatesFields;
//import edu.byu.plugins.editors.ArchDescPhysicalDescFields;

public class ResourceComponentBasicInfoPanel extends NLADomainEditorFields {

	private ResourcesComponents resourceComponentModel;
	protected ArchDescriptionInstances currentInstance;
	protected ArchDescriptionInstancesEditor dialogInstances;
	protected String defaultInstanceType = "";


	public ResourceComponentBasicInfoPanel(PresentationModel detailsModel) {
		this.detailsModel = detailsModel;
		initComponents();
	}

	public Component getInitialFocusComponent() {
		return resourcesLevel;
	}

	private void resourcesLevelActionPerformed() {
		//setOtherLevelEnabledDisabled(resourcesLevel, label_otherLevel, resourcesOtherLevel); tingram
	}

	private void insertInlineTagActionPerformed() {
		InLineTagsUtils.wrapInTagActionPerformed(insertInlineTag, resourcesTitle, editorField.getParentEditor());
	}

	private void dateTableMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			/* tingram
			try {
				DomainEditor domainEditor = new DomainEditor(ArchDescriptionDates.class, editorField.getParentEditor(), "Dates", new ArchDescriptionDatesFields());
				domainEditor.setCallingTable(dateTable);
				domainEditor.setNavigationButtonListeners(domainEditor);
				editRelatedRecord(dateTable, ArchDescriptionDates.class, true, domainEditor);
			} catch (UnsupportedTableModelException e1) {
				new ErrorDialog("Error creating editor for Dates", e1).showDialog();
			}
			*/
		}
	}
	
	
	
	private void addDateActionPerformed(ActionEvent e) {
		//addDateActionPerformed(dateTable, resourceComponentModel); tingram
	}
	

	private void removeDateActionPerformed(ActionEvent e) {
		try {
			this.removeRelatedTableRow(dateTable, resourceComponentModel);
		} catch (ObjectNotRemovedException e1) {
			new ErrorDialog("Date not removed", e1).showDialog();
		}
	}

	public DomainSortableTable getDateTable() {
		return dateTable;
	}

	private void physicalDescriptionMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			/* tingram
			try {
				DomainEditor domainEditor = new DomainEditor(ArchDescriptionPhysicalDescriptions.class, editorField.getParentEditor(), "Physical Descriptions", new ArchDescPhysicalDescFields());
				domainEditor.setCallingTable(physicalDescriptionsTable);
				domainEditor.setNavigationButtonListeners(domainEditor);
				editRelatedRecord(physicalDescriptionsTable, ArchDescriptionPhysicalDescriptions.class, true, domainEditor);
			} catch (UnsupportedTableModelException e1) {
				new ErrorDialog("Error creating editor for Dates", e1).showDialog();
			}
			*/
		}
	}
	
	
	private void addPhysicalDescriptionActionPerformed() {
		//addPhysicalDescriptionActionPerformed(physicalDescriptionsTable, resourceComponentModel); tingram
	}
	

	private void removePhysicalDescriptionActionPerformed() {
		try {
			this.removeRelatedTableRow(physicalDescriptionsTable, resourceComponentModel);
		} catch (ObjectNotRemovedException e1) {
			new ErrorDialog("Physical description not removed", e1).showDialog();
		}
	}

	private void instancesTableMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
            // get the current instance record to edit
            DomainObject instanceRecord = null;
            int selectedRow = getInstancesTable().getSelectedRow();
		    if (selectedRow != -1) {
			    instanceRecord = getInstancesTable().getSortedList().get(selectedRow);
            }

            if(usePluginDomainEditor(false, instanceRecord, getInstancesTable())) {
                return;
            }

			if (handleTableMouseClick(e, getInstancesTable(), ArchDescriptionInstances.class) == JOptionPane.OK_OPTION) {
				findLocationForInstance(currentInstance);
			}
		}
	}

	public void findLocationForInstance(ArchDescriptionInstances newInstance) {
		if (newInstance instanceof ArchDescriptionAnalogInstances) {
			final Resources parentResource = (Resources) editorField.getModel();
			final ArchDescriptionAnalogInstances analogInstance = (ArchDescriptionAnalogInstances) newInstance;
			Thread performer = new Thread(new Runnable() {
				public void run() {
					InfiniteProgressPanel monitor = ATProgressUtil.createModalProgressMonitor(ApplicationFrame.getInstance(), 1000);
					monitor.start("Gathering Containers...");
					try {
						analogInstance.setLocation(parentResource.findLocationForContainer(analogInstance.getTopLevelLabel(), monitor));
					} finally {
						monitor.close();
					}
				}
			}, "Performer");
			performer.start();
		}
	}

	private void addInstanceButtonActionPerformed() {
//		ArchDescription archDescriptionModel = (ArchDescription) super.getModel();
		ArchDescriptionInstances newInstance = null;
		Vector<String> possibilities = LookupListUtils.getLookupListValues(LookupListUtils.LIST_NAME_INSTANCE_TYPES);
		ImageIcon icon = null;
		try {
			// add a special entry for digital object link to the possibilities vector
            possibilities.add(ArchDescriptionInstances.DIGITAL_OBJECT_INSTANCE_LINK);
            Collections.sort(possibilities);

            dialogInstances = (ArchDescriptionInstancesEditor) DomainEditorFactory.getInstance().createDomainEditorWithParent(ArchDescriptionInstances.class, getParentEditor(), getInstancesTable());
		} catch (DomainEditorCreationException e) {
			new ErrorDialog(getParentEditor(), "Error creating editor for ArchDescriptionInstances", e).showDialog();
		}

        dialogInstances.setNewRecord(true);

		int returnStatus;
		Boolean done = false;
		while (!done) {
			defaultInstanceType = (String) JOptionPane.showInputDialog(getParentEditor(), "What type of instance would you like to create",
					"", JOptionPane.PLAIN_MESSAGE, icon, possibilities.toArray(), defaultInstanceType);
			System.out.println("adding new instance");
			if ((defaultInstanceType != null) && (defaultInstanceType.length() > 0)) {
				if (defaultInstanceType.equalsIgnoreCase(ArchDescriptionInstances.DIGITAL_OBJECT_INSTANCE)) {
					System.out.println("adding new digital instance");
					newInstance = new ArchDescriptionDigitalInstances(resourceComponentModel);
					//addDatesToNewDigitalInstance((ArchDescriptionDigitalInstances)newInstance, resourceComponentModel); tingram
				} else if (defaultInstanceType.equalsIgnoreCase(ArchDescriptionInstances.DIGITAL_OBJECT_INSTANCE_LINK)) {
                    // add a digital object link or links instead
                    addDigitalInstanceLink((Resources) editorField.getModel());
                    return;
				} else {
					newInstance = new ArchDescriptionAnalogInstances(resourceComponentModel);
				}

                newInstance.setInstanceType(defaultInstanceType);

                // see whether to use a plugin
                if(usePluginDomainEditor(true, newInstance, getInstancesTable())) {
                    return;
                }

				dialogInstances.setModel(newInstance, null);
//				dialogInstances.setResourceInfo((Resources) editorField.getModel());
				returnStatus = dialogInstances.showDialog();
				if (returnStatus == JOptionPane.OK_OPTION) {
					dialogInstances.commitChangesToCurrentRecord();
					resourceComponentModel.addInstance(newInstance);
					getInstancesTable().getEventList().add(newInstance);
					findLocationForInstance(newInstance);
                    ApplicationFrame.getInstance().setRecordDirty(); // set the record dirty
					done = true;
				} else if (returnStatus == StandardEditor.OK_AND_ANOTHER_OPTION) {
					dialogInstances.commitChangesToCurrentRecord();
					resourceComponentModel.addInstance(newInstance);
					getInstancesTable().getEventList().add(newInstance);
					findLocationForInstance(newInstance);
                    ApplicationFrame.getInstance().setRecordDirty(); // set the record dirty
				} else {
					done = true;
				}
			} else {
				done = true;
			}
		}
		dialogInstances.setNewRecord(false);
	}

	/**
	 * Method to open dialog that allows linking of digital objects to this resource
	 * or resource component
	 * @param parentResource The parent resource component
	 */
	private void addDigitalInstanceLink(Resources parentResource) {
		DigitalObjectLookup digitalObjectPicker = new DigitalObjectLookup(getParentEditor(), this);
		digitalObjectPicker.setParentResource(parentResource);
		digitalObjectPicker.showDialog(this);
	}

	private void removeInstanceButtonActionPerformed() {
		try {
			this.removeRelatedTableRow(getInstancesTable(), resourceComponentModel);
		} catch (ObjectNotRemovedException e) {
			new ErrorDialog("Instance not removed", e).showDialog();
		}
	}

	public DomainSortableTable getPhysicalDescriptionsTable() {
		return physicalDescriptionsTable;
	}

	public DomainSortableTable getInstancesTable() {
		return instancesTable;
	}

	public JButton getAddInstanceButton() {
		return addInstanceButton;
	}

	public JButton getRemoveInstanceButton() {
		return removeInstanceButton;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		panel7 = new JPanel();
		panel3 = new JPanel();
		label_resourcesLevel = new JLabel();
		resourcesLevel = ATBasicComponentFactory.createComboBox(detailsModel, ResourcesComponents.PROPERTYNAME_LEVEL, ResourcesComponents.class);
		panel12 = new JPanel();
		label3 = new JLabel();
		resourcesDateBegin2 = ATBasicComponentFactory.createTextField(detailsModel.getModel(ResourcesComponents.PROPERTYNAME_PERSISTENT_ID));
		label_otherLevel = new JLabel();
		resourcesOtherLevel = ATBasicComponentFactory.createTextField(detailsModel.getModel(ResourcesComponents.PROPERTYNAME_OTHER_LEVEL),false);
		label_resourcesTitle = new JLabel();
		scrollPane42 = new JScrollPane();
		resourcesTitle = ATBasicComponentFactory.createTextArea(detailsModel.getModel(ArchDescription.PROPERTYNAME_TITLE),false);
		tagApplicatorPanel = new JPanel();
		insertInlineTag = ATBasicComponentFactory.createUnboundComboBox(InLineTagsUtils.getInLineTagList(InLineTagsUtils.TITLE));
		label_repositoryName4 = new JLabel();
		scrollPane8 = new JScrollPane();
		dateTable = new DomainSortableTable(ArchDescriptionDates.class);
		panel22 = new JPanel();
		addDate = new JButton();
		removeDate = new JButton();
		panel9 = new JPanel();
		label_resourcesLanguageCode2 = new JLabel();
		resourcesLanguageCode = ATBasicComponentFactory.createComboBox(detailsModel, ResourcesComponents.PROPERTYNAME_LANGUAGE_CODE, ResourcesComponents.class);
		panel23 = new JPanel();
		label_resourcesLanguageNote2 = new JLabel();
		scrollPane423 = new JScrollPane();
		resourcesLanguageNote = ATBasicComponentFactory.createTextArea(detailsModel.getModel(ResourcesComponents.PROPERTYNAME_REPOSITORY_PROCESSING_NOTE),false);
		separator2 = new JSeparator();
		panel10 = new JPanel();
		panel1 = new JPanel();
		label_resourcesLevel2 = new JLabel();
		subdivisionIdentifier = ATBasicComponentFactory.createTextField(detailsModel.getModel(ResourcesComponents.PROPERTYNAME_UNIQUE_IDENTIFIER),false);
		label_repositoryName5 = new JLabel();
		scrollPane9 = new JScrollPane();
		physicalDescriptionsTable = new DomainSortableTable(ArchDescriptionPhysicalDescriptions.class);
		panel24 = new JPanel();
		addPhysicalDescription = new JButton();
		removePhysicalDescription = new JButton();
		panel2 = new JPanel();
		panel6 = new JPanel();
		label1 = new JLabel();
		scrollPane4 = new JScrollPane();
		instancesTable = new DomainSortableTable(ArchDescriptionInstances.class, ArchDescriptionInstances.PROPERTYNAME_INSTANCE_TYPE);
		panel13 = new JPanel();
		addInstanceButton = new JButton();
		removeInstanceButton = new JButton();
		panel4 = new JPanel();
		restrictionsApply2 = ATBasicComponentFactory.createCheckBox(detailsModel, ResourcesComponents.PROPERTYNAME_INTERNAL_ONLY, ResourcesComponents.class);
		resourcesRestrictionsApply = ATBasicComponentFactory.createCheckBox(detailsModel, ArchDescription.PROPERTYNAME_RESTRICTIONS_APPLY, ResourcesComponents.class);
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBackground(new Color(200, 205, 232));
		setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC
			},
			RowSpec.decodeSpecs("default")));

		//======== panel7 ========
		{
			panel7.setOpaque(false);
			panel7.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel7.setBorder(Borders.DLU2_BORDER);
			panel7.setLayout(new FormLayout(
				ColumnSpec.decodeSpecs("default:grow"),
				new RowSpec[] {
					new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
				}));

			//======== panel3 ========
			{
				panel3.setOpaque(false);
				panel3.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel3.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.MIN_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.RIGHT, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
					},
					new RowSpec[] {
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC
					}));

				//---- label_resourcesLevel ----
				label_resourcesLevel.setText("Level");
				label_resourcesLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourcesLevel, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_LEVEL);
				panel3.add(label_resourcesLevel, cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

				//---- resourcesLevel ----
				resourcesLevel.setOpaque(false);
				resourcesLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				resourcesLevel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						resourcesLevelActionPerformed();
					}
				});
				panel3.add(resourcesLevel, cc.xywh(3, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));

				//======== panel12 ========
				{
					panel12.setOpaque(false);
					panel12.setLayout(new FormLayout(
						new ColumnSpec[] {
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC
						},
						RowSpec.decodeSpecs("default")));

					//---- label3 ----
					label3.setText("Persistent ID");
					label3.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label3, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_PERSISTENT_ID);
					panel12.add(label3, cc.xy(1, 1));

					//---- resourcesDateBegin2 ----
					resourcesDateBegin2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					resourcesDateBegin2.setEditable(false);
					resourcesDateBegin2.setOpaque(false);
					panel12.add(resourcesDateBegin2, cc.xy(3, 1));
				}
				panel3.add(panel12, cc.xy(5, 1));

				//---- label_otherLevel ----
				label_otherLevel.setText("Other Level");
				label_otherLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_otherLevel, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_OTHER_LEVEL);
				panel3.add(label_otherLevel, cc.xywh(1, 3, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

				//---- resourcesOtherLevel ----
				resourcesOtherLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel3.add(resourcesOtherLevel, cc.xywh(3, 3, 3, 1));

				//---- label_resourcesTitle ----
				label_resourcesTitle.setText("Title");
				label_resourcesTitle.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourcesTitle, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_TITLE);
				panel3.add(label_resourcesTitle, cc.xywh(1, 5, 5, 1));

				//======== scrollPane42 ========
				{
					scrollPane42.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
					scrollPane42.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));

					//---- resourcesTitle ----
					resourcesTitle.setRows(4);
					resourcesTitle.setLineWrap(true);
					resourcesTitle.setWrapStyleWord(true);
					resourcesTitle.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					scrollPane42.setViewportView(resourcesTitle);
				}
				panel3.add(scrollPane42, cc.xywh(1, 7, 5, 1));

				//======== tagApplicatorPanel ========
				{
					tagApplicatorPanel.setOpaque(false);
					tagApplicatorPanel.setLayout(new FormLayout(
						new ColumnSpec[] {
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC
						},
						RowSpec.decodeSpecs("default")));

					//---- insertInlineTag ----
					insertInlineTag.setOpaque(false);
					insertInlineTag.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					insertInlineTag.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							insertInlineTagActionPerformed();
						}
					});
					tagApplicatorPanel.add(insertInlineTag, cc.xy(1, 1));
				}
				panel3.add(tagApplicatorPanel, cc.xywh(1, 9, 5, 1));
			}
			panel7.add(panel3, cc.xywh(1, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//---- label_repositoryName4 ----
			label_repositoryName4.setText("Dates");
			label_repositoryName4.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel7.add(label_repositoryName4, cc.xy(1, 3));

			//======== scrollPane8 ========
			{
				scrollPane8.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane8.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				scrollPane8.setPreferredSize(new Dimension(200, 104));

				//---- dateTable ----
				dateTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
				dateTable.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						dateTableMouseClicked(e);
					}
				});
				scrollPane8.setViewportView(dateTable);
			}
			panel7.add(scrollPane8, cc.xywh(1, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//======== panel22 ========
			{
				panel22.setBackground(new Color(231, 188, 251));
				panel22.setOpaque(false);
				panel22.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel22.setMinimumSize(new Dimension(100, 29));
				panel22.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC
					},
					RowSpec.decodeSpecs("default")));

				//---- addDate ----
				addDate.setText("Add Date");
				addDate.setOpaque(false);
				addDate.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				addDate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addDateActionPerformed(e);
					}
				});
				panel22.add(addDate, cc.xy(1, 1));

				//---- removeDate ----
				removeDate.setText("Remove Date");
				removeDate.setOpaque(false);
				removeDate.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				removeDate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeDateActionPerformed(e);
					}
				});
				panel22.add(removeDate, cc.xy(3, 1));
			}
			panel7.add(panel22, cc.xywh(1, 7, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));

			//======== panel9 ========
			{
				panel9.setOpaque(false);
				panel9.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec("left:min(default;200px)")
					},
					RowSpec.decodeSpecs("default")));

				//---- label_resourcesLanguageCode2 ----
				label_resourcesLanguageCode2.setText("Lanaguage");
				label_resourcesLanguageCode2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourcesLanguageCode2, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_LANGUAGE_CODE);
				panel9.add(label_resourcesLanguageCode2, cc.xy(1, 1));

				//---- resourcesLanguageCode ----
				resourcesLanguageCode.setMaximumSize(new Dimension(150, 32767));
				resourcesLanguageCode.setOpaque(false);
				resourcesLanguageCode.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel9.add(resourcesLanguageCode, cc.xywh(3, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
			}
			panel7.add(panel9, cc.xy(1, 9));

			//======== panel23 ========
			{
				panel23.setOpaque(false);
				panel23.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel23.setLayout(new FormLayout(
					ColumnSpec.decodeSpecs("default:grow"),
					new RowSpec[] {
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
					}));

				//---- label_resourcesLanguageNote2 ----
				label_resourcesLanguageNote2.setText("Repository Processing Note");
				label_resourcesLanguageNote2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourcesLanguageNote2, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_REPOSITORY_PROCESSING_NOTE);
				panel23.add(label_resourcesLanguageNote2, new CellConstraints(1, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets( 0, 10, 0, 0)));

				//======== scrollPane423 ========
				{
					scrollPane423.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
					scrollPane423.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));

					//---- resourcesLanguageNote ----
					resourcesLanguageNote.setRows(4);
					resourcesLanguageNote.setWrapStyleWord(true);
					resourcesLanguageNote.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					scrollPane423.setViewportView(resourcesLanguageNote);
				}
				panel23.add(scrollPane423, new CellConstraints(1, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets( 0, 10, 0, 0)));
			}
			panel7.add(panel23, cc.xy(1, 11));
		}
		add(panel7, cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

		//---- separator2 ----
		separator2.setForeground(new Color(147, 131, 86));
		separator2.setOrientation(SwingConstants.VERTICAL);
		add(separator2, cc.xywh(3, 1, 1, 1, CellConstraints.FILL, CellConstraints.FILL));

		//======== panel10 ========
		{
			panel10.setOpaque(false);
			panel10.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel10.setBorder(Borders.DLU2_BORDER);
			panel10.setLayout(new FormLayout(
				ColumnSpec.decodeSpecs("default:grow"),
				new RowSpec[] {
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.NO_GROW),
					FormFactory.LINE_GAP_ROWSPEC,
					new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC
				}));

			//======== panel1 ========
			{
				panel1.setOpaque(false);
				panel1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel1.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
					},
					RowSpec.decodeSpecs("default")));

				//---- label_resourcesLevel2 ----
				label_resourcesLevel2.setText("Component Unique Identifier");
				label_resourcesLevel2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourcesLevel2, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_UNIQUE_IDENTIFIER);
				panel1.add(label_resourcesLevel2, cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

				//---- subdivisionIdentifier ----
				subdivisionIdentifier.setColumns(5);
				subdivisionIdentifier.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel1.add(subdivisionIdentifier, cc.xywh(3, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
			}
			panel10.add(panel1, cc.xy(1, 1));

			//---- label_repositoryName5 ----
			label_repositoryName5.setText("Physical Description");
			label_repositoryName5.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel10.add(label_repositoryName5, cc.xy(1, 3));

			//======== scrollPane9 ========
			{
				scrollPane9.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane9.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				scrollPane9.setPreferredSize(new Dimension(200, 104));

				//---- physicalDescriptionsTable ----
				physicalDescriptionsTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
				physicalDescriptionsTable.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						physicalDescriptionMouseClicked(e);
					}
				});
				scrollPane9.setViewportView(physicalDescriptionsTable);
			}
			panel10.add(scrollPane9, cc.xywh(1, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//======== panel24 ========
			{
				panel24.setBackground(new Color(231, 188, 251));
				panel24.setOpaque(false);
				panel24.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel24.setMinimumSize(new Dimension(100, 29));
				panel24.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC
					},
					RowSpec.decodeSpecs("default")));

				//---- addPhysicalDescription ----
				addPhysicalDescription.setText("Add Description");
				addPhysicalDescription.setOpaque(false);
				addPhysicalDescription.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				addPhysicalDescription.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addPhysicalDescriptionActionPerformed();
					}
				});
				panel24.add(addPhysicalDescription, cc.xy(1, 1));

				//---- removePhysicalDescription ----
				removePhysicalDescription.setText("Remove Description");
				removePhysicalDescription.setOpaque(false);
				removePhysicalDescription.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				removePhysicalDescription.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removePhysicalDescriptionActionPerformed();
					}
				});
				panel24.add(removePhysicalDescription, cc.xy(3, 1));
			}
			panel10.add(panel24, cc.xywh(1, 7, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));

			//======== panel2 ========
			{
				panel2.setBackground(new Color(182, 187, 212));
				panel2.setBorder(new BevelBorder(BevelBorder.LOWERED));
				panel2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel2.setLayout(new FormLayout(
					"default:grow",
					"fill:default:grow"));

				//======== panel6 ========
				{
					panel6.setOpaque(false);
					panel6.setBorder(Borders.DLU2_BORDER);
					panel6.setLayout(new FormLayout(
						new ColumnSpec[] {
							FormFactory.RELATED_GAP_COLSPEC,
							new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
						},
						new RowSpec[] {
							FormFactory.DEFAULT_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
							FormFactory.LINE_GAP_ROWSPEC,
							FormFactory.DEFAULT_ROWSPEC
						}));

					//---- label1 ----
					label1.setText("Instances");
					label1.setForeground(new Color(0, 0, 102));
					label1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label1, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_INSTANCES);
					panel6.add(label1, cc.xywh(1, 1, 2, 1));

					//======== scrollPane4 ========
					{
						scrollPane4.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
						scrollPane4.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));

						//---- instancesTable ----
						instancesTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
						instancesTable.setRowHeight(20);
						instancesTable.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								instancesTableMouseClicked(e);
							}
						});
						scrollPane4.setViewportView(instancesTable);
					}
					panel6.add(scrollPane4, cc.xy(2, 3));

					//======== panel13 ========
					{
						panel13.setBackground(new Color(231, 188, 251));
						panel13.setOpaque(false);
						panel13.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
						panel13.setLayout(new FormLayout(
							new ColumnSpec[] {
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC
							},
							RowSpec.decodeSpecs("default")));

						//---- addInstanceButton ----
						addInstanceButton.setBackground(new Color(231, 188, 251));
						addInstanceButton.setText("Add Instance");
						addInstanceButton.setOpaque(false);
						addInstanceButton.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
						addInstanceButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								addInstanceButtonActionPerformed();
							}
						});
						panel13.add(addInstanceButton, cc.xy(1, 1));

						//---- removeInstanceButton ----
						removeInstanceButton.setBackground(new Color(231, 188, 251));
						removeInstanceButton.setText("Remove Instance");
						removeInstanceButton.setOpaque(false);
						removeInstanceButton.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
						removeInstanceButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								removeInstanceButtonActionPerformed();
							}
						});
						panel13.add(removeInstanceButton, cc.xy(3, 1));
					}
					panel6.add(panel13, cc.xywh(1, 5, 2, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
				}
				panel2.add(panel6, cc.xy(1, 1));
			}
			panel10.add(panel2, cc.xy(1, 9));

			//======== panel4 ========
			{
				panel4.setOpaque(false);
				panel4.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC
					},
					RowSpec.decodeSpecs("default")));

				//---- restrictionsApply2 ----
				restrictionsApply2.setBackground(new Color(231, 188, 251));
				restrictionsApply2.setText("Internal Only");
				restrictionsApply2.setOpaque(false);
				restrictionsApply2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				restrictionsApply2.setText(ATFieldInfo.getLabel(ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_INTERNAL_ONLY));
				panel4.add(restrictionsApply2, cc.xy(1, 1));

				//---- resourcesRestrictionsApply ----
				resourcesRestrictionsApply.setBackground(new Color(231, 188, 251));
				resourcesRestrictionsApply.setText("Restrictions Apply");
				resourcesRestrictionsApply.setOpaque(false);
				resourcesRestrictionsApply.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				resourcesRestrictionsApply.setText(ATFieldInfo.getLabel(ResourcesComponents.class, ArchDescription.PROPERTYNAME_RESTRICTIONS_APPLY));
				panel4.add(resourcesRestrictionsApply, cc.xy(3, 1));
			}
			panel10.add(panel4, cc.xy(1, 11));
		}
		add(panel10, cc.xywh(5, 1, 1, 1, CellConstraints.FILL, CellConstraints.FILL));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel panel7;
	private JPanel panel3;
	private JLabel label_resourcesLevel;
	public JComboBox resourcesLevel;
	private JPanel panel12;
	private JLabel label3;
	public JTextField resourcesDateBegin2;
	private JLabel label_otherLevel;
	public JTextField resourcesOtherLevel;
	private JLabel label_resourcesTitle;
	private JScrollPane scrollPane42;
	public JTextArea resourcesTitle;
	private JPanel tagApplicatorPanel;
	public JComboBox insertInlineTag;
	private JLabel label_repositoryName4;
	private JScrollPane scrollPane8;
	private DomainSortableTable dateTable;
	private JPanel panel22;
	private JButton addDate;
	private JButton removeDate;
	private JPanel panel9;
	private JLabel label_resourcesLanguageCode2;
	public JComboBox resourcesLanguageCode;
	private JPanel panel23;
	private JLabel label_resourcesLanguageNote2;
	private JScrollPane scrollPane423;
	public JTextArea resourcesLanguageNote;
	private JSeparator separator2;
	private JPanel panel10;
	private JPanel panel1;
	private JLabel label_resourcesLevel2;
	public JTextField subdivisionIdentifier;
	private JLabel label_repositoryName5;
	private JScrollPane scrollPane9;
	private DomainSortableTable physicalDescriptionsTable;
	private JPanel panel24;
	private JButton addPhysicalDescription;
	private JButton removePhysicalDescription;
	private JPanel panel2;
	private JPanel panel6;
	private JLabel label1;
	private JScrollPane scrollPane4;
	private DomainSortableTable instancesTable;
	private JPanel panel13;
	private JButton addInstanceButton;
	private JButton removeInstanceButton;
	private JPanel panel4;
	public JCheckBox restrictionsApply2;
	public JCheckBox resourcesRestrictionsApply;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public void setModel(ResourcesComponents resourcesModel) {
		this.resourceComponentModel = resourcesModel;
		dateTable.updateCollection(this.resourceComponentModel.getArchDescriptionDates());
		physicalDescriptionsTable.updateCollection(this.resourceComponentModel.getPhysicalDesctiptions());
		instancesTable.updateCollection(this.resourceComponentModel.getInstances());
		
	}

}