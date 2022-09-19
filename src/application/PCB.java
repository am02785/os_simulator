package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class PCB {
	
	private int processID = 0;
	private String instructions = null;
	private List<String> commands = null;
	private int instructionNumber = 0;
	private int arrivalTime = 0;
	private int timeAddedToReadyBuffer = 0;
	private int waitingTime = 0;
	private int responseTime = 0;
	private int turnaroundTime = 0;
	private int CPUBurst = 0;
	private int priority = 0;
	private int memoryRequired = 0;
	private int bankProcessNum = 0;
	private ScriptEngine engine = null;
	private int virtualAddress = 0;
	private String VMInstruction = null;
	private List<String> fileSystemCommand = null;
	private List<String> IOCommand = null;
	private String target = null;
	private String result = null;
	
	
	public PCB(int processID, String instructions, int CPUBurst, int priority, int memoryRequired) {
		super();
		this.processID = processID;
		this.instructions = instructions;
		this.commands = new ArrayList<>();
		this.CPUBurst = CPUBurst;
		this.priority = priority;
		this.memoryRequired = memoryRequired;
		ScriptEngineManager manager = new ScriptEngineManager();
		this.engine = manager.getEngineByName("js");
		this.result = "";
	}

	public int getProcessID() {
		return this.processID;
	}
	
	public String getInstructions() {
		return this.instructions;
	}
	
	public List<String> getCommands() {
		return this.commands;
	}
	
	public int getInstructionNumber() {
		return this.instructionNumber;
	}
	
	public int getArrivalTime() {
		return this.arrivalTime;
	}
	
	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	public int getTimeAddedToReadyBuffer() {
		return this.timeAddedToReadyBuffer;
	}
	
	public void setTimeAddedToReadyBuffer(int timeAddedToReadyBuffer) {
		this.timeAddedToReadyBuffer = timeAddedToReadyBuffer;
	}
	
	public int getWaitingTime() {
		return this.waitingTime;
	}
	
	public void addWaitingTime(int waitingTime) {
		this.waitingTime += waitingTime;
	}
	
	public int getResponseTime() {
		return this.responseTime;
	}
	
	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}
	
	public int getTurnaroundTime() {
		return this.turnaroundTime;
	}
	
	public void setTurnaroundTime(int turnaroundTime) {
		this.turnaroundTime = turnaroundTime;
	}
		
	public int getCPUBurst() {
		return this.CPUBurst;
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	public int getMemoryRequired() {
		return this.memoryRequired;
	}
	
	public void setBankProcessNum(int bankProcessNum) {
		this.bankProcessNum = bankProcessNum;
	}
	
	public int getBankProcessNum() {
		return this.bankProcessNum;
	}
	
	public ScriptEngine getEngine() {
		return this.engine;
	}
	
	public String getResult() {
		return this.result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public void addCommand(String command) {
		this.commands.add(command);
	}
	
	public void setCommand(String command) {
		this.commands.set(this.instructionNumber, command);
	}
	
	public int getVirtualAddress() {
		return this.virtualAddress;
	}
	
	public void setVirtualAddress(int virtualAddress) {
		this.virtualAddress = virtualAddress;
	}
	
	public String getVMInstruction() {
		return this.VMInstruction;
	}
	
	public void setVMInstruction(String VMInstruction) {
		this.VMInstruction = VMInstruction;
	}
	
	public List<String> getFileSystemCommand() {
		return this.fileSystemCommand;
	}
	
	public void setFileSystemCommand(List<String> fileSystemCommand) {
		this.fileSystemCommand = fileSystemCommand;
	}
	
    public List<String> getIOCommand() {
    	return this.IOCommand;
    }
    
    public void setIOCommand(List<String> IOCommand) {
    	this.IOCommand = IOCommand;
    }
	
	public String getTarget() {
		return this.target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public void incrementInstrunctionNumber() {
		this.instructionNumber++;
	}
	
}
