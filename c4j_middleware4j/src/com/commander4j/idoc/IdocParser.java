package com.commander4j.idoc;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class IdocParser
{
	private String _configFile;
	private String _fileToProcess;
	private ArrayList<IdocConfigData> _configValues;
	private IdocOutputData _outputData;

	public IdocParser(String configFile, String fileToProcess)
	{
		_configFile = configFile;
		_fileToProcess = fileToProcess;
		_outputData = new IdocOutputData();
	}

	public String getFileToProcess()
	{
		return _fileToProcess;
	}

	public String getconfigFile()
	{
		return _configFile;
	}

	public void ReadConfigFile() throws Exception, FileNotFoundException
	{

		_configValues = new ArrayList<IdocConfigData>();
		boolean bFoundStartLine = false;
		File cfgFile = new File(_configFile);
		if (cfgFile.exists())
		{
			Scanner in = new Scanner(cfgFile);
			try
			{
				while (in.hasNextLine())
				{
					String line = in.nextLine();
					line = line.replaceAll("\t", "");
					line = line.trim();

					// ignore comments and blanks
					if (line.equals(""))
						continue;

					if (line.startsWith("'"))
						continue;

					if (line.toLowerCase().startsWith("#start"))
					{

						if (!bFoundStartLine)
						{
							String msgType = line.substring(6).trim().toLowerCase();
							String[] msgTypeValues = msgType.split(" ");
							if (msgTypeValues.length > 0 && !msgTypeValues[0].equals(""))
							{
								bFoundStartLine = true;
								continue;
							} else
							{
								throw new Exception("Configuration file does not have a correct #start line");
							}
						} else
						{
							throw new Exception("Configuration file has multiple #start lines");
						}

					}

					String[] configValues = line.split(",");
					IdocConfigData cfgData = new IdocConfigData();
					for (int i = 0; i < configValues.length; i++)
					{
						switch (i)
						{
						case 0:
						{
							cfgData.SegmentDefinition = configValues[i].trim();
							break;
						}
						case 1:
						{
							cfgData.IDOCField = configValues[i].trim();
							break;
						}
						case 2:
						{
							cfgData.nFieldOffset = Integer.parseInt(configValues[i].trim());
							break;
						}
						case 3:
						{
							cfgData.nLength = Integer.parseInt(configValues[i].trim());
							break;
						}
						case 4:
						{
							cfgData.BIFSection = configValues[i].trim();
							break;
						}
						case 5:
						{
							cfgData.bNewSection = (configValues[i].trim().equals("1"));
							break;
						}
						case 6:
						{
							cfgData.BIFField = configValues[i].trim();
							break;
						}
						default:
						{
							break;
						}
						}
					}

					cfgData.position = _configValues.size();
					_configValues.add(cfgData);
				}

			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} finally
			{
				in.close();
				in = null;
			}

			if (!bFoundStartLine)
				throw new Exception("Configuration file does not contain a #start line");

			if (_configValues.size() == 0)
				throw new Exception("Configuration file does not contain any configuration");

		} else
		{
			throw new Exception("Configuration file not found");
		}

	}

	public void GetData() throws Exception
	{
		File dataFile = new File(_fileToProcess);
		if (dataFile.exists())
		{
			Scanner srcScanner = new Scanner(dataFile, "UTF-8");

			String source = "";
			while (srcScanner.hasNextLine())
			{
				source = source.concat(srcScanner.nextLine());
			}

			int nLocationIndex = 0;
			int nLowestIndex = 0;
			int nStringStartPosition = 0;
			String CurrentSegmentDescription = "";

			HashMap<String, String> subKeys = new HashMap<String, String>();

			String section = "";

			boolean found = false;
			ArrayList<String> distinctSegments = new ArrayList<String>();
			for (IdocConfigData cfg : _configValues)
			{
				if (!cfg.SegmentDefinition.equals(null) && !cfg.SegmentDefinition.equals(""))
				{
					found = false;
					for (String s : distinctSegments)
					{
						if (s.equals(cfg.SegmentDefinition))
						{
							found = true;
						}

					}
					if (!found)
						distinctSegments.add(cfg.SegmentDefinition);
				}
			}

			String value = "";
			String field = "";
			String keyField = "";
			String keyValue = "";
			HashMap<String, String> subSectionKeys = new HashMap<String, String>();
			boolean hasSubKey = false;
			boolean hasMultipleSubKeys = false;

			while (true)
			{

				nLocationIndex = Integer.MAX_VALUE;

				for (String segname : distinctSegments)
				{
					if (!segname.equals(""))
					{
						int nNewIndex = source.indexOf(segname, nStringStartPosition);

						if (nLocationIndex > nNewIndex && nNewIndex >= 0)
						{
							nLocationIndex = nNewIndex;
							nLowestIndex = getLowestConfigIndex(segname);
							CurrentSegmentDescription = segname;

						}
					}
				}

				// no more keywords found

				if (nLocationIndex == Integer.MAX_VALUE)
				{
					break;
				}

				for (int i = nLowestIndex; i < _configValues.size(); i++)
				{
					hasSubKey = false;
					IdocConfigData cfg = _configValues.get(i);
					if (cfg.SegmentDefinition.equals(CurrentSegmentDescription))
					{

						if (cfg.bNewSection)
						{
							section = cfg.BIFSection;
							hasMultipleSubKeys = false;
							subSectionKeys.clear();
							keyField = "";
							keyValue = "";
						}

						int startPos = nLocationIndex + cfg.nFieldOffset;
						int endPos = startPos + cfg.nLength;
						value = source.substring(startPos, endPos).trim();

						if (!cfg.BIFField.equals(""))
							field = cfg.BIFField;

						if (field.indexOf("[KEY]") >= 0)
						{
							field = field.replaceAll("\\[KEY\\]", "");

						} else if (field.indexOf("[SUBKEY]") >= 0)
						{
							field = field.replaceAll("\\[SUBKEY\\]", "");
							_outputData.AddDataSegmentWithKey(section, field, value);
							keyField = field;
							keyValue = value;
							hasSubKey = true;
							if (subKeys.containsKey(field))
								subKeys.remove(field);

							subKeys.put(field, value);
						} else if (field.indexOf("*") >= 0)
						{
							String subKeyMatch = field.substring(field.indexOf("*") + 1);
							field = field.substring(0, field.indexOf("*"));

							// add a subkey value
							if (subKeys.containsKey(subKeyMatch))
							{
								hasMultipleSubKeys = true;
								String subKeyValue = subKeys.get(subKeyMatch);

								subSectionKeys.put(field, value);
								subSectionKeys.put(subKeyMatch, subKeyValue);

								_outputData.AddDataSegmentWithKeys(section, subSectionKeys);

							}
						}

						if (!hasSubKey && !cfg.BIFField.toLowerCase().equals("messageid"))
						{
							if (hasMultipleSubKeys && !cfg.bNewSection)
							{
								_outputData.AddValueToSegment(section, field, value, subSectionKeys);
							} else if (!hasMultipleSubKeys)
							{
								_outputData.AddDataSegment(section);
								if (!keyField.equals(""))
									_outputData.AddValueToSegment(section, field, value, keyField, keyValue);
								else
									_outputData.AddValueToSegment(section, field, value);
							}
						}

						if (cfg.BIFField.toLowerCase().equals("messageid"))
						{
							_outputData.setMsgId(value);
						}
					}

				}

				nStringStartPosition = nLocationIndex + CurrentSegmentDescription.length();
			}
			srcScanner.close();
		} else
		{
			throw new Exception("File not found");
		}
		dataFile = null;
	}

	private int getLowestConfigIndex(String segment)
	{

		for (IdocConfigData cfg : _configValues)
		{
			if (cfg.SegmentDefinition.equals(segment))
				return _configValues.indexOf(cfg);
		}
		return -1;
	}

	public ArrayList<IdocConfigData> GetConfigData()
	{
		return _configValues;
	}

	public IdocOutputData GetOutputData()
	{
		return _outputData;
	}

}
