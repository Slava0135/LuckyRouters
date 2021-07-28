import arc.math.geom.Point2

interface RouterEvent {
    val message: String
    fun create(pos: Point2)
}