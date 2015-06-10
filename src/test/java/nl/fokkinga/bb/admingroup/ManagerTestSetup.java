package nl.fokkinga.bb.admingroup;

import blackboard.data.ValidationException;
import blackboard.data.course.Course;
import blackboard.data.course.Group;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupDbPersister;
import nl.fokkinga.bb.AllTestsSuite;
import org.junit.Before;

import java.util.List;


/**
 * @author <a href="p.r.fokkinga [at] rug.nl">Peter Fokkinga</a>
 */
public class ManagerTestSetup {
	Course crs, crs2;
	Group grpSet, grpOne;
	GroupCode codeOne;

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
		codeOne = new GroupCode(grpOne, "foo#bar");
		GroupCodeDAO.get().persist(codeOne);
	}
}
