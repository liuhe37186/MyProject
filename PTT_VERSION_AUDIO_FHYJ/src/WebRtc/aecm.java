package WebRtc;

public class aecm {
	public native void aecm_init(int nsmode);
	public native short[] aecm_process( short[] far_frame, short[] near_frame, int echotail);
	public native short[] aecm_processnoise( short[] far_frame, short[] near_frame, int echotail);
	public native short[] aecm_onlyneardata(short[] near_frame, int echotail);
	public native short[] aecm_onlyneardatanoise(short[] near_frame, int echotail);
	public native void aecm_uinit();
	public native short[] setfar(int num);
//	public native void WritetoNative(short[] array, int size);
	
	static{
		
		System.loadLibrary("aecm_jni");
	}
}
