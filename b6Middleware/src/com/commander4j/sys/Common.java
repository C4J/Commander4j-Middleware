package com.commander4j.sys;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.swing.Icon;

import com.commander4j.email.EmailQueue;
import com.commander4j.gui.JListRenderer;
import com.commander4j.mw.Core;
import com.commander4j.prop.JProp;
import com.commander4j.util.JImageIconLoader;

public class Common
{

	public static final JImageIconLoader imageIconloader = new JImageIconLoader();
	
	public final static String image_path = System.getProperty("user.dir") + File.separator + "images" + File.separator;

	public final static String image_cancel = "cancel.gif";
	public final static String image_ok = "ok.gif";
	public final static String image_error = "error.gif";
	public final static String image_close = "exit.gif";
	public final static String image_help = "help.gif";
	public final static String image_refresh = "refresh.gif";
	public final static String image_confirm = "CMD_Icon.gif";
	public final static String image_interface = "interface.gif";
	public static String helpURL = "http://wiki.commander4j.com";
	
	public final static Icon icon_ok = Common.imageIconloader.getImageIcon(Common.image_ok);
	public final static Icon icon_cancel = Common.imageIconloader.getImageIcon(Common.image_cancel);
	public final static Icon icon_error = Common.imageIconloader.getImageIcon(Common.image_error);
	public final static Icon icon_help = Common.imageIconloader.getImageIcon(Common.image_help);
	public final static Icon icon_close = Common.imageIconloader.getImageIcon(Common.image_close);
	public final static Icon icon_refresh = Common.imageIconloader.getImageIcon(Common.image_refresh);
	public final static Icon icon_confirm = Common.imageIconloader.getImageIcon(Common.image_confirm);
	public final static Icon icon_interface = Common.imageIconloader.getImageIcon(Common.image_interface);

	public final static Font font_std = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_list = new Font("Monospaced", 0, 11);
	public final static Font font_btn = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_bold = new Font("Arial", Font.BOLD, 11);

	public static final JListRenderer renderer_list = new JListRenderer();

	public final static Color color_listHighlighted = new Color(184, 207, 229);

	public final static Color color_listFontSelected = Color.BLACK;
	public final static Color color_listFontStandard = Color.BLUE;
	public final static Color color_app_window = new Color(241, 241, 241);
	public final static Color color_button = new Color(233,236,242);
	public final static Color color_button_hover =  new Color(214, 214, 214);
	public final static Color color_button_font = Color.black;
	public final static Color color_button_font_hover = Color.black;
	public final static Color color_listBackground = new Color(243, 251, 255);
	public final static Core core = new Core();
	
	public static CheckboxMenuItem runningStatus = new CheckboxMenuItem("Running...");
	public static String runMode = "";

	public static EmailQueue emailqueue = new EmailQueue();
	
	public static String pathRoot = "config";
	public static String pathMap = pathRoot+"//maps//{mapId}";
	public static String pathMapInput = pathMap+"//connectors//{inId}";
	public static String pathMapOutput = pathMap+"//connectors//{outId}";
	public static JProp props = new JProp(pathRoot);
	
	public static int LFAdjustWidth=0;
	public static int LFAdjustHeight=0;
	public static int LFTreeMenuAdjustWidth=0;
	public static int LFTreeMenuAdjustHeight=0;
	
}
