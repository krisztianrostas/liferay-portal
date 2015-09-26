/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.verify.extender.internal;

import com.liferay.osgi.service.tracker.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.RunnableUtil;
import com.liferay.portal.output.stream.container.OutputStreamContainer;
import com.liferay.portal.output.stream.container.OutputStreamContainerFactory;
import com.liferay.portal.output.stream.container.OutputStreamContainerFactoryTracker;
import com.liferay.portal.verify.VerifyException;
import com.liferay.portal.verify.VerifyProcess;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Miguel Pastor
 * @author Raymond Augé
 * @author Carlos Sierra Andrés
 */
@Component(
	immediate = true,
	property = {
		"osgi.command.function=execute", "osgi.command.function=executeAll",
		"osgi.command.scope=verify"
	},
	service = {VerifyProcessTracker.class}
)
public class VerifyProcessTracker {

	public void execute(final String verifyProcessName) {
		executeVerifyProcess(
			verifyProcessName, null, "verify-" + verifyProcessName);
	}

	public void execute(
		String verifyProcessName, String outputStreamContainerFactoryName) {

		executeVerifyProcess(
			verifyProcessName, outputStreamContainerFactoryName,
			"verify-" + verifyProcessName);
	}

	public void executeAll() {
		Set<String> verifyProcessNames = _verifyProcesses.keySet();

		for (String verifyProcessName : verifyProcessNames) {
			executeVerifyProcess(
				verifyProcessName, null, "verify-" + verifyProcessName);
		}
	}

	public void executeAll(String outputStreamContainerFactoryName) {
		Set<String> verifyProcessNames = _verifyProcesses.keySet();

		for (String verifyProcessName : verifyProcessNames) {
			executeVerifyProcess(
				verifyProcessName, outputStreamContainerFactoryName,
				"verify-" + verifyProcessName);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		try {
			_verifyProcesses = ServiceTrackerMapFactory.singleValueMap(
				bundleContext, VerifyProcess.class, "verify.process.name");

			_verifyProcesses.open();
		}
		catch (InvalidSyntaxException ise) {
			throw new IllegalStateException(ise);
		}
	}

	protected void close(OutputStream outputStream) {
		try {
			outputStream.close();
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Deactivate
	protected void deactivate() {
		_verifyProcesses.close();
	}

	protected void executeVerifyProcess(
		String verifyProcessName, OutputStream outputStream) {

		PrintWriter printWriter = new PrintWriter(outputStream, true);

		printWriter.println("Executing " + verifyProcessName);

		VerifyProcess verifyProcess = getVerifyProcess(verifyProcessName);

		try {
			verifyProcess.verify();
		}
		catch (VerifyException ve) {
			_log.error(ve, ve);
		}
	}

	protected void executeVerifyProcess(
		final String verifyProcessName, String outputStreamContainerFactoryName,
		String outputStreamName) {

		OutputStreamContainerFactory outputStreamContainerFactory = null;

		if (outputStreamContainerFactoryName != null) {
			_outputStreamContainerFactoryTracker.
				getOutputStreamContainerFactory(
					outputStreamContainerFactoryName);
		}
		else {
			outputStreamContainerFactory =
				_outputStreamContainerFactoryTracker.
					getOutputStreamContainerFactory();
		}

		OutputStreamContainer outputStreamContainer =
			outputStreamContainerFactory.create(outputStreamName);

		final OutputStream outputStream =
			outputStreamContainer.getOutputStream();

		RunnableUtil.runWithSwappedSystemOut(
			new Runnable() {

				@Override
				public void run() {
					executeVerifyProcess(verifyProcessName, outputStream);
				}

			}, outputStream);

		close(outputStream);
	}

	protected VerifyProcess getVerifyProcess(String verifyProcessName) {
		VerifyProcess verifyProcess = _verifyProcesses.getService(
			verifyProcessName);

		if (verifyProcess == null) {
			throw new IllegalArgumentException(
				"No verify process with name " + verifyProcessName);
		}

		return verifyProcess;
	}

	@Reference
	protected void setOutputStreamTracker(
		OutputStreamContainerFactoryTracker
			outputStreamContainerFactoryTracker) {

		_outputStreamContainerFactoryTracker =
			outputStreamContainerFactoryTracker;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		VerifyProcessTracker.class);

	private OutputStreamContainerFactoryTracker
		_outputStreamContainerFactoryTracker;
	private ServiceTrackerMap<String, VerifyProcess> _verifyProcesses;

}