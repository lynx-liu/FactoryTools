///////////////////////////////////////////////////
//				date: 2011.12.13
//				author: 刘立向
//				email: llxxhm@126.com
//				qq: 515311445
//				msn: llxxhm@126.com
///////////////////////////////////////////////////

package com.android.factorytest.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

public class QREncode
{
    public static final int QR_LEVEL_L = 0;
    public static final int QR_LEVEL_M = 1;
	public static final int QR_LEVEL_Q = 2;
    public static final int QR_LEVEL_H = 3;

	// データモード
	static final short QR_MODE_NUMERAL = 0;
	static final short QR_MODE_ALPHABET = 1;
	static final short QR_MODE_8BIT = 2;
	static final short QR_MODE_KANJI = 3;

	// バージョン(型番)グループ
	static final int QR_VRESION_S = 0; // 1 ～ 9
	static final int QR_VRESION_M = 1; // 10 ～ 26
	static final int QR_VRESION_L = 2; // 27 ～ 40

	static final int MAX_DATACODEWORD = 2956; // データコードワード最大値(バージョン40-L)
	static final int MAX_ALLCODEWORD = 3706; // 総コードワード数最大値
	static final int MAX_CODEBLOCK = 153; // ブロックデータコードワード数最大値(ＲＳコードワードを含む)

	static final int MAX_MODULESIZE = 177; // 一辺モジュール数最大値

	// ビットマップ描画時マージン
	static final public int QR_MARGIN = 4;

	static class RS_BLOCKINFO
	{
		int ncRSBlock;		// ＲＳブロック数
		int ncAllCodeWord;	// ブロック内コードワード数
		int ncDataCodeWord;	// データコードワード数(コードワード数 - ＲＳコードワード数)
		RS_BLOCKINFO(int ncrsBlock, int ncallcodeWord, int ncdatacodeWord)
		{
			ncRSBlock = ncrsBlock;
			ncAllCodeWord = ncallcodeWord;
			ncDataCodeWord = ncdatacodeWord;
		}
	}
	
	static class QR_VERSIONINFO// QRコードバージョン(型番)関連情報
	{
		int nVersionNo;	   // バージョン(型番)番号(1～40)
		int ncAllCodeWord; // 総コードワード数

		// 以下配列添字は誤り訂正率(0 = L, 1 = M, 2 = Q, 3 = H)
		int ncDataCodeWord[];	// データコードワード数(総コードワード数 - ＲＳコードワード数)

		int ncAlignPoint;	// アライメントパターン座標数
		int nAlignPoint[];	// アライメントパターン中心座標

		RS_BLOCKINFO RS_BlockInfo1[]; // ＲＳブロック情報(1)
		RS_BLOCKINFO RS_BlockInfo2[]; // ＲＳブロック情報(2)
		
		QR_VERSIONINFO(int versionNo, int ncallcodeWord, int ncdatacodeWord[], int ncalignPoint, int nalignPoint[], RS_BLOCKINFO bs_blockInfo1[], RS_BLOCKINFO bs_blockInfo2[])
		{
			nVersionNo = versionNo;
			ncAllCodeWord = ncallcodeWord;
			ncDataCodeWord = ncdatacodeWord;
			ncAlignPoint = ncalignPoint;
			nAlignPoint = nalignPoint;
			RS_BlockInfo1 = bs_blockInfo1;
			RS_BlockInfo2 = bs_blockInfo2;
		}
	}

