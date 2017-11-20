package eu.ha3.katastrophe.run_length_encoding

import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList

/**
 * (Default template)
 * Created on 2017-11-20
 *
 * @author Ha3
 */
abstract class AbstractRunLengthEncodingTest {
    abstract fun rle(): RunLengthEncoding<Int>

    @Test
    fun empty_list() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf<Int>()),
                    Is.`is`(Item.of()))

    @Test
    fun single_element() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1)),
                    Is.`is`(Item.of(Obj(1))))

    @Test
    fun two_distinct_elements() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 2)),
                    Is.`is`(Item.of(Obj(1), Obj(2))))

    @Test
    fun three_distinct_elements() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 2, 3)),
                    Is.`is`(Item.of(Obj(1), Obj(2), Obj(3))))

    @Test
    fun four_distinct_elements() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 2, 3, 4)),
                    Is.`is`(Item.of(Obj(1), Obj(2), Obj(3), Obj(4))))

    @Test
    fun two_identical_elements() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 1)),
                    Is.`is`(Item.of(Run(Obj(1), 2))))

    @Test
    fun three_identical_elements() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 1, 1)),
                    Is.`is`(Item.of(Run(Obj(1), 3))))

    @Test
    fun two_identical_elements_with_single_at_end() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 1, 2)),
                    Is.`is`(Item.of(Run(Obj(1), 2), Obj(2))))

    @Test
    fun two_groups_of_identical_elements() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 1, 2, 2)),
                    Is.`is`(Item.of(Run(Obj(1), 2), Run(Obj(2), 2))))

    @Test
    fun two_groups_of_identical_elements_separated_by_single_element() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(1, 1, 5, 2, 2)),
                    Is.`is`(Item.of(Run(Obj(1), 2), Obj(5), Run(Obj(2), 2))))

    @Test
    fun two_identical_elements_with_single_at_beginning() =
            MatcherAssert.assertThat(rle().runLengthEncodingOf(listOf(5, 1, 1)),
                    Is.`is`(Item.of(Obj(5), Run(Obj(1), 2))))

    @Test
    fun runLengthEncoding_should_not_StackOverflow_on_large_inputs() {
        val random = Random(0) // Seeded in purpose
        println(rle().runLengthEncodingOf(IntStream.range(0, 20_000).map { random.nextInt(4) }.toList()))
    }
}
