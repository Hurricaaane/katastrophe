package eu.ha3.katastrophe.monad

import java.time.LocalDate

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */

data class RequestModel(val headers: List<Header>, val parameters: List<Parameter>, val method: String?) {
    constructor(headers: List<Header>, parameters: List<Parameter>) : this(headers, parameters, null)
}
data class ResponseModel(val status: Int, val body: String)
data class Header(val key: String, val values: List<String>)
data class Parameter(val key: String, val value: String)
data class FetchMessageQuery(val id: String)
data class CountBetweenQuery(val begin: LocalDate, val end: LocalDate)
data class SomeMessage(val author: String, val content: String)
