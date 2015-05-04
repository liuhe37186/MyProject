FU *H264FUManager::ProcessFU(RtpPacket *rtppack)
{
	unsigned char *pPayload = (unsigned char *)rtppack->payloadBuffer();
	unsigned int startBit = pPayload[1] & 0x80;
	unsigned int endBit = rtppack->hdr().markBit;//??

	FU * pfuGet = NULL;
	//start
	if (startBit)
	{
		FU *pfu = new FU;
		pfu->timeStamp = rtppack->hdr().timeStamp;
		pfu->seqNumOrig = rtppack->hdr().seqNum;
		pfu->seqNumReconstruct = pfu->seqNumOrig;

		//Not need H264 start code,because input filter will add
		//Data copy
		pfu->data.append((char *)pPayload, rtppack->payloadLength());
		pfu->len += rtppack->payloadLength();

		m_listFuSet.push_back(pfu);
	}

	//end and middle
	else
	{
		list<FU *>::iterator it = m_listFuSet.begin();
		while (it != m_listFuSet.end())
		{
			//record the latest data before the comming data
			if ((*it)->timeStamp < rtppack->hdr().timeStamp)
			{
				delete (*it);
				*it = NULL;
				it = m_listFuSet.erase(it);
				continue;
			}

			else if ((*it)->timeStamp == rtppack->hdr().timeStamp)
			{
				if ((*it)->seqNumReconstruct < rtppack->hdr().seqNum)
				{
					(*it)->data.append(3, 0x00);
					(*it)->data.append(1, 0x01);
					(*it)->len += 4;

					unsigned char *pPayload = (unsigned char *)rtppack->payloadBuffer();
					(*it)->data.append((char *)&pPayload[0], rtppack->payloadLength() - 0);
					(*it)->seqNumReconstruct = rtppack->hdr().seqNum;
					(*it)->len += rtppack->payloadLength() - 0;

					//end process
					if (endBit)
						pfuGet = (*it);
				}
				else
				{
					//do nothing
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
