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
import blackboard.persist.dao.impl.SimpleDAO;
import blackboard.persist.impl.JdbcQueryHelper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;


/**
 * @author <a href="peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
class AdminGroupDAO extends SimpleDAO<AdminGroup> {

	private static final Supplier<AdminGroupDAO> DAO_SUPPLIER = Suppliers.memoize(
			new Supplier<AdminGroupDAO>() {
				public AdminGroupDAO get() {
					return new AdminGroupDAO();
				}
			});


	private AdminGroupDAO() {
		super(AdminGroup.MAP);
	}


	static AdminGroupDAO get() {
		return DAO_SUPPLIER.get();
	}


	boolean makeGroupMemberOfGroupSet(Id grpId, Id grpSetId) {
		JdbcQueryHelper query = new JdbcQueryHelper("UPDATE groups SET set_pk1=? WHERE pk1=?");
		query.setId(1, grpSetId);
		query.setId(2, grpId);
		return query.executeUpdate() == 1;
	}
}