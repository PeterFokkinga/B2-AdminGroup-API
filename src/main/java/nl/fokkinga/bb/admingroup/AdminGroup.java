/*
 * Copyright 2015 Peter R. Fokkinga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.data.ValidationWarning;
import blackboard.data.course.Group;
import blackboard.persist.Id;
import blackboard.persist.impl.mapping.*;
import blackboard.persist.impl.mapping.annotation.AnnotationMappingFactory;
import nl.fokkinga.bb.Util;

import java.util.List;

import static nl.fokkinga.bb.Util.isEmpty;
import static nl.fokkinga.bb.Util.notEmpty;


/**
 *
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
public class AdminGroup extends Group {
	public static final DbObjectMap MAP = AnnotationMappingFactory.getMap(AdminGroup.class);

	static {
		/*
		 * using this MAP allows comparing the ID of a Group with the ID of an
		 * AdminGroup; without it equals would always return false even when
		 * both IDs refer to the same record in the database
		 */
		MAP.removeMapping("id");
		IdMapping m = new IdMapping("id", Group.DATA_TYPE, "pk1", Mapping.Use.OUTPUT, Mapping.Use.NONE, true);
		m.setPhysicalName("_id");
		MAP.addMapping(m);
	}

	private GroupCode groupCode = new GroupCode();
	private boolean codesLoaded = false;
	private boolean batchUidChanged = false;

	public AdminGroup() {
		super();

		// prevent Group.validate from throwing a NPE
		setCourseId(Id.UNSET_ID);
	}


	@Override public void setCourseId(Id crsId) {
		// prevent Group.validate from throwing a NPE on a new object
		super.setCourseId(crsId != null ? crsId : Id.UNSET_ID);
	}


	synchronized GroupCode getGroupCode() {
		if (!codesLoaded && Id.isValidPkId(getId())) {
			List<GroupCode> codes = GroupCodeDAO.get().loadByGroupId(getId());
			if (codes.size() > 0) {
				groupCode = codes.get(0);
			}
			codesLoaded = true;
		}
		groupCode.setGroupId(getId());
		groupCode.setCourseId(getCourseId());
		return groupCode;
	}


	/**
	 * Use the batch_uid to store a composite key. Composite keys are a
	 * cornerstone of the IMS Enterprise data model and consist of a "source"
	 * (identifier of the system where the ID originates) and an "id" (the
	 * unique identifier of the object within that system).
	 *
	 * @param src the source part of the composite key; should not be NULL,
	 *            empty or contain the separator character
	 * @param id  the id part of the composite key; should not be NULL or empty
	 * @throws IllegalArgumentException when one (or both) of the parameters
	 *         are not valid
	 */
	public void setSourcedId(String src, String id) {
		groupCode.setSourcedId(src, id);
	}

	public void setBatchUid(String batchUid) {
		String safeUid = Util.makeSafe(batchUid);
		batchUidChanged = batchUidChanged || !getGroupCode().getBatchUid().equals(safeUid);
		getGroupCode().setBatchUid(safeUid);
	}


	public String getBatchUid() { return getGroupCode().getBatchUid(); }


	/**
	 * Check whether this group be persisted. Requirements of a valid group
	 * object are:
	 * <ul>
	 *   <li>course ID should be valid, meaning not-NULL and coming from a
	 *   persisted course</li>
	 *   <li>the title should not be empty, meaning that it should
	 *   have at least one non-whitespace character</li>
	 *   <li>the group code should be valid</li>
	 * </ul>
	 *
	 * @throws ValidationException when one or more of the described requirements
	 * are not met
	 * @see ValidationException#getWarnings for a description of the failed
	 * requirements
	 * @see GroupCode#validate() for requirements on the group code
	 */
	@Override public void validate() throws ValidationException {
		ValidationException ve = new ValidationException();
		try {
			super.validate();
		} catch (ValidationException e) {
			ve.getWarnings().addAll(e.getWarnings());
		}
		try {
			getGroupCode().validate();
		} catch (ValidationException e) {
			ve.getWarnings().addAll(e.getWarnings());
		}
		if (isEmpty(getTitle())) {
			ve.addWarning(new ValidationWarning("Required field not set", "Title must not be empty."));
		}
		if (!ve.getWarnings().isEmpty()) {
			throw ve;
		}
	}


	/**
	 * The {@code toString} method has been overriden for the sole purpose of
	 * facilitating debugging. The returned format and/or contents are not stable
	 * and cannot be relied upon.
	 *
	 * @return a summary of the internal state of the object
	 */
	@Override public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append("{pk1=").append(Id.isValidPkId(getId()) ? Util.toNumber(getId()) : "new");
		if (notEmpty(getBatchUid())) {
			sb.append(" batch_uid='").append(getBatchUid()).append("'");
		}
		if (isGroupSet()) {
			sb.append(" groupset");
		} else if (Id.isValidPkId(getSetId())) {
			sb.append(" set_pk1=").append(Util.toNumber(getSetId()));
		}
		if (Id.isValidPkId(getCourseId())) {
			sb.append(" crs_pk1=").append(Util.toNumber(getCourseId()));
		}
		sb.append(" title='").append(getTitle());
		return sb.append("'}").toString();
	}
}
