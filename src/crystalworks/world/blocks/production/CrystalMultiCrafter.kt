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
 * v2 — supports multi-recipe selection (checkboxes), manual consumption in craft(),
 *        vertical stat layout, payload inventory bar.
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

        // Config: intArrayOf(recipeBitmask, threadCount)
        config(IntArray::class.java) { build: CrystalMultiCrafterBuild, data: IntArray ->
            if (data.size > 0) build.recipeMask = data[0]
            if (data.size > 1) build.applyThreads(data[1])
        }
        configClear { build: CrystalMultiCrafterBuild -> build.recipeMask = 0 }

        // Manual consumption in craft() — only PowerConsumer remains for display
        consume(ConsumePowerDynamic { build: Building ->
            val b = build as CrystalMultiCrafterBuild
            var total = 0f
            for (i in 0 until recipes.size) {
                if (b.recipeEnabled(i)) total += recipes[i].powerUse
            }
            total * b.activeThreads().coerceAtLeast(1)
        })
    }

    override fun init() {
        outputsLiquid = recipes.any { it.outputLiquids.isNotEmpty() }
        hasLiquids = hasLiquids || outputsLiquid || recipes.any { it.inputLiquids.isNotEmpty() }
        hasItems = hasItems || recipes.any { it.inputItems.isNotEmpty() || it.outputItems.isNotEmpty() }
        hasPower = hasPower || recipes.any { it.powerUse > 0f }
        outputsPayload = recipes.any { it.outputPayload != null }
        super.init()
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
                val recipeName = CoreBundle.get("recipe.$name-${recipe.name}.name", recipe.name)
                table.table(Styles.grayPanel) { t ->
                    t.left().defaults().pad(3f)
                    // Top row: icon + name + time
                    t.table { top ->
                        top.left()
                        top.image(recipe.icon.uiIcon).size(28f).padRight(4f)
                        top.add(recipeName).left().width(120f).color(Color.white)
                        top.add("${Mathf.round(recipe.craftTime / 60f)}s").color(Color.lightGray).padLeft(6f)
                    }.growX().row()
                    // Bottom row: inputs → outputs
                    t.table { bottom ->
                        bottom.left()
                        addStacks(bottom, recipe.inputItems, recipe.inputLiquids, recipe.inputPayloads, true)
                        bottom.image(Icon.right).size(20f).padLeft(4f).padRight(4f)
                        addStacks(bottom, recipe.outputItems, recipe.outputLiquids,
                            if (recipe.outputPayload == null) Seq() else Seq.with(PayloadStack(recipe.outputPayload, 1)),
                            false)
                    }
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
            if (count == 0) box.add("None").color(Color.lightGray)
        }.left().minWidth(80f)
    }

    override fun setBars() {
        super.setBars()
        addBar("progress") { e: CrystalMultiCrafterBuild ->
            val progress = e.totalProgressFraction()
            Bar({ Core.bundle.get("bar.progress") }, { Pal.ammo }, { progress })
        }
        addBar("threads") { e: CrystalMultiCrafterBuild ->
            Bar({ "${CoreBundle.get("bar.crystal-works-threads", "Threads")}: ${e.activeThreads()}/${maxThreads}" }, { Pal.accent }, { e.activeThreads() / maxThreads.toFloat() })
        }
        // Dynamic liquid bar: shows output liquid of current/any active recipe
        addBar("current-output-liquid") { e: CrystalMultiCrafterBuild ->
            val activeLiquids = recipes.filterIndexed { i, _ -> e.recipeEnabled(i) }
                .flatMap { it.outputLiquids.map { ls -> ls.liquid } }.distinct()
            if (activeLiquids.isEmpty()) return@addBar null
            // show the first output liquid across active recipes
            val liq = activeLiquids.first()
            Bar({ liq.localizedName }, { liq.barColor() }, { e.liquids.get(liq) / liquidCapacity })
        }
        // Payload inventory bar
        addBar("payloads") { e: CrystalMultiCrafterBuild ->
            val total = e.payloadInventory.total()
            if (total <= 0) return@addBar null
            Bar(
                { "${CoreBundle.get("bar.crystal-works-payload-inventory", "Payloads")}: $total" },
                { Pal.items },
                { total.toFloat() / payloadCapacity }
            )
        }
    }

    inner class CrystalMultiCrafterBuild : PayloadBlockBuild<Payload>() {
        /** Bitmask: bit i = recipe i is enabled */
        var recipeMask = if (recipes.isEmpty) 0 else 1
        var threadCount = 1
        /** Per-recipe progress. Array size = recipes.size, set in init after recipes defined */
        var recipeProgress = FloatArray(0)
        var totalProgress = 0f
        var warmup = 0f
        var outputPayload: Payload? = null
        val payloadInventory = PayloadSeq()
        val outputVector = Vec2()
        var outputRotation = 0f

        /** Ensure recipeProgress is sized correctly after recipes are registered */
        private fun ensureProgressArray() {
            if (recipeProgress.size != recipes.size) recipeProgress = FloatArray(recipes.size)
        }

        fun recipeEnabled(index: Int): Boolean = index in 0 until recipes.size && (recipeMask and (1 shl index)) != 0

        fun activeRecipeCount(): Int = recipeMask.countOneBits()

        fun activeThreads(): Int = if (enabled && activeRecipeCount() > 0) threadCount else 0

        fun applyThreads(value: Int) { threadCount = Mathf.clamp(value, 1, maxThreads) }

        fun toggleRecipe(index: Int) {
            ensureProgressArray()
            recipeMask = recipeMask xor (1 shl index)
        }

        /** Fraction of total work done across active recipes, normalized to [0,1] */
        fun totalProgressFraction(): Float {
            val count = activeRecipeCount()
            if (count == 0) return 0f
            var sum = 0f
            for (i in 0 until recipes.size) if (recipeEnabled(i)) sum += recipeProgress[i]
            return Mathf.clamp(sum / count)
        }

        override fun getPayloads(): PayloadSeq = payloadInventory

        override fun acceptPayload(source: Building, payload: Payload): Boolean {
            if (!super.acceptPayload(source, payload)) return false
            val content = payloadContent(payload) ?: return false
            // Check if ANY active recipe needs this payload type
            var anyNeeds = false
            var maxNeeded = 0
            for (i in 0 until recipes.size) {
                if (!recipeEnabled(i)) continue
                val r = recipes[i]
                for (ps in r.inputPayloads) {
                    if (ps.item == content) {
                        anyNeeds = true
                        maxNeeded = max(maxNeeded, ps.amount * maxThreads * 2)
                    }
                }
            }
            if (!anyNeeds) return false
            return payloadInventory.get(content) < maxNeeded
        }

        override fun shouldConsume(): Boolean {
            if (activeRecipeCount() == 0 || outputPayload != null) return false
            // Manual consumption in craft(), but must return true to drive update
            return super.shouldConsume()
        }

        override fun updateTile() {
            ensureProgressArray()
            super.updateTile()
            moveInputPayloadToInventory()
            var anyWorking = false
            var activeCount = activeRecipeCount()

            if (enabled && activeCount > 0 && efficiency > 0f && outputPayload == null) {
                // Distribute maxThreads across active recipes: each gets 1+n bonus threads
                val basePerRecipe = max(1, maxThreads / activeCount)
                for (i in 0 until recipes.size) {
                    if (!recipeEnabled(i)) continue
                    val r = recipes[i]
                    if (!canFitOutputs(r)) continue

                    if (!hasInputs(r)) continue
                    val speed = basePerRecipe
                    recipeProgress[i] += getProgressIncrease(r.craftTime / speed)
                    anyWorking = true

                    // Continuous liquid output during crafting (recipe-specific)
                    r.outputLiquids.forEach { out ->
                        handleLiquid(this, out.liquid,
                            min(out.amount * getProgressIncrease(1f) * speed,
                                liquidCapacity - liquids.get(out.liquid)))
                    }
                }
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed)

                if (wasVisible && Mathf.chanceDelta(updateEffectChance.toDouble())) {
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread))
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed)
            }

            totalProgress += warmup * Time.delta

            // Craft finished recipes
            for (i in 0 until recipes.size) {
                if (recipeEnabled(i) && recipeProgress[i] >= 1f) craft(i)
            }

            // Dump outputs for all active recipes (gated by internal timer)
            for (i in 0 until recipes.size) if (recipeEnabled(i)) dumpOutputs(recipes[i])

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
            // For payload: check if another recipe is currently outputting a payload
            if (r.outputPayload != null && outputPayload != null) return false
            return true
        }

        /** Returns true if we have enough inputs for this recipe (all input types satisfied) */
        private fun hasInputs(r: Recipe): Boolean {
            r.inputItems.forEach { if (items.get(it.item) < it.amount) return false }
            r.inputLiquids.forEach { if (liquids.get(it.liquid) < it.amount) return false }
            r.inputPayloads.forEach { if (payloadInventory.get(it.item) < it.amount) return false }
            return true
        }

        private fun craft(index: Int) {
            val r = recipes[index]
            // Manual consumption: remove inputs
            r.inputItems.forEach { stack -> items.remove(stack.item, stack.amount) }
            r.inputLiquids.forEach { stack -> liquids.remove(stack.liquid, stack.amount) }
            r.inputPayloads.forEach { stack -> payloadInventory.remove(stack.item, stack.amount) }

            // Produce outputs
            r.outputItems.forEach { stack -> repeat(stack.amount) { offload(stack.item) } }
            r.outputPayload?.let {
                outputPayload = BuildPayload(it, team)
                outputVector.setZero()
                outputRotation = rotdeg()
                it.placeEffect.at(x, y, it.size * Vars.tilesize.toFloat())
            }
            if (wasVisible) craftEffect.at(x, y)
            recipeProgress[index] %= 1f
        }

        private fun dumpOutputs(r: Recipe) {
            if (timer(timerDump, dumpTime / timeScale)) r.outputItems.forEach { dump(it.item) }
            r.outputLiquids.forEachIndexed { i, stack ->
                dumpLiquid(stack.liquid, 2f, if (liquidOutputDirections.size > i) liquidOutputDirections[i] else -1)
            }
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
            // Accept item if any active recipe needs it
            for (i in 0 until recipes.size) {
                if (!recipeEnabled(i)) continue
                val r = recipes[i]
                if (r.inputItems.any { it.item == item } && items.get(item) < getMaximumAccepted(item)) return true
            }
            return false
        }

        override fun getMaximumAccepted(item: Item): Int {
            var maxNeed = 0
            for (i in 0 until recipes.size) {
                if (!recipeEnabled(i)) continue
                recipes[i].inputItems.firstOrNull { it.item == item }?.let {
                    maxNeed = max(maxNeed, it.amount * maxThreads * 2)
                }
            }
            return max(itemCapacity, maxNeed)
        }

        override fun buildConfiguration(table: Table) {
            ensureProgressArray()
            table.table(Styles.black6) { t ->
                t.left().defaults().size(42f).pad(2f)
                recipes.forEachIndexed { index, r ->
                    val button = t.button(Tex.whiteui, Styles.clearTogglei, 36f) {
                        // Toggle recipe on/off
                        configure(intArrayOf(recipeMask xor (1 shl index), threadCount))
                    }.tooltip(r.icon.localizedName).get()
                    button.style.imageUp = TextureRegionDrawable(r.icon.uiIcon)
                    button.update { button.setChecked(recipeEnabled(index)) }
                    if ((index + 1) % 4 == 0) t.row()
                }
            }.row()
            table.table(Styles.black6) { t ->
                t.left().defaults().size(42f).pad(2f)
                for (i in 1..maxThreads) {
                    val button = t.button("${i}x", Styles.clearTogglet) { configure(intArrayOf(recipeMask, i)) }.get()
                    button.update { button.setChecked(threadCount == i) }
                }
            }.left()
        }

        override fun display(table: Table) {
            super.display(table)
            table.row()
            table.table { t ->
                val active = (0 until recipes.size).filter { recipeEnabled(it) }
                if (active.isEmpty()) {
                    t.image(Icon.cancel).size(32f).padRight(4f)
                    t.add("None").color(Color.lightGray)
                } else if (active.size == 1) {
                    val r = recipes[active[0]]
                    t.image(r.icon.uiIcon).size(32f).padRight(4f)
                    t.label { CoreBundle.get("recipe.${name}-${r.name}.name", r.name) }.wrap().width(210f).color(Color.lightGray)
                } else {
                    val r = recipes[active[0]]
                    t.image(r.icon.uiIcon).size(32f).padRight(4f)
                    t.add("${active.size} ${CoreBundle.get("bar.crystal-works-recipes", "recipes")}").color(Color.lightGray)
                }
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
            // Draw icon of the most-recently-active recipe (for visual cue)
            val active = (0 until recipes.size).filter { recipeEnabled(it) }
            if (active.isNotEmpty()) {
                val idx = active.minByOrNull { recipeProgress[it] } ?: active.first()
                val r = recipes[idx]
                Draw.z(Layer.blockOver + 0.1f)
                Draw.color(Pal.accent, warmup)
                Draw.rect(r.icon.uiIcon, x, y, 24f, 24f)
                Draw.color()
            }
        }

        override fun drawSelect() {
            super.drawSelect()
            // Show first active recipe icon
            val idx = (0 until recipes.size).firstOrNull { recipeEnabled(it) }
            idx?.let { drawItemSelection(recipes[it].icon) }
        }

        override fun sense(sensor: LAccess): Double {
            if (sensor == LAccess.progress) return totalProgressFraction().toDouble()
            return super.sense(sensor)
        }

        override fun progress(): Float = totalProgressFraction()
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
        override fun shouldAmbientSound(): Boolean = efficiency > 0f && activeRecipeCount() > 0
        override fun config(): Any = intArrayOf(recipeMask, threadCount)

        override fun write(write: Writes) {
            super.write(write)
            write.i(recipeMask)
            write.i(threadCount)
            for (f in recipeProgress) write.f(f)
            write.f(totalProgress)
            write.f(warmup)
            payloadInventory.write(write)
            Payload.write(outputPayload, write)
            write.f(outputVector.x)
            write.f(outputVector.y)
            write.f(outputRotation)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            ensureProgressArray()
            recipeMask = read.i()
            threadCount = read.i()
            for (i in recipeProgress.indices) recipeProgress[i] = read.f()
            totalProgress = read.f()
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