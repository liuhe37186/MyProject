LOCAL_PATH := $(call my-dir)
SPEEX	:= speex-1.2rc1
SILK     := silk
############################## opencore-amr-dir #############################

OC_BASE := $(LOCAL_PATH)/opencore-amr/opencore

AMR_BASE := $(OC_BASE)/codecs_v2/audio/gsm_amr

DEC_DIR := $(AMR_BASE)/amr_nb/dec
ENC_DIR := $(AMR_BASE)/amr_nb/enc
COMMON_DIR := $(AMR_BASE)/amr_nb/common
DEC_SRC_DIR := $(DEC_DIR)/src
ENC_SRC_DIR := $(ENC_DIR)/src
COMMON_SRC_DIR := $(COMMON_DIR)/src
OSCL := $(LOCAL_PATH)/opencore-amr/oscl
#############################################################
include $(CLEAR_VARS)
LOCAL_MODULE    := OSNetworkSystem
LOCAL_SRC_FILES := OSNetworkSystem.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := speex_jni
LOCAL_SRC_FILES := speex_jni.cpp \
		$(SPEEX)/libspeex/speex.c \
		$(SPEEX)/libspeex/speex_callbacks.c \
		$(SPEEX)/libspeex/bits.c \
		$(SPEEX)/libspeex/modes.c \
		$(SPEEX)/libspeex/nb_celp.c \
		$(SPEEX)/libspeex/exc_20_32_table.c \
		$(SPEEX)/libspeex/exc_5_256_table.c \
		$(SPEEX)/libspeex/exc_5_64_table.c \
		$(SPEEX)/libspeex/exc_8_128_table.c \
		$(SPEEX)/libspeex/exc_10_32_table.c \
		$(SPEEX)/libspeex/exc_10_16_table.c \
		$(SPEEX)/libspeex/filters.c \
		$(SPEEX)/libspeex/quant_lsp.c \
		$(SPEEX)/libspeex/ltp.c \
		$(SPEEX)/libspeex/lpc.c \
		$(SPEEX)/libspeex/lsp.c \
		$(SPEEX)/libspeex/vbr.c \
		$(SPEEX)/libspeex/gain_table.c \
		$(SPEEX)/libspeex/gain_table_lbr.c \
		$(SPEEX)/libspeex/lsp_tables_nb.c \
		$(SPEEX)/libspeex/cb_search.c \
		$(SPEEX)/libspeex/vq.c \
		$(SPEEX)/libspeex/window.c \
		$(SPEEX)/libspeex/high_lsp_tables.c

LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SPEEX)/include
LOCAL_CFLAGS = -DFIXED_POINT -DEXPORT="" -UHAVE_CONFIG_H -I$(LOCAL_PATH)/$(SPEEX)/include
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
BV16     := bx16_fixedp
LOCAL_MODULE    := bv16_jni
LOCAL_SRC_FILES := bv16_jni.cpp \
	$(BV16)/bvcommon/a2lsp.c \
	$(BV16)/bvcommon/allpole.c \
	$(BV16)/bvcommon/allzero.c  \
	$(BV16)/bvcommon/autocor.c \
	$(BV16)/bvcommon/basop32.c \
	$(BV16)/bvcommon/cmtables.c \
	$(BV16)/bvcommon/levdur.c \
	$(BV16)/bvcommon/lsp2a.c \
	$(BV16)/bvcommon/mathtables.c \
	$(BV16)/bvcommon/mathutil.c \
	$(BV16)/bvcommon/memutil.c \
	$(BV16)/bvcommon/ptdec.c \
	$(BV16)/bvcommon/stblzlsp.c \
	$(BV16)/bvcommon/utility.c \
	$(BV16)/bvcommon/vqdecode.c \
	$(BV16)/bv16/bitpack.c \
	$(BV16)/bv16/bv.c \
	$(BV16)/bv16/coarptch.c \
	$(BV16)/bv16/decoder.c \
	$(BV16)/bv16/encoder.c \
	$(BV16)/bv16/excdec.c \
	$(BV16)/bv16/excquan.c \
	$(BV16)/bv16/fineptch.c \
	$(BV16)/bv16/g192.c \
	$(BV16)/bv16/gaindec.c \
	$(BV16)/bv16/gainquan.c \
	$(BV16)/bv16/levelest.c \
	$(BV16)/bv16/lspdec.c \
	$(BV16)/bv16/lspquan.c \
	$(BV16)/bv16/plc.c \
	$(BV16)/bv16/postfilt.c \
	$(BV16)/bv16/preproc.c \
	$(BV16)/bv16/ptquan.c \
	$(BV16)/bv16/tables.c 
	
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(BV16)/bvcommon $(LOCAL_PATH)/$(BV16)/bv16 $(LOCAL_PATH)/$(BV16)
#LOCAL_CFLAGS = -O3 -marm -march=armv6 -mtune=arm1136j-s -DWMOPS=0 -DG192BITSTREAM=0
#LOCAL_CFLAGS = -O3 -DWMOPS=0 -DG192BITSTREAM=0
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
SILK     := silk
LOCAL_MODULE    := silkcommon
LOCAL_SRC_FILES :=  $(SILK)/src/SKP_Silk_A2NLSF.c \
	$(SILK)/src/SKP_Silk_CNG.c \
	$(SILK)/src/SKP_Silk_HP_variable_cutoff_FIX.c \
	$(SILK)/src/SKP_Silk_LBRR_reset.c \
	$(SILK)/src/SKP_Silk_LPC_inv_pred_gain.c \
	$(SILK)/src/SKP_Silk_LPC_stabilize.c \
	$(SILK)/src/SKP_Silk_LPC_synthesis_filter.c \
	$(SILK)/src/SKP_Silk_LPC_synthesis_order16.c \
	$(SILK)/src/SKP_Silk_LP_variable_cutoff.c \
	$(SILK)/src/SKP_Silk_LSF_cos_table.c \
	$(SILK)/src/SKP_Silk_LTP_analysis_filter_FIX.c \
	$(SILK)/src/SKP_Silk_LTP_scale_ctrl_FIX.c \
	$(SILK)/src/SKP_Silk_MA.c \
	$(SILK)/src/SKP_Silk_NLSF2A.c \
	$(SILK)/src/SKP_Silk_NLSF2A_stable.c \
	$(SILK)/src/SKP_Silk_NLSF_MSVQ_decode.c \
	$(SILK)/src/SKP_Silk_NLSF_MSVQ_encode_FIX.c \
	$(SILK)/src/SKP_Silk_NLSF_VQ_rate_distortion_FIX.c \
	$(SILK)/src/SKP_Silk_NLSF_VQ_sum_error_FIX.c \
	$(SILK)/src/SKP_Silk_NLSF_VQ_weights_laroia.c \
	$(SILK)/src/SKP_Silk_NLSF_stabilize.c \
	$(SILK)/src/SKP_Silk_NSQ.c \
	$(SILK)/src/SKP_Silk_NSQ_del_dec.c \
	$(SILK)/src/SKP_Silk_PLC.c \
	$(SILK)/src/SKP_Silk_VAD.c \
	$(SILK)/src/SKP_Silk_VQ_nearest_neighbor_FIX.c \
	$(SILK)/src/SKP_Silk_allpass_int.c \
	$(SILK)/src/SKP_Silk_ana_filt_bank_1.c \
	$(SILK)/src/SKP_Silk_apply_sine_window.c \
	$(SILK)/src/SKP_Silk_array_maxabs.c \
	$(SILK)/src/SKP_Silk_autocorr.c \
	$(SILK)/src/SKP_Silk_biquad.c \
	$(SILK)/src/SKP_Silk_biquad_alt.c \
	$(SILK)/src/SKP_Silk_burg_modified.c \
	$(SILK)/src/SKP_Silk_bwexpander.c \
	$(SILK)/src/SKP_Silk_bwexpander_32.c \
	$(SILK)/src/SKP_Silk_code_signs.c \
	$(SILK)/src/SKP_Silk_control_codec_FIX.c \
	$(SILK)/src/SKP_Silk_corrMatrix_FIX.c \
	$(SILK)/src/SKP_Silk_create_init_destroy.c \
	$(SILK)/src/SKP_Silk_dec_API.c \
	$(SILK)/src/SKP_Silk_decode_core.c \
	$(SILK)/src/SKP_Silk_decode_frame.c \
	$(SILK)/src/SKP_Silk_decode_indices_v4.c \
	$(SILK)/src/SKP_Silk_decode_parameters.c \
	$(SILK)/src/SKP_Silk_decode_parameters_v4.c \
	$(SILK)/src/SKP_Silk_decode_pulses.c \
	$(SILK)/src/SKP_Silk_decoder_set_fs.c \
	$(SILK)/src/SKP_Silk_detect_SWB_input.c \
	$(SILK)/src/SKP_Silk_enc_API.c \
	$(SILK)/src/SKP_Silk_encode_frame_FIX.c \
	$(SILK)/src/SKP_Silk_encode_parameters.c \
	$(SILK)/src/SKP_Silk_encode_parameters_v4.c \
	$(SILK)/src/SKP_Silk_encode_pulses.c \
	$(SILK)/src/SKP_Silk_find_LPC_FIX.c \
	$(SILK)/src/SKP_Silk_find_LTP_FIX.c \
	$(SILK)/src/SKP_Silk_find_pitch_lags_FIX.c \
	$(SILK)/src/SKP_Silk_find_pred_coefs_FIX.c \
	$(SILK)/src/SKP_Silk_gain_quant.c \
	$(SILK)/src/SKP_Silk_init_encoder_FIX.c \
	$(SILK)/src/SKP_Silk_inner_prod_aligned.c \
	$(SILK)/src/SKP_Silk_interpolate.c \
	$(SILK)/src/SKP_Silk_k2a.c \
	$(SILK)/src/SKP_Silk_k2a_Q16.c \
	$(SILK)/src/SKP_Silk_lin2log.c \
	$(SILK)/src/SKP_Silk_log2lin.c \
	$(SILK)/src/SKP_Silk_lowpass_int.c \
	$(SILK)/src/SKP_Silk_lowpass_short.c \
	$(SILK)/src/SKP_Silk_noise_shape_analysis_FIX.c \
	$(SILK)/src/SKP_Silk_pitch_analysis_core.c \
	$(SILK)/src/SKP_Silk_pitch_est_tables.c \
	$(SILK)/src/SKP_Silk_prefilter_FIX.c \
	$(SILK)/src/SKP_Silk_process_NLSFs_FIX.c \
	$(SILK)/src/SKP_Silk_process_gains_FIX.c \
	$(SILK)/src/SKP_Silk_pulses_to_bytes.c \
	$(SILK)/src/SKP_Silk_quant_LTP_gains_FIX.c \
	$(SILK)/src/SKP_Silk_range_coder.c \
	$(SILK)/src/SKP_Silk_regularize_correlations_FIX.c \
	$(SILK)/src/SKP_Silk_resample_1_2.c \
	$(SILK)/src/SKP_Silk_resample_1_2_coarse.c \
	$(SILK)/src/SKP_Silk_resample_1_2_coarsest.c \
	$(SILK)/src/SKP_Silk_resample_1_3.c \
	$(SILK)/src/SKP_Silk_resample_2_1_coarse.c \
	$(SILK)/src/SKP_Silk_resample_2_3.c \
	$(SILK)/src/SKP_Silk_resample_2_3_coarse.c \
	$(SILK)/src/SKP_Silk_resample_2_3_coarsest.c \
	$(SILK)/src/SKP_Silk_resample_2_3_rom.c \
	$(SILK)/src/SKP_Silk_resample_3_1.c \
	$(SILK)/src/SKP_Silk_resample_3_2.c \
	$(SILK)/src/SKP_Silk_resample_3_2_rom.c \
	$(SILK)/src/SKP_Silk_resample_3_4.c \
	$(SILK)/src/SKP_Silk_resample_4_3.c \
	$(SILK)/src/SKP_Silk_residual_energy16_FIX.c \
	$(SILK)/src/SKP_Silk_residual_energy_FIX.c \
	$(SILK)/src/SKP_Silk_scale_copy_vector16.c \
	$(SILK)/src/SKP_Silk_scale_vector.c \
	$(SILK)/src/SKP_Silk_schur.c \
	$(SILK)/src/SKP_Silk_schur64.c \
	$(SILK)/src/SKP_Silk_shell_coder.c \
	$(SILK)/src/SKP_Silk_sigm_Q15.c \
	$(SILK)/src/SKP_Silk_solve_LS_FIX.c \
	$(SILK)/src/SKP_Silk_sort.c \
	$(SILK)/src/SKP_Silk_sum_sqr_shift.c \
	$(SILK)/src/SKP_Silk_tables_LTP.c \
	$(SILK)/src/SKP_Silk_tables_NLSF_CB0_10.c \
	$(SILK)/src/SKP_Silk_tables_NLSF_CB0_16.c \
	$(SILK)/src/SKP_Silk_tables_NLSF_CB1_10.c \
	$(SILK)/src/SKP_Silk_tables_NLSF_CB1_16.c \
	$(SILK)/src/SKP_Silk_tables_gain.c \
	$(SILK)/src/SKP_Silk_tables_other.c \
	$(SILK)/src/SKP_Silk_tables_pitch_lag.c \
	$(SILK)/src/SKP_Silk_tables_pulses_per_block.c \
	$(SILK)/src/SKP_Silk_tables_sign.c \
	$(SILK)/src/SKP_Silk_tables_type_offset.c
	
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS = -O3 
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SILK)/src $(LOCAL_PATH)/$(SILK)/interface
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := silk8_jni
LOCAL_SRC_FILES := silk8_jni.cpp 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SILK)/src $(LOCAL_PATH)/$(SILK)/interface
LOCAL_CFLAGS = -O3 
LOCAL_STATIC_LIBRARIES :=  silkcommon
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := silk16_jni
LOCAL_SRC_FILES := silk16_jni.cpp 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SILK)/src $(LOCAL_PATH)/$(SILK)/interface
LOCAL_CFLAGS = -O3 
LOCAL_STATIC_LIBRARIES :=  silkcommon
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := silk24_jni
LOCAL_SRC_FILES := silk24_jni.cpp 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SILK)/src $(LOCAL_PATH)/$(SILK)/interface
LOCAL_CFLAGS = -O3 
LOCAL_STATIC_LIBRARIES :=  silkcommon
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
SPANDSP     := spandsp
LOCAL_MODULE    := g722_jni
LOCAL_SRC_FILES := g722_jni.cpp \
	$(SPANDSP)/g722.c \
	$(SPANDSP)/vector_int.c
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SPANDSP)/spandsp $(LOCAL_PATH)/$(SPANDSP)
LOCAL_CFLAGS = -O3
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
SPANDSP     := spandsp
LOCAL_MODULE    := gsm_jni
LOCAL_SRC_FILES := gsm_jni.cpp \
	$(SPANDSP)/gsm0610_decode.c \
	$(SPANDSP)/gsm0610_encode.c \
	$(SPANDSP)/gsm0610_lpc.c \
	$(SPANDSP)/gsm0610_preprocess.c \
	$(SPANDSP)/gsm0610_rpe.c \
	$(SPANDSP)/gsm0610_short_term.c \
	$(SPANDSP)/gsm0610_long_term.c
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SPANDSP)/spandsp $(LOCAL_PATH)/$(SPANDSP)
LOCAL_CFLAGS = -O3
include $(BUILD_SHARED_LIBRARY)


