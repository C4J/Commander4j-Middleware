package com.commander4j.email;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import com.commander4j.sys.Common;
import com.commander4j.util.EncryptData;
import com.commander4j.util.JCipher;
import com.commander4j.util.JXMLDocument;
import com.commander4j.util.Utility;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.Multipart;

public class SendEmail
{
	Logger logger = org.apache.logging.log4j.LogManager.getLogger((SendEmail.class));

	private Properties smtpProperties;
	private HashMap<String, distributionList> distList = new HashMap<String, distributionList>();
	private HashMap<String, Calendar> emailLog = new HashMap<String, Calendar>();
	private Utility util = new Utility();
	private JCipher cipher = new JCipher(EncryptData.key);

	public void init(String distributionID)
	{

		if (distList.containsKey(distributionID) == false)
		{
			String filename = System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "email.xml";

			JXMLDocument doc = new JXMLDocument(filename);

			smtpProperties = System.getProperties();
			Boolean cont = true;
			int seq = 1;
			while (cont)
			{
				String prop = doc.findXPath("//configuration/property[" + String.valueOf(seq) + "]/@name").trim();
				String val = doc.findXPath("//configuration/property[" + String.valueOf(seq) + "]/@value").trim();
				String encrypted = doc.findXPath("//configuration/property[" + String.valueOf(seq) + "]/@encrypted").trim().toLowerCase();
				
				if (encrypted.equals(""))
				{
					encrypted = "no";
				}
				
				if (encrypted.equals("yes"))
				{
					val = cipher.decode(val);
				}
				
				if (prop.equals(""))
				{
					cont = false;
				}
				else
				{
					smtpProperties.setProperty(prop, val);
					seq++;
				}
			}

			String addressList = "";
			String enabled = "";
			addressList = util.replaceNullStringwithBlank(doc.findXPath("//emailSettings/distributionList[@id='" + distributionID + "'][@enabled='Y']/toAddressList").trim());
			String temp = util.replaceNullStringwithBlank(doc.findXPath("//emailSettings/distributionList[@id='" + distributionID + "'][@enabled='Y']/@maxFrequencyMins").trim());
			enabled = util.replaceNullStringwithBlank(doc.findXPath("//emailSettings/distributionList[@id='" + distributionID + "'][@enabled='Y']/@enabled").trim());

			if (temp.equals(""))
				temp = "0";

			distributionList newItem = new distributionList();

			newItem.listId = distributionID;
			newItem.addressList = addressList;
			newItem.maxFrequencyMins = Long.valueOf(temp);
			newItem.enabled = enabled.toUpperCase();

			distList.put(distributionID, newItem);
		}
	}

	public synchronized boolean Send(String distributionID, String subject, String messageText, String filename)
	{
		boolean result = true;

		if (Common.emailEnabled == false)
		{
			return result;
		}

		if (distList.containsKey(distributionID) == false)
		{
			init(distributionID);
		}

		if (distList.containsKey(distributionID) == true)
		{

			if (distList.get(distributionID).enabled.equals("Y"))
			{
				String emailKey = "[" + distributionID + "] - [" + subject + "]";
				logger.debug(emailKey);

				Calendar lastSent  = Calendar.getInstance();
				Calendar now = Calendar.getInstance();

				if (emailLog.containsKey(emailKey))
				{
					lastSent = emailLog.get(emailKey);
				}
				else
				{
					lastSent.add(Calendar.DATE, -1);
					emailLog.put(emailKey, lastSent);
				}

				long seconds = (now.getTimeInMillis() - lastSent.getTimeInMillis()) / 1000;

				long ageInMins = seconds / 60;

				logger.debug("Last email to " + emailKey + " was at " + util.getISODateStringFromCalendar(lastSent));
				logger.debug("Current time is " + util.getISODateStringFromCalendar(now));

				logger.debug("Minutes since last email to " + emailKey + " is " + String.valueOf(ageInMins));

				if (ageInMins >= distList.get(distributionID).maxFrequencyMins)
				{

					emailLog.put(emailKey, now);
					logger.debug("Email frequency permitted.");

					try
					{

						Properties propAuth = new Properties();
						Properties propNoAuth = new Properties();

						propAuth.putAll(smtpProperties);
						propNoAuth.putAll(smtpProperties);

						Session authenticatedSession = Session.getInstance(propAuth, new Authenticator()
						{
							@Override
							protected PasswordAuthentication getPasswordAuthentication()
							{
								return new PasswordAuthentication(smtpProperties.get("mail.smtp.user").toString(), smtpProperties.get("mail.smtp.password").toString());
							}
						});

						propNoAuth.put("mail.smtp.user","");
						propNoAuth.put("mail.smtp.password","");
						

						Session unauthenticatedSession = Session.getInstance(propAuth,null);

						MimeMessage message;

						if (smtpProperties.get("mail.smtp.auth").toString().toLowerCase().equals("true"))
						{
							logger.debug("Email authentication required");
							message = new MimeMessage(authenticatedSession);
						}
						else
						{
							logger.debug("Email no authentication required");
							message = new MimeMessage(unauthenticatedSession);
						}

						String emails = distList.get(distributionID).addressList;

						logger.debug("Email To: " + emails);
						message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(emails));

						message.setFrom(new InternetAddress(smtpProperties.get("mail.smtp.from").toString()));

						message.setSubject(subject);

						MimeBodyPart mimeBodyPart = new MimeBodyPart();

						// mimeBodyPart.setContent(messageText, "text/html;
						// charset=utf-8");
						mimeBodyPart.setText(messageText, "utf-8");

						Multipart multipart = new MimeMultipart();
						multipart.addBodyPart(mimeBodyPart);

						if (filename.equals("") == false)
						{
							logger.debug("Email add attachment [" + util.getFilenameFromPath(filename) + "]");

							MimeBodyPart attachmentBodyPart = new MimeBodyPart();
							attachmentBodyPart.attachFile(new File(filename));
							attachmentBodyPart.setDescription(filename);

							multipart.addBodyPart(attachmentBodyPart);

						}
						message.setContent(multipart);

						logger.debug("Sending email");
						Transport.send(message);
						logger.debug("Email sent");

						message = null;
					}
					catch (Exception ex)
					{
						logger.error("Error encountered sending email [" + ex.getMessage() + "]");
					}

				}
				else
				{
					// okToSend = false;
					logger.debug("Email suppressed - too frequent");
				}

			}
			else
			{
				logger.debug("Email Distribution list is disabled.");
			}
		}
		else
		{
			logger.debug("Disabled or empty email distribution list. No email sent.");
		}

		return result;
	}
}