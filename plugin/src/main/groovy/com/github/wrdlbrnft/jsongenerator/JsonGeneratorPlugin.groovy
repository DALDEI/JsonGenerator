package com.github.wrdlbrnft.jsongenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.tasks.Delete

/**
 * Created by Xaver on 17/09/16.
 */
class JsonGeneratorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.jsonGenerator = project.container(JsonGeneratorExtension);

        project.afterEvaluate {

            def cleanTask = project.tasks.findByName('clean')
            if (cleanTask == null) {
                cleanTask = project.task('clean') {}
            }

            def buildTask = project.tasks.findByName('build')
            if (buildTask == null) {
                buildTask = project.task('build') {}
            }

            project.extensions.jsonGenerator.each { JsonGeneratorExtension extension ->
                validateExtension(extension);
                def cleanJsonFileTask = createCleanTaskForExtension(project, cleanTask, extension);
                createCreateTaskForExtension(project, buildTask, cleanJsonFileTask, extension);
            }
        }
    }

    static def createCreateTaskForExtension(Project project, Task buildTask, Task cleanJsonFileTask, JsonGeneratorExtension extension) {
        def extensionName = extension.name.substring(0, 1).toUpperCase() + extension.name.substring(1);
        def taskName = 'create' + extensionName + 'GeneratedJsonFile'
        def createJsonFileTask = project.task(taskName) << {
            def json = extension.template.text
            extension.variables.keySet().each { key ->
                def value = String.valueOf(extension.variables.get(key))
                json = json.replace('$[' + key + ']$', value)
            }
            extension.output.withWriter { out ->
                out.write(json);
            }
            if (!extension.output.exists()) {
                throw new ProjectConfigurationException('Failed to create json file for ' + extension.name, null)
            }
            println extension.name + ' Json File has been created: ' + extension.output
        }
        createJsonFileTask.dependsOn cleanJsonFileTask
        buildTask.dependsOn createJsonFileTask
        createJsonFileTask
    }

    static def createCleanTaskForExtension(Project project, Task cleanTask, JsonGeneratorExtension extension) {
        def extensionName = extension.name.substring(0, 1).toUpperCase() + extension.name.substring(1);
        def taskName = 'clean' + extensionName + 'GeneratedJsonFile'
        def cleanJsonFileTask = project.task(taskName, type: Delete) {
            delete extension.output
        }
        cleanTask.dependsOn cleanJsonFileTask
        cleanJsonFileTask
    }

    static def validateExtension(JsonGeneratorExtension extension) {
        if (extension.template == null || extension.output == null) {
            throw new ProjectConfigurationException('You need to specify at least a template file and an output file to generate a json', null)
        }
        if (!extension.template.exists()) {
            throw new ProjectConfigurationException('The specified template file ' + extension.template + ' does not exist!', null)
        }
    }
}
