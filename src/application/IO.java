package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javafx.scene.chart.XYChart;

public class IO extends Thread implements IOSystem {
	
	private ProcessCreation processCreation = null;
	private CPU cpu = null;
	private ArrayList<MemoryMappedFile> peripherals = new ArrayList<MemoryMappedFile>();
	private Hashtable vars = new Hashtable();
	private int totalWriteTime = 0;
	private int totalReadTime = 0;
	private int numberOfWriteOperations = 0;
	private int numberOfReadOperations = 0;
	private double averageWriteTime = 0;
	private double averageReadTime = 0;
	private XYChart.Series writeSeries = null;
	private XYChart.Series readSeries = null;
	
	public IO(ProcessCreation processCreation, CPU cpu) {
		super();
		this.processCreation = processCreation;
		this.cpu = cpu;
		this.writeSeries = new XYChart.Series();
		this.writeSeries.setName("write");
		this.readSeries = new XYChart.Series();
		this.readSeries.setName("read");
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				PCB process = this.cpu.getProcessFromIOBuffer();
				if (process == null) {
					continue;
				}
				String result = "";
				List<String> command = process.getIOCommand();
				String target = process.getTarget();
				switch (command.get(0)) {
				case "addDevice":
					result = Integer.toString(this.addDevice(command.get(1), command.get(2)));
					break;
				case "input":
					Byte[] bytesBuffer = this.read(process, command.get(1), Integer.parseInt(command.get(2)), Integer.parseInt(command.get(3)));
					if (bytesBuffer != null) {
                    	result = Integer.toString(bytesBuffer.length);
                    }
                    else {
                    	result = "-1";
                    }
					break;
				case "output":
					ArrayList<Byte> dataArray = new ArrayList<Byte>();
					for (int i = 3; i < command.size(); i++) {
						String text = command.get(i);
						byte[] bytes = text.getBytes();
						for (int c = 0; c < text.length(); c++) {
							dataArray.add(bytes[c]);
						}
						dataArray.add((byte) ' ');
					}
					Byte[] bytesArray = dataArray.toArray(new Byte[0]); 
					result = Integer.toString(this.write(process, command.get(1), Integer.parseInt(command.get(2)), bytesArray));
					break;
				case "removeDevice":
					result = Integer.toString(this.removeDevice(command.get(1)));
					break;
				}
				if (target == null) {
		    		result = process.getResult() + result + "\n";
		    	}
		    	else {
		    		this.vars.put(target, new Integer(result));
		    		result = process.getResult() + target + " = " + result + "\n";
		    	}
		    	process.setTarget(null);
		    	process.setResult(result);
		    	this.processCreation.putProcess(process);
			}
		}
		catch (InterruptedException e) {
		}
	}
	
	public Hashtable getVars() {
		return this.vars;
	}

	@Override
	public int addDevice(String deviceName, String BufferFileName) {
		this.peripherals.add(new MemoryMappedFile(deviceName, BufferFileName));
    	return 0;    	
	}

	@Override
	public int write(PCB process, String deviceName, int position, Byte[] bytesBuffer) {
		int count = 0;
		for (int i = 0; i < this.peripherals.size(); i++) {
			if (this.peripherals.get(i).getDeviceName().equals(deviceName)) {
				int initialTime = (int) (LocalTime.now().toNanoOfDay() / Math.pow(10, 6));
				count = this.peripherals.get(i).write(process, position, bytesBuffer.length, bytesBuffer);
				this.totalWriteTime += ((LocalTime.now().toNanoOfDay() / Math.pow(10, 6))
						- initialTime);
				this.numberOfWriteOperations ++;
			}
		}
		return count;
	}

	@Override
	public Byte[] read(PCB process, String deviceName, int position, int size) {
		for (int i = 0; i < this.peripherals.size(); i++) {
			if (this.peripherals.get(i).getDeviceName().equals(deviceName)) {
				int initialTime = (int) (LocalTime.now().toNanoOfDay() / Math.pow(10, 6));
				Byte[] bytesBuffer = this.peripherals.get(i).read(process, position, size);
				this.totalReadTime += ((LocalTime.now().toNanoOfDay() / Math.pow(10, 6))
						- initialTime);
				this.numberOfReadOperations ++;
				return bytesBuffer;
			}
		}
		return null;
	}

	@Override
	public int removeDevice(String deviceName) {
		for (int i = 0; i < this.peripherals.size(); i++) {
			if (this.peripherals.get(i).getDeviceName().equals(deviceName)) {
				this.peripherals.remove(i);
				return 0;
			}
		}
		return -1;
	}
	
	public String getVariables() {
		String result = "";
		for (Enumeration e = this.vars.keys(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            Object val = this.vars.get(key);
            result += key + " = " + val + "\n";
        }
		return result;
	}
	
	public void generateStatistics() {
		if (this.numberOfWriteOperations != 0 && this.numberOfReadOperations != 0) {
			this.averageWriteTime = this.totalWriteTime / this.numberOfWriteOperations;
			this.averageReadTime = this.totalReadTime / this.numberOfReadOperations;
		}
		for (MemoryMappedFile peripheral : this.peripherals) {
			this.writeSeries.getData().add(new XYChart.Data(peripheral.getDeviceName(),
					peripheral.getNumberOfWriteOperations()));
			this.readSeries.getData().add(new XYChart.Data(peripheral.getDeviceName(),
					peripheral.getNumberOfReadOperations()));
		}
	}
	
	public double getAverageWriteTime() {
		return this.averageWriteTime;
	}
	
	public double getAverageReadTime() {
		return this.averageReadTime;
	}
	
	public XYChart.Series getWriteSeries() {
		return this.writeSeries;
	}
	
	public XYChart.Series getReadSeries() {
		return this.readSeries;
	}
	
}
