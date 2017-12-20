package com.reptile.dd;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface DD  extends Library{
	  DD INSTANCE = (DD)Native.loadLibrary("dd74000x32", DD.class);//32位系统
//	  DD INSTANCE = (DD)Native.loadLibrary("dd74000x64.64", DD.class);//64位系统
	   //64位JAVA调用*64.dll, 32位调用*32.dll 。与系统本身位数无关。
	   public int DD_mov(int x, int y);
	   public int DD_movR(int dx, int dy);
	   public int DD_btn(int btn);
	   public int DD_whl(int whl);
	   public int DD_key(int ddcode, int flag);
	   public int DD_str(String s);  
}
