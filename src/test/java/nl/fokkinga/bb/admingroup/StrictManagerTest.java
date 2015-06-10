package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.data.course.Group;
import blackboard.persist.*;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.impl.GroupDAO;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


/**
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
public class StrictManagerTest extends ManagerTestSetup{

	@Test
	public void insertTest() throws ValidationException, PersistenceException {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();

		AdminGroup ag = new AdminGroup();
		ag.setTitle("Hello, World!");
		ag.setCourseId(crs.getId());
		ag.setSourcedId("foo", "baz");
		mngr.persist(ag);
		assertTrue(Id.isValidPkId(ag.getId()));

		try {
			Group grp = GroupDbLoader.Default.getInstance().loadById(ag.getId());
			assertNotNull(grp);
			assertEquals(ag.getId(), grp.getId());
			assertEquals(crs.getId(), grp.getCourseId());
			assertEquals("Hello, World!", grp.getTitle());
		} catch (KeyNotFoundException e) {
			fail("new admin group not found in database");
		}

		List<GroupCode> codes = GroupCodeDAO.get().loadByGroupId(ag.getId());
		assertNotNull(codes);
		assertEquals(1, codes.size());
		assertEquals("foo", codes.get(0).getSourcedIdSource());
		assertEquals("baz", codes.get(0).getSourcedIdId());
		assertEquals(crs.getId(), codes.get(0).getCourseId());
	}

	@Test
	public void insertDuplicateBatchUidTest() throws ValidationException, PersistenceException {
		List<GroupCode> codes = GroupCodeDAO.get().loadBySourcedId("foo", "bar");
		int numCodesBefore = codes.size();
		assertTrue(numCodesBefore > 0);
		assertNotEquals(crs2.getId(), codes.get(0).getCourseId());

		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();

		AdminGroup ag = new AdminGroup();
		ag.setTitle("Hello, World!");
		ag.setCourseId(crs2.getId());
		ag.setSourcedId("foo", "bar");
		try {
			mngr.persist(ag);
			fail("persisting an already existing batch_uid should fail for strict mode");
		} catch (DuplicateBatchUidException e) { /* expected behaviour */ }
		// ag.getId() now has a validPkId, that is a bit weird

		try {
			GroupDbLoader.Default.getInstance().loadById(ag.getId());
			fail("duplicate group should not exist");
		} catch (KeyNotFoundException e) { /* expected behaviour */ }

		codes = GroupCodeDAO.get().loadBySourcedId("foo", "bar");
		int numCodesAfter = codes.size();
		assertEquals(numCodesBefore, numCodesAfter);
	}

	@Test
	public void updateTest() throws ValidationException, PersistenceException {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		AdminGroup ag = mngr.loadGroupById(grpOne.getId());
		assertFalse(ag.getIsAvailable());
		ag.setTitle("A New Hope");
		ag.setIsAvailable(true);
		ag.setBatchUid("foo#baz");
		mngr.persist(ag);

		try {
			Group grp = GroupDbLoader.Default.getInstance().loadById(grpOne.getId());
			assertNotNull(grp);
			assertTrue(grp.getIsAvailable());
			assertEquals(ag.getId(), grp.getId());
			assertEquals(crs.getId(), grp.getCourseId());
			assertEquals("A New Hope", grp.getTitle());
		} catch (KeyNotFoundException e) {
			fail("updated admin group not found in database");
		}

		List<GroupCode> codes = GroupCodeDAO.get().loadByGroupId(ag.getId());
		assertNotNull(codes);
		assertEquals(1, codes.size());
		assertEquals("foo", codes.get(0).getSourcedIdSource());
		assertEquals("baz", codes.get(0).getSourcedIdId());
	}


	@Test
	public void updateDuplicateBatchUidTest() throws ValidationException, PersistenceException {
		List<GroupCode> codes = GroupCodeDAO.get().loadBySourcedId("foo", "bar");
		int numCodesBefore = codes.size();
		assertTrue(numCodesBefore > 0);
		assertNotEquals(crs2.getId(), codes.get(0).getCourseId());

		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();

		AdminGroup ag = new AdminGroup();
		ag.setTitle("Hello, World!");
		ag.setCourseId(crs2.getId());
		ag.setSourcedId("foo", "baz");
		mngr.persist(ag);

		assertTrue(Id.isValidPkId(ag.getId()));
		assertEquals(1, GroupCodeDAO.get().loadByGroupId(ag.getId()).size());

		ag.setTitle("A New Hope");
		ag.setBatchUid("foo#bar");
		try {
			mngr.persist(ag);
			fail("persisting an already existing batch_uid should fail for strict mode");
		} catch (DuplicateBatchUidException e) { /* expected behaviour */ }

		try {
			Group grp = GroupDbLoader.Default.getInstance().loadById(ag.getId());
			assertNotNull(grp);
			assertEquals(ag.getId(), grp.getId());
			assertEquals(crs2.getId(), grp.getCourseId());
			assertEquals("Hello, World!", grp.getTitle());
		} catch (KeyNotFoundException e) {
		  fail("the original group data should still exist!");
		}

		codes = GroupCodeDAO.get().loadBySourcedId("foo", "bar");
		int numCodesAfter = codes.size();
		assertEquals(numCodesBefore, numCodesAfter);
		codes = GroupCodeDAO.get().loadByGroupId(ag.getId());
		assertEquals(1, codes.size());
		assertEquals("foo#baz", codes.get(0).getBatchUid()); // the original
	}

	@Test
	public void deleteGroupTest() throws PersistenceException {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		mngr.deleteGroupById(grpOne.getId());

		try {
			GroupDbLoader.Default.getInstance().loadById(grpOne.getId());
			fail("deleted group should not exist");
		} catch (KeyNotFoundException e) { /* expected behaviour */ }

		List<GroupCode> codes = GroupCodeDAO.get().loadByGroupId(grpOne.getId());
		assertEquals(0, codes.size());
	}

	@Test
	public void deleteGroupSetTest() throws ValidationException, PersistenceException {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();

		AdminGroup set = new AdminGroup();
		set.setTitle("a set");
		set.setCourseId(crs.getId());
		set.setGroupSet(true);
		set.setBatchUid("groupset#42");
		mngr.persist(set);
		assertTrue(Id.isValidPkId(set.getId()));

		AdminGroup grp = new AdminGroup();
		grp.setTitle("a set");
		grp.setCourseId(crs.getId());
		grp.setSetId(set.getId());
		grp.setBatchUid("group#42");
		mngr.persist(grp);
		assertTrue(Id.isValidPkId(set.getId()));

		List<Group> groups = GroupDAO.get().loadGroupSetList(set.getId());
		assertEquals(1, groups.size());
		List<GroupCode> codes = GroupCodeDAO.get().loadByGroupSetId(set.getId());
		assertEquals(1, codes.size());

		mngr.deleteGroupById(set.getId());

		try {
			GroupDbLoader.Default.getInstance().loadById(set.getId());
			fail("deleted group set should not exist");
		} catch (KeyNotFoundException e) { /* expected behaviour */ }
		try {
			GroupDbLoader.Default.getInstance().loadById(grp.getId());
			fail("group in deleted group set should not exist");
		} catch (KeyNotFoundException e) { /* expected behaviour */ }

		assertTrue(GroupCodeDAO.get().loadByGroupId(set.getId()).isEmpty());
		assertTrue(GroupCodeDAO.get().loadByGroupId(grp.getId()).isEmpty());
	}

	@Test
	public void groupToGroupSetTest() {
		AdminGroupManager mngr = AdminGroupManagerFactory.getStrictManager();
		assertEquals(grpSet.getCourseId(), grpOne.getCourseId());
		assertNotEquals(grpSet.getId(), grpOne.getSetId());
		assertEquals(0, GroupDAO.get().loadGroupSetList(grpSet.getId()).size());

		mngr.addGroupToGroupSet(grpOne.getId(), grpSet.getId());
		assertEquals(1, GroupDAO.get().loadGroupSetList(grpSet.getId()).size());
	}
}
