package eu.ha3.katastrophe.monad

import eu.ha3.katastrophe.monad.ws.AuthenticationService
import eu.ha3.katastrophe.monad.ws.Controller
import eu.ha3.katastrophe.monad.ws.MessageRepository
import eu.ha3.katastrophe.monad.ws.MessageService
import org.junit.jupiter.api.Test

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */

class WebServiceTest {

    @Test
    fun either() {
        val controller = Controller(MessageService(MessageRepository()), AuthenticationService())

        println(controller.processRequest(RequestModel(
                headers = listOf(),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization ", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello", "Bearer bad"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Berer not a good token"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer unknown token"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello "))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1111"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "9999"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "9999"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("end", "2012-04-20")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-40"), Parameter("end", "2012-04-20")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01"), Parameter("end", "2012-04-40")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01"), Parameter("end", "2012-04-20")),
                method = "countBetween"
        )))
    }


}