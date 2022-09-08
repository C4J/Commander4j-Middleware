package com.commander4j.sys;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.swing.Icon;

import com.commander4j.email.EmailQueue;
import com.commander4j.gui.JListRenderer;
import com.commander4j.mw.StartMain;
import com.commander4j.mw.StartService;
import com.commander4j.util.JImageIconLoader;

public class Common
{

	public static final JImageIconLoader imageIconloader = new JImageIconLoader();

	public static String logDir = "";
	public final static String image_path = System.getProperty("user.dir") + File.separator + "images" + File.separator;

	public final static String image_cancel = "cancel.gif";
	public final static String image_ok = "ok.gif";
	public final static String image_error = "error.gif";
	public final static String image_close = "exit.gif";
	public final static String image_confirm = "CMD_Icon.gif";
	public final static String image_interface = "interface.gif";

	public static Icon icon_ok = Common.imageIconloader.getImageIcon(Common.image_ok);
	public final static Icon icon_cancel = Common.imageIconloader.getImageIcon(Common.image_cancel);
	public final static Icon icon_error = Common.imageIconloader.getImageIcon(Common.image_error);
	public final static Icon icon_close = Common.imageIconloader.getImageIcon(Common.image_close);
	public final static Icon icon_confirm = Common.imageIconloader.getImageIcon(Common.image_confirm);
	public final static Icon icon_interface = Common.imageIconloader.getImageIcon(Common.image_interface);

	public final static Font font_list = new Font("Monospaced", 0, 11);
	public final static Color color_listBackground = new Color(243, 251, 255);
	public static final JListRenderer renderer_list = new JListRenderer();

	public final static Color color_listHighlighted = new Color(184, 207, 229);

	public final static Color color_listFontSelected = Color.BLACK;
	public final static Color color_listFontStandard = Color.BLUE;

	public final static StartMain smw = new StartMain();

	public static Integer ArchiveRetentionDays = 30;

	//public static StartGUI sgui;
	public static StartService sservice;
	public static CheckboxMenuItem runningStatus = new CheckboxMenuItem("Running...");
	public static String runMode = "";
	public static Boolean emailEnabled = true;
	public static String configName = "";
	public static EmailQueue emailqueue = new EmailQueue();
	public static String statusReportTime = "00:00:00";
	
	public static Integer retryOpenFileCount = 0;
	public static Integer retryOpenFileDelay = 1000;

}
