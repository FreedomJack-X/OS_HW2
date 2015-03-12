package com.os.process;

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
		while(true)
		{
			for(int i = 0; i < NumCores; i++)
			{
				
			}
			totalTime++;
		}
	}
	
	public void SJFNoPre(){
		
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
}
