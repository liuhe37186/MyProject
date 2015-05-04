////////////////////////////////////////////////////////////////////////////////
// H264��֡FU��Ƭ RTP��װ
////////////////////////////////////////////////////////////////////////////////

//H264 nalͷ
typedef struct {
	//byte 0
	unsigned char TYPE:5;
	unsigned char NRI:2;
	unsigned char F:1;    

} NALU_HEADER; /**//* 1 BYTES */

//FU indicator
typedef struct {
	//byte 0
	unsigned char TYPE:5;
	unsigned char NRI:2; 
	unsigned char F:1;
} FU_INDICATOR; /**//* 1 BYTES */

//FU header
typedef struct {
	//byte 0
	unsigned char TYPE:5;
	unsigned char R:1;
	unsigned char E:1;
	unsigned char S:1;    
} FU_HEADER; /**//* 1 BYTES */

/*
 * �÷��� ��H264�ĵ�֡����FU��Ƭ
 * inbuffer ��H264��֡
 * len ��֡����
 * timestamp ��ʱ���������RTPЭ��ͷTimeStamp��
 * payloadType ��RTP�������ͣ�����RTPЭ��ͷPayloadType��
 */
void RtpStack::transmitH264FU(char* inbuffer, int len, u_int32_t timestamp, int payloadType)
{
	//����FU��Ƭ���Ƚ���1400�ֽڣ���̫��MTUΪ1500bytes��
#define FRANGMENT_BUG_SIZE 1400
	int marker = 0;

	//����H264��֡ <= 1400�ֽ�ʹ��һ��RTP������
	if(len <= FRANGMENT_BUG_SIZE)
	{
		marker = 1;
		//RtpPacket C++������RTPЭ���װ�������������Ϊ��H264��֡����RTP��װ������ͬ��
		RtpPacket *packet = new RtpPacket;
		packet->encode(inbuffer,len,timestamp,0,payloadType,m_lastSeq++,marker);
		transmit(packet);
	}
	//����H264��֡ > 1400�ֽ�,�Ը�֡ʹ��FU��Ƭ��ʹ�ö��RTP�����͡�����RTPʱ�����ͬ�����кŵ���
	else
	{
		int k = 0;
		int l = 0;
		k = len / FRANGMENT_BUG_SIZE;
		l = len % FRANGMENT_BUG_SIZE;
		int t = 0; 
		while(t <= k)
		{
			NALU_HEADER *pNaluHdr = (NALU_HEADER *)inbuffer;
			//��һƬ
			if(!t)
			{
				char fu[FRANGMENT_BUG_SIZE + 1];
				memcpy_s(fu + 1, FRANGMENT_BUG_SIZE, inbuffer, FRANGMENT_BUG_SIZE);

				//���� FU indicator
				FU_INDICATOR *pFuIndicator = (FU_INDICATOR *)&fu[0];
				pFuIndicator->F = pNaluHdr->F;
				pFuIndicator->NRI = pNaluHdr->NRI;
				pFuIndicator->TYPE = 28;

				//���� FU header
				FU_HEADER *pFUHdr = (FU_HEADER *)&fu[1];
				pFUHdr->E = 0;
				pFUHdr->R = 0;
				pFUHdr->S = 1;
				pFUHdr->TYPE = pNaluHdr->TYPE;

				//��һƬ��RTPЭ��ͷ��mark�ֶ�Ϊ0.
				marker = 0;
				RtpPacket *packet = new RtpPacket;
				packet->encode(fu,FRANGMENT_BUG_SIZE + 1,timestamp,0,payloadType,m_lastSeq++,marker);
				transmit(packet);
			}
			//���һƬ
			else if(k == t && l > 0)
			{
				char *fu = new char[l + 2];
				memcpy_s(fu + 2, l, inbuffer + t * FRANGMENT_BUG_SIZE, l);
				//Set FU indicator
				FU_INDICATOR *pFuIndicator = (FU_INDICATOR *)&fu[0];
				pFuIndicator->F = pNaluHdr->F;
				pFuIndicator->NRI = pNaluHdr->NRI;
				pFuIndicator->TYPE = 28;

				//Set FU header
				FU_HEADER *pFUHdr = (FU_HEADER *)&fu[1];
				pFUHdr->E = 1;
				pFUHdr->R = 0;
				pFUHdr->S = 0;
				pFUHdr->TYPE = pNaluHdr->TYPE;

				//���һƬ��RTPЭ��ͷ��mark�ֶ�Ϊ1.
				marker=1;
				RtpPacket *packet = new RtpPacket;
				packet->encode(fu,l + 2,timestamp,0,payloadType,m_lastSeq++,marker);
				transmit(packet);
				delete[] fu;
			}
			//�м�Ƭ
			else if(t < k && 0 != t)
			{
				char fu[FRANGMENT_BUG_SIZE + 2];
				memcpy_s(fu + 2, FRANGMENT_BUG_SIZE, inbuffer + t * FRANGMENT_BUG_SIZE, FRANGMENT_BUG_SIZE);

				//Set FU indicator
				FU_INDICATOR *pFuIndicator = (FU_INDICATOR *)&fu[0];
				pFuIndicator->F = pNaluHdr->F;
				pFuIndicator->NRI = pNaluHdr->NRI;
				pFuIndicator->TYPE = 28;

				//Set FU header
				FU_HEADER *pFUHdr = (FU_HEADER *)&fu[1];
				pFUHdr->E = 0;
				pFUHdr->R = 0;
				pFUHdr->S = 0;
				pFUHdr->TYPE = pNaluHdr->TYPE;

				//�м�Ƭ��RTPЭ��ͷ��mark�ֶ�Ϊ0.
				marker=0;
				RtpPacket *packet = new RtpPacket;
				packet->encode(fu,FRANGMENT_BUG_SIZE + 2,timestamp,0,payloadType,m_lastSeq++,marker);
				transmit(packet);
			}
			t++;
		}
	}
}


