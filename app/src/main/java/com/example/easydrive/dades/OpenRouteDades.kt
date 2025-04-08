package com.example.easydrive.dades

data class OpenRouteDades(
    val bbox: List<Double>,
    val features: List<Feature>,
    val metadata: Metadata,
    val type: String
)
data class Feature(
    val bbox: List<Double>,
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)

data class Metadata(
    val attribution: String,
    val engine: Engine,
    val query: Query,
    val service: String,
    val timestamp: Long
)

data class Geometry(
    val coordinates: List<List<Double>>,
    val type: String
)

data class Engine(
    val build_date: String,
    val graph_date: String,
    val version: String
)

data class Properties(
    val segments: List<Segment>,
    val summary: Summary,
    val way_points: List<Int>
)

data class Query(
    val coordinates: List<List<Double>>,
    val format: String,
    val profile: String,
    val profileName: String
)

data class Segment(
    val distance: Double,
    val duration: Double,
    val steps: List<Step>
)

data class Step(
    val distance: Double,
    val duration: Double,
    val instruction: String,
    val name: String,
    val type: Int,
    val way_points: List<Int>
)

data class Summary(
    val distance: Double,
    val duration: Double
)
