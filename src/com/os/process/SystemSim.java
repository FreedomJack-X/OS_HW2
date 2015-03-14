package com.os.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class SystemSim {
	private int NumCores;		  // number of cores
	private int ready[];	  // ready state of each cores
	private Process processes[];  // List of processes
	private Queue<Process> queue; // queue of processes
	private int totalTime;		  // Timer
	private int switchTime;
	
	public SystemSim(float probabilityInteractive, int numCores, int numProcess, int switchTime){
		Random randObj = new Random();
		processes = new Process[numProcess];
		NumCores = numCores;
		ready = new int[numCores];
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
		Process[] processCopied = new Process[processes.length];
		for (int i = 0; i < processes.length; i++)
			processCopied[i] = processes[i];
		
		queue = new LinkedList<Process>();

		runCores(processCopied);
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
	
	public void runCores(Process[] systemProcess)
	{
		int currentTime = 0;
		
		for (int i = 0; i < systemProcess.length; i++)
			queue.add(systemProcess[i]);
		
		int[] currentTimes = new int[NumCores];
		for (int i = 0; i < NumCores; i++)
			currentTimes[i] = 0;
	
		//run all interactive processes
		while (true)
		{
			//run each core separately
			for (int i = 0; i < NumCores; i++)
			{
				Process currentProcess = queue.remove(); 
				
				//reset queue
				if (queue.isEmpty())
				{
					for (int j = 0; j < systemProcess.length; j++)
						queue.add(systemProcess[j]);
				}
				
				Process nextProcess = queue.peek();
				
				currentTimes[i] = runCurrentProcess(currentProcess, nextProcess, currentTimes[i]);
			}
			
			//terminate the program
			if (checkIfCPUProcessDone(systemProcess))
				break;
		}
		
		Arrays.sort(currentTimes);
		printAllProcessStats(systemProcess, currentTimes[NumCores - 1]);
	}
	
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
		currentTime += currentProcess.getHumanResponseTime();

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
