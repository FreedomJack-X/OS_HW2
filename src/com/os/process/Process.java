package com.os.process;

import java.util.Random;

public class Process 
{
	private int type;
	public static final int TYPE_INTERACTIVE = 0;
	public static final int TYPE_CPU = 1;
	public int getType() { return type; }
	public String getTypeString() 
	{
		if (type == TYPE_INTERACTIVE) { return "Interactive"; }
		if (type == TYPE_CPU) { return "CPU-bound"; }
		return ""; 
	}
	
	private int burstTime;
	
	//for interactive process
	private int lastTimeRan;
	
	//for cpu-bound process
	private int burstsLeft;
	private int totalTurnaround;
	
	private Random randomObj;
	
	public Process(int type)
	{
		randomObj = new Random();
		
		this.type = type;
		if (type == TYPE_INTERACTIVE)
		{
			burstTime = randomObj.nextInt(200 - 20) + 20; 
		}
		else if (type == TYPE_CPU)
		{
			//there are b=6 bursts
			burstTime = randomObj.nextInt(3000 - 200) + 200;
			
			burstsLeft = 2; //set to 2 for testing purposes
		}
		
		totalTurnaround = 0;
		lastTimeRan = 0;
	}
	
	public int getHumanResponseTime() { return randomObj.nextInt(4500 - 1000) + 1000; }
	
	public int getBurstTime() { return burstTime; } 
	public void decrementBursts() { burstsLeft--; } 
	public boolean isBurstsDone() { return (burstsLeft <= 0); } 
		
	public int getWaitTime(int currentTime) { return currentTime - this.lastTimeRan; }
	public void setLastTimeRan(int time) { this.lastTimeRan = time; }
	public int getTotalTurnaround() { return this.totalTurnaround; }
	public void incrementTurnaround(int time) { this.totalTurnaround += time; }
}
