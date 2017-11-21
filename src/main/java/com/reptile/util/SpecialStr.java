package com.reptile.util;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.reptile.winio.User32;
import com.reptile.winio.User32Util;
import com.reptile.winio.VKMapping;
import com.reptile.winio.VirtualKeyBoard;
import com.reptile.winio.WinIo32;

/**
 * 
 * @author Bigyoung
 * @version V1.0
 * @Description winio特殊字符处理类
 * @date 2017-11-13 2:50
 *
 */
public class SpecialStr {
	public static final WinIo32		winIo32		= WinIo32.INSTANCE;
	private static final HttpSession	HttpSession	= null;
	private static CYDMDemo			cydmDemo	= new CYDMDemo();
	Resttemplate				resttemplate	= new Resttemplate();
	application				application	= new application();
	Logger					logger		= Logger.getLogger( VirtualKeyBoard.class );
	static {
		if ( !WinIo32.INSTANCE.InitializeWinIo() )
		{
			System.err.println( "Cannot Initialize the WinIO" );
			System.exit( 1 );
		}
	}


	public static void KeyDown( int key ) throws Exception
	{
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal( WinIo32.CONTROL_PORT, 0xd2, 1 );
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal( WinIo32.DATA_PORT, key, 1 );
	}


	public static void KeyUp( int key ) throws Exception
	{
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal( WinIo32.CONTROL_PORT, 0xd2, 1 );
		User32Util.KBCWait4IBE();
		winIo32.SetPortVal( WinIo32.DATA_PORT, (key | 0x80), 1 );
	}


	public static void KeyPress( char key ) throws Exception
	{
		KeyPress( VKMapping.toVK( "" + key ) );
	}


	public static void KeyPresss( String key ) throws Exception
	{
		KeyPress( VKMapping.toVK( "" + key ) );
	}


	public static void KeyPress( int vk ) throws Exception
	{
		int scan = User32.INSTANCE.MapVirtualKey( vk, 0 );
		KeyDown( scan );
		KeyUp( scan );
	}


	public static void KeyPressKeyDown( int vk ) throws Exception
	{
		int scan = User32.INSTANCE.MapVirtualKey( vk, 0 );
		KeyDown( scan );
	}


	public static void KeyPressKeyUp( int vk ) throws Exception
	{
		int scan = User32.INSTANCE.MapVirtualKey( vk, 0 );
		KeyUp( scan );
	}
	public static void ShiftDown() throws Exception
	{
		KeyPressKeyDown(VKMapping.toVK( "Shift" ));
		Thread.sleep( 50 );
	}
	public static void ShiftUp() throws Exception
	{
		KeyPressKeyUp(VKMapping.toVK( "Shift" ));
		Thread.sleep( 50 );
	}
	
	
	
		public static void SpecialStr(String str) throws Exception{
		
		for (int i = 0; i < str.length(); i++) {
		Thread.sleep( 50 );
		String number = String.valueOf( str.charAt( i ) );	
		/*判断是否是数字*/
		if( StringUtils.isNumeric( number)){
			KeyPress( str.charAt(i) );
		/*判断是否是大写*/
		}else if(Character.isUpperCase( str.charAt( i ) )){
			ShiftDown();
			KeyPress( number.toLowerCase().charAt( 0 ) );
			ShiftUp();	
		/*判断是否小写*/
		}else if(Character.isLowerCase(str.charAt( i ))){
			Thread.sleep( 50 );
			KeyPress( str.charAt( i ) );
		}else{
		/*特殊字符处理*/
			switch (number) {
			case "@":
				ShiftDown();
				KeyPresss( "2" );
				ShiftUp();
			break;
			case "!":
				ShiftDown();
				KeyPresss( "1" );
				ShiftUp();
				break;
			case "#":
				ShiftDown();
				KeyPresss( "3" );
				ShiftUp();
				break;
			case "$":
				ShiftDown();
				KeyPresss( "4" );
				ShiftUp();
				break;
			case "%":
				ShiftDown();
				KeyPresss( "5" );
				ShiftUp();
				break;
			case "^":
				ShiftDown();
				KeyPresss( "6" );
				ShiftUp();
				break;
			case "&":
				ShiftDown();
				KeyPresss( "7" );
				ShiftUp();
				break;
			case "*":
				ShiftDown();
				KeyPresss( "8" );
				ShiftUp();
				break;
			case "(":
				ShiftDown();
				KeyPresss( "9" );
				ShiftUp();
				break;
			case ")":
				ShiftDown();
				KeyPresss( "0" );
				ShiftUp();
				break;
	
			case ".":
				KeyPress( str.charAt( i ) );
				break;

			default:
				break;
			}
			
		}
			
		}
			
			
		}
		
	}


