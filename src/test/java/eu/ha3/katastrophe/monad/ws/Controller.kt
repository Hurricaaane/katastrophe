package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.*

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */

interface IController {
    fun processRequest(request: RequestModel): ResponseModel
}

class Controller(private val messageService: IMessageService, authenticationService: IAuthenticationService) : IController {
    private val daysBetween: DaysBetween = DaysBetween(messageService)
    private val controllerAuthenticationLogic: ControllerAuthenticationLogic

    override fun processRequest(request: RequestModel) = Perhaps.ret(request)
            .verify(controllerAuthenticationLogic::checkAuthentication)
            .bind(this::resolveAndExecuteAction)
            .otherwise(this::toError)

    private fun resolveAndExecuteAction(request: RequestModel): Perhaps<ResponseModel> {
        val ret = Perhaps.ret(request)

        return request.method?.let {
            when (it) {
                "countBetween" -> ret.bind(daysBetween::action)
                else -> ret.bind(this::defaultMethod)
            }
        } ?: ret.bind(this::defaultMethod)
    }


    private fun defaultMethod(request: RequestModel): Perhaps<ResponseModel> = Perhaps.ret(request)
            .bind(this::asFetchMessageQuery)
            .bind(messageService::fetchMessage)
            .bind(this::someMessageAsResponse)

    private fun asFetchMessageQuery(it: RequestModel): Perhaps<FetchMessageQuery> = Perhaps.ret(it)
            .verify(this::validateRequestForFetchMessageQuery)
            .map(this::requestToFetchMessageQuery)

    private fun validateRequestForFetchMessageQuery(it: RequestModel): Err? = when {
        !it.parameters.any { it.key == "id" } -> Err.PARAMETER_MISSING_ID
        else -> null
    }

    private fun requestToFetchMessageQuery(it: RequestModel): FetchMessageQuery =
            FetchMessageQuery(it.parameters.first { it.key == "id" }.value)

    private fun someMessageAsResponse(it: SomeMessage?): Perhaps<ResponseModel> = when {
        it != null -> Perhaps.ret(ResponseModel(200, it.author + ": " + it.content))
        else -> Perhaps.Fail(Err.NOT_FOUND)
    }


    private fun toError(it: Err): ResponseModel = ResponseModel(it.status, it.name)

    class ControllerAuthenticationLogic(private val authenticationService: IAuthenticationService) {
        fun checkAuthentication(me: RequestModel): Err? = Perhaps.ret(me)
                .verify(this::validateAuthHeader)
                .map(this::extractAuthorizationHeader)
                .bind(this::validateTokenType)
                .map(this::extractBearerToken)
                .verify(authenticationService::checkIfAuthenticated)
                .left()

        private fun validateAuthHeader(it: RequestModel): Err? = when {
            it.headers.any { it.key.startsWith("Authorization ") } -> Err.AUTHORIZATION_HEADER_HAS_TRAILING_WHITESPACE
            !it.headers.any { it.key == "Authorization" } -> Err.AUTHORIZATION_HEADER_MISSING
            it.headers.first { it.key == "Authorization" }.values.size > 1 -> Err.TOO_MANY_AUTHORIZATION_HEADERS
            else -> null
        }

        private fun extractAuthorizationHeader(it: RequestModel): String = it.headers.filter { it.key == "Authorization" }.first().values.first()

        private fun validateTokenType(it: String): Perhaps<String> = when {
            !it.startsWith("Bearer ") -> Perhaps.Fail(Err.NOT_BEARER_TOKEN)
            it.trim() != it -> Perhaps.Fail(Err.TOKEN_HAS_TRAILING_WHITESPACE)
            else -> Perhaps.ret(it)
        }

        private fun extractBearerToken(it: String): String = it.substring("Bearer ".length)

    }

    init {
        this.controllerAuthenticationLogic = ControllerAuthenticationLogic(authenticationService)
    }
}