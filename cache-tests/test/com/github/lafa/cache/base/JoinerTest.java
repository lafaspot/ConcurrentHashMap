/*
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lafa.cache.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.lafa.cache.base.Joiner.MapJoiner;
import com.github.lafa.cache.common.testing.NullPointerTester;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Unit test for {@link Joiner}.
 *
 * @author Kevin Bourrillion
 */
public class JoinerTest extends TestCase {
	private static final Joiner J = Joiner.on("-");

	// <Integer> needed to prevent warning :(
	private static final List<Integer> ITERABLE_ = Arrays.<Integer>asList();
	private static final List<Integer> ITERABLE_1 = Arrays.asList(1);
	private static final List<Integer> ITERABLE_12 = Arrays.asList(1, 2);
	private static final List<Integer> ITERABLE_123 = Arrays.asList(1, 2, 3);
	private static final List<Integer> ITERABLE_NULL = Arrays.asList((Integer) null);
	private static final List<Integer> ITERABLE_NULL_NULL = Arrays.asList((Integer) null, null);
	private static final List<Integer> ITERABLE_NULL_1 = Arrays.asList(null, 1);
	private static final List<Integer> ITERABLE_1_NULL = Arrays.asList(1, null);
	private static final List<Integer> ITERABLE_1_NULL_2 = Arrays.asList(1, null, 2);
	private static final List<Integer> ITERABLE_FOUR_NULLS = Arrays.asList((Integer) null, null, null, null);

	public void testNoSpecialNullBehavior() {
		checkNoOutput(J, ITERABLE_);
		checkResult(J, ITERABLE_1, "1");
		checkResult(J, ITERABLE_12, "1-2");
		checkResult(J, ITERABLE_123, "1-2-3");

		try {
			J.join(ITERABLE_NULL);
			fail();
		} catch (NullPointerException expected) {
		}
		try {
			J.join(ITERABLE_1_NULL_2);
			fail();
		} catch (NullPointerException expected) {
		}

		try {
			J.join(ITERABLE_NULL.iterator());
			fail();
		} catch (NullPointerException expected) {
		}
		try {
			J.join(ITERABLE_1_NULL_2.iterator());
			fail();
		} catch (NullPointerException expected) {
		}
	}

	public void testOnCharOverride() {
		Joiner onChar = Joiner.on('-');
		checkNoOutput(onChar, ITERABLE_);
		checkResult(onChar, ITERABLE_1, "1");
		checkResult(onChar, ITERABLE_12, "1-2");
		checkResult(onChar, ITERABLE_123, "1-2-3");
	}

	public void testSkipNulls() {
		Joiner skipNulls = J.skipNulls();
		checkNoOutput(skipNulls, ITERABLE_);
		checkNoOutput(skipNulls, ITERABLE_NULL);
		checkNoOutput(skipNulls, ITERABLE_NULL_NULL);
		checkNoOutput(skipNulls, ITERABLE_FOUR_NULLS);
		checkResult(skipNulls, ITERABLE_1, "1");
		checkResult(skipNulls, ITERABLE_12, "1-2");
		checkResult(skipNulls, ITERABLE_123, "1-2-3");
		checkResult(skipNulls, ITERABLE_NULL_1, "1");
		checkResult(skipNulls, ITERABLE_1_NULL, "1");
		checkResult(skipNulls, ITERABLE_1_NULL_2, "1-2");
	}

	public void testUseForNull() {
		Joiner zeroForNull = J.useForNull("0");
		checkNoOutput(zeroForNull, ITERABLE_);
		checkResult(zeroForNull, ITERABLE_1, "1");
		checkResult(zeroForNull, ITERABLE_12, "1-2");
		checkResult(zeroForNull, ITERABLE_123, "1-2-3");
		checkResult(zeroForNull, ITERABLE_NULL, "0");
		checkResult(zeroForNull, ITERABLE_NULL_NULL, "0-0");
		checkResult(zeroForNull, ITERABLE_NULL_1, "0-1");
		checkResult(zeroForNull, ITERABLE_1_NULL, "1-0");
		checkResult(zeroForNull, ITERABLE_1_NULL_2, "1-0-2");
		checkResult(zeroForNull, ITERABLE_FOUR_NULLS, "0-0-0-0");
	}

