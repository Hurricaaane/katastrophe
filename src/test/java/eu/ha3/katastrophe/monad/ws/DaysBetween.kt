package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.*
import org.funktionale.currying.curried
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.UnsupportedTemporalTypeException

/**
 * (Default template)
 * Created on 2017-11-24
 *
 * @author Ha3
 */
class DaysBetween(private val useCase: IMessageService) {

    internal fun action(request: RequestModel): Possibly<ResponseModel> = Possibly.ret(request)
            .bind(this::asCountBetweenQuery)
            .bind(useCase::getCountBetween)
            .map(this::countAsResponse)

    private fun asCountBetweenQuery(it: RequestModel): Possibly<CountBetweenQuery> = Possibly.ret(it)
            .verify(this::validateRequestForCountBetweenQuery)
            .bind(this::requestCountBetweenQueryB)

    private fun validateRequestForCountBetweenQuery(it: RequestModel): Err? = when {
        !it.parameters.any { it.key == "begin" } -> Err.PARAMETER_MISSING_BEGIN_DATE
        !it.parameters.any { it.key == "end" } -> Err.PARAMETER_MISSING_END_DATE
        else -> null
    }


    private fun requestCountBetweenQueryA(model: RequestModel): Possibly<CountBetweenQuery> {
        return try {
            Possibly.ret(CountBetweenQuery(extractLocalDateOrThrow(model, "begin"), extractLocalDateOrThrow(model, "end")))

        } catch (e: UnsupportedTemporalTypeException) {
            Possibly.Fail(Err.DATE_FORMAT_IS_INVALID)

        } catch (e: DateTimeException) {
            Possibly.Fail(Err.DATE_FORMAT_IS_INVALID)
        }
    }

    private fun extractLocalDateOrThrow(it: RequestModel, param: String): LocalDate =
            LocalDate.from(DateTimeFormatter.ISO_DATE.parse(it.parameters.first { it.key == param }.value))

    private fun requestCountBetweenQueryB(me: RequestModel): Possibly<CountBetweenQuery> {
//        Possibly.ret(me)
//        val a = perhapsExtractLocalDate(me, "begin")
//        val b = perhapsExtractLocalDate(me, "end")
//
//        Possibly.ret(::CountBetweenQuery)
//                .map { it(a, b) }
//
//        val map: Possibly<Possibly<Possibly<LocalDate>>> = Possibly.ret(::CountBetweenQuery.curried())
//                .map { function -> Possibly.ret(function).map { perhapsExtractLocalDate(me, "begin") } }
//                .map { function -> Possibly.ret(function).map { perhapsExtractLocalDate(me, "end") } }
//
//
//        val map1: Possibly<Possibly.Vex<Possibly.Vex<(begin: LocalDate) -> (end: LocalDate) -> CountBetweenQuery>>> = Possibly.ret(::CountBetweenQuery.curried())
//                .map { function -> Possibly.ret(function).apply { perhapsExtractLocalDate(me, "begin") } }
//                .map { function -> Possibly.ret(function).apply { perhapsExtractLocalDate(me, "begin") } }

        val newCountBetweenQueryCurried: (begin: LocalDate) -> (end: LocalDate) -> CountBetweenQuery =
                ::CountBetweenQuery.curried()

        return Possibly.ret(newCountBetweenQueryCurried)
                .bind { function -> perhapsExtractLocalDate(me, "begin").map(function) }
                .bind { function -> perhapsExtractLocalDate(me, "end").map(function) }
    }

//    private fun requestCountBetweenQueryB(me: RequestModel): Possibly<CountBetweenQuery> {
////        Possibly.ret { it: RequestModel, param: String -> this.extractLocalDateOrThrow(it, param) }
////                .bind { it(me, "begin") }
//
//        Possibly.ret( me )
//                .map { { it: RequestModel, param: String -> this.extractLocalDateOrThrow(it, param) }.curried() }
//                .map
//
//        Possibly.ret( { it: RequestModel, param: String -> this.extractLocalDateOrThrow(it, param) }.curried() )
//                .bind(me)
//
//        val bind: Possibly<String> = Possibly.ret(::CountBetweenQuery)
//                .map { Possibly.ret { this::extractLocalDateOrThrow } ( this ).map ( it ).map ( "begin" ) }
//                .map { Possibly.ret { this::extractLocalDateOrThrow } (this ).map ( it ).map ( "end" ) }
//
//    }

    private fun perhapsExtractLocalDate(it: RequestModel, param: String): Possibly<LocalDate> = try {
        Possibly.ret(LocalDate.from(DateTimeFormatter.ISO_DATE.parse(it.parameters.first { it.key == param }.value)))

    } catch (e: UnsupportedTemporalTypeException) {
        Possibly.Fail(Err.DATE_FORMAT_IS_INVALID)

    } catch (e: DateTimeException) {
        Possibly.Fail(Err.DATE_FORMAT_IS_INVALID)
    }

    private fun countAsResponse(it: Long): ResponseModel = ResponseModel(200, it.toString())
}