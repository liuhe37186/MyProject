package org.zoolu.tools;

import java.io.Serializable;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.message.Message;

public class InCallInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3744607845079809322L;

	public Call call = null;
	public NameAddress callee = null;
	public NameAddress caller = null;
	public String sdp = "";
	public Message invite = null;
	
}
