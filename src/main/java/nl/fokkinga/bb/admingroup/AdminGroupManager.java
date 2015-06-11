package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.db.Transaction;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;

import java.util.List;


/**
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
public interface AdminGroupManager {

	/**
	 * Get a group with the given batch_uid. Depending on the implementation
	 * of the manager the result will be <em>the</em> group identified by the
	 * batch_uid or just <em>a</em> group having the batch_uid.
	 *
	 * @param uid the batch_uid to search for
	 * @return a (the) group that has the given batch_uid or NULL when no groups
	 * could be found
	 * @throws IllegalStateException when the batch_uid violates the uniqueness
	 *                               rules of the group manager implementation
	 * @see #loadGroupsByBatchUid(String) is a better choice when you're not
	 * using the batch_uid as a uniquely identifying property of a group
	 */
	AdminGroup loadGroupByBatchUid(String uid);

	/**
	 * Get groups with the given batch_uid.
	 *
	 * @param uid uid the batch_uid to search for
	 * @return the groups that have the given batch_uid; the result may be empty
	 * but is never NULL
	 */
	List<AdminGroup> loadGroupsByBatchUid(String uid);

	/**
	 * Get the group identified by the given id.
	 *
	 * @param grpId the ID of the group
	 * @return the group with the given ID or NULL if no such group exists
	 */
	AdminGroup loadGroupById(Id grpId);

	/**
	 * Get all groups belonging to the course.
	 *
	 * @param crsId the ID of the course to retrieve the groups for
	 * @return the groups (<em>not group sets!</em>) that are in the course;
	 * the result may be empty but is never NULL
	 */
	List<AdminGroup> loadGroupsByCourseId(Id crsId);

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
	 * @param grpId    the ID of the group that should be part of the group set
	 * @param grpSetId the ID of the group set that will be parent for the group
	 * @return true when the move was successful
	 */
	boolean addGroupToGroupSet(Id grpId, Id grpSetId);


	@Transaction
	void persist(AdminGroup grp) throws PersistenceException, ValidationException;
}
