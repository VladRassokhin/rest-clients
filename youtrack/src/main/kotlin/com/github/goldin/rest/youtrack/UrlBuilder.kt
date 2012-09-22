package com.github.goldin.rest.youtrack

import java.net.URLEncoder
import kotlin.test.assertFalse


/**
 * Builds YouTrack REST URLs.
 */
class UrlBuilder( url : String )
{
    private val url : String = if ( url.endsWith( "/" )) url.substring( 0, url.length() - 1 )
                               else                      url
    {
        assertFalse( this.url.trim().isEmpty() || this.url.endsWith( "/" ))
    }


    /**
     * http://evgeny-goldin.org/youtrack/rest/application.wadl
     */
    fun wadl(): String = "$url/rest/application.wadl"


    /**
     * http://confluence.jetbrains.net/display/YTD4/Get+an+Issue
     */
    fun issue ( issueId : String ): String
    {
        checkNotNull( issueId, "'issueId' can't be null" )
        return "$url/rest/issue/$issueId"
    }


    /**
     * http://confluence.jetbrains.net/display/YTD4/Check+that+an+Issue+Exists
     */
    fun issueExists( issueId : String ): String
    {
        checkNotNull( issueId, "'issueId' can't be null" )
        return "${ issue( issueId ) }/exists"
    }


    /**
     * [[URLEncoder.encode()]] wrapper using "UTF-8" charset.
     */
    private fun encode( s : String ): String = URLEncoder.encode( s, "UTF-8" )!!


    /**
     * Builds a URL (url?a=b&c=d) from pairs of arguments ( #(a,b), #(c,d), .. ).
     */
    fun url( url: String, arguments: Collection<Pair<String, String>> ): String = url + '?' + arguments.map {
        pair -> "${ encode( pair.first ) }=${ encode( pair.second ) }"
    }.
    makeString( "&" )
}
