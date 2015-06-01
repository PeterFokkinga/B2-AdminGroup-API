/*
 * Copyright 2015 Peter R. Fokkinga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.fokkinga.bb;

import blackboard.persist.Id;



public final class Util {
	/**
	 * Returned by {@link #toNumber(Id)} when its argument is an unset id.
	 */
	public static final int UNSET_ID_VALUE = -1;

	private Util() {}


	/**
	 * Convenience method for preventing NullPointerException.
	 *
	 * @param input a string that may be NULL
	 * @return the provided string with leading/trailing whitespace removed,
	 *         or an empty string if the input was null
	 */
	public static String makeSafe(String input) {
		return (input != null) ? input.trim() : "";
	}


	/**
	 * Checks if the given string is empty or not.
	 *
	 * @param str the string to test
	 * @return false if the string contains text; true if it is NULL or only
	 *         contains whitespace
	 */
	public static boolean isEmpty(String str) {
		return (str == null) || (str.trim().length() == 0);
	}


	/**
	 * Checks if the given string is empty or not.
	 *
	 * @param str the string to test
	 * @return true if the string contains text; false if it is NULL or only
	 *         contains whitespace
	 */
	public static boolean notEmpty(String str) {
		return (str != null) && (str.trim().length() > 0);
	}


	/**
	 * Converts Blackboard Id object to its numeric representation. When the
	 * external string representation of an Id looks like "_1234_1", the
	 * corresponding numeric representation is 1234.
	 *
	 * @param id can be any type of Id (courseId, userId, groupId, etc)
	 * @return numeric representation for the Id or UNSET_ID_VALUE if the
	 *         Id is Id.UNSET_ID
	 * @throws NumberFormatException when the given ID is NULL
	 * @see #UNSET_ID_VALUE
	 */
	public static long toNumber(Id id) throws NumberFormatException {
		if (id == null) {
			throw new NumberFormatException("id is null");
		} else if (id == Id.UNSET_ID) {
			return UNSET_ID_VALUE;
		}
		String externalId = id.toExternalString();
		return externalId.startsWith("_")
				? Long.valueOf(externalId.substring(1, externalId.lastIndexOf('_')))
				: Long.valueOf(externalId);
	}
}