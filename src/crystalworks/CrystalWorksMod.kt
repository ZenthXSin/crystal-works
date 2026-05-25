package crystalworks

import arc.util.Log
import crystalworks.content.CWBlocks
import crystalworks.content.CWItems
import crystalworks.content.CWLiquids
import crystalworks.content.CWStatusEffects
import mindustry.mod.Mod

class CrystalWorksMod : Mod() {

    override fun init() {
        super.init()
        Log.info("[Crystal Works] Mod initialized")
    }

    override fun loadContent() {
        Log.info("[Crystal Works] Loading Kotlin content")
        CWItems.load()
        CWLiquids.load()
        CWStatusEffects.load()
        CWBlocks.load()
    }
}
