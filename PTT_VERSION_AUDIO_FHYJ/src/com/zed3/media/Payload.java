package com.zed3.media;

import java.io.Serializable;

public class Payload implements Serializable {
	byte[] payload;
	public Payload(byte[] payload) {
		// TODO Auto-generated constructor stub
		this.payload = payload;
	}
	public byte[] getPayload() {
		return payload;
	}
}
