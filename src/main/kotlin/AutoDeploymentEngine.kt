import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

fun main() {
    AutoDeploymentEngine()
}

class AutoDeploymentEngine {

    private val logLocation = File("/var/log/AutoDeploymentEngine")

    init {
        if (!logLocation.exists()) logLocation.mkdirs()
        Thread{AutoUpdateApp("AutoDeploymentEngine")}.start()
        while (true) {
//            val jsonString = "{\"action\": {\"AutoDeploymentEngine\": \"update\"}}"
//            val jsonString = "{\"action\": {\"AutoDeploymentEngine\": \"update\"}}"
//            println(apiRequest(jsonString))
            Thread.sleep(1000 * 2)
            val actions = apiRequest("{\"AutoDeploymentEngine\":  \"serverName\"}")
                .replace("[", "")
                .replace("]", "")
                .split(",").also { println(it) }
            actions.forEach { action ->
                when {
                    action.contains("\"start") -> start(action)
                    action.contains("\"stop") -> stop(action)
                    action.contains("\"deploy") -> deploy(action)
                    action.contains("\"restart") -> restart(action)
                    else -> println("nothing to run")
                }
            }
            Thread.sleep(6 * 1000)
        }
    }

    private fun parseFolder(url: String): String = url.split("\"")[3].split("/", ".")[2]

    private fun parseUrl(url: String): String = url.split("\"")[3]


    private fun start(url: String) {
        val folder = File("${System.getProperty("user.home")}/code/${parseFolder(url)}")
        if (!folder.exists()) deploy(url)
        "bash $folder/${parseFolder(url)}/scripts/start.bash >> ${logLocation.absoluteFile}".runCommand(null)
    }

    private fun stop(url: String) {
        val folder = File("${System.getProperty("user.home")}/code/${parseUrl(url)}")
        "bash $folder/${parseFolder(url)}/scripts/kill.bash >> ${logLocation.absoluteFile}".runCommand(null)
    }

    private fun deploy(url: String) {
        val folder = File("${System.getProperty("user.home")}/code/")
        "git clone ${parseUrl(url)} $folder/${parseFolder(url)}".also(::println).runCommand(null)
        "bash $folder/${parseFolder(url)}/scripts/install.bash >> ${logLocation.absoluteFile}".runCommand(null)
    }

    fun restart(url: String) {
    }


    private fun apiRequest(jsonString: String): String {
        val url = URL("http://youcantblock.me:556/")
        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.doOutput = true
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/json; utf-8")
        con.setRequestProperty("Accept", "application/json")
        con.outputStream.use { os ->
            val input = jsonString.toByteArray()
            os.write(input, 0, input.size)
        }
        BufferedReader(InputStreamReader(con.inputStream, "utf-8")).use { br ->
            val response = StringBuilder()
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                response.append(responseLine!!.trim { it <= ' ' })
            }
            return response.toString()
        }
    }


    /**
     * Modifies the string class to add a run command func
     */
    private fun String.runCommand(workingDir: File?) {
        ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()
    }
}