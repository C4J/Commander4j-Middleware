package com.commander4j.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.StringWriter;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.w3c.dom.Document;

public class Utility
{
	
	public static GraphicsDevice getGraphicsDevice()
	{
		GraphicsDevice result;

		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] devices;

		try
		{
			devices = ge.getScreenDevices();

			GraphicsDevice currentDevice = null;

			for (GraphicsDevice device : devices)
			{
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				if (bounds.contains(mouseLocation))
				{
					currentDevice = device;
					break;
				}
			}

			GraphicsDevice[] gs = ge.getScreenDevices();

			String defaultID = currentDevice.getIDstring();

			int monitorIndex = 0;

			for (int x = 0; x < gs.length; x++)
			{
				if (gs[x].getIDstring().equals(defaultID))
				{
					monitorIndex = x;
					break;
				}
			}

			result = gs[monitorIndex];
		}
		catch (HeadlessException ex)
		{
			result = null;
		}

		return result;
	}

	
	public Timestamp getTimeStampFromISOString(String isoString)
	{
		Timestamp result;

		// 2010-05-31T10:14:49
		// 0123456789111111111
		// 012345678
		try
		{
			Calendar caldate = Calendar.getInstance();

			int year = 0;
			int month = 0;
			int day = 0;
			int hour = 0;
			int min = 0;
			int second = 0;

			year = Integer.valueOf(isoString.substring(0, 4));
			month = Integer.valueOf(isoString.substring(5, 7));
			day = Integer.valueOf(isoString.substring(8, 10));

			try
			{
				hour = Integer.valueOf(isoString.substring(11, 13));
			}
			catch (Exception ex)
			{
			}

			try
			{
				min = Integer.valueOf(isoString.substring(14, 16));
			}
			catch (Exception ex)
			{
			}

			try
			{
				second = Integer.valueOf(isoString.substring(17, 19));
			}
			catch (Exception ex)
			{
			}

			if ((month < 1) | (month > 12))
			{
				throw new Exception("Invalid month " + String.valueOf(month));
			}

			if ((day < 1) | (day > 31))
			{
				throw new Exception("Invalid day " + String.valueOf(day));
			}

			caldate.set(year, month - 1, day, hour, min, second);

			result = new Timestamp(caldate.getTimeInMillis());

			result.setNanos(0); // or other value
		}
		catch (Exception ex)
		{
			result = null;
		}

		return result;
	}
		
	public void retryDelay(int milliseconds)
	{
		
		try
		{
			Thread.sleep(milliseconds);
		}
		catch (Exception e)
		{

		}
	}
	
	public String getISODateTimeString()
	{
		String result = "";
		Timestamp ts = getSQLDateTime();
		try
		{
			String temp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(ts);
			result = temp.substring(0, 4);
			result = result + "-";
			result = result + temp.substring(5, 7);
			result = result + "-";
			result = result + temp.substring(8, 10);
			result = result + "T";
			result = result + temp.substring(11, 13);
			result = result + ":";
			result = result + temp.substring(14, 16);
			result = result + ":";
			result = result + temp.substring(17, 19);
		}
		catch (Exception ex)
		{
			result = "Error";
		}
		return result;
	}
	
	public String getFilenameFromPath(String path)
	{
		String result = "";
		String temp = replaceNullStringwithBlank(path);
		int size = temp.length();

		if (size > 0)
		{
			for (int x = size; x > 0; x--)
			{
				if (temp.substring(x - 1, x).equals("\\"))
				{
					break;
				}
				if (temp.substring(x - 1, x).equals("/"))
				{
					break;
				}
				result = temp.substring(x - 1, x) + result;
			}
		}

		return result;
	}
	
	public long compareTwoTimeStamps(java.sql.Timestamp currentTime, java.sql.Timestamp oldTime)
	{
	  long milliseconds1 = oldTime.getTime();
	  long milliseconds2 = currentTime.getTime();

	  long diff = milliseconds2 - milliseconds1;
	  //long diffSeconds = diff / 1000;
	  long diffMinutes = diff / (60 * 1000);
	  //long diffHours = diff / (60 * 60 * 1000);
	  //long diffDays = diff / (24 * 60 * 60 * 1000);

	   return diffMinutes;
	}
	
	public String getClientName()
	{
		String result = "";
		String clientname = "";

		try
		{
			clientname = System.getenv("Clientname").toString();

			if (clientname.equals("Console"))
			{
				clientname = "unknown";
			}
		} catch (Exception e)
		{
			clientname = "unknown";
		}

		if (clientname.equals("unknown"))
		{
			try
			{
				clientname = InetAddress.getLocalHost().getHostName().toLowerCase();
			} catch (Exception e)
			{
				clientname = "unknown";
			}
		}

		result = left(clientname, 40);

		return result;
	}
	
	public String left(String inputstr, int size)
	{
		String result = replaceNullStringwithBlank(inputstr);

		if (size > inputstr.length())
		{
			size = inputstr.length();
		}

		if (size >= 0)
		{
			result = inputstr.substring(0, size);
		} else
		{
			result = "";
		}

		return result;
	}
	
	public String getISODateStringFormat(Date ts)
	{
		String result = "";

		try
		{
			String temp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(ts);
			// String temp = ts.toString(); 0123456789012345678
			result = temp.substring(0, 4);
			result = result + "-";
			result = result + temp.substring(5, 7);
			result = result + "-";
			result = result + temp.substring(8, 10);

		} catch (Exception ex)
		{
			result = "Error";
		}

		return result;
	}

	public String getISODateStringFromCalendar(Calendar cal)
	{
		String result = "";
		
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		result = format1.format(cal.getTime());

		return result;
	}
	
	public final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public String getDateTimeString(String fmt)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	public String getCurrentTimeStampString()
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}
	
	public Timestamp getSQLDateTime()
	{
		Calendar caldate = Calendar.getInstance();
		Timestamp t = new Timestamp(caldate.getTimeInMillis());
		t.setTime(caldate.getTimeInMillis());

		return t;
	}

	public synchronized String nvl(String value, String defaultValue)
	{
		String result = "";

		if (value == null)
		{
			value = "";
		}

		result = value;

		if (result.equals(""))
		{
			result = defaultValue;
		}

		return result;
	}

	public synchronized String getStringFromDocument(Document doc)
	{

		try
		{
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (Exception ex)
		{
			return " Error displaying document(data)";
		}
	}

	public String replaceNullObjectwithBlank(Object value)
	{
		String result = "";

		if (value != null)
		{
			result = value.toString();
		}

		return result;
	}

	public String replaceNullStringwithBlank(String value)
	{
		if (value == null)
		{
			value = "";
		}

		return value;
	}

	public void pause(int value)
	{
		for (int x = 0; x < value; x++)
		{
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{

				e.printStackTrace();
			}
		}
	}

	public void initLogging(String filename)
	{
		if (filename.isEmpty())
		{
			filename = "xml" + File.separator + "config" + File.separator + "log4j2.xml";
		}

		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		File file = new File(filename);

		// this will force a reconfiguration
		context.setConfigLocation(file.toURI());

	}

	public String getLogFilename(String filename)
	{
		return System.getProperty("user.dir") + File.separator + "interface" + File.separator + "log" + File.separator + filename;
	}

	public String getISOTimeStampStringFormat(Timestamp ts)
	{
		String result = "";

		try
		{
			String temp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
			// String temp = ts.toString(); 0123456789012345678
			result = temp.substring(0, 4);
			result = result + "-";
			result = result + temp.substring(5, 7);
			result = result + "-";
			result = result + temp.substring(8, 10);
			result = result + "T";
			result = result + temp.substring(11, 13);
			result = result + ":";
			result = result + temp.substring(14, 16);
			result = result + ":";
			result = result + temp.substring(17, 19);
		} catch (Exception ex)
		{
			result = "Error";
		}

		return result;
	}

	public String padSpace(int size)
	{
		String s = "";

		for (int i = 0; i < size; i++)
		{
			s = s + " ";
		}

		return s;
	}

	public String padString(int size, String character)
	{
		String s = "";

		for (int i = 0; i < size; i++)
		{
			s = s + character;
		}

		return s;
	}

	public String padString(String input, boolean right, int size, String character)
	{
		int inputlength = 0;
		String result = replaceNullStringwithBlank(input);

		inputlength = result.length();

		if (inputlength > size)
		{
			// result = result.substring(0,size-1);
			result = result.substring(0, size);
		} else
		{
			if (inputlength < size)
			{
				if (right == true)
				{
					result = result + padString(size - inputlength, character);
				} else
				{
					result = padString(size - inputlength, character) + result;
				}
			}
		}

		return result;
	}

}
