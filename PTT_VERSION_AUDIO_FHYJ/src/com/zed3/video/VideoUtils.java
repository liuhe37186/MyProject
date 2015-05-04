package com.zed3.video;

public class VideoUtils {
	/* NV21: YYYYYYYY VUVU->I420: YYYYYYYY UU VV */
	public static void NV21ToI420pWithRotate90DegreeLeftwise(int width, int height,
			byte[] src, byte[] dst) {
		int i, j;
		int size = width * height;
		int size2 = size * 5 / 4;
		int k = 0;
		int bv;

		for (i = width; i > 0; i--) {
			bv = 0;
			for (j = 0; j < height; j++) {
				dst[k] = src[bv + i - 1];
				bv += width;
				k++;
			}
		}
		k = 0;
		for (i = width; i > 0; i -= 2) {
			bv = 0;
			for (j = 0; j < height / 2; j++) {
				dst[size + k] = src[size + bv + i - 1];
				dst[size2 + k] = src[size + bv + i - 2];
				bv += width;
				k++;
			}
		}

	}

	public static void NV21ToI420pWithRotate90DegreeRightwise(int width, int height,
			byte[] src, byte[] dst) {
		int i, j;
		int size = width * height;
		int size2 = size * 3 / 2;
		int k = 0;
		int maxv;
		int bv;

		maxv = (height - 1) * width;
		for (i = 0; i < width; i++) {
			bv = maxv;
			for (j = height; j > 0; j--) {
				dst[k] = src[bv + i];
				bv -= width;
				k++;
			}
		}
		k = 0;
		maxv = ((height / 2) - 1) * width;
		for (i = 0; i < width; i += 2) {
			bv = maxv;
			for (j = height / 2; j > 0; j--) {
				dst[size + k] = src[size + bv + i + 1];
				dst[size + size / 4 + k] = src[size + bv + i];
				bv -= width;
				k++;
			}
		}

	}

	// -----------------------------
	public static void NV21Rotate90DegreeLeftwise(int width, int height, byte[] src,
			byte[] dst) {
		int i, j;
		int size = width * height;
		int k = 0;
		int bv;

		for (i = width; i > 0; i--) {
			bv = 0;
			for (j = 0; j < height; j++) {
				dst[k] = src[bv + i - 1];
				bv += width;
				k++;
			}
		}

		for (i = width; i > 0; i -= 2) {
			bv = 0;
			for (j = 0; j < height / 2; j++) {
				dst[k++] = src[size + bv + i - 2];
				dst[k++] = src[size + bv + i - 1];
				bv += width;
			}
		}

	}

	public static void NV21Rotate90DegreeRightwise(int width, int height, byte[] src,
			byte[] dst) {
		int i, j;
		int size = width * height;
		int k = 0;
		int maxv;
		int bv;

		maxv = (height - 1) * width;
		for (i = 0; i < width; i++) {
			bv = maxv;
			for (j = height; j > 0; j--) {
				dst[k] = src[bv + i];
				bv -= width;
				k++;
			}
		}
		maxv = ((height / 2) - 1) * width;
		for (i = 0; i < width; i += 2) {
			bv = maxv;
			for (j = height / 2; j > 0; j--) {
				dst[k++] = src[size + bv + i];
				dst[k++] = src[size + bv + i + 1];
				bv -= width;
			}
		}

	}

	public static  void NV21Rotate90DegreeLeftwiseMi(int width, int height,
			byte[] src, byte[] dst) {
		int i, j;
		int size = width * height;
		int k = 0;
		int bv;

		for (i = width; i > 0; i--) {
			bv = 0;
			for (j = 0; j < height; j++) {
				dst[k] = src[bv + i - 1];
				bv += width;
				k++;
			}
		}

		for (i = width; i > 0; i -= 2) {
			bv = 0;
			for (j = 0; j < height / 2; j++) {
				dst[k++] = src[size + bv + i - 1];
				dst[k++] = src[size + bv + i - 2];
				bv += width;
			}
		}

	}

