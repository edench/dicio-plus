package org.stypox.dicio.skills.flight

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

data object FlightInfo : SkillInfo("flight") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_flight)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_flight)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Flight)

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.Flight[ctx.sentencesLanguage] ?: return null
        return FlightSkill(FlightInfo, data)
    }
}
