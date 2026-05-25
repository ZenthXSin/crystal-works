package crystalworks.content

import arc.graphics.Color
import mindustry.type.Liquid

object CWLiquids {
    lateinit var crystalFluid: Liquid

    fun load() {
        crystalFluid = Liquid("crystal-works-crystal-fluid", Color.valueOf("4A90D9")).apply {
            heatCapacity = 1.5f
            viscosity = 0.3f
            flammability = 0.05f
            explosiveness = 0.2f
        }
    }
}
