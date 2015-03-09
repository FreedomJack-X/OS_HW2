package com.os.process;

import java.util.Random;

public class FCFS 
{
	public static void initProcess(Process[] processes, float probabilityInteractive)
	{
		Random randObj = new Random();
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

	public static boolean checkIfCPUDone(Process[] processes)
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
	
	public static void printAllProcessStats(Process[] processes)
	{
		int minTurnaround = Integer.MAX_VALUE, maxTurnaround = Integer.MIN_VALUE; 
		int minWait = Integer.MAX_VALUE, maxWait = Integer.MIN_VALUE;
		double averageTurnaround = 0, averageWait = 0;
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
		}
		
		averageTurnaround /= (double)processes.length;
		averageWait /= (double)processes.length;
		
		System.out.println("Turnaround time: min " + minTurnaround + "ms; avg " + String.format("%.2f", averageTurnaround) + "ms; max " + maxTurnaround + "ms");
		System.out.println("Total wait time: min " + minWait + "ms; avg " + String.format("%.2f", averageWait) + "ms; max " + maxWait + "ms");
	}
	
	public static void main(String[] args) 
	{
		//Simulation Parameters
		float probabilityInteractive = 0.8f; //p_int; //process breakdown: 80% interactive, 20% cpu bound
		int timeContextSwitch = 4; //t_cs = 4ms
		
		//Initialize all processes
		Process[] processes = new Process[12]; //assume 12 processes
		initProcess(processes, probabilityInteractive);
		
		int currentTime = 0;
		
		//run all interactive processes
		while (true)
		{
			for (int i = 0; i < processes.length; i++)
			{
				int beforeRan = currentTime;
				
				//run the burst
				currentTime += processes[i].getBurstTime();
				int waitTime = processes[i].getWaitTime(beforeRan);
				int turnaroundTime = processes[i].getWaitTime(beforeRan) + processes[i].getBurstTime();
				System.out.println("[time " + currentTime + "ms] " + processes[i].getTypeString() + " process ID " + i + " CPU burst done " +
						 "(turnaround time " + turnaroundTime + "ms, total wait time " + waitTime + "ms)");
				
				processes[i].incrementTurnaround(turnaroundTime);
				processes[i].incrementWait(waitTime);
				
				//adjust stats for cpu-bound processes only
				if (processes[i].getType() == Process.TYPE_CPU)
				{	
					processes[i].decrementBursts();
					if (processes[i].isBurstsDone())
					{
						System.out.println("[time " + currentTime + "ms] CPU-bound process ID " + i + " terminated " + 
								"(total turnaround time " + processes[i].getTotalTurnaround() + "ms, total wait time xxxms");
					}
				}
				
				//human input delay (for interactive process)
				currentTime += processes[i].getHumanResponseTime();
				
				//put in the ready queue
				processes[i].setLastTimeRan(currentTime);
				
				//context switch
				System.out.println("[time " + currentTime + "ms] Context switch (swapping out process ID " + i +
						" for process ID " + ((i + 1)%processes.length) + ")");
				currentTime += timeContextSwitch;
			}

			//terminate the program
			if (checkIfCPUDone(processes))
				break;
		}
		
		printAllProcessStats(processes);
	}

}
