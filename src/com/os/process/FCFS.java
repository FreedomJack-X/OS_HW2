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
	
	public static void main(String[] args) 
	{
		//Simulation Parameters
		float probabilityInteractive = 0.8f; //p_int; //process breakdown: 80% interactive, 20% cpu bound
		int timeContextSwitch = 4; //t_cs = 4ms
		
		//Initialize all processes
		Process[] processes = new Process[12]; //assume 12 processes
		initProcess(processes, probabilityInteractive);
		
		int overallTime = 0;
		int minTurnaround = Integer.MAX_VALUE, maxTurnaround = Integer.MIN_VALUE;
		int minWait = Integer.MAX_VALUE, maxWait = Integer.MIN_VALUE;
		
		//run all interactive processes
		while (true)
		{
			for (int i = 0; i < processes.length; i++)
			{
				int beforeRan = overallTime;
				
				//run the burst
				overallTime += processes[i].getBurstTime();
				int waitTime = processes[i].getWaitTime(beforeRan);
				int turnaroundTime = processes[i].getWaitTime(beforeRan) + processes[i].getBurstTime();
				System.out.println("[time " + overallTime + "ms] " + processes[i].getTypeString() + " process ID " + i + " CPU burst done " +
						 "(turnaround time " + turnaroundTime + "ms, total wait time " + waitTime + "ms)");
				
				//keep track of statistics
				minTurnaround = Math.min(minTurnaround, turnaroundTime);
				maxTurnaround = Math.max(maxTurnaround, turnaroundTime);
				minWait = Math.min(minWait, waitTime);
				maxWait = Math.max(maxWait, waitTime);
		
				processes[i].incrementTurnaround(turnaroundTime);
				//adjust stats for cpu-bound processes only
				if (processes[i].getType() == Process.TYPE_CPU)
				{	
					processes[i].decrementBursts();
					if (processes[i].isBurstsDone())
					{
						System.out.println("[time " + overallTime + "ms] CPU-bound process ID " + i + " terminated " + 
								"(total turnaround time " + processes[i].getTotalTurnaround() + "ms, total wait time xxxms");
					}
				}
				
				//human input delay (for interactive process)
				overallTime += processes[i].getHumanResponseTime();
				
				//put in the ready queue
				processes[i].setLastTimeRan(overallTime);
				
				//context switch
				System.out.println("[time " + overallTime + "ms] Context switch (swapping out process ID " + i +
						" for process ID " + ((i + 1)%processes.length) + ")");
				overallTime += timeContextSwitch;
			}

			//terminate the program
			if (checkIfCPUDone(processes))
				break;
		}
		
		System.out.println("Turnaround time: min " + minTurnaround + " ms; avg ___ ms; max " + maxTurnaround + " ms");
		System.out.println("Total wait time: min " + minWait + " ms; avg ___ ms; max " + maxWait + " ms");
	}

}
