package com.os.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class SystemSim {
	private int NumCores;		  // number of cores
	private int ready[];	  	  // ready state of each cores
	private int coreToProcessID[]; //each core runs a different process
	private Process processes[];  // List of processes
	private Queue<Process> queue; // queue of processes
	private int totalTime;		  // Timer
	private int switchTime;
	
	public SystemSim(float probabilityInteractive, int numCores, int numProcess, int switchTime){
		Random randObj = new Random();
		processes = new Process[numProcess];
		NumCores = numCores;
		ready = new int[processes.length];
		totalTime = 0;
		this.switchTime = switchTime;
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
		for (int i = 0; i < processes.length; i++)
			queue.add(processes[i]);
		
		coreToProcessID = new int[NumCores];
		for (int i = 0; i < coreToProcessID.length; i++)
			coreToProcessID[i] = -1;

		int totalTime = 0;

		//run all interactive processes
		while (true)
		{
			//check all the waiting processes
			//System.out.println("totalTime: " + totalTime);
			for (int i = 0; i < processes.length; i++)
			{
				//block time not initialized
				if (processes[i].getIOBlockTime() == 0)
					continue;

				//System.out.println("totalTime: " + totalTime);
				//System.out.println("process " + i + " block time " + processes[i].getIOBlockTime());
				
				if (processes[i].getIOBlockTime() == totalTime)
				{
					System.out.println("WAIT IS OVER\n Process " + i + ": ");
					System.out.println("total time: " + totalTime);
					//System.out.println("ready[i]: " + ready[i] + ", block time = " + processes[i].getIOBlockTime() + 
					//		", total time: " + totalTime);
					processes[i].setIOBlockTime(Integer.MAX_VALUE);
					queue.add(processes[i]);
				}
			}
			
			
			//run each core separately
			for (int i = 0; i < NumCores; i++)
			{
				Process currentProcess, nextProcess;
				
				//if uninitialized, remove process from the queue
				if (coreToProcessID[i] == -1)
				{
					currentProcess = queue.remove();
					coreToProcessID[i] = currentProcess.ID;
					ready[coreToProcessID[i]] = totalTime + currentProcess.getBurstTime();
					//currentProcess.setIOBlockTime(totalTime);
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
				if (ready[currentProcess.ID] == totalTime)
				{	
					System.out.println("swap process");
					//check if cpu-bound process terminated
					if (currentProcess.getType() == Process.TYPE_CPU)
					{	
						System.out.println("before BURSTS LEFT TO GO: " + currentProcess.getBurstsLeft());
						currentProcess.decrementBursts();
						System.out.println("after BURSTS LEFT TO GO: " + currentProcess.getBurstsLeft());
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
					
					
					//Queue is empty, don't go forward
					if (queue.isEmpty())
					{
						continue;
					}
					
					//remove nextProcess from the queue
					nextProcess = queue.remove();
					
					//Context Switch
					System.out.println("[time " + totalTime + "ms] " + 
							"Context switch (swapping out process ID " + currentProcess.ID + 
							" for process ID " + nextProcess.ID + ")");

					//save it in the core->process id map
					coreToProcessID[i] = nextProcess.ID;

					//process will be put in ready queue after its burst is done
					ready[nextProcess.ID] = totalTime + nextProcess.getBurstTime();

					//FOR NOW (ignore I/O)
					//add it to the back
					/////queue.add(nextProcess);
					currentProcess.setIOBlockTime(totalTime);
	
					//System.out.println("Core: " + i);
					//for (int j = 0; j < processes.length; j++)
					//	System.out.println("ready process " + j + " : " + ready[j]);
					
					nextProcess.incrementProcessStats(0, 1, 0);
				}		
				else
				{
					currentProcess.incrementProcessStats(1, 0, 1);
				}
			}
			
			totalTime++;
			
			//terminate the program
			boolean isCPUProcessDone = true;
			for (int i = 0; i < processes.length; i++)
			{
				//only check cpu-bound processes
				if (processes[i].getType() == Process.TYPE_CPU)
				{
					//System.out.println("CPU PROCESS ID " + i + " is it done? " + processes[i].isBurstsDone());
					//System.out.println("How many bursts left? " + processes[i].getBurstsLeft());
					isCPUProcessDone &= processes[i].isBurstsDone();
				}
			}
			if (isCPUProcessDone)
				break;
			
			//if (totalTime > 2000)
			//	break;
		}

		printAllProcessStats(processes, totalTime);
	}
	
	public void SJFNoPre(){
		queue = new PriorityQueue<Process>(processes.length, Process.ProcessComparatorBurst);

		for (int i = 0; i < processes.length; i++)
		{
			queue.add(processes[i]);
			System.out.println("[time 0ms] "+processes[i].getTypeString()+" process ID "+i+" entered ready queue (requires "+processes[i].getBurstTime() + "ms CPU time)");
		}
		while(true)
		{
			for(int i = 0; i < NumCores; i++)
			{
				if(totalTime >= ready[i])
				{
					if(!queue.isEmpty())
					{
						Process newProcess = queue.remove();
						ready[i] += newProcess.getBurstTime();
						ready[i] += switchTime;
						//System.out.println("[time "+totalTime +"ms] Context switch (swapping out process ID "+ +" for process ID "+ newProcess.ID +")");
//						if()
//						{
//							
//						}
					}
					else
					{
						continue;
					}
				}
			}
			totalTime++;
		}
		//runCores(processSorted);
	}
	
	public void SJFWithPre(){
		
	}
	
	public void RR(){
		
	}
	
	/***
	 * 
	 * @param num id of algorithm, default==> FCFS, 1==>SJFnoPre, 2==>SJFWithPre, 3==>RR
	 */
	public void run(int num){
		switch(num){
			case 1:
				SJFNoPre();
				break;
			case 2:
				SJFWithPre();
				break;
			case 3:
				RR();
				break;
			default:
				FCFS();
				break;
				
		}
	}
	
	//
	// Helper functions
	// 
	
	public int runCurrentProcess(Process currentProcess, Process nextProcess, int currentTime)
	{
		int timeContextSwitch = 4;
		int burstTime = currentProcess.getBurstTime();
		int turnaroundTime = currentTime + burstTime;
		int waitTime = turnaroundTime - burstTime;
		//int turnaroundTime = waitTime + burstTime; //t = w + b

		//run the burst
		currentTime += burstTime;
		System.out.println("[time " + currentTime + "ms] " + 
				currentProcess.getTypeString() + " process ID " + currentProcess.ID + " CPU burst done " +
				"(turnaround time " + turnaroundTime + "ms, total wait time " + waitTime + "ms)");
		currentProcess.incrementProcessStats(burstTime, turnaroundTime, waitTime);

		//adjust stats for cpu-bound processes only
		if (currentProcess.getType() == Process.TYPE_CPU)
		{	
			currentProcess.decrementBursts();
			if (currentProcess.isBurstsDone())
			{
				System.out.println("[time " + currentTime + "ms] " +
						"CPU-bound process ID " + currentProcess.ID + " terminated " + 
						"(total turnaround time " + currentProcess.getTotalTurnaround() + "ms, " +
						"total wait time " + currentProcess.getTotalWait() +"ms)");
			}
		}

		//human input delay (for interactive process)
		currentTime += currentProcess.getIOBlockTime();

		//put current process in the ready queue
		//get ready to switch to next process
		nextProcess.setLastTimeRan(currentTime - turnaroundTime);

		//context switch
		System.out.println("[time " + currentTime + "ms] Context switch " +
				"(swapping out process ID " + currentProcess.ID +
				" for process ID " + nextProcess.ID + ")");
		currentTime += timeContextSwitch;
		
		return currentTime;
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
		double totalBurst = 0, totalPercentBurst = 0;
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
			
			totalBurst += processes[i].getTotalBurst();
		}
		
		averageTurnaround /= (double)processes.length;
		averageWait /= (double)processes.length;
		totalPercentBurst = (totalBurst / finishedTime) * 100;
		
		//Print out process statistics
		System.out.println("----------------------------------------");
		System.out.println("Turnaround time: min " + minTurnaround + "ms; avg " + String.format("%.2f", averageTurnaround) + "ms; max " + maxTurnaround + "ms");
		System.out.println("Total wait time: min " + minWait + "ms; avg " + String.format("%.2f", averageWait) + "ms; max " + maxWait + "ms");
		System.out.println("Average CPU utilization: " + String.format("%.2f", totalPercentBurst) + "%\n");
		
		System.out.println("Average CPU utilization per process: ");
		for (int i = 0; i < processes.length; i++)
		{
			double percentBurst = ((double)processes[i].getTotalBurst() / finishedTime) * 100;
			System.out.println("process ID " +  i + ": " + String.format("%.2f", percentBurst) +"%");
		}
	}
}
