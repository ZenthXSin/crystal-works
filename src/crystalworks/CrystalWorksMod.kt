package crystalworks

import arc.util.Log
import crystalworks.content.CWBlocks
import crystalworks.content.CWItems
import crystalworks.content.CWLiquids
import crystalworks.content.CWStatusEffects
import mindustry.mod.Mod
import mindustry.content.TechTree
import mindustry.content.Blocks
import mindustry.game.Objectives
import arc.struct.Seq

class CrystalWorksMod : Mod() {

    override fun init() {
        super.init()
        Log.info("[Crystal Works] Mod initialized")
        loadTechTree()
    }

    override fun loadContent() {
        Log.info("[Crystal Works] Loading Kotlin content")
        CWItems.load()
        CWLiquids.load()
        CWStatusEffects.load()
        CWBlocks.load()
    }
}


private fun loadTechTree() {
    val parent = TechTree.all.find { it.content == CWBlocks.crystalFluidMixer } ?: return
    val node = TechTree.TechNode(parent, CWBlocks.crystalAssembler, CWBlocks.crystalAssembler.researchRequirements())
    node.objectives.addAll(Seq.with(Objectives.Produce(CWItems.crystalAlloy), Objectives.Research(Blocks.payloadConveyor)))
}
