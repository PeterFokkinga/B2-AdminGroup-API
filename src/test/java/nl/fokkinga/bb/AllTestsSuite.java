package nl.fokkinga.bb;

/**
 * @author <a href="p.r.fokkinga [at] rug.nl">Peter Fokkinga</a>
 */

import blackboard.data.ValidationException;
import blackboard.data.course.Course;
import blackboard.data.course.Group;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbPersister;
import blackboard.persist.course.GroupDbPersister;
import nl.fokkinga.bb.admingroup.GroupCodeDAOTest;
import nl.fokkinga.bb.admingroup.GroupCodeTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
		UtilTest.class
		, GroupCodeTest.class
		, GroupCodeDAOTest.class
})

public class AllTestsSuite {
	public static Course crs, crsTwo;
	public static Group mainGroupSet;


	@BeforeClass
	public static void setupCourse() throws PersistenceException, ValidationException {
		crs = new Course();
		crs.setCourseId("junit.GroupCreateTest");
		crs.setTitle("JUnit GroupCreateTest");
		crs.setIsAvailable(true);
		CourseDbPersister.Default.getInstance().persist(crs);

		crsTwo = new Course();
		crsTwo.setCourseId("junit.GroupCreateTest2");
		crsTwo.setTitle("JUnit GroupCreateTest - 2");
		crsTwo.setIsAvailable(true);
		CourseDbPersister.Default.getInstance().persist(crsTwo);

		mainGroupSet = new Group();
		mainGroupSet.setIsAvailable(true);
		mainGroupSet.setGroupSet(true);
		mainGroupSet.setCourseId(crs.getId());
		mainGroupSet.setTitle("Main Group Set");
		GroupDbPersister.Default.getInstance().persist(mainGroupSet);
	}


	@AfterClass
	public static void removeCourse() throws PersistenceException {
		CourseDbPersister dao = CourseDbPersister.Default.getInstance();
		dao.deleteById(crs.getId());
		dao.deleteById(crsTwo.getId());
	}
}
