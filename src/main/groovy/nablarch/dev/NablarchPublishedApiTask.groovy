package nablarch.dev

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.*

/**
 * 公開APIドキュメントを生成するためのタスク。
 *
 * @author T.Kawasaki
 * @since 1.5.0
 */
class NablarchPublishedApiTask extends Javadoc {

  /** ドックレットの完全修飾名 */
  static final String DOCLET_CLASS = 'nablarch.tool.published.doclet.PublishedDoclet'

  /** リンク先API */
  static final String LINK_OFFLINE = 'http://docs.oracle.com/javase/jp/6/api/'

  static final String PUBLISHED_CONF = 'publishedApiDoc'

  /**
   * 生成するAPIの名前。
   * 使用許可API一覧ファイルの名前に使用される。
   */
  String apiName

  /**
   * 公開許可が与えられてるロール。
   */
  String publishedFor

  /** コンストラクタ。*/
  NablarchPublishedApiTask() {
    addFixedOptionParametersTo(options)
    setSource(project.sourceSets.main.allJava)
  }

  /** {@inheritDoc} */
  @TaskAction
  @Override
  protected void generate() {
    assert apiName != null
    assert publishedFor != null
    def pubConf = project.configurations.getByName(PUBLISHED_CONF)
    assert pubConf != null : "configuration [${PUBLISHED_CONF}] must be set."
    failOnError = true
    title = project.name

    destinationDir = project.file("${project.docsDir}/publishedApiDoc/${publishedFor}")

    classpath += project.configurations[PUBLISHED_CONF]

    (options as StandardJavadocDocletOptions).with {
      docletpath = project.configurations[PUBLISHED_CONF].files.asType(List)
      addStringOption('tag', "${publishedFor}")
      addStringOption('output', "${project.buildDir}/${apiName}For${publishedFor.capitalize()}.config")
      def enc = project.getRequired('encoding')
      encoding = enc
      docEncoding = enc
      charSet = enc
      windowTitle = apiName
      docTitle = apiName
      locale = project.getRequired('locale');
    }

    retrievePackageList(LINK_OFFLINE)

    super.generate()
  }

  protected File getPackageList() {
    File dir = project.file("${project.buildDir}/tmp/javaapi/")
    dir.mkdirs()
    return new File(dir, "package-list")
  }

  protected void retrievePackageList(String baseUrl) {
    if (packageList.exists()) {
      return;
    }
    URL url = new URL("${baseUrl}/package-list")
    String content
    try {
      content = url.text
    } catch (IOException e) {
      logger.lifecycle("could not retrive [${url}]", e)
      return
    }
    packageList.createNewFile()
    packageList.text = content
  }

  /**
   * 本タスクのプションパラメータ固定値を設定する。
   *
   * @param javadocOptions 設定対象オブジェクト
   */
  private void addFixedOptionParametersTo(MinimalJavadocOptions javadocOptions) {

    def StandardJavadocDocletOptions opts = javadocOptions as StandardJavadocDocletOptions

    opts.with {
      author = true
      version = true
      memberLevel = JavadocMemberLevel.PROTECTED
      use = true
      noNavBar = false
      noTree = true
      noDeprecated = false
      noDeprecatedList = false
      noIndex = false
      splitIndex = true
      noHelp = true

      // Nablarch Specific Options
      group(project.name, ['nablarch.*'])
      doclet = DOCLET_CLASS

      linksOffline(LINK_OFFLINE, packageList.parent)
    }
  }
}
