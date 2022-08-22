/*
 * Created on 2005-5-7
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.protocol.access.sgip.pdu;


import com.protocol.access.sgip.sms.ByteBuffer;
import com.protocol.access.sgip.sms.PDUException;
import com.protocol.access.sgip.SgipConstant;

/**
 * @author lucien
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TerminateResp extends Response {

	public TerminateResp() {
		super(SgipConstant.SGIP_UNBIND_RESP);
	}

	/* (non-Javadoc)
	 * @see cmpp.sms.ByteData#setData(cmpp.sms.util.ByteBuffer)
	 */
	public void setData(ByteBuffer buffer) throws PDUException {
		header.setData(buffer);
		
	}

	/* (non-Javadoc)
	 * @see cmpp.sms.ByteData#getData()
	 */
	public ByteBuffer getData() {
		return header.getData();
	}
	
	public String name() {
		return "SGIP UnbindResp";
	}
}
