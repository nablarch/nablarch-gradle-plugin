package nablarch.dev

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Nablarchの依存モジュールのバージョンを動的に切り替えるプラグイン。
 *
 * @author Naoki Yamamoto
 */
class NablarchVersionPlugin implements Plugin<Project> {

    /** プロパティファイルのパス */
    private static final String PROPERTY_FILE_PATH = "https://github.com/nablarch/nablarch-module-version/blob/master/"

    @Override
    void apply(Project target) {
        Project.mixin(ProjectMixin)

        // 現在のブランチから読み込むプロパティファイルを取得
        def branch = "git rev-parse --abbrev-ref HEAD".execute().text.replaceAll(/(\r|\n)/, "")

        // Jenkins上でビルドする場合、ブランチ名が取得できないのでディレクトリからブランチを判定する。
        if (branch.equals("HEAD")) {
            def pwd = "pwd".execute().text.replaceAll(/(\r|\n)/, "")
            def dirname = ("dirname " + pwd).execute().text.replaceAll(/(\r|\n)/, "")
            branch = ("basename " + dirname).execute().text.replaceAll(/(\r|\n)/, "")
        }

        // プロパティファイルのロード
        def props = new Properties()
        try {
            def url = new URL(PROPERTY_FILE_PATH + branch + ".properties?raw=true")
            props.load(url.openStream())
        } catch (FileNotFoundException ignored) {
            def url = new URL(PROPERTY_FILE_PATH + "develop.properties?raw=true")
            props.load(url.openStream())
        }

        props.each {
            target.extensions.add(it.key as String, it.value)
        }
    }
}
