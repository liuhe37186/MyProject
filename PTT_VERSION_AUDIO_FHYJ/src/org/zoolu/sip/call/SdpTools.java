/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 * Nitin Khanna, Hughes Systique Corp. (Reason: Android specific change, optmization, bug fix) 
 */

package org.zoolu.sip.call;

import java.util.Enumeration;
import java.util.Vector;

import org.zoolu.sdp.AttributeField;
import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.tools.MyLog;

import com.zed3.codecs.CodecBase;
import com.zed3.location.MemoryMg;
import com.zed3.sipua.ui.Receiver;

/**
 * Class SdpTools collects some static methods for managing SDP materials.
 */
public class SdpTools {
	private static CodecBase codecbase;
	private static String tag = "SdpTools";
	private static boolean needLog = true;

	/**
	 * Costructs a new SessionDescriptor from a given SessionDescriptor with
	 * olny media types and attribute values specified by a MediaDescriptor
	 * Vector.
	 * <p>
	 * If no attribute is specified for a particular media, all present
	 * attributes are kept. <br>
	 * If no attribute is present for a selected media, the media is kept
	 * (regardless any sepcified attributes).
	 * 
	 * @param sdp
	 *            the given SessionDescriptor
	 * @param m_descs
	 *            Vector of MediaDescriptor with the selecting media types and
	 *            attributes
	 * @return this SessionDescriptor
	 */
	/* HSC CHANGES START */
	public static SessionDescriptor sdpMediaProduct(SessionDescriptor sdp,
			Vector<MediaDescriptor> m_descs) {
		Vector<MediaDescriptor> new_media = new Vector<MediaDescriptor>();
		if (m_descs != null) {
			//当前为组呼话ptt为true 不走此处，组合接听走这里
			//MyLog.e(tag, (UserAgent.ua_ptt_mode==true ? "ptt true":"ptt false"));
			if(Receiver.call_state == 2 )//终端仅做为主叫,sdp协商后没ptime，则默认用20 2==UA_STATE_OUTGOING_CALL
			{
				MemoryMg.SdpPtime = 20;// 终端作主叫，sdp协商后不带ptime 则默认为20
				MyLog.e(tag, "call state OUTGOING_CALL sdpptime* "+MemoryMg.SdpPtime);
			}
			
			for (Enumeration<MediaDescriptor> e = m_descs.elements(); e
					.hasMoreElements();) {
				MediaDescriptor spec_md = e.nextElement();
				// System.out.print("DEBUG: SDP: sdp_select:
				// "+spec_md.toString());
				MediaDescriptor prev_md = sdp.getMediaDescriptor(spec_md
						.getMedia().getMedia());
				// System.out.print("DEBUG: SDP: sdp_origin:
				// "+prev_md.toString());
				if (prev_md != null) {
					Vector<AttributeField> spec_attributes = spec_md
							.getAttributes();
					Vector<AttributeField> prev_attributes = prev_md
							.getAttributes();
					MediaField prev_mf = prev_md.getMedia();
					Vector<String> new_formats = new Vector<String>(prev_mf.getFormatList());
					new_formats.retainAll(spec_md.getMedia().getFormatList());
					
					if (spec_attributes.size() == 0
							|| prev_attributes.size() == 0) {
						new_media.addElement(prev_md);
					} else {
						Vector<AttributeField> new_attributes = new Vector<AttributeField>();
//						//当前为组呼话ptt为true 不走此处，组合接听走这里
//						//MyLog.e(tag, (UserAgent.ua_ptt_mode==true ? "ptt true":"ptt false"));
//						if(Receiver.call_state == 2 )//终端仅做为主叫,sdp协商后没ptime，则默认用20 2==UA_STATE_OUTGOING_CALL
//						{
//							MemoryMg.SdpPtime = 20;// 终端作主叫，sdp协商后不带ptime 则默认为20
//							MyLog.e(tag, "call state OUTGOING_CALL sdpptime* "+MemoryMg.SdpPtime);
//						}
						
						for (Enumeration<AttributeField> i = spec_attributes
								.elements(); i.hasMoreElements();) {
							AttributeField spec_attr = i.nextElement();
							String spec_name = spec_attr.getAttributeName();
							String spec_value = spec_attr.getAttributeValue();
							//MyLog.e("sdptools", "name:"+spec_name+" value:"+spec_value);
							//视频转发会携带此标识
							if(spec_name.equalsIgnoreCase("sendonly")) {
								MemoryMg.getInstance().isSendOnly=true;
							}//不能有else
							
							//终端做为主叫，sdp协商后的ptime值
							if (spec_name.equalsIgnoreCase("ptime")) {
								MemoryMg.SdpPtime = Integer.valueOf(spec_value);
								
								MyLog.e(tag,
										"outgoingcall after sdp ptime value is:"
												+ spec_value);
							}//不能有else
							
							
							if (spec_value == null)
								continue;
							for (Enumeration<AttributeField> k = prev_attributes
									.elements(); k.hasMoreElements();) {//begin for
								AttributeField prev_attr = k.nextElement();
								String prev_name = prev_attr.getAttributeName();
								String prev_value = prev_attr
										.getAttributeValue();
								if (prev_name.equals(spec_name)
										&& prev_value
												.equalsIgnoreCase(spec_value)) {
									new_attributes.addElement(prev_attr);
									
									//amr  add  mode   add by oumogang 2012-12-27
									//a=fmtp:114 mode-set=0
									break;
								}// add by zzhan???????
								else if ((prev_value.contains("H264")
										&& spec_value.contains("H264")
										&& !prev_value.contains("H264S") && !spec_value
											.contains("H264S"))
										|| (prev_value.contains("H264S") && spec_value
												.contains("H264S"))) {
									new_attributes.addElement(spec_attr);
									MyLog.e("cccc4444spec", spec_value);// 137 H264S/90000
									MyLog.e("cccc4444prev", prev_value);
									
									//Add by zzhan 2013-2-21 sdp新增与eyebeam的协商，默认126，若为125则。。。
									String payloadtype = spec_value.substring(0, spec_value.indexOf(" "));
									if (!new_formats.contains(payloadtype)){
										new_formats.add(payloadtype);
									}
									
								}
								else if(prev_value.contains("profile-level-id") && spec_value.contains("profile-level-id")){
									
									String payload = spec_value.substring(0, spec_value.indexOf(" "));
									boolean isH264 = false;
									for (Enumeration<AttributeField> m = spec_attributes
											.elements(); m.hasMoreElements();) {
										AttributeField spec_attr2 = m.nextElement();
										if (spec_attr2.getAttributeName().contains("rtpmap") 
												&& spec_attr2.getAttributeValue().contains(payload)
												&& spec_attr2.getAttributeValue().contains("H264"))
										{
											//if(spec_attr2.getAttributeValue().contains("H264S"))
											//	break;
											//prev 137  spec 126
											MyLog.e("h264h264s", "prev:"+prev_value+" spec:"+spec_value);
											
											if (!prev_value
													.substring(
															0,
															prev_value
																	.indexOf(" "))
													.equals(spec_value
															.substring(
																	0,
																	spec_value
																			.indexOf(" "))))
												break;
											isH264 = true;
											break;
										}
									}
									if (isH264)
									{
										String fmtpValue = payload + prev_value.substring(prev_value.indexOf(" "));
										new_attributes.addElement(new AttributeField(prev_name, fmtpValue));
										MyLog.e("cccc5555", prev_name);//fmtp
										MyLog.e("cccc56666", fmtpValue);//126 profile-level-id=42e00b;cif=1;fps=10
										
									}
								}
							}//end for
							
							MyLog.e(tag, "sdptool ptime-1:"+MemoryMg.SdpPtime);
						}//end for
						MediaField new_mf = new MediaField(prev_mf.getMedia(), prev_mf.getPort(), 0,
								prev_mf.getTransport(), new_formats);
						if (new_attributes.size() > 0)
							new_media.addElement(new MediaDescriptor(new_mf, prev_md.getConnection(),
									new_attributes));
				        else {
			                if(new_mf.getMedia().startsWith("audio") && new_formats.size() > 0) {
			                        new_media.addElement(new MediaDescriptor(new_mf, prev_md.getConnection(),
			                                new_attributes)); // new_attributes is empty but this is ok here.
			                }
				        }
					}
				}
			}
		}
		MyLog.e("cccc1", sdp.toString());
		SessionDescriptor new_sdp = new SessionDescriptor(sdp);
		new_sdp.removeMediaDescriptors();
		new_sdp.addMediaDescriptors(new_media);
		MyLog.e("cccc2", new_sdp.toString());
		return new_sdp;
	}

