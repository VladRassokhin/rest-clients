package com.github.goldin.rest.common

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.security.SecureRandom
import java.nio.charset.Charset
import java.util.Random
import java.io.InputStream
import com.google.common.io.InputSupplier
import java.awt.event.MouseEvent
import java.awt.event.MouseAdapter
import com.google.common.io.ByteStreams
import java.util.zip.Adler32
import java.util.zip.Checksum
import com.google.common.primitives.Bytes
import com.google.common.io.ByteStreams.*
import jet.Map
import jet.List
import com.google.common.io.CharStreams
import java.io.InputStreamReader


class InputStreamSupplier( val stream: InputStream ): InputSupplier<InputStream>
{
    override fun getInput(): InputStream = stream
}


/**
 * Base class for all other tests. Contains various utility methods.
 */
open class BaseTest
{
    public val restClientsUrl  : String = "http://rest-clients.myjetbrains.com/youtrack/"
    public val evgenyGoldinUrl : String = "http://evgeny-goldin.org/youtrack"
    public val jetbrainsUrl    : String = "http://youtrack.jetbrains.com/"


    fun dateFormat( pattern: String, timezone: String = "GMT" ): DateFormat
    {
        val dateFormat = SimpleDateFormat( pattern )
        dateFormat.setTimeZone( TimeZone.getTimeZone( timezone ))
        return dateFormat
    }


    fun inputStream( resourcePath: String ): InputStream = this.javaClass.getResourceAsStream( resourcePath )!!


    fun toString( resourcePath: String ): String = CharStreams.toString( InputStreamReader( inputStream( resourcePath )))!!


    fun random(): Random = SecureRandom( System.currentTimeMillis().toString().getBytes( "UTF-8" ))


    fun checksum( content: String, charset : String = "UTF-8", checksum : Checksum = Adler32()): Long =
        getChecksum( newInputStreamSupplier( content.getBytes( charset )), checksum )


    fun checksum( stream: InputStream, checksum : Checksum = Adler32()): Long =
        getChecksum( InputStreamSupplier( stream ), checksum )
}


