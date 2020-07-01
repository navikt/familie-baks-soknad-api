package no.nav.familie.ba.soknad.api.config

import com.nimbusds.jose.util.BoundedInputStream
import com.nimbusds.jose.util.IOUtils
import com.nimbusds.jose.util.Resource
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import java.io.IOException
import java.io.InputStream
import java.lang.ClassCastException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext


class CustomTLSProxyAwareResourceRetriewer(usePlainTextForHttps: Boolean)
    : ProxyAwareResourceRetriever(null, usePlainTextForHttps) {

    override fun retrieveResource(url: URL?): Resource {
        val ssl = SSLContext.getInstance("TLSv1.2")
        ssl.init(null, null, SecureRandom())

        var connection: HttpsURLConnection? = null
        try {
            connection = url!!.openConnection() as HttpsURLConnection
            connection.sslSocketFactory = ssl.socketFactory

            connection.apply {
                this.connectTimeout = connectTimeout
                this.readTimeout = readTimeout
            }
            var content: String? = null
            getInputStream(connection, sizeLimit).use { inputStream ->
                content = IOUtils.readInputStreamToString(inputStream, StandardCharsets.UTF_8)
            }

            // Check HTTP code + message
            val statusCode = connection.responseCode
            val statusMessage = connection.responseMessage

            // Ensure 2xx status code
            if (statusCode > 299 || statusCode < 200) {
                throw IOException("HTTP $statusCode: $statusMessage")
            }

            return Resource(content, connection.contentType)

        } catch (e: ClassCastException) {
            throw IOException("Couldn't open HTTP(S) connection: ", e)
        } finally {
            connection?.disconnect()
        }


    }

    @Throws(IOException::class)
    private fun getInputStream(con: HttpURLConnection, sizeLimit: Int): InputStream {

        val inputStream = con.inputStream

        return if (sizeLimit > 0) BoundedInputStream(inputStream, getSizeLimit().toLong()) else inputStream
    }
}