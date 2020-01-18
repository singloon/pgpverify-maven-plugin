/*
 * Copyright 2017 Slawomir Jaranowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simplify4u.plugins;

import static org.simplify4u.plugins.TestUtils.getArtifact;
import static org.simplify4u.plugins.TestUtils.getPGPgpPublicKey;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Slawomir Jaranowski.
 */
public class KeysMapTest {

    private KeysMap keysMap;

    @BeforeMethod
    public void setUp() throws ComponentLookupException, PlexusContainerException {
        PlexusContainer container = new DefaultPlexusContainer();
        keysMap = container.lookup(KeysMap.class);
    }

    @AfterMethod
    public void tearDown() {
        keysMap = null;
    }

    @Test
    public void isComponentSet() {
        assertNotNull(keysMap);
    }

    @Test
    public void nullLocationTest() throws Exception {
        keysMap.load(null);

        assertTrue(keysMap.isValidKey(getArtifact("test.group", "test", "1.1.1"), null, null));
    }

    @Test
    public void emptyLocationTest() throws Exception {
        keysMap.load("");

        assertTrue(keysMap.isValidKey(getArtifact("test.group", "test", "1.1.1"), null, null));
    }


    @Test
    public void validKeyFromMap1() throws Exception {
        keysMap.load("/keysMap1.list");

        assertTrue(
                keysMap.isValidKey(
                        getArtifact("junit", "junit", "4.12"),
                        getPGPgpPublicKey(0x123456789abcdef0L), null));
        assertTrue(
                keysMap.isValidKey(
                        getArtifact("junit", "junit", "4.12"),
                        getPGPgpPublicKey(0x123456789abcdeffL), null));

        assertTrue(
                keysMap.isValidKey(
                        getArtifact("testlong", "fingerprint", "x.x.x"),
                        getPGPgpPublicKey(0x123456789abcdef0L), null));

    }

    @Test
    public void validKeyFromMap2() throws Exception {
        keysMap.load("/keysMap1.list");

        assertTrue(
                keysMap.isValidKey(
                        getArtifact("test.test", "test", "1.2.3"),
                        getPGPgpPublicKey(0x123456789abcdef0L), null));
    }

    @Test
    public void invalidKeyFromMap() throws Exception {
        keysMap.load("/keysMap1.list");

        assertFalse(
                keysMap.isValidKey(
                        getArtifact("junit", "junit", "4.11"),
                        getPGPgpPublicKey(0x123456789abcdef0L), null));
    }

    @Test
    public void keysProcessedInEncounterOrder() throws Exception {
        keysMap.load("/keysMap2.list");

        assertTrue(
                keysMap.isValidKey(
                        getArtifact("test", "test-package", "1.0.0"),
                        getPGPgpPublicKey(0xA6ADFC93EF34893EL), null));
    }

    @Test
    public void artifactsWithoutKeysProcessed() throws Exception {
        keysMap.load("/keysMap3.list");

        assertTrue(
                keysMap.isNoKey(
                        getArtifact("test", "test-package", "1.0.0")));
        assertFalse(
                keysMap.isValidKey(
                        getArtifact("test", "test-package", "1.0.0"),
                        getPGPgpPublicKey(0xA6ADFC93EF34893EL), null));
        assertFalse(
                keysMap.isNoKey(
                        getArtifact("test", "test-package-2", "1.0.0")));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Key length for = 0x10 is 8 bits, should be between 64 and 160 bits")
    public void shortKeyShouldThrownException() throws Exception {
        keysMap.load("/keyMap-keyToShort.list");

    }
}
