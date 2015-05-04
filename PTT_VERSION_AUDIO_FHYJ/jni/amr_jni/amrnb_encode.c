static AmrEncState* enc_init(void) {
	AmrEncState *s = (AmrEncState*) malloc(sizeof(AmrEncState));
	if(s==NULL) {
		return	NULL;
	}
	memset(s, 0, sizeof(EncState));
	s->dtx = 0;
	s->req_mode = MR122;
	return s;
}

static void enc_uninit(AmrEncState *s){
	if(s!=NULL) {
		free(s);
	}
}

static void enc_preprocess(AmrEncState *s) {
	s->amr_enc = Encoder_Interface_init(s->dtx);
}

static void enc_postprocess(AmrEncState *s) {
	Encoder_Interface_exit(s->amr_enc);
	s->amr_enc = NULL;	
}

static int enc_set_file(AmrEncState *s, unsigned char *wavstream, int wavstreamlen)
{
	s->wavstream = wavstream;
	s->wavdatasize = wavstreamlen;	
																			
	s->amrdatasize = s->wavdatasize * (block_size[s->req_mode]+1) / (160*2);
	s->amrstreamlen = s->amrdatasize + strlen(AMR_MAGIC_NUMBER);
	s->amrstream = (unsigned char*) malloc(s->amrstreamlen);
	if(s->amrstream==NULL) {
		strcpy(s->message, "Cannot alloc amrstream in enc_set_file\n");
		return -1;
	}		
	memset(s->amrstream, 0, s->amrstreamlen);												
	
	strcpy(s->message, "enc_set_file success!");
	return 0;
}

static void enc_process(AmrEncState *s)
{
	s->curwavstream = s->wavstream;
	s->curamrstream = s->amrstream;
	memcpy(s->curamrstream, AMR_MAGIC_NUMBER, strlen(AMR_MAGIC_NUMBER));
	s->curamrstream += strlen(AMR_MAGIC_NUMBER);
	
	int curwavdatasize = s->wavdatasize;
	int curamrdatasize = s->amrdatasize;
	while(curwavdatasize>=160*2 && curamrdatasize>=(block_size[s->req_mode]+1)) {
		int byte_conter = Encoder_Interface_Encode(s->amr_enc, req_mode, s->curwavstream, s->curamrstream, 0);
		s->curwavstream += 160*2;
		s->curwavdatasize -= 160*2;
		s->curamrstream += byte_conter;
		s->curamrdatasize -= byte_conter;
	}
}

//以wavstream和wavstreamlen，计算amr编码后需要的长度并分配空间，返回amrstream和amrstream的长度
int amr_encode_init(unsigned char *wavstream, int wavstreamlen, AmrEncState **s, unsigned char **amrstream)
{
if(NULL==wavstream || wavstreamlen<=0)	return	-1;
	*s = enc_init();
	if(*s == NULL)	return	-1;		// "enc_init fail!";
		
	if(enc_set_file(*s, wavstream, wavstreamlen) == -1) {
		enc_uninit(*s);
		*s = NULL;
		return	-1;		// mes;
	}	
	
	
	*armstream = (*s)->amrstream;
	return	(*s)->amrstreamlen;
}

void wavencode2amr(AmrEncState *s)
{
	if(NULL==s)	return;
	enc_preprocess(s);	
	enc_process(s);

	enc_postprocess(s);
	enc_uninit(s);
	return;
}