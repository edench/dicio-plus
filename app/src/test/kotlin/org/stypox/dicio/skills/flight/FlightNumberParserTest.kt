package org.stypox.dicio.skills.flight

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FlightNumberParserTest : StringSpec({
    "digits and letters" {
        FlightNumberParser.parse("UA 123") shouldBe "UA123"
        FlightNumberParser.parse("ua123") shouldBe "UA123"
        FlightNumberParser.parse("UAL 123") shouldBe "UAL123"
    }
    "spoken letters and digit words" {
        FlightNumberParser.parse("u a one two three") shouldBe "UA123"
        FlightNumberParser.parse("b a two oh nine") shouldBe "BA209"
    }
    "airline names and grouped numbers" {
        FlightNumberParser.parse("united one twenty three") shouldBe "UA123"
        FlightNumberParser.parse("delta twelve thirty four") shouldBe "DL1234"
        FlightNumberParser.parse("jet blue flight 7") shouldBe "B67"
        FlightNumberParser.parse("american airlines one hundred") shouldBe "AA100"
        FlightNumberParser.parse("air canada two thousand five") shouldBe "AC2005"
        FlightNumberParser.parse("virgin atlantic twenty") shouldBe "VS20"
    }
    "not a flight number" {
        FlightNumberParser.parse("") shouldBe null
        FlightNumberParser.parse("to the moon") shouldBe null
        FlightNumberParser.parse("u a") shouldBe null
        FlightNumberParser.parse("united 123456") shouldBe null
    }
})
