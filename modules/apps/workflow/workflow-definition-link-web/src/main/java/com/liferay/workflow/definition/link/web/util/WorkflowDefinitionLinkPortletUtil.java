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

package com.liferay.workflow.definition.link.web.util;

import com.liferay.workflow.definition.link.web.search.WorkflowDefinitionLinkSearchEntry;
import com.liferay.workflow.definition.link.web.util.comparator.WorkflowDefinitionLinkSearchEntryResourceComparator;
import com.liferay.workflow.definition.link.web.util.comparator.WorkflowDefinitionLinkSearchEntryWorkflowComparator;

import java.util.Comparator;

/**
 * @author Leonardo Barros
 */
public class WorkflowDefinitionLinkPortletUtil {

	public static Comparator<WorkflowDefinitionLinkSearchEntry>
		getWorkflowDefinitionLinkOrderByComparator(
			String orderByCol, String orderByType) {

		boolean orderByAsc = false;

		if (orderByType.equals("asc")) {
			orderByAsc = true;
		}

		Comparator<WorkflowDefinitionLinkSearchEntry> orderByComparator = null;

		if (orderByCol.equals("resource")) {
			orderByComparator =
				new WorkflowDefinitionLinkSearchEntryResourceComparator(
					orderByAsc);
		}
		else if (orderByCol.equals("workflow")) {
			orderByComparator =
				new WorkflowDefinitionLinkSearchEntryWorkflowComparator(
					orderByAsc);
		}

		return orderByComparator;
	}

}