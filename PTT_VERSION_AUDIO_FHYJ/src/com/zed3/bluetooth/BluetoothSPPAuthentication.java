package com.zed3.bluetooth;

import com.zed3.utils.Tea;

import android.R.integer;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;


public class BluetoothSPPAuthentication implements Runnable{

	private static final String TAG = "BluetoothSPPAuthentication";
	private int radom;
	java.util.Random mRandom = new java.util.Random();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String address = "zm04";
//		getCiphertext();
		radom = 0;
		int[] key1 = getSecretKey(address,1,radom);
		int[] key2 = getSecretKey(address,2,radom);
		
		byte[] text1 = getSecretText(address,1,radom);
		byte[] text2 = getSecretText(address,2,radom);
	}

	public byte[] getSecretText(String bluetoothName, int type, int radom) {
		// TODO Auto-generated method stub
		if (type != 1 && type != 2) {
			throw new RuntimeException("bad type error "+type+"type should be 1 or 2");
		}
		
		//bluetoothAddress
		byte[] text = new byte[8];
		switch (type) {
		case 1:
			//1.ȡ���������������4�ֽڣ����Ƴ��Ȳ���4�ֽڵģ���0��
			if (bluetoothName.length()<4) {
				for (int i = 0; i < 4 - bluetoothName.length(); i++) {
					bluetoothName += "0";
				}
			}
			//2.ȡ�����1���벽��1�õ���4�ֽ�ƴ��8�ֽ����ġ�ƴ������������£�
			byte[] nameBytes = bluetoothName.getBytes();
			for (int i = 0; i < 4; i++) {
				text[2*i] = nameBytes[i];
				text[2*i+1] = (byte) (radom>>(24-8*i));
			}
			break;
		case 2:
			
			//����2����������ȡ���������������8�ֽڡ����Ƴ��Ȳ���8�ֽڵģ���0x55��

			//16���ֽڣ�6+6+4
			//����������ַ�ֽ�
			break;

		default:
			break;
		}
		return text;
	}

	/**
	 * 
	 * @param phoneAddress        98:D6:F7:3A:30:16
	 * @param bluetoothAddress    00:13:8A:20:04:7F
	 * @param type
	 * @param radom
	 * @return
	 */
	public int[] getSecretKey(String bluetoothAddress, int type,
			int radom) {
		// TODO Auto-generated method stub
		if (type != 1 && type != 2) {
			throw new RuntimeException("bad type error "+type+"type should be 1 or 2");
		}
		if (bluetoothAddress.length() != 17) {
			throw new RuntimeException("bad bluetoothAddress error "+bluetoothAddress);
		}
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			throw new RuntimeException(" unsupport bluetooth error ");
		}
		String phoneAddress = bluetoothAdapter.getAddress();
		phoneAddress = "11:22:33:44:55:66";
		

		//bluetoothAddress
		byte[] key = new byte[16];
		switch (type) {
		case 1:
			radom = (char)mRandom.nextInt();
			//16���ֽڣ�6+6+4
			//�ֻ�������ַ�ֽ�
			String[] phoneStrings = phoneAddress.split(":");
			byte[] phoneBytes = new byte[6];
			for (int i = 0; i < 6; i++) {
				phoneBytes[i] = (byte) Integer.parseInt(phoneStrings[i], 16);
			}
			//����������ַ�ֽ�
			String[] bluetoothStrings = bluetoothAddress.split(":");
			byte[] bluetoothBytes = new byte[6];
			for (int i = 0; i < 6; i++) {
				bluetoothBytes[i] = (byte) Integer.parseInt(bluetoothStrings[i], 16);
			}
			//1.�ֻ�������ַ�����������ַ���ֽ���򣬵õ�6���ֽڡ�
			for (int i = 0; i < bluetoothStrings.length; i++) {
				key[i] = (byte) (phoneBytes[i]^bluetoothBytes[i]);
			}
			//2.�ֻ�������ַ���������������ַ���ֽ���򣬵õ�6���ֽڡ�
			for (int i = 0; i < bluetoothStrings.length; i++) {
				key[i+6] = (byte) (phoneBytes[5-i]^bluetoothBytes[i]);
			}
			//3.����������ַ��4���ֽڡ�
			for (int i = 0; i < 4; i++) {
				key[i+12] = (byte) (bluetoothBytes[2+i]);
			}
			break;
		case 2:
			//16���ֽڣ�6+6+4
			//�ֻ�������ַ�ֽ�
			phoneStrings = phoneAddress.split(":");
			phoneBytes = new byte[6];
			for (int i = 0; i < 6; i++) {
				phoneBytes[i] = (byte) Integer.parseInt(phoneStrings[i], 16);
			}
			//����������ַ�ֽ�
			bluetoothStrings = bluetoothAddress.split(":");
			bluetoothBytes = new byte[6];
			for (int i = 0; i < 6; i++) {
				bluetoothBytes[i] = (byte) Integer.parseInt(bluetoothStrings[i], 16);
			}
			//1.�ֻ�������ַ�����������ַ���ֽ���򣬵õ�6���ֽڡ�
			for (int i = 0; i < bluetoothStrings.length; i++) {
				key[i] = (byte) (phoneBytes[i]^bluetoothBytes[i]);
			}
			//2.�ֻ�������ַ���������������ַ���ֽ���򣬵õ�6���ֽڡ�
			for (int i = 0; i < bluetoothStrings.length; i++) {
				key[i+6] = (byte) (phoneBytes[5-i]^bluetoothBytes[i]);
			}
			//3.�����1��4���ֽڡ�
			for (int i = 0; i < 4; i++) {
				key[i+12] = (byte) (radom>>(24-8*i));
			}
			break;

		default:
			break;
		}
		return byteToInt(key);
	}

	private byte[] intToByte(int[] content) {
		// TODO Auto-generated method stub
		Tea tea = new Tea();
		return tea.intToByte(content, 0);
	}
	private int[] byteToInt(byte[] key) {
		// TODO Auto-generated method stub
		Tea tea = new Tea();
		return tea.byteToInt(key, 0);
	}
	
	private void testTea() {
		// TODO Auto-generated method stub
		BluetoothSPPAuthentication au = new BluetoothSPPAuthentication();
		int radom = /*Integer.parseInt("0x10203040", 16)*/0x10203040;
		Log.i(TAG, "testTea() radom = "+"0x10203040");
		String bluetoothAddress = "77:88:99:aa:bb:cc";
		Log.i(TAG, "testTea() bluetoothAddress = "+bluetoothAddress);
		int[] key1 = au.getSecretKey(bluetoothAddress, 1, radom);
		byte[] secretText1 = au.getSecretText(bluetoothAddress, 1, radom);
		String text1 = new String(secretText1/*,Charset.forName("")*/);
		Log.i(TAG, "testTea() text1 = "+text1);
		
	}

}
