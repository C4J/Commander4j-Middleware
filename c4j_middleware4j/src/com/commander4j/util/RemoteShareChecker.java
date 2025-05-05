package com.commander4j.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class RemoteShareChecker
{

	private String errorMessage = "";

	private void setErrorMessage(String message)
	{
		this.errorMessage = message;
	}

	public String getErrorMessage()
	{
		return this.errorMessage;
	}

	public boolean isValidPath(String path)
	{
		boolean result = true;

		Path remotePath = Paths.get(path);

		try
		{
			Files.readAttributes(remotePath, BasicFileAttributes.class);
			setErrorMessage("File is accessible: " + remotePath);

			if (Files.isRegularFile(remotePath))
			{
				if (canDelete(remotePath))
				{
					setErrorMessage("The file can be deleted.");
				}
				else
				{
					setErrorMessage("The file cannot be deleted due to permission or other restrictions.");
					result = false;
				}
			}

			if (Files.isRegularFile(remotePath))
			{
				if (isFileLocked(remotePath))
				{
					setErrorMessage("The file is locked by another process.");
					result = false;
				}
				else
				{
					setErrorMessage("The file is not locked.");
				}
			}
		}
		catch (NoSuchFileException e)
		{
			setErrorMessage("The file or directory does not exist: " + e.getFile());
			result = false;
		}
		catch (AccessDeniedException e)
		{
			setErrorMessage("Access denied to the path: " + e.getFile());
			result = false;
		}
		catch (FileSystemException e)
		{
			setErrorMessage("File system error: " + e.getMessage());
			result = false;
		}
		catch (IOException e)
		{
			setErrorMessage("An I/O error occurred: " + e.getMessage());
			result = false;
		}

		return result;
	}

	private boolean canDelete(Path path)
	{
		try
		{
			Path parentDir = path.getParent();

			// Check if the parent directory is writable
			if (!Files.isWritable(parentDir))
			{
				setErrorMessage("Parent directory is not writable: " + parentDir);
				return false;
			}

			// Platform-specific writable check
			if (!System.getProperty("os.name").toLowerCase().contains("win"))
			{
				PosixFileAttributes posixAttributes = Files.readAttributes(path, PosixFileAttributes.class);
				Set<PosixFilePermission> permissions = posixAttributes.permissions();
				if (!permissions.contains(PosixFilePermission.OWNER_WRITE))
				{
					setErrorMessage("File is not writable according to POSIX permissions: " + path);
					return false;
				}
			}
			else
			{
				DosFileAttributes dosAttributes = Files.readAttributes(path, DosFileAttributes.class);
				if (dosAttributes.isReadOnly())
				{
					setErrorMessage("File is read-only: " + path);
					return false;
				}
			}

			return true;
		}
		catch (IOException e)
		{
			setErrorMessage("Error checking delete permissions: " + e.getMessage());
			return false;
		}
	}

	private boolean isFileLocked(Path path)
	{
		// Attempt to open the file with exclusive access
		try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE))
		{
			// File is not locked if we successfully open it
			return false;
		}
		catch (IOException e)
		{
			setErrorMessage("Error: Unable to open the file for write access. It may be locked.");
			return true;
		}
	}
}
