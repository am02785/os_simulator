package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import javafx.scene.chart.XYChart;

public class CPU extends Thread {
	
	private ProcessCreation processCreation = null;
	private Dispatcher dispatcher = null;
	private Vector terminatedBuffer = null;
	private Vector VMBuffer = null;
	private Vector fileSystemBuffer = null;
	private Vector IOBuffer = null;
	private int sizeOfVMBuffer = 0;
	private int sizeOfFileSystemBuffer = 0;
	private int sizeOfIOBuffer = 0;
	private int timeQuantum = 0;
	private String directory = null;
	private List<String> history = null;
	private VM vm = null;
	private int virtualAddress = 0;
	private int virtualMemorySize = 0;
	private Bank bank = null;
	private JavaFileSystem fileSystem = null;
	private IO io = null;
    private int headMovement = 0;
    private int cylinderPosition = 0;
	private double throughput = 0;
	private double totalRunningTime = 0;
	XYChart.Series series = null;
	
	public CPU(ProcessCreation processCreation, Dispatcher dispatcher,
			Vector terminatedBuffer, Vector VMBuffer,
			Vector fileSystemBuffer, Vector IOBuffer,
			int sizeOfVMBuffer, int sizeOfFileSystemBuffer,
			int sizeOfIOBuffer, int timeQuantum,
			VM vm, int virtualMemorySize,
			Bank bank, JavaFileSystem fileSystem) {
		super();
		this.processCreation = processCreation;
		this.dispatcher = dispatcher;
		this.terminatedBuffer = terminatedBuffer;
		this.VMBuffer = VMBuffer;
		this.fileSystemBuffer = fileSystemBuffer;
		this.IOBuffer = IOBuffer;
		this.sizeOfVMBuffer = sizeOfVMBuffer;
		this.sizeOfFileSystemBuffer = sizeOfFileSystemBuffer;
		this.sizeOfIOBuffer = sizeOfIOBuffer;
		this.timeQuantum = timeQuantum;
		this.directory = System.getProperty("user.dir");
		this.history = new ArrayList<>();
		this.vm = vm;
		this.virtualMemorySize = virtualMemorySize;
		this.bank = bank;
		this.fileSystem = fileSystem;
		this.series = new XYChart.Series();
		this.series.getData().add(new XYChart.Data(0.0, 0.0));
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				PCB pcb = this.dispatcher.getProcess();
			//	System.out.println(pcb.getInstructionNumber());
			//	System.out.println(pcb.getCommands().get(pcb.getInstructionNumber()));
				LocalTime initialTime = LocalTime.now();
				for (int i = pcb.getInstructionNumber(); i < pcb.getCommands().size(); i++) {
					pcb.addWaitingTime((int) (((initialTime.toNanoOfDay() / Math.pow(10, 6))
							- this.processCreation.getInitialTime()) - pcb.getTimeAddedToReadyBuffer()));
					if (i == 0) {
						pcb.setResponseTime((int) (((initialTime.toNanoOfDay() / Math.pow(10, 6))
								- this.processCreation.getInitialTime()) - pcb.getArrivalTime()));
						if (pcb.getMemoryRequired() > 0) {
							int[] maxDemand = new int[1];
							maxDemand[0] = pcb.getMemoryRequired();
							this.bank.addCustomer(pcb, maxDemand);
						}
					}
					String commandLine = pcb.getCommands().get(i);
					List<String> command = new ArrayList<>();
					for (String token : commandLine.split(" ")) {
						command.add(token);
					}
					ProcessBuilder pb = new ProcessBuilder(command);
					pb.directory(new File(this.directory));
					if (command.get(0).startsWith("!")) {
						if (command.size() > 1) {
							String result = pcb.getResult();
							result += "too many arguments\n";
							pcb.setResult(result);
							pcb.incrementInstrunctionNumber();
							continue;
						}
						else if (command.get(0).substring(1).equals("!")) {
							try {
								commandLine = this.history.get(this.history.size() - 1);
								command = new ArrayList<>();
								for (String token : commandLine.split(" ")) {
									command.add(token);
								}
								pb = new ProcessBuilder(command);
								pb.directory(new File(this.directory));
								pcb.setCommand(commandLine);
							}
							catch (IndexOutOfBoundsException e) {
								String result = pcb.getResult();
								result += "there is no previous command entered by the user\n";
								pcb.setResult(result);
								pcb.incrementInstrunctionNumber();
								continue;
							}
						}
						else {
							try {
								int commandNumber = Integer.parseInt(command.get(0).substring(1)); 
								commandLine = this.history.get(commandNumber);
								command = new ArrayList<>();
								for (String token : commandLine.split(" ")) {
									command.add(token);
								}
								pb = new ProcessBuilder(command);
								pb.directory(new File(this.directory));
								pcb.setCommand(commandLine);
							}
							catch (NumberFormatException e) {
								String result = pcb.getResult();
								result += "the command number enetered is not an integer\n";
								pcb.setResult(result);
								pcb.incrementInstrunctionNumber();
								continue;
							}
							catch (IndexOutOfBoundsException e) {
								String result = pcb.getResult();
								result += "the command number is not a valid number in the command history\n";
								pcb.setResult(result);
								pcb.incrementInstrunctionNumber();
								continue;
							}
						}
					}
					if (command.get(0).equals("cd")) {
						if (command.size() == 1) {
							this.directory = System.getProperty("user.dir");
						}
						else if (command.size() == 2) {
							while (command.get(1).startsWith("\\") || command.get(1).startsWith("/")) {
								command.set(1, command.get(1).substring(1));
							}
							if (new File(this.directory + "\\" + command.get(1)).exists()) {
								this.directory = this.directory + "\\" + command.get(1);
							}
							else {
								String result = pcb.getResult();
								result += "the directory " + this.directory + "\\" + command.get(1) + " does not exist\n";
								pcb.setResult(result);
							}
						}
						else {
							String result = pcb.getResult();
							result += "too many arguments\n";
							pcb.setResult(result);
						}
						this.history.add(commandLine);
					}
					else if (command.get(0).equals("history")) {
						if (command.size() > 1) {
							String result = pcb.getResult();
							result += "too many arguments\n";
							pcb.setResult(result);
						}
						else {
							String result = pcb.getResult();
							for (int j = 0; j < this.history.size(); j++) {
								result += j + " " + this.history.get(j) + "\n";
							}
							pcb.setResult(result);
						}
					}
					else if (command.get(0).equals("allocateMemory")) {
						if (this.virtualAddress == (this.virtualMemorySize)) {
							String result = pcb.getResult();
							result += "Virtual Memory is full\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else {
							int[] request = new int[1];
							request[0] = 1;
							if (this.bank.requestResources(pcb, pcb.getBankProcessNum(), request)) {
								String result = pcb.getResult();
								result += "Approved\n";
								pcb.setResult(result);
								pcb.incrementInstrunctionNumber();
								pcb.setVirtualAddress(this.virtualAddress);
								pcb.setVMInstruction("allocateMemory");
								this.putProcessInVMBuffer(pcb);
								this.virtualAddress ++;
								this.history.add(commandLine);
								this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
								break;
							}
							else {
								String result = pcb.getResult();
								result += "Denied";
								pcb.setResult(result);
								this.history.add(commandLine);
							}
						}
					}
					else if (command.get(0).equals("deallocateMemory")) {
						if (this.virtualAddress == 0) {
							String result = pcb.getResult();
							result += "VirtualMemory is empty\n";
							pcb.setResult(result);
						}
						else {
							this.virtualAddress --;
							pcb.setVirtualAddress(this.virtualAddress);
							pcb.setVMInstruction("deallocateMemory");
							this.putProcessInVMBuffer(pcb);
						}
						this.history.add(commandLine);
					}
					else if (command.get(0).equals("getState")) {
						this.bank.getState(pcb);
						this.history.add(commandLine);
					}
					else if (command.get(0).contains("write") || (command.size() > 2 && command.get(2).contains("write"))
							|| command.get(0).contains("seek") || (command.size() > 2 && command.get(2).contains("seek"))) {
						if (!(command.size() == 4 || command.size() == 6)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else if (command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							String arg1 = null;
							String arg2 = null;
							String arg3 = null;
							if (this.fileSystem.getVars().get(command.get(3)) != null) {
								arg1 =  this.fileSystem.getVars().get(command.get(3)).toString();
							}
							else {
								arg1 = command.get(3);
							}
							if (this.fileSystem.getVars().get(command.get(4)) != null) {
								arg2 =  this.fileSystem.getVars().get(command.get(4)).toString();
							}
							else {
								arg2 = command.get(4);
							}
							if (this.fileSystem.getVars().get(command.get(5)) != null) {
								arg3 =  this.fileSystem.getVars().get(command.get(5)).toString();
							}
							else {
								arg3 = command.get(5);
							}
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							fileSystemCommand.add(arg1);
							fileSystemCommand.add(arg2);
							fileSystemCommand.add(arg3);
							if (command.get(0).contains("write") || (command.size() > 2 && command.get(2).contains("write"))) {
								pcb.setFileSystemCommand(fileSystemCommand);
								pcb.setTarget(target);
								this.putProcessInFileSystemBuffer(pcb);
							}
							else {
								this.fileSystem.runCommand(pcb, fileSystemCommand, target);
							}
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							String arg1 = null;
							String arg2 = null;
							String arg3 = null;
							if (this.fileSystem.getVars().get(command.get(1)) != null) {
								arg1 =  this.fileSystem.getVars().get(command.get(1)).toString();
							}
							else {
								arg1 = command.get(1);
							}
							if (this.fileSystem.getVars().get(command.get(2)) != null) {
								arg2 =  this.fileSystem.getVars().get(command.get(2)).toString();
							}
							else {
								arg2 = command.get(2);
							}
							if (this.fileSystem.getVars().get(command.get(3)) != null) {
								arg3 =  this.fileSystem.getVars().get(command.get(3)).toString();
							}
							else {
								arg3 = command.get(3);
							}
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							fileSystemCommand.add(arg1);
							fileSystemCommand.add(arg2);
							fileSystemCommand.add(arg3);
							if (command.get(0).contains("write") || (command.size() > 2 && command.get(2).contains("write"))) {
								pcb.setFileSystemCommand(fileSystemCommand);
								this.putProcessInFileSystemBuffer(pcb);
							}
							else {
								this.fileSystem.runCommand(pcb, fileSystemCommand, null);
							}
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).contains("formatDisk") || (command.size() > 2 && command.get(2).contains("formatDisk"))
							|| command.get(0).contains("read") || (command.size() > 2 && command.get(2).contains("read"))) {
						if (!(command.size() == 3 || command.size() == 5)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else if (command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							String arg1 = null;
							String arg2 = null;
							if (this.fileSystem.getVars().get(command.get(3)) != null) {
								arg1 =  this.fileSystem.getVars().get(command.get(3)).toString();
							}
							else {
								arg1 = command.get(3);
							}
							if (this.fileSystem.getVars().get(command.get(4)) != null) {
								arg2 =  this.fileSystem.getVars().get(command.get(4)).toString();
							}
							else {
								arg2 = command.get(4);
							}
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							fileSystemCommand.add(arg1);
							fileSystemCommand.add(arg2);
							if (command.get(0).contains("read") || (command.size() > 2 && command.get(2).contains("read"))) {
								pcb.setFileSystemCommand(fileSystemCommand);
								pcb.setTarget(target);
								this.putProcessInFileSystemBuffer(pcb);
							}
							else {
								this.fileSystem.runCommand(pcb, fileSystemCommand, target);
							}
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							String arg1 = null;
							String arg2 = null;	
							if (this.fileSystem.getVars().get(command.get(1)) != null) {
								arg1 =  this.fileSystem.getVars().get(command.get(1)).toString();
							}
							else {
								arg1 = command.get(1);
							}
							if (this.fileSystem.getVars().get(command.get(2)) != null) {
								arg2 =  this.fileSystem.getVars().get(command.get(2)).toString();
							}
							else {
								arg2 = command.get(2);
							}
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							fileSystemCommand.add(arg1);
							fileSystemCommand.add(arg2);
							if (command.get(0).contains("read") || (command.size() > 2 && command.get(2).contains("read"))) {
								pcb.setFileSystemCommand(fileSystemCommand);
								this.putProcessInFileSystemBuffer(pcb);
							}
							else {
								this.fileSystem.runCommand(pcb, fileSystemCommand, null);
							}
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).contains("open") || (command.size() > 2 && command.get(2).contains("open"))
							|| command.get(0).contains("inumber") || (command.size() > 2 && command.get(2).contains("inumber"))
							|| command.get(0).contains("close") || (command.size() > 2 && command.get(2).contains("close"))
							|| command.get(0).contains("delete") || (command.size() > 2 && command.get(2).contains("delete"))) {
						if (!(command.size() == 2 || command.size() == 4)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else if (command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							String arg1 = null;
							if (this.fileSystem.getVars().get(command.get(3)) != null) {
								arg1 =  this.fileSystem.getVars().get(command.get(3)).toString();
							}
							else {
								arg1 = command.get(3);
							}
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							fileSystemCommand.add(arg1);
							this.fileSystem.runCommand(pcb, fileSystemCommand, target);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							String arg1 = null;
							if (this.fileSystem.getVars().get(command.get(1)) != null) {
								arg1 =  this.fileSystem.getVars().get(command.get(1)).toString();
							}
							else {
								arg1 = command.get(1);
							}
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							fileSystemCommand.add(arg1);
							this.fileSystem.runCommand(pcb, fileSystemCommand, null);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).contains("shutdown") || (command.size() > 2 && command.get(2).contains("shutdown"))
							|| command.get(0).contains("create") || (command.size() > 2 && command.get(2).contains("create"))) {
						if (!(command.size() == 1 || command.size() == 3)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else if (command.size() > 1 && command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							this.fileSystem.runCommand(pcb, fileSystemCommand, target);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							List<String> fileSystemCommand = new ArrayList<>();
							fileSystemCommand.add(line);
							this.fileSystem.runCommand(pcb, fileSystemCommand, null);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).equals("fileSystemVars")) {
						pcb.incrementInstrunctionNumber();
						String line = command.get(0);
						List<String> fileSystemCommand = new ArrayList<>();
						fileSystemCommand.add(line);
						this.fileSystem.runCommand(pcb, fileSystemCommand, null);
						this.history.add(commandLine);
						this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
						break;
					}
					else if (command.get(0).contains("addDevice") || (command.size() > 2 && command.get(2).contains("addDevice"))) {
						if (!(command.size() == 3 || command.size() == 5)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else if (command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							String arg1 = null;
							String arg2 = null;
							if (this.io.getVars().get(command.get(3)) != null) {
								arg1 =  this.io.getVars().get(command.get(3)).toString();
							}
							else {
								arg1 = command.get(3);
							}
							if (this.io.getVars().get(command.get(4)) != null) {
								arg2 =  this.io.getVars().get(command.get(4)).toString();
							}
							else {
								arg2 = command.get(4);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							IOCommand.add(arg2);
							pcb.setIOCommand(IOCommand);
							pcb.setTarget(target);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							String arg1 = null;
							String arg2 = null;	
							if (this.io.getVars().get(command.get(1)) != null) {
								arg1 =  this.io.getVars().get(command.get(1)).toString();
							}
							else {
								arg1 = command.get(1);
							}
							if (this.io.getVars().get(command.get(2)) != null) {
								arg2 =  this.io.getVars().get(command.get(2)).toString();
							}
							else {
								arg2 = command.get(2);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							IOCommand.add(arg2);
							pcb.setIOCommand(IOCommand);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).contains("input") || (command.size() > 2 && command.get(2).contains("input"))) {
						if (!(command.size() == 4 || command.size() == 6)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else if (command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							String arg1 = null;
							String arg2 = null;
							String arg3 = null;
							if (this.io.getVars().get(command.get(3)) != null) {
								arg1 =  this.io.getVars().get(command.get(3)).toString();
							}
							else {
								arg1 = command.get(3);
							}
							if (this.io.getVars().get(command.get(4)) != null) {
								arg2 =  this.io.getVars().get(command.get(4)).toString();
							}
							else {
								arg2 = command.get(4);
							}
							if (this.io.getVars().get(command.get(5)) != null) {
								arg3 =  this.io.getVars().get(command.get(5)).toString();
							}
							else {
								arg3 = command.get(5);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							IOCommand.add(arg2);
							IOCommand.add(arg3);
							pcb.setIOCommand(IOCommand);
							pcb.setTarget(target);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							String arg1 = null;
							String arg2 = null;
							String arg3 = null;
							if (this.io.getVars().get(command.get(1)) != null) {
								arg1 =  this.io.getVars().get(command.get(1)).toString();
							}
							else {
								arg1 = command.get(1);
							}
							if (this.io.getVars().get(command.get(2)) != null) {
								arg2 =  this.io.getVars().get(command.get(2)).toString();
							}
							else {
								arg2 = command.get(2);
							}
							if (this.io.getVars().get(command.get(3)) != null) {
								arg3 =  this.io.getVars().get(command.get(3)).toString();
							}
							else {
								arg3 = command.get(3);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							IOCommand.add(arg2);
							IOCommand.add(arg3);
							pcb.setIOCommand(IOCommand);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).contains("output") || (command.size() > 2 && command.get(2).contains("output"))) {
						if ((command.size() < 4 && !command.contains("="))
								|| (command.size() < 6)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						if (command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							String arg1 = null;
							String arg2 = null;
							if (this.io.getVars().get(command.get(3)) != null) {
								arg1 =  this.io.getVars().get(command.get(3)).toString();
							}
							else {
								arg1 = command.get(3);
							}
							if (this.io.getVars().get(command.get(4)) != null) {
								arg2 =  this.io.getVars().get(command.get(4)).toString();
							}
							else {
								arg2 = command.get(4);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							IOCommand.add(arg2);
							for (int j = 5; j < command.size(); j++) {
								IOCommand.add(command.get(j));
							}
							pcb.setIOCommand(IOCommand);
							pcb.setTarget(target);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							String arg1 = null;
							String arg2 = null;
							if (this.io.getVars().get(command.get(1)) != null) {
								arg1 =  this.io.getVars().get(command.get(1)).toString();
							}
							else {
								arg1 = command.get(1);
							}
							if (this.io.getVars().get(command.get(2)) != null) {
								arg2 =  this.io.getVars().get(command.get(2)).toString();
							}
							else {
								arg2 = command.get(2);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							IOCommand.add(arg2);
							for (int j = 3; j < command.size(); j++) {
								IOCommand.add(command.get(j));
							}
							pcb.setIOCommand(IOCommand);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).contains("removeDevice") || (command.size() > 2 && command.get(2).contains("removeDevice"))) {
						if (!(command.size() == 2 || command.size() == 4)) {
							String result = pcb.getResult();
							result += "invalid number of arguments\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						else if (command.get(1).equals("=")) {
							pcb.incrementInstrunctionNumber();
							String target = command.get(0);
							String line = command.get(2);
							String arg1 = null;
							if (this.io.getVars().get(command.get(3)) != null) {
								arg1 =  this.io.getVars().get(command.get(3)).toString();
							}
							else {
								arg1 = command.get(3);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							pcb.setIOCommand(IOCommand);
							pcb.setTarget(target);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
						else {
							pcb.incrementInstrunctionNumber();
							String line = command.get(0);
							String arg1 = null;
							if (this.io.getVars().get(command.get(1)) != null) {
								arg1 =  this.io.getVars().get(command.get(1)).toString();
							}
							else {
								arg1 = command.get(1);
							}
							List<String> IOCommand = new ArrayList<>();
							IOCommand.add(line);
							IOCommand.add(arg1);
							pcb.setIOCommand(IOCommand);
							this.putProcessInIOBuffer(pcb);
							this.history.add(commandLine);
							this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							break;
						}
					}
					else if (command.get(0).equals("IOVars")) {
						String result = pcb.getResult();
						result += this.io.getVariables();
						result += "\n";
						pcb.setResult(result);
						this.history.add(commandLine);
					}
					else if (command.get(0).equals("exit")) {
						this.putProcessInTerminatedBuffer(pcb);
						int currentTime = (int) (LocalTime.now().toNanoOfDay() / Math.pow(10, 6));
						double totalTime = 0;
						totalTime += currentTime - this.processCreation.getInitialTime();
						this.throughput = this.terminatedBuffer.size() / totalTime;
						pcb.setTurnaroundTime((int) totalTime - pcb.getArrivalTime());
						this.totalRunningTime = currentTime - (initialTime.toNanoOfDay() / Math.pow(10, 6));
						this.series.getData().add(new XYChart.Data(totalTime, this.terminatedBuffer.size()));
					}
					else if (commandLine.length() > 5 && commandLine.substring(0, 6).equals("print(") && commandLine.endsWith(")")) {
						try {
							String result = pcb.getResult();
							result += pcb.getEngine().eval(commandLine.substring(6, commandLine.length() - 1));
							result += "\n";
							pcb.setResult(result);
							this.history.add(commandLine);
						}
						catch (ScriptException e) {
							e.printStackTrace();
						}
					}
					else {
						try {
							if (commandLine.contains("print")) {
								throw new ScriptException("");
							}
							pcb.getEngine().eval(commandLine);
							this.history.add(commandLine);
						}
						catch(ScriptException e) {
							//this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
							//this.putProcessInIOBuffer(pcb);
							//break;
							try {
								Process process = pb.start();
								InputStream is = process.getInputStream();
								InputStreamReader isr = new InputStreamReader(is);
								BufferedReader br = new BufferedReader(isr);
								String line;
								String result = pcb.getResult();
								while ( (line = br.readLine()) != null) {
									result += line;
									result += "\n";
								}
								pcb.setResult(result);
								br.close();
							}
							catch (IOException IOexception) {
								String result = pcb.getResult();
								result = result + "\'" + command.get(0) + "\' is not recognised as an internal or external command,";
								result += "\noperative program or batch file\n";
								pcb.setResult(result);
							}
							finally {
								this.history.add(commandLine);
							}
						}
					}
					int elapsedTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
					this.totalRunningTime = (int) ((LocalTime.now().toNanoOfDay() - initialTime.toNanoOfDay()) / Math.pow(10, 6));
					if (elapsedTime > this.timeQuantum && !(this.terminatedBuffer.contains(pcb) || this.IOBuffer.contains(pcb))) {
						pcb.incrementInstrunctionNumber();
						this.processCreation.putProcess(pcb);
						break;
					}
					else {
						pcb.incrementInstrunctionNumber();
					}
				}
			}
		}
		catch (InterruptedException e) {
		}
	}
	
	private synchronized void putProcessInTerminatedBuffer(PCB process) throws InterruptedException {
		this.terminatedBuffer.addElement(process);
		notify();
	}
	
	private synchronized void putProcessInVMBuffer(PCB process) throws InterruptedException {
		while (this.VMBuffer.size() == this.sizeOfVMBuffer) {
			wait();
		}
		this.VMBuffer.addElement(process);
		notify();
	}
	
	public synchronized PCB getProcessFromVMBuffer() throws InterruptedException {
		notify();
		while (this.VMBuffer.size() == 0) {
			wait();
		}
		PCB pcb = (PCB) this.VMBuffer.remove(0);
		return pcb;
	}
	
	private synchronized void putProcessInFileSystemBuffer(PCB process) throws InterruptedException {
		while (this.fileSystemBuffer.size() == this.sizeOfFileSystemBuffer) {
			wait();
		}
		this.fileSystemBuffer.addElement(process);
		notify();
	}
	
	public synchronized PCB fcfsDiskSchedulingAlgorithm() throws InterruptedException {
		notify();
		if (this.fileSystemBuffer.size() == 0) {
			return null;
		}
		PCB pcb = (PCB) this.fileSystemBuffer.remove(0);
		int fd = Integer.parseInt(pcb.getFileSystemCommand().get(1));
		this.headMovement += Math.abs(this.fileSystem.getFileTable().getSeekPointer(fd) - this.cylinderPosition);
		this.cylinderPosition = this.fileSystem.getFileTable().getSeekPointer(fd);
		return pcb;
	}
	
	public synchronized PCB sstfDiskSchedulingAlgorithm() throws InterruptedException {
		notify();
		if (this.fileSystemBuffer.size() == 0) {
			return null;
		}
		PCB pcb = null;
		int shortestHeadMovement = Integer.MAX_VALUE;
		for (Object object : this.fileSystemBuffer) {
			PCB process = (PCB) object;
			int fd = Integer.parseInt(process.getFileSystemCommand().get(1));
			if (Math.abs(this.fileSystem.getFileTable().getSeekPointer(fd) - this.cylinderPosition) < shortestHeadMovement) {
				pcb = process;
				shortestHeadMovement = Math.abs(this.fileSystem.getFileTable().getSeekPointer(fd) - this.cylinderPosition);
			}
		}
		this.fileSystemBuffer.remove(pcb);
		int fd = Integer.parseInt(pcb.getFileSystemCommand().get(1));
		this.headMovement += shortestHeadMovement;
		this.cylinderPosition = this.fileSystem.getFileTable().getSeekPointer(fd);
		return pcb;
	}
	
	private synchronized void putProcessInIOBuffer(PCB process) throws InterruptedException {
		if (this.IOBuffer.size() == this.sizeOfIOBuffer) {
			wait();
		}
		this.IOBuffer.addElement(process);
		notify();
	}
	
	public synchronized PCB getProcessFromIOBuffer() throws InterruptedException {
		notify();
		while (this.IOBuffer.size() == 0) {
			return null;
		}
		PCB pcb = (PCB) this.IOBuffer.remove(0);
		return pcb;
	}
	
	public String getDirectory() {
		return this.directory;
	}
	
	public void addToHistory(String commandLine) {
		this.history.add(commandLine);
	}
	
	public void setVM(VM vm) {
		this.vm = vm;
	}
	
	public int getVirtualAddress() {
		return this.virtualAddress;
	}
	
	public int getRemainingMemory() {
		return this.virtualMemorySize - this.virtualAddress;
	}
	
	public void setFileSystem(JavaFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
	
	public void setIO(IO io) {
		this.io = io;
	}
	
	public int getHeadMovement() {
		return this.headMovement;
	}
	
	public double getThroughput() {
		return this.throughput;
	}
	
	public double getTotalRunningTime() {
		return this.totalRunningTime;
	}
	
	public XYChart.Series getSeries() {
		return this.series;
	}

}
