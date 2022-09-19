package application;

import java.io.IOException;
import java.util.Collections;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class View extends Main {
	
	private Stage primaryStage = null;
	private Controller controller = null;
	
	private ObservableList<String> schedulingAlgorithmList = FXCollections
			.observableArrayList("fcfs", "sjf", "rr", "hpfsp", "hpfsn");
	
	public View(Controller controller) {
		this.primaryStage = new Stage();
		this.controller = controller;
	}
	
	//public void processSchedulerMenu() {
		//try {
			//FXMLLoader loader = new FXMLLoader(getClass()
				//	.getResource("/application/processSchedulerMenu.fxml"));
		//	loader.setController(this.controller);
		//	Parent root = loader.load();
		//	this.primaryStage.setTitle("process scheduler menu");
		//	Scene scene = new Scene(root);
		//	this.primaryStage.setScene(scene);
		//	ObservableList<Node> children = root.getChildrenUnmodifiable();
		//	ChoiceBox schedulingAlgorithm = (ChoiceBox) children.get(1);
		//	schedulingAlgorithm.setValue("fcfs");
		//	schedulingAlgorithm.setItems(this.schedulingAlgorithmList);
		//	TextField timeQuantum = (TextField) children.get(4);
		//	this.primaryStage.show();
		//} 
		//catch(Exception e) {
		//	e.printStackTrace();
		//}
	//}

	public void createProcessMenu() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass()
					.getResource("/application/CreateProcessMenu.fxml"));
			loader.setController(this.controller);
			Parent root = loader.load();
			this.primaryStage.setTitle("create process menu");
			this.primaryStage.setScene(new Scene(root));
			this.primaryStage.show();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resultsMenu(String results) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass()
					.getResource("/application/resultsMenu.fxml"));
			loader.setController(this.controller);
			Parent root = loader.load();
			Stage resultsMenuStage = new Stage();
			resultsMenuStage.setTitle("results menu");
			resultsMenuStage.setScene(new Scene(root));
			ObservableList<Node> children = root.getChildrenUnmodifiable();
			TextArea resultsTextArea = (TextArea) children.get(2);
			resultsTextArea.setText(results);
			resultsMenuStage.show();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processSchedulerAnalysisMenu(XYChart.Series series, double CPUUtilization,
			double throughput, int averageTurnaroundTime,
			int averageWaitingTime, int averageResponseTime) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass()
					.getResource("/application/PSAnalysisMenu.fxml"));
			loader.setController(this.controller);
			Parent root = loader.load();
			Stage PSAnalysisMenuStage = new Stage();
			PSAnalysisMenuStage.setTitle("PS analysis menu");
			PSAnalysisMenuStage.setScene(new Scene(root));
			ObservableList<Node> children = root.getChildrenUnmodifiable();
			LineChart GraphOfNumberOfProcessesCompletedAgainstTime = (LineChart) children.get(0);
			GraphOfNumberOfProcessesCompletedAgainstTime.getData().add(series);
			NumberAxis xAxis = (NumberAxis) GraphOfNumberOfProcessesCompletedAgainstTime.getXAxis();
			NumberAxis yAxis = (NumberAxis) GraphOfNumberOfProcessesCompletedAgainstTime.getYAxis();
			xAxis.setLabel("time (ms)");
			yAxis.setLabel("processes completed");
			TextField CPUUtilizationTextField = (TextField) children.get(2);
			CPUUtilizationTextField.setText(Double.toString(CPUUtilization));
			TextField throughputTextField = (TextField) children.get(4);
			throughputTextField.setText(Double.toString(throughput));
			TextField averageTurnaroundTimeTextField = (TextField) children.get(6);
			averageTurnaroundTimeTextField.setText(Integer.toString(averageTurnaroundTime));
			TextField averageWaitingTimeTextField = (TextField) children.get(8);
			averageWaitingTimeTextField.setText(Integer.toString(averageWaitingTime));
			TextField averageResponseTimeTextField = (TextField) children.get(10);
			averageResponseTimeTextField.setText(Integer.toString(averageResponseTime));
			PSAnalysisMenuStage.show();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void MMUAnalysisMenu(ObservableList<PieChart.Data> pieChartData, int numberOfTranslatedAddresses,
			int pageFaults, double pageFaultRate,
			int TLBHits, double TLBHitRate) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass()
					.getResource("/application/MMUAnalysisMenu.fxml"));
			loader.setController(this.controller);
			Parent root = loader.load();
			Stage MMUAnalysisMenuStage = new Stage();
			MMUAnalysisMenuStage.setTitle("MMU analysis menu");
			MMUAnalysisMenuStage.setScene(new Scene(root));
			ObservableList<Node> children = root.getChildrenUnmodifiable();
			PieChart memoryUsage = (PieChart) children.get(0);
			memoryUsage.setData(pieChartData);
			TextField numberOfTranslatedAddressesTextField = (TextField) children.get(2);
			numberOfTranslatedAddressesTextField.setText(Integer.toString(numberOfTranslatedAddresses));
			TextField pageFaultsTextField = (TextField) children.get(4);
			pageFaultsTextField.setText(Integer.toString(pageFaults));
			TextField pageFaultRateTextField = (TextField) children.get(6);
			pageFaultRateTextField.setText(Double.toString(pageFaultRate));
			TextField TLBHitsTextField = (TextField) children.get(8);
			TLBHitsTextField.setText(Integer.toString(TLBHits));
			TextField TLBHitRateTextField = (TextField) children.get(10);
			TLBHitRateTextField.setText(Double.toString(numberOfTranslatedAddresses));
			MMUAnalysisMenuStage.show();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void FSAnalysisMenu(ObservableList<PieChart.Data> pieChartData, int seekDistance) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass()
					.getResource("/application/FSAnalysisMenu.fxml"));
			loader.setController(this.controller);
			Parent root = loader.load();
			Stage FSAnalysisMenuStage = new Stage();
			FSAnalysisMenuStage.setTitle("FS analysis menu");
			FSAnalysisMenuStage.setScene(new Scene(root));
			ObservableList<Node> children = root.getChildrenUnmodifiable();
			PieChart filesUsed = (PieChart) children.get(0);
			filesUsed.setData(pieChartData);
			TextField seekDistanceTextField = (TextField) children.get(2);
			seekDistanceTextField.setText(Integer.toString(seekDistance));
			FSAnalysisMenuStage.show();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void IOAnalysisMenu(XYChart.Series writeSeries, XYChart.Series readSeries,
			double averageWriteTime, double averageReadTime) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass()
					.getResource("/application/IOAnalysisMenu.fxml"));
			loader.setController(this.controller);
			Parent root = loader.load();
			Stage IOAnalysisMenuStage = new Stage();
			IOAnalysisMenuStage.setTitle("IO analysis menu");
			IOAnalysisMenuStage.setScene(new Scene(root));
			ObservableList<Node> children = root.getChildrenUnmodifiable();
			BarChart peripheralUsageBarChart = (BarChart) children.get(0);
			peripheralUsageBarChart.getData().add(writeSeries);
			peripheralUsageBarChart.getData().add(readSeries);
			Axis xAxis = peripheralUsageBarChart.getXAxis();
			NumberAxis yAxis = (NumberAxis) peripheralUsageBarChart.getYAxis();
			xAxis.setLabel("peripherals");
			yAxis.setLabel("number of operations");
			TextField averageWriteTimeTextField = (TextField) children.get(2);
			averageWriteTimeTextField.setText(Double.toString(averageWriteTime));
			TextField averageReadTimeTextField = (TextField) children.get(4);
			averageReadTimeTextField.setText(Double.toString(averageReadTime));
			IOAnalysisMenuStage.show();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
