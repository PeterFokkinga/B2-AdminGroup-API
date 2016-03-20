package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import nl.fokkinga.bb.AllTestsSuite;
import nl.fokkinga.bb.Util;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author <a href="mailto:peter [at] fokkinga.nl">Peter Fokkinga</a>
 */
public class AdminGroupTest {

	@Test
	public void emptyObjectTest() {
		AdminGroup ag = new AdminGroup();
		assertTrue(ag.toString().contains("pk1=new"));
		assertTrue(ag.isNew());

		try {
			ag.validate();
		} catch (ValidationException e) {
			/*
			 * at the very least:
			 *  missing title
			 *  missing course ID
			 *  various group code violations
			 */
			assertTrue(e.getWarnings().size() > 3);
		}
	}

	@Test
	public void properObjectTest() throws ValidationException, PersistenceException {
		AdminGroup ag = new AdminGroup();
		ag.setTitle("Hello, World!");
		ag.setCourseId(AllTestsSuite.crsTwo.getId());
		ag.setGroupSet(true);
		ag.setBatchUid("properobject");

		try {
			ag.validate();
		} catch (ValidationException e) {
			fail("should validate; " + e.getMessage());
		}
		assertTrue(ag.isNew());
		AdminGroupDAO.get().persist(ag);
		assertFalse(ag.isNew());

		assertTrue(ag.toString().contains("batch_uid='properobject'"));
		assertTrue(ag.toString().contains("groupset"));
		assertTrue(ag.toString().contains("crs_pk1="));

		ag.setGroupSet(false);
		ag.setSetId(AllTestsSuite.mainGroupSet.getId());
		assertTrue(ag.toString().contains("set_pk1="));
	}

	@Test
	public void groupWithoutCodeTest() {
		try {
			AdminGroup ag = AdminGroupDAO.get().loadById(AllTestsSuite.mainGroupSet.getId());
			assertNotNull(ag.getGroupCode());
			assertNotNull(ag.getBatchUid());
			assertTrue(ag.getBatchUid().startsWith(GroupCode.BBLEARN_SOURCEDID_SOURCE));
			assertTrue(ag.getBatchUid().contains(String.valueOf(Util.toNumber(AllTestsSuite.mainGroupSet.getId()))));
		} catch (KeyNotFoundException e) {
			fail("group without group code should be returned by AdminGroupDAO");
		}
	}
}
