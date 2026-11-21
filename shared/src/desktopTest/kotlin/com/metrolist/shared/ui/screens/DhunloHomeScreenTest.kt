package com.metrolist.shared.ui.screens

import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.ArtistTiny
import com.metrolist.shared.model.HomeSection
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.PodcastItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.ui.components.formatHomeGreeting
import com.metrolist.shared.ui.components.getTimeBasedGreeting
import com.metrolist.shared.ui.components.heroItemBadgeLabel
import com.metrolist.shared.ui.components.heroItemSubtitle
import com.metrolist.shared.ui.components.selectFeaturedHomeItem
import com.metrolist.shared.viewmodels.HomeViewModel.HomeUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DhunloHomeScreenTest {

    @Test
    fun timeBasedGreetingMatchesHourRanges() {
        assertEquals("Good morning", getTimeBasedGreeting(4))
        assertEquals("Good morning", getTimeBasedGreeting(8))
        assertEquals("Good morning", getTimeBasedGreeting(11))

        assertEquals("Good afternoon", getTimeBasedGreeting(12))
        assertEquals("Good afternoon", getTimeBasedGreeting(14))
        assertEquals("Good afternoon", getTimeBasedGreeting(16))

        assertEquals("Good evening", getTimeBasedGreeting(17))
        assertEquals("Good evening", getTimeBasedGreeting(19))
        assertEquals("Good evening", getTimeBasedGreeting(21))

        assertEquals("Good night", getTimeBasedGreeting(22))
        assertEquals("Good night", getTimeBasedGreeting(23))
        assertEquals("Good night", getTimeBasedGreeting(0))
        assertEquals("Good night", getTimeBasedGreeting(3))
    }

    @Test
    fun formatHomeGreetingIncludesPersonalizedNameWhenAvailable() {
        assertEquals("Good morning, Alex", formatHomeGreeting("Alex", hour = 9))
        assertEquals("Good afternoon, Sarah", formatHomeGreeting("  Sarah  ", hour = 13))
        assertEquals("Good evening", formatHomeGreeting(null, hour = 18))
        assertEquals("Good night", formatHomeGreeting("", hour = 23))
        assertEquals("Good morning", formatHomeGreeting("   ", hour = 8))
    }

    @Test
    fun selectFeaturedHomeItemReturnsNullWhenEmpty() {
        val emptyState = HomeUiState()
        assertNull(selectFeaturedHomeItem(emptyState), "Must return null when no real music data is available (never fabricate fake tracks)")
    }

    @Test
    fun selectFeaturedHomeItemPrioritizesContentSourcesCorrectly() {
        val sampleArtist = listOf(ArtistTiny("art1", "Test Artist"))
        val quickPickSong = SongItem("qp1", "Quick Pick Track", null, sampleArtist, null, 180L)
        val trendingSong = SongItem("tr1", "Trending Track", null, sampleArtist, null, 200L)
        val newAlbum = AlbumItem("alb1", "New Release Album", null, sampleArtist, "2026")
        val chartAlbum = AlbumItem("alb2", "Chart Album", null, sampleArtist, "2026")
        val keepListeningSong = SongItem("kl1", "Keep Listening Track", null, sampleArtist, null, 210L)
        val sectionItem = SongItem("sec1", "Section Track", null, sampleArtist, null, 190L)

        // 1. Quick picks priority
        val stateWithAll =
            HomeUiState(
                quickPicks = listOf(quickPickSong),
                trending = listOf(trendingSong),
                newReleases = listOf(newAlbum),
                albumCharts = listOf(chartAlbum),
                keepListening = listOf(keepListeningSong),
            )
        assertEquals(quickPickSong, selectFeaturedHomeItem(stateWithAll))

        // 2. Trending fallback
        val stateWithTrending =
            HomeUiState(
                trending = listOf(trendingSong),
                newReleases = listOf(newAlbum),
            )
        assertEquals(trendingSong, selectFeaturedHomeItem(stateWithTrending))

        // 3. New releases fallback
        val stateWithNewReleases =
            HomeUiState(
                newReleases = listOf(newAlbum),
                albumCharts = listOf(chartAlbum),
            )
        assertEquals(newAlbum, selectFeaturedHomeItem(stateWithNewReleases))

        // 4. Album charts fallback
        val stateWithAlbumCharts =
            HomeUiState(
                albumCharts = listOf(chartAlbum),
                keepListening = listOf(keepListeningSong),
            )
        assertEquals(chartAlbum, selectFeaturedHomeItem(stateWithAlbumCharts))

        // 5. Keep listening fallback
        val stateWithKeepListening =
            HomeUiState(
                keepListening = listOf(keepListeningSong),
            )
        assertEquals(keepListeningSong, selectFeaturedHomeItem(stateWithKeepListening))

        // 6. Section items fallback
        val stateWithSection =
            HomeUiState(
                sections = listOf(HomeSection("sec_title", listOf(sectionItem))),
            )
        assertEquals(sectionItem, selectFeaturedHomeItem(stateWithSection))
    }

    @Test
    fun heroItemMetadataAndBadgesFormatAccurately() {
        val sampleArtist = listOf(ArtistTiny("art1", "Dhunlo Artist"))
        val song = SongItem("s1", "Lunar Drift", null, sampleArtist, null, 240L)
        val album = AlbumItem("a1", "Obsidian Waves", null, sampleArtist, "2026")
        val playlist = PlaylistItem("p1", "Night Drive", null, "Curator", "50 songs")
        val podcast = PodcastItem("pod1", "Audio Tech", null, "Dhunlo Media", "12 episodes")

        assertEquals("SPOTLIGHT • FEATURED TRACK", heroItemBadgeLabel(song))
        assertEquals("SPOTLIGHT • FEATURED ALBUM", heroItemBadgeLabel(album))
        assertEquals("SPOTLIGHT • FEATURED PLAYLIST", heroItemBadgeLabel(playlist))
        assertEquals("SPOTLIGHT • PODCAST", heroItemBadgeLabel(podcast))

        assertEquals("Dhunlo Artist", heroItemSubtitle(song))
        assertEquals("Dhunlo Artist", heroItemSubtitle(album))
        assertEquals("Curator", heroItemSubtitle(playlist))
        assertEquals("Dhunlo Media", heroItemSubtitle(podcast))
    }
}
