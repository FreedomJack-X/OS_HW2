package com.os.process;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class SystemSim {
	private Process processes[];  		// List of processes
	private Queue<Process> queue; 		// queue of processes
	private int numCores;		  		// number of cores
	private int coreToProcessID[]; 		//each core runs a different process
	private int timeFinish[];	  	 	// check when each process finishes; if finished, then context switch
	private int timeEnterReady[]; 		//time when the process enters the ready queue; used to calculate turnaround time
	private int numContextSwitch[];		//amount of context switches per process
	private int timeSwitch;
		
	public SystemSim(float probabilityInteractive, int numCores, int numProcess, int timeSwitch){
		Random randObj = new Random();
		processes = new Process[numProcess];
		this.numCores = numCores;
		timeFinish = new int[processes.length];
		timeEnterReady = new int[processes.length];
		numContextSwitch = new int[processes.length];
		this.timeSwitch = timeSwitch;
		for (int i = 0; i < processes.length; i++)
		{
			//determine process type
			float probability = randObj.nextFloat();
			
			if (probability < probabilityInteractive)
				processes[i] = new Process(Process.TYPE_INTERACTIVE, i);
			else
				processes[i] = new Process(Process.TYPE_CPU, i);
			
			//Process have entered ready queue
			//System.out.println("[time 0ms] " + processes[i].getTypeString() + " process ID " + i + 
			//		" entered ready queue (requires "  + processes[i].getBurstTime() + "ms CPU time)");
		}
	}
	
	public void FCFS(){		
		queue = new LinkedList<Process>();
		initAlgorithm();
		
		int totalTime = 0;

		//run all interactive processes
		while (true)
		{
			//check all the waiting processes
			handleWaitingProcesses(queue, processes, totalTime);
			
			//run each core separately
			for (int i = 0; i < numCores; i++)
			{
				Process currentProcess, nextProcess;
				
				//if uninitialized, remove process from the queue
				if (coreToProcessID[i] == -1)
				{
					//no processes to run
					if (queue.isEmpty())
						continue;
					
					currentProcess = queue.remove();
					
					coreToProcessID[i] = currentProcess.ID;
					timeFinish[coreToProcessID[i]] = totalTime + currentProcess.getBurstTime();
				}
				else	
				{
					currentProcess = processes[coreToProcessID[i]];
				}
				
				//fetch the next process
				nextProcess = queue.peek();
				
				//if core is finished running current process
				//-->swap currentProcess with newProcess
				//-->remove newProcess from queue (add it back later once it's done waiting for I/O)
				//...
				//increment time
				
				//core is finished running process
				if (timeFinish[currentProcess.ID] == totalTime)
				{	
					//check if cpu-bound process terminated
					handleProcessBurstDone(currentProcess, totalTime);
					
					//io blocking for this process
					currentProcess.setIOBlockTime(totalTime);
					
					//Queue is empty, don't go forward
					if (queue.isEmpty())
					{
						//this core isn't running any process 
						coreToProcessID[i] = -1;
						continue;
					}
					
					//remove nextProcess from the queue
					nextProcess = queue.remove();
					
					//Context Switch
					handleContextSwitchNoPre(currentProcess, nextProcess, i, totalTime, nextProcess.getBurstTime());
				}		
				else
				{
					currentProcess.incrementProcessStats(1, 0, 0);
				}
			}
			
			totalTime++;
			
			//terminate the program
			if (checkIfCPUProcessDone(processes))
				break;
		}

		printAllProcessStats(processes, totalTime);
	}
	
	public void SJFNoPre(){
		queue = new PriorityQueue<Process>(processes.length, Process.ProcessComparatorBurst);
		initAlgorithm();
	
		int totalTime = 0;
		
		while(true)
		{
			//check all the waiting processes
			handleWaitingProcesses(queue, processes, totalTime);
			
			for(int i = 0; i < numCores; i++)
			{
				Process currentProcess, nextProcess;
				
				//if uninitialized, remove process from the queue
				if (coreToProcessID[i] == -1)
				{
					//no processes to run
					if (queue.isEmpty())
						continue;
					
					currentProcess = queue.remove();
					
					coreToProcessID[i] = currentProcess.ID;
					timeFinish[coreToProcessID[i]] = totalTime + currentProcess.getBurstTime();
				}
				else	
				{
					currentProcess = processes[coreToProcessID[i]];
				}
				
				//if core is finished running current process
				//-->swap currentProcess with newProcess
				//-->remove newProcess from queue (add it back later once it's done waiting for I/O)
				//...
				//increment time
				
				//core is finished running process
				if (timeFinish[currentProcess.ID] == totalTime)
				{	
					//check if cpu-bound process terminated
					handleProcessBurstDone(currentProcess, totalTime);
					
					//io blocking for this process
					currentProcess.setIOBlockTime(totalTime);
					
					//Queue is empty, don't go forward
					if (queue.isEmpty())
					{
						//this core isn't running any process 
						coreToProcessID[i] = -1;
						continue;
					}
					
					//remove nextProcess from the queue
					nextProcess = queue.remove();
					
					//Context Switch
					handleContextSwitchNoPre(currentProcess, nextProcess, i, totalTime, nextProcess.getBurstTime());
				}		
				else
				{
					currentProcess.incrementProcessStats(1, 0, 0);
				}
			}
			totalTime++;
			
			//terminate the program
			if (checkIfCPUProcessDone(processes))
				break;
		}
		printAllProcessStats(processes, totalTime);
	}
	
	public void SJFWithPre(){		
		queue = new PriorityQueue<Process>(processes.length, Process.ProcessComparatorRemain);;
		initAlgorithm();
		
		int totalTime = 0;
		
		// run all interactive processes
		while (true)
		{
			// check all the waiting processes
			handleWaitingProcesses(queue, processes, totalTime);
			
			// run each core separately
			for(int i = 0; i < numCores; i++)
			{
				Process currentProcess, nextProcess;
				
				//if uninitialized, remove process from the queue
				if (coreToProcessID[i] == -1)
				{
					//no processes to run
					if (queue.isEmpty())
						continue;
					
					currentProcess = queue.remove();
					
					coreToProcessID[i] = currentProcess.ID;
					timeFinish[coreToProcessID[i]] = totalTime + currentProcess.getRemainBurst();
				}
				else	
				{
					currentProcess = processes[coreToProcessID[i]];
				}
				
				//if core is finished running current process
				//-->swap currentProcess with newProcess
				//-->remove newProcess from queue (add it back later once it's done waiting for I/O)
				//...
				//increment time
				
				//core is finished running process
				if (timeFinish[currentProcess.ID] == totalTime)
				{	
					//check if cpu-bound process terminated
					handleProcessBurstDone(currentProcess, totalTime);
					
					//io blocking for this process
					currentProcess.setIOBlockTime(totalTime);
					
					//Queue is empty, don't go forward
					if (queue.isEmpty())
					{
						//this core isn't running any process 
						coreToProcessID[i] = -1;
						continue;
					}
					
					//remove nextProcess from the queue
					nextProcess = queue.remove();
					
					//Context Switch
					handleContextSwitchNoPre(currentProcess, nextProcess, i, totalTime, nextProcess.getBurstTime());

					if (nextProcess.getRemainBurst() <= 0) //if process burst runs out, reset it 
						nextProcess.initRemainBurst();
				}
				else if(!queue.isEmpty() && currentProcess.getRemainBurst() > queue.peek().getRemainBurst()){
					//Context Switch
					System.out.println("[time " + totalTime + "ms] " + 
							"Context switch (swapping out process ID " + currentProcess.ID + 
							" for process ID " + queue.peek().ID + ")");
					queue.add(currentProcess);
					currentProcess = queue.remove();
					timeFinish[currentProcess.ID] = totalTime + currentProcess.getRemainBurst();
					coreToProcessID[i] = currentProcess.ID;
				}
				else
				{
					currentProcess.incrementProcessStats(1, 0, 0);
					currentProcess.decreaseRemainBurst(1);
				}
			}
			totalTime++;
			
			//terminate the program
			if (checkIfCPUProcessDone(processes))
				break;
		}
		printAllProcessStats(processes, totalTime);
	}
	
	public void RR(){
		int fixedTimeslice = 80;
		
		queue = new LinkedList<Process>();
		initAlgorithm();

		int totalTime = 0;

		//run all interactive processes
		while (true)
		{
			//check all the waiting processes
			handleWaitingProcesses(queue, processes, totalTime);
			
			//run each core separately
			for (int i = 0; i < numCores; i++)
			{
				Process currentProcess, nextProcess;
				
				//if uninitialized, remove process from the queue
				if (coreToProcessID[i] == -1)
				{
					//no processes to run
					if (queue.isEmpty())
						continue;
					
					currentProcess = queue.remove();
					
					//the core is running this process
					coreToProcessID[i] = currentProcess.ID;
					
					//its ready time is a fixed timeslice away
					timeFinish[coreToProcessID[i]] = totalTime + fixedTimeslice;
					
					//set process running
					currentProcess.initRemainBurst();
				}
				else	
				{
					currentProcess = processes[coreToProcessID[i]];
				}
				
				//check if the process stopped running before its fixed timeslice was up
				boolean isCurrentProcessDone = false;
				if (currentProcess.getRemainBurst() <= 0)
				{
					isCurrentProcessDone = true;
					currentProcess.initRemainBurst();
					
					//check if cpu-bound process terminated
					handleProcessBurstDone(currentProcess, totalTime);
				}

				//current process's burst isn't done yet, but it ran out of time
				//put process in the back of the queue
				if (!isCurrentProcessDone && timeFinish[currentProcess.ID] == totalTime)
					queue.add(currentProcess);
				
				//fetch the next process
				nextProcess = queue.peek();
				
				//if core is finished running current process OR timeslice runs out
				//-->swap currentProcess with newProcess
				//-->remove newProcess from queue (add it back later once it's done waiting for I/O)
				//...
				//increment time
				
				//core is finished running process
				if (isCurrentProcessDone || timeFinish[currentProcess.ID] == totalTime)
				{	
					//io blocking for this process
					currentProcess.setIOBlockTime(totalTime);
					
					//Queue is empty, don't go forward
					if (queue.isEmpty())
					{
						//this core isn't running any process 
						coreToProcessID[i] = -1;
						continue;
					}
					
					//remove nextProcess from the queue
					nextProcess = queue.remove();

					//Context Switch
					handleContextSwitchNoPre(currentProcess, nextProcess, i, totalTime, fixedTimeslice);

					if (nextProcess.getRemainBurst() <= 0) //if process burst runs out, reset it 
						nextProcess.initRemainBurst();
				}		
				else
				{
					currentProcess.incrementProcessStats(1, 0, 0);
					currentProcess.decreaseRemainBurst(1);
				}
			}
			
			totalTime++;
			
			//terminate the program
			if (checkIfCPUProcessDone(processes))
				break;
		}

		printAllProcessStats(processes, totalTime);
	}
	
	//
	// Helper functions
	// 
	public void initAlgorithm()
	{
		for (int i = 0; i < processes.length; i++)
		{
			processes[i].initBurstTime();
			processes[i].initRemainBurst();
			queue.add(processes[i]);
			timeEnterReady[i] = 0;
			numContextSwitch[i] = 0;
			System.out.println("[time 0ms] "+
					processes[i].getTypeString() +" process ID " + i + " entered ready queue " +
							"(requires "+processes[i].getBurstTime() + "ms CPU time)");
		}
		
		coreToProcessID = new int[numCores];
		for (int i = 0; i < coreToProcessID.length; i++)
			coreToProcessID[i] = -1;
	}
	
	public void handleWaitingProcesses(Queue<Process> queue, Process[] processes, int totalTime)
	{
		//check all the waiting processes
		for (int i = 0; i < processes.length; i++)
		{
			//block time not initialized
			if (processes[i].getIOBlockTime() == 0)
				continue;
			
			//unblock this process, add it back to the queue
			if (processes[i].getIOBlockTime() == totalTime)
			{
				processes[i].zeroIOBlockTime();
				queue.add(processes[i]);
				timeEnterReady[i] = totalTime;
				processes[i].initBurstTime();
				processes[i].initRemainBurst();
			}
		}
	}
	
	public void handleProcessBurstDone(Process currentProcess, int totalTime)
	{
		//check if cpu-bound process terminated
		if (currentProcess.getType() == Process.TYPE_CPU)
		{	
			currentProcess.decrementNumBursts();
			if (currentProcess.isBurstsDone())
			{
				System.out.println("[time " + totalTime + "ms] " +
						"CPU-bound process ID " + currentProcess.ID + " terminated " + 
						"(total turnaround time " + currentProcess.getTotalTurnaround() + "ms, " +
						"total wait time " + currentProcess.getTotalWait() +"ms)");
			}	
		}
		else if (currentProcess.getType() == Process.TYPE_INTERACTIVE)
		{
			System.out.println("[time " + totalTime + "ms] " + 
					"Interactive process ID " + currentProcess.ID + " CPU burst done " +
					"(turnaround time " + currentProcess.getTotalTurnaround() + "ms, " +
							"total wait time " + currentProcess.getTotalWait() + "ms)");
		}
	}
	
	public void handleContextSwitchNoPre(Process currentProcess, Process nextProcess, int coreNum, int totalTime, int duration)
	{
		int turnaroundTime = totalTime - timeEnterReady[currentProcess.ID];
		int waitTime = turnaroundTime - currentProcess.getBurstTime();
		
		//calculate turnaround time
		currentProcess.incrementProcessStats(0, 0, turnaroundTime);
		
		//calculate wait time
		currentProcess.incrementProcessStats(0, waitTime, 0);
		
		//print message
		numContextSwitch[currentProcess.ID]++;
		System.out.println("[time " + totalTime + "ms] " + 
				"Context switch (swapping out process ID " + currentProcess.ID + 
				" for process ID " + nextProcess.ID + ")");
		
		//set core to run this new process
		coreToProcessID[coreNum] = nextProcess.ID;
		
		//process will be put in ready queue after its burst (or timeslice) is done
		timeFinish[nextProcess.ID] = totalTime + duration + timeSwitch;
	}
	
	public boolean checkIfCPUProcessDone(Process[] processes)
	{
		boolean isCPUProcessDone = true;
		for (int i = 0; i < processes.length; i++)
		{
			//only check cpu-bound processes
			if (processes[i].getType() == Process.TYPE_CPU)
			{
				isCPUProcessDone &= processes[i].isBurstsDone();
			}
		}
		return isCPUProcessDone;
	}
	
	public void printAllProcessStats(Process[] processes, int finishedTime)
	{
		//Calculate process statistics
		int minTurnaround = Integer.MAX_VALUE, maxTurnaround = Integer.MIN_VALUE; 
		int minWait = Integer.MAX_VALUE, maxWait = Integer.MIN_VALUE;
		double averageTurnaround = 0, averageWait = 0;
		double totalBurst = 0, averagePercentBurst = 0;
		for (int i = 0; i < processes.length; i++)
		{
			//keep track of statistics
			int processTurnaround = processes[i].getTotalTurnaround();
			minTurnaround = Math.min(minTurnaround, processTurnaround);
			maxTurnaround = Math.max(maxTurnaround, processTurnaround);
			averageTurnaround += processTurnaround;
			
			int processWait = processes[i].getTotalWait();
			minWait = Math.min(minWait, processWait);
			maxWait = Math.max(maxWait, processWait);
			averageWait += processWait;
			
			totalBurst += processes[i].getTotalBurst() - (timeSwitch * numContextSwitch[i]);
		}
		
		averageTurnaround /= (double)processes.length;
		averageWait /= (double)processes.length;
		averagePercentBurst = ((totalBurst / finishedTime) * 100);
		averagePercentBurst /= numCores;
		
		//Print out process statistics
		System.out.println("----------------------------------------");
		System.out.println("Turnaround time: min " + minTurnaround + "ms; avg " + String.format("%.2f", averageTurnaround) + "ms; max " + maxTurnaround + "ms");
		System.out.println("Total wait time: min " + minWait + "ms; avg " + String.format("%.2f", averageWait) + "ms; max " + maxWait + "ms");
		System.out.println("Average CPU utilization: " + String.format("%.2f", averagePercentBurst) + "%\n");
		
		System.out.println("Average CPU utilization per process: ");
		for (int i = 0; i < processes.length; i++)
		{
			int overallBurst = processes[i].getTotalBurst() - (timeSwitch * numContextSwitch[i]);
			double percentBurst = ((double)overallBurst / finishedTime) * 100;
			System.out.println("process ID " +  i + ": " + String.format("%.2f", percentBurst) +"%");
		}
	}
}
