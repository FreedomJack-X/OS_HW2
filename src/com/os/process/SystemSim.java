package com.os.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Queue;
import java.util.Random;

public class SystemSim {
	private int NumCores;		  // number of cores
	private boolean ready[];	  // ready state of each cores
	private Process processes[];  // List of processes
	private Queue<Process> queue; // queue of processes
	private int totalTime;		  // Timer
	
	public SystemSim(float probabilityInteractive, int numCores, int numProcess){
		Random randObj = new Random();
		processes = new Process[numProcess];
		NumCores = numCores;
		ready = new boolean[numCores];
		totalTime = 0;
		for (int j = 0; j < ready.length; j++)
		{
			ready[j] = true;
		}
		for (int i = 0; i < processes.length; i++)
		{
			//determine process type
			float probability = randObj.nextFloat();
			
			if (probability < probabilityInteractive)
				processes[i] = new Process(Process.TYPE_INTERACTIVE);
			else
				processes[i] = new Process(Process.TYPE_CPU);
			
			//Process have entered ready queue
			System.out.println("[time 0ms] " + processes[i].getTypeString() + " process ID " + i + 
					" entered ready queue (requires "  + processes[i].getBurstTime() + "ms CPU time)");
		}
		
		
	}
	
	public void FCFS(){
		runSingleCore(processes);
	}
	
	public void SJFNoPre(){
		//sort processes by burst time
		ArrayList<Process> processSorted = new ArrayList<Process>();
		for (int i = 0; i < processes.length; i++)
			processSorted.add(processes[i]);

		Collections.sort(processSorted, new Comparator<Process>(){
			public int compare(Process p1, Process p2)
			{
				return Integer.compare(p1.getBurstTime(), p2.getBurstTime());
			}
		});
		
		Process[] processSortedArray = new Process[processes.length];
		for (int i = 0; i < processes.length; i++)
			processSortedArray[i] = processSorted.get(i);
		
		runSingleCore(processSortedArray);
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
	
	public void runSingleCore(Process[] systemProcess)
	{
		int timeContextSwitch = 4;
		int currentTime = 0;
		
		//run all interactive processes
		while (true)
		{
			//FOR NOW: Assume there's only 1 CPU-core 
			for (int i = 0; i < systemProcess.length; i++)
			{
				int burstTime = systemProcess[i].getBurstTime();
				int waitTime = systemProcess[i].getWaitTime(currentTime);
				int turnaroundTime = waitTime + burstTime; //t = w + b

				//run the burst
				currentTime += burstTime;
				System.out.println("[time " + currentTime + "ms] " + 
						systemProcess[i].getTypeString() + " process ID " + i + " CPU burst done " +
						"(turnaround time " + turnaroundTime + "ms, total wait time " + waitTime + "ms)");
				systemProcess[i].incrementProcessStats(burstTime, turnaroundTime, waitTime);

				//adjust stats for cpu-bound processes only
				if (systemProcess[i].getType() == Process.TYPE_CPU)
				{	
					systemProcess[i].decrementBursts();
					if (systemProcess[i].isBurstsDone())
					{
						System.out.println("[time " + currentTime + "ms] CPU-bound process ID " + i + " terminated " + 
								"(total turnaround time " + systemProcess[i].getTotalTurnaround() + "ms, " +
								"total wait time " + systemProcess[i].getTotalWait() +"ms)");
					}
				}

				//human input delay (for interactive process)
				currentTime += systemProcess[i].getHumanResponseTime();

				//put process in the ready queue
				systemProcess[(i + 1)%systemProcess.length].setLastTimeRan(currentTime - turnaroundTime);

				//context switch
				System.out.println("[time " + currentTime + "ms] Context switch (swapping out process ID " + i +
						" for process ID " + ((i + 1)%systemProcess.length) + ")");
				currentTime += timeContextSwitch;
			}

			//terminate the program
			if (checkIfCPUDone(systemProcess))
				break;
		}
		
		printAllProcessStats(systemProcess, currentTime);
	}
	
	public boolean checkIfCPUDone(Process[] processes)
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
