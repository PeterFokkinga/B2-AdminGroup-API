package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.db.Transaction;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;


/**
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
public interface AdminGroupManager {
	AdminGroup loadGroupById(Id grpId);

	/**
	 * Delete the given group / group set. When deleting a group its group codes
	 * will be deleted as well. When deleting a group set its child groups
	 * (including any group codes) will be deleted as well.
	 *
	 * @param grpId the ID of the group or group set to delete
	 */
	void deleteGroupById(Id grpId);

	/**
	 * Make a group part of a group set. With the Blackboard API it is only
	 * possible to add a group to a group set when creating the group. With
	 * this method you can add a "single" group to a group set, or move a
	 * group from one group set to another.
	 *
	 * @param grpId the ID of the group that should be part of the group set
	 * @param grpSetId the ID of the group set that will be parent for the group
	 * @return true when the move was successful
	 */
	boolean addGroupToGroupSet(Id grpId, Id grpSetId);


	@Transaction
	void persist(AdminGroup grp) throws PersistenceException, ValidationException;
}
