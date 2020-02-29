import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class AutoUpdateApp(private val programName: String) {
    init {
        Thread {
            val jsonString = "{\"programCheckIn\": \"AutoDeploymentEngine\"}"
            while (true) {
                println("sending")
                apiRequest(jsonString)
                Thread.sleep(6 * 1000)
            }
        }.start()
    }

    private fun apiRequest(jsonString: String) {
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
            println(response.toString())
        }
    }
}