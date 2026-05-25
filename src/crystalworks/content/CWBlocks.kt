package crystalworks.content

import arc.graphics.Color
import mindustry.content.Fx
import mindustry.gen.Sounds
import mindustry.content.Items
import mindustry.content.Liquids
import mindustry.content.StatusEffects
import mindustry.entities.bullet.BasicBulletType
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import mindustry.world.Block
import mindustry.world.blocks.defense.Wall
import mindustry.world.blocks.defense.turrets.ItemTurret
import mindustry.world.blocks.defense.turrets.LiquidTurret
import mindustry.world.blocks.environment.OreBlock
import mindustry.world.blocks.liquid.Conduit
import mindustry.world.blocks.liquid.LiquidBridge
import mindustry.world.blocks.liquid.LiquidJunction
import mindustry.world.blocks.power.Battery
import mindustry.world.blocks.power.BeamNode
import mindustry.world.blocks.power.ConsumeGenerator
import mindustry.world.blocks.power.PowerNode
import mindustry.world.blocks.production.BurstDrill
import mindustry.world.blocks.production.Drill
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.draw.DrawCrucibleFlame
import mindustry.world.draw.DrawDefault
import mindustry.world.draw.DrawGlowRegion
import mindustry.world.draw.DrawLiquidTile
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawPower

object CWBlocks {
    lateinit var crystalOre: Block
    lateinit var crystalDrill: Block
    lateinit var crystalBorer: Block
    lateinit var crystalSmelter: Block
    lateinit var crystalFluidMixer: Block
    lateinit var crystalBattery: Block
    lateinit var crystalGenerator: Block
    lateinit var crystalPowerNode: Block
    lateinit var crystalBeamTower: Block
    lateinit var crystalConduit: Block
    lateinit var crystalBridgeConduit: Block
    lateinit var crystalLiquidJunction: Block
    lateinit var crystalTurret: Block
    lateinit var crystalFluidTurret: Block
    lateinit var crystalWall: Block
    lateinit var crystalWallLarge: Block

