package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.*
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.UnsupportedTemporalTypeException

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */

interface IController {
    fun processRequest(request: RequestModel): ResponseModel
}

class Controller(private val useCase: IMessageService, authenticationService: IAuthenticationService) : IController {
    private val controllerAuthenticationLogic: ControllerAuthenticationLogic

    override fun processRequest(request: RequestModel) = Either.ret<Err, RequestModel>(request)
            .verify(controllerAuthenticationLogic::checkAuthentication)
            .bind(this::resolveAndExecuteAction)
            .otherwise(this::toError)

    private fun resolveAndExecuteAction(request: RequestModel): Either<Err, ResponseModel> {
        val ret = Either.ret<Err, RequestModel>(request)

        return request.method?.let {
            when (it) {
                "countBetween" -> ret.bind(this::daysBetweenMethod)
                else -> ret.bind(this::defaultMethod)
            }
        } ?: ret.bind(this::defaultMethod)
    }

    private fun daysBetweenMethod(request: RequestModel): Either<Err, ResponseModel> = Either.ret<Err, RequestModel>(request)
            .bind(this::asCountBetweenQuery)
            .bind(useCase::getCountBetween)
            .map(this::countAsResponse)

    private fun asCountBetweenQuery(it: RequestModel): Either<Err, CountBetweenQuery> = Either.ret<Err, RequestModel>(it)
            .verify(this::validateRequestForCountBetweenQuery)
            .bind(this::requestCountBetweenQuery)

    private fun requestCountBetweenQuery(it: RequestModel): Either<Err, CountBetweenQuery> {
        return try {
            Either.ret(CountBetweenQuery(extractLocalDate(it, "begin"), extractLocalDate(it, "end")))

        } catch (e: UnsupportedTemporalTypeException) {
            Either.Left(Err.DATE_FORMAT_IS_INVALID)

        } catch (e: DateTimeException) {
            Either.Left(Err.DATE_FORMAT_IS_INVALID)
        }
    }

    private fun extractLocalDate(it: RequestModel, param: String) =
            LocalDate.from(DateTimeFormatter.ISO_DATE.parse(it.parameters.first { it.key == param }.value))

    private fun validateRequestForCountBetweenQuery(it: RequestModel): Err? = when {
        !it.parameters.any { it.key == "begin" } -> Err.PARAMETER_MISSING_BEGIN_DATE
        !it.parameters.any { it.key == "end" } -> Err.PARAMETER_MISSING_END_DATE
        else -> null
    }

    private fun defaultMethod(request: RequestModel): Either<Err, ResponseModel> = Either.ret<Err, RequestModel>(request)
            .bind(this::asFetchMessageQuery)
            .bind(useCase::fetchMessage)
            .map(this::someMessageAsResponse)

    private fun asFetchMessageQuery(it: RequestModel): Either<Err, FetchMessageQuery> = Either.ret<Err, RequestModel>(it)
            .verify(this::validateRequestForFetchMessageQuery)
            .map(this::requestToFetchMessageQuery)

    private fun validateRequestForFetchMessageQuery(it: RequestModel): Err? = when {
        !it.parameters.any { it.key == "id" } -> Err.PARAMETER_MISSING_ID
        else -> null
    }

    private fun requestToFetchMessageQuery(it: RequestModel): FetchMessageQuery =
            FetchMessageQuery(it.parameters.first { it.key == "id" }.value)

    private fun someMessageAsResponse(it: SomeMessage?): ResponseModel = when {
        it != null -> ResponseModel(200, it.author + ": " + it.content)
        else -> ResponseModel(404, "Not found")
    }

    private fun countAsResponse(it: Long): ResponseModel = ResponseModel(200, it.toString())


    private fun toError(it: Err): ResponseModel = ResponseModel(it.status, it.name)

    class ControllerAuthenticationLogic(private val authenticationService: IAuthenticationService) {
        fun checkAuthentication(me: RequestModel): Err? = Either.ret<Err, RequestModel>(me)
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

        private fun validateTokenType(it: String): Either<Err, String> = when {
            !it.startsWith("Bearer ") -> Either.Left(Err.NOT_BEARER_TOKEN)
            it.trim() != it -> Either.Left(Err.TOKEN_HAS_TRAILING_WHITESPACE)
            else -> Either.ret(it)
        }

        private fun extractBearerToken(it: String): String = it.substring("Bearer ".length)

    }

    init {
        this.controllerAuthenticationLogic = ControllerAuthenticationLogic(authenticationService)
    }
}