	/* HSC CHANGES END */
	/**
	 * Costructs a new SessionDescriptor from a given SessionDescriptor with
	 * olny the first specified media attribute. /** Keeps only the fisrt
	 * attribute of the specified type for each media.
	 * <p>
	 * If no attribute is present for a media, the media is dropped.
	 * 
	 * @param sdp
	 *            the given SessionDescriptor
	 * @param a_name
	 *            the attribute name
	 * @return this SessionDescriptor
	 */
	/* HSC CHANGES START */
	public static SessionDescriptor sdpAttirbuteSelection(
			SessionDescriptor sdp, String a_name) {
		Vector<MediaDescriptor> new_media = new Vector<MediaDescriptor>();
		for (Enumeration<MediaDescriptor> e = sdp.getMediaDescriptors()
				.elements(); e.hasMoreElements();) {
			/* HSC CHANGES END */
			MediaDescriptor md = e.nextElement();
			AttributeField attr = md.getAttribute(a_name);
			if (attr != null) {
				new_media.addElement(new MediaDescriptor(md.getMedia(), md
						.getConnection(), attr));
			}
		}
		SessionDescriptor new_sdp = new SessionDescriptor(sdp);
		new_sdp.removeMediaDescriptors();
		new_sdp.addMediaDescriptors(new_media);
		return new_sdp;
	}

}
