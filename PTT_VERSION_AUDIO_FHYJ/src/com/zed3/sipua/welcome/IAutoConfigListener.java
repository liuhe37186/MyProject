package com.zed3.sipua.welcome;

public interface IAutoConfigListener {
	public void TimeOut();//连接服务器超时
	public void FetchConfigFailed();//返回404
//	public void FetchConfigFailed401();//返回401 //http 这个值不需要
//	public void FetchConfigSuccess();//返回200 // 这个不需要
	public void ParseConfigOK();
	/**
	 * 解析服务器返回数据错误
	 */
	public void parseFailed();
}
