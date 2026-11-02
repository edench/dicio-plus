package org.stypox.dicio.skills.flight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.Body
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString

sealed interface FlightOutput : SkillOutput {
    data class Position(
        val altitudeFeet: Int?,
        val speedKnots: Int?,
        val lat: Double,
        val lon: Double,
    )

    data class Found(
        val code: String,
        val airline: String,
        val from: String,
        val to: String,
        val position: Position?,
    ) : FlightOutput {
        override fun getSpeechOutput(ctx: SkillContext): String {
            val route = ctx.getString(R.string.skill_flight_route, airline.ifEmpty { code }, from, to)
            val status = when {
                position == null -> ctx.getString(R.string.skill_flight_not_tracked)
                position.altitudeFeet == null || position.altitudeFeet < 100 ->
                    ctx.getString(R.string.skill_flight_on_ground)
                else -> ctx.getString(
                    R.string.skill_flight_airborne,
                    position.altitudeFeet, position.speedKnots ?: 0
                )
            }
            return "$route $status"
        }

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Headline(text = "$code · $from → $to")
                Body(text = getSpeechOutput(ctx))
                if (position != null) {
                    Body(text = "%.3f, %.3f".format(position.lat, position.lon))
                }
            }
        }
    }

    data class NotUnderstood(val spoken: String) : FlightOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_flight_not_understood, spoken)
    }

    data class NotFound(val code: String) : FlightOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_flight_not_found, code)
    }

    data object NetworkError : FlightOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_flight_network_error)
    }
}
