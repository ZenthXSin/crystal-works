package crystalworks.world.blocks.production

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Vec2
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Effect
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.gen.Icon
import mindustry.gen.Sounds
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.gen.Tex
import mindustry.logic.LAccess
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import mindustry.type.PayloadSeq
import mindustry.type.PayloadStack
import mindustry.ui.Bar
import mindustry.ui.Styles
import mindustry.world.Block
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.Payload
import mindustry.world.blocks.payloads.PayloadBlock
import mindustry.world.blocks.payloads.UnitPayload
import mindustry.world.consumers.ConsumeItemDynamic
import mindustry.world.consumers.ConsumeLiquidsDynamic
import mindustry.world.consumers.ConsumePayloadDynamic
import mindustry.world.consumers.ConsumePowerDynamic
import mindustry.world.draw.DrawBlock
import mindustry.world.draw.DrawDefault
import mindustry.world.meta.Stat
import mindustry.world.meta.StatValues
import mindustry.world.meta.BlockFlag
import arc.struct.EnumSet
import arc.util.Eachable
import kotlin.math.max
import kotlin.math.min

/**
 * Multi-recipe crafter with selectable parallel thread count and payload input/output.
 * Verified against Mindustry v155.4/v158 payload APIs: PayloadBlockBuild, PayloadSeq,
 * ConsumeItemDynamic, ConsumeLiquidsDynamic, ConsumePayloadDynamic and ConsumePowerDynamic.
 */
class CrystalMultiCrafter(name: String) : PayloadBlock(name) {
    class Recipe(
        val name: String,
        val icon: mindustry.ctype.UnlockableContent,
        val craftTime: Float,
        val inputItems: Array<ItemStack> = ItemStack.empty,
        val inputLiquids: Array<LiquidStack> = emptyArray(),
        val inputPayloads: Seq<PayloadStack> = Seq(),
        val outputItems: Array<ItemStack> = ItemStack.empty,
        val outputLiquids: Array<LiquidStack> = emptyArray(),
        val outputPayload: Block? = null,
        val powerUse: Float = 0f
    )

    val recipes = Seq<Recipe>()
    var maxThreads = 4
    var craftEffect: Effect = Fx.smeltsmoke
    var updateEffect: Effect = Fx.none
    var updateEffectChance = 0.03f
    var updateEffectSpread = 4f
    var warmupSpeed = 0.03f
    var liquidOutputDirections = intArrayOf(-1)
    var drawer: DrawBlock = DrawDefault()
    var payloadCapacity = 10 // total payload inventory slots

    init {
        update = true
        solid = true
        configurable = true
        saveConfig = true
        clearOnDoubleTap = true
        rotate = true
        hasItems = true
        hasLiquids = true
        hasPower = true
        acceptsPayload = true
        outputsPayload = true
        sync = true
        ambientSound = Sounds.loopMachine
        ambientSoundVolume = 0.04f
        flags = EnumSet.of(BlockFlag.factory)

        config(IntArray::class.java) { build: CrystalMultiCrafterBuild, data: IntArray ->
            if (data.isNotEmpty()) build.setRecipe(data[0])
            if (data.size > 1) build.applyThreads(data[1])
        }
        config(Integer::class.java) { build: CrystalMultiCrafterBuild, value: Integer -> build.setRecipe(value.toInt()) }
        configClear { build: CrystalMultiCrafterBuild -> build.setRecipe(-1) }

        consume(ConsumeItemDynamic { build: CrystalMultiCrafterBuild -> build.recipe()?.inputItems ?: ItemStack.empty })
        consume(ConsumeLiquidsDynamic { build: CrystalMultiCrafterBuild -> build.recipe()?.inputLiquids ?: emptyArray() })
        consume(ConsumePayloadDynamic { build: CrystalMultiCrafterBuild -> build.recipe()?.inputPayloads ?: Seq() })
        consume(ConsumePowerDynamic { build: Building ->
            val b = build as CrystalMultiCrafterBuild
            val recipe = b.recipe()
            if (recipe == null) 0f else recipe.powerUse * b.activeThreads()
        })
    }