    fun load() {
        crystalOre = OreBlock("crystal-ore").apply {
            itemDrop = CWItems.crystal
            mapColor = Color.valueOf("65C3E8FF")
            useColor = true
            oreDefault = true
            oreThreshold = 0.82f
            oreScale = 30f
        }

        crystalDrill = Drill("crystal-drill").apply {
            size = 3
            health = 540
            tier = 5
            drillTime = 200f
            hardnessDrillMultiplier = 8f
            liquidBoostIntensity = 2.0f
            itemCapacity = 30
            hasLiquids = true
            drawMineItem = true
            alwaysUnlocked = false
            consumePower(2.5f)
            consumeLiquid(Liquids.water, 0.3f).boost()
            requirements(Category.production, ItemStack.with(Items.copper, 80, Items.lead, 60, CWItems.crystal, 30))
        }

        crystalBorer = BurstDrill("crystal-borer").apply {
            size = 4
            health = 800
            tier = 6
            drillTime = 240f
            rotateSpeed = -0.6f
            warmupSpeed = 0.3f
            liquidBoostIntensity = 2.2f
            liquidCapacity = 60f
            itemCapacity = 50
            drawRim = true
            ambientSoundVolume = 0.08f
            consumePower(4.5f)
            consumeLiquid(Liquids.water, 0.5f).boost()
            requirements(Category.production, ItemStack.with(Items.copper, 200, Items.lead, 150, CWItems.crystal, 100, Items.graphite, 60))
        }

        crystalSmelter = GenericCrafter("crystal-smelter").apply {
            size = 3
            health = 440
            craftTime = 100f
            outputItem = ItemStack(CWItems.crystalAlloy, 2)
            drawer = DrawMulti(
                DrawDefault(),
                DrawCrucibleFlame(),
                DrawGlowRegion().apply {
                    color = Color.valueOf("4A8EC7")
                    alpha = 0.6f
                }
            )
            consumeItems(*ItemStack.with(CWItems.crystal, 4, Items.lead, 6))
            consumePower(2.4f)
            requirements(Category.crafting, ItemStack.with(Items.copper, 120, Items.lead, 80, Items.graphite, 40))
        }

        crystalFluidMixer = GenericCrafter("crystal-fluid-mixer").apply {
            size = 3
            health = 440
            craftTime = 75f
            outputLiquid = LiquidStack(CWLiquids.crystalFluid, 0.4f)
            itemCapacity = 20
            liquidCapacity = 60f
            hasItems = true
            hasLiquids = true
            hasPower = true
            outputsLiquid = true
            drawer = DrawMulti(
                DrawDefault(),
                DrawLiquidTile(CWLiquids.crystalFluid),
                DrawGlowRegion().apply {
                    color = Color.valueOf("65C3E8")
                    alpha = 0.7f
                }
            )
            consumeItems(*ItemStack.with(CWItems.crystal, 2))
            consumeLiquid(Liquids.water, 0.5f)
            consumePower(2.0f)
            requirements(Category.crafting, ItemStack.with(Items.copper, 100, Items.lead, 70, CWItems.crystal, 25))
        }

        crystalBattery = Battery("crystal-battery").apply {
            size = 2
            health = 640
            conductivePower = true
            consumePowerBuffered(12000f)
            drawer = DrawMulti(
                DrawDefault(),
                DrawPower().apply {
                    emptyLightColor = Color.valueOf("4A90D9")
                    fullLightColor = Color.valueOf("65C3E8")
                },
                mindustry.world.draw.DrawRegion("-top"),
                DrawGlowRegion().apply {
                    color = Color.valueOf("65C3E8")
                    alpha = 0.6f
                }
            )
            requirements(Category.power, ItemStack.with(CWItems.crystal, 50, Items.lead, 80, Items.silicon, 40, Items.graphite, 30))
        }

        crystalGenerator = ConsumeGenerator("crystal-generator").apply {
            size = 2
            health = 640
            powerProduction = 3.0f
            itemDuration = 90f
            warmupSpeed = 0.04f
            effectChance = 0.05f
            generateEffect = Fx.generatespark
            generateEffectRange = 2f
            baseLightRadius = 50f
            consumeItems(*ItemStack.with(CWItems.crystalAlloy, 1))
            drawer = DrawMulti(
                DrawDefault(),
                DrawGlowRegion().apply {
                    color = Color.valueOf("65C3E8")
                    alpha = 0.7f
                },
                mindustry.world.draw.DrawRegion("-top")
            )
            requirements(Category.power, ItemStack.with(CWItems.crystal, 40, Items.lead, 60, Items.silicon, 30, Items.graphite, 20))
        }

        crystalPowerNode = PowerNode("crystal-power-node").apply {
            size = 1
            health = 180
            maxNodes = 12
            laserRange = 9f
            laserColor1 = Color.valueOf("B8F0FF")
            laserColor2 = Color.valueOf("65C3E8")
            conductivePower = true
            underBullets = true
            crushFragile = true
            requirements(Category.power, ItemStack.with(CWItems.crystal, 8, Items.lead, 12, Items.silicon, 6))
        }

        crystalBeamTower = BeamNode("crystal-beam-tower").apply {
            size = 2
            health = 720
            range = 18
            consumesPower = true
            outputsPower = true
            conductivePower = true
            laserWidth = 0.55f
            laserColor1 = Color.valueOf("B8F0FF")
            laserColor2 = Color.valueOf("65C3E8")
            pulseScl = 18f
            pulseMag = 0.12f
            underBullets = true
            consumePowerBuffered(18000f)
            requirements(Category.power, ItemStack.with(CWItems.crystalAlloy, 45, CWItems.crystal, 80, Items.silicon, 55, Items.graphite, 35))
        }

        crystalConduit = Conduit("crystal-conduit").apply {
            health = 200
            size = 1
            liquidCapacity = 20f
            leaks = false
            botColor = Color.valueOf("4A90D9")
            requirements(Category.liquid, ItemStack.with(CWItems.crystal, 10, Items.lead, 10))
        }

        crystalBridgeConduit = LiquidBridge("crystal-bridge-conduit").apply {
            size = 1
            health = 200
            range = 10
            hasPower = false
            liquidCapacity = 20f
            requirements(Category.liquid, ItemStack.with(CWItems.crystal, 15, Items.lead, 20, Items.graphite, 10))
        }

        crystalLiquidJunction = LiquidJunction("crystal-liquid-junction").apply {
            size = 1
            health = 120
            liquidCapacity = 12f
            requirements(Category.liquid, ItemStack.with(CWItems.crystal, 8, Items.lead, 10))
        }

        crystalTurret = ItemTurret("crystal-turret").apply {
            size = 2
            health = 960
            reload = 35f
            range = 210f
            maxAmmo = 60
            recoilTime = 10f
            recoil = 1.5f
            shootSound = Sounds.shootLaser
            ammoPerShot = 2
            targetGround = true
            targetAir = true
            inaccuracy = 2f
            rotateSpeed = 8f
            consumePower(2.5f)
            ammo(CWItems.crystal, BasicBulletType(7.0f, 28f).apply {
                lifetime = 30f
                width = 8f
                height = 12f
                ammoMultiplier = 3f
                status = CWStatusEffects.crystalShock
                statusDuration = 120f
                hitEffect = Fx.hitLancer
                despawnEffect = Fx.hitLancer
                frontColor = Color.valueOf("65C3E8")
                backColor = Color.valueOf("4A8EC7")
                hitColor = Color.valueOf("65C3E8")
                trailLength = 8
                trailWidth = 4f
                trailColor = Color.valueOf("65C3E8")
                lightning = 3
                lightningDamage = 10f
                lightningLength = 15
                lightningLengthRand = 10
            })
            requirements(Category.turret, ItemStack.with(Items.copper, 90, Items.lead, 60, CWItems.crystal, 35, Items.silicon, 40))
        }

        crystalFluidTurret = LiquidTurret("crystal-fluid-turret").apply {
            size = 2
            health = 880
            reload = 10f
            range = 170f
            shootSound = Sounds.shootLaser
            shootEffect = Fx.none
            smokeEffect = Fx.none
            extinguish = false
            targetGround = true
            targetAir = true
            inaccuracy = 3f
            rotateSpeed = 7f
            liquidCapacity = 30f
            consumePower(3.0f)
            ammo(CWLiquids.crystalFluid, BasicBulletType(6.0f, 15f).apply {
                lifetime = 28f
                width = 6f
                height = 10f
                ammoMultiplier = 4f
                frontColor = Color.valueOf("65C3E8")
                backColor = Color.valueOf("4A90D9")
                hitEffect = Fx.hitMeltdown
                despawnEffect = Fx.hitMeltdown
                trailLength = 6
                trailWidth = 3f
                trailColor = Color.valueOf("65C3E8")
                status = StatusEffects.melting
                statusDuration = 60f
            })
            requirements(Category.turret, ItemStack.with(CWItems.crystal, 40, Items.lead, 50, Items.silicon, 30, Items.titanium, 20))
        }

        crystalWall = Wall("crystal-wall").apply {
            size = 1
            health = 840
            lightningChance = 0.08f
            lightningDamage = 12f
            lightningLength = 10
            lightningColor = Color.valueOf("65C3E8")
            flashHit = true
            flashColor = Color.valueOf("B8F0FF")
            requirements(Category.defense, ItemStack.with(CWItems.crystal, 6))
        }

        crystalWallLarge = Wall("crystal-wall-large").apply {
            size = 2
            health = 1680
            lightningChance = 0.12f
            lightningDamage = 18f
            lightningLength = 14
            lightningColor = Color.valueOf("65C3E8")
            flashHit = true
            flashColor = Color.valueOf("B8F0FF")
            requirements(Category.defense, ItemStack.with(CWItems.crystal, 24, Items.lead, 30, Items.graphite, 20))
        }
    }
}
