/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.zed3.media;

import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;
import org.zoolu.tools.MyLog;

import android.preference.PreferenceManager;

import com.zed3.codecs.Codecs;
import com.zed3.location.MemoryMg;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Sipdroid;

/** Audio launcher based on javax.sound  */
public class JAudioLauncher implements MediaLauncher
{  
   /** Event logger. */
   Log log=null;

   /** Sample rate [bytes] */
   int sample_rate=8000;
   /** Sample size [bytes] */
   int sample_size=1;
   /** Frame size [bytes] */
   int frame_size=160;
   /** Frame rate [frames per second] */
   int frame_rate=50; //=sample_rate/(frame_size/sample_size);
   boolean signed=false; 
   boolean big_endian=false;

   //String filename="audio.wav"; 

   /** Test tone */
   public static final String TONE="TONE";

   /** Test tone frequency [Hz] */
   public static int tone_freq=100;
   /** Test tone ampliture (from 0.0 to 1.0) */
   public static double tone_amp=1.0;

   /** Runtime media process */
   Process media_process=null;
   
   int dir; // duplex= 0, recv-only= -1, send-only= +1; 

   SipdroidSocket socket=null;
   
   //Modify by zzhan 2014-11-04
   private RtpStreamSender_group sender_group = null;
   private RtpStreamSender_signal sender_signal = null;
   private RtpStreamReceiver_group receiver_group=null;
   private RtpStreamReceiver_signal receiver_signal=null;
   
   //change DTMF
   boolean useDTMF = false;  // zero means not use outband DTMF

private String tag = "JAudioLauncher";
   
   /** Costructs the audio launcher */
//   public JAudioLauncher(RtpStreamSender rtp_sender, RtpStreamReceiver rtp_receiver, Log logger)
//   {  log=logger;
//      sender=rtp_sender;
//      receiver=rtp_receiver;
//   }

