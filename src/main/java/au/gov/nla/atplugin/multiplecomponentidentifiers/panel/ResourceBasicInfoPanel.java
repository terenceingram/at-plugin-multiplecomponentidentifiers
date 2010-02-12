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

public class ResourceBasicInfoPanel extends NLADomainEditorFields {

	private Resources resourceModel;
	protected ArchDescriptionInstances currentInstance;
	protected ArchDescriptionInstancesEditor dialogInstances;
	protected String defaultInstanceType = "";


	public ResourceBasicInfoPanel(PresentationModel detailsModel) {
		this.detailsModel = detailsModel;
		initComponents();
		
		accessionsTable.setClazzAndColumns(AccessionsResources.PROPERTYNAME_ACCESSION_NUMBER,
				AccessionsResources.class,
				AccessionsResources.PROPERTYNAME_ACCESSION_NUMBER,
				AccessionsResources.PROPERTYNAME_ACCESSION_TITLE);
		
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
	
	private void changeRepositoryButtonActionPerformed() {
		Vector repositories = Repositories.getRepositoryList();
		ImageIcon icon = null;
		
		//Repositories currentRepostory = ((Resources) this.getModel()).getRepository();
		Repositories currentRepostory = this.resourceModel.getRepository();
		Resources model = resourceModel;
        //SelectFromList dialog = new SelectFromList(this.getParentEditor(), "Select a repository", repositories.toArray());
		SelectFromList dialog = new SelectFromList(editorField.getParentEditor(), "Select a repository", repositories.toArray());
        dialog.setSelectedValue(currentRepostory);
        if (dialog.showDialog() == JOptionPane.OK_OPTION) {
            model.setRepository((Repositories)dialog.getSelectedValue());
            setRepositoryText(model);
            ApplicationFrame.getInstance().setRecordDirty(); // set the record dirty
        }
	}

	private void setRepositoryText(Resources model) {
		if (model.getRepository() == null) {
			this.repositoryName.setText("");
		} else {
			this.repositoryName.setText(model.getRepository().getShortName());
		}
	}

	public JButton getChangeRepositoryButton() {
		return changeRepositoryButton;
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
            //dialogInstances = (ArchDescriptionInstancesEditor) DomainEditorFactory.getInstance().createDomainEditorWithParent(ArchDescriptionInstances.class, getParentEditor(), getInstancesTable());
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

			if ((defaultInstanceType != null) && (defaultInstanceType.length() > 0)) {
				if (defaultInstanceType.equalsIgnoreCase(ArchDescriptionInstances.DIGITAL_OBJECT_INSTANCE)) {
					newInstance = new ArchDescriptionDigitalInstances(resourceModel,
                            (Resources) editorField.getModel());
					addDatesToNewDigitalInstance((ArchDescriptionDigitalInstances)newInstance, resourceModel);
				} else if (defaultInstanceType.equalsIgnoreCase(ArchDescriptionInstances.DIGITAL_OBJECT_INSTANCE_LINK)) {
                    // add a digital object link or links instead
                    addDigitalInstanceLink((Resources) editorField.getModel());
                    return;
				} else {
					newInstance = new ArchDescriptionAnalogInstances(resourceModel);
				}

                newInstance.setInstanceType(defaultInstanceType);

                // see whether to use a plugin
                if(usePluginDomainEditor(true, newInstance, getInstancesTable())) {
                    return;
                }

				dialogInstances.setModel(newInstance, null);
				dialogInstances.setResourceInfo((Resources) editorField.getModel());
				returnStatus = dialogInstances.showDialog();
				if (returnStatus == JOptionPane.OK_OPTION) {
					dialogInstances.commitChangesToCurrentRecord();
					resourceModel.addInstance(newInstance);
					getInstancesTable().getEventList().add(newInstance);
					findLocationForInstance(newInstance);
                    ApplicationFrame.getInstance().setRecordDirty(); // set the record dirty
					done = true;
				} else if (returnStatus == StandardEditor.OK_AND_ANOTHER_OPTION) {
					dialogInstances.commitChangesToCurrentRecord();
					resourceModel.addInstance(newInstance);
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
		DigitalObjectLookup digitalObjectPicker = new DigitalObjectLookup(getParentEditor(), editorField);
		digitalObjectPicker.setParentResource(parentResource);
		digitalObjectPicker.showDialog(this);
	}

	private void removeInstanceButtonActionPerformed() {
		ArchDescription archDescriptionModel = (ArchDescription) super.getModel();
		try {
//			this.removeRelatedTableRow(physicalDescriptionsTable, resourceModel);
			this.removeRelatedTableRow(getInstancesTable(), resourceModel);
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

	public DomainSortableTable getIdentifiersTable() {
		return identifiersTable;
	}

	private void addIdentifierActionPerformed() {
		addIdentifierActionPerformed(identifiersTable, resourceModel);
	}

	private void removeIdentifierActionPerformed() {
		try {
			this.removeRelatedTableRow(identifiersTable, resourceModel);
		} catch (ObjectNotRemovedException e1) {
			new ErrorDialog("Identifier not removed", e1).showDialog();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		panel16 = new JPanel();
		label_resourcesTitle = new JLabel();
		scrollPane2 = new JScrollPane();
		resourcesTitle = ATBasicComponentFactory.createTextArea(detailsModel.getModel(ArchDescription.PROPERTYNAME_TITLE),false);
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
		panel1 = new JPanel();
		label_resourcesLanguageCode = new JLabel();
		resourcesLanguageCode = ATBasicComponentFactory.createComboBox(detailsModel, Resources.PROPERTYNAME_LANGUAGE_CODE, Resources.class);
		panel37 = new JPanel();
		panel20 = new JPanel();
		ExtentLabel = new JLabel();
		panel21 = new JPanel();
		label_resourcesExtentNumber = new JLabel();
		resourcesExtentNumber = ATBasicComponentFactory.createDoubleField(detailsModel,Resources.PROPERTYNAME_EXTENT_NUMBER);
		extentType = ATBasicComponentFactory.createComboBox(detailsModel, Resources.PROPERTYNAME_EXTENT_TYPE, Resources.class);
		label_resourcesExtentDescription = new JLabel();
		scrollPane422 = new JScrollPane();
		containerSummary = ATBasicComponentFactory.createTextArea(detailsModel.getModel(Resources.PROPERTYNAME_CONTAINER_SUMMARY),false);
		label_resourcesLanguageNote = new JLabel();
		scrollPane423 = new JScrollPane();
		resourcesLanguageNote = ATBasicComponentFactory.createTextArea(detailsModel.getModel(Resources.PROPERTYNAME_REPOSITORY_PROCESSING_NOTE),false);
		panel6 = new JPanel();
		label_agreementReceived2 = new JLabel();
		repositoryName = new JTextField();
		changeRepositoryButton = new JButton();
		separator2 = new JSeparator();
		panel13 = new JPanel();
		panel12 = new JPanel();
		label_resourceIdentifier1 = new JLabel();
		resourceIdentifier1 = ATBasicComponentFactory.createTextField(detailsModel.getModel(Resources.PROPERTYNAME_RESOURCE_IDENTIFIER_1));
		resourceIdentifier2 = ATBasicComponentFactory.createTextField(detailsModel.getModel(Resources.PROPERTYNAME_RESOURCE_IDENTIFIER_2));
		resourceIdentifier3 = ATBasicComponentFactory.createTextField(detailsModel.getModel(Resources.PROPERTYNAME_RESOURCE_IDENTIFIER_3));
		resourceIdentifier4 = ATBasicComponentFactory.createTextField(detailsModel.getModel(Resources.PROPERTYNAME_RESOURCE_IDENTIFIER_4));
		panel19 = new JPanel();
		label_resourcesLevel = new JLabel();
		resourcesLevel = ATBasicComponentFactory.createComboBox(detailsModel, Resources.PROPERTYNAME_LEVEL, Resources.class);
		label_otherLevel = new JLabel();
		resourcesOtherLevel = ATBasicComponentFactory.createTextField(detailsModel.getModel(Resources.PROPERTYNAME_OTHER_LEVEL),false);
		label_repositoryName5 = new JLabel();
		scrollPane9 = new JScrollPane();
		identifiersTable = new DomainSortableTable();
		panel23 = new JPanel();
		addIdentifier = new JButton();
		removeIdentifier = new JButton();
		label1 = new JLabel();
		scrollPane6 = new JScrollPane();
		instancesTable = new DomainSortableTable(ArchDescriptionInstances.class, ArchDescriptionInstances.PROPERTYNAME_INSTANCE_TYPE);
		panel29 = new JPanel();
		addInstanceButton = new JButton();
		removeInstanceButton = new JButton();
		restrictionsApply = ATBasicComponentFactory.createCheckBox(detailsModel, ArchDescription.PROPERTYNAME_RESTRICTIONS_APPLY, Resources.class);
		OtherAccessionsLabel = new JLabel();
		scrollPane4 = new JScrollPane();
		accessionsTable = new DomainSortableTable();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBackground(new Color(200, 205, 232));
		setLayout(new FormLayout(
			new ColumnSpec[] {
				new ColumnSpec("max(min;300px)"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC
			},
			RowSpec.decodeSpecs("default")));

		//======== panel16 ========
		{
			panel16.setOpaque(false);
			panel16.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel16.setBorder(Borders.DLU2_BORDER);
			panel16.setLayout(new FormLayout(
				ColumnSpec.decodeSpecs("default:grow, default"),
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
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					new RowSpec(RowSpec.TOP, Sizes.DEFAULT, FormSpec.NO_GROW),
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC
				}));

			//---- label_resourcesTitle ----
			label_resourcesTitle.setText("Title");
			label_resourcesTitle.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			ATFieldInfo.assignLabelInfo(label_resourcesTitle, Resources.class, Resources.PROPERTYNAME_TITLE);
			panel16.add(label_resourcesTitle, cc.xy(1, 1));

			//======== scrollPane2 ========
			{

				//---- resourcesTitle ----
				resourcesTitle.setRows(4);
				resourcesTitle.setLineWrap(true);
				resourcesTitle.setWrapStyleWord(true);
				resourcesTitle.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				scrollPane2.setViewportView(resourcesTitle);
			}
			panel16.add(scrollPane2, cc.xywh(1, 3, 2, 1));

			//---- insertInlineTag ----
			insertInlineTag.setOpaque(false);
			insertInlineTag.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			insertInlineTag.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertInlineTagActionPerformed();
				}
			});
			panel16.add(insertInlineTag, cc.xy(1, 5));

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
			panel16.add(panel38, cc.xywh(1, 7, 2, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//======== panel1 ========
			{
				panel1.setOpaque(false);
				panel1.setLayout(new FormLayout(
					new ColumnSpec[] {
						new ColumnSpec(ColumnSpec.LEFT, Sizes.DEFAULT, FormSpec.NO_GROW),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec("left:min(default;200px)")
					},
					RowSpec.decodeSpecs("default")));

				//---- label_resourcesLanguageCode ----
				label_resourcesLanguageCode.setText("Language");
				label_resourcesLanguageCode.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourcesLanguageCode, Resources.class, Resources.PROPERTYNAME_LANGUAGE_CODE);
				panel1.add(label_resourcesLanguageCode, cc.xy(1, 1));

				//---- resourcesLanguageCode ----
				resourcesLanguageCode.setMaximumSize(new Dimension(50, 27));
				resourcesLanguageCode.setOpaque(false);
				resourcesLanguageCode.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel1.add(resourcesLanguageCode, cc.xywh(3, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
			}
			panel16.add(panel1, cc.xy(1, 9));

			//======== panel37 ========
			{
				panel37.setBorder(new BevelBorder(BevelBorder.LOWERED));
				panel37.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel37.setBackground(new Color(182, 187, 212));
				panel37.setLayout(new FormLayout(
					"60px:grow",
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
				panel37.add(panel20, cc.xywh(1, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
			}
			panel16.add(panel37, cc.xywh(1, 11, 2, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//---- label_resourcesLanguageNote ----
			label_resourcesLanguageNote.setText("Repository Processing Note");
			label_resourcesLanguageNote.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			ATFieldInfo.assignLabelInfo(label_resourcesLanguageNote, Resources.class, Resources.PROPERTYNAME_REPOSITORY_PROCESSING_NOTE);
			panel16.add(label_resourcesLanguageNote, cc.xy(1, 13));

			//======== scrollPane423 ========
			{
				scrollPane423.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane423.setOpaque(false);
				scrollPane423.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));

				//---- resourcesLanguageNote ----
				resourcesLanguageNote.setRows(4);
				resourcesLanguageNote.setLineWrap(true);
				resourcesLanguageNote.setWrapStyleWord(true);
				resourcesLanguageNote.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				scrollPane423.setViewportView(resourcesLanguageNote);
			}
			panel16.add(scrollPane423, cc.xywh(1, 15, 2, 1));

			//======== panel6 ========
			{
				panel6.setOpaque(false);
				panel6.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel6.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
					},
					RowSpec.decodeSpecs("default")));

				//---- label_agreementReceived2 ----
				label_agreementReceived2.setText("Repository:");
				label_agreementReceived2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_agreementReceived2, Resources.class, Resources.PROPERTYNAME_REPOSITORY);
				panel6.add(label_agreementReceived2, cc.xy(1, 1));

				//---- repositoryName ----
				repositoryName.setEditable(false);
				repositoryName.setOpaque(false);
				repositoryName.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				repositoryName.setBorder(null);
				panel6.add(repositoryName, cc.xy(3, 1));

				//---- changeRepositoryButton ----
				changeRepositoryButton.setText("Change");
				changeRepositoryButton.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				changeRepositoryButton.setOpaque(false);
				changeRepositoryButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						changeRepositoryButtonActionPerformed();
					}
				});
				panel6.add(changeRepositoryButton, cc.xywh(5, 1, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
			}
			panel16.add(panel6, cc.xy(1, 17));
		}
		add(panel16, cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

		//---- separator2 ----
		separator2.setForeground(new Color(147, 131, 86));
		separator2.setOrientation(SwingConstants.VERTICAL);
		add(separator2, cc.xywh(3, 1, 1, 1, CellConstraints.FILL, CellConstraints.FILL));

		//======== panel13 ========
		{
			panel13.setOpaque(false);
			panel13.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel13.setBorder(Borders.DLU2_BORDER);
			panel13.setLayout(new FormLayout(
				ColumnSpec.decodeSpecs("default:grow, 3dlu"),
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
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC
				}));

			//======== panel12 ========
			{
				panel12.setBackground(new Color(231, 188, 251));
				panel12.setOpaque(false);
				panel12.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel12.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
					},
					RowSpec.decodeSpecs("default")));
				((FormLayout)panel12.getLayout()).setColumnGroups(new int[][] {{3, 5, 7, 9}});

				//---- label_resourceIdentifier1 ----
				label_resourceIdentifier1.setText("Resource ID");
				label_resourceIdentifier1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourceIdentifier1, Resources.class, Resources.PROPERTYNAME_RESOURCE_IDENTIFIER);
				panel12.add(label_resourceIdentifier1, cc.xy(1, 1));

				//---- resourceIdentifier1 ----
				resourceIdentifier1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel12.add(resourceIdentifier1, cc.xy(3, 1));

				//---- resourceIdentifier2 ----
				resourceIdentifier2.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel12.add(resourceIdentifier2, cc.xy(5, 1));

				//---- resourceIdentifier3 ----
				resourceIdentifier3.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel12.add(resourceIdentifier3, cc.xy(7, 1));

				//---- resourceIdentifier4 ----
				resourceIdentifier4.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel12.add(resourceIdentifier4, cc.xy(9, 1));
			}
			panel13.add(panel12, cc.xy(1, 1));

			//======== panel19 ========
			{
				panel19.setOpaque(false);
				panel19.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel19.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						new ColumnSpec(Sizes.DLUX3),
						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC
					},
					RowSpec.decodeSpecs("default")));

				//---- label_resourcesLevel ----
				label_resourcesLevel.setText("Level");
				label_resourcesLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_resourcesLevel, Resources.class, Resources.PROPERTYNAME_LEVEL);
				panel19.add(label_resourcesLevel, cc.xywh(1, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

				//---- resourcesLevel ----
				resourcesLevel.setOpaque(false);
				resourcesLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				resourcesLevel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						resourcesLevelActionPerformed();
					}
				});
				panel19.add(resourcesLevel, cc.xywh(3, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));

				//---- label_otherLevel ----
				label_otherLevel.setText("Other Level");
				label_otherLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				ATFieldInfo.assignLabelInfo(label_otherLevel, Resources.class, Resources.PROPERTYNAME_OTHER_LEVEL);
				panel19.add(label_otherLevel, cc.xy(5, 1));

				//---- resourcesOtherLevel ----
				resourcesOtherLevel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel19.add(resourcesOtherLevel, new CellConstraints(7, 1, 3, 1, CellConstraints.FILL, CellConstraints.TOP, new Insets( 0, 0, 0, 5)));
			}
			panel13.add(panel19, cc.xy(1, 3));

			//---- label_repositoryName5 ----
			label_repositoryName5.setText("Identifiers");
			label_repositoryName5.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel13.add(label_repositoryName5, cc.xy(1, 5));

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
			panel13.add(scrollPane9, cc.xywh(1, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//======== panel23 ========
			{
				panel23.setBackground(new Color(231, 188, 251));
				panel23.setOpaque(false);
				panel23.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel23.setMinimumSize(new Dimension(100, 29));
				panel23.setLayout(new FormLayout(
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
				panel23.add(addIdentifier, cc.xy(1, 1));

				//---- removeIdentifier ----
				removeIdentifier.setText("Remove Identifier");
				removeIdentifier.setOpaque(false);
				removeIdentifier.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				removeIdentifier.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeIdentifierActionPerformed();
					}
				});
				panel23.add(removeIdentifier, cc.xy(3, 1));
			}
			panel13.add(panel23, cc.xywh(1, 9, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));

			//---- label1 ----
			label1.setText("Instances");
			label1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			ATFieldInfo.assignLabelInfo(label1, Resources.class, ResourcesComponents.PROPERTYNAME_INSTANCES);
			panel13.add(label1, cc.xy(1, 11));

			//======== scrollPane6 ========
			{
				scrollPane6.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane6.setOpaque(false);
				scrollPane6.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));

				//---- instancesTable ----
				instancesTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
				instancesTable.setRowHeight(20);
				instancesTable.setFocusable(false);
				instancesTable.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						instancesTableMouseClicked(e);
					}
				});
				scrollPane6.setViewportView(instancesTable);
			}
			panel13.add(scrollPane6, cc.xywh(1, 13, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

			//======== panel29 ========
			{
				panel29.setBackground(new Color(231, 188, 251));
				panel29.setOpaque(false);
				panel29.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				panel29.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC
					},
					RowSpec.decodeSpecs("default")));

				//---- addInstanceButton ----
				addInstanceButton.setText("Add Instance");
				addInstanceButton.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				addInstanceButton.setOpaque(false);
				addInstanceButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addInstanceButtonActionPerformed();
					}
				});
				panel29.add(addInstanceButton, cc.xy(1, 1));

				//---- removeInstanceButton ----
				removeInstanceButton.setText("Remove Instance");
				removeInstanceButton.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
				removeInstanceButton.setOpaque(false);
				removeInstanceButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeInstanceButtonActionPerformed();
					}
				});
				panel29.add(removeInstanceButton, cc.xy(3, 1));
			}
			panel13.add(panel29, cc.xywh(1, 15, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));

			//---- restrictionsApply ----
			restrictionsApply.setText("Restrictions Apply");
			restrictionsApply.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			restrictionsApply.setOpaque(false);
			restrictionsApply.setText(ATFieldInfo.getLabel(Resources.class, ArchDescription.PROPERTYNAME_RESTRICTIONS_APPLY));
			panel13.add(restrictionsApply, cc.xy(1, 17));

			//---- OtherAccessionsLabel ----
			OtherAccessionsLabel.setText("Accessions linked to this Resource ID:");
			OtherAccessionsLabel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			panel13.add(OtherAccessionsLabel, cc.xy(1, 19));

			//======== scrollPane4 ========
			{
				scrollPane4.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane4.setPreferredSize(new Dimension(300, 100));
				scrollPane4.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));

				//---- accessionsTable ----
				accessionsTable.setPreferredScrollableViewportSize(new Dimension(300, 100));
				accessionsTable.setFocusable(false);
				scrollPane4.setViewportView(accessionsTable);
			}
			panel13.add(scrollPane4, cc.xywh(1, 21, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
		}
		add(panel13, cc.xywh(5, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel panel16;
	private JLabel label_resourcesTitle;
	private JScrollPane scrollPane2;
	public JTextArea resourcesTitle;
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
	private JPanel panel1;
	private JLabel label_resourcesLanguageCode;
	public JComboBox resourcesLanguageCode;
	private JPanel panel37;
	private JPanel panel20;
	private JLabel ExtentLabel;
	private JPanel panel21;
	private JLabel label_resourcesExtentNumber;
	public JFormattedTextField resourcesExtentNumber;
	public JComboBox extentType;
	private JLabel label_resourcesExtentDescription;
	private JScrollPane scrollPane422;
	public JTextArea containerSummary;
	private JLabel label_resourcesLanguageNote;
	private JScrollPane scrollPane423;
	public JTextArea resourcesLanguageNote;
	private JPanel panel6;
	private JLabel label_agreementReceived2;
	public JTextField repositoryName;
	private JButton changeRepositoryButton;
	private JSeparator separator2;
	private JPanel panel13;
	private JPanel panel12;
	private JLabel label_resourceIdentifier1;
	public JTextField resourceIdentifier1;
	public JTextField resourceIdentifier2;
	public JTextField resourceIdentifier3;
	public JTextField resourceIdentifier4;
	private JPanel panel19;
	private JLabel label_resourcesLevel;
	public JComboBox resourcesLevel;
	private JLabel label_otherLevel;
	public JTextField resourcesOtherLevel;
	private JLabel label_repositoryName5;
	private JScrollPane scrollPane9;
	private DomainSortableTable identifiersTable;
	private JPanel panel23;
	private JButton addIdentifier;
	private JButton removeIdentifier;
	private JLabel label1;
	private JScrollPane scrollPane6;
	private DomainSortableTable instancesTable;
	private JPanel panel29;
	private JButton addInstanceButton;
	private JButton removeInstanceButton;
	public JCheckBox restrictionsApply;
	private JLabel OtherAccessionsLabel;
	private JScrollPane scrollPane4;
	private DomainSortableTable accessionsTable;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public void setModel(Resources resourcesModel) {
		this.resourceModel = resourcesModel;
		identifiersTable.updateCollection(this.resourceModel.getArchDescComponentIdentifiers());
		instancesTable.updateCollection(this.resourceModel.getInstances());
		setRepositoryText(this.resourceModel);
	}
	
}
