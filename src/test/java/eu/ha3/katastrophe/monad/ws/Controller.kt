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

    override fun processRequest(request: RequestModel) = Possibly.ret(request)
            .verify(controllerAuthenticationLogic::checkAuthentication)
            .bind(this::resolveAndExecuteAction)
            .otherwise(this::toError)

    private fun resolveAndExecuteAction(request: RequestModel): Possibly<ResponseModel> {
        val ret = Possibly.ret(request)

        return request.method?.let {
            when (it) {
                "countBetween" -> ret.bind(daysBetween::action)
                else -> ret.bind(this::defaultMethod)
            }
        } ?: ret.bind(this::defaultMethod)
    }


    private fun defaultMethod(request: RequestModel): Possibly<ResponseModel> = Possibly.ret(request)
            .bind(this::asFetchMessageQuery)
            .bind(messageService::fetchMessage)
            .bind(this::someMessageAsResponse)

    private fun asFetchMessageQuery(it: RequestModel): Possibly<FetchMessageQuery> = Possibly.ret(it)
            .verify(this::validateRequestForFetchMessageQuery)
            .map(this::requestToFetchMessageQuery)

    private fun validateRequestForFetchMessageQuery(it: RequestModel): Err? = when {
        !it.parameters.any { it.key == "id" } -> Err.PARAMETER_MISSING_ID
        else -> null
    }

    private fun requestToFetchMessageQuery(it: RequestModel): FetchMessageQuery =
            FetchMessageQuery(it.parameters.first { it.key == "id" }.value)

    private fun someMessageAsResponse(it: SomeMessage?): Possibly<ResponseModel> = when {
        it != null -> Possibly.ret(ResponseModel(200, it.author + ": " + it.content))
        else -> Possibly.Fail(Err.NOT_FOUND)
    }


    private fun toError(it: Err): ResponseModel = ResponseModel(it.status, it.name)

    class ControllerAuthenticationLogic(private val authenticationService: IAuthenticationService) {
        fun checkAuthentication(me: RequestModel): Err? = Possibly.ret(me)
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

        private fun validateTokenType(it: String): Possibly<String> = when {
            !it.startsWith("Bearer ") -> Possibly.Fail(Err.NOT_BEARER_TOKEN)
            it.trim() != it -> Possibly.Fail(Err.TOKEN_HAS_TRAILING_WHITESPACE)
            else -> Possibly.ret(it)
        }

        private fun extractBearerToken(it: String): String = it.substring("Bearer ".length)

    }

    init {
        this.controllerAuthenticationLogic = ControllerAuthenticationLogic(authenticationService)
    }
}