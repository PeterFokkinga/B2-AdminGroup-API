package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.data.course.Course;
import blackboard.data.course.Group;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupDbPersister;
import nl.fokkinga.bb.AllTestsSuite;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


/**
 * @author <a href="p.r.fokkinga [at] rug.nl">Peter Fokkinga</a>
 */
public class GroupCodeDAOTest {
	Course crs, crs2;
	Group grpSet;
	Group grpOne, grpTwo, grpThreeInCrs2;

	@Before
	public void setup() throws PersistenceException, ValidationException {
		crs = AllTestsSuite.crs;
		crs2 = AllTestsSuite.crsTwo;
		grpSet = AllTestsSuite.mainGroupSet;
		GroupDbLoader ldr = GroupDbLoader.Default.getInstance();
		List<Group> groups = ldr.loadByCourseId(crs.getId());
		groups.addAll(ldr.loadByCourseId(crs2.getId()));
		GroupDbPersister dao = GroupDbPersister.Default.getInstance();
		for (Group grp : groups) {
			if (!grp.isGroupSet()) {
				dao.deleteById(grp.getId());
			}
		}
		grpOne = new Group();
		grpOne.setTitle("one");
		grpOne.setCourseId(crs.getId());
		dao.persist(grpOne);

		grpTwo = new Group();
		grpTwo.setTitle("two");
		grpTwo.setCourseId(crs.getId());
		dao.persist(grpTwo);

		grpThreeInCrs2 = new Group();
		grpThreeInCrs2.setTitle("three");
		grpThreeInCrs2.setCourseId(crs2.getId());
		dao.persist(grpThreeInCrs2);
	}

	@Test
	public void isUniqueTest() {
		GroupCodeDAO dao = GroupCodeDAO.get();
		assertTrue(dao.isUnique("foo", null));
		
		dao.persist(new GroupCode(grpOne, "foo"));
		assertTrue(dao.isUnique("foo", null));
		assertTrue(dao.isUnique("foo", grpOne.getId()));
		assertFalse(dao.isUnique("foo", grpTwo.getId()));

		dao.persist(new GroupCode(grpTwo, "bar"));
		assertTrue(dao.isUnique("foo", null));
		assertTrue(dao.isUnique("foo", grpOne.getId()));
		assertFalse(dao.isUnique("foo", grpTwo.getId()));

		dao.persist(new GroupCode(grpThreeInCrs2, "foo"));
		assertFalse(dao.isUnique("foo", null));
		assertFalse(dao.isUnique("foo", grpOne.getId()));
		assertFalse(dao.isUnique("foo", grpTwo.getId()));
	}

	@Test
	public void isUniqueInCourseTest() {
		GroupCodeDAO dao = GroupCodeDAO.get();
		assertTrue(dao.isUniqueInCourse(crs.getId(), "foo", null));

		dao.persist(new GroupCode(grpOne, "foo"));
		assertTrue(dao.isUniqueInCourse(crs.getId(), "foo", null));
		assertTrue(dao.isUniqueInCourse(crs.getId(), "foo", grpOne.getId()));
		assertFalse(dao.isUniqueInCourse(crs.getId(), "foo", grpTwo.getId()));

		dao.persist(new GroupCode(grpTwo, "bar"));
		assertTrue(dao.isUniqueInCourse(crs.getId(), "foo", null));
		assertTrue(dao.isUniqueInCourse(crs.getId(), "foo", grpOne.getId()));
		assertFalse(dao.isUniqueInCourse(crs.getId(), "foo", grpTwo.getId()));

		dao.persist(new GroupCode(grpThreeInCrs2, "foo"));
		assertTrue(dao.isUniqueInCourse(crs.getId(), "foo", null));
		assertTrue(dao.isUniqueInCourse(crs.getId(), "foo", grpOne.getId()));
		assertFalse(dao.isUniqueInCourse(crs.getId(), "foo", grpTwo.getId()));
	}

	@Test
	public void loadByGroupIdTest() throws PersistenceException {
		GroupCodeDAO dao = GroupCodeDAO.get();
		List<GroupCode> codes = dao.loadByGroupId(Id.generateId(Group.DATA_TYPE, 999L));
		assertNotNull(codes);
		assertEquals(0, codes.size());

		codes = dao.loadByGroupId(grpOne.getId());
		assertNotNull(codes);
		assertEquals(0, codes.size());

		dao.persist(new GroupCode(grpOne, "foo"));
		codes = dao.loadByGroupId(grpOne.getId());
		assertEquals(1, codes.size());

		dao.persist(new GroupCode(grpOne, "bar"));
		codes = dao.loadByGroupId(grpOne.getId());
		assertEquals(2, codes.size());
	}

	@Test
	public void loadByBatchUidTest() throws PersistenceException {
		GroupCodeDAO dao = GroupCodeDAO.get();
		List<GroupCode> codes = dao.loadByBatchUid("foo");
		assertNotNull(codes);
		assertEquals(0, codes.size());

		dao.persist(new GroupCode(grpOne, "foo"));
		codes = dao.loadByBatchUid("foo");
		assertEquals(1, codes.size());

		dao.persist(new GroupCode(grpOne, "bar"));
		codes = dao.loadByBatchUid("foo");
		assertEquals(1, codes.size());

		dao.persist(new GroupCode(grpThreeInCrs2, "foo"));
		codes = dao.loadByBatchUid("foo");
		assertEquals(2, codes.size());
	}

	@Test
	public void loadBySourcedIdTest() {
		GroupCodeDAO dao = GroupCodeDAO.get();
		List<GroupCode> codes = dao.loadBySourcedId("", null);
		assertNotNull(codes);
		assertEquals(dao.loadAll().size(), codes.size());

		dao.persist(new GroupCode(grpOne, "junit#foo"));
		dao.persist(new GroupCode(grpOne, "junit#bar"));
		dao.persist(new GroupCode(grpTwo, "ng#baz"));
		codes = dao.loadBySourcedId("", null);
		assertEquals(dao.loadAll().size(), codes.size());

		codes = dao.loadBySourcedId("junit", null);
		assertEquals(2, codes.size());

		codes = dao.loadBySourcedId("junit", "foo");
		assertEquals(1, codes.size());
	}


	@Test
	public void deleteByGroupIdTest() {
		GroupCodeDAO dao = GroupCodeDAO.get();
		dao.persist(new GroupCode(grpOne, "foo"));
		dao.persist(new GroupCode(grpOne, "bar"));
		dao.persist(new GroupCode(grpTwo, "baz"));
		assertEquals(2, dao.loadByGroupId(grpOne.getId()).size());
		assertEquals(1, dao.loadByGroupId(grpTwo.getId()).size());

		dao.deleteByGroupId(grpOne.getId());
		assertEquals(0, dao.loadByGroupId(grpOne.getId()).size());
		assertEquals(1, dao.loadByGroupId(grpTwo.getId()).size());
	}
}
