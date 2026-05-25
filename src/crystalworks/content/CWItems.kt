package crystalworks.content

import arc.graphics.Color
import mindustry.type.Item

object CWItems {
    lateinit var crystal: Item
    lateinit var crystalAlloy: Item

    fun load() {
        crystal = Item("crystal", Color.valueOf("65C3E8")).apply {
            hardness = 4
            explosiveness = 0.1f
            flammability = 0f
            radioactivity = 0f
            charge = 0.6f
        }

        crystalAlloy = Item("crystal-alloy", Color.valueOf("5D8AA8")).apply {
            hardness = 1
            explosiveness = 0f
            flammability = 0.2f
            radioactivity = 0f
            charge = 0.3f
        }
    }
}
