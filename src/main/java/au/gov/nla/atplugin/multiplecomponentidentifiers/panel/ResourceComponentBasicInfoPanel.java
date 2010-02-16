package au.gov.nla.atplugin.multiplecomponentidentifiers.panel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Collections;
import javax.swing.*;
import javax.swing.border.*;

import au.gov.nla.atplugin.multiplecomponentidentifiers.editor.ArchDescComponentIdentifiersFields;
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

public class ResourceComponentBasicInfoPanel extends NLADomainEditorFields {

	private ResourcesComponents resourceComponentModel;
	protected ArchDescriptionInstances currentInstance;
	protected ArchDescriptionInstancesEditor dialogInstances;
	protected String defaultInstanceType = "";


	public ResourceComponentBasicInfoPanel(PresentationModel detailsModel) {
		this.detailsModel = detailsModel;
		initComponents();
		
		identifiersTable.setClazzAndColumns(ArchDescComponentIdentifiers.PROPERTYNAME_IDENTIFIER_TYPE, 
				ArchDescComponentIdentifiers.class,
				ArchDescComponentIdentifiers.PROPERTYNAME_IDENTIFIER_TYPE,
				ArchDescComponentIdentifiers.PROPERTYNAME_IDENTIFIER_LABEL,
				ArchDescComponentIdentifiers.PROPERTYNAME_COMPONENT_IDENTIFIER);
	}

	public Component getInitialFocusComponent() {
		return resourcesLevel;
	}

	private void resourcesLevelActionPerformed() {
		setOtherLevelEnabledDisabled(resourcesLevel, label_otherLevel, resourcesOtherLevel);
	}

