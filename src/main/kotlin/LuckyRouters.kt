import arc.Events
import arc.math.geom.Point2
import arc.struct.Seq
import arc.util.Align
import arc.util.Timer
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.content.Fx
import mindustry.entities.Units
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.mod.Plugin
import mindustry.world.Block
import java.util.*

class LuckyRouters : Plugin() {

    val random = Random()
    lateinit var router: Block

    val activeRouters = Seq<Point2>()

    val spawnInterval: Float
    val routerHealth: Float
    val effectUpdateInterval: Float

    val safeArea = 3

    init {
        spawnInterval = getProp("spawnInterval").toFloat()
        effectUpdateInterval = getProp("effectUpdateInterval").toFloat()
        routerHealth = getProp("routerHealth").toFloat()
    }

    @Suppress("UNCHECKED_CAST")
    fun getProp(key: String): String {
        val props  = javaClass.classLoader.getResourceAsStream("config.properties").use {
            Properties().apply { load(it) }
        }
        return (props.getProperty(key)) ?: throw RuntimeException("Could not find property $key")
    }

    override fun init() {

        router = Blocks.router

        Timer.schedule({
            if (Vars.state.isPlaying && !Vars.state.serverPaused) {
                spawnRouter(getPosition())
            }
        }, 0f, spawnInterval)

        Timer.schedule({
            for (pos in activeRouters) {
                Vars.world.tile(pos.x, pos.y).let {
                    if (it.build == null || it.build.block != router || it.build.team != Team.derelict) {
                        activeRouters.remove(pos)
                        spawnEvent(pos)
                    } else {
                        Call.label("[gold]?[]", effectUpdateInterval, (Vars.tilesize * pos.x).toFloat(), (Vars.tilesize * pos.y).toFloat())
                    }
                }
            }
        }, 0f, effectUpdateInterval)

        Events.on(EventType.BlockDestroyEvent::class.java) { e ->
            if (e.tile.build.block == router && e.tile.build.team == Team.derelict) {
                Point2(e.tile.x.toInt(), e.tile.y.toInt()).let {
                    activeRouters.remove(it)
                }
            }
        }

        Events.on(EventType.WorldLoadEvent::class.java) { event ->
            activeRouters.clear()
            for (tile in Vars.world.tiles) {
                tile.build?.let {
                    if (it.block == router && it.team == Team.derelict) activeRouters.add(Point2(tile.x.toInt(), tile.y.toInt()))
                }
            }
        }
    }

    private tailrec fun getPosition(): Point2 {
        val pos = generateRandomPosition()
        return if (isValidPosition(pos)) pos else getPosition()
    }

    private fun generateRandomPosition() = Point2(random.nextInt(Vars.world.width()), random.nextInt(Vars.world.height()))

    private fun isValidPosition(pos: Point2): Boolean {

        if ((router.solid || router.solidifes)
            && Units.anyEntities(
                pos.x * Vars.tilesize + router.offset - safeArea * Vars.tilesize/2f,
                pos.y * Vars.tilesize + router.offset - safeArea * Vars.tilesize/2f,
                (safeArea * Vars.tilesize).toFloat(), (safeArea * Vars.tilesize).toFloat()))
            return false

        val tile = Vars.world.tile(pos.x, pos.y) ?: return false;

        val offsetx = -(safeArea - 1) / 2;
        val offsety = -(safeArea - 1) / 2;

        for (dx in 0 until safeArea) {
            for (dy in 0 until safeArea){

                val wx = dx + offsetx + tile.x
                val wy = dy + offsety + tile.y

                val check = Vars.world.tile(wx, wy);

                if (check == null || (check.floor().isDeep) || check.solid() || check.build != null || !check.floor().placeableOn) return false;
            }
        }

        return true;
    }

    private fun spawnRouter(pos: Point2) {
        Call.setTile(Vars.world.tile(pos.x, pos.y), router, Team.derelict, 0)
        activeRouters.add(pos)
        Call.effect(Fx.upgradeCore, (Vars.tilesize * pos.x).toFloat(), (Vars.tilesize * pos.y).toFloat(), router.size.toFloat(), Team.derelict.color)
        Call.infoPopup("[gold]Lucky router[] appeared at ${pos.x}, ${pos.y}!", 5f, Align.bottom, 0, 0, 0, 0)
        Vars.world.build(pos.x, pos.y)?.health = routerHealth
    }

    private fun spawnEvent(pos: Point2) {
        RouterEvents.getRandomEvent().let{
            it.create(pos)
            Call.label(it.message, 3f, (Vars.tilesize * pos.x).toFloat(), (Vars.tilesize * pos.y).toFloat())
        }
    }
}