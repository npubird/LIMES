package org.aksw.limes.core.gui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;

public class TaskResultSerializer {

	public static File folder = new File(System.getProperty("user.dir") + "/cache/");
	static Logger logger = LoggerFactory.getLogger(TaskResultSerializer.class.getName());

	/**
	 * Checks if the task result was serialized by checking the hashcode of the
	 * task
	 * 
	 * @param task
	 *            GetClassesTask or GetPropertiesTask object
	 * @return result of the task or null if it was not serialized
	 */
	@SuppressWarnings("rawtypes")
	public static Object getTaskResult(Task task) {
		final String hash = task.hashCode() + "";
		final File serializationFile = new File(folder + hash + ".ser");
		logger.info("Checking for file " + serializationFile.getAbsolutePath());
		Object taskResult = null;
		try {
			if (serializationFile.exists()) {
				logger.info("Found serialization. Loading data from file " + serializationFile.getAbsolutePath());
				final FileInputStream fileIn = new FileInputStream(serializationFile);
				final ObjectInputStream in = new ObjectInputStream(fileIn);
				taskResult = in.readObject();
				in.close();
				fileIn.close();
			} else {
				return null;
			}
			if (taskResult == null) {
				throw new Exception();
			} else {
				logger.info("Serialization loaded successfully from file " + serializationFile.getAbsolutePath());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return taskResult;
	}

	@SuppressWarnings("rawtypes")
	public static void serializeTaskResult(Task task, Object result) {
		try {
			final String hash = task.hashCode() + "";
			final File serializationFile = new File(folder + hash + ".ser");
			final FileOutputStream fileOut = new FileOutputStream(serializationFile);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(result);
			out.close();
			fileOut.close();
			logger.info("Serialized data is saved in " + serializationFile.getAbsolutePath());
		} catch (final IOException i) {
			i.printStackTrace();
		}
	}
}
