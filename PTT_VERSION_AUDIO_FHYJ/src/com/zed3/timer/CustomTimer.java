package com.zed3.timer;


public class CustomTimer implements Runnable{
	
	private CustomTimerTask task;
	private boolean isRunning;
	private long lastTime;
	private long delay;
	private long rate;
	private Thread thread;
	private boolean hasScheduled;
	private boolean hasCanceled;
	private boolean hasDone;
	
	public CustomTimer(){
		
	}
	
	public void schedule(CustomTimerTask task,long delay,long rate){
		if (hasScheduled) {
			return;
		}
		this.task = task;
		this.delay = delay;
		this.rate = rate;
		thread = new Thread(task);
		thread.start();
		hasScheduled = true;
	}
	public void cancel(){
		if (hasCanceled) {
			return;
		}
		thread.stop();
		hasCanceled = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isRunning = true;
		while (isRunning) {
			if (lastTime == -1) {
				lastTime = System.currentTimeMillis();
				task.run();
			}
			if((System.currentTimeMillis()- lastTime)%rate==0) {
				if (hasDone) {
					return;
				}
				task.run();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