    override fun init() {
        outputsLiquid = recipes.any { it.outputLiquids.isNotEmpty() }
        hasLiquids = hasLiquids || outputsLiquid || recipes.any { it.inputLiquids.isNotEmpty() }
        hasItems = hasItems || recipes.any { it.inputItems.isNotEmpty() || it.outputItems.isNotEmpty() }
        hasPower = hasPower || recipes.any { it.powerUse > 0f }
        outputsPayload = recipes.any { it.outputPayload != null }
        super.init()
        // populate liquidFilter after super.init() allocates the array (Block.init() creates new boolean[content.liquids().size])
        for (r in recipes) for (ls in r.inputLiquids) liquidFilter[ls.liquid.id.toInt()] = true
    }

    override fun load() {
        super.load()
        drawer.load(this)
    }

    override fun icons(): Array<TextureRegion> = drawer.finalIcons(this)

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        drawer.drawPlan(this, plan, list)
    }

    override fun drawOverlay(x: Float, y: Float, rotation: Int) {
        if (recipes.any { it.outputPayload != null }) {
            Draw.rect(outRegion, x, y, rotation * 90f)
        }
        recipes.firstOrNull { it.outputLiquids.isNotEmpty() }?.outputLiquids?.forEachIndexed { i, stack ->
            val dir = if (liquidOutputDirections.size > i) liquidOutputDirections[i] else -1
            if (dir != -1) {
                Draw.rect(stack.liquid.fullIcon, x + Geometry.d4x(dir + rotation) * (size * Vars.tilesize / 2f + 4f), y + Geometry.d4y(dir + rotation) * (size * Vars.tilesize / 2f + 4f), 8f, 8f)
            }
        }
    }

    override fun setStats() {
        super.setStats()
        stats.add(Stat.productionTime, "$maxThreads ${CoreBundle.get("stat.crystal-works-threads", "threads")}")
        stats.add(Stat.output) { table ->
            table.left()
            recipes.forEach { recipe ->
                table.table(Styles.grayPanel) { row ->
                    row.left().defaults().pad(3f)
                    row.image(recipe.icon.uiIcon).size(32f)
                    row.add(CoreBundle.get("recipe.$name-${recipe.name}.name", recipe.name)).left().width(110f)
                    row.add("${Mathf.round(recipe.craftTime / 60f)}s").color(Color.lightGray)
                    addStacks(row, recipe.inputItems, recipe.inputLiquids, recipe.inputPayloads, true)
                    row.image(Icon.right).size(24f).padLeft(6f).padRight(6f)
                    addStacks(row, recipe.outputItems, recipe.outputLiquids, if (recipe.outputPayload == null) Seq() else Seq.with(PayloadStack(recipe.outputPayload, 1)), false)
                }.growX().pad(2f)
                table.row()
            }
        }
    }

    private fun addStacks(table: Table, items: Array<ItemStack>, liquids: Array<LiquidStack>, payloads: Seq<PayloadStack>, input: Boolean) {
        table.table { box ->
            box.left()
            var count = 0
            items.forEach { box.add(StatValues.stack(it)).padRight(2f); if (++count % 4 == 0) box.row() }
            liquids.forEach { box.add(StatValues.displayLiquid(it.liquid, it.amount * 60f, true)).padRight(2f); if (++count % 2 == 0) box.row() }
            payloads.forEach { box.add(StatValues.stack(it)).padRight(2f); if (++count % 4 == 0) box.row() }
            if (count == 0) box.add(if (input) "@none" else "-").color(Color.lightGray)
        }.left().minWidth(90f)
    }

    override fun setBars() {
        super.setBars()
        addBar("progress") { e: CrystalMultiCrafterBuild -> Bar({ Core.bundle.get("bar.progress") }, { Pal.ammo }, { e.progress }) }
        addBar("threads") { e: CrystalMultiCrafterBuild -> Bar({ "${CoreBundle.get("bar.crystal-works-threads", "Threads")}: ${e.activeThreads()}/${maxThreads}" }, { Pal.accent }, { e.activeThreads() / maxThreads.toFloat() }) }
        // dynamic liquid bar: shows output liquid of current recipe
        addBar("current-output-liquid") { e: CrystalMultiCrafterBuild ->
            val r = e.recipe()
            if (r == null || r.outputLiquids.isEmpty()) return@addBar null
            val ls = r.outputLiquids[0]
            Bar(
                { ls.liquid.localizedName },
                { ls.liquid.barColor() },
                { e.liquids.get(ls.liquid) / liquidCapacity }
            )
        }
    }

    inner class CrystalMultiCrafterBuild : PayloadBlockBuild<Payload>() {
        var recipeIndex = if (recipes.isEmpty) -1 else 0
        var threadCount = 1
        var progress = 0f
        var totalProgress = 0f
        var warmup = 0f
        var outputPayload: Payload? = null
        val payloadInventory = PayloadSeq()
        val outputVector = Vec2()
        var outputRotation = 0f

        fun recipe(): Recipe? = if (recipeIndex in 0 until recipes.size) recipes[recipeIndex] else null
        fun setRecipe(index: Int) { recipeIndex = if (index in 0 until recipes.size) index else -1; progress = 0f }
        fun applyThreads(value: Int) { threadCount = Mathf.clamp(value, 1, maxThreads) }
        fun activeThreads(): Int = if (enabled && recipe() != null) threadCount else 0

        override fun getPayloads(): PayloadSeq = payloadInventory

        override fun acceptPayload(source: Building, payload: Payload): Boolean {
            if (!super.acceptPayload(source, payload)) return false
            val content = payloadContent(payload) ?: return false
            // check current recipe needs this payload type
            val r = recipe() ?: return false
            if (r.inputPayloads.none { it.item == content }) return false
            // check capacity: don't exceed required amount * maxThreads buffer
            val maxNeeded = r.inputPayloads
                .filter { it.item == content }
                .maxOfOrNull { it.amount * maxThreads * 2 } ?: return false
            return payloadInventory.get(content) < maxNeeded
        }

        override fun handlePayload(source: Building, payload: Payload) {
            super.handlePayload(source, payload)
        }

        override fun shouldConsume(): Boolean {
            val r = recipe() ?: return false
            if (outputPayload != null) return false
            if (!canFitOutputs(r)) return false
            return super.shouldConsume()
        }

        override fun updateTile() {
            super.updateTile()
            moveInputPayloadToInventory()
            val r = recipe()
            if (r != null && efficiency > 0f && outputPayload == null) {
                val scale = activeThreads().coerceAtLeast(1)
                progress += getProgressIncrease(r.craftTime / scale)
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed)

                r.outputLiquids.forEach { out -> handleLiquid(this, out.liquid, min(out.amount * getProgressIncrease(1f) * scale, liquidCapacity - liquids.get(out.liquid))) }

                if (wasVisible && Mathf.chanceDelta(updateEffectChance.toDouble())) {
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread))
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed)
            }

            totalProgress += warmup * Time.delta

            if (r != null && progress >= 1f) craft(r)
            dumpOutputs(r)
            moveOutCraftedPayload()
        }

        private fun moveInputPayloadToInventory() {
            if (payload != null && moveInPayload(false)) {
                payloadContent(payload!!)?.let { payloadInventory.add(it, 1) }
                payload = null
            }
        }

        private fun payloadContent(payload: Payload): mindustry.ctype.UnlockableContent? = when (payload) {
            is BuildPayload -> payload.block()
            is UnitPayload -> payload.unit.type
            else -> null
        }

        private fun canFitOutputs(r: Recipe): Boolean {
            r.outputItems.forEach { if (items.get(it.item) + it.amount > itemCapacity) return false }
            r.outputLiquids.forEach { if (liquids.get(it.liquid) >= liquidCapacity - 0.001f) return false }
            return true
        }

        private fun craft(r: Recipe) {
            consume()
            r.outputItems.forEach { stack -> repeat(stack.amount) { offload(stack.item) } }
            r.outputPayload?.let {
                outputPayload = BuildPayload(it, team)
                outputVector.setZero()
                outputRotation = rotdeg()
                it.placeEffect.at(x, y, it.size * Vars.tilesize.toFloat())
            }
            if (wasVisible) craftEffect.at(x, y)
            progress %= 1f
        }

        private fun dumpOutputs(r: Recipe?) {
            if (r == null) return
            if (timer(timerDump, dumpTime / timeScale)) r.outputItems.forEach { dump(it.item) }
            r.outputLiquids.forEachIndexed { i, stack -> dumpLiquid(stack.liquid, 2f, if (liquidOutputDirections.size > i) liquidOutputDirections[i] else -1) }
        }

        private fun moveOutCraftedPayload() {
            val out = outputPayload ?: return
            out.set(x + outputVector.x, y + outputVector.y, outputRotation)
            val dest = Vec2().trns(rotdeg(), size * Vars.tilesize / 2f)
            outputRotation = Angles.moveToward(outputRotation, rotdeg(), payloadRotateSpeed * delta())
            outputVector.approach(dest, payloadSpeed * delta())
            if (outputVector.within(dest, 0.001f)) {
                outputVector.clamp(-size * Vars.tilesize / 2f, -size * Vars.tilesize / 2f, size * Vars.tilesize / 2f, size * Vars.tilesize / 2f)
                val front = front()
                val canMove = front != null && (front.block.outputsPayload || front.block.acceptsPayload)
                if (canMove && movePayload(out)) outputPayload = null
                else if ((front == null || !front.tile.solid()) && out.dump()) outputPayload = null
            }
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            val r = recipe() ?: return false
            return r.inputItems.any { it.item == item } && items.get(item) < getMaximumAccepted(item)
        }

        override fun getMaximumAccepted(item: Item): Int {
            val r = recipe() ?: return 0
            return max(itemCapacity, (r.inputItems.firstOrNull { it.item == item }?.amount ?: 0) * maxThreads * 2)
        }

        override fun buildConfiguration(table: Table) {
            table.table(Styles.black6) { t ->
                t.left().defaults().size(42f).pad(2f)
                recipes.forEachIndexed { index, r ->
                    val button = t.button(Tex.whiteui, Styles.clearTogglei, 36f) { configure(intArrayOf(index, threadCount)) }.tooltip(r.icon.localizedName).get()
                    button.style.imageUp = TextureRegionDrawable(r.icon.uiIcon)
                    button.update { button.setChecked(recipeIndex == index) }
                    if ((index + 1) % 4 == 0) t.row()
                }
            }.row()
            table.table(Styles.black6) { t ->
                t.left().defaults().size(42f).pad(2f)
                for (i in 1..maxThreads) {
                    val button = t.button("${i}x", Styles.clearTogglet) { configure(intArrayOf(recipeIndex, i)) }.get()
                    button.update { button.setChecked(threadCount == i) }
                }
            }.left()
        }

        override fun display(table: Table) {
            super.display(table)
            table.row()
            table.table { t ->
                t.image(recipe()?.icon?.uiIcon ?: Icon.cancel.region).size(32f).padRight(4f)
                t.label { recipe()?.let { CoreBundle.get("recipe.${name}-${it.name}.name", it.name) } ?: "@none" }.wrap().width(210f).color(Color.lightGray)
                t.row()
                t.add("${CoreBundle.get("bar.crystal-works-threads", "Threads")}: ${activeThreads()}/$maxThreads").color(Color.lightGray).left()
            }.left()
        }

        override fun draw() {
            drawer.draw(this)
            Draw.z(Layer.blockOver)
            drawPayload()
            outputPayload?.let {
                it.set(x + outputVector.x, y + outputVector.y, outputRotation)
                it.draw()
            }
            recipe()?.let {
                Draw.z(Layer.blockOver + 0.1f)
                Draw.color(Pal.accent, warmup)
                Draw.rect(it.icon.uiIcon, x, y, 24f, 24f)
                Draw.color()
            }
        }

        override fun drawSelect() {
            super.drawSelect()
            recipe()?.let { drawItemSelection(it.icon) }
        }

        override fun sense(sensor: LAccess): Double {
            if (sensor == LAccess.progress) return progress().toDouble()
            return super.sense(sensor)
        }

        override fun progress(): Float = Mathf.clamp(progress)
        override fun warmup(): Float = warmup
        override fun totalProgress(): Float = totalProgress
        override fun getPayload(): Payload? = outputPayload ?: payload
        override fun takePayload(): Payload? {
            val out = outputPayload
            if (out != null) {
                outputPayload = null
                return out
            }
            return super.takePayload()
        }
        override fun shouldAmbientSound(): Boolean = efficiency > 0f
        override fun config(): Any = intArrayOf(recipeIndex, threadCount)

        override fun write(write: Writes) {
            super.write(write)
            write.i(recipeIndex)
            write.i(threadCount)
            write.f(progress)
            write.f(warmup)
            payloadInventory.write(write)
            Payload.write(outputPayload, write)
            write.f(outputVector.x)
            write.f(outputVector.y)
            write.f(outputRotation)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            recipeIndex = read.i()
            threadCount = read.i()
            progress = read.f()
            warmup = read.f()
            payloadInventory.read(read)
            outputPayload = Payload.read(read)
            outputVector.set(read.f(), read.f())
            outputRotation = read.f()
        }
    }
}

private object CoreBundle {
    fun get(key: String, fallback: String): String = Core.bundle.get(key, fallback)
}
