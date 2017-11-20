package eu.ha3.katastrophe.run_length_encoding

/**
 * (Default template)
 * Created on 2017-11-20
 *
 * @author Ha3
 */
internal class ImmutableRunLengthEncodingTest: AbstractRunLengthEncodingTest() {
    override fun rle(): RunLengthEncoding<Int> = ImmutableRunLengthEncoding()

    class ImmutableRunLengthEncoding<T>: RunLengthEncoding<T> {
        override fun runLengthEncodingOf(raws: List<T>): List<Item<T>> = encode(raws.map { Obj(it) })

        private fun <T> encode(items: List<Item<T>>): List<Item<T>> {
            val lhs: Item<T> by lazy { items[0] }
            val rhs: Obj<T> by lazy { items[1] as Obj }

            return when {
                items.isEmpty() -> Item.of()
                items.size == 1 -> items
                lhs is Run<T> && lhs.elt == rhs -> combineAndEncode(lhs.increment(), items)
                lhs is Obj && lhs == rhs -> combineAndEncode(Run(lhs, 2), items)
                else -> Item.of(lhs) + encode(items.subList(1, items.size))
            }
        }

        private fun <T> combineAndEncode(replacement: Item<T>, items: List<Item<T>>): List<Item<T>> = encode(listOf(replacement) + items.subList(2, items.size))
    }
}