	public static void NV21Rotate90DegreeRightwiseMi(int width, int height,
			byte[] src, byte[] dst) {
		int i, j;
		int size = width * height;
		int k = 0;
		int maxv;
		int bv;

		maxv = (height - 1) * width;
		for (i = 0; i < width; i++) {
			bv = maxv;
			for (j = height; j > 0; j--) {
				dst[k] = src[bv + i];
				bv -= width;
				k++;
			}
		}
		maxv = ((height / 2) - 1) * width;
		for (i = 0; i < width; i += 2) {
			bv = maxv;
			for (j = height / 2; j > 0; j--) {
				dst[k++] = src[size + bv + i + 1];
				dst[k++] = src[size + bv + i];
				bv -= width;
			}
		}

	}
	public static void NV21ToI420pWithRotate180Degree(int width, int height, byte[] src,byte[] dst) {
		int i,j;
		int size = width * height;
		int k=0;
		
	    k=0; 	
	    for( i = size; i >0; i--) {
                dst[k++] = src[i-1];
	        }
	    k=0;
	    for( i = size/2; i >0; i -= 2) {
	       	    dst[size+k] =src[size+ i - 1];  
	            dst[size+size/4+k] =  src[size+ i-2];
		    k++;	
	    }  
		
	}
	public static void NV21ToI420p(int width, int height, byte[] src,byte[] dst) {
		int i,j;
		int size = width * height;
		int k=0;
		
	    k=0; 	
//	    for( i = size; i >0; i--) {
//                dst[k++] = src[i-1];
//	        }
	    System.arraycopy(src, 0, dst, 0, size);
	    k=0;
	    for( i = 0; i <size/2; i += 2) {
	       	    dst[size+k] =src[size+ i + 1];  
	            dst[size+size/4+k] =  src[size+ i];
		    k++;	
	    }  
		
	}

	public static void NV21Rotate180Degree(int width, int height, byte[] src,byte[] dst) {
		int i,j;
		int size = width * height;
		int k=0;
		
	    k=0; 	
	    for( i = size; i >0; i--) {
                dst[k++] = src[i-1];
	        }

	    k=0;
	    for( i = size/2; i >0; i -= 2) {
	            dst[size+k] =src[size+ i -2];  
	            dst[size+k+1] =  src[size+ i -1];
		    k +=2;	
	        }  
		
	}
	public static void NV21Rotate180DegreeMi(int width, int height, byte[] src,byte[] dst) {
		int i,j;
		int size = width * height;
		int k=0;
		
	    k=0; 	
	    for( i = size; i >0; i--) {
                dst[k++] = src[i-1];
	        }

	    k=0;
	    for( i = size/2; i >0; i -= 2) {
	            dst[size+k] = src[size+ i -1];  
	            dst[size+k+1] =  src[size+ i -2];
		    k +=2;	
	        }  
		
	}

	//暂时没有用到
	private void changeUV2(int w, int h, byte[] src) {
		// int i;
		int size = w * h;
		// for (i = 0; i < (size / 2); i += 2) {
		// byte tmp = src[size + i];
		// src[size + i] = src[size + i + 1];
		// src[size + i + 1] = tmp;
		// }
		byte[] temp = new byte[w * h / 4];
		System.arraycopy(src, size, temp, 0, temp.length);
		System.arraycopy(src, size + size / 4, src, size, temp.length);
		System.arraycopy(temp, 0, src, size + size / 4, temp.length);
	}

	byte[] YUV420spRotateNegative90(final byte[] src, int srcWidth, int height) // -90
	{
		byte[] dst = new byte[src.length];
		int nWidth = 0, nHeight = 0;
		int wh = 0;
		int uvHeight = 0;
		if (srcWidth != nWidth || height != nHeight) {
			nWidth = srcWidth;
			nHeight = height;
			wh = srcWidth * height;
			uvHeight = height >> 1;// uvHeight = height / 2
		}

		// 旋转Y
		int k = 0;
		for (int i = 0; i < srcWidth; i++) {
			int nPos = srcWidth - 1;
			for (int j = 0; j < height; j++) {
				dst[k] = src[nPos - i];
				k++;
				nPos += srcWidth;
			}
		}

		for (int i = 0; i < srcWidth; i += 2) {
			int nPos = wh + srcWidth - 1;
			for (int j = 0; j < uvHeight; j++) {
				dst[k] = src[nPos - i - 1];
				dst[k + 1] = src[nPos - i];
				k += 2;
				nPos += srcWidth;
			}
		}

		return dst;
	}

