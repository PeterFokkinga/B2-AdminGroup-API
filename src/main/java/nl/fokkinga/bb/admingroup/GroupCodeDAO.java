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

import blackboard.persist.Id;
import blackboard.persist.PersistenceRuntimeException;
import blackboard.persist.course.impl.GroupDbMap;
import blackboard.persist.dao.impl.SimpleDAO;
import blackboard.persist.impl.SimpleJoinQuery;
import blackboard.persist.impl.SimpleSelectQuery;
import blackboard.platform.query.Criteria;
import blackboard.platform.query.CriterionBuilder;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.List;

import static blackboard.persist.impl.SimpleJoinQuery.JoinType.Inner;
import static nl.fokkinga.bb.Util.notEmpty;


/**
 * Provides low-level access to the batch_uid of a group. The database objects
 * are defined in Blackboard's group management building block.
 * <p>
 * Please note that the batch_uid for groups is more flexible than for other
 * objects like courses and users. With groups you can have</p>
 * <ul>
 * <li>multiple batch_uid values for the same group</li>
 * <li>multiple groups all having the same batch_uid value</li>
 * </ul>
 * <p>
 * Add the following permission to the bb-manifest.xml of your building block
 * when you want to persist or delete {@code GroupCode} objects:<br>
 * {@code <permission type="persist" name="group" actions="create,modify,delete"/>}
 *
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 * @see GroupCode
 */
public class GroupCodeDAO extends SimpleDAO<GroupCode> {

	private static final Supplier<GroupCodeDAO> DAO_SUPPLIER = Suppliers.memoize(
			new Supplier<GroupCodeDAO>() {
				public GroupCodeDAO get() {
					return new GroupCodeDAO();
				}
			});


	private GroupCodeDAO() {
		super(GroupCode.class, "Group");
	}


	/**
	 * Gets the data access object for {@link GroupCode}
	 *
	 * @return the data access object for {@link GroupCode} objects.
	 */
	public static GroupCodeDAO get() { return DAO_SUPPLIER.get(); }


	/**
	 * Will either insert or update the given group code. The group code object
	 * is not validated, so any deficiencies in its data will cause a database
	 * exception.
	 *
	 * @param groupCode the object to persist
	 * @throws PersistenceRuntimeException when the object could not be persisted
	 * @see GroupCode#validate()
	 */
	@Override
	public void persist(GroupCode groupCode) throws PersistenceRuntimeException {
		super.persist(groupCode);
	}


	/**
	 * Will delete all (if any) group codes related to the given group.
	 *
	 * @param grpId the ID of the group to clear all group codes for
	 * @throws IllegalArgumentException when the {@code grpId} parameter is NULL
	 */
	public void deleteByGroupId(Id grpId) {
		if (grpId == null) {
			throw new IllegalArgumentException("deleteByGroupId: parameter 'grpId' should not be NULL");
		}
		List<GroupCode> codes = loadByGroupId(grpId);
		for (GroupCode groupCode : codes) {
			getDAOSupport().delete(groupCode);
		}
	}


	/**
	 * Load group code objects based on a (partial) composite key. The method
	 * will search for all group codes with the given source when the
	 * {@code id} parameter is NULL. If the {@code src} parameter is an empty
	 * string then this method will behave as {@link #loadAll()} ({@code loadAll}
	 * is faster though).
	 *
	 * @param src the source part of the composite key; should not be NULL
	 * @param id  the id part of the composite key; may be NULL (see the method
	 *            description on the behaviour when ID is NULL)
	 * @return the group codes that match the (partially) given composite key
	 * @throws IllegalArgumentException when the {@code src} parameter is NULL
	 * @see GroupCode#setSourcedId(String, String)
	 */
	public List<GroupCode> loadBySourcedId(String src, String id) {
		if (src == null) {
			throw new IllegalArgumentException("loadBySourcedId: parameter 'src' should not be NULL");
		}
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		if (notEmpty(id)) {
			query.addWhere("BatchUID", src + GroupCode.SOURCEDID_SEPARATOR + id);
		} else {
			query.addLikeWhere("BatchUID", src + "%");
		}
		return getDAOSupport().loadList(query);
	}


	/**
	 * Get all the group codes for the given group. While it probably would be
	 * wise to have only one batch_uid for a group it is actually possible to
	 * have multiple batch_uid values for a single group.
	 *
	 * @param grpId the ID of the group to find the group codes for
	 * @return the group codes defined for the given group; the list will be
	 * empty when there is no batch_uid defined for the group
	 * @throws IllegalArgumentException when the {@code grpId} parameter is NULL
	 */
	public List<GroupCode> loadByGroupId(Id grpId) {
		if (grpId == null) {
			throw new IllegalArgumentException("loadByGroupId: parameter 'grpId' should not be NULL");
		}
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		query.addWhere("GroupId", grpId);
		return getDAOSupport().loadList(query);
	}