	private void insertInlineTagActionPerformed() {
		InLineTagsUtils.wrapInTagActionPerformed(insertInlineTag, resourcesTitle, editorField.getParentEditor());
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
			
			ResourcesComponents components = (ResourcesComponents)editorField.getModel();
			//final Resources parentResource = (Resources) editorField.getModel();
			final Resources parentResource = components.getResource();
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
		ArchDescriptionInstances newInstance = null;
		Vector<String> possibilities = LookupListUtils.getLookupListValues(LookupListUtils.LIST_NAME_INSTANCE_TYPES);
		ImageIcon icon = null;
		try {
			// add a special entry for digital object link to the possibilities vector
            possibilities.add(ArchDescriptionInstances.DIGITAL_OBJECT_INSTANCE_LINK);
            Collections.sort(possibilities);
            dialogInstances = (ArchDescriptionInstancesEditor) DomainEditorFactory.getInstance().createDomainEditorWithParent(ArchDescriptionInstances.class, editorField.getParentEditor(), getInstancesTable());
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
					addDatesToNewDigitalInstance((ArchDescriptionDigitalInstances)newInstance, resourceComponentModel);
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

	public DomainSortableTable getInstancesTable() {
		return instancesTable;
	}

	public JButton getAddInstanceButton() {
		return addInstanceButton;
	}

	public JButton getRemoveInstanceButton() {
		return removeInstanceButton;
	}

	private void identifiersMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {		
			try {
				DomainEditor domainEditor = new DomainEditor(ArchDescComponentIdentifiers.class, editorField.getParentEditor(), "Multiple Component Identifiers", new ArchDescComponentIdentifiersFields());
				domainEditor.setCallingTable(identifiersTable);
				domainEditor.setNavigationButtonListeners(domainEditor);
				editRelatedRecord(identifiersTable, ArchDescComponentIdentifiers.class, true, domainEditor);
			} catch (UnsupportedTableModelException e1) {
				new ErrorDialog("Error creating editor for Identifiers", e1).showDialog();
			}			
		}
	}

	private void addIdentifierActionPerformed() {
		addIdentifierActionPerformed(identifiersTable, resourceComponentModel);
	}

	private void removeIdentifierActionPerformed() {
		try {
			this.removeRelatedTableRow(identifiersTable, resourceComponentModel);
		} catch (ObjectNotRemovedException e1) {
			new ErrorDialog("Identifier not removed", e1).showDialog();
		}
	}

	public DomainSortableTable getIdentifiersTable() {
		return identifiersTable;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		panel7 = new JPanel();
		label_resourcesTitle = new JLabel();
		scrollPane42 = new JScrollPane();
		resourcesTitle = ATBasicComponentFactory.createTextArea(detailsModel.getModel(ArchDescription.PROPERTYNAME_TITLE),false);
		tagApplicatorPanel = new JPanel();
		insertInlineTag = ATBasicComponentFactory.createUnboundComboBox(InLineTagsUtils.getInLineTagList(InLineTagsUtils.TITLE));
		panel38 = new JPanel();
		panel35 = new JPanel();
		label_resourcesDateExpression = new JLabel();
		resourcesDateExpression = ATBasicComponentFactory.createTextField(detailsModel.getModel(ArchDescription.PROPERTYNAME_DATE_EXPRESSION),false);
		Date1Label1 = new JLabel();
		label_resourcesDateBegin = new JLabel();
		resourcesDateBegin = ATBasicComponentFactory.createIntegerField(detailsModel,ArchDescription.PROPERTYNAME_DATE_BEGIN);
		label_resourcesDateEnd = new JLabel();
		resourcesDateEnd = ATBasicComponentFactory.createIntegerField(detailsModel,ArchDescription.PROPERTYNAME_DATE_END);
		BulkDatesLabel = new JLabel();
		label_resourcesBulkDateBegin = new JLabel();
		resourcesBulkDateBegin = ATBasicComponentFactory.createIntegerField(detailsModel,Resources.PROPERTYNAME_BULK_DATE_BEGIN);
		label_resourcesBulkDateEnd = new JLabel();
		resourcesBulkDateEnd = ATBasicComponentFactory.createIntegerField(detailsModel,Resources.PROPERTYNAME_BULK_DATE_END);
		panel9 = new JPanel();
		label_resourcesLanguageCode2 = new JLabel();
		resourcesLanguageCode = ATBasicComponentFactory.createComboBox(detailsModel, ResourcesComponents.PROPERTYNAME_LANGUAGE_CODE, ResourcesComponents.class);
		panel5 = new JPanel();
		panel20 = new JPanel();
		ExtentLabel = new JLabel();
		panel21 = new JPanel();
		label_resourcesExtentNumber = new JLabel();
		resourcesExtentNumber = ATBasicComponentFactory.createDoubleField(detailsModel,Resources.PROPERTYNAME_EXTENT_NUMBER);
		extentType = ATBasicComponentFactory.createComboBox(detailsModel, Resources.PROPERTYNAME_EXTENT_TYPE, Resources.class);
		label_resourcesExtentDescription = new JLabel();
		scrollPane422 = new JScrollPane();
		containerSummary = ATBasicComponentFactory.createTextArea(detailsModel.getModel(Resources.PROPERTYNAME_CONTAINER_SUMMARY),false);
		panel23 = new JPanel();
		label_resourcesLanguageNote2 = new JLabel();
		scrollPane423 = new JScrollPane();
		resourcesLanguageNote = ATBasicComponentFactory.createTextArea(detailsModel.getModel(ResourcesComponents.PROPERTYNAME_REPOSITORY_PROCESSING_NOTE),false);
		separator2 = new JSeparator();
		panel10 = new JPanel();
		panel3 = new JPanel();
		label_resourcesLevel = new JLabel();
		resourcesLevel = ATBasicComponentFactory.createComboBox(detailsModel, ResourcesComponents.PROPERTYNAME_LEVEL, ResourcesComponents.class);
		panel12 = new JPanel();
		label3 = new JLabel();
		resourcesDateBegin2 = ATBasicComponentFactory.createTextField(detailsModel.getModel(ResourcesComponents.PROPERTYNAME_PERSISTENT_ID));
		label_otherLevel = new JLabel();
		resourcesOtherLevel = ATBasicComponentFactory.createTextField(detailsModel.getModel(ResourcesComponents.PROPERTYNAME_OTHER_LEVEL),false);
		label_repositoryName5 = new JLabel();
		scrollPane9 = new JScrollPane();
		identifiersTable = new DomainSortableTable();
		panel25 = new JPanel();
		addIdentifier = new JButton();
		removeIdentifier = new JButton();
		label1 = new JLabel();
		scrollPane4 = new JScrollPane();
		instancesTable = new DomainSortableTable(ArchDescriptionInstances.class, ArchDescriptionInstances.PROPERTYNAME_INSTANCE_TYPE);
		panel24 = new JPanel();
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
				new ColumnSpec[] {
					new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC
				},
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
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
				}));

			//---- label_resourcesTitle ----
			label_resourcesTitle.setText("Title");
			label_resourcesTitle.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			ATFieldInfo.assignLabelInfo(label_resourcesTitle, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_TITLE);
			panel7.add(label_resourcesTitle, cc.xy(1, 1));

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
			panel7.add(scrollPane42, cc.xywh(1, 3, 2, 1));

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
			panel7.add(tagApplicatorPanel, cc.xywh(1, 5, 2, 1));

			//======== panel38 ========
			{
				panel38.setBorder(new BevelBorder(BevelBorder.LOWERED));
				panel38.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel38.setBackground(new Color(182, 187, 212));
				panel38.setLayout(new FormLayout(
					"60px:grow",
					"fill:default:grow"));

				//======== panel35 ========
				{
					panel35.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.setOpaque(false);
					panel35.setBorder(Borders.DLU2_BORDER);
					panel35.setLayout(new FormLayout(
						new ColumnSpec[] {
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC
						},
						new RowSpec[] {
							FormFactory.DEFAULT_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							FormFactory.DEFAULT_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							FormFactory.DEFAULT_ROWSPEC
						}));

					//---- label_resourcesDateExpression ----
					label_resourcesDateExpression.setText("Date Expression");
					label_resourcesDateExpression.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label_resourcesDateExpression, Resources.class, Resources.PROPERTYNAME_DATE_EXPRESSION);
					panel35.add(label_resourcesDateExpression, cc.xywh(2, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));

					//---- resourcesDateExpression ----
					resourcesDateExpression.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.add(resourcesDateExpression, new CellConstraints(4, 1, 5, 1, CellConstraints.DEFAULT, CellConstraints.TOP, new Insets( 0, 0, 0, 5)));

					//---- Date1Label1 ----
					Date1Label1.setText("Inclusive Dates");
					Date1Label1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.add(Date1Label1, new CellConstraints(2, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets( 0, 5, 0, 0)));

					//---- label_resourcesDateBegin ----
					label_resourcesDateBegin.setText("Begin");
					label_resourcesDateBegin.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label_resourcesDateBegin, Resources.class, Resources.PROPERTYNAME_DATE_BEGIN);
					panel35.add(label_resourcesDateBegin, cc.xy(4, 3));

					//---- resourcesDateBegin ----
					resourcesDateBegin.setColumns(4);
					resourcesDateBegin.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.add(resourcesDateBegin, cc.xywh(6, 3, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

					//---- label_resourcesDateEnd ----
					label_resourcesDateEnd.setText("End");
					label_resourcesDateEnd.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label_resourcesDateEnd, Resources.class, Resources.PROPERTYNAME_DATE_END);
					panel35.add(label_resourcesDateEnd, cc.xy(8, 3));

					//---- resourcesDateEnd ----
					resourcesDateEnd.setColumns(4);
					resourcesDateEnd.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.add(resourcesDateEnd, new CellConstraints(10, 3, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets( 0, 0, 0, 5)));

					//---- BulkDatesLabel ----
					BulkDatesLabel.setText("Bulk Dates");
					BulkDatesLabel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.add(BulkDatesLabel, new CellConstraints(2, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets( 0, 5, 0, 0)));

					//---- label_resourcesBulkDateBegin ----
					label_resourcesBulkDateBegin.setText("Begin");
					label_resourcesBulkDateBegin.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label_resourcesBulkDateBegin, Resources.class, Resources.PROPERTYNAME_BULK_DATE_BEGIN);
					panel35.add(label_resourcesBulkDateBegin, cc.xy(4, 5));

					//---- resourcesBulkDateBegin ----
					resourcesBulkDateBegin.setColumns(4);
					resourcesBulkDateBegin.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.add(resourcesBulkDateBegin, cc.xywh(6, 5, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

					//---- label_resourcesBulkDateEnd ----
					label_resourcesBulkDateEnd.setText("End");
					label_resourcesBulkDateEnd.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label_resourcesBulkDateEnd, Resources.class, Resources.PROPERTYNAME_BULK_DATE_END);
					panel35.add(label_resourcesBulkDateEnd, cc.xy(8, 5));

					//---- resourcesBulkDateEnd ----
					resourcesBulkDateEnd.setColumns(4);
					resourcesBulkDateEnd.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel35.add(resourcesBulkDateEnd, new CellConstraints(10, 5, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets( 0, 0, 0, 5)));
				}
				panel38.add(panel35, cc.xy(1, 1));
			}
			panel7.add(panel38, cc.xywh(1, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

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

			//======== panel5 ========
			{
				panel5.setBackground(new Color(182, 187, 212));
				panel5.setBorder(new BevelBorder(BevelBorder.LOWERED));
				panel5.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel5.setLayout(new FormLayout(
					"default:grow",
					"fill:default:grow"));

				//======== panel20 ========
				{
					panel20.setOpaque(false);
					panel20.setBorder(Borders.DLU2_BORDER);
					panel20.setLayout(new FormLayout(
						new ColumnSpec[] {
							FormFactory.UNRELATED_GAP_COLSPEC,
							new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
						},
						new RowSpec[] {
							FormFactory.DEFAULT_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							FormFactory.DEFAULT_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							FormFactory.DEFAULT_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
						}));

					//---- ExtentLabel ----
					ExtentLabel.setText("Extent");
					ExtentLabel.setForeground(new Color(0, 0, 102));
					ExtentLabel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					panel20.add(ExtentLabel, cc.xywh(1, 1, 2, 1));

					//======== panel21 ========
					{
						panel21.setOpaque(false);
						panel21.setLayout(new FormLayout(
							new ColumnSpec[] {
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC
							},
							RowSpec.decodeSpecs("default")));

						//---- label_resourcesExtentNumber ----
						label_resourcesExtentNumber.setText("Extent");
						label_resourcesExtentNumber.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
						ATFieldInfo.assignLabelInfo(label_resourcesExtentNumber, Resources.class, Resources.PROPERTYNAME_EXTENT_NUMBER);
						panel21.add(label_resourcesExtentNumber, cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

						//---- resourcesExtentNumber ----
						resourcesExtentNumber.setColumns(4);
						resourcesExtentNumber.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
						panel21.add(resourcesExtentNumber, cc.xywh(3, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

						//---- extentType ----
						extentType.setOpaque(false);
						extentType.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
						panel21.add(extentType, new CellConstraints(5, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT, new Insets( 0, 5, 5, 5)));
					}
					panel20.add(panel21, cc.xy(2, 3));

					//---- label_resourcesExtentDescription ----
					label_resourcesExtentDescription.setText("Container Summary");
					label_resourcesExtentDescription.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
					ATFieldInfo.assignLabelInfo(label_resourcesExtentDescription, Resources.class, Resources.PROPERTYNAME_CONTAINER_SUMMARY);
					panel20.add(label_resourcesExtentDescription, cc.xy(2, 5));

					//======== scrollPane422 ========
					{
						scrollPane422.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
						scrollPane422.setOpaque(false);
						scrollPane422.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));

						//---- containerSummary ----
						containerSummary.setRows(4);
						containerSummary.setWrapStyleWord(true);
						containerSummary.setLineWrap(true);
						containerSummary.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
						scrollPane422.setViewportView(containerSummary);
					}
					panel20.add(scrollPane422, new CellConstraints(1, 7, 2, 1, CellConstraints.DEFAULT, CellConstraints.FILL, new Insets( 0, 15, 5, 5)));
				}
				panel5.add(panel20, cc.xywh(1, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
			}
			panel7.add(panel5, cc.xy(1, 11));

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
			panel7.add(panel23, cc.xy(1, 13));
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
				new ColumnSpec[] {
					new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC
				},
				new RowSpec[] {
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.NO_GROW),
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC
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
			}
			panel10.add(panel3, cc.xywh(1, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//---- label_repositoryName5 ----
			label_repositoryName5.setText("Identifiers");
			label_repositoryName5.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel10.add(label_repositoryName5, cc.xy(1, 3));

			//======== scrollPane9 ========
			{
				scrollPane9.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane9.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				scrollPane9.setPreferredSize(new Dimension(200, 104));

				//---- identifiersTable ----
				identifiersTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
				identifiersTable.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						identifiersMouseClicked(e);
					}
				});
				scrollPane9.setViewportView(identifiersTable);
			}
			panel10.add(scrollPane9, cc.xywh(1, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//======== panel25 ========
			{
				panel25.setBackground(new Color(231, 188, 251));
				panel25.setOpaque(false);
				panel25.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel25.setMinimumSize(new Dimension(100, 29));
				panel25.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC
					},
					RowSpec.decodeSpecs("default")));

				//---- addIdentifier ----
				addIdentifier.setText("Add Identifier");
				addIdentifier.setOpaque(false);
				addIdentifier.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				addIdentifier.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addIdentifierActionPerformed();
					}
				});
				panel25.add(addIdentifier, cc.xy(1, 1));

				//---- removeIdentifier ----
				removeIdentifier.setText("Remove Identifier");
				removeIdentifier.setOpaque(false);
				removeIdentifier.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				removeIdentifier.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeIdentifierActionPerformed();
					}
				});
				panel25.add(removeIdentifier, cc.xy(3, 1));
			}
			panel10.add(panel25, cc.xywh(1, 7, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));

			//---- label1 ----
			label1.setText("Instances");
			label1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			label1.setBackground(new Color(238, 238, 238));
			ATFieldInfo.assignLabelInfo(label1, ResourcesComponents.class, ResourcesComponents.PROPERTYNAME_INSTANCES);
			panel10.add(label1, cc.xywh(1, 9, 2, 1));

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
			panel10.add(scrollPane4, cc.xy(1, 11));

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
				panel24.add(addInstanceButton, cc.xy(1, 1));

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
				panel24.add(removeInstanceButton, cc.xy(3, 1));
			}
			panel10.add(panel24, cc.xywh(1, 13, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));

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
			panel10.add(panel4, cc.xy(1, 15));
		}
		add(panel10, cc.xywh(5, 1, 1, 1, CellConstraints.FILL, CellConstraints.FILL));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel panel7;
	private JLabel label_resourcesTitle;
	private JScrollPane scrollPane42;
	public JTextArea resourcesTitle;
	private JPanel tagApplicatorPanel;
	public JComboBox insertInlineTag;
	private JPanel panel38;
	private JPanel panel35;
	private JLabel label_resourcesDateExpression;
	public JTextField resourcesDateExpression;
	private JLabel Date1Label1;
	private JLabel label_resourcesDateBegin;
	public JFormattedTextField resourcesDateBegin;
	private JLabel label_resourcesDateEnd;
	public JFormattedTextField resourcesDateEnd;
	private JLabel BulkDatesLabel;
	private JLabel label_resourcesBulkDateBegin;
	public JFormattedTextField resourcesBulkDateBegin;
	private JLabel label_resourcesBulkDateEnd;
	public JFormattedTextField resourcesBulkDateEnd;
	private JPanel panel9;
	private JLabel label_resourcesLanguageCode2;
	public JComboBox resourcesLanguageCode;
	private JPanel panel5;
	private JPanel panel20;
	private JLabel ExtentLabel;
	private JPanel panel21;
	private JLabel label_resourcesExtentNumber;
	public JFormattedTextField resourcesExtentNumber;
	public JComboBox extentType;
	private JLabel label_resourcesExtentDescription;
	private JScrollPane scrollPane422;
	public JTextArea containerSummary;
	private JPanel panel23;
	private JLabel label_resourcesLanguageNote2;
	private JScrollPane scrollPane423;
	public JTextArea resourcesLanguageNote;
	private JSeparator separator2;
	private JPanel panel10;
	private JPanel panel3;
	private JLabel label_resourcesLevel;
	public JComboBox resourcesLevel;
	private JPanel panel12;
	private JLabel label3;
	public JTextField resourcesDateBegin2;
	private JLabel label_otherLevel;
	public JTextField resourcesOtherLevel;
	private JLabel label_repositoryName5;
	private JScrollPane scrollPane9;
	private DomainSortableTable identifiersTable;
	private JPanel panel25;
	private JButton addIdentifier;
	private JButton removeIdentifier;
	private JLabel label1;
	private JScrollPane scrollPane4;
	private DomainSortableTable instancesTable;
	private JPanel panel24;
	private JButton addInstanceButton;
	private JButton removeInstanceButton;
	private JPanel panel4;
	public JCheckBox restrictionsApply2;
	public JCheckBox resourcesRestrictionsApply;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public void setModel(ResourcesComponents resourceComponents) {
		this.resourceComponentModel = resourceComponents;
		identifiersTable.updateCollection(this.resourceComponentModel.getArchDescComponentIdentifiers());
		instancesTable.updateCollection(this.resourceComponentModel.getInstances());
	}

}