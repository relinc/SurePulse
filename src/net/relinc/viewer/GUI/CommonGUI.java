package net.relinc.viewer.GUI;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.relinc.libraries.application.FileFX;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.sample.SampleGroup;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class CommonGUI {
	public static Stage stage;
	
	static ListView<Sample> realCurrentSamplesListView = new ListView<Sample>();
	
	//********Region for GUI for right option pane to open
	static AnchorPane optionPane = new AnchorPane();
	static TreeView<FileFX> sampleDirectoryTreeView = new TreeView<FileFX>();
	static Button changeDirectoryButton = new Button("Change Directory");
	static Button refreshDirectoryButton = new Button("", SPOperations.getIcon("/net/relinc/viewer/images/refreshIcon.png"));
	static Button xButton = new Button("X");
	static Button addSelectedSampleButton = new Button("Add Selected Sample(s)");
	//*******
	static String treeViewHomePath = SPSettings.Workspace.getPath() + "/Sample Data";
	
	
	//********Region for GUI for export pane to open
	static TreeItem<String> sampleGroupRoot;
	static ArrayList<SampleGroup> sampleGroups = new ArrayList<SampleGroup>();
	static SampleGroup currentSelectedSampleGroup;
	static TextField tbSampleGroup = new TextField();
	static Button buttonCreateSampleGroup = new Button("Create Group");
	static TreeView<String> treeViewSampleGroups = new TreeView<String>();
	static Button buttonAddSampleToGroup = new Button("Add Samples to Group");
	static Button buttonExportData = new Button("Export To Excel");
	static Button buttonExportCSV = new Button("Export CSV");
	static Button buttonDeleteSelectedGroup = new Button("Delete Group");
	static CheckBox includeSummaryPage = new CheckBox("Include Summary Page");
	//*******
}