	byte[] YUV420spRotatePositive90(final byte[] src, int srcWidth, int height) // +90
	{
		byte[] dst = new byte[src.length];
		int nWidth = 0, nHeight = 0;
		int wh = 0;
		int uvHeight = 0;
		if (srcWidth != nWidth || height != nHeight) {
			nWidth = srcWidth;
			nHeight = height;
			wh = srcWidth * height;
			uvHeight = height >> 1;// uvHeight = height / 2
		}

		// 旋转Y
		int k = 0;
		for (int i = 0; i < srcWidth; i++) {
			int nPos = (nHeight - 1) * srcWidth;
			for (int j = 0; j < height; j++) {
				dst[k] = src[nPos - i];
				k++;
				nPos -= srcWidth;
			}
		}

		for (int i = 0; i < srcWidth; i += 2) {
			int nPos = wh + srcWidth - 1;
			for (int j = 0; j < uvHeight; j++) {
				dst[k] = src[nPos - i - 1];
				dst[k + 1] = src[nPos - i];
				k += 2;
				nPos += srcWidth;
			}
		}

		return dst;
	}

	void changeNV21ToY420(byte[] src, byte[] dst, int width, int heigth) {
		int i;
		int size = width * heigth;
		i = 0;
		for (i = 0; i < size; i++) {
			dst[i] = src[i];
		}
		for (int j = 0; j < size / 2; j += 2) {
			dst[j + size] = src[size + j + 1];
			dst[j + size / 4] = src[size + j];
		}
	}

	void YUV420spRotateClockwise90ToUV(byte[] des, byte[] src, int width,
			int height) {
		int bindex = 0;
		int size = width * height;
		int bi;
		int bv;
		int k = 0;
		int i, j;
		int maxv;
		bindex = (height - 1);
		maxv = (height - 1) * width;
		for (i = 0; i < width; i++) {
			bv = maxv;
			for (j = bindex; j >= 0; j--) {
				des[k] = src[bv + i];
				bv -= width;
				k++;
			}
		}
		bindex = (height / 2) - 1;
		maxv = ((height / 2) - 1) * width;
		for (i = 0; i < width; i += 2) {
			bv = maxv;
			for (j = bindex; j >= 0; j--) {
				des[k] = src[size + bv + i];
				des[k + 1] = src[size + bv + i + 1];
				bv -= width;
				k += 2;
			}
		}
	}

	void YUV420spRotateClockwise90To420P(byte[] des, byte[] src, int width,
			int height) {
		int bindex = 0;
		int size = width * height;
		int bi;
		int bv;
		int k = 0;
		int i, j;
		int maxv;
		bindex = (height - 1);
		maxv = (height - 1) * width;
		for (i = 0; i < width; i++) {
			bv = maxv;
			for (j = bindex; j >= 0; j--) {
				des[k] = src[bv + i];
				bv -= width;
				k++;
			}
		}
		bindex = (height / 2) - 1;
		maxv = ((height / 2) - 1) * width;

		/**
		 * src[size+i]=tmpUV[2*i+1]; src[size+i+size/4]=tmpUV[2*i];
		 * */
		k = 0;
		for (i = 0; i < width; i += 2) {
			bv = maxv;
			for (j = bindex; j >= 0; j--) {
				des[k + size] = src[size + bv + i + 1];
				des[size + size / 4 + k] = src[size + bv + i];
				bv -= width;
				k++;
			}
		}
	}
	private void yuv420spToyuv420p(int w, int h, byte[] src) {
		int i;
		int size = w * h;
		i = 0;
		// for(i =0;i<size;i++){
		// dst[i] =src[i];
		// }
		byte[] tmpUV = new byte[w * h / 2];
		System.arraycopy(src, size, tmpUV, 0, w * h / 2);
		for (i = 0; i < size / 4; i++) {
			// dst[size+i] =src[size+2*i+1];
			// dst[size+size/4 +i] = src[size+2*i];
			src[size + i] = tmpUV[2 * i + 1];
			src[size + i + size / 4] = tmpUV[2 * i];
		}

	}

	public static void changeUV(int w, int h, byte[] src) {
		int i;
		int size = w * h;
		for (i = 0; i < (size / 2); i += 2) {
			byte tmp = src[size + i];
			src[size + i] = src[size + i + 1];
			src[size + i + 1] = tmp;
		}
	}
}
