package com.example.easydrive.dades

data class GeoapifyDades(
    val address_line1: String,
    val address_line2: String,
    val bbox: Bbox,
    val city: String,
    val country: String,
    val country_code: String,
    val county: String,
    val county_code: String,
    val formatted: String,
    val iso3166_2: String,
    val iso3166_2_sublevel: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val housenumber:String,
    val place_id: String,
    val plus_code: String,
    val plus_code_short: String,
    val postcode: String,
    val rank: Rank,
    val region: String,
    val result_type: String,
    val state: String,
    val street: String,
    val timezone: Timezone
)

data class Bbox(
    val lat1: Double,
    val lat2: Double,
    val lon1: Double,
    val lon2: Double
)

data class Rank(
    val confidence: Int,
    val confidence_street_level: Int,
    val importance: Double,
    val match_type: String,
    val popularity: Double
)

data class Timezone(
    val abbreviation_DST: String,
    val abbreviation_STD: String,
    val name: String,
    val offset_DST: String,
    val offset_DST_seconds: Int,
    val offset_STD: String,
    val offset_STD_seconds: Int
)