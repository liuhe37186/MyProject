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

package com.zed3.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketOptions;
import java.net.UnknownHostException;

import org.zoolu.tools.MyLog;

import com.zed3.net.impl.OSNetworkSystem;
import com.zed3.net.impl.PlainDatagramSocketImpl;

public class SipdroidSocket extends DatagramSocket {

	PlainDatagramSocketImpl impl;
	public static boolean loaded = false;
	
	public SipdroidSocket(int port) throws SocketException, UnknownHostException {
		super(!loaded?port:0);
		if (loaded) {
			impl = new PlainDatagramSocketImpl();
			impl.create();
			impl.bind(port,InetAddress.getByName("0"));
		}
	}
	
	public void close() {
		super.close();
		if (loaded) impl.close();
	}
	
	public void setSoTimeout(int val) throws SocketException {
		if (loaded) impl.setOption(SocketOptions.SO_TIMEOUT, val);
		else super.setSoTimeout(val);
	}
	
	public void receive(DatagramPacket pack) throws IOException {
		if (loaded) impl.receive(pack);
		else super.receive(pack);
	}
	
	public void send(DatagramPacket pack) throws IOException {
		//Modify by zzhan 2013-5-5
		/*
		if (loaded) impl.send(pack);
		else super.send(pack);
		*/
		try {
			if (loaded) impl.send(pack);
			else super.send(pack);
		} catch (IllegalArgumentException e) {
			// 2.2+ seems to add this exception for address
			// connection problems
			MyLog.i("SipdroidSocket", "send function exception:" +e.toString());
			throw new IOException(e.toString());
		}
	}
	
	public boolean isConnected() {
		if (loaded) return true;
		else return super.isConnected();
	}
	
	public void disconnect() {
		if (!loaded) super.disconnect();
	}
	
	public void connect(InetAddress addr,int port) {
		if (!loaded) super.connect(addr,port);
	}

	static {
		String runTimeValue = getVM();
		System.out.println("-----runTimeValue:"+runTimeValue);
		if(runTimeValue.contains("ART")){
		}else{
			try {
				System.out.println("-----static:"+runTimeValue);
		        System.loadLibrary("OSNetworkSystem");
		        OSNetworkSystem.getOSNetworkSystem().oneTimeInitialization(true);
		        SipdroidSocket.loaded = true;
			} catch (Throwable e) {
			}
		}
			
	}
	private static final String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
    private static final String LIB_DALVIK = "libdvm.so";
    private static final String LIB_ART = "libart.so";
    private static final String LIB_ART_D = "libartd.so";
	public static String getVM(){
		try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            try {
                Method get = systemProperties.getMethod("get",
                   String.class, String.class);
                if (get == null) {
                    return "null";
                }
                try {
                    final String value = (String)get.invoke(
                        systemProperties, SELECT_RUNTIME_PROPERTY,
                        /* Assuming default is */"Dalvik");
                    if (LIB_DALVIK.equals(value)) {
                        return "Dalvik";
                    } else if (LIB_ART.equals(value)) {
                        return "ART";
                    } else if (LIB_ART_D.equals(value)) {
                        return "ART debug build";
                    }
 
                    return value;
                } catch (IllegalAccessException e) {
                    return "IllegalAccessException";
                } catch (IllegalArgumentException e) {
                    return "IllegalArgumentException";
                } catch (InvocationTargetException e) {
                    return "InvocationTargetException";
                }
            } catch (NoSuchMethodException e) {
                return "SystemProperties.get(String key, String def) method is not found";
            }
        } catch (ClassNotFoundException e) {
            return "SystemProperties class is not found";
        }
		
	}
}
