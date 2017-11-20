package eu.ha3.katastrophe.run_length_encoding

/**
 * (Default template)
 * Created on 2017-11-20
 *
 * @author Ha3
 */
internal class FunctionalRunLengthEncodingTest: AbstractRunLengthEncodingTest() {
    override fun rle(): RunLengthEncoding<Int> = FunctionalRunLengthEncoding()

    class FunctionalRunLengthEncoding<T>: RunLengthEncoding<T> {
        override fun runLengthEncodingOf(raws: List<T>): List<Item<T>> = raws
                .fold(Item.of()) { acc, obj -> encodingFold(acc, obj) }

        private fun <T> encodingFold(acc: List<Item<T>>, rhs: T): List<Item<T>> {
            val lhs: Item<T> by lazy { acc.last() }

            return when {
                acc.isEmpty() -> acc + Obj(rhs)
                lhs is Run<T> && lhs.elt.raw == rhs -> acc.dropLast(1) + lhs.increment()
                lhs is Obj && lhs.raw == rhs -> acc.dropLast(1) + Run(lhs, 2)
                else -> acc + Obj(rhs)

            } as MutableList<Item<T>>
        }
    }
}