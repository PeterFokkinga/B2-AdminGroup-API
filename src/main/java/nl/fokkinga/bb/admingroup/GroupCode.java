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
 * @author <a href="peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
@Table("bb_groupcode")
public class GroupCode extends AbstractIdentifiable {
	public static final DataType DATA_TYPE = new DataType(GroupCode.class);
	public static final DbObjectMap MAP = AnnotationMappingFactory.getMap(GroupCode.class);
	public static final String SOURCEDID_SEPARATOR = "#";
	public static final String BBLEARN_SOURCEDID_SOURCE = "bblearn";

	@Column(value = "batch_uid", def = "BatchUID")
	private String batchUid;

	@Column(value = "group_pk1", def = "GroupId")
	@RefersTo(Group.class)
	private Id groupId;

	@Column(value = "crsmain_pk1", def = "CourseId")
	@RefersTo(Course.class)
	private Id courseId;


	public void setSourcedId(String src, String id) {
		if (isEmpty(id) || isEmpty(src)) {
			throw new IllegalArgumentException("both Source and ID must have a non-empty value");
		}
		batchUid = src + SOURCEDID_SEPARATOR + id;
	}

	public String getSourcedIdSource() {
		if (isEmpty(batchUid)) {
			return Id.isValidPkId(groupId) ? BBLEARN_SOURCEDID_SOURCE : "";
		}
		int idx = batchUid.indexOf(SOURCEDID_SEPARATOR);
		return idx > 0 ? batchUid.substring(0, idx) : "";
	}


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


	public void validate() throws ValidationException {
		ValidationException ve = new ValidationException();

		if (!Id.isValidPkId(courseId)) {
			ve.addWarning(new ValidationWarning("Required field not set", "CourseId value must be set."));
		}
		if (!Id.isValid(groupId)) {
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
