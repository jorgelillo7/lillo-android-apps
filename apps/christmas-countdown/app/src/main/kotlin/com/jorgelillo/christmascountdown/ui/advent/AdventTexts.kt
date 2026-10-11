package com.jorgelillo.christmascountdown.ui.advent

import android.content.res.Resources
import androidx.annotation.StringRes
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.domain.AdventActivity
import com.jorgelillo.christmascountdown.domain.AdventDoor

/** The text of each activity. Exhaustive: a new AdventActivity doesn't compile until it has one. */
@StringRes
fun AdventActivity.text(): Int = when (this) {
    AdventActivity.LETTER -> R.string.advent_letter
    AdventActivity.PAPER_SNOWFLAKES -> R.string.advent_paper_snowflakes
    AdventActivity.NATIVITY_OR_TREE -> R.string.advent_nativity_or_tree
    AdventActivity.DOOR_GARLAND -> R.string.advent_door_garland
    AdventActivity.PAPER_CHAIN -> R.string.advent_paper_chain
    AdventActivity.STOCKING -> R.string.advent_stocking
    AdventActivity.TREE_ORNAMENT -> R.string.advent_tree_ornament
    AdventActivity.WINDOW_LIGHTS -> R.string.advent_window_lights
    AdventActivity.PAPER_STARS -> R.string.advent_paper_stars
    AdventActivity.GLITTER_PINECONES -> R.string.advent_glitter_pinecones
    AdventActivity.TABLE_WREATH -> R.string.advent_table_wreath
    AdventActivity.MOVIE_NIGHT -> R.string.advent_movie_night
    AdventActivity.COOKIES -> R.string.advent_cookies
    AdventActivity.CHRISTMAS_CARDS -> R.string.advent_christmas_cards
    AdventActivity.CHOCOLATE_CHURROS -> R.string.advent_chocolate_churros
    AdventActivity.DRAWING_GIFT -> R.string.advent_drawing_gift
    AdventActivity.SING_A_CAROL -> R.string.advent_sing_a_carol
    AdventActivity.SOCK_SNOWMAN -> R.string.advent_sock_snowman
    AdventActivity.JAR_LANTERN -> R.string.advent_jar_lantern
    AdventActivity.FAMILY_STORY -> R.string.advent_family_story
    AdventActivity.TREE_PIZZA -> R.string.advent_tree_pizza
    AdventActivity.SNOW_GLOBE -> R.string.advent_snow_globe
    AdventActivity.POLVORONES -> R.string.advent_polvorones
    AdventActivity.DONATE -> R.string.advent_donate
    AdventActivity.LIGHTS_WALK -> R.string.advent_lights_walk
    AdventActivity.BOARD_GAMES -> R.string.advent_board_games
    AdventActivity.SECRET_SANTA -> R.string.advent_secret_santa
    AdventActivity.CHRISTMAS_MARKET -> R.string.advent_christmas_market
    AdventActivity.UGLY_SWEATERS -> R.string.advent_ugly_sweaters
    AdventActivity.PARADE_OR_CONCERT -> R.string.advent_parade_or_concert
    AdventActivity.ICE_SKATING -> R.string.advent_ice_skating
    AdventActivity.VISIT_NATIVITY -> R.string.advent_visit_nativity
    AdventActivity.FAMILY_PHOTO -> R.string.advent_family_photo
    AdventActivity.CAROL_KARAOKE -> R.string.advent_carol_karaoke
    AdventActivity.CHRISTMAS_EVE -> R.string.advent_christmas_eve
}

/** What a door says, as one line (for the share text). */
fun Resources.doorText(door: AdventDoor): String = when (door) {
    is AdventDoor.Activity -> getString(door.activity.text())
    is AdventDoor.Song -> getString(R.string.advent_song_text, door.song.title, door.song.artist) + " " + door.song.url
}
