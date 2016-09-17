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

            project.extensions.jsonGenerator.each { JsonGeneratorExtension extension ->
                validateExtension(extension);
                createCleanTaskForExtension(project, cleanTask, extension);
                String json = extension.template.text
                extension.variables.keySet().each { key ->
                    def value = String.valueOf(extension.variables.get(key))
                    json = json.replace('$[' + key + ']$', value)
                }
                extension.output.withWriter { out ->
                    out.write(json);
                }
            }
        }
    }

    static def createCleanTaskForExtension(Project project, Task cleanTask, JsonGeneratorExtension extension) {
        def extensionName = extension.name.substring(0, 1).toUpperCase() + extension.name.substring(1);
        def taskName = 'clean' + extensionName + 'GeneratedJsonFiles'
        cleanTask.dependsOn project.task(taskName, type: Delete) {
            delete extension.output
        }
    }

    static void validateExtension(JsonGeneratorExtension extension) {
        if (extension.template == null || extension.output == null) {
            throw new ProjectConfigurationException('You need to specify at least a template file and an output file to generate a json', null)
        }
        if (!extension.template.exists()) {
            throw new ProjectConfigurationException('The specified template file ' + extension.template + ' does not exist!', null)
        }
    }
}
