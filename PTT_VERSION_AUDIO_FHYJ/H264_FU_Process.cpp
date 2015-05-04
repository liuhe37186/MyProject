////////////////////////////////////////////////////////////////////////////////
// H264单帧FU分片 RTP封装
////////////////////////////////////////////////////////////////////////////////

//H264 nal头
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
 * 该方法 对H264的单帧进行FU分片
 * inbuffer ：H264单帧
 * len ：帧长度
 * timestamp ：时间戳（用于RTP协议头TimeStamp）
 * payloadType ：RTP负载类型（用于RTP协议头PayloadType）
 */
void RtpStack::transmitH264FU(char* inbuffer, int len, u_int32_t timestamp, int payloadType)
{
	//定义FU分片长度界限1400字节（以太网MTU为1500bytes）
#define FRANGMENT_BUG_SIZE 1400
	int marker = 0;

	//对于H264单帧 <= 1400字节使用一个RTP包发送
	if(len <= FRANGMENT_BUG_SIZE)
	{
		marker = 1;
		//RtpPacket C++类用于RTP协议封装，以下三句代码为对H264单帧进行RTP封装。（下同）
		RtpPacket *packet = new RtpPacket;
		packet->encode(inbuffer,len,timestamp,0,payloadType,m_lastSeq++,marker);
		transmit(packet);
	}
	//对于H264单帧 > 1400字节,对该帧使用FU分片，使用多个RTP包发送。其中RTP时间戳相同，序列号递增
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
			//第一片
			if(!t)
			{
				char fu[FRANGMENT_BUG_SIZE + 1];
				memcpy_s(fu + 1, FRANGMENT_BUG_SIZE, inbuffer, FRANGMENT_BUG_SIZE);

				//设置 FU indicator
				FU_INDICATOR *pFuIndicator = (FU_INDICATOR *)&fu[0];
				pFuIndicator->F = pNaluHdr->F;
				pFuIndicator->NRI = pNaluHdr->NRI;
				pFuIndicator->TYPE = 28;

				//设置 FU header
				FU_HEADER *pFUHdr = (FU_HEADER *)&fu[1];
				pFUHdr->E = 0;
				pFUHdr->R = 0;
				pFUHdr->S = 1;
				pFUHdr->TYPE = pNaluHdr->TYPE;

				//第一片的RTP协议头的mark字段为0.
				marker = 0;
				RtpPacket *packet = new RtpPacket;
				packet->encode(fu,FRANGMENT_BUG_SIZE + 1,timestamp,0,payloadType,m_lastSeq++,marker);
				transmit(packet);
			}
			//最后一片
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

				//最后一片的RTP协议头的mark字段为1.
				marker=1;
				RtpPacket *packet = new RtpPacket;
				packet->encode(fu,l + 2,timestamp,0,payloadType,m_lastSeq++,marker);
				transmit(packet);
				delete[] fu;
			}
			//中间片
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

				//中间片的RTP协议头的mark字段为0.
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
// H264FU分片 组合单帧
////////////////////////////////////////////////////////////////////////////////
//FU结构体，用来存储合片后的h264帧
typedef struct _fu
{
	FU_TYPE type;
	string data;
	//帧的时间戳
	unsigned int timeStamp;
	unsigned int seqNumOrig;
	//当前分片的序列号
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
 * 该方法为对H264的FU分片进行组合（针对type为28的包进行组片，其余类型包直接对RTP负载解码即可）
 * rtppack ：rtp数据包
 * 返回值 ： FU结构体指针
 */
FU *H264FUManager2::ProcessFU(RtpPacket *rtppack)
{
	unsigned char *pPayload = (unsigned char *)rtppack->payloadBuffer();
	unsigned int startBit = pPayload[1] & 0x80;
	unsigned int endBit = pPayload[1] & 0x40;

	FU * pfuGet = NULL;
	//如果为第一个分片
	if (startBit)
	{
		//新创建一个FU结构体，存储分片组合后的H264帧
		FU *pfu = new FU;
		pfu->timeStamp = rtppack->hdr().timeStamp;
		pfu->seqNumOrig = rtppack->hdr().seqNum;
		pfu->seqNumReconstruct = pfu->seqNumOrig;

		// For these NALUs, the first two bytes are the FU indicator and the FU header.
		// If the start bit is set, we reconstruct the original NAL header:
		
		//合并H264 nal头，并将第一片存入FU中
		char nalhr;
		nalhr = ((pPayload[0] & 0xE0) + (pPayload[1] & 0x1F));
		pfu->data.append(&nalhr, 1);
		pfu->data.append((char *)(pPayload + 2), rtppack->payloadLength() - 2);
		pfu->len += (rtppack->payloadLength()) - 1;

		//将FU放到一个集合中
		m_listFuSet.push_back(pfu);
	}

	//中间片或最后一片
	else
	{
		//遍历集合的所有FU
		list<FU *>::iterator it = m_listFuSet.begin();
		while (it != m_listFuSet.end())
		{
			//如果集合中的FU的时间戳 < 刚收到的数据包的时间戳，则将原来的FU删除
			if ((*it)->timeStamp < rtppack->hdr().timeStamp)
			{
				delete (*it);
				*it = NULL;
				it = m_listFuSet.erase(it);
				continue;
			}
			//如果找到FU的时间戳与新收到的FU分片的时间戳一致
			else if ((*it)->timeStamp == rtppack->hdr().timeStamp)
			{
				//如果新收到的分片的序列号 == 上一个分片的序列号 + 1
				if (((*it)->seqNumReconstruct + 1) == rtppack->hdr().seqNum)
				{
					unsigned char *pPayload = (unsigned char *)rtppack->payloadBuffer();
					(*it)->data.append((char *)&pPayload[2], rtppack->payloadLength() - 2);
					(*it)->seqNumReconstruct = rtppack->hdr().seqNum;
					(*it)->len += rtppack->payloadLength() - 2;

					//如果是最后一片，那将返回当前的FU（表明完整的帧已组合完毕）
					if (endBit)
						pfuGet = (*it);
				}
				//否则的话从集合中删除当前FU
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