################################   amr-codec-jni    ###################################
################################   amr-jni-encode   ################################
include $(CLEAR_VARS)

LOCAL_MODULE := libencode

amr_dir := amr_jni
LOCAL_SRC_FILES := \
    $(amr_dir)/interf_enc.c \
    $(amr_dir)/sp_enc.c \

LOCAL_C_INCLUDES += $(common_C_INCLUDES)

LOCAL_PRELINK_MODULE := true #false

include $(BUILD_STATIC_LIBRARY)

# Build JNI wrapper
include $(CLEAR_VARS)
LOCAL_ARM_MODE := arm
LOCAL_MODULE := libamr-jni-encode

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \
    $(amr_dir)

LOCAL_SRC_FILES := $(amr_dir)/amr-jni-encode.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

LOCAL_STATIC_LIBRARIES := libencode
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)


################################   amr-jni-decode   ################################

include $(CLEAR_VARS)

LOCAL_MODULE := libdecode

amr_dir := amr_jni
LOCAL_SRC_FILES := \
    $(amr_dir)/interf_dec.c \
    $(amr_dir)/sp_dec.c \


LOCAL_C_INCLUDES += $(common_C_INCLUDES)

LOCAL_PRELINK_MODULE := false

include $(BUILD_STATIC_LIBRARY)