	// QRコードバージョン(型番)情報
	static QR_VERSIONINFO QR_VersonInfo[] =
	{
			new QR_VERSIONINFO( 0,    0, new int[]{   0,    0,    0,    0},   0, new int[]{  0,   0,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0)}),
			new QR_VERSIONINFO( 1,   26, new int[]{  19,   16,   13,    9},   0, new int[]{  0,   0,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1,  26,  19), new RS_BLOCKINFO(  1,  26,  16), new RS_BLOCKINFO(  1,  26,  13), new RS_BLOCKINFO(  1,  26,   9)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0)}),
			new QR_VERSIONINFO( 2,   44, new int[]{  34,   28,   22,   16},   1, new int[]{ 18,   0,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1,  44,  34), new RS_BLOCKINFO(  1,  44,  28), new RS_BLOCKINFO(  1,  44,  22), new RS_BLOCKINFO(  1,  44,  16)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0)}),
			new QR_VERSIONINFO( 3,   70, new int[]{  55,   44,   34,   26},   1, new int[]{ 22,   0,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1,  70,  55), new RS_BLOCKINFO(  1,  70,  44), new RS_BLOCKINFO(  2,  35,  17), new RS_BLOCKINFO(  2,  35,  13)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0)}),
			new QR_VERSIONINFO( 4,  100, new int[]{  80,   64,   48,   36},   1, new int[]{ 26,   0,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 100,  80), new RS_BLOCKINFO(  2,  50,  32), new RS_BLOCKINFO(  2,  50,  24), new RS_BLOCKINFO(  4,  25,   9)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0)}),
			new QR_VERSIONINFO( 5,  134, new int[]{ 108,   86,   62,   46},   1, new int[]{ 30,   0,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 134, 108), new RS_BLOCKINFO(  2,  67,  43), new RS_BLOCKINFO(  2,  33,  15), new RS_BLOCKINFO(  2,  33,  11)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  2,  34,  16), new RS_BLOCKINFO(  2,  34,  12)}),
			new QR_VERSIONINFO( 6,  172, new int[]{ 136,  108,   76,   60},   1, new int[]{ 34,   0,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2,  86,  68), new RS_BLOCKINFO(  4,  43,  27), new RS_BLOCKINFO(  4,  43,  19), new RS_BLOCKINFO(  4,  43,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0)}),
			new QR_VERSIONINFO( 7,  196, new int[]{ 156,  124,   88,   66},   2, new int[]{ 22,  38,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2,  98,  78), new RS_BLOCKINFO(  4,  49,  31), new RS_BLOCKINFO(  2,  32,  14), new RS_BLOCKINFO(  4,  39,  13)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  4,  33,  15), new RS_BLOCKINFO(  1,  40,  14)}),
			new QR_VERSIONINFO( 8,  242, new int[]{ 194,  154,  110,   86},   2, new int[]{ 24,  42,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2, 121,  97), new RS_BLOCKINFO(  2,  60,  38), new RS_BLOCKINFO(  4,  40,  18), new RS_BLOCKINFO(  4,  40,  14)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  2,  61,  39), new RS_BLOCKINFO(  2,  41,  19), new RS_BLOCKINFO(  2,  41,  15)}),
			new QR_VERSIONINFO( 9,  292, new int[]{ 232,  182,  132,  100},   2, new int[]{ 26,  46,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2, 146, 116), new RS_BLOCKINFO(  3,  58,  36), new RS_BLOCKINFO(  4,  36,  16), new RS_BLOCKINFO(  4,  36,  12)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  2,  59,  37), new RS_BLOCKINFO(  4,  37,  17), new RS_BLOCKINFO(  4,  37,  13)}),
			new QR_VERSIONINFO(10,  346, new int[]{ 274,  216,  154,  122},   2, new int[]{ 28,  50,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2,  86,  68), new RS_BLOCKINFO(  4,  69,  43), new RS_BLOCKINFO(  6,  43,  19), new RS_BLOCKINFO(  6,  43,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2,  87,  69), new RS_BLOCKINFO(  1,  70,  44), new RS_BLOCKINFO(  2,  44,  20), new RS_BLOCKINFO(  2,  44,  16)}),
			new QR_VERSIONINFO(11,  404, new int[]{ 324,  254,  180,  140},   2, new int[]{ 30,  54,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 101,  81), new RS_BLOCKINFO(  1,  80,  50), new RS_BLOCKINFO(  4,  50,  22), new RS_BLOCKINFO(  3,  36,  12)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  4,  81,  51), new RS_BLOCKINFO(  4,  51,  23), new RS_BLOCKINFO(  8,  37,  13)}),
			new QR_VERSIONINFO(12,  466, new int[]{ 370,  290,  206,  158},   2, new int[]{ 32,  58,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2, 116,  92), new RS_BLOCKINFO(  6,  58,  36), new RS_BLOCKINFO(  4,  46,  20), new RS_BLOCKINFO(  7,  42,  14)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2, 117,  93), new RS_BLOCKINFO(  2,  59,  37), new RS_BLOCKINFO(  6,  47,  21), new RS_BLOCKINFO(  4,  43,  15)}),
			new QR_VERSIONINFO(13,  532, new int[]{ 428,  334,  244,  180},   2, new int[]{ 34,  62,   0,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 133, 107), new RS_BLOCKINFO(  8,  59,  37), new RS_BLOCKINFO(  8,  44,  20), new RS_BLOCKINFO( 12,  33,  11)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  1,  60,  38), new RS_BLOCKINFO(  4,  45,  21), new RS_BLOCKINFO(  4,  34,  12)}),
			new QR_VERSIONINFO(14,  581, new int[]{ 461,  365,  261,  197},   3, new int[]{ 26,  46,  66,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  3, 145, 115), new RS_BLOCKINFO(  4,  64,  40), new RS_BLOCKINFO( 11,  36,  16), new RS_BLOCKINFO( 11,  36,  12)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 146, 116), new RS_BLOCKINFO(  5,  65,  41), new RS_BLOCKINFO(  5,  37,  17), new RS_BLOCKINFO(  5,  37,  13)}),
			new QR_VERSIONINFO(15,  655, new int[]{ 523,  415,  295,  223},   3, new int[]{ 26,  48,  70,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  5, 109,  87), new RS_BLOCKINFO(  5,  65,  41), new RS_BLOCKINFO(  5,  54,  24), new RS_BLOCKINFO( 11,  36,  12)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 110,  88), new RS_BLOCKINFO(  5,  66,  42), new RS_BLOCKINFO(  7,  55,  25), new RS_BLOCKINFO(  7,  37,  13)}),
			new QR_VERSIONINFO(16,  733, new int[]{ 589,  453,  325,  253},   3, new int[]{ 26,  50,  74,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  5, 122,  98), new RS_BLOCKINFO(  7,  73,  45), new RS_BLOCKINFO( 15,  43,  19), new RS_BLOCKINFO(  3,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 123,  99), new RS_BLOCKINFO(  3,  74,  46), new RS_BLOCKINFO(  2,  44,  20), new RS_BLOCKINFO( 13,  46,  16)}),
			new QR_VERSIONINFO(17,  815, new int[]{ 647,  507,  367,  283},   3, new int[]{ 30,  54,  78,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 135, 107), new RS_BLOCKINFO( 10,  74,  46), new RS_BLOCKINFO(  1,  50,  22), new RS_BLOCKINFO(  2,  42,  14)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  5, 136, 108), new RS_BLOCKINFO(  1,  75,  47), new RS_BLOCKINFO( 15,  51,  23), new RS_BLOCKINFO( 17,  43,  15)}),
			new QR_VERSIONINFO(18,  901, new int[]{ 721,  563,  397,  313},   3, new int[]{ 30,  56,  82,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  5, 150, 120), new RS_BLOCKINFO(  9,  69,  43), new RS_BLOCKINFO( 17,  50,  22), new RS_BLOCKINFO(  2,  42,  14)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 151, 121), new RS_BLOCKINFO(  4,  70,  44), new RS_BLOCKINFO(  1,  51,  23), new RS_BLOCKINFO( 19,  43,  15)}),
			new QR_VERSIONINFO(19,  991, new int[]{ 795,  627,  445,  341},   3, new int[]{ 30,  58,  86,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  3, 141, 113), new RS_BLOCKINFO(  3,  70,  44), new RS_BLOCKINFO( 17,  47,  21), new RS_BLOCKINFO(  9,  39,  13)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 142, 114), new RS_BLOCKINFO( 11,  71,  45), new RS_BLOCKINFO(  4,  48,  22), new RS_BLOCKINFO( 16,  40,  14)}),
			new QR_VERSIONINFO(20, 1085, new int[]{ 861,  669,  485,  385},   3, new int[]{ 34,  62,  90,   0,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  3, 135, 107), new RS_BLOCKINFO(  3,  67,  41), new RS_BLOCKINFO( 15,  54,  24), new RS_BLOCKINFO( 15,  43,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  5, 136, 108), new RS_BLOCKINFO( 13,  68,  42), new RS_BLOCKINFO(  5,  55,  25), new RS_BLOCKINFO( 10,  44,  16)}),
			new QR_VERSIONINFO(21, 1156, new int[]{ 932,  714,  512,  406},   4, new int[]{ 28,  50,  72,  94,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 144, 116), new RS_BLOCKINFO( 17,  68,  42), new RS_BLOCKINFO( 17,  50,  22), new RS_BLOCKINFO( 19,  46,  16)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 145, 117), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO(  6,  51,  23), new RS_BLOCKINFO(  6,  47,  17)}),
			new QR_VERSIONINFO(22, 1258, new int[]{1006,  782,  568,  442},   4, new int[]{ 26,  50,  74,  98,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2, 139, 111), new RS_BLOCKINFO( 17,  74,  46), new RS_BLOCKINFO(  7,  54,  24), new RS_BLOCKINFO( 34,  37,  13)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  7, 140, 112), new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO( 16,  55,  25), new RS_BLOCKINFO(  0,   0,   0)}),
			new QR_VERSIONINFO(23, 1364, new int[]{1094,  860,  614,  464},   4, new int[]{ 30,  54,  78, 102,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 151, 121), new RS_BLOCKINFO(  4,  75,  47), new RS_BLOCKINFO( 11,  54,  24), new RS_BLOCKINFO( 16,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  5, 152, 122), new RS_BLOCKINFO( 14,  76,  48), new RS_BLOCKINFO( 14,  55,  25), new RS_BLOCKINFO( 14,  46,  16)}),
			new QR_VERSIONINFO(24, 1474, new int[]{1174,  914,  664,  514},   4, new int[]{ 28,  54,  80, 106,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  6, 147, 117), new RS_BLOCKINFO(  6,  73,  45), new RS_BLOCKINFO( 11,  54,  24), new RS_BLOCKINFO( 30,  46,  16)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 148, 118), new RS_BLOCKINFO( 14,  74,  46), new RS_BLOCKINFO( 16,  55,  25), new RS_BLOCKINFO(  2,  47,  17)}),
			new QR_VERSIONINFO(25, 1588, new int[]{1276, 1000,  718,  538},   4, new int[]{ 32,  58,  84, 110,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  8, 132, 106), new RS_BLOCKINFO(  8,  75,  47), new RS_BLOCKINFO(  7,  54,  24), new RS_BLOCKINFO( 22,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 133, 107), new RS_BLOCKINFO( 13,  76,  48), new RS_BLOCKINFO( 22,  55,  25), new RS_BLOCKINFO( 13,  46,  16)}),
			new QR_VERSIONINFO(26, 1706, new int[]{1370, 1062,  754,  596},   4, new int[]{ 30,  58,  86, 114,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 10, 142, 114), new RS_BLOCKINFO( 19,  74,  46), new RS_BLOCKINFO( 28,  50,  22), new RS_BLOCKINFO( 33,  46,  16)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  2, 143, 115), new RS_BLOCKINFO(  4,  75,  47), new RS_BLOCKINFO(  6,  51,  23), new RS_BLOCKINFO(  4,  47,  17)}),
			new QR_VERSIONINFO(27, 1828, new int[]{1468, 1128,  808,  628},   4, new int[]{ 34,  62,  90, 118,   0,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  8, 152, 122), new RS_BLOCKINFO( 22,  73,  45), new RS_BLOCKINFO(  8,  53,  23), new RS_BLOCKINFO( 12,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 153, 123), new RS_BLOCKINFO(  3,  74,  46), new RS_BLOCKINFO( 26,  54,  24), new RS_BLOCKINFO( 28,  46,  16)}),
			new QR_VERSIONINFO(28, 1921, new int[]{1531, 1193,  871,  661},   5, new int[]{ 26,  50,  74,  98, 122,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  3, 147, 117), new RS_BLOCKINFO(  3,  73,  45), new RS_BLOCKINFO(  4,  54,  24), new RS_BLOCKINFO( 11,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 10, 148, 118), new RS_BLOCKINFO( 23,  74,  46), new RS_BLOCKINFO( 31,  55,  25), new RS_BLOCKINFO( 31,  46,  16)}),
			new QR_VERSIONINFO(29, 2051, new int[]{1631, 1267,  911,  701},   5, new int[]{ 30,  54,  78, 102, 126,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  7, 146, 116), new RS_BLOCKINFO( 21,  73,  45), new RS_BLOCKINFO(  1,  53,  23), new RS_BLOCKINFO( 19,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  7, 147, 117), new RS_BLOCKINFO(  7,  74,  46), new RS_BLOCKINFO( 37,  54,  24), new RS_BLOCKINFO( 26,  46,  16)}),
			new QR_VERSIONINFO(30, 2185, new int[]{1735, 1373,  985,  745},   5, new int[]{ 26,  52,  78, 104, 130,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  5, 145, 115), new RS_BLOCKINFO( 19,  75,  47), new RS_BLOCKINFO( 15,  54,  24), new RS_BLOCKINFO( 23,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 10, 146, 116), new RS_BLOCKINFO( 10,  76,  48), new RS_BLOCKINFO( 25,  55,  25), new RS_BLOCKINFO( 25,  46,  16)}),
			new QR_VERSIONINFO(31, 2323, new int[]{1843, 1455, 1033,  793},   5, new int[]{ 30,  56,  82, 108, 134,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 13, 145, 115), new RS_BLOCKINFO(  2,  74,  46), new RS_BLOCKINFO( 42,  54,  24), new RS_BLOCKINFO( 23,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  3, 146, 116), new RS_BLOCKINFO( 29,  75,  47), new RS_BLOCKINFO(  1,  55,  25), new RS_BLOCKINFO( 28,  46,  16)}),
			new QR_VERSIONINFO(32, 2465, new int[]{1955, 1541, 1115,  845},   5, new int[]{ 34,  60,  86, 112, 138,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 17, 145, 115), new RS_BLOCKINFO( 10,  74,  46), new RS_BLOCKINFO( 10,  54,  24), new RS_BLOCKINFO( 19,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  0,   0,   0), new RS_BLOCKINFO( 23,  75,  47), new RS_BLOCKINFO( 35,  55,  25), new RS_BLOCKINFO( 35,  46,  16)}),
			new QR_VERSIONINFO(33, 2611, new int[]{2071, 1631, 1171,  901},   5, new int[]{ 30,  58,  86, 114, 142,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 17, 145, 115), new RS_BLOCKINFO( 14,  74,  46), new RS_BLOCKINFO( 29,  54,  24), new RS_BLOCKINFO( 11,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  1, 146, 116), new RS_BLOCKINFO( 21,  75,  47), new RS_BLOCKINFO( 19,  55,  25), new RS_BLOCKINFO( 46,  46,  16)}),
			new QR_VERSIONINFO(34, 2761, new int[]{2191, 1725, 1231,  961},   5, new int[]{ 34,  62,  90, 118, 146,   0}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 13, 145, 115), new RS_BLOCKINFO( 14,  74,  46), new RS_BLOCKINFO( 44,  54,  24), new RS_BLOCKINFO( 59,  46,  16)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  6, 146, 116), new RS_BLOCKINFO( 23,  75,  47), new RS_BLOCKINFO(  7,  55,  25), new RS_BLOCKINFO(  1,  47,  17)}),
			new QR_VERSIONINFO(35, 2876, new int[]{2306, 1812, 1286,  986},   6, new int[]{ 30,  54,  78, 102, 126, 150}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 12, 151, 121), new RS_BLOCKINFO( 12,  75,  47), new RS_BLOCKINFO( 39,  54,  24), new RS_BLOCKINFO( 22,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  7, 152, 122), new RS_BLOCKINFO( 26,  76,  48), new RS_BLOCKINFO( 14,  55,  25), new RS_BLOCKINFO( 41,  46,  16)}),
			new QR_VERSIONINFO(36, 3034, new int[]{2434, 1914, 1354, 1054},   6, new int[]{ 24,  50,  76, 102, 128, 154}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  6, 151, 121), new RS_BLOCKINFO(  6,  75,  47), new RS_BLOCKINFO( 46,  54,  24), new RS_BLOCKINFO(  2,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 14, 152, 122), new RS_BLOCKINFO( 34,  76,  48), new RS_BLOCKINFO( 10,  55,  25), new RS_BLOCKINFO( 64,  46,  16)}),
			new QR_VERSIONINFO(37, 3196, new int[]{2566, 1992, 1426, 1096},   6, new int[]{ 28,  54,  80, 106, 132, 158}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 17, 152, 122), new RS_BLOCKINFO( 29,  74,  46), new RS_BLOCKINFO( 49,  54,  24), new RS_BLOCKINFO( 24,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 153, 123), new RS_BLOCKINFO( 14,  75,  47), new RS_BLOCKINFO( 10,  55,  25), new RS_BLOCKINFO( 46,  46,  16)}),
			new QR_VERSIONINFO(38, 3362, new int[]{2702, 2102, 1502, 1142},   6, new int[]{ 32,  58,  84, 110, 136, 162}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 152, 122), new RS_BLOCKINFO( 13,  74,  46), new RS_BLOCKINFO( 48,  54,  24), new RS_BLOCKINFO( 42,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 18, 153, 123), new RS_BLOCKINFO( 32,  75,  47), new RS_BLOCKINFO( 14,  55,  25), new RS_BLOCKINFO( 32,  46,  16)}),
			new QR_VERSIONINFO(39, 3532, new int[]{2812, 2216, 1582, 1222},   6, new int[]{ 26,  54,  82, 110, 138, 166}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 20, 147, 117), new RS_BLOCKINFO( 40,  75,  47), new RS_BLOCKINFO( 43,  54,  24), new RS_BLOCKINFO( 10,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  4, 148, 118), new RS_BLOCKINFO(  7,  76,  48), new RS_BLOCKINFO( 22,  55,  25), new RS_BLOCKINFO( 67,  46,  16)}),
			new QR_VERSIONINFO(40, 3706, new int[]{2956, 2334, 1666, 1276},   6, new int[]{ 30,  58,  86, 114, 142, 170}, new RS_BLOCKINFO[]{new RS_BLOCKINFO( 19, 148, 118), new RS_BLOCKINFO( 18,  75,  47), new RS_BLOCKINFO( 34,  54,  24), new RS_BLOCKINFO( 20,  45,  15)}, new RS_BLOCKINFO[]{new RS_BLOCKINFO(  6, 149, 119), new RS_BLOCKINFO( 31,  76,  48), new RS_BLOCKINFO( 34,  55,  25), new RS_BLOCKINFO( 61,  46,  16)})
	};

	/////////////////////////////////////////////////////////////////////////////
	// GF(2^8)α指数→整数変換テーブル
	static short byExpToInt[] =
	{
		  1,   2,   4,   8,  16,  32,  64, 128,  29,  58, 116, 232, 205, 135,  19,  38,
		 76, 152,  45,  90, 180, 117, 234, 201, 143,   3,   6,  12,  24,  48,  96, 192,
		157,  39,  78, 156,  37,  74, 148,  53, 106, 212, 181, 119, 238, 193, 159,  35,
		 70, 140,   5,  10,  20,  40,  80, 160,  93, 186, 105, 210, 185, 111, 222, 161,
		 95, 190,  97, 194, 153,  47,  94, 188, 101, 202, 137,  15,  30,  60, 120, 240,
		253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223, 163,  91, 182, 113, 226,
		217, 175,  67, 134,  17,  34,  68, 136,  13,  26,  52, 104, 208, 189, 103, 206,
		129,  31,  62, 124, 248, 237, 199, 147,  59, 118, 236, 197, 151,  51, 102, 204,
		133,  23,  46,  92, 184, 109, 218, 169,  79, 158,  33,  66, 132,  21,  42,  84,
		168,  77, 154,  41,  82, 164,  85, 170,  73, 146,  57, 114, 228, 213, 183, 115,
		230, 209, 191,  99, 198, 145,  63, 126, 252, 229, 215, 179, 123, 246, 241, 255,
		227, 219, 171,  75, 150,  49,  98, 196, 149,  55, 110, 220, 165,  87, 174,  65,
		130,  25,  50, 100, 200, 141,   7,  14,  28,  56, 112, 224, 221, 167,  83, 166,
		 81, 162,  89, 178, 121, 242, 249, 239, 195, 155,  43,  86, 172,  69, 138,   9,
		 18,  36,  72, 144,  61, 122, 244, 245, 247, 243, 251, 235, 203, 139,  11,  22,
		 44,  88, 176, 125, 250, 233, 207, 131,  27,  54, 108, 216, 173,  71, 142,   1
	};

	/////////////////////////////////////////////////////////////////////////////
	// GF(2^8)α整数→指数変換テーブル
    static short[] byIntToExp =
	{
		  0,   0,   1,  25,   2,  50,  26, 198,   3, 223,  51, 238,  27, 104, 199,  75,
		  4, 100, 224,  14,  52, 141, 239, 129,  28, 193, 105, 248, 200,   8,  76, 113,
		  5, 138, 101,  47, 225,  36,  15,  33,  53, 147, 142, 218, 240,  18, 130,  69,
		 29, 181, 194, 125, 106,  39, 249, 185, 201, 154,   9, 120,  77, 228, 114, 166,
		  6, 191, 139,  98, 102, 221,  48, 253, 226, 152,  37, 179,  16, 145,  34, 136,
		 54, 208, 148, 206, 143, 150, 219, 189, 241, 210,  19,  92, 131,  56,  70,  64,
		 30,  66, 182, 163, 195,  72, 126, 110, 107,  58,  40,  84, 250, 133, 186,  61,
		202,  94, 155, 159,  10,  21, 121,  43,  78, 212, 229, 172, 115, 243, 167,  87,
		  7, 112, 192, 247, 140, 128,  99,  13, 103,  74, 222, 237,  49, 197, 254,  24,
		227, 165, 153, 119,  38, 184, 180, 124,  17,  68, 146, 217,  35,  32, 137,  46,
		 55,  63, 209,  91, 149, 188, 207, 205, 144, 135, 151, 178, 220, 252, 190,  97,
		242,  86, 211, 171,  20,  42,  93, 158, 132,  60,  57,  83,  71, 109,  65, 162,
		 31,  45,  67, 216, 183, 123, 164, 118, 196,  23,  73, 236, 127,  12, 111, 246,
		108, 161,  59,  82,  41, 157,  85, 170, 251,  96, 134, 177, 187, 204,  62,  90,
		203,  89,  95, 176, 156, 169, 160,  81,  11, 245,  22, 235, 122, 117,  44, 215,
		 79, 174, 213, 233, 230, 231, 173, 232, 116, 214, 244, 234, 168,  80,  88, 175
	};

	/////////////////////////////////////////////////////////////////////////////
	// 誤り訂正生成多項式α係数
    static short byRSExp7[]  = {87, 229, 146, 149, 238, 102,  21};
	static short[] byRSExp10 = {251,  67,  46,  61, 118,  70,  64,  94,  32,  45};
	static short[] byRSExp13 = { 74, 152, 176, 100,  86, 100, 106, 104, 130, 218, 206, 140,  78};
    static short byRSExp15[] = {  8, 183,  61,  91, 202,  37,  51,  58,  58, 237, 140, 124,   5,  99, 105};
    static short byRSExp16[] = {120, 104, 107, 109, 102, 161,  76,   3,  91, 191, 147, 169, 182, 194, 225, 120};
    static short byRSExp17[] = { 43, 139, 206,  78,  43, 239, 123, 206, 214, 147,  24,  99, 150,  39, 243, 163, 136};
    static short byRSExp18[] = {215, 234, 158,  94, 184,  97, 118, 170,  79, 187, 152, 148, 252, 179,   5,  98,  96, 153};
    static short byRSExp20[] = { 17,  60,  79,  50,  61, 163,  26, 187, 202, 180, 221, 225,  83, 239, 156, 164, 212, 212, 188, 190};
    static short byRSExp22[] = {210, 171, 247, 242,  93, 230,  14, 109, 221,  53, 200,  74,   8, 172,  98,  80, 219, 134, 160, 105, 165, 231};
    static short byRSExp24[] = {229, 121, 135,  48, 211, 117, 251, 126, 159, 180, 169, 152, 192, 226, 228, 218, 111,   0, 117, 232,  87,  96, 227,  21};
    static short byRSExp26[] = {173, 125, 158,   2, 103, 182, 118,  17, 145, 201, 111,  28, 165,  53, 161,  21, 245, 142,  13, 102,  48, 227, 153, 145, 218,  70};
    static short byRSExp28[] = {168, 223, 200, 104, 224, 234, 108, 180, 110, 190, 195, 147, 205,  27, 232, 201,  21,  43, 245,  87,  42, 195, 212, 119, 242,  37,   9, 123};
    static short byRSExp30[] = { 41, 173, 145, 152, 216,  31, 179, 182,  50,  48, 110,  86, 239,  96, 222, 125,  42, 173, 226, 193, 224, 130, 156,  37, 251, 216, 238,  40, 192, 180};
    static short byRSExp32[] = { 10,   6, 106, 190, 249, 167,   4,  67, 209, 138, 138,  32, 242, 123,  89,  27, 120, 185,  80, 156,  38,  69, 171,  60,  28, 222,  80,  52, 254, 185, 220, 241};
    static short byRSExp34[] = {111,  77, 146,  94,  26,  21, 108,  19, 105,  94, 113, 193,  86, 140, 163, 125,  58, 158, 229, 239, 218, 103,  56,  70, 114,  61, 183, 129, 167,  13,  98,  62, 129,  51};
    static short byRSExp36[] = {200, 183,  98,  16, 172,  31, 246, 234,  60, 152, 115,   0, 167, 152, 113, 248, 238, 107,  18,  63, 218,  37,  87, 210, 105, 177, 120,  74, 121, 196, 117, 251, 113, 233,  30, 120};
    static short byRSExp38[] = {159,  34,  38, 228, 230,  59, 243,  95,  49, 218, 176, 164,  20,  65,  45, 111,  39,  81,  49, 118, 113, 222, 193, 250, 242, 168, 217,  41, 164, 247, 177,  30, 238,  18, 120, 153,  60, 193};
    static short byRSExp40[] = { 59, 116,  79, 161, 252,  98, 128, 205, 128, 161, 247,  57, 163,  56, 235, 106,  53,  26, 187, 174, 226, 104, 170,   7, 175,  35, 181, 114,  88,  41,  47, 163, 125, 134,  72,  20, 232,  53,  35,  15};
    static short byRSExp42[] = {250, 103, 221, 230,  25,  18, 137, 231,   0,   3,  58, 242, 221, 191, 110,  84, 230,   8, 188, 106,  96, 147,  15, 131, 139,  34, 101, 223,  39, 101, 213, 199, 237, 254, 201, 123, 171, 162, 194, 117,  50,  96};
    static short byRSExp44[] = {190,   7,  61, 121,  71, 246,  69,  55, 168, 188,  89, 243, 191,  25,  72, 123,   9, 145,  14, 247,   1, 238,  44,  78, 143,  62, 224, 126, 118, 114,  68, 163,  52, 194, 217, 147, 204, 169,  37, 130, 113, 102,  73, 181};
    static short byRSExp46[] = {112,  94,  88, 112, 253, 224, 202, 115, 187,  99,  89,   5,  54, 113, 129,  44,  58,  16, 135, 216, 169, 211,  36,   1,   4,  96,  60, 241,  73, 104, 234,   8, 249, 245, 119, 174,  52,  25, 157, 224,  43, 202, 223,  19,  82,  15};
    static short byRSExp48[] = {228,  25, 196, 130, 211, 146,  60,  24, 251,  90,  39, 102, 240,  61, 178,  63,  46, 123, 115,  18, 221, 111, 135, 160, 182, 205, 107, 206,  95, 150, 120, 184,  91,  21, 247, 156, 140, 238, 191,  11,  94, 227,  84,  50, 163,  39,  34, 108};
    static short byRSExp50[] = {232, 125, 157, 161, 164,   9, 118,  46, 209,  99, 203, 193,  35,   3, 209, 111, 195, 242, 203, 225,  46,  13,  32, 160, 126, 209, 130, 160, 242, 215, 242,  75,  77,  42, 189,  32, 113,  65, 124,  69, 228, 114, 235, 175, 124, 170, 215, 232, 133, 205};
    static short byRSExp52[] = {116,  50,  86, 186,  50, 220, 251,  89, 192,  46,  86, 127, 124,  19, 184, 233, 151, 215,  22,  14,  59, 145,  37, 242, 203, 134, 254,  89, 190,  94,  59,  65, 124, 113, 100, 233, 235, 121,  22,  76,  86,  97,  39, 242, 200, 220, 101,  33, 239, 254, 116,  51};
    static short byRSExp54[] = {183,  26, 201,  87, 210, 221, 113,  21,  46,  65,  45,  50, 238, 184, 249, 225, 102,  58, 209, 218, 109, 165,  26,  95, 184, 192,  52, 245,  35, 254, 238, 175, 172,  79, 123,  25, 122,  43, 120, 108, 215,  80, 128, 201, 235,   8, 153,  59, 101,  31, 198,  76,  31, 156};
    static short byRSExp56[] = {106, 120, 107, 157, 164, 216, 112, 116,   2,  91, 248, 163,  36, 201, 202, 229,   6, 144, 254, 155, 135, 208, 170, 209,  12, 139, 127, 142, 182, 249, 177, 174, 190,  28,  10,  85, 239, 184, 101, 124, 152, 206,  96,  23, 163,  61,  27, 196, 247, 151, 154, 202, 207,  20,  61,  10};
    static short byRSExp58[] = { 82, 116,  26, 247,  66,  27,  62, 107, 252, 182, 200, 185, 235,  55, 251, 242, 210, 144, 154, 237, 176, 141, 192, 248, 152, 249, 206,  85, 253, 142,  65, 165, 125,  23,  24,  30, 122, 240, 214,   6, 129, 218,  29, 145, 127, 134, 206, 245, 117,  29,  41,  63, 159, 142, 233, 125, 148, 123};
    static short byRSExp60[] = {107, 140,  26,  12,   9, 141, 243, 197, 226, 197, 219,  45, 211, 101, 219, 120,  28, 181, 127,   6, 100, 247,   2, 205, 198,  57, 115, 219, 101, 109, 160,  82,  37,  38, 238,  49, 160, 209, 121,  86,  11, 124,  30, 181,  84,  25, 194,  87,  65, 102, 190, 220,  70,  27, 209,  16,  89,   7,  33, 240};
    static short byRSExp62[] = { 65, 202, 113,  98,  71, 223, 248, 118, 214,  94,   0, 122,  37,  23,   2, 228,  58, 121,   7, 105, 135,  78, 243, 118,  70,  76, 223,  89,  72,  50,  70, 111, 194,  17, 212, 126, 181,  35, 221, 117, 235,  11, 229, 149, 147, 123, 213,  40, 115,   6, 200, 100,  26, 246, 182, 218, 127, 215,  36, 186, 110, 106};
    static short byRSExp64[] = { 45,  51, 175,   9,   7, 158, 159,  49,  68, 119,  92, 123, 177, 204, 187, 254, 200,  78, 141, 149, 119,  26, 127,  53, 160,  93, 199, 212,  29,  24, 145, 156, 208, 150, 218, 209,   4, 216,  91,  47, 184, 146,  47, 140, 195, 195, 125, 242, 238,  63,  99, 108, 140, 230, 242,  31, 204,  11, 178, 243, 217, 156, 213, 231};
    static short byRSExp66[] = {  5, 118, 222, 180, 136, 136, 162,  51,  46, 117,  13, 215,  81,  17, 139, 247, 197, 171,  95, 173,  65, 137, 178,  68, 111,  95, 101,  41,  72, 214, 169, 197,  95,   7,  44, 154,  77, 111, 236,  40, 121, 143,  63,  87,  80, 253, 240, 126, 217,  77,  34, 232, 106,  50, 168,  82,  76, 146,  67, 106, 171,  25, 132,  93,  45, 105};
    static short byRSExp68[] = {247, 159, 223,  33, 224,  93,  77,  70,  90, 160,  32, 254,  43, 150,  84, 101, 190, 205, 133,  52,  60, 202, 165, 220, 203, 151,  93,  84,  15,  84, 253, 173, 160,  89, 227,  52, 199,  97,  95, 231,  52, 177,  41, 125, 137, 241, 166, 225, 118,   2,  54,  32,  82, 215, 175, 198,  43, 238, 235,  27, 101, 184, 127,   3,   5,   8, 163, 238};
	static short[][] byRSExp =
	{
		null,      null,      null,      null,      null,      null,      null,      byRSExp7,  null,      null,
		byRSExp10, null,      null,      byRSExp13, null,      byRSExp15, byRSExp16, byRSExp17, byRSExp18, null,
		byRSExp20, null,      byRSExp22, null,      byRSExp24, null,      byRSExp26, null,      byRSExp28, null,
		byRSExp30, null,      byRSExp32, null,      byRSExp34, null,      byRSExp36, null,      byRSExp38, null,
		byRSExp40, null,      byRSExp42, null,      byRSExp44, null,      byRSExp46, null,      byRSExp48, null,
		byRSExp50, null,      byRSExp52, null,      byRSExp54, null,      byRSExp56, null,      byRSExp58, null,
		byRSExp60, null,      byRSExp62, null,      byRSExp64, null,      byRSExp66, null,      byRSExp68
	};

	// 文字数インジケータビット長(バージョングループ別, {S, M, L})
    static int[] nIndicatorLenNumeral = {10, 12, 14};
	static int[] nIndicatorLenAlphabet = { 9, 11, 13};
	static int[] nIndicatorLen8Bit = { 8, 16, 16};
	static int[] nIndicatorLenKanji = { 8, 10, 12};

	static int	m_nLevel;//纠错等级(0 = L, 1 = M, 2 = Q, 3 = H)
	static int	m_nVersion;//版本号(1～40)
	static int	m_nMaskingNo;//掩膜方式
	static int	m_ncDataCodeWordBit; // データコードワードビット長
	static short m_byDataCodeWord[] = new short[MAX_DATACODEWORD]; // 入力データエンコードエリア
	static int m_ncAllCodeWord; // 総コードワード数(ＲＳ誤り訂正データを含む)
	static short[] m_byAllCodeWord = new short[MAX_ALLCODEWORD]; // 総コードワード算出エリア
	static short[] m_byRSWork = new short[MAX_CODEBLOCK]; // ＲＳコードワード算出ワーク
	static int	m_nSymbleSize;
	static short[][] m_byModuleData = new short[MAX_MODULESIZE][MAX_MODULESIZE];
	static int[] m_nBlockLength = new int[MAX_DATACODEWORD];
	static short[] m_byBlockMode = new short[MAX_DATACODEWORD];

	//scale为二维码图像缩放倍数
	public static Bitmap getQRcode(char lpData[], float scale)
    {
        Bitmap qrcode = Encode(lpData,0,QREncode.QR_LEVEL_M,0,-1);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        return Bitmap.createBitmap(qrcode,0,0,qrcode.getWidth(),qrcode.getHeight(),matrix,false);//filter为滤波处理，滤波放大后二维码会变模糊，flase时不滤波
    }

	private static Bitmap Encode(char lpData[], int nLen, int nLevel, int nVersion, int nMaskingNo)
	{
		if(lpData==null) return null;

		m_nLevel = nLevel;
		m_nMaskingNo = nMaskingNo;

		// データ長が指定されていない場合は lstrlen によって取得
		if(nLen == 0) nLen = lpData.length;
		if(nLen == 0) return null; // データなし

		// バージョン(型番)チェック
		int nEncodeVersion = GetEncodeVersion(nVersion, lpData, nLen);
		if(nEncodeVersion == 0) return null; // 容量オーバー

		if(nVersion == 0)
		{
			m_nVersion = nEncodeVersion;// 型番自動
		}
		else
		{
			if(nEncodeVersion <= nVersion)
			{
				m_nVersion = nVersion;
			}
			else
			{
				m_nVersion = nEncodeVersion; // バージョン(型番)自動拡張
			}
		}

		// ターミネータコード"0000"付加
		int ncDataCodeWord = QR_VersonInfo[m_nVersion].ncDataCodeWord[nLevel];
		int ncTerminater = Math.min(4, (ncDataCodeWord * 8) - m_ncDataCodeWordBit);
		if(ncTerminater > 0)
			m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, 0, ncTerminater);

		int i=0,j=0;

		// パディングコード"11101100, 00010001"付加
		short byPaddingCode = 0xec;
		for(i = (m_ncDataCodeWordBit + 7) / 8; i < ncDataCodeWord; ++i)
		{
			m_byDataCodeWord[i] = byPaddingCode;

			byPaddingCode = (short)(byPaddingCode == 0xec ? 0x11 : 0xec);
		}

		// 総コードワード算出エリアクリア
		m_ncAllCodeWord = QR_VersonInfo[m_nVersion].ncAllCodeWord;
		for(int index=0;index<m_ncAllCodeWord;index++)
			m_byAllCodeWord[index] = 0;

		int nDataCwIndex = 0; // データコードワード処理位置

		// データブロック分割数
		int ncBlock1 = QR_VersonInfo[m_nVersion].RS_BlockInfo1[nLevel].ncRSBlock;
		int ncBlock2 = QR_VersonInfo[m_nVersion].RS_BlockInfo2[nLevel].ncRSBlock;
		int ncBlockSum = ncBlock1 + ncBlock2;

		int nBlockNo = 0; // 処理中ブロック番号

		// ブロック別データコードワード数
		int ncDataCw1 = QR_VersonInfo[m_nVersion].RS_BlockInfo1[nLevel].ncDataCodeWord;
		int ncDataCw2 = QR_VersonInfo[m_nVersion].RS_BlockInfo2[nLevel].ncDataCodeWord;

		// データコードワードインターリーブ配置
		for(i = 0; i < ncBlock1; ++i)
		{
			for(j = 0; j < ncDataCw1; ++j)
			{
				m_byAllCodeWord[(ncBlockSum * j) + nBlockNo] = m_byDataCodeWord[nDataCwIndex++];
			}
			++nBlockNo;
		}

		for(i = 0; i < ncBlock2; ++i)
		{
			for(j = 0; j < ncDataCw2; ++j)
			{
				if(j < ncDataCw1)
				{
					m_byAllCodeWord[(ncBlockSum * j) + nBlockNo] = m_byDataCodeWord[nDataCwIndex++];
				}
				else
				{
					// ２種目ブロック端数分配置
					m_byAllCodeWord[(ncBlockSum * ncDataCw1) + i]  = m_byDataCodeWord[nDataCwIndex++];
				}	
			}
			++nBlockNo;
		}

		// ブロック別ＲＳコードワード数(※現状では同数)
		int ncRSCw1 = QR_VersonInfo[m_nVersion].RS_BlockInfo1[nLevel].ncAllCodeWord - ncDataCw1;
		int ncRSCw2 = QR_VersonInfo[m_nVersion].RS_BlockInfo2[nLevel].ncAllCodeWord - ncDataCw2;

		/////////////////////////////////////////////////////////////////////////
		// ＲＳコードワード算出

		nDataCwIndex = 0;
		nBlockNo = 0;

		for(i = 0; i < ncBlock1; ++i)
		{
			for(int index=0;index<MAX_CODEBLOCK;index++)
				m_byRSWork[index] = 0;
			
			for(int index=0;index<ncDataCw1;index++)
			{
				m_byRSWork[index] = m_byDataCodeWord[nDataCwIndex+index];
			}
			GetRSCodeWord(m_byRSWork, ncDataCw1, ncRSCw1);

			// ＲＳコードワード配置
			for(j = 0; j < ncRSCw1; ++j)
			{
				m_byAllCodeWord[ncDataCodeWord + (ncBlockSum * j) + nBlockNo] = m_byRSWork[j];
			}

			nDataCwIndex += ncDataCw1;
			++nBlockNo;
		}

		for(i = 0; i < ncBlock2; ++i)
		{
			for(int index=0;index<MAX_CODEBLOCK;index++)
			{
				m_byRSWork[index] = 0;
			}
			
			for(int index=0;index<ncDataCw2;index++)
			{
				m_byRSWork[index] = m_byDataCodeWord[nDataCwIndex+index];
			}
			GetRSCodeWord(m_byRSWork, ncDataCw2, ncRSCw2);

			// ＲＳコードワード配置
			for(j = 0; j < ncRSCw2; ++j)
			{
				m_byAllCodeWord[ncDataCodeWord + (ncBlockSum * j) + nBlockNo] = m_byRSWork[j];
			}

			nDataCwIndex += ncDataCw2;
			++nBlockNo;
		}

		m_nSymbleSize = m_nVersion * 4 + 17;

		// モジュール配置
		FormatModule();

		return CreateBitmapFromData();
	}

	static Bitmap CreateBitmapFromData()
	{
		int nWidth = m_nSymbleSize + (QR_MARGIN * 2);
	    Bitmap bitmap = Bitmap.createBitmap(nWidth, nWidth, Bitmap.Config.ARGB_8888);
	    
	    //把背景清为白色
	    for(int i=0;i<nWidth;i++)
	    {
	    	for(int j=0;j<nWidth;j++)
	    	{
	    		bitmap.setPixel(i, j, Color.WHITE);
	    	}
	    }
	    
		// ドット描画
		for(int i = 0; i < m_nSymbleSize; ++i)
		{
			for(int j = 0; j < m_nSymbleSize; ++j)
			{
				if (m_byModuleData[i][j]!=0)
				{
					bitmap.setPixel(i + QR_MARGIN, j + QR_MARGIN, Color.BLACK);
				}
			}
		}
		return bitmap;
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：データパターン配置
    static void SetCodeWordPattern()
	{
		int x = m_nSymbleSize;
		int y = m_nSymbleSize - 1;

		int nCoef_x = 1; // ｘ軸配置向き
		int nCoef_y = 1; // ｙ軸配置向き

		int i, j;

		for (i = 0; i < m_ncAllCodeWord; ++i)
		{
			for (j = 0; j < 8; ++j)
			{
				do
				{
					x += nCoef_x;
					nCoef_x *= -1;

					if (nCoef_x < 0)
					{
						y += nCoef_y;

						if (y < 0 || y == m_nSymbleSize)
						{
							y = (y < 0) ? 0 : m_nSymbleSize - 1;
							nCoef_y *= -1;

							x -= 2;

							if (x == 6) // タイミングパターン
								--x;
						}
					}
				}
				while ((m_byModuleData[x][y] & 0x20)>0); // 機能モジュールを除外

				m_byModuleData[x][y] = (short) ((m_byAllCodeWord[i] & (1 << (7 - j)))>0 ? 0x02 : 0x00);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：マスキングパターン配置
	// 引  数：マスキングパターン番号
    static void SetMaskingPattern(int nPatternNo)
	{
		int i, j;

		for (i = 0; i < m_nSymbleSize; ++i)
		{
			for (j = 0; j < m_nSymbleSize; ++j)
			{
				if ((m_byModuleData[j][i] & 0x20) == 0) // 機能モジュールを除外
				{
					boolean bMask;

					switch (nPatternNo)
					{
					case 0:
						bMask = ((i + j) % 2 == 0);
						break;

					case 1:
						bMask = (i % 2 == 0);
						break;

					case 2:
						bMask = (j % 3 == 0);
						break;

					case 3:
						bMask = ((i + j) % 3 == 0);
						break;

					case 4:
						bMask = (((i / 2) + (j / 3)) % 2 == 0);
						break;

					case 5:
						bMask = (((i * j) % 2) + ((i * j) % 3) == 0);
						break;

					case 6:
						bMask = ((((i * j) % 2) + ((i * j) % 3)) % 2 == 0);
						break;

					default: // case 7:
						bMask = ((((i * j) % 3) + ((i + j) % 2)) % 2 == 0);
						break;
					}
					m_byModuleData[j][i] = (short)((m_byModuleData[j][i] & 0xfe) | ((((m_byModuleData[j][i] & 0x02) > 1)?1:0) ^ (bMask? 1:0)));
				}
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：フォーマット情報配置
	// 引  数：マスキングパターン番号
    static void SetFormatInfoPattern(int nPatternNo)
	{
		int nFormatInfo;
		int i;

		switch (m_nLevel)
		{
		case QR_LEVEL_M:
			nFormatInfo = 0x00; // 00nnnb
			break;

		case QR_LEVEL_L:
			nFormatInfo = 0x08; // 01nnnb
			break;

		case QR_LEVEL_Q:
			nFormatInfo = 0x18; // 11nnnb
			break;

		default: // case QR_LEVEL_H:
			nFormatInfo = 0x10; // 10nnnb
			break;
		}

		nFormatInfo += nPatternNo;

		int nFormatData = nFormatInfo << 10;

		// 剰余ビット算出
		for (i = 0; i < 5; ++i)
		{
			if ((nFormatData & (1 << (14 - i)))!=0)
			{
				nFormatData ^= (0x0537 << (4 - i)); // 10100110111b
			}
		}

		nFormatData += nFormatInfo << 10;

		// マスキング
		nFormatData ^= 0x5412; // 101010000010010b

		// 左上位置検出パターン周り配置
		for (i = 0; i <= 5; ++i)
			m_byModuleData[8][i] = (short) ((nFormatData & (1 << i))!=0 ? 0x30 : 0x20);

		m_byModuleData[8][7] = (short) ((nFormatData & (1 << 6))!=0 ? 0x30 : 0x20);
		m_byModuleData[8][8] = (short) ((nFormatData & (1 << 7))!=0 ? 0x30 : 0x20);
		m_byModuleData[7][8] = (short) ((nFormatData & (1 << 8))!=0 ? 0x30 : 0x20);

		for (i = 9; i <= 14; ++i)
			m_byModuleData[14 - i][8] = (short) ((nFormatData & (1 << i))!=0 ? 0x30 : 0x20);

		// 右上位置検出パターン下配置
		for (i = 0; i <= 7; ++i)
			m_byModuleData[m_nSymbleSize - 1 - i][8] = (short) ((nFormatData & (1 << i))!=0 ? 0x30 : 0x20);

		// 左下位置検出パターン右配置
		m_byModuleData[8][m_nSymbleSize - 8] = 0x30; // 固定暗モジュール

		for (i = 8; i <= 14; ++i)
			m_byModuleData[8][m_nSymbleSize - 15 + i] = (short) ((nFormatData & (1 << i))!=0 ? 0x30 : 0x20);
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：モジュールへのデータ配置
	// 戻り値：一辺のモジュール数
    static void FormatModule()
	{
		int i=0, j=0;
		for(i=0;i<MAX_MODULESIZE;i++)
		{
			for(j=0;j<MAX_MODULESIZE;j++)
				m_byModuleData[i][j] = 0;
		}

		SetFunctionModule();// 機能モジュール配置
		SetCodeWordPattern();// データパターン配置

		if(m_nMaskingNo == -1)
		{
			// 最適マスキングパターン選択
			m_nMaskingNo = 0;
			SetMaskingPattern(m_nMaskingNo); // マスキング
			SetFormatInfoPattern(m_nMaskingNo); // フォーマット情報パターン配置

			int nMinPenalty = CountPenalty();
			for(i = 1; i <= 7; ++i)
			{
				SetMaskingPattern(i); // マスキング
				SetFormatInfoPattern(i); // フォーマット情報パターン配置

				int nPenalty = CountPenalty();
				if (nPenalty < nMinPenalty)
				{
					nMinPenalty = nPenalty;
					m_nMaskingNo = i;
				}
			}
		}

		SetMaskingPattern(m_nMaskingNo); // マスキング
		SetFormatInfoPattern(m_nMaskingNo); // フォーマット情報パターン配置

		// モジュールパターンをブール値に変換
		for(i = 0; i < m_nSymbleSize; ++i)
		{
			for(j = 0; j < m_nSymbleSize; ++j)
			{
				m_byModuleData[i][j] = (short)(m_byModuleData[i][j] & 0x11);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：マスク後ペナルティスコア算出
    static int CountPenalty()
	{
		int nPenalty = 0;
		int i, j, k;

		// 同色の列の隣接モジュール
		for (i = 0; i < m_nSymbleSize; ++i)
		{
			for (j = 0; j < m_nSymbleSize - 4; ++j)
			{
				int nCount = 1;

				for (k = j + 1; k < m_nSymbleSize; k++)
				{
					if (((m_byModuleData[i][j] & 0x11) == 0) == ((m_byModuleData[i][k] & 0x11) == 0))
						++nCount;
					else
						break;
				}

				if (nCount >= 5)
				{
					nPenalty += 3 + (nCount - 5);
				}

				j = k - 1;
			}
		}

		// 同色の行の隣接モジュール
		for (i = 0; i < m_nSymbleSize; ++i)
		{
			for (j = 0; j < m_nSymbleSize - 4; ++j)
			{
				int nCount = 1;

				for (k = j + 1; k < m_nSymbleSize; k++)
				{
					if (((m_byModuleData[j][i] & 0x11) == 0) == ((m_byModuleData[k][i] & 0x11) == 0))
						++nCount;
					else
						break;
				}

				if (nCount >= 5)
				{
					nPenalty += 3 + (nCount - 5);
				}

				j = k - 1;
			}
		}

		// 同色のモジュールブロック（２×２）
		for (i = 0; i < m_nSymbleSize - 1; ++i)
		{
			for (j = 0; j < m_nSymbleSize - 1; ++j)
			{
				if ((((m_byModuleData[i][j] & 0x11) == 0) == ((m_byModuleData[i + 1][j]		& 0x11) == 0)) &&
					(((m_byModuleData[i][j] & 0x11) == 0) == ((m_byModuleData[i]	[j + 1] & 0x11) == 0)) &&
					(((m_byModuleData[i][j] & 0x11) == 0) == ((m_byModuleData[i + 1][j + 1] & 0x11) == 0)))
				{
					nPenalty += 3;
				}
			}
		}

		// 同一列における 1:1:3:1:1 比率（暗:明:暗:明:暗）のパターン
		for (i = 0; i < m_nSymbleSize; ++i)
		{
			for (j = 0; j < m_nSymbleSize - 6; ++j)
			{
				if (((j == 0) ||				 ((m_byModuleData[i][j - 1] & 0x11)==0)) && // 明 または シンボル外
												 ( m_byModuleData[i][j]     & 0x11)!=0   && // 暗 - 1
												 ((m_byModuleData[i][j + 1] & 0x11)==0)  && // 明 - 1
												 ( m_byModuleData[i][j + 2] & 0x11)!=0   && // 暗 ┐
												 ( m_byModuleData[i][j + 3] & 0x11)!=0   && // 暗 │3
												 ( m_byModuleData[i][j + 4] & 0x11)!=0   && // 暗 ┘
												 ((m_byModuleData[i][j + 5] & 0x11)==0)  && // 明 - 1
												 ( m_byModuleData[i][j + 6] & 0x11)!=0   && // 暗 - 1
					((j == m_nSymbleSize - 7) || ((m_byModuleData[i][j + 7] & 0x11)==0)))   // 明 または シンボル外
				{
					// 前または後に4以上の明パターン
					if (((j < 2 || (m_byModuleData[i][j - 2] & 0x11)==0) && 
						 (j < 3 || (m_byModuleData[i][j - 3] & 0x11)==0) &&
						 (j < 4 || (m_byModuleData[i][j - 4] & 0x11)==0)) ||
						((j >= m_nSymbleSize - 8  || (m_byModuleData[i][j + 8]  & 0x11)==0) &&
						 (j >= m_nSymbleSize - 9  || (m_byModuleData[i][j + 9]  & 0x11)==0) &&
						 (j >= m_nSymbleSize - 10 || (m_byModuleData[i][j + 10] & 0x11)==0)))
					{
						nPenalty += 40;
					}
				}
			}
		}

		// 同一行における 1:1:3:1:1 比率（暗:明:暗:明:暗）のパターン
		for (i = 0; i < m_nSymbleSize; ++i)
		{
			for (j = 0; j < m_nSymbleSize - 6; ++j)
			{
				if (((j == 0) ||				 ((m_byModuleData[j - 1][i] & 0x11)==0)) && // 明 または シンボル外
												 ( m_byModuleData[j]    [i] & 0x11)!=0   && // 暗 - 1
												 ((m_byModuleData[j + 1][i] & 0x11)==0)  && // 明 - 1
												 ( m_byModuleData[j + 2][i] & 0x11)!=0   && // 暗 ┐
												 ( m_byModuleData[j + 3][i] & 0x11)!=0   && // 暗 │3
												 ( m_byModuleData[j + 4][i] & 0x11)!=0   && // 暗 ┘
												 ((m_byModuleData[j + 5][i] & 0x11)==0)  && // 明 - 1
												 ( m_byModuleData[j + 6][i] & 0x11)!=0   && // 暗 - 1
					((j == m_nSymbleSize - 7) || ((m_byModuleData[j + 7][i] & 0x11)==0)))   // 明 または シンボル外
				{
					// 前または後に4以上の明パターン
					if (((j < 2 || (m_byModuleData[j - 2][i] & 0x11)==0) && 
						 (j < 3 || (m_byModuleData[j - 3][i] & 0x11)==0) &&
						 (j < 4 || (m_byModuleData[j - 4][i] & 0x11)==0)) ||
						((j >= m_nSymbleSize - 8  || (m_byModuleData[j + 8][i]  & 0x11)==0) &&
						 (j >= m_nSymbleSize - 9  || (m_byModuleData[j + 9][i]  & 0x11)==0) &&
						 (j >= m_nSymbleSize - 10 || (m_byModuleData[j + 10][i] & 0x11)==0)))
					{
						nPenalty += 40;
					}
				}
			}
		}

		// 全体に対する暗モジュールの占める割合
		int nCount = 0;
		for (i = 0; i < m_nSymbleSize; ++i)
		{
			for (j = 0; j < m_nSymbleSize; ++j)
			{
				if ((m_byModuleData[i][j] & 0x11)==0)
				{
					++nCount;
				}
			}
		}
		nPenalty += (Math.abs(50 - ((nCount * 100) / (m_nSymbleSize * m_nSymbleSize))) / 5) * 10;
		return nPenalty;
	}

	/////////////////////////////////////////////////////////////////////////////
	// CQR_Encode::SetFinderPattern
	// 用  途：位置検出パターン配置
	// 引  数：配置左上座標

	static void SetFinderPattern(int x, int y)
	{
		short byPattern[] = {0x7f, 0x41, 0x5d, 0x5d, 0x5d, 0x41,0x7f};
		for(int i = 0; i < 7; ++i)
		{
			for(int j = 0; j < 7; ++j)
			{
				m_byModuleData[x + j][y + i] = (short) ((byPattern[i] & (1 << (6 - j)))>0 ? 0x30 : 0x20); 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// CQR_Encode::SetAlignmentPattern
	// 用  途：位置合わせパターン配置
	// 引  数：配置中央座標
    static void SetAlignmentPattern(int x, int y)
	{
		short byPattern[] = {0x1f, 0x11, 0x15, 0x11, 0x1f};
		if ((m_byModuleData[x][y] & 0x20)>0)
			return; // 機能モジュールと重複するため除外

		x -= 2; y -= 2; // 左上隅座標に変換
		for(int i = 0; i < 5; ++i)
		{
			for(int j = 0; j < 5; ++j)
			{
				m_byModuleData[x + j][y + i] = (short) ((byPattern[i] & (1 << (4 - j)))>0 ? 0x30 : 0x20); 
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////
	// 用  途：機能モジュール配置
	// 備  考：フォーマット情報は機能モジュール登録のみ(実データは空白)
    static void SetFunctionModule()
	{
		int i=0, j=0;

		// 位置検出パターン
		SetFinderPattern(0, 0);
		SetFinderPattern(m_nSymbleSize - 7, 0);
		SetFinderPattern(0, m_nSymbleSize - 7);

		// 位置検出パターンセパレータ
		for(i = 0; i < 8; ++i)
		{
			m_byModuleData[i][7] = m_byModuleData[7][i] = 0x20;
			m_byModuleData[m_nSymbleSize - 8][i] = m_byModuleData[m_nSymbleSize - 8 + i][7] = 0x20;
			m_byModuleData[i][m_nSymbleSize - 8] = m_byModuleData[7][m_nSymbleSize - 8 + i] = 0x20;
		}

		// フォーマット情報記述位置を機能モジュール部として登録
		for(i = 0; i < 9; ++i)
		{
			m_byModuleData[i][8] = m_byModuleData[8][i] = 0x20;
		}

		for(i = 0; i < 8; ++i)
		{
			m_byModuleData[m_nSymbleSize - 8 + i][8] = m_byModuleData[8][m_nSymbleSize - 8 + i] = 0x20;
		}

		// バージョン情報パターン
		SetVersionPattern();

		// 位置合わせパターン
		for(i = 0; i < QR_VersonInfo[m_nVersion].ncAlignPoint; ++i)
		{
			SetAlignmentPattern(QR_VersonInfo[m_nVersion].nAlignPoint[i], 6);
			SetAlignmentPattern(6, QR_VersonInfo[m_nVersion].nAlignPoint[i]);

			for(j = 0; j < QR_VersonInfo[m_nVersion].ncAlignPoint; ++j)
			{
				SetAlignmentPattern(QR_VersonInfo[m_nVersion].nAlignPoint[i], QR_VersonInfo[m_nVersion].nAlignPoint[j]);
			}
		}

		// タイミングパターン
		for(i = 8; i <= m_nSymbleSize - 9; ++i)
		{
			m_byModuleData[i][6] = (short) ((i % 2) == 0 ? 0x30 : 0x20);
			m_byModuleData[6][i] = (short) ((i % 2) == 0 ? 0x30 : 0x20);
		}
	}

/////////////////////////////////////////////////////////////////////////////
	// 用  途：バージョン(型番)情報パターン配置
	// 備  考：拡張ＢＣＨ(18,6)符号を誤り訂正として使用
static void SetVersionPattern()
	{
		if (m_nVersion <= 6) return;
		int nVerData = m_nVersion << 12;

		int i=0,j=0;
		// 剰余ビット算出
		for(i = 0; i < 6; ++i)
		{
			if ((nVerData & (1 << (17 - i))) >0)
			{
				nVerData ^= (0x1f25 << (5 - i));
			}
		}

		nVerData += m_nVersion << 12;
		for(i = 0; i < 6; ++i)
		{
			for(j = 0; j < 3; ++j)
			{
				m_byModuleData[m_nSymbleSize - 11 + j][i] = m_byModuleData[i][m_nSymbleSize - 11 + j] =	(short) ((nVerData & (1 << (i * 3 + j)))>0 ? 0x30 : 0x20);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：ＲＳ誤り訂正コードワード取得
	// 引  数：データコードワードアドレス、データコードワード長、ＲＳコードワード長
	// 備  考：総コードワード分のエリアを確保してから呼び出し
    static void GetRSCodeWord(short lpbyRSWork[], int ncDataCodeWord, int ncRSCodeWord)
	{
		int i=0, j=0;
		for(i = 0; i < ncDataCodeWord ; ++i)
		{
			if(lpbyRSWork[0] != 0)
			{
				short nExpFirst = byIntToExp[lpbyRSWork[0]]; // 初項係数より乗数算出
				for(j = 0; j < ncRSCodeWord; ++j)
				{
					// 各項乗数に初項乗数を加算（% 255 → α^255 = 1）
					short nExpElement = (short)(((byRSExp[ncRSCodeWord][j] + nExpFirst)) % 255);

					// 排他論理和による剰余算出
					lpbyRSWork[j] = (short)(lpbyRSWork[j + 1] ^ byExpToInt[nExpElement]);
				}

				// 残り桁をシフト
				for(j = ncRSCodeWord; j < ncDataCodeWord + ncRSCodeWord - 1; ++j)
					lpbyRSWork[j] = lpbyRSWork[j + 1];
			}
			else
			{
				// 残り桁をシフト
				for(j = 0; j < ncDataCodeWord + ncRSCodeWord - 1; ++j)
					lpbyRSWork[j] = lpbyRSWork[j + 1];
			}
		}
	}

	static int GetEncodeVersion(int nVersion, char lpData[], int nLen)
	{
		int i=0, j=0;
		int nVerGroup = nVersion >= 27 ? QR_VRESION_L : (nVersion >= 10 ? QR_VRESION_M : QR_VRESION_S);
		for(i = nVerGroup; i <= QR_VRESION_L; ++i)
		{
			if(EncodeSourceData(lpData, nLen, i))
			{
				if(i == QR_VRESION_S)
				{
					for(j = 1; j <= 9; ++j)
					{
						if((m_ncDataCodeWordBit + 7) / 8 <= QR_VersonInfo[j].ncDataCodeWord[m_nLevel])
							return j;
					}
				}
				else if(i == QR_VRESION_M)
				{
					for(j = 10; j <= 26; ++j)
					{
						if((m_ncDataCodeWordBit + 7) / 8 <= QR_VersonInfo[j].ncDataCodeWord[m_nLevel])
							return j;
					}
				}
				else if(i == QR_VRESION_L)
				{
					for(j = 27; j <= 40; ++j)
					{
						if((m_ncDataCodeWordBit + 7) / 8 <= QR_VersonInfo[j].ncDataCodeWord[m_nLevel])
							return j;
					}
				}
			}
		}
		return 0;
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：漢字モード該当チェック
	// 引  数：調査文字（16ビット文字）
	// 戻り値：該当時=TRUE
	// 備  考：EBBFh 以降の S-JIS は対象外
    static boolean IsKanjiData(char c1, char c2)
	{
		if(((c1 >= 0x81 && c1 <= 0x9f) || (c1 >= 0xe0 && c1 <= 0xeb)) && (c2 >= 0x40))
		{
			if((c1 == 0x9f && c2 > 0xfc) || (c1 == 0xeb && c2 > 0xbf))
				return false;
			return true;
		}
		return false;
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：数字モード該当チェック
	// 引  数：調査文字
	// 戻り値：該当時=TRUE
    static boolean IsNumeralData(char c)
	{
		if(c >= '0' && c <= '9')
			return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：英数字モード該当チェック
	// 引  数：調査文字
	// 戻り値：該当時=TRUE
    static boolean IsAlphabetData(char c)
	{
		if (c >= '0' && c <= '9')
			return true;

		if (c >= 'A' && c <= 'Z')
			return true;

		if (c == ' ' || c == '$' || c == '%' || c == '*' || c == '+' || c == '-' || c == '.' || c == '/' || c == ':')
			return true;

		return false;
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：ビット長取得
	// 引  数：データモード種別、データ長、バージョン(型番)グループ
	// 戻り値：データビット長
	// 備  考：漢字モードでのデータ長引数は文字数ではなくバイト数
    static int GetBitLength(short nMode, int ncData, int nVerGroup)
	{
		int ncBits = 0;
		switch(nMode)
		{
			case QR_MODE_NUMERAL:
				{
					ncBits = 4 + nIndicatorLenNumeral[nVerGroup] + (10 * (ncData / 3));
					switch (ncData % 3)
					{
						case 1:
							ncBits += 4;
						break;

						case 2:
							ncBits += 7;
						break;
					}
				}
			break;

			case QR_MODE_ALPHABET:
				ncBits = 4 + nIndicatorLenAlphabet[nVerGroup] + (11 * (ncData / 2)) + (6 * (ncData % 2));
			break;

			case QR_MODE_8BIT:
				ncBits = 4 + nIndicatorLen8Bit[nVerGroup] + (8 * ncData);
			break;

			case QR_MODE_KANJI:
			default:
				ncBits = 4 + nIndicatorLenKanji[nVerGroup] + (13 * (ncData / 2));
			break;
		}
		return ncBits;
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：入力データエンコード
	// 引  数：入力データ、入力データ長、バージョン(型番)グループ
	// 戻り値：エンコード成功時=TRUE
    static boolean EncodeSourceData(char lpsSource[], int ncLength, int nVerGroup)
	{
		int i=0, j=0;
		int m_ncDataBlock = 0;
		for(int index=0;index<MAX_DATACODEWORD;index++)
		{
			m_nBlockLength[index] = 0;
		}

		// どのモードが何文字(バイト)継続しているかを調査
		for(i = 0; i < ncLength; ++i)
		{
			short byMode = QR_MODE_8BIT;
			if(i < ncLength - 1 && IsKanjiData(lpsSource[i], lpsSource[i + 1]))
				byMode = QR_MODE_KANJI;
			else if(IsNumeralData(lpsSource[i]))
				byMode = QR_MODE_NUMERAL;
			else if(IsAlphabetData(lpsSource[i]))
				byMode = QR_MODE_ALPHABET;
			else
				byMode = QR_MODE_8BIT;

			if(i == 0) m_byBlockMode[0] = byMode;
			if(m_byBlockMode[m_ncDataBlock] != byMode) m_byBlockMode[++m_ncDataBlock] = byMode;

			++m_nBlockLength[m_ncDataBlock];
			if(byMode == QR_MODE_KANJI)
			{
				// 漢字は文字数ではなく	数で記録
				++m_nBlockLength[m_ncDataBlock];
				++i;
			}
		}

		++m_ncDataBlock;

		/////////////////////////////////////////////////////////////////////////
		// 隣接する英数字モードブロックと数字モードブロックの並びをを条件により結合

		int ncSrcBits, ncDstBits; // 元のビット長と単一の英数字モードブロック化した場合のビット長
		int nBlock = 0;

		while(nBlock < m_ncDataBlock - 1)
		{
			int ncJoinFront, ncJoinBehind; // 前後８ビットバイトモードブロックと結合した場合のビット長
			int nJoinPosition = 0; // ８ビットバイトモードブロックとの結合：-1=前と結合、0=結合しない、1=後ろと結合

			// 「数字－英数字」または「英数字－数字」の並び
			if((m_byBlockMode[nBlock] == QR_MODE_NUMERAL  && m_byBlockMode[nBlock + 1] == QR_MODE_ALPHABET) ||
				(m_byBlockMode[nBlock] == QR_MODE_ALPHABET && m_byBlockMode[nBlock + 1] == QR_MODE_NUMERAL))
			{
				// 元のビット長と単一の英数字モードブロック化した場合のビット長を比較
				ncSrcBits = GetBitLength(m_byBlockMode[nBlock], m_nBlockLength[nBlock], nVerGroup) +
							GetBitLength(m_byBlockMode[nBlock + 1], m_nBlockLength[nBlock + 1], nVerGroup);

				ncDstBits = GetBitLength(QR_MODE_ALPHABET, m_nBlockLength[nBlock] + m_nBlockLength[nBlock + 1], nVerGroup);

				if(ncSrcBits > ncDstBits)
				{
					// 前後に８ビットバイトモードブロックがある場合、それらとの結合が有利かどうかをチェック
					if(nBlock >= 1 && m_byBlockMode[nBlock - 1] == QR_MODE_8BIT)
					{
						// 前に８ビットバイトモードブロックあり
						ncJoinFront = GetBitLength(QR_MODE_8BIT, m_nBlockLength[nBlock - 1] + m_nBlockLength[nBlock], nVerGroup) +
									  GetBitLength(m_byBlockMode[nBlock + 1], m_nBlockLength[nBlock + 1], nVerGroup);

						if(ncJoinFront > ncDstBits + GetBitLength(QR_MODE_8BIT, m_nBlockLength[nBlock - 1], nVerGroup))
							ncJoinFront = 0; // ８ビットバイトモードブロックとは結合しない
					}
					else
						ncJoinFront = 0;

					if(nBlock < m_ncDataBlock - 2 && m_byBlockMode[nBlock + 2] == QR_MODE_8BIT)
					{
						// 後ろに８ビットバイトモードブロックあり
						ncJoinBehind = GetBitLength(m_byBlockMode[nBlock], m_nBlockLength[nBlock], nVerGroup) +
									   GetBitLength(QR_MODE_8BIT, m_nBlockLength[nBlock + 1] + m_nBlockLength[nBlock + 2], nVerGroup);

						if(ncJoinBehind > ncDstBits + GetBitLength(QR_MODE_8BIT, m_nBlockLength[nBlock + 2], nVerGroup))
							ncJoinBehind = 0; // ８ビットバイトモードブロックとは結合しない
					}
					else
						ncJoinBehind = 0;

					if(ncJoinFront != 0 && ncJoinBehind != 0)
					{
						// 前後両方に８ビットバイトモードブロックがある場合はデータ長が短くなる方を優先
						nJoinPosition = (ncJoinFront < ncJoinBehind) ? -1 : 1;
					}
					else
					{
						nJoinPosition = (ncJoinFront != 0) ? -1 : ((ncJoinBehind != 0) ? 1 : 0);
					}

					if(nJoinPosition != 0)
					{
						// ８ビットバイトモードブロックとの結合
						if(nJoinPosition == -1)
						{
							m_nBlockLength[nBlock - 1] += m_nBlockLength[nBlock];

							// 後続をシフト
							for(i = nBlock; i < m_ncDataBlock - 1; ++i)
							{
								m_byBlockMode[i]  = m_byBlockMode[i + 1];
								m_nBlockLength[i] = m_nBlockLength[i + 1];
							}
						}
						else
						{
							m_byBlockMode[nBlock + 1] = QR_MODE_8BIT;
							m_nBlockLength[nBlock + 1] += m_nBlockLength[nBlock + 2];

							// 後続をシフト
							for(i = nBlock + 2; i < m_ncDataBlock - 1; ++i)
							{
								m_byBlockMode[i]  = m_byBlockMode[i + 1];
								m_nBlockLength[i] = m_nBlockLength[i + 1];
							}
						}
						--m_ncDataBlock;
					}
					else
					{
						// 英数字と数字の並びを単一の英数字モードブロックに統合

						if(nBlock < m_ncDataBlock - 2 && m_byBlockMode[nBlock + 2] == QR_MODE_ALPHABET)
						{
							// 結合しようとするブロックの後ろに続く英数字モードブロックを結合
							m_nBlockLength[nBlock + 1] += m_nBlockLength[nBlock + 2];

							// 後続をシフト
							for(i = nBlock + 2; i < m_ncDataBlock - 1; ++i)
							{
								m_byBlockMode[i]  = m_byBlockMode[i + 1];
								m_nBlockLength[i] = m_nBlockLength[i + 1];
							}
							--m_ncDataBlock;
						}

						m_byBlockMode[nBlock] = QR_MODE_ALPHABET;
						m_nBlockLength[nBlock] += m_nBlockLength[nBlock + 1];

						// 後続をシフト
						for(i = nBlock + 1; i < m_ncDataBlock - 1; ++i)
						{
							m_byBlockMode[i]  = m_byBlockMode[i + 1];
							m_nBlockLength[i] = m_nBlockLength[i + 1];
						}

						--m_ncDataBlock;

						if(nBlock >= 1 && m_byBlockMode[nBlock - 1] == QR_MODE_ALPHABET)
						{
							// 結合したブロックの前の英数字モードブロックを結合
							m_nBlockLength[nBlock - 1] += m_nBlockLength[nBlock];

							// 後続をシフト
							for(i = nBlock; i < m_ncDataBlock - 1; ++i)
							{
								m_byBlockMode[i]  = m_byBlockMode[i + 1];
								m_nBlockLength[i] = m_nBlockLength[i + 1];
							}
							--m_ncDataBlock;
						}
					}
					continue; // 現在位置のブロックを再調査
				}
			}
			++nBlock; // 次ブロックを調査
		}

		/////////////////////////////////////////////////////////////////////////
		// 連続する短いモードブロックを８ビットバイトモードブロック化

		nBlock = 0;
		while(nBlock < m_ncDataBlock - 1)
		{
			ncSrcBits = GetBitLength(m_byBlockMode[nBlock], m_nBlockLength[nBlock], nVerGroup)
						+ GetBitLength(m_byBlockMode[nBlock + 1], m_nBlockLength[nBlock + 1], nVerGroup);

			ncDstBits = GetBitLength(QR_MODE_8BIT, m_nBlockLength[nBlock] + m_nBlockLength[nBlock + 1], nVerGroup);

			// 前に８ビットバイトモードブロックがある場合、重複するインジケータ分を減算
			if(nBlock >= 1 && m_byBlockMode[nBlock - 1] == QR_MODE_8BIT)
				ncDstBits -= (4 + nIndicatorLen8Bit[nVerGroup]);

			// 後ろに８ビットバイトモードブロックがある場合、重複するインジケータ分を減算
			if(nBlock < m_ncDataBlock - 2 && m_byBlockMode[nBlock + 2] == QR_MODE_8BIT)
				ncDstBits -= (4 + nIndicatorLen8Bit[nVerGroup]);

			if(ncSrcBits > ncDstBits)
			{
				if(nBlock >= 1 && m_byBlockMode[nBlock - 1] == QR_MODE_8BIT)
				{
					// 結合するブロックの前にある８ビットバイトモードブロックを結合
					m_nBlockLength[nBlock - 1] += m_nBlockLength[nBlock];

					// 後続をシフト
					for(i = nBlock; i < m_ncDataBlock - 1; ++i)
					{
						m_byBlockMode[i]  = m_byBlockMode[i + 1];
						m_nBlockLength[i] = m_nBlockLength[i + 1];
					}

					--m_ncDataBlock;
					--nBlock;
				}

				if(nBlock < m_ncDataBlock - 2 && m_byBlockMode[nBlock + 2] == QR_MODE_8BIT)
				{
					// 結合するブロックの後ろにある８ビットバイトモードブロックを結合
					m_nBlockLength[nBlock + 1] += m_nBlockLength[nBlock + 2];

					// 後続をシフト
					for(i = nBlock + 2; i < m_ncDataBlock - 1; ++i)
					{
						m_byBlockMode[i]  = m_byBlockMode[i + 1];
						m_nBlockLength[i] = m_nBlockLength[i + 1];
					}
					--m_ncDataBlock;
				}

				m_byBlockMode[nBlock] = QR_MODE_8BIT;
				m_nBlockLength[nBlock] += m_nBlockLength[nBlock + 1];

				// 後続をシフト
				for(i = nBlock + 1; i < m_ncDataBlock - 1; ++i)
				{
					m_byBlockMode[i]  = m_byBlockMode[i + 1];
					m_nBlockLength[i] = m_nBlockLength[i + 1];
				}

				--m_ncDataBlock;

				// 結合したブロックの前から再調査
				if(nBlock >= 1) --nBlock;
				continue;
			}
			++nBlock; // 次ブロックを調査
		}

		/////////////////////////////////////////////////////////////////////////
		// ビット配列化
		int ncComplete = 0; // 処理済データカウンタ
		int wBinCode;
		m_ncDataCodeWordBit = 0; // ビット単位処理カウンタ
		for(int index=0;index<MAX_DATACODEWORD;index++)
		{
			m_byDataCodeWord[index] = 0;
		}

		for(i = 0; i < m_ncDataBlock && m_ncDataCodeWordBit != -1; ++i)
		{
			if(m_byBlockMode[i] == QR_MODE_NUMERAL)
			{
				/////////////////////////////////////////////////////////////////
				// 数字モード

				// インジケータ(0001b)
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, 1, 4); 

				// 文字数セット
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, m_nBlockLength[i], nIndicatorLenNumeral[nVerGroup]);

				// ビット列保存
				for(j = 0; j < m_nBlockLength[i]; j += 3)
				{
					if(j < m_nBlockLength[i] - 2)
					{
						wBinCode = (((lpsSource[ncComplete + j]	  - '0') * 100) +
										  ((lpsSource[ncComplete + j + 1] - '0') * 10) +
										   (lpsSource[ncComplete + j + 2] - '0'));

						m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, wBinCode, 10);
					}
					else if (j == m_nBlockLength[i] - 2)
					{
						// 端数２バイト
						wBinCode = (((lpsSource[ncComplete + j] - '0') * 10) +
										   (lpsSource[ncComplete + j + 1] - '0'));

						m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, wBinCode, 7);
					}
					else if (j == m_nBlockLength[i] - 1)
					{
						// 端数１バイト
						wBinCode = (lpsSource[ncComplete + j] - '0');

						m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, wBinCode, 4);
					}
				}

				ncComplete += m_nBlockLength[i];
			}

			else if(m_byBlockMode[i] == QR_MODE_ALPHABET)
			{
				/////////////////////////////////////////////////////////////////
				// 英数字モード

				// モードインジケータ(0010b)
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, 2, 4);

				// 文字数セット
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, m_nBlockLength[i], nIndicatorLenAlphabet[nVerGroup]);

				// ビット列保存
				for(j = 0; j < m_nBlockLength[i]; j += 2)
				{
					if(j < m_nBlockLength[i] - 1)
					{
						wBinCode = ((AlphabetToBinaly(lpsSource[ncComplete + j]) * 45) +
										   AlphabetToBinaly(lpsSource[ncComplete + j + 1]));

						m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, wBinCode, 11);
					}
					else
					{
						// 端数１バイト
						wBinCode = AlphabetToBinaly(lpsSource[ncComplete + j]);

						m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, wBinCode, 6);
					}
				}

				ncComplete += m_nBlockLength[i];
			}

			else if(m_byBlockMode[i] == QR_MODE_8BIT)
			{
				/////////////////////////////////////////////////////////////////
				// ８ビットバイトモード

				// モードインジケータ(0100b)
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, 4, 4);

				// 文字数セット
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, m_nBlockLength[i], nIndicatorLen8Bit[nVerGroup]);

				// ビット列保存
				for(j = 0; j < m_nBlockLength[i]; ++j)
				{
					m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, lpsSource[ncComplete + j], 8);
				}

				ncComplete += m_nBlockLength[i];
			}
			else // m_byBlockMode[i] == QR_MODE_KANJI
			{
				/////////////////////////////////////////////////////////////////
				// 漢字モード

				// モードインジケータ(1000b)
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, 8, 4);

				// 文字数セット
				m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, (m_nBlockLength[i] / 2), nIndicatorLenKanji[nVerGroup]);

				// 漢字モードでビット列保存
				for(j = 0; j < m_nBlockLength[i] / 2; ++j)
				{
					int wBinCode1 = KanjiToBinaly((((short)lpsSource[ncComplete + (j * 2)] << 8) + (short)lpsSource[ncComplete + (j * 2) + 1]));

					m_ncDataCodeWordBit = SetBitStream(m_ncDataCodeWordBit, wBinCode1, 13);
				}

				ncComplete += m_nBlockLength[i];
			}
		}
		return (m_ncDataCodeWordBit != -1);
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：漢字モード文字のバイナリ化
	// 引  数：対象文字
	// 戻り値：バイナリ値
    static int KanjiToBinaly(int wc)
	{
		if (wc >= 0x8140 && wc <= 0x9ffc)
			wc -= 0x8140;
		else // (wc >= 0xe040 && wc <= 0xebbf)
			wc -= 0xc140;
		return (((wc >> 8) * 0xc0) + (wc & 0x00ff));
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：英数字モード文字のバイナリ化
	// 引  数：対象文字
	// 戻り値：バイナリ値
    static short AlphabetToBinaly(char c)
	{
		if (c >= '0' && c <= '9') return (short)(c - '0');

		if (c >= 'A' && c <= 'Z') return (short)(c - 'A' + 10);

		if (c == ' ') return 36;

		if (c == '$') return 37;

		if (c == '%') return 38;

		if (c == '*') return 39;

		if (c == '+') return 40;

		if (c == '-') return 41;

		if (c == '.') return 42;

		if (c == '/') return 43;

		return 44; // c == ':'
	}

	/////////////////////////////////////////////////////////////////////////////
	// 用  途：ビットセット
	// 引  数：挿入位置、ビット配列データ、データビット長(最大16)
	// 戻り値：次回挿入位置(バッファオーバー時=-1)
	// 備  考：m_byDataCodeWord に結果をセット(要ゼロ初期化)
    static int SetBitStream(int nIndex, int wData, int ncData)
	{
		if(nIndex == -1 || nIndex + ncData > MAX_DATACODEWORD * 8)
			return -1;

		for(int i = 0; i < ncData; ++i)
		{
			if((wData & (1 << (ncData - i - 1))) >0)
			{
				m_byDataCodeWord[(nIndex + i) / 8] |= 1 << (7 - ((nIndex + i) % 8));
			}
		}
		return nIndex + ncData;
	}
}
