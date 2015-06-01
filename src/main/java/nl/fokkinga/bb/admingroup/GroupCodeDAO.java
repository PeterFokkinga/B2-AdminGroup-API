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
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.dao.impl.SimpleDAO;
import blackboard.persist.impl.SimpleSelectQuery;
import blackboard.platform.query.Criteria;
import blackboard.platform.query.CriterionBuilder;

import java.util.List;

import static nl.fokkinga.bb.Util.notEmpty;


/**
 * @author <a href="peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
public class GroupCodeDAO extends SimpleDAO<GroupCode> {

	private GroupCodeDAO() {
		super(GroupCode.class, "Group");
	}

	public static GroupCodeDAO get() { return new GroupCodeDAO(); }


	public void deleteByGroupId(Id grpId) throws KeyNotFoundException {
		GroupCode groupCode = loadByGroupId(grpId);
		getDAOSupport().delete(groupCode);
	}


	public List<GroupCode> loadBySourcedId(String source, String id) {
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		if (notEmpty(id)) {
			query.addWhere("BatchUID", source + GroupCode.SOURCEDID_SEPARATOR + id);
		} else {
			query.addLikeWhere("BatchUID", source + "%");
		}
		return getDAOSupport().loadList(query);
	}


	public GroupCode loadByGroupId(Id grpId) throws KeyNotFoundException {
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		query.addWhere("GroupId", grpId);
		return getDAOSupport().load(query);
	}


	public List<GroupCode> loadByBatchUid(String uid) {
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		query.addWhere("BatchUID", uid);
		return getDAOSupport().loadList(query);
	}


	boolean existsOther(String batchUid, Id grpId) {
		if ("".equals(batchUid)) {
			return false;
		}
		SimpleSelectQuery query = new SimpleSelectQuery(GroupCode.MAP);
		Criteria criteria = query.getCriteria();
		CriterionBuilder builder = criteria.createBuilder();
		criteria.add(builder.equal("BatchUID", batchUid));
		if (Id.isValidPkId(grpId)) {
			criteria.add(builder.notEqual("GroupId", grpId));
		}
		return getDAOSupport().loadList(query).size() > 0;
	}
}
