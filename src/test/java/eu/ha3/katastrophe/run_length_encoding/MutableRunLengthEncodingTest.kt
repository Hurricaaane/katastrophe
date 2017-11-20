package eu.ha3.katastrophe.run_length_encoding

/**
 * (Default template)
 * Created on 2017-11-20
 *
 * @author Ha3
 */
internal class MutableRunLengthEncodingTest: AbstractRunLengthEncodingTest() {
    override fun rle(): RunLengthEncoding<Int> = MutableRunLengthEncoding()

    class MutableRunLengthEncoding<T>: RunLengthEncoding<T> {
        override fun runLengthEncodingOf(raws: List<T>): List<Item<T>> {
            val items: MutableList<Item<T>> = raws.map { Obj(it) }.toMutableList()
            encode(items)

            return items.toList()
        }

        private fun <T> encode(items: MutableList<Item<T>>) {
            if (items.size < 2) {
                return
            }

            val lhs = items[0] // as (Run<T> | Obj)
            val rhs = items[1] as Obj

            return when {
                lhs is Run<T> && lhs.elt == rhs -> combineAndEncode(items, lhs.increment())
                lhs is Obj && lhs == rhs -> combineAndEncode(items, Run(lhs, 2))
                else -> encode(items.subList(1, items.size))
            }
        }

        private fun <T> combineAndEncode(items: MutableList<Item<T>>, replacement: Item<T>) {
            items[0] = replacement
            items.removeAt(1)
            encode(items)
        }
    }
}