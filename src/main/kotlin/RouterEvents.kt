import arc.math.geom.Point2
import arc.struct.Seq
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.world.Block

object RouterEvents {

    val events = Seq<RouterEvent>()

    private val around = arrayOf(Point2(0, 0), Point2(0, 1), Point2(1, 0), Point2(1, 1), Point2(0, -1), Point2(-1, 0), Point2(-1, -1), Point2(1, -1), Point2(-1, 1))

    init {
        events.addAll(
            object : RouterEvent {
                override val message = "items"
                override fun create(pos: Point2) {
                    Call.setTile(Vars.world.tile(pos.x, pos.y), Blocks.vault, Team.derelict, 0)
                    Call.effect(Fx.upgradeCore, (Vars.tilesize * pos.x).toFloat(), (Vars.tilesize * pos.y).toFloat(), Blocks.vault.size.toFloat(), Team.derelict.color)
                    try {
                        Call.setItem(Vars.world.build(pos.x, pos.y), Vars.content.items().random(), Blocks.vault.itemCapacity)
                    } catch (e: Exception) {}
                }
            },
            object : RouterEvent {
                override val message = "boom"
                override fun create(pos: Point2) {
                    Call.setTile(Vars.world.tile(pos.x, pos.y), Blocks.thoriumReactor, Team.derelict, 0)
                    try {
                        Call.setItem(Vars.world.build(pos.x, pos.y), Items.thorium, Blocks.thoriumReactor.itemCapacity)
                    } catch (e: Exception) {}
                }
            },
            object : RouterEvent {
                val ores = Seq<Block>().apply { addAll(Blocks.oreCoal, Blocks.oreCopper, Blocks.oreLead, Blocks.oreScrap, Blocks.oreThorium, Blocks.oreTitanium) }
                override val message = "ores"
                override fun create(pos: Point2) {
                    val ore = ores.random()
                    Vars.world.tile(pos.x, pos.y).let {
                        for (point in around) {
                            Call.setFloor(it.nearby(point), it.nearby(point).floor(), ore)
                        }
                    }
                }
            },
            object : RouterEvent {
                override val message = "floor is lava"
                override fun create(pos: Point2) {
                    Vars.world.tile(pos.x, pos.y).let {
                        for (point in around) {
                            Call.setFloor(it.nearby(point), Blocks.magmarock, it.nearby(point).overlay())
                        }
                    }
                }
            },
            object : RouterEvent {
                override val message = "floor is water"
                override fun create(pos: Point2) {
                    Vars.world.tile(pos.x, pos.y).let {
                        for (point in around) {
                            Call.setFloor(it.nearby(point), Blocks.water, Blocks.air)
                        }
                    }
                }
            },
            object : RouterEvent {
                override val message = "floor is sand"
                override fun create(pos: Point2) {
                    Vars.world.tile(pos.x, pos.y).let {
                        for (point in around) {
                            Call.setFloor(it.nearby(point), Blocks.darksand, it.nearby(point).overlay())
                        }
                    }
                }
            },
            object : RouterEvent {
                override val message = "death"
                override fun create(pos: Point2) {
                    Vars.content.units().random().spawn(Vars.state.teams.active.random().team, (Vars.tilesize * pos.x).toFloat(), (Vars.tilesize * pos.y).toFloat())
                }
            },
        )
    }

    fun getRandomEvent() = events.random()
}