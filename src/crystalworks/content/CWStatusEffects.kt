package crystalworks.content

import arc.graphics.Color
import mindustry.type.StatusEffect

object CWStatusEffects {
    lateinit var crystalShock: StatusEffect

    fun load() {
        crystalShock = StatusEffect("crystal-shock").apply {
            color = Color.valueOf("65C3E8")
            damageMultiplier = 0.7f
            speedMultiplier = 0.6f
            damage = 0.5f
            effectChance = 0.3f
            permanent = false
        }
    }
}
