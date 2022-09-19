package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

public class OSSimulator {
	
	private File file = null;
	private Vector readyBuffer = null;
	private Vector runningBuffer = null;
	private Vector terminatedBuffer = null;
	private Vector VMBuffer = null;
	private Vector fileSystemBuffer = null;
	private Vector IOBuffer = null;
	private ProcessCreation processCreation = null;
	private Dispatcher dispatcher = null;
	private CPU cpu = null;
	private IO io = null;
	private int sizeOfreadyBuffer = 0;
	private int sizeOfrunningBuffer = 0;
	private int sizeOfVMBuffer = 0;
	private int sizeOfFileSystemBuffer = 0;
	private int sizeOfIOBuffer = 0;
	private VM vm = null;
	private int virtualMemorySize = 0;
	private Bank bank = null;
	private JavaFileSystem fileSystem = null;
	private View view = null;
	private int initialTime = 0;
	
	public OSSimulator(Controller controller) throws IOException {
		super();
		this.readyBuffer = new Vector();
		this.runningBuffer = new Vector();
		this.terminatedBuffer = new Vector();
		this.VMBuffer = new Vector();
		this.fileSystemBuffer = new Vector();
		this.IOBuffer = new Vector();
		Scanner myReader = new Scanner(new File("ini.txt"));
		int numberOfProcesses = Integer.parseInt(myReader.nextLine());
		String schedulingAlgorithm = myReader.nextLine();
		int timeQuantum = Integer.MAX_VALUE;
		if (schedulingAlgorithm.equals("RR") || schedulingAlgorithm.equals("HPFSP")) {
			timeQuantum = Integer.parseInt(myReader.nextLine());
		}
		String fileName = myReader.nextLine();
		int pageTableEntries = Integer.parseInt(myReader.nextLine());
		int numberOfFrames = Integer.parseInt(myReader.nextLine());
		int pageSize = Integer.parseInt(myReader.nextLine());
		int TLBSize = Integer.parseInt(myReader.nextLine());
		String pageReplacementAlgorithm = myReader.nextLine();
		int blockSize = Integer.parseInt(myReader.nextLine());
		int numBlocks = Integer.parseInt(myReader.nextLine());
		int nodeSize = Integer.parseInt(myReader.nextLine());
		int maxFiles = Integer.parseInt(myReader.nextLine());
		String diskSchedulingAlgorithm = myReader.nextLine();
		myReader.close();
		this.sizeOfreadyBuffer = numberOfProcesses;
		this.sizeOfrunningBuffer = numberOfProcesses;
		this.sizeOfVMBuffer = numberOfProcesses;
		this.sizeOfFileSystemBuffer = numberOfProcesses;
		if (fileName.equals("")) {
			try {
				this.file = new File("processes.txt");
				if (!this.file.createNewFile()) {
					this.file.delete();
				}
				this.file.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			this.file = new File(fileName);
		}
		this.initialTime = (int) (LocalTime.now().toNanoOfDay() / Math.pow(10, 6));
		this.processCreation = new ProcessCreation(this.readyBuffer, this.sizeOfreadyBuffer,
				this.file, this.initialTime);
		this.processCreation.start();
		this.dispatcher = new Dispatcher(this.processCreation, this.runningBuffer,
				this.sizeOfrunningBuffer, schedulingAlgorithm);
		this.dispatcher.start();
		this.virtualMemorySize = pageTableEntries * pageSize;
		int resources[] = new int[1];
		resources[0] = this.virtualMemorySize;
		this.bank = new BankImpl(resources);
		this.cpu = new CPU(this.processCreation, this.dispatcher,
				this.terminatedBuffer, this.VMBuffer,
				this.fileSystemBuffer, this.IOBuffer,
				this.sizeOfVMBuffer, this.sizeOfFileSystemBuffer,
				this.sizeOfIOBuffer, timeQuantum,
				this.vm, this.virtualMemorySize,
				this.bank, this.fileSystem);
		this.cpu.start();
		this.fileSystem = new JavaFileSystem(blockSize, numBlocks,
				nodeSize, maxFiles,
				diskSchedulingAlgorithm, this.processCreation,
				this.cpu);
		this.fileSystem.start();
		this.cpu.setFileSystem(this.fileSystem);
		this.io = new IO(this.processCreation, this.cpu);
		this.io.start();
		this.cpu.setIO(this.io);
		this.vm = new VM(pageTableEntries, numberOfFrames, pageSize,
				TLBSize, pageReplacementAlgorithm, this.processCreation, this.cpu,
				this.bank);
		this.makeBackingStore();
		this.vm.start();
		this.cpu.setVM(this.vm);
		this.view = new View(controller);
		this.view.createProcessMenu();
	}
	
	//public void startProcessScheduler(String schedulingAlgorithm, int timeQuantum, File file) {
	//	if (file == null) {
	//		try {
	//			this.file = new File("processes.txt");
	//			if (!this.file.createNewFile()) {
	//				this.file.delete();
	//			}
	//			this.file.createNewFile();
	//		}
	//		catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}
	//	else {
	//		this.file = file;
	//	}
	//	this.initialTime = (int) (LocalTime.now().toNanoOfDay() / Math.pow(10, 6));
	//	this.processCreation = new ProcessCreation(this.readyBuffer, this.sizeOfreadyBuffer,
	//			this.file, this.initialTime);
	//	this.processCreation.start();
	//	this.dispatcher = new Dispatcher(this.processCreation, this.runningBuffer,
	//			this.sizeOfrunningBuffer, schedulingAlgorithm);
	//	this.dispatcher.start();
	//	this.cpu = new CPU(this.processCreation, this.dispatcher,
	//			this.terminatedBuffer, this.fileSystemBuffer,
	//			this.IOBuffer, this.sizeOfIOBuffer,
	//			this.sizeOfFileSystemBuffer, timeQuantum,
	//			this.vm, this.virtualMemorySize,
	//			this.fileSystem);
	//	this.cpu.start();
	//	this.fileSystem.setCPU(this.cpu);
	//	this.fileSystem.start();
	//	this.io = new IO(this.processCreation, this.cpu);
	//	this.io.start();
	//	this.vm.setProcessCreation(this.processCreation);
	//	this.fileSystem.setProcessCreation(this.processCreation);
	//	this.view.createProcessMenu();
	//}
	
	public void createProcess(String instructions, int CPUBurst, int priority, int memoryRequired) {
		try {
			FileWriter myWriter = new FileWriter(this.file);
			instructions = instructions.replace("\n", "");
			String[] processesArray = instructions.split(";");
			List<String> processes = new ArrayList<>();
			for (String process : processesArray) {
				while (process.startsWith(" ")) {
					process = process.substring(1);
				}
				while (process.endsWith(" ")) {
					process = process.substring(0, process.length() - 2);
				}
				processes.add(process + ";");
			}
			if (!(processes.get(processes.size() -1).equals("exit;"))) {
				processes.add("exit;");
			}
			for (String process : processes) {
				myWriter.write(process);
			}
			myWriter.write(CPUBurst + ";");
			myWriter.write(priority + ";");
			myWriter.write(memoryRequired + ";\n");
			myWriter.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setFile(File file) {
		this.file = file;
		this.processCreation.setFile(file);
	}
	
	public void makeBackingStore() throws IOException {
		File file = null;
		RandomAccessFile disk = null;
		try {
			file = new File("BACKING_STORE");
			disk = new RandomAccessFile(file, "rw");
			for (int i = 0; i < this.virtualMemorySize / 4; i++) {
				disk.writeInt(i);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			disk.close();
		}
	}
	
	public void showResults() {
		String  results = "";
		for (Object process : this.terminatedBuffer) {
			PCB pcb = (PCB) process;
			results += "process ID: ";
			results += pcb.getProcessID();
			results += "\n\n";
			results += pcb.getResult();
			results += "\n";
		}
		this.view.resultsMenu(results);
	}
	
	public void showPSAnalysis() {
		XYChart.Series series = this.cpu.getSeries(); 
		int totalTime = (int) (LocalTime.now().toNanoOfDay() / Math.pow(10, 6)) - this.initialTime;
		double CPUUtilization = (this.cpu.getTotalRunningTime() / totalTime) * 100;
		double throughput = this.cpu.getThroughput();
		int totalTurnaroundTime = 0;
		int totalWaitingTime = 0;
		int totalResponseTime = 0;
		for (Object process : this.terminatedBuffer) {
			PCB pcb = (PCB) process;
			totalTurnaroundTime += pcb.getTurnaroundTime();
			totalWaitingTime += pcb.getWaitingTime();
			totalResponseTime += pcb.getResponseTime();
		}
		int averageTurnaroundTime = 0;
		int averageWaitingTime = 0;
		int averageResponseTime = 0;
		try {
			averageTurnaroundTime = totalTurnaroundTime / this.terminatedBuffer.size();
		}
		catch(ArithmeticException e) {
		}
		try {
			averageWaitingTime = totalWaitingTime / this.terminatedBuffer.size();
		}
		catch(ArithmeticException e) {
		}
		try {
			averageResponseTime = totalResponseTime / this.terminatedBuffer.size();
		}
		catch(ArithmeticException e) {
		}
		this.view.processSchedulerAnalysisMenu(series, CPUUtilization, throughput, averageTurnaroundTime,
				averageWaitingTime, averageResponseTime);
	}
	
	public void showMMUAnalysis() {
		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
				new PieChart.Data("memory used", this.cpu.getVirtualAddress()),
				new PieChart.Data("memory remaining", this.cpu.getRemainingMemory()));
		int numberOfTranslatedAddresses = this.vm.getNumberOfTranslatedAddresses();
		int pageFaults = this.vm.getPageFaults();
		double pageFaultRate = this.vm.getPageFaultRate();
		int TLBHits = this.vm.getTLBHits();
		double TLBHitRate = this.vm.getTLBHitRate();
		this.view.MMUAnalysisMenu(pieChartData, numberOfTranslatedAddresses, pageFaults, pageFaultRate,
				TLBHits, TLBHitRate);
	}
	
	public void showFSAnalysis() {
		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
				new PieChart.Data("used files", this.fileSystem.getUsedFiles()),
				new PieChart.Data("remaining files", this.fileSystem.getRemainingFiles()));
		int seekDistance = this.cpu.getHeadMovement();
		this.view.FSAnalysisMenu(pieChartData, seekDistance);
	}
	
	public void showIOAnalysis() {
		this.io.generateStatistics();
		XYChart.Series writeSeries = this.io.getWriteSeries();
		XYChart.Series readSeries = this.io.getReadSeries();
		double averageWriteTime = this.io.getAverageWriteTime();
		double averageReadTime = this.io.getAverageReadTime();
		this.view.IOAnalysisMenu(writeSeries, readSeries, averageWriteTime, averageReadTime);
	}
	
}