# Build JNI wrapper
include $(CLEAR_VARS)

LOCAL_MODULE := libamr-jni-decode

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \
    $(amr_dir)

LOCAL_SRC_FILES := $(amr_dir)/amr-jni-decode.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

LOCAL_STATIC_LIBRARIES := libdecode
LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

###########################  opencore-amr-decode  ###############################
include $(CLEAR_VARS)
LOCAL_MODULE := libopencore-amr-decode
LOCAL_SRC_FILES :=\
        $(DEC_SRC_DIR)/agc.cpp \
        $(DEC_SRC_DIR)/amrdecode.cpp \
        $(DEC_SRC_DIR)/a_refl.cpp \
        $(DEC_SRC_DIR)/b_cn_cod.cpp \
        $(DEC_SRC_DIR)/bgnscd.cpp \
        $(DEC_SRC_DIR)/c_g_aver.cpp \
        $(DEC_SRC_DIR)/d1035pf.cpp \
        $(DEC_SRC_DIR)/d2_11pf.cpp \
        $(DEC_SRC_DIR)/d2_9pf.cpp \
        $(DEC_SRC_DIR)/d3_14pf.cpp \
        $(DEC_SRC_DIR)/d4_17pf.cpp \
        $(DEC_SRC_DIR)/d8_31pf.cpp \
        $(DEC_SRC_DIR)/dec_amr.cpp \
        $(DEC_SRC_DIR)/dec_gain.cpp \
        $(DEC_SRC_DIR)/dec_input_format_tab.cpp \
        $(DEC_SRC_DIR)/dec_lag3.cpp \
        $(DEC_SRC_DIR)/dec_lag6.cpp \
        $(DEC_SRC_DIR)/d_gain_c.cpp \
        $(DEC_SRC_DIR)/d_gain_p.cpp \
        $(DEC_SRC_DIR)/d_plsf_3.cpp \
        $(DEC_SRC_DIR)/d_plsf_5.cpp \
        $(DEC_SRC_DIR)/d_plsf.cpp \
        $(DEC_SRC_DIR)/dtx_dec.cpp \
        $(DEC_SRC_DIR)/ec_gains.cpp \
        $(DEC_SRC_DIR)/ex_ctrl.cpp \
        $(DEC_SRC_DIR)/if2_to_ets.cpp \
        $(DEC_SRC_DIR)/int_lsf.cpp \
        $(DEC_SRC_DIR)/lsp_avg.cpp \
        $(DEC_SRC_DIR)/ph_disp.cpp \
        $(DEC_SRC_DIR)/post_pro.cpp \
        $(DEC_SRC_DIR)/preemph.cpp \
        $(DEC_SRC_DIR)/pstfilt.cpp \
        $(DEC_SRC_DIR)/qgain475_tab.cpp \
        $(DEC_SRC_DIR)/sp_dec.cpp \
        $(DEC_SRC_DIR)/wmf_to_ets.cpp
