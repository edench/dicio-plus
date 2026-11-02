package org.stypox.dicio.skills.unit_conversion

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object UnitConversionInfo : SkillInfo("unit_conversion") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_unit_conversion)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_unit_conversion)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.SwapHoriz)


    override fun build(ctx: SkillContext): Skill<*>? {
        if (!(Sentences.UnitConversion[ctx.sentencesLanguage] != null && ctx.parserFormatter != null)) return null
        return UnitConversionSkill(
            UnitConversionInfo,
            Sentences.UnitConversion[ctx.sentencesLanguage]!!
        )
    }
}
