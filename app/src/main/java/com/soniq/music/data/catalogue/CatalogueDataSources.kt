package com.soniq.music.data.catalogue

import com.soniq.music.database.dao.CatalogueDao
import com.soniq.music.database.dao.RecentSearchDao
import com.soniq.music.database.entity.RecentSearchEntity
import com.soniq.music.database.entity.toDomain
import com.soniq.music.database.entity.toEntity
import com.soniq.music.domain.model.Album
import com.soniq.music.domain.model.Artist
import com.soniq.music.domain.model.Genre
import com.soniq.music.domain.model.RecentSearchQuery
import com.soniq.music.domain.model.Track
import com.soniq.music.network.SupabaseClientProvider
import com.soniq.music.network.dto.AlbumDto
import com.soniq.music.network.dto.ArtistDto
import com.soniq.music.network.dto.GenreDto
import com.soniq.music.network.dto.TrackDto
import com.soniq.music.network.dto.toDomain
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class RemoteCataloguePayload(
    val genres: List<Genre>,
    val artists: List<Artist>,
    val albums: List<Album>,
    val tracks: List<Track>
)

interface CatalogueRemoteDataSource {
    val isConfigured: Boolean
    suspend fun fetchAuthorizedCatalogue(): RemoteCataloguePayload
}

interface CatalogueLocalDataSource {
    fun observeStarterTracks(): Flow<List<Track>>
    fun observeAllTracks(): Flow<List<Track>>
    fun observeArtists(): Flow<List<Artist>>
    fun observeAlbums(): Flow<List<Album>>
    fun observeGenres(): Flow<List<Genre>>
    fun observeRecentSearches(): Flow<List<RecentSearchQuery>>

    suspend fun getAllTracksOnce(): List<Track>
    suspend fun getAllArtistsOnce(): List<Artist>
    suspend fun getAllAlbumsOnce(): List<Album>
    suspend fun getAllGenresOnce(): List<Genre>

    suspend fun saveCatalogue(
        genres: List<Genre>,
        artists: List<Artist>,
        albums: List<Album>,
        tracks: List<Track>
    )

    suspend fun recordRecentSearch(query: String, timestampMs: Long)
    suspend fun clearRecentSearches()
}

@Singleton
class SupabaseCatalogueRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : CatalogueRemoteDataSource {

    override val isConfigured: Boolean
        get() = clientProvider.isConfigured

    override suspend fun fetchAuthorizedCatalogue(): RemoteCataloguePayload {
        val client = requireNotNull(clientProvider.client) { "Supabase is not configured." }
        val genreDtos = client.postgrest.from("genres").select().decodeList<GenreDto>()
        val artistDtos = client.postgrest.from("artists").select().decodeList<ArtistDto>()
        val albumDtos = client.postgrest.from("albums").select().decodeList<AlbumDto>()
        val trackDtos = client.postgrest.from("tracks").select().decodeList<TrackDto>()

        val genres = genreDtos.map { it.toDomain() }
        val genreNamesById = genres.associate { it.id to it.name }
        val artists = artistDtos.map { it.toDomain() }
        val artistNamesById = artists.associate { it.id to it.name }
        val albums = albumDtos.map { dto ->
            dto.toDomain(artistName = artistNamesById[dto.artistId].orEmpty())
        }
        val albumTitlesById = albums.associate { it.id to it.title }
        val tracks = trackDtos.map { dto ->
            dto.toDomain(
                artistName = artistNamesById[dto.artistId].orEmpty(),
                albumTitle = dto.albumId?.let { albumTitlesById[it] }.orEmpty(),
                categoryName = dto.genreId?.let { genreNamesById[it] }.orEmpty()
            )
        }

        return RemoteCataloguePayload(
            genres = genres,
            artists = artists,
            albums = albums,
            tracks = tracks
        )
    }
}

@Singleton
class RoomCatalogueLocalDataSource @Inject constructor(
    private val catalogueDao: CatalogueDao,
    private val recentSearchDao: RecentSearchDao
) : CatalogueLocalDataSource {

    override fun observeStarterTracks(): Flow<List<Track>> =
        catalogueDao.observeStarterTracks().map { list -> list.map { it.toDomain() } }

    override fun observeAllTracks(): Flow<List<Track>> =
        catalogueDao.observeAllTracks().map { list -> list.map { it.toDomain() } }

    override fun observeArtists(): Flow<List<Artist>> =
        catalogueDao.observeArtists().map { list -> list.map { it.toDomain() } }

    override fun observeAlbums(): Flow<List<Album>> =
        catalogueDao.observeAlbums().map { list -> list.map { it.toDomain() } }

    override fun observeGenres(): Flow<List<Genre>> =
        catalogueDao.observeGenres().map { list -> list.map { it.toDomain() } }

    override fun observeRecentSearches(): Flow<List<RecentSearchQuery>> =
        recentSearchDao.observeRecentSearches().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllTracksOnce(): List<Track> =
        catalogueDao.getAllTracksOnce().map { it.toDomain() }

    override suspend fun getAllArtistsOnce(): List<Artist> =
        catalogueDao.getAllArtistsOnce().map { it.toDomain() }

    override suspend fun getAllAlbumsOnce(): List<Album> =
        catalogueDao.getAllAlbumsOnce().map { it.toDomain() }

    override suspend fun getAllGenresOnce(): List<Genre> =
        catalogueDao.getAllGenresOnce().map { it.toDomain() }

    override suspend fun saveCatalogue(
        genres: List<Genre>,
        artists: List<Artist>,
        albums: List<Album>,
        tracks: List<Track>
    ) {
        if (genres.isNotEmpty()) catalogueDao.upsertGenres(genres.map { it.toEntity() })
        if (artists.isNotEmpty()) catalogueDao.upsertArtists(artists.map { it.toEntity() })
        if (albums.isNotEmpty()) catalogueDao.upsertAlbums(albums.map { it.toEntity() })
        if (tracks.isNotEmpty()) catalogueDao.upsertTracks(tracks.map { it.toEntity() })
    }

    override suspend fun recordRecentSearch(query: String, timestampMs: Long) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        recentSearchDao.upsert(
            RecentSearchEntity(
                normalizedQuery = trimmed.lowercase(),
                displayQuery = trimmed,
                searchedAtEpochMs = timestampMs
            )
        )
    }

    override suspend fun clearRecentSearches() {
        recentSearchDao.clearAll()
    }
}
