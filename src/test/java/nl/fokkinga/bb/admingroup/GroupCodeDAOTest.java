package nl.fokkinga.bb.admingroup;

import blackboard.data.course.Course;
import blackboard.data.course.Group;
import nl.fokkinga.bb.AllTestsSuite;
import org.junit.Before;


/**
 * @author <a href="p.r.fokkinga [at] rug.nl">Peter Fokkinga</a>
 */
public class GroupCodeDAOTest {
	Course crs;
	Group grpSet;

	@Before
	public void setup() {
		crs = AllTestsSuite.crs;
		grpSet = AllTestsSuite.mainGroupSet;
	}

}