LOCAL_C_INCLUDES := \
        $(LOCAL_PATH)/opencore-amr/amrnb
include $(BUILD_SHARED_LIBRARY)

######################      opencore-amr-encode       #########################
include $(CLEAR_VARS)
LOCAL_MODULE := libopencore-amr-encode
LOCAL_SRC_FILES :=\
    $(ENC_SRC_DIR)/amrencode.cpp \
        $(ENC_SRC_DIR)/autocorr.cpp \
        $(ENC_SRC_DIR)/c1035pf.cpp \
        $(ENC_SRC_DIR)/c2_11pf.cpp \
        $(ENC_SRC_DIR)/c2_9pf.cpp \
        $(ENC_SRC_DIR)/c3_14pf.cpp \
        $(ENC_SRC_DIR)/c4_17pf.cpp \
        $(ENC_SRC_DIR)/c8_31pf.cpp \
        $(ENC_SRC_DIR)/calc_cor.cpp \
        $(ENC_SRC_DIR)/calc_en.cpp \
        $(ENC_SRC_DIR)/cbsearch.cpp \
        $(ENC_SRC_DIR)/cl_ltp.cpp \
        $(ENC_SRC_DIR)/cod_amr.cpp \
        $(ENC_SRC_DIR)/convolve.cpp \
        $(ENC_SRC_DIR)/cor_h.cpp \
        $(ENC_SRC_DIR)/cor_h_x2.cpp \
        $(ENC_SRC_DIR)/cor_h_x.cpp \
        $(ENC_SRC_DIR)/corrwght_tab.cpp \
        $(ENC_SRC_DIR)/div_32.cpp \
        $(ENC_SRC_DIR)/dtx_enc.cpp \
        $(ENC_SRC_DIR)/enc_lag3.cpp \
        $(ENC_SRC_DIR)/enc_lag6.cpp \
        $(ENC_SRC_DIR)/enc_output_format_tab.cpp \
        $(ENC_SRC_DIR)/ets_to_if2.cpp \
        $(ENC_SRC_DIR)/ets_to_wmf.cpp \
        $(ENC_SRC_DIR)/g_adapt.cpp \
        $(ENC_SRC_DIR)/gain_q.cpp \
        $(ENC_SRC_DIR)/g_code.cpp \
        $(ENC_SRC_DIR)/g_pitch.cpp \
        $(ENC_SRC_DIR)/hp_max.cpp \
        $(ENC_SRC_DIR)/inter_36.cpp \
        $(ENC_SRC_DIR)/inter_36_tab.cpp \
        $(ENC_SRC_DIR)/l_abs.cpp \
        $(ENC_SRC_DIR)/lag_wind.cpp \
        $(ENC_SRC_DIR)/lag_wind_tab.cpp \
        $(ENC_SRC_DIR)/l_comp.cpp \
        $(ENC_SRC_DIR)/levinson.cpp \
        $(ENC_SRC_DIR)/l_extract.cpp \
        $(ENC_SRC_DIR)/lflg_upd.cpp \
        $(ENC_SRC_DIR)/l_negate.cpp \
        $(ENC_SRC_DIR)/lpc.cpp \
        $(ENC_SRC_DIR)/ol_ltp.cpp \
        $(ENC_SRC_DIR)/pitch_fr.cpp \
        $(ENC_SRC_DIR)/pitch_ol.cpp \
        $(ENC_SRC_DIR)/p_ol_wgh.cpp \
        $(ENC_SRC_DIR)/pre_big.cpp \
        $(ENC_SRC_DIR)/pre_proc.cpp \
        $(ENC_SRC_DIR)/prm2bits.cpp \
        $(ENC_SRC_DIR)/qgain475.cpp \
        $(ENC_SRC_DIR)/qgain795.cpp \
        $(ENC_SRC_DIR)/q_gain_c.cpp \
        $(ENC_SRC_DIR)/q_gain_p.cpp \
        $(ENC_SRC_DIR)/qua_gain.cpp \
        $(ENC_SRC_DIR)/s10_8pf.cpp \
        $(ENC_SRC_DIR)/set_sign.cpp \
        $(ENC_SRC_DIR)/sid_sync.cpp \
        $(ENC_SRC_DIR)/sp_enc.cpp \
        $(ENC_SRC_DIR)/spreproc.cpp \
        $(ENC_SRC_DIR)/spstproc.cpp \
        $(ENC_SRC_DIR)/ton_stab.cpp \
        $(ENC_SRC_DIR)/vad1.cpp
        
