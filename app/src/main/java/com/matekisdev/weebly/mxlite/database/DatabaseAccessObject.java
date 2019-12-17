package com.matekisdev.weebly.mxlite.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public interface DatabaseAccessObject
{
    @Query("SELECT * FROM MP4")
    List<Mp4FileEntity> getEveryMp4File();

    @Query("SELECT * FROM MP4 WHERE PATH=:path")
    Mp4FileEntity getMp4FileByPath(String path);

    @Insert(onConflict = IGNORE)
    void insertMp4File(Mp4FileEntity... mp4FileEntity);

    @Delete
    void deleteMp4File(Mp4FileEntity... mp4FileEntity);

    ////////////////////////////////////////////////////////

    @Query("SELECT * FROM RECENTLY_OPENED")
    List<RecentFileEntity> getEveryRecentMp4File();

    @Query("INSERT INTO RECENTLY_OPENED VALUES (NULL, :path, :timestamp)")
    void insertRecentMp4FileByPathAndTs(String path, String timestamp);

    @Query("DELETE FROM RECENTLY_OPENED WHERE PATH=:path")
    void deleteRecentMp4FileByPath(String path);
}
