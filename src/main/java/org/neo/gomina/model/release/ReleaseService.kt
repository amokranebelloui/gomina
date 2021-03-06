package org.neo.gomina.model.release

import org.neo.gomina.model.event.Event
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.version.Version
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object ReleaseService {

    fun releaseDates(log: List<Commit>, releaseEvents: List<Event>): MutableMap<Commit, LocalDateTime> {
        val commitReleaseDates = mutableMapOf<Commit, LocalDateTime>()
        val sortedReleases = releaseEvents.sortedByDescending { it.timestamp }
        sortedReleases.forEach { release ->
            var foundVersion = false
            log.forEach { commit ->
                if (commit.version != null && Version.isStable(commit.version) && commit.version == release.version) {
                    foundVersion = true
                }
                if (foundVersion) {
                    commitReleaseDates[commit] = release.timestamp
                }
            }
        }
        return commitReleaseDates
    }

    fun commitToRelease(log: List<Commit>, releaseEvents: List<Event>): Int? {
        return if (releaseEvents.isNotEmpty()) {
            val releaseDates = releaseDates(log, releaseEvents).mapKeys { (commit, releaseDate) -> commit.revision }
            log
                .fold(0 to 0) { (sum, count), commit ->
                    val releaseDate = releaseDates[commit.revision] ?: LocalDateTime.now(ZoneOffset.UTC)
                    sum + ChronoUnit.DAYS.between(commit.date, releaseDate).toInt() to count + 1
                }
                .let { (sum, count) ->
                    if (count > 0) sum / count else null
                }
        }
        else null
    }

}