   /** Costructs the audio launcher */
   public JAudioLauncher(int local_port, String remote_addr, int remote_port, int direction, String audiofile_in, String audiofile_out, int sample_rate, int sample_size, int frame_size, Log logger, Codecs.Map payload_type, int dtmf_pt)
   {  
	  log=logger;
      frame_rate=sample_rate/frame_size;
      
      //oumogang 
      MyLog.i(tag, "frame_rate = "+frame_size);
      
      useDTMF = (dtmf_pt != 0);
      try
      {
    	 CallRecorder call_recorder = null;
    	 if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean(com.zed3.sipua.ui.Settings.PREF_CALLRECORD,
					com.zed3.sipua.ui.Settings.DEFAULT_CALLRECORD))
    		 call_recorder = new CallRecorder(null,payload_type.codec.samp_rate()); // Autogenerate filename from date. 
    	 //socket=new SipdroidSocket(local_port);//sipdroidsocket 语音端口默认为2100，
    	 socket=new SipdroidSocket(0);//先改为0，让系统为其分配端口，防止端口被占用
         dir=direction;
         
         //Modify by zzhan 2014-11-04
         // single call
         if (dir == 0)
         {  printLog("new audio sender to "+remote_addr+":"+remote_port,LogLevel.MEDIUM);
            //audio_input=new AudioInput();
            
            sender_signal=new RtpStreamSender_signal(true,payload_type,frame_rate,frame_size,socket,remote_addr,remote_port,call_recorder);
            sender_signal.setSyncAdj(2);
            sender_signal.setDTMFpayloadType(dtmf_pt);
            
            receiver_signal=new RtpStreamReceiver_signal(socket,payload_type,call_recorder);
         } 
         // gourp call
         else {
            printLog("new audio receiver on "+local_port,LogLevel.MEDIUM);
         	sender_group=new RtpStreamSender_group(true,payload_type,frame_rate,frame_size,socket,remote_addr,remote_port,call_recorder);
         	sender_group.setSyncAdj(2);
            sender_group.setDTMFpayloadType(dtmf_pt);
            
            receiver_group=new RtpStreamReceiver_group(socket,payload_type,call_recorder);
            
         }
      }
      catch (Exception e) {
    	  printException(e,LogLevel.HIGH); 
    	  e.printStackTrace();
      }
   }
     
   	//Added by zzhan 2011-10-26
	//mode : duplex= 0, recv-only= -1, send-only= +1; 
	public boolean startMedia(int mode)
	{
	   if (mode != 0 && (sender_group == null || receiver_group == null))
		   return false;
	   
	   MyLog.e(tag, "starting java audio.." + mode);

	   switch (mode){
	   		//duplex
	   case 0:
//		   sender_signal.SndResume();
//		   receiver_signal.RcvResume();
		   break;
		   //recv only
	   case -1:
		   sender_group.SndSuspend();
		   receiver_group.RcvResume();
		   break;
		   //send only
	   case 1:
		   sender_group.SndResume();
		   receiver_group.RcvSuspend();
		   break;
	   }
	   
	   //Modified by zzhan 2011-10-26
	   if (sender_signal != null && !sender_signal.isRunning() && mode == 0){
		   MyLog.e(tag, "start sending");
			   sender_signal.start();
	   } 

	   if (receiver_signal != null && !receiver_signal.isRunning() && mode == 0){
		   MyLog.e(tag, "start receiving");
		   receiver_signal.start();
	   }
	   if(sender_group != null && !sender_group.isRunning() && mode != 0){
		   sender_group.start();
//		   receiver_group.startBackgroudAfterThreadStarting();
	   }
	   if(receiver_group != null && !receiver_group.isRunning() && mode != 0){
		   receiver_group.start();
		   //receiver_group.startBackgroudAfterThreadStarting();
	   }
	   return true;      
	}

   /** Stops media application */
   public boolean stopMedia()
   {  printLog("halting java audio..",LogLevel.HIGH);    
      if (sender_group!=null)
      {  sender_group.halt(); 
      		//Add by zzhan 2013-5-13
			try {
				//interrupt add by oumogang 2014-03-25
				sender_group.interrupt();
				sender_group.join(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MyLog.e(tag, "sender join exception : " + e.toString());
			}
		
			sender_group=null;	
			MyLog.e(tag, "sender halted");
      }      
      if (receiver_group!=null)
      {  
    	  //receiver_group.stopBackgroudBeforeThreadStopping();
    	  receiver_group.halt();
	      	//Add by zzhan 2013-5-13
			try {
				//interrupt add by oumogang 2014-03-25
				receiver_group.interrupt();
				receiver_group.join(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MyLog.e(tag, "receiver join exception : " + e.toString());
			}
			receiver_group=null;
			MyLog.e(tag, "receiver halted");
      }   
      if (sender_signal!=null)
      {  sender_signal.halt(); 
      		//Add by zzhan 2013-5-13
			try {
				//interrupt add by oumogang 2014-03-25
				sender_signal.interrupt();
				sender_signal.join(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MyLog.e(tag, "sender join exception : " + e.toString());
			}
		
			sender_signal=null;	
			MyLog.e(tag, "sender halted");
      }      
      if (receiver_signal!=null)
      {  
    	  receiver_signal.halt();
	      	//Add by zzhan 2013-5-13
			try {
				//interrupt add by oumogang 2014-03-25
				receiver_signal.interrupt();
				receiver_signal.join(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MyLog.e(tag, "receiver join exception : " + e.toString());
			}
			receiver_signal=null;
			MyLog.e(tag, "receiver halted");
      }
      if (socket != null)
    	  socket.close();
      return true;
   }

   public boolean muteMedia()
   {
	   if(Receiver.GetCurUA().IsPttMode()){
		   if (sender_group != null)
			   return sender_group.mute();
	   }else if(sender_signal != null){
		   return sender_signal.mute();
	   }
	   return false;
   }
   
   public int speakerMedia(int mode)
   {
	   if(Receiver.GetCurUA().IsPttMode()){
	   if (receiver_group != null)
		   return receiver_group.speaker(mode);
	   }else if (receiver_signal != null)
		   return receiver_signal.speaker(mode);
	   return 0;
   }

   public void bluetoothMedia()
   {
	   if (receiver_group != null)
		   receiver_group.bluetooth();
	   if(receiver_signal != null){
		   receiver_signal.bluetooth();
	   }
   }

   //change DTMF
	/** Send outband DTMF packets **/
  public boolean sendDTMF(char c){
	    if (! useDTMF) return false;
	    if(sender_signal != null){
	    	sender_signal.sendDTMF(c);
	    }
	    return true;
  }
  
   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,LogLevel.HIGH);
   }

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {
	  if (Sipdroid.release) return;
	  if (log!=null) log.println("AudioLauncher: "+str,level+SipStack.LOG_LEVEL_UA);  
      if (level<=LogLevel.HIGH) System.out.println("AudioLauncher: "+str);
   }

   /** Adds the Exception message to the default Log */
   void printException(Exception e,int level)
   { 
	  if (Sipdroid.release) return;
	  if (log!=null) log.printException(e,level+SipStack.LOG_LEVEL_UA);
      if (level<=LogLevel.HIGH) e.printStackTrace();
   }

@Override
public boolean startMedia() {
	return false;
}

}