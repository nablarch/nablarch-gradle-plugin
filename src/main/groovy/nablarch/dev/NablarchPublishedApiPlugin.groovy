package nablarch.dev

import org.gradle.api.*

class NablarchPublishedApiPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    Project.mixin(ProjectMixin)
    project.with {
      apply plugin: 'java'

      extensions.create("publishedApi", NablarchPublishedApiExtension)

      publishedApi.roles.each { role ->
        def taskName = "generatePublishedApiDocFor${role.capitalize()}"
        task([type: NablarchPublishedApiTask], taskName) {
          doFirst {
            apiName = project.publishedApi.apiName
            publishedFor = role
          }
        }
      }
    }
  }

  static class NablarchPublishedApiExtension {

    /**
     * 生成するAPIの名前。
     * 使用許可API一覧ファイルの名前に使用される。
     */
    String apiName

    /** 公開許可が与えられてるロール一覧。 */
    List<String> roles = ['architect', 'programmer']

  }
}
