package nablarch.dev

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 対象プロジェクトが外部ライブラリ(名称に「nablarch」を含まないjar)に依存しているかを検査する。
 * 検査結果は、外部ライブラリのリストを書き込んだファイルとして出力する。
 *
 * [検査結果の出力方式]
 * 対象プロジェクトのビルドディレクトリ配下にdependenciesディレクトリを作成し、「NotNablarchJars.txt」という名称で保存する。
 * 対象プロジェクトが外部ライブラリへ依存していない場合、空のファイルが作成される。
 * NotNablarchJars.txtは実行の度に上書き保存される。
 *
 * [判定条件]
 * 対象プロジェクトがcompileスコープで依存しているライブラリのみを検査対象とする。
 * 対象プロジェクトがcompileスコープを持たない場合は検査対象外とする。
 * jarの名称に「nablarch」を含むかどうかの判定は、大文字小文字の違いを無視する。
 *
 * @author Akihiko Ookubo
 */
class OutputNotNablarchJarsPlugin implements Plugin<Project> {

    /**
     * outputNotNablarchJarsをProjectに適用する。
     *
     * @param project 適用対象プロジェクト
     */
    @Override
    void apply(Project project) {
        project.task('outputNotNablarchJars') << {

            List<String> jarNames = new ArrayList<>()

            if (project.configurations.hasProperty("compile")) {
                project.configurations.compile.each {
                    File file
                        -> jarNames.addAll(file.name.split(".jar").findAll { jarName -> !jarName.toLowerCase().contains("nablarch") })
                }
                outputJarList(project.buildDir, jarNames)
            } else {
                println("Could not find property 'compile' from project:" + project.name)
            }
        }
    }

    /**
     * NotNablarchJars.txtを作成し書き込む。
     *
     * @param outputRootDir dependencies/NotNablarchJars.txtを作成するディレクトリ
     * @param jarNames jarの名称のリスト
     */
    private static void outputJarList(File outputRootDir, List<String> jarNames) {

        def outputDir = new File("${outputRootDir}/dependencies/")
        outputDir.mkdirs()

        def outPutFile = new File(outputDir, "NotNablarchJars.txt")
        outPutFile.createNewFile()
        outPutFile.write("")

        outPutFile.withWriter {out ->
            jarNames.each{jarName ->
                out.write(jarName)
                out.write(System.properties['line.separator'])
            }
        }
    }
}