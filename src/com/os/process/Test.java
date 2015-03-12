package com.os.process;

import java.util.Random;

public class Test 
{
	public static void main(String[] args) 
	{
		SystemSim system = new SystemSim(0.8f, 2, 12);
		system.FCFS();
		//system.SJFNoPre();
	}

}