LOCAL_C_INCLUDES := \
        $(LOCAL_PATH)/opencore-amr/amrnb
include $(BUILD_SHARED_LIBRARY)

###########################      opencore-amr-common      #############################
include $(CLEAR_VARS)
LOCAL_MODULE := libopencore-amr-common
LOCAL_SRC_FILES :=\
    $(COMMON_SRC_DIR)/add.cpp \
        $(COMMON_SRC_DIR)/az_lsp.cpp \
        $(COMMON_SRC_DIR)/bitno_tab.cpp \
        $(COMMON_SRC_DIR)/bitreorder_tab.cpp \
        $(COMMON_SRC_DIR)/c2_9pf_tab.cpp \
        $(COMMON_SRC_DIR)/div_s.cpp \
        $(COMMON_SRC_DIR)/extract_h.cpp \
        $(COMMON_SRC_DIR)/extract_l.cpp \
        $(COMMON_SRC_DIR)/gains_tbl.cpp \
        $(COMMON_SRC_DIR)/gc_pred.cpp \
        $(COMMON_SRC_DIR)/get_const_tbls.cpp \
        $(COMMON_SRC_DIR)/gmed_n.cpp \
        $(COMMON_SRC_DIR)/gray_tbl.cpp \
        $(COMMON_SRC_DIR)/grid_tbl.cpp \
        $(COMMON_SRC_DIR)/int_lpc.cpp \
        $(COMMON_SRC_DIR)/inv_sqrt.cpp \
        $(COMMON_SRC_DIR)/inv_sqrt_tbl.cpp \
        $(COMMON_SRC_DIR)/l_deposit_h.cpp \
        $(COMMON_SRC_DIR)/l_deposit_l.cpp \
        $(COMMON_SRC_DIR)/log2.cpp \
        $(COMMON_SRC_DIR)/log2_norm.cpp \
        $(COMMON_SRC_DIR)/log2_tbl.cpp \
        $(COMMON_SRC_DIR)/lsfwt.cpp \
        $(COMMON_SRC_DIR)/l_shr_r.cpp \
        $(COMMON_SRC_DIR)/lsp_az.cpp \
        $(COMMON_SRC_DIR)/lsp.cpp \
        $(COMMON_SRC_DIR)/lsp_lsf.cpp \
        $(COMMON_SRC_DIR)/lsp_lsf_tbl.cpp \
        $(COMMON_SRC_DIR)/lsp_tab.cpp \
        $(COMMON_SRC_DIR)/mult_r.cpp \
        $(COMMON_SRC_DIR)/negate.cpp \
        $(COMMON_SRC_DIR)/norm_l.cpp \
        $(COMMON_SRC_DIR)/norm_s.cpp \
        $(COMMON_SRC_DIR)/overflow_tbl.cpp \
        $(COMMON_SRC_DIR)/ph_disp_tab.cpp \
        $(COMMON_SRC_DIR)/pow2.cpp \
        $(COMMON_SRC_DIR)/pow2_tbl.cpp \
        $(COMMON_SRC_DIR)/pred_lt.cpp \
        $(COMMON_SRC_DIR)/q_plsf_3.cpp \
        $(COMMON_SRC_DIR)/q_plsf_3_tbl.cpp \
        $(COMMON_SRC_DIR)/q_plsf_5.cpp \
        $(COMMON_SRC_DIR)/q_plsf_5_tbl.cpp \
        $(COMMON_SRC_DIR)/q_plsf.cpp \
        $(COMMON_SRC_DIR)/qua_gain_tbl.cpp \
        $(COMMON_SRC_DIR)/reorder.cpp \
        $(COMMON_SRC_DIR)/residu.cpp \
        $(COMMON_SRC_DIR)/round.cpp \
        $(COMMON_SRC_DIR)/set_zero.cpp \
        $(COMMON_SRC_DIR)/shr.cpp \
        $(COMMON_SRC_DIR)/shr_r.cpp \
        $(COMMON_SRC_DIR)/sqrt_l.cpp \
        $(COMMON_SRC_DIR)/sqrt_l_tbl.cpp \
        $(COMMON_SRC_DIR)/sub.cpp \
        $(COMMON_SRC_DIR)/syn_filt.cpp \
        $(COMMON_SRC_DIR)/weight_a.cpp \
        $(COMMON_SRC_DIR)/window_tab.cpp

include $(BUILD_SHARED_LIBRARY)

