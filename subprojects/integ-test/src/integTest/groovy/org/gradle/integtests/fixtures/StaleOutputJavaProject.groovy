/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.fixtures

import com.google.common.io.Files
import org.gradle.integtests.fixtures.executer.ExecutionResult
import org.gradle.test.fixtures.archive.JarTestFixture
import org.gradle.test.fixtures.file.TestFile

class StaleOutputJavaProject {
    public final static String JAR_TASK_NAME = 'jar'
    private final TestFile testDir
    private final String projectPath
    private final String rootDirName
    private final String buildDirName
    private final TestFile mainSourceFile
    private final TestFile redundantSourceFile
    private final TestFile mainClassFile
    private final TestFile redundantClassFile

    StaleOutputJavaProject(TestFile testDir) {
        this(testDir, null)
    }

    StaleOutputJavaProject(TestFile testDir, String rootDirName) {
        this(testDir, rootDirName, 'build', "")
    }

    StaleOutputJavaProject(TestFile testDir, String rootDirName, String buildDirName, String projectPath) {
        this.testDir = testDir
        this.rootDirName = rootDirName
        this.buildDirName = buildDirName
        this.projectPath = projectPath
        mainSourceFile = writeJavaSourceFile('Main')
        redundantSourceFile = writeJavaSourceFile('Redundant')
        mainClassFile = determineClassFile(mainSourceFile)
        redundantClassFile = determineClassFile(redundantSourceFile)
    }

    private TestFile writeJavaSourceFile(String className) {
        String sourceFilePath = "src/main/java/${className}.java"
        sourceFilePath = prependRootDirName(sourceFilePath)
        def sourceFile = testDir.file(sourceFilePath)
        sourceFile << "public class $className {}"
        sourceFile
    }

    private TestFile determineClassFile(File sourceFile) {
        String classFilePath = "$buildDirName/classes/main/${Files.getNameWithoutExtension(sourceFile.name)}.class"
        classFilePath = prependRootDirName(classFilePath)
        testDir.file(classFilePath)
    }

    private String prependRootDirName(String filePath) {
        rootDirName ? "$rootDirName/$filePath" : filePath
    }

    String getRootDirName() {
        rootDirName
    }

    String getBuildDirName() {
        buildDirName
    }

    TestFile getBuildDir() {
        testDir.file("$rootDirName/$buildDirName")
    }

    TestFile getRedundantSourceFile() {
        redundantSourceFile
    }

    TestFile getMainClassFile() {
        mainClassFile
    }

    TestFile getRedundantClassFile() {
        redundantClassFile
    }

    TestFile getCustomOutputDir() {
        testDir.file("out")
    }

    TestFile getMainClassFileAlternate() {
        customOutputDir.file(mainClassFile.name)
    }

    TestFile getRedundantClassFileAlternate() {
        customOutputDir.file(redundantClassFile.name)
    }


    TestFile getJarFile() {
        String jarFileName = rootDirName ? "${rootDirName}.jar" : "${testDir.name}.jar"
        String path = prependRootDirName("$buildDirName/libs/$jarFileName")
        testDir.file(path)
    }

    String getBuildDirCleanupMessage() {
        String path = prependRootDirName(buildDirName)
        createCleanupMessage(path)
    }

    String defaultOutputDir() {
        "$buildDirName/classes/main"
    }

    String getClassesDirCleanupMessage(String path) {
        createCleanupMessage(prependRootDirName(path))
    }

    String getCompileTaskPath() {
        "${projectPath}:compileJava"
    }
    String getJarTaskPath() {
        "${projectPath}:$JAR_TASK_NAME"
    }

    void assertBuildTasksExecuted(ExecutionResult result) {
        result.assertTaskNotSkipped(getCompileTaskPath())
        result.assertTaskNotSkipped(getJarTaskPath())
    }

    void assertBuildTasksSkipped(ExecutionResult result) {
        result.assertTaskSkipped(getCompileTaskPath())
        result.assertTaskSkipped(getJarTaskPath())
    }

    void assertDoesNotHaveCleanupMessage(ExecutionResult result, String path=defaultOutputDir()) {
        assert !result.output.contains(getClassesDirCleanupMessage(path))
    }

    void assertHasCleanupMessage(ExecutionResult result, String path=defaultOutputDir()) {
        result.assertOutputContains(getClassesDirCleanupMessage(path))
    }

    private String createCleanupMessage(String path) {
        "Cleaned up directory '${new File(testDir, path)}'"
    }

    boolean assertJarHasDescendants(String... relativePaths) {
        new JarTestFixture(jarFile).hasDescendants(relativePaths)
    }
}
