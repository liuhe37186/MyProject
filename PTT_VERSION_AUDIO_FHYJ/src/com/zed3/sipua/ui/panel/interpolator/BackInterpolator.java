package com.zed3.sipua.ui.panel.interpolator;

/*
 *
 * Open source under the BSD License. 
 * 
 * Copyright © 2001 Robert Penner
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list 
 * of conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution.
 * 
 * Neither the name of the author nor the names of contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE. 
 *
 */


import android.view.animation.Interpolator;

public class BackInterpolator implements Interpolator {

	private int type;
	private float overshot;

	public BackInterpolator(int type, float overshot) {
		this.type = type;
		this.overshot = overshot;
	}

	public float getInterpolation(float t) {
		if (type == EasingType.IN) {
			return in(t, overshot);
		} else
		if (type == EasingType.OUT) {
			return out(t, overshot);
		} else
		if (type == EasingType.INOUT) {
			return inout(t, overshot);
		}
		return 0;
	}

	private float in(float t, float o) {
		if (o == 0) {
			o = 1.70158f;
		}
		return t*t*((o+1)*t - o);
	}

	private float out(float t, float o) {
		if (o == 0) {
			o = 1.70158f;
		}
		return ((t-=1)*t*((o+1)*t + o) + 1);
	}
	
	private float inout(float t, float o) {
		if (o == 0) {
			o = 1.70158f;
		}
		t *= 2;
		if (t < 1) {
			return 0.5f*(t*t*(((o*=(1.525))+1)*t - o));
		} else {
			return 0.5f*((t-=2)*t*(((o*=(1.525))+1)*t + o) + 2);
		}
	}
}
