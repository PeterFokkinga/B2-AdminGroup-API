package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.data.course.Group;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupDbPersister;
import nl.fokkinga.bb.Util;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


/**
 * @author <a href="p.r.fokkinga [at] rug.nl">Peter Fokkinga</a>
 */
public class StrictLoadingTest extends ManagerTestSetup {

	@Test
	public void loadByIdTest() throws PersistenceException {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		AdminGroup grp = mngr.loadById(Id.generateId(Group.DATA_TYPE, 999L));
		assertNull(grp);

		grp = mngr.loadById(grpOne.getId());
		assertNotNull(grp);
		assertEquals(codeOne, grp.getGroupCode());
		assertEquals(grpOne.getId(), grp.getId());

		grp = mngr.loadById(grpSet.getId()); // doesn't have a group code in the db
		assertNotNull(grp);

		GroupCode gc = grp.getGroupCode();
		assertNotNull(gc);
		assertNotNull(gc.getBatchUid());
		assertTrue(gc.getBatchUid().startsWith(GroupCode.BBLEARN_SOURCEDID_SOURCE));
		assertEquals(GroupCode.BBLEARN_SOURCEDID_SOURCE, gc.getSourcedIdSource());

		String grpPk1 = String.valueOf(Util.toNumber(grpSet.getId()));
		assertTrue(gc.getBatchUid().contains(grpPk1));
		assertEquals(grpPk1, gc.getSourcedIdId());
	}

	@Test
	public void loadGroupByBatchUidTest() throws PersistenceException, ValidationException {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		AdminGroup grp = mngr.loadSingleByBatchUid("xxxxx");
		assertNull(grp);

		grp = mngr.loadSingleByBatchUid(codeOne.getBatchUid());
		assertNotNull(grp);
		assertEquals(codeOne.getBatchUid(), grp.getBatchUid());
		assertEquals(codeOne.getGroupId(), grp.getId());

		Group g2 = new Group();
		g2.setTitle("Hello, World!");
		g2.setCourseId(codeOne.getCourseId());
		GroupDbPersister.Default.getInstance().persist(g2);
		assertTrue(Id.isValidPkId(g2.getId()));
		GroupCode c2 = new GroupCode(g2, codeOne.getBatchUid());
		GroupCodeDAO.get().persist(c2);
		assertTrue(Id.isValidPkId(c2.getId()));

		try {
			mngr.loadSingleByBatchUid(codeOne.getBatchUid());
			fail("duplicate batch_uid should cause IllegalStateException");
		} catch (IllegalStateException e) { /* expected behaviour */ }

		String batchUid = GroupCode.generateBatchUid(grpSet.getId());
		grp = mngr.loadSingleByBatchUid(batchUid);
		assertNotNull(grp);
		assertEquals(grpSet.getId(), grp.getId());
		assertEquals(batchUid, grp.getBatchUid());
	}

	@Test
	public void loadGroupsByBatchUidTest() throws PersistenceException, ValidationException {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		List<AdminGroup> grps = mngr.loadByBatchUid("xxxxx");
		assertNotNull(grps);
		assertEquals(0, grps.size());

		grps = mngr.loadByBatchUid(codeOne.getBatchUid());
		assertNotNull(grps);
		assertEquals(1, grps.size());
		assertEquals(codeOne.getBatchUid(), grps.get(0).getBatchUid());
		assertEquals(codeOne.getGroupId(), grps.get(0).getId());

		Group g2 = new Group();
		g2.setTitle("Hello, World!");
		g2.setCourseId(codeOne.getCourseId());
		GroupDbPersister.Default.getInstance().persist(g2);
		assertTrue(Id.isValidPkId(g2.getId()));
		GroupCode c2 = new GroupCode(g2, codeOne.getBatchUid());
		GroupCodeDAO.get().persist(c2);
		assertTrue(Id.isValidPkId(c2.getId()));

		try {
			mngr.loadByBatchUid(codeOne.getBatchUid());
			fail("duplicate batch_uid should cause IllegalStateException");
		} catch (IllegalStateException e) { /* expected behaviour */ }
	}

	@Test
	public void loadGroupsByCourseIdTest() {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		List<AdminGroup> grps = mngr.loadGroupsByCourseId(crs2.getId());
		assertNotNull(grps);
		assertEquals(0, grps.size());

		grps = mngr.loadGroupsByCourseId(crs.getId());
		assertEquals(2, grps.size());
		for (AdminGroup grp : grps) {
			assertFalse(grp.isGroupSet());
		}
	}

	@Test
	public void loadGroupSetsByCourseIdTest() {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		List<AdminGroup> grps = mngr.loadGroupSetsByCourseId(crs.getId());
		assertEquals(1, grps.size());
		assertTrue(grps.get(0).isGroupSet());
	}
}