typedef enum _fu_type
{
	FU_TYPE_START = 0,
	FU_TYPE_MIDDLE,
	FU_TYPE_END,
	FU_TYPE_INVALID = -1
}FU_TYPE;


////////////////////////////////////////////////////////////////////////////////
// H264FU��Ƭ ��ϵ�֡
////////////////////////////////////////////////////////////////////////////////
//FU�ṹ�壬�����洢��Ƭ���h264֡
typedef struct _fu
{
	FU_TYPE type;
	string data;
	//֡��ʱ���
	unsigned int timeStamp;
	unsigned int seqNumOrig;
	//��ǰ��Ƭ�����к�
	unsigned int seqNumReconstruct;
	unsigned int len;

	_fu()
	{
		type = FU_TYPE_INVALID;
		timeStamp = 0;
		seqNumOrig = 0;
		seqNumReconstruct = 0;
		len = 0;
		if (len > 0)
			data.erase(0, len);
	}

	const char *GetData()
	{
		return data.c_str();
	}

	int GetDataLen()
	{
		return len;
	}
}FU;

/*
 * �÷���Ϊ��H264��FU��Ƭ������ϣ����typeΪ28�İ�������Ƭ���������Ͱ�ֱ�Ӷ�RTP���ؽ��뼴�ɣ�
 * rtppack ��rtp���ݰ�
 * ����ֵ �� FU�ṹ��ָ��
 */
FU *H264FUManager2::ProcessFU(RtpPacket *rtppack)
{
	unsigned char *pPayload = (unsigned char *)rtppack->payloadBuffer();
	unsigned int startBit = pPayload[1] & 0x80;
	unsigned int endBit = pPayload[1] & 0x40;

	FU * pfuGet = NULL;
	//���Ϊ��һ����Ƭ
	if (startBit)
	{
		//�´���һ��FU�ṹ�壬�洢��Ƭ��Ϻ��H264֡
		FU *pfu = new FU;
		pfu->timeStamp = rtppack->hdr().timeStamp;
		pfu->seqNumOrig = rtppack->hdr().seqNum;
		pfu->seqNumReconstruct = pfu->seqNumOrig;

		// For these NALUs, the first two bytes are the FU indicator and the FU header.
		// If the start bit is set, we reconstruct the original NAL header:
		
		//�ϲ�H264 nalͷ��������һƬ����FU��
		char nalhr;
		nalhr = ((pPayload[0] & 0xE0) + (pPayload[1] & 0x1F));
		pfu->data.append(&nalhr, 1);
		pfu->data.append((char *)(pPayload + 2), rtppack->payloadLength() - 2);
		pfu->len += (rtppack->payloadLength()) - 1;

		//��FU�ŵ�һ��������
		m_listFuSet.push_back(pfu);
	}

	//�м�Ƭ�����һƬ
	else
	{
		//�������ϵ�����FU
		list<FU *>::iterator it = m_listFuSet.begin();
		while (it != m_listFuSet.end())
		{
			//��������е�FU��ʱ��� < ���յ������ݰ���ʱ�������ԭ����FUɾ��
			if ((*it)->timeStamp < rtppack->hdr().timeStamp)
			{
				delete (*it);
				*it = NULL;
				it = m_listFuSet.erase(it);
				continue;
			}
			//����ҵ�FU��ʱ��������յ���FU��Ƭ��ʱ���һ��
			else if ((*it)->timeStamp == rtppack->hdr().timeStamp)
			{
				//������յ��ķ�Ƭ�����к� == ��һ����Ƭ�����к� + 1
				if (((*it)->seqNumReconstruct + 1) == rtppack->hdr().seqNum)
				{
					unsigned char *pPayload = (unsigned char *)rtppack->payloadBuffer();
					(*it)->data.append((char *)&pPayload[2], rtppack->payloadLength() - 2);
					(*it)->seqNumReconstruct = rtppack->hdr().seqNum;
					(*it)->len += rtppack->payloadLength() - 2;

					//��������һƬ���ǽ����ص�ǰ��FU������������֡�������ϣ�
					if (endBit)
						pfuGet = (*it);
				}
				//����Ļ��Ӽ�����ɾ����ǰFU
				else
				{
					delete (*it);
					*it = NULL;
					it = m_listFuSet.erase(it);
					continue;
				}
				break;
			}
			else
			{
				//do nothing
			}
			++it;
		}
	}

	return pfuGet;
}