	/**
	 * Get all the group codes for all the groups that are part of the given
	 * group set. The group code of the group set will <em>not</em> be
	 * included!
	 *
	 * @param grpSetId the ID of the group set to find the "child" group codes for
	 * @return the group codes defined for the groups that are part of the
	 * group set
	 * @throws IllegalArgumentException when the {@code grpId} parameter is NULL
	 */
	public List<GroupCode> loadByGroupSetId(Id grpSetId) {
		if (grpSetId == null) {
			throw new IllegalArgumentException("loadByGroupSetId: parameter 'grpSetId' should not be NULL");
		}
		SimpleJoinQuery query = new SimpleJoinQuery(GroupCode.MAP, "gc");
		query.setSingleObject(true);
		query.addJoin(Inner, GroupDbMap.MAP, "g", "id", "GroupId", false);
		Criteria criteria = query.getCriteria();
		CriterionBuilder cuBuilder = criteria.createBuilder("g");
		criteria.add(cuBuilder.equal("setId", grpSetId));
		return getDAOSupport().loadList(query);
	}


	/**
	 * Get all the group codes for the given batch_uid. While it probably would be
	 * wise to treat the batch_uid as a unique identifier (like Blackboard does
	 * with User and Course objects), it is actually possible to have multiple
	 * groups having the same batch_uid.
	 *
	 * @param uid the batch_uid of the group to find the group codes for
	 * @return ; the list will be empty if there is no batch_uid defined for the group
	 * @throws IllegalArgumentException when the {@code uid} parameter is NULL
	 */
	public List<GroupCode> loadByBatchUid(String uid) {
		if (uid == null) {
			throw new IllegalArgumentException("loadByBatchUid: parameter 'uid' should not be NULL");
		}
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		query.addWhere("BatchUID", uid);
		return getDAOSupport().loadList(query);
	}


	/**
	 * Check whether the batch_uid is used by at most one group. An additional
	 * constraint can be that the group using the batch_uid must be the given
	 * group ID.
	 *
	 * @param uid   the batch_uid to check; should NOT be NULL
	 * @param grpId additional constraint on the group using the batch_uid,
	 *              may be NULL
	 * @return true when the batch_uid is not in use, or only in use by the
	 * given group ID, or (when group ID is NULL) only in use by a
	 * single group
	 * @throws IllegalArgumentException when the {@code uid} parameter is NULL
	 */
	public boolean isUnique(String uid, Id grpId) {
		if (uid == null) {
			throw new IllegalArgumentException("isUnique: parameter 'uid' should not be NULL");
		}
		if ("".equals(uid)) {
			return false;
		}
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		Criteria criteria = query.getCriteria();
		CriterionBuilder builder = criteria.createBuilder();
		criteria.add(builder.equal("BatchUID", uid));
		boolean withConstraint = Id.isValidPkId(grpId);
		if (withConstraint) {
			criteria.add(builder.notEqual("GroupId", grpId));
		}
		int numMatches = getDAOSupport().loadList(query).size();
		return withConstraint ? (numMatches == 0) : (numMatches <= 1);
	}


	/**
	 * Check whether the batch_uid is used by at most one group in the course.
	 * An additional constraint can be that the group using the batch_uid must
	 * be the given group ID.
	 *
	 * @param crsId ; should be a valid ID
	 * @param uid   the batch_uid to check; should NOT be NULL
	 * @param grpId additional constraint on the group using the batch_uid,
	 *              may be NULL
	 * @return true when the batch_uid is not in use in the course, or only in
	 * use by the given group ID, or (when group ID is NULL) only in use by a
	 * single group
	 * @throws IllegalArgumentException when the {@code crsId} parameter does
	 *                                  not have a valid ID
	 * @throws IllegalArgumentException when the {@code uid} parameter is NULL
	 */
	public boolean isUniqueInCourse(Id crsId, String uid, Id grpId) {
		if (uid == null) {
			throw new IllegalArgumentException("isUniqueInCourse: parameter 'uid' should not be NULL");
		} else if (!Id.isValidPkId(crsId)) {
			throw new IllegalArgumentException("isUniqueInCourse: parameter 'crsId' should be a valid ID");
		}
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		Criteria criteria = query.getCriteria();
		CriterionBuilder builder = criteria.createBuilder();
		criteria.add(builder.equal("BatchUID", uid));
		criteria.add(builder.equal("CourseId", crsId));
		boolean withConstraint = Id.isValidPkId(grpId);
		if (withConstraint) {
			criteria.add(builder.notEqual("GroupId", grpId));
		}
		int numMatches = getDAOSupport().loadList(query).size();
		return withConstraint ? (numMatches == 0) : (numMatches <= 1);
	}
}
