package eu.ha3.katastrophe.monad

import eu.ha3.katastrophe.monad.ws.AuthenticationService
import eu.ha3.katastrophe.monad.ws.Controller
import eu.ha3.katastrophe.monad.ws.MessageRepository
import eu.ha3.katastrophe.monad.ws.MessageService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */

class WebServiceTest {
    @Test
    fun webServiceTest() {
        val controller = Controller(MessageService(MessageRepository()), AuthenticationService())

        assertThat(controller.processRequest(RequestModel(
                headers = listOf(),
                parameters = listOf(Parameter("id", "1992"))
        )), `is`(ResponseModel(401, Err.AUTHORIZATION_HEADER_MISSING.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization ", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1992"))
        )), `is`(ResponseModel(400, Err.AUTHORIZATION_HEADER_HAS_TRAILING_WHITESPACE.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello", "Bearer bad"))),
                parameters = listOf(Parameter("id", "1992"))
        )), `is`(ResponseModel(400, Err.TOO_MANY_AUTHORIZATION_HEADERS.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Berer not a good token"))),
                parameters = listOf(Parameter("id", "1992"))
        )), `is`(ResponseModel(400, Err.NOT_BEARER_TOKEN.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer unknown token"))),
                parameters = listOf(Parameter("id", "1992"))
        )), `is`(ResponseModel(401, Err.NOT_AUTHENTICATED.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello "))),
                parameters = listOf(Parameter("id", "1992"))
        )), `is`(ResponseModel(400, Err.TOKEN_HAS_TRAILING_WHITESPACE.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1992"))
        )), `is`(ResponseModel(200, "Hay: 1992 Message")))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1111"))
        )), `is`(ResponseModel(404, Err.NOT_FOUND.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "9999"))
        )), `is`(ResponseModel(500, Err.UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "9999"))
        )), `is`(ResponseModel(500, Err.UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01")),
                method = "countBetween"
        )), `is`(ResponseModel(400, Err.PARAMETER_MISSING_END_DATE.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("end", "2012-04-20")),
                method = "countBetween"
        )), `is`(ResponseModel(400, Err.PARAMETER_MISSING_BEGIN_DATE.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-40"), Parameter("end", "2012-04-20")),
                method = "countBetween"
        )), `is`(ResponseModel(400, Err.DATE_FORMAT_IS_INVALID.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01"), Parameter("end", "2012-04-40")),
                method = "countBetween"
        )), `is`(ResponseModel(400, Err.DATE_FORMAT_IS_INVALID.toString())))
        assertThat(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01"), Parameter("end", "2012-04-20")),
                method = "countBetween"
        )), `is`(ResponseModel(200, "28")))
    }


}