	private static void checkNoOutput(Joiner joiner, Collection<Integer> set) {
		assertEquals("", joiner.join(set));
		assertEquals("", joiner.join(set.iterator()));

		Object[] array = new ArrayList(set).toArray(new Integer[0]);
		assertEquals("", joiner.join(array));

		StringBuilder sb1FromIterable = new StringBuilder();
		assertSame(sb1FromIterable, joiner.appendTo(sb1FromIterable, set));
		assertEquals(0, sb1FromIterable.length());

		StringBuilder sb1FromIterator = new StringBuilder();
		assertSame(sb1FromIterator, joiner.appendTo(sb1FromIterator, set));
		assertEquals(0, sb1FromIterator.length());

		StringBuilder sb2 = new StringBuilder();
		assertSame(sb2, joiner.appendTo(sb2, array));
		assertEquals(0, sb2.length());

		try {
			joiner.appendTo(NASTY_APPENDABLE, set);
		} catch (IOException e) {
			throw new AssertionError(e);
		}

		try {
			joiner.appendTo(NASTY_APPENDABLE, set.iterator());
		} catch (IOException e) {
			throw new AssertionError(e);
		}

		try {
			joiner.appendTo(NASTY_APPENDABLE, array);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private static final Appendable NASTY_APPENDABLE = new Appendable() {
		@Override
		public Appendable append(CharSequence csq) throws IOException {
			throw new IOException();
		}

		@Override
		public Appendable append(CharSequence csq, int start, int end) throws IOException {
			throw new IOException();
		}

		@Override
		public Appendable append(char c) throws IOException {
			throw new IOException();
		}
	};

	private static void checkResult(Joiner joiner, List<Integer> parts, String expected) {
		assertEquals(expected, joiner.join(parts));
		assertEquals(expected, joiner.join(parts.iterator()));

		StringBuilder sb1FromIterable = new StringBuilder().append('x');
		joiner.appendTo(sb1FromIterable, parts);
		assertEquals("x" + expected, sb1FromIterable.toString());

		StringBuilder sb1FromIterator = new StringBuilder().append('x');
		joiner.appendTo(sb1FromIterator, parts.iterator());
		assertEquals("x" + expected, sb1FromIterator.toString());

		Integer[] partsArray = parts.toArray(new Integer[0]);
		assertEquals(expected, joiner.join(partsArray));

		StringBuilder sb2 = new StringBuilder().append('x');
		joiner.appendTo(sb2, partsArray);
		assertEquals("x" + expected, sb2.toString());

		int num = partsArray.length - 2;
		if (num >= 0) {
			Object[] rest = new Integer[num];
			for (int i = 0; i < num; i++) {
				rest[i] = partsArray[i + 2];
			}

			assertEquals(expected, joiner.join(partsArray[0], partsArray[1], rest));

			StringBuilder sb3 = new StringBuilder().append('x');
			joiner.appendTo(sb3, partsArray[0], partsArray[1], rest);
			assertEquals("x" + expected, sb3.toString());
		}
	}

	public void test_useForNull_skipNulls() {
		Joiner j = Joiner.on("x").useForNull("y");
		try {
			j = j.skipNulls();
			fail();
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void test_skipNulls_useForNull() {
		Joiner j = Joiner.on("x").skipNulls();
		try {
			j = j.useForNull("y");
			fail();
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void test_useForNull_twice() {
		Joiner j = Joiner.on("x").useForNull("y");
		try {
			j = j.useForNull("y");
			fail();
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void testMap() {
		MapJoiner j = Joiner.on(';').withKeyValueSeparator(":");
		Map<String, String> map1 = new HashMap<>();
		Map<String, String> map2 = new HashMap<>();
		map2.put("", "");

		assertEquals("", j.join(map1));
		assertEquals(":", j.join(map2));

		Map<String, String> mapWithNulls = new LinkedHashMap<>();
		mapWithNulls.put("a", null);
		mapWithNulls.put(null, "b");

		try {
			j.join(mapWithNulls);
			fail();
		} catch (NullPointerException expected) {
		}

		assertEquals("a:00;00:b", j.useForNull("00").join(mapWithNulls));

		StringBuilder sb = new StringBuilder();
		Map<Integer, Integer> map = new HashMap<>();
		map.put(1, 2);
		map.put(3, 4);
		map.put(5, 6);
		j.appendTo(sb, map);
		assertEquals("1:2;3:4;5:6", sb.toString());
	}

	public void testEntries() {
		MapJoiner j = Joiner.on(";").withKeyValueSeparator(":");
		assertEquals("", j.join(new HashMap<>().entrySet()));
		assertEquals("", j.join(new HashMap<>().entrySet().iterator()));
		final Map<String, String> m3 = new HashMap<>();
		m3.put("", "");
		assertEquals(":", j.join(m3.entrySet()));
		final Map<String, String> m4 = new HashMap<>();
		m4.put("", "");
		assertEquals(":", j.join(m4.entrySet().iterator()));

		Map<String, String> mapWithNulls = new LinkedHashMap();
		mapWithNulls.put("a", null);
		mapWithNulls.put(null, "b");
		Set<Entry<String, String>> entriesWithNulls = mapWithNulls.entrySet();

		try {
			j.join(entriesWithNulls);
			fail();
		} catch (NullPointerException expected) {
		}

		try {
			j.join(entriesWithNulls.iterator());
			fail();
		} catch (NullPointerException expected) {
		}

		assertEquals("a:00;00:b", j.useForNull("00").join(entriesWithNulls));
		assertEquals("a:00;00:b", j.useForNull("00").join(entriesWithNulls.iterator()));
	}

	public void test_skipNulls_onMap() {
		Joiner j = Joiner.on(",").skipNulls();
		try {
			j.withKeyValueSeparator("/");
			fail();
		} catch (UnsupportedOperationException expected) {
		}
	}

	private static class DontStringMeBro implements CharSequence {
		@Override
		public int length() {
			return 3;
		}

		@Override
		public char charAt(int index) {
			return "foo".charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return "foo".subSequence(start, end);
		}

		@Override
		public String toString() {
			throw new AssertionFailedError("shouldn't be invoked");
		}
	}

	// StringBuilder.append in GWT invokes Object.toString(), unlike the JRE
	// version.
	public void testDontConvertCharSequenceToString() {
		assertEquals("foo,foo", Joiner.on(",").join(new DontStringMeBro(), new DontStringMeBro()));
		assertEquals("foo,bar,foo",
				Joiner.on(",").useForNull("bar").join(new DontStringMeBro(), null, new DontStringMeBro()));
	}
}
