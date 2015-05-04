package com.zed3.video;

import java.util.ArrayList;
import java.util.List;

import org.zoolu.tools.MyLog;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.location.MemoryMg;

public class PhoneSupportTest {
	private String[] getPixList(){
		String mode = Build.MODEL.toLowerCase();
		if(mode.contains("mi") || mode.contains("g716-l070")){
			return new String[]{"384*288","480*320","640*480", "720*480", "1280*720"};
		}else{
			return new String[]{"320*240","352*288","640*480", "720*480", "1280*720"};
		}
	} 
	private static final String tag = "PhoneSupportTest";
	private static List<Size> listSize = null;
	public boolean startTest() {// only test back camera
		Camera camera = null;
		try {
			camera = Camera.open(0);
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		Parameters param = camera.getParameters();
		List<Size> sizeList = getSupportPreViewSize(camera);
		getSupportCodec();
		listSize = sizeList;
		MemoryMg.getInstance().SupportVideoSizeStr = getSupportSizeList();
		if (sizeList == null || sizeList.size() < 1)
			return false;
		for (Size size : sizeList) {
			int width = size.width;
			int height = size.height;
			if (camera != null) {
				try {
					param.setPreviewSize(height, width);
					camera.setParameters(param);
					return true;
				} catch (Exception e) {
					// e.printStackTrace();
					return false;
				} finally {
					closeCamera(camera);
				}
			}
		}
		return true;
	}

	private void closeCamera(Camera camera) {
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}
	private void getSupportCodec(){
		String codecInfo = "";
		 ArrayList<CodecInfo> codecs = new ArrayList<CodecInfo>(CodecInfo.getSupportedCodecs());
	        if(codecs != null && codecs.size() > 0){
	        	for(CodecInfo codec:codecs){
	        		String codecStr = codec.toString();
	        		Log.e("Ht", "\n"+codecStr);
	        		codecInfo += codecStr;
	        	}
	        }
	        if(!TextUtils.isEmpty(codecInfo)){
	        	DeviceVideoInfo.isCodecK3 = codecInfo.toLowerCase().contains("k3");
	        }
	}
	private List<Size> getSupportPreViewSize(Camera camera) {
		if (camera == null) {
			MyLog.e(tag, "camera is not open!");
			throw new NullPointerException();
		} else {
			Parameters param = camera.getParameters();
			return param.getSupportedPreviewSizes();
		}
	}

	public static final int ColorFormat_NV21 = 0;
	public static final int ColorFormat_I420 = 1;
	public static int[][] ColorFormatList = {

			{
					ColorFormat_NV21,
					MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar },
			{ ColorFormat_I420,
					MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar } };

	public static int getEncodeSupportColor() {
		int color = -1;
		for(int i = 0;i<MediaCodecList.getCodecCount();i++){
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			if(codecInfo.isEncoder()){
				MediaCodecInfo.CodecCapabilities capabilities = codecInfo
						.getCapabilitiesForType("video/avc");
				if(capabilities == null) continue;
				int length = capabilities.colorFormats.length;
				for(int j = 0;j<length;j++){
					int format = capabilities.colorFormats[j];
					if(format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar){
						return ColorFormat_NV21;
					}else if(format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar){
						return ColorFormat_I420;
					}
				}
			}else{
				continue;
			}
			
		}
		return color;
	}
	public String getSupportSizeList(){
		if(listSize == null || listSize.size() < 1){
			return "";
		}
		String result = "";
		for(String s:getPixList()){
			for(Size size:listSize){
				String str = size.width+"*"+size.height;
				if(s.equalsIgnoreCase(str)){
					result += s+",";
					continue;
				}
			}
		}
		if(result.length() > 0){
			result = result.substring(0, result.length()-1);
		};
		return result;
	}
}
