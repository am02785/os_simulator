package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Controller implements Initializable{
	
	private OSSimulator osSimulator = null;
	private File file = null;
		
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
		
	public void start(ActionEvent event) throws IOException {
		Button button = (Button) event.getSource();
		Stage stage = (Stage) button.getScene().getWindow();
		stage.close();
		this.osSimulator = new OSSimulator(this);
	}
	
	//public void startProcessScheduler(ActionEvent event) {
	//	Button button = (Button) event.getSource();
	//	Scene scene = button.getScene();
	//	Parent parent =  scene.getRoot();
	//	ObservableList<Node> children = parent.getChildrenUnmodifiable();
	//	ChoiceBox schedulingAlgorithmChoiceBox = (ChoiceBox) children.get(1);
	//	String schedulingAlgorithm = (String) schedulingAlgorithmChoiceBox.getValue();
	//	int timeQuantum = Integer.MAX_VALUE;
	//	if (schedulingAlgorithm.equals("rr") || schedulingAlgorithm.equals("hpfsp")) {
	//		TextField timeQuantumTextField = (TextField) children.get(4);
	//		timeQuantum = (Integer) Integer.parseInt(timeQuantumTextField.getText().replace(" ",""));
	//	}
	//	this.osSimulator.startProcessScheduler(schedulingAlgorithm, timeQuantum, this.file);
	//}
	
	public void chooseFile(ActionEvent event) {
		Button button = (Button) event.getSource();
		Scene scene = button.getScene();
		Window window = scene.getWindow();
		FileChooser fileChooser = new FileChooser();
		this.file = fileChooser.showOpenDialog(window);
		if (this.file != null) {
			this.osSimulator.setFile(this.file);
		}
	}
	
	public void createProcess(ActionEvent event) {
		Button button = (Button) event.getSource();
		Scene scene = button.getScene();
		Parent parent = scene.getRoot();
		ObservableList<Node> children = parent.getChildrenUnmodifiable();
		TextArea instructionsTextArea = (TextArea) children.get(1);
		String instructions = instructionsTextArea.getText();
		TextField CPUBurstTextField = (TextField) children.get(3);
		int CPUBurst = Integer.parseInt(CPUBurstTextField.getText().replace(" ", ""));
		TextField priorityTextField = (TextField) children.get(5);
		int priority = Integer.parseInt(priorityTextField.getText().replace(" ", ""));
		TextField memoryRequiredTextField = (TextField) children.get(11);
		int memoryRequired = 0;
		if (!(memoryRequiredTextField.getText().equals(""))) {
			memoryRequired = Integer.parseInt(memoryRequiredTextField.getText().replace(" ", ""));
		}
		instructionsTextArea.setText("");
		CPUBurstTextField.setText("");
		priorityTextField.setText("");
		memoryRequiredTextField.setText("");
		this.osSimulator.createProcess(instructions, CPUBurst, priority, memoryRequired);
	}
	
	public void showResults() {
		this.osSimulator.showResults();
	}
	
	public void showPSAnalysis() {
		this.osSimulator.showPSAnalysis();
	}
	
	public void showMMUAnalysis() {
		this.osSimulator.showMMUAnalysis();
	}
	
	public void showFSAnalysis() {
		this.osSimulator.showFSAnalysis();
	}
	
	public void showIOAnalysis() {
		this.osSimulator.showIOAnalysis();
	}
	
	public void close(ActionEvent event) {
		Button button = (Button) event.getSource();
		Scene scene = button.getScene();
		Window window = scene.getWindow();
		window.hide();
	}
	
	public void closeOSSimulator(ActionEvent event) {
		Button button = (Button) event.getSource();
		Scene scene = button.getScene();
		Window window = scene.getWindow();
		window.hide();
		System.exit(0);
	}
	
}
