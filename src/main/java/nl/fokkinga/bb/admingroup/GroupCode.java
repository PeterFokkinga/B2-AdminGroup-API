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

import blackboard.data.*;
import blackboard.data.course.Course;
import blackboard.data.course.Group;
import blackboard.persist.DataType;
import blackboard.persist.Id;
import blackboard.persist.impl.mapping.DbObjectMap;
import blackboard.persist.impl.mapping.annotation.*;
import nl.fokkinga.bb.Util;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import static nl.fokkinga.bb.Util.isEmpty;


/**
 * Provides low-level access to the batch_uid of a group.
 * <p>
 * Please note that the batch_uid for groups is more flexible than for other
 * objects like courses and users. With groups you can have</p>
 * <ul>
 * <li>multiple batch_uid values for the same group</li>
 * <li>multiple groups all having the same batch_uid value</li>
 * </ul>
 *
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
@Table("bb_groupcode")
public class GroupCode extends AbstractIdentifiable {
	public static final DataType DATA_TYPE = new DataType(GroupCode.class);
	public static final String SOURCEDID_SEPARATOR = "#";
	public static final String BBLEARN_SOURCEDID_SOURCE = "bblearn";
	static final DbObjectMap MAP = AnnotationMappingFactory.getMap(GroupCode.class);

	@Column(value = "batch_uid", def = "BatchUID")
	private String batchUid;

	@Column(value = "group_pk1", def = "GroupId")
	@RefersTo(Group.class)
	private Id groupId;

	@Column(value = "crsmain_pk1", def = "CourseId")
	@RefersTo(Course.class)
	private Id courseId;


	public GroupCode() {}

	public GroupCode(Group grp, String batchUid) {
		groupId = grp.getId();
		courseId = grp.getCourseId();
		this.batchUid = batchUid;
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
		if (isEmpty(id) || isEmpty(src)) {
			throw new IllegalArgumentException("setSourcedId: both 'source' and 'id' parameters must have a non-empty value");
		} else if (src.contains(SOURCEDID_SEPARATOR)) {
			throw new IllegalArgumentException("setSourcedId: the parameter 'source' should not contain the character '"
					+ SOURCEDID_SEPARATOR + "'");
		}
		batchUid = src + SOURCEDID_SEPARATOR + id;
	}


	/**
	 * Extract the "source" part of the batch_uid that holds a composite key.
	 *
	 * @return tthe "source" part of the composite key, an empty string when the
	 * batch_uid appears to be "just" a batch_uid (not a composite key) or
	 * {@link #BBLEARN_SOURCEDID_SOURCE} when the batch_uid is empty
	 */
	public String getSourcedIdSource() {
		if (isEmpty(batchUid)) {
			return Id.isValidPkId(groupId) ? BBLEARN_SOURCEDID_SOURCE : "";
		}
		int idx = batchUid.indexOf(SOURCEDID_SEPARATOR);
		return idx > 0 ? batchUid.substring(0, idx) : "";
	}


	/**
	 * Extract the "id" part of the batch_uid that holds a composite key.
	 *
	 * @return the "id" part of the composite key, the entire batch_uid when the
	 * batch_uid appears to be "just" a batch_uid (not a composite key), the
	 * group id as a number when the batch_uid is empty and there is a valid
	 * group id or an empty string when both the batch_uid is empty and the
	 * group id is not valid
	 */
	public String getSourcedIdId() {
		if (isEmpty(batchUid)) {
			return Id.isValidPkId(groupId) ? String.valueOf(Util.toNumber(groupId)) : "";
		}
		int idx = batchUid.indexOf(SOURCEDID_SEPARATOR);
		return idx > 0 ? batchUid.substring(idx + 1) : batchUid;
	}


	public void setBatchUid(String value) { batchUid = value; }
	public String getBatchUid() { return Util.makeSafe(batchUid); }

	public void setGroupId(Id value) { groupId = value; }
	public Id getGroupId() { return groupId; }

	public void setCourseId(Id value) { courseId = value; }
	public Id getCourseId() { return courseId; }


	/**
	 * Check whether this group code can be persisted. Requirements of a
	 * valid group code object are:
	 * <ul>
	 *   <li>course ID and group ID should be valid, meaning not-NULL and
	 *   coming from a persisted course / group object</li>
	 *   <li>the batch_uid should not be empty, meaning that it should
	 *   have at least one non-whitespace character</li>
	 *   <li>the batch_uid should be no longer than 100 characters</li>
	 *   <li>the batch_uid should only consist of characters from the ISO-8859
	 *   character set</li>
	 * </ul>
	 *
	 * @throws ValidationException when one or more of the described requirements
	 * are not met
	 * @see ValidationException#getWarnings for a description of the failed
	 * requirements
	 */
	public void validate() throws ValidationException {
		ValidationException ve = new ValidationException();

		if (!Id.isValidPkId(courseId)) {
			ve.addWarning(new ValidationWarning("Required field not set", "CourseId value must be set."));
		}
		if (!Id.isValidPkId(groupId)) {
			ve.addWarning(new ValidationWarning("Required field not set", "GroupId value must be set."));
		}
		if (isEmpty(batchUid)) {
			ve.addWarning(new ValidationWarning("Required field not set", "Batch_UID value must be set."));
		} else {
			if (batchUid.length() > 100) {
				ve.addWarning(new ValidationWarning("Batch_uid '" + batchUid + "' is longer than 100 characters"));
			}
			CharsetEncoder enc = StandardCharsets.ISO_8859_1.newEncoder();
			if (!enc.canEncode(batchUid)) {
				ve.addWarning(new ValidationWarning("Batch_uid '" + batchUid + "' contains non-ISO_8859 characters."));
			}
		}
		if (ve.getWarnings().size() > 0) {
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
		if (Id.isValidPkId(groupId)) {
			sb.append(" group_pk1=").append(Util.toNumber(groupId));
		}
		sb.append(" batch_uid='").append(batchUid);
		return sb.append("'}").toString();
	}
}
