package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.data.ValidationWarning;
import blackboard.data.course.Course;
import blackboard.data.course.Group;
import nl.fokkinga.bb.AllTestsSuite;
import nl.fokkinga.bb.Util;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


/**
 * @author <a href="p.r.fokkinga [at] rug.nl">Peter Fokkinga</a>
 */
public class GroupCodeTest {
	Course crs;
	Group grpSet;

	@Before
	public void setup() {
		crs = AllTestsSuite.crs;
		grpSet = AllTestsSuite.mainGroupSet;
	}

	@Test
	public void sourcedidTest() {
		GroupCode code = new GroupCode();
		assertEquals("", code.getSourcedIdSource());
		assertEquals("", code.getSourcedIdId());

		code.setGroupId(grpSet.getId());
		String grpPk1 = String.valueOf(Util.toNumber(grpSet.getId()));
		assertEquals(GroupCode.BBLEARN_SOURCEDID_SOURCE, code.getSourcedIdSource());
		assertEquals(grpPk1, code.getSourcedIdId());

		String src = "foo";
		String id = "bar";
		code.setSourcedId(src, id);
		assertEquals(src, code.getSourcedIdSource());
		assertEquals(id, code.getSourcedIdId());

		try {
			code.setSourcedId(src, "");
			fail("empty ID should not be accepted");
		} catch (IllegalArgumentException e) { /* expected behaviour */ }

		try {
			code.setSourcedId(null, id);
			fail("empty Source should not be accepted");
		} catch (IllegalArgumentException e) { /* expected behaviour */ }
	}

	@Test
	public void emptyTest() {
		GroupCode code = new GroupCode();
		assertTrue(code.toString().contains("new"));
		assertFalse(code.toString().contains("group_pk1"));
		assertTrue("".equals(code.getBatchUid()));
		try {
			code.validate();
			fail("empty group code should fail validation");
		} catch (ValidationException e) {
			List<ValidationWarning> warnings = e.getWarnings();
			assertTrue(warnings.size() > 0);
		}
	}

	@Test
	public void tooLongBatchUidTest() {
		GroupCode code = new GroupCode();
		String batchUid = StringUtils.repeat("x", 100);
		assertTrue(batchUid.length() == 100);
		code.setCourseId(crs.getId());
		code.setGroupId(grpSet.getId());

		assertTrue(code.toString().contains("group_pk1"));

		code.setBatchUid(batchUid);
		try {
			code.validate();
		} catch (ValidationException e) {
			fail("batch_uid with length 100 should be acceptable");
		}
		code.setBatchUid(StringUtils.repeat("x", 101));
		try {
			code.validate();
			fail("batch_uid with length > 0 should fail validation");
		} catch (ValidationException e) { /* expected behaviour */ }
	}

	@Test
	public void utf8BatchUidTest() {
		GroupCode code = new GroupCode();
		code.setCourseId(crs.getId());
		code.setGroupId(grpSet.getId());
		code.setBatchUid("Hello, \u201CWorld\u201D");
		try {
			code.validate();
			fail("batch_uid with non-ISO_8859 characters should fail validation");
		} catch (ValidationException e) { /* expected behaviour */ }
	}
}
