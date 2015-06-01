package nl.fokkinga.bb;

import blackboard.data.course.Course;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import org.junit.Test;

import static nl.fokkinga.bb.Util.*;
import static org.junit.Assert.*;


public class UtilTest {
	@Test
	public void makeSafeTest() {
		assertEquals("", makeSafe(""));
		assertEquals("", makeSafe(null));
		assertEquals("Hello, World!", makeSafe("Hello, World!"));
		assertEquals("Hello,\nWorld!", makeSafe("   Hello,\nWorld!   "));
	}

	@Test
	public void isEmptyTest() {
		assertTrue(isEmpty(""));
		assertTrue(isEmpty("   "));
		assertTrue(isEmpty(" \n  "));
		assertTrue(isEmpty(null));
		assertFalse(isEmpty("-1"));
		assertFalse(isEmpty("0"));
		assertFalse(isEmpty("Hello, World!"));
	}

	@Test
	public void isNotEmptyTest() {
		assertFalse(notEmpty(""));
		assertFalse(notEmpty("   "));
		assertFalse(notEmpty(" \n  "));
		assertFalse(notEmpty(null));
		assertTrue(notEmpty("-1"));
		assertTrue(notEmpty("0"));
		assertFalse(isEmpty("Hello, World!"));
	}

	@Test
	public void toNumberTest() throws PersistenceException {
		try {
			toNumber(null);
			fail("NULL argument should cause NumberFormatException");
		} catch (NumberFormatException e) { /* expected behaviour */ }

		assertEquals(Util.UNSET_ID_VALUE, toNumber(Id.UNSET_ID));

		Id newId = Id.newId(Course.DATA_TYPE);
		/*
		 * the external representation of a "proper" id is _123_1
		 * but for a newly created (not yet persisted) id it is 123
		 */
		assertEquals(newId.toExternalString(), String.valueOf(toNumber(newId)));

		Id existingId = Id.generateId(Course.DATA_TYPE, 5L);
		assertEquals(5, toNumber(existingId));
	}
}