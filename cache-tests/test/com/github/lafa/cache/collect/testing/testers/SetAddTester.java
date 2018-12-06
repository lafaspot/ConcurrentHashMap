/*
 * Copyright (C) 2007 The Guava Authors
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

package com.github.lafa.cache.collect.testing.testers;

import com.github.lafa.cache.collect.testing.Helpers;
import com.github.lafa.cache.collect.testing.features.CollectionFeature;
import com.github.lafa.cache.collect.testing.features.CollectionSize;



import static com.github.lafa.cache.collect.testing.features.CollectionFeature.ALLOWS_NULL_VALUES;
import static com.github.lafa.cache.collect.testing.features.CollectionFeature.SUPPORTS_ADD;
import static com.github.lafa.cache.collect.testing.features.CollectionSize.ZERO;

import java.lang.reflect.Method;
import org.junit.Ignore;

/**
 * A generic JUnit test which tests add operations on a set. Can't be invoked directly; please see
 * {@link com.github.lafa.cache.collect.testing.SetTestSuiteBuilder}.
 *
 * @author Kevin Bourrillion
 */

@Ignore // Affects only Android test runner, which respects JUnit 4 annotations on JUnit 3 tests.
public class SetAddTester<E> extends AbstractSetTester<E> {
  @CollectionFeature.Require(SUPPORTS_ADD)
  @CollectionSize.Require(absent = ZERO)
  public void testAdd_supportedPresent() {
    assertFalse("add(present) should return false", getSet().add(e0()));
    expectUnchanged();
  }

  @CollectionFeature.Require(value = {SUPPORTS_ADD, ALLOWS_NULL_VALUES})
  @CollectionSize.Require(absent = ZERO)
  public void testAdd_supportedNullPresent() {
    E[] array = createArrayWithNullElement();
    collection = getSubjectGenerator().create(array);
    assertFalse("add(nullPresent) should return false", getSet().add(null));
    expectContents(array);
  }

  /**
   * Returns the {@link Method} instance for {@link #testAdd_supportedNullPresent()} so that tests
   * can suppress it. See {@link CollectionAddTester#getAddNullSupportedMethod()} for details.
   */
   // reflection
  public static Method getAddSupportedNullPresentMethod() {
    return Helpers.getMethod(SetAddTester.class, "testAdd_supportedNullPresent");
  }
}