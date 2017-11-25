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

    internal fun action(request: RequestModel): Perhaps<ResponseModel> = Perhaps.ret(request)
            .bind(this::asCountBetweenQuery)
            .bind(useCase::getCountBetween)
            .map(this::countAsResponse)

    private fun asCountBetweenQuery(it: RequestModel): Perhaps<CountBetweenQuery> = Perhaps.ret(it)
            .verify(this::validateRequestForCountBetweenQuery)
            .bind(this::requestCountBetweenQueryB)

    private fun validateRequestForCountBetweenQuery(it: RequestModel): Err? = when {
        !it.parameters.any { it.key == "begin" } -> Err.PARAMETER_MISSING_BEGIN_DATE
        !it.parameters.any { it.key == "end" } -> Err.PARAMETER_MISSING_END_DATE
        else -> null
    }


    private fun requestCountBetweenQueryA(model: RequestModel): Perhaps<CountBetweenQuery> {
        return try {
            Perhaps.ret(CountBetweenQuery(extractLocalDateOrThrow(model, "begin"), extractLocalDateOrThrow(model, "end")))

        } catch (e: UnsupportedTemporalTypeException) {
            Perhaps.Fail(Err.DATE_FORMAT_IS_INVALID)

        } catch (e: DateTimeException) {
            Perhaps.Fail(Err.DATE_FORMAT_IS_INVALID)
        }
    }

    private fun extractLocalDateOrThrow(it: RequestModel, param: String): LocalDate =
            LocalDate.from(DateTimeFormatter.ISO_DATE.parse(it.parameters.first { it.key == param }.value))

    private fun requestCountBetweenQueryB(me: RequestModel): Perhaps<CountBetweenQuery> {
//        Perhaps.ret(me)
//        val a = perhapsExtractLocalDate(me, "begin")
//        val b = perhapsExtractLocalDate(me, "end")
//
//        Perhaps.ret(::CountBetweenQuery)
//                .map { it(a, b) }
//
//        val map: Perhaps<Perhaps<Perhaps<LocalDate>>> = Perhaps.ret(::CountBetweenQuery.curried())
//                .map { function -> Perhaps.ret(function).map { perhapsExtractLocalDate(me, "begin") } }
//                .map { function -> Perhaps.ret(function).map { perhapsExtractLocalDate(me, "end") } }
//
//
//        val map1: Perhaps<Perhaps.Vex<Perhaps.Vex<(begin: LocalDate) -> (end: LocalDate) -> CountBetweenQuery>>> = Perhaps.ret(::CountBetweenQuery.curried())
//                .map { function -> Perhaps.ret(function).apply { perhapsExtractLocalDate(me, "begin") } }
//                .map { function -> Perhaps.ret(function).apply { perhapsExtractLocalDate(me, "begin") } }

        val newCountBetweenQueryCurried: (begin: LocalDate) -> (end: LocalDate) -> CountBetweenQuery =
                ::CountBetweenQuery.curried()

        return Perhaps.ret(newCountBetweenQueryCurried)
                .bind { function -> perhapsExtractLocalDate(me, "begin").map(function) }
                .bind { function -> perhapsExtractLocalDate(me, "end").map(function) }
    }

//    private fun requestCountBetweenQueryB(me: RequestModel): Perhaps<CountBetweenQuery> {
////        Perhaps.ret { it: RequestModel, param: String -> this.extractLocalDateOrThrow(it, param) }
////                .bind { it(me, "begin") }
//
//        Perhaps.ret( me )
//                .map { { it: RequestModel, param: String -> this.extractLocalDateOrThrow(it, param) }.curried() }
//                .map
//
//        Perhaps.ret( { it: RequestModel, param: String -> this.extractLocalDateOrThrow(it, param) }.curried() )
//                .bind(me)
//
//        val bind: Perhaps<String> = Perhaps.ret(::CountBetweenQuery)
//                .map { Perhaps.ret { this::extractLocalDateOrThrow } ( this ).map ( it ).map ( "begin" ) }
//                .map { Perhaps.ret { this::extractLocalDateOrThrow } (this ).map ( it ).map ( "end" ) }
//
//    }

    private fun perhapsExtractLocalDate(it: RequestModel, param: String): Perhaps<LocalDate> = try {
        Perhaps.ret(LocalDate.from(DateTimeFormatter.ISO_DATE.parse(it.parameters.first { it.key == param }.value)))

    } catch (e: UnsupportedTemporalTypeException) {
        Perhaps.Fail(Err.DATE_FORMAT_IS_INVALID)

    } catch (e: DateTimeException) {
        Perhaps.Fail(Err.DATE_FORMAT_IS_INVALID)
    }

    private fun countAsResponse(it: Long): ResponseModel = ResponseModel(200, it.toString())
}