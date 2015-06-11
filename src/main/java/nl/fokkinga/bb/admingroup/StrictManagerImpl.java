package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.persist.*;
import blackboard.persist.course.GroupDbPersister;
import blackboard.platform.course.CourseGroupManagerFactory;

import java.util.List;


/**
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
public class StrictManagerImpl implements AdminGroupManager {

	@Override public AdminGroup loadById(Id grpId) {
		try {
			return AdminGroupDAO.get().loadById(grpId);
		} catch (KeyNotFoundException e) { /* don't care */ }
		return null;
	}


	@Override public AdminGroup loadSingleByBatchUid(String uid) {
		List<AdminGroup> groups = loadByBatchUid(uid);
		return groups.isEmpty() ? null : groups.get(0);
	}


	@Override public List<AdminGroup> loadByBatchUid(String uid) {
		List<AdminGroup> result = AdminGroupDAO.get().loadByBatchUid(uid);
		if (result.size() > 1) {
			throw new IllegalStateException("multiple groups have batch_uid " + uid);
		}
		return result;
	}


	@Override public List<AdminGroup> loadGroupsByCourseId(Id crsId) {
		return AdminGroupDAO.get().loadByCourseId(crsId, AdminGroupDAO.Selector.GROUPS);
	}


	@Override public boolean addGroupToGroupSet(Id grpId, Id grpSetId) {
		return AdminGroupDAO.get().makeGroupMemberOfGroupSet(grpId, grpSetId);
	}


	@Override public void persist(AdminGroup grp) throws ValidationException, PersistenceException {
		grp.validate();

		/*
		 * persist group first, otherwise when the group is new the group code
		 * returned by gtGroupCode will not have a proper group ID
		 */
		GroupDbPersister.Default.getInstance().persist(grp);

		GroupCodeDAO dao = GroupCodeDAO.get();
		dao.persist(grp.getGroupCode());
		if (!dao.isUnique(grp.getBatchUid(), grp.getId())) {

			/*
			 * this will cause a complete rollback, including the persist of the
			 * group, because in the interface this method is annotated with
			 * @Transaction
			 */
			throw new DuplicateBatchUidException(grp.getGroupCode().toString());
		}
	}


	@Override public void deleteGroupById(Id grpId) {
		AdminGroup grp = loadById(grpId);
		if (grp != null) {
			if (grp.isGroupSet()) {
				CourseGroupManagerFactory.getInstance().deleteGroupSet(grp.getId());
			} else {
				try {
					GroupDbPersister.Default.getInstance().deleteById(grpId);
				} catch (PersistenceException e) {
					throw new PersistenceRuntimeException(grp + " delete caused: " + e.getMessage(), e);
				}
			}
		}
	}
}
