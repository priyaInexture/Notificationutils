package com.location.locationutills

import com.chibatching.kotpref.KotprefModel

/**
 * Created by Android on 2/15/2018.
 */
object AppPreference : KotprefModel() {
    var lat by stringPref()
    var long by stringPref()
    var location by stringPref()
}
