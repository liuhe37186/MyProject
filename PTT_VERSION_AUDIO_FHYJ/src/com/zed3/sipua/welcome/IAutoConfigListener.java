package com.zed3.sipua.welcome;

public interface IAutoConfigListener {
	public void TimeOut();//���ӷ�������ʱ
	public void FetchConfigFailed();//����404
//	public void FetchConfigFailed401();//����401 //http ���ֵ����Ҫ
//	public void FetchConfigSuccess();//����200 // �������Ҫ
	public void ParseConfigOK();
	/**
	 * �����������������ݴ���
	 */
	public void parseFailed